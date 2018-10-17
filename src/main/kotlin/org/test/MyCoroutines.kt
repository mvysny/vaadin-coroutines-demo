package org.test

import com.vaadin.server.ErrorEvent
import com.vaadin.ui.UI
import kotlinx.coroutines.experimental.*
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Response
import kotlin.coroutines.experimental.CoroutineContext

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
        cont.invokeOnCancellation { if (!f.isDone) f.cancel(true) }
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
 * Launches a coroutine in Vaadin UI thread in the scope of given [ui] (or the current one if none specified).
 *
 * All exceptions are caught and sent to the Vaadin [UI.errorHandler] - they are NOT propagated upwards and do not cancel parent job.
 */
fun CoroutineScope.launchVaadin(ui: UI = UI.getCurrent(), block: suspend () -> Unit): Job = launch(VaadinDispatcher(ui)) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        ui.access {
            if (ui.errorHandler != null) {
                ui.errorHandler.error(ErrorEvent(t))
            } else {
                throw t
            }
        }
    }
}
