package org.test

import io.javalin.Javalin
import io.javalin.http.servlet.JavalinServlet
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * A demo REST endpoints called by [RestClient]. To test tat everything works,
 * run this from terminal: `curl http://localhost:8080/rest` - should print
 * "Hello".
 */
@WebServlet(
    urlPatterns = ["/rest/*"],
    name = "MyRestServlet",
    asyncSupported = false
)
class MyRestServlet : HttpServlet() {
    val javalin: JavalinServlet = Javalin.createStandalone()
        .get("/rest") { ctx -> ctx.result("Hello!") }
        .get("/rest/tickets/available") { ctx ->
            Thread.sleep(serviceDurationMs) // simulate delay
            ctx.result("25")
        }
        .post("/rest/tickets/purchase") { _ ->
            Thread.sleep(serviceDurationMs) // simulate delay
            println(
                """
            ===================
            PURCHASED
            ===================
        """.trimIndent()
            )
        }
        .javalinServlet()

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        javalin.service(req, resp)
    }

    companion object {
        var serviceDurationMs = 1000L
    }
}
