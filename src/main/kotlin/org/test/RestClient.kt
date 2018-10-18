package org.test

import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinServletRequest
import org.asynchttpclient.DefaultAsyncHttpClient

object RestClient {
    private val port: Int get() = (VaadinRequest.getCurrent() as VaadinServletRequest).localPort
    /**
     * Checks whether there are still tickets available. Suspends until the response is available, then returns it.
     * See [TicketsRest] for the server dummy implementation.
     */
    suspend fun getNumberOfAvailableTickets(): Int {
        checkUIThread()
        val response = DefaultAsyncHttpClient().prepareGet("http://localhost:$port/rest/tickets/available").async()
        return response.toInt()
    }

    /**
     * Buys a ticket. NOTE: canceling the job running this function won't cancel the request and thus the request processing will proceed
     * irrespective of this job being canceled.
     */
    suspend fun buyTicket() {
        checkUIThread()
        DefaultAsyncHttpClient().preparePost("http://localhost:$port/rest/tickets/purchase").async()
    }
}
