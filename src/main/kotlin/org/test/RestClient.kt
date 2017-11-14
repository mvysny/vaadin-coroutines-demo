package org.test

import org.asynchttpclient.DefaultAsyncHttpClient

object RestClient {
    /**
     * Checks whether there are still tickets available. Suspends until the response is available, then returns it.
     * See [TicketsRest] for the server dummy implementation.
     */
    suspend fun getNumberOfAvailableTickets(): Int {
        val response = DefaultAsyncHttpClient().prepareGet("http://localhost:8080/rest/tickets/available").async()
        return response.toInt()
    }

    /**
     * Buys a ticket. NOTE: canceling the job running this function won't cancel the request and thus the request processing will proceed
     * irrespective of this job being canceled.
     */
    suspend fun buyTicket() {
        DefaultAsyncHttpClient().preparePost("http://localhost:8080/rest/tickets/purchase").async()
    }
}
