package org.test

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
