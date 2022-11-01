package org.test

import io.javalin.Javalin
import io.javalin.http.JavalinServlet
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * A demo REST endpoints called by [RestClient].
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
