package org.test

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Provides access to person list. To test, just run `curl http://localhost:8080/rest/person`
 */
@Path("/tickets")
class TicketsRest {
    @GET()
    @Path("/available")
    @Produces(MediaType.TEXT_PLAIN)
    fun free(): String {
        Thread.sleep(1000) // simulate delay
        return "25"
    }

    @POST()
    @Path("/purchase")
    fun cancel() {
        Thread.sleep(3000) // simulate delay
        println("""
            ===================
            PURCHASED
            ===================
        """.trimIndent())
    }
}
