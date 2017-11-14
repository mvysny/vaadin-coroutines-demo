package org.test

import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.horizontalLayout
import com.github.vok.karibudsl.label
import com.github.vok.karibudsl.verticalLayout
import com.vaadin.ui.UI
import com.vaadin.ui.Window
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import kotlin.coroutines.experimental.CoroutineContext

class ConfirmDialog(private val response: (Boolean) -> Unit) : Window() {
    init {
        caption = "Confirm"; center(); isResizable = true; isModal = false
        addCloseListener({ _ ->
            response(false)
        })
        verticalLayout {
            label("Are you sure?")
            horizontalLayout {
                button("Yes", { response(true) })
                button("No", { close() }) // calls close listener which responds with false
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

suspend fun BoundRequestBuilder.async(): Response =
    suspendCancellableCoroutine { cont: CancellableContinuation<Response> ->
        val f = execute(object : AsyncCompletionHandler<Response>() {

            @Throws(Exception::class)
            override fun onCompleted(response: Response): Response {
                cont.resume(response)
                return response
            }

            override fun onThrowable(t: Throwable) {
                cont.resumeWithException(t)
            }
        })
        cont.invokeOnCompletion { if (!f.isDone) f.cancel(true) }
    }

suspend fun bla(): String {
    val asyncHttpClient = DefaultAsyncHttpClient()
    val response = asyncHttpClient.prepareGet("https://www.google.com/").setFollowRedirect(true).async()
    if (response.statusCode != 200) throw RuntimeException("Request failed: ${response.statusCode} ${response.statusText}")
    return response.responseBody
}

suspend fun confirmDialog(): Boolean {
    return suspendCancellableCoroutine { cont: CancellableContinuation<Boolean> ->
        checkUIThread()
        val dlg = ConfirmDialog({ response ->
            if (!cont.isCompleted) cont.resume(response)
        })
        dlg.show()
        cont.invokeOnCompletion { checkUIThread(); dlg.close() }
    }
}

/**
 * Implements [CoroutineDispatcher] on top of an arbitrary Android [Handler].
 */
data class Vaadin(val ui: UI = UI.getCurrent()) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        ui.access(block)
    }
}
