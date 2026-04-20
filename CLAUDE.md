# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Demo of using Kotlin coroutines inside a Vaadin Flow app. The app is built on top of [Vaadin Boot](https://github.com/mvysny/vaadin-boot) (embedded Jetty + `main()`, no Spring) and [Karibu-DSL](https://github.com/mvysny/karibu-dsl). Companion blog post: <https://mvysny.github.io/vaadin-and-kotlin-coroutines/>.

## Commands

Requires JDK 21+. All commands run via the Gradle wrapper.

- Build everything (dev mode): `./gradlew build` (also the default when invoking `./gradlew` with no args — `defaultTasks("clean", "build")`).
- Run the app locally: `./gradlew run` → <http://localhost:8080/>. Entry point: `org.test.MainKt`.
- Run all tests: `./gradlew test`.
- Run a single test class: `./gradlew test --tests 'org.test.MyUITest'`.
- Run a single test method: `./gradlew test --tests 'org.test.MyUITest.error handler'` (backticked method names need the literal spaces, quoted).
- Production build (bundles frontend, needed for Docker image and `build/distributions/app.tar`): `./gradlew -Pvaadin.productionMode build`. This is what CI and the Dockerfile run.
- Docker: `docker build -t test/vaadin-coroutines-demo:latest .` then `docker run --rm -ti -p8080:8080 test/vaadin-coroutines-demo`.
- Hit the REST endpoints directly: `curl http://localhost:8080/rest`, `/rest/tickets/available`, `POST /rest/tickets/purchase`.

Dependency versions live in `gradle/libs.versions.toml` (version catalog). Bumping Vaadin/Kotlin/Karibu goes there, not in `build.gradle.kts`.

## Architecture

The whole point of the project is the glue between Kotlin coroutines and Vaadin's threading model. Understanding this requires reading four files together.

### Coroutine ↔ Vaadin bridge (`VaadinCoroutineSupport.kt`)

- `VaadinDispatcher` is a `CoroutineDispatcher` that routes every resumption through `UI.access { … }`, so **all coroutine code runs on the Vaadin UI thread** and any UI mutations are auto-pushed to the browser via Vaadin Push (`@Push` on `MyApp`).
- `VaadinExceptionHandler` is a `CoroutineExceptionHandler` that forwards coroutine failures to `VaadinSession.errorHandler` (falling back to rethrow). This is why uncaught exceptions inside `launch { … }` end up in the Notification toast installed by `VaadinInitListener`.
- `vaadin(ui)` composes both into a `CoroutineContext`. Callers combine it with their own `Job`/`SupervisorJob` to form a scope.
- `checkUIThread()` is the assertion used throughout; any suspend function touching Vaadin components should call it on entry.

### Scope lifecycle (`MainRoute.kt`)

- The route itself implements `CoroutineScope`; its context is `vaadin() + SupervisorJob()`.
- **Use `SupervisorJob`, not `Job`.** A child failure under a plain `Job` cancels the parent, which prevents launching further coroutines from the same view. Tests (`VaadinCoroutineSupportTest`, `DialogsTest`) and the comment in `MainRoute` both call this out.
- `onDetach` cancels the supervisor, which cascades cancellation into every running coroutine (including suspended dialogs — see below).
- The per-route `job: Job?` field demonstrates targeted cancellation of a single long-running operation ("Cancel Purchase" button).

### Suspending UI primitives (`Dialogs.kt`)

- `withProgressDialog(message) { block }` opens a progress `Dialog`, runs `block`, then closes it in `finally`. **`block` must call a suspend function** — otherwise Vaadin batches the open+close inside one HTTP request and the dialog never renders. Keep this invariant when adding new dialogs.
- `confirmDialog(message)` is the canonical pattern for turning a callback-style Vaadin dialog into a suspend function: `suspendCancellableCoroutine` + `cont.invokeOnCancellation { dlg.close() }`. That cancellation hook is what lets `supervisorJob.cancel()` in `onDetach` (or the session-close button) tear down open dialogs cleanly.

### REST client (`RestClient.kt`, `MyRestEndpoints.kt`)

- `HttpClient.async(req)` adapts `sendAsync(...)` to suspend via `kotlinx.coroutines.future.await`. Non-200 responses throw — that flow is exercised by the "Test - call non-existing REST" button and the `error handler` test.
- `MyRestServlet` is a Javalin-backed `HttpServlet` registered via `@WebServlet` annotation scanning (Vaadin Boot picks it up). `serviceDurationMs` is mutable so tests can shorten the simulated delay.
- `port` is a top-level mutable `var` in `Main.kt`, set when Vaadin Boot picks a port. Tests overwrite it to point at a separate Jetty instance they stand up themselves.

### Vaadin service init (`VaadinInitListener.kt` + `META-INF/services/...`)

`VaadinInitListener` is registered via the standard Java ServiceLoader file (`src/main/resources/META-INF/services/com.vaadin.flow.server.VaadinServiceInitListener`). It installs the session-wide `ErrorHandler` that surfaces coroutine failures as notifications. `MyApp` (same file) is the `AppShellConfigurator` carrying `@Push` and the Lumo stylesheet — `@Push` is required for the dispatcher's `ui.access` writes to reach the browser without a client-initiated request.

## Testing notes

Karibu-Testing drives the UI without a browser. A few conventions worth knowing when writing or debugging tests:

- `MockVaadin.setup(routes)` in `@BeforeEach`, `tearDown()` in `@AfterEach`. `Routes().autoDiscoverViews("org.test")` is done once per class.
- Coroutines submitted via `launch` don't execute until the UI queue runs. Use `MockVaadin.runUIQueue()` (or `clientRoundtrip()`) to flush. `runUIQueue(propagateExceptionToHandler = true)` makes exceptions reach the session `ErrorHandler`; without it, they surface as `ExecutionException` from `runUIQueue` itself.
- `retry { … }` in `src/test/kotlin/org/test/Utils.kt` loops the UI queue for up to 2s — use it whenever a test depends on an async REST call completing.
- `MyUITest` spins up a real Jetty+Javalin on port 23442 so the app's `HttpClient` can actually hit the REST endpoints. It sets `FakeHttpEnvironment.localPort` and the app-global `port` var to match.
