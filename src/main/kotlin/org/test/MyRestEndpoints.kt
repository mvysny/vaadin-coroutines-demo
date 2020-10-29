package org.test

import io.javalin.Javalin
import io.javalin.http.JavalinServlet
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = ["/rest/*"], name = "MyRestServlet", asyncSupported = false)
class MyRestServlet : HttpServlet() {
    val javalin: JavalinServlet = Javalin.createStandalone()
            .get("/rest") { ctx -> ctx.result("Hello!") }
            .apply { ticketsRestAPI(1000) }
            .servlet()

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        javalin.service(req, resp)
    }
}

fun Javalin.ticketsRestAPI(serviceDurationMs: Long = 50) {
    get("/rest/tickets/available") { ctx ->
        Thread.sleep(serviceDurationMs) // simulate delay
        ctx.result("25")
    }
    post("/rest/tickets/purchase") { _ ->
        Thread.sleep(serviceDurationMs) // simulate delay
        println("""
            ===================
            PURCHASED
            ===================
        """.trimIndent())
    }
}
