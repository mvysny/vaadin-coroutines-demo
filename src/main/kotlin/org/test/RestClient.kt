package org.test

import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinServletRequest
import org.asynchttpclient.DefaultAsyncHttpClient

/**
 * Uses the AsyncHttpClient to call REST endpoints from Kotlin Coroutines.
 */
object RestClient {
    private val port: Int get() {
        val portEnv: String = System.getenv("PORT") ?: ""
        if (portEnv.isNotBlank()) {
            return portEnv.toInt()
        }
        return (VaadinRequest.getCurrent() as VaadinServletRequest).localPort
    }
    /**
     * Checks whether there are still tickets available. Suspends until the response is available, then returns it.
     * See [ticketsRestAPI] for the server dummy implementation.
     */
    suspend fun getNumberOfAvailableTickets(): Int {
        checkUIThread()
        val response = Bootstrap.asyncHttpClient!!.prepareGet("http://localhost:$port/rest/tickets/available").async()
        return response.toInt()
    }

    /**
     * Buys a ticket. NOTE: canceling the job running this function won't cancel the request and thus the request processing will proceed
     * irrespective of this job being canceled.
     */
    suspend fun buyTicket() {
        checkUIThread()
        Bootstrap.asyncHttpClient!!.preparePost("http://localhost:$port/rest/tickets/purchase").async()
    }
}
