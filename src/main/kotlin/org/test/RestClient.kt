package org.test

import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinServletRequest
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

/**
 * Uses the AsyncHttpClient to call REST endpoints from Kotlin Coroutines.
 */
object RestClient {
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private val port: Int get() {
        val portEnv: String = System.getenv("PORT") ?: ""
        if (portEnv.isNotBlank()) {
            return portEnv.toInt()
        }
        return (VaadinRequest.getCurrent() as VaadinServletRequest).localPort
    }
    /**
     * Checks whether there are still tickets available. Suspends until the response is available, then returns it.
     * See [MyRestServlet] for the server dummy implementation.
     */
    suspend fun getNumberOfAvailableTickets(): Int {
        checkUIThread()
        val request = "http://localhost:$port/rest/tickets/available".httpGet()
        val response: String = client.async(request)
        return response.toInt()
    }

    /**
     * Buys a ticket. NOTE: canceling the job running this function won't cancel the request and thus the request processing will proceed
     * irrespective of this job being canceled.
     */
    suspend fun buyTicket() {
        checkUIThread()
        val request = "http://localhost:$port/rest/tickets/purchase".httpPost()
        client.async(request)
    }
}

fun String.httpGet(): HttpRequest = HttpRequest.newBuilder(URI.create(this)).build()
fun String.httpPost(body: String = ""): HttpRequest = HttpRequest.newBuilder(URI.create(this)).POST(HttpRequest.BodyPublishers.ofString(body)).build()

/**
 * Asynchronously processes given request and returns the response body. Fails if the server returns anything but 200.
 * @return the response body
 */
suspend fun HttpClient.async(req: HttpRequest): String {
    val future = sendAsync(req, BodyHandlers.ofString())
    val response = future.await()
    check(response.statusCode() == 200) { "Request $req failed with ${response.statusCode()}: ${response.body()}" }
    return response.body()
}
