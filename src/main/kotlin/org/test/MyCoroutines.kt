package org.test

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.ErrorEvent
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.VaadinSession
import kotlinx.coroutines.*
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun checkUIThread() {
    require(UI.getCurrent() != null) { "Not running in Vaadin UI thread" }
}

/**
 * Asynchronously processes given request and returns the response body. Fails if the server returns anything but 200.
 * @return the response body
 */
suspend fun BoundRequestBuilder.async(): String =
    suspendCancellableCoroutine { cont: CancellableContinuation<String> ->
        val f: ListenableFuture<Response> = setFollowRedirect(true).execute(object : AsyncCompletionHandler<Response>() {

            @Throws(Exception::class)
            override fun onCompleted(response: Response): Response {
                if (response.statusCode != 200) {
                    cont.resumeWithException(RuntimeException("Got failure response: ${response.statusCode} ${response.statusText}"))
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
        ui.access { block.run() }
    }
}

/**
 * If the coroutine fails, redirect the exception to the Vaadin Error Handler
 * (the [VaadinSession.errorHandler] if set; if not,
 * the handler will simply rethrow the exception).
 */
private data class VaadinExceptionHandler(val ui: UI) : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // send the exception to Vaadin
        ui.access {
            val errorHandler: ErrorHandler? = VaadinSession.getCurrent().errorHandler
            if (errorHandler != null) {
                errorHandler.error(ErrorEvent(exception))
            } else {
                throw exception
            }
        }
    }
}

/**
 * Provides the Vaadin Coroutine context for given [ui] (or the current one if none specified).
 */
fun vaadin(ui: UI = UI.getCurrent()): CoroutineContext = VaadinDispatcher(ui) + VaadinExceptionHandler(ui)
