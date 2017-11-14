package org.test

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Provides access to person list. To test, just run `curl http://localhost:8080/rest/person`
 */
@Path("/reservations")
class ReservationsRest {
    @GET()
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    fun isValid(): String {
        Thread.sleep(1000) // simulate delay
        return "valid"
    }

    @POST()
    @Path("/cancel")
    fun cancel() {
        Thread.sleep(3000) // simulate delay
        println("""
            ===================
            CANCELED
            ===================
        """.trimIndent())
    }
}
