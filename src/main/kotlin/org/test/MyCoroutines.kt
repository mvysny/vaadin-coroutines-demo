package org.test

import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.horizontalLayout
import com.github.vok.karibudsl.label
import com.github.vok.karibudsl.verticalLayout
import com.vaadin.ui.UI
import com.vaadin.ui.Window
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import kotlin.coroutines.experimental.CoroutineContext

/**
 * A simple confirmation dialog.
 * @property response invoked with the user's response: true if the user pressed yes, false if the user pressed no or closed the dialog.
 */
class ConfirmDialog(private val response: (confirmed: Boolean) -> Unit) : Window() {
    init {
        caption = "Confirm"; center(); isResizable = true; isModal = false
        val registration = addCloseListener({ _ -> response(false) })
        verticalLayout {
            label("Are you sure?")
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

private fun checkUIThread() {
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
                if (response.statusCode != 200) {
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
 * Downloads contents of the www.google.com asynchronously and suspends; when the page is ready returns the page contents.
 * @return the page contents
 */
suspend fun getGoogleCom(): String {
    val asyncHttpClient = DefaultAsyncHttpClient()
    val response = asyncHttpClient.prepareGet("https://www.google.com/").async()
    return response
}

/**
 * Opens a confirmation dialog and suspends; resumes when the dialog is closed or a button is clicked inside of the dialog.
 * Supports cancelation - closes the dialog automatically.
 * @return true if the user pressed yes, false if the user pressed no or closed the dialog.
 */
suspend fun confirmDialog(): Boolean {
    return suspendCancellableCoroutine { cont: CancellableContinuation<Boolean> ->
        checkUIThread()
        val dlg = ConfirmDialog({ response -> cont.resume(response) })
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
        ui.access { throw exception }
    }
}

/**
 * Provides the Vaadin Coroutine context for given [ui] (or the current one if none specified).
 */
fun vaadin(ui: UI = UI.getCurrent()) = VaadinDispatcher(ui) + VaadinExceptionHandler(ui)
