package org.test

import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.horizontalLayout
import com.github.vok.karibudsl.label
import com.github.vok.karibudsl.verticalLayout
import com.vaadin.ui.UI
import com.vaadin.ui.Window
import kotlinx.coroutines.experimental.*
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import kotlin.coroutines.experimental.CoroutineContext

/**
 * A simple confirmation dialog.
 * @property response invoked with the user's response: true if the user pressed yes, false if the user pressed no or closed the dialog.
 */
class ConfirmDialog(message: String, private val response: (confirmed: Boolean) -> Unit) : Window() {
    init {
        caption = "Confirm"; center(); isResizable = true; isModal = false
        val registration = addCloseListener({ _ -> response(false) })
        verticalLayout {
            label(message)
            horizontalLayout {
                button("Yes", { registration.remove(); close(); response(true) })
                button("No", { registration.remove(); close(); response(false) })
            }
        }
    }

    fun show() {
        UI.getCurrent().addWindow(this)
    }
}

fun checkUIThread() {
    require(UI.getCurrent() != null) { "Not running in Vaadin UI thread" }
}

/**
 * Asynchronously processes given request and returns the response body. Fails if the server returns anything but 200.
 * @return the response body
 */
suspend fun BoundRequestBuilder.async(): String =
    suspendCancellableCoroutine { cont: CancellableContinuation<String> ->
        val f = setFollowRedirect(true).execute(object : AsyncCompletionHandler<Response>() {

            @Throws(Exception::class)
            override fun onCompleted(response: Response): Response {
                if (response.statusCode !in 200..299) {
                    cont.resumeWithException(RuntimeException("Request failed: ${response.statusCode} ${response.statusText}"))
                } else {
                    cont.resume(response.responseBody)
                }
                return response
            }

            override fun onThrowable(t: Throwable) {
                cont.resumeWithException(t)
            }
        })
        cont.invokeOnCompletion { if (!f.isDone) f.cancel(true) }
    }

/**
 * Checks whether a reservation is still valid. See [ReservationsRest] for the server dummy implementation.
 */
suspend fun isReservationValid(): Boolean {
    val response = DefaultAsyncHttpClient().prepareGet("http://localhost:8080/rest/reservations/status").async()
    return response == "valid"
}

suspend fun cancelReservation() {
    DefaultAsyncHttpClient().preparePost("http://localhost:8080/rest/reservations/cancel").async()
}

/**
 * Opens a confirmation dialog and suspends; resumes when the dialog is closed or a button is clicked inside of the dialog.
 * Supports cancelation - closes the dialog automatically.
 * @return true if the user pressed yes, false if the user pressed no or closed the dialog.
 */
suspend fun confirmDialog(message: String = "Are you sure?"): Boolean {
    return suspendCancellableCoroutine { cont: CancellableContinuation<Boolean> ->
        checkUIThread()
        val dlg = ConfirmDialog(message, { response -> cont.resume(response) })
        dlg.show()
        cont.invokeOnCompletion { checkUIThread(); dlg.close() }
    }
}

/**
 * Implements [CoroutineDispatcher] on top of Vaadin [UI] and makes sure that all coroutine code runs in the UI thread.
 * Actions done in the UI thread are then automatically pushed by Vaadin Push to the browser.
 */
private data class VaadinDispatcher(val ui: UI) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        ui.access(block)
    }
}

/**
 * If the coroutine fails, redirect the exception to the Vaadin Error Handler (the [UI.errorHandler] if specified; if not,
 * Vaadin will just log the exception).
 */
private data class VaadinExceptionHandler(val ui: UI) : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // ignore CancellationException (they are normal means to terminate a coroutine)
        if (exception is CancellationException) return
        // try cancel job in the context
        context[Job]?.cancel(exception)
        // send the exception to Vaadin
        ui.access { throw exception }
    }
}

/**
 * Provides the Vaadin Coroutine context for given [ui] (or the current one if none specified).
 */
fun vaadin(ui: UI = UI.getCurrent()) = VaadinDispatcher(ui) + VaadinExceptionHandler(ui)
