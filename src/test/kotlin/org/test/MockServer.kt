package org.test

import io.javalin.Javalin

fun Javalin.ticketsRestAPI(serviceDurationMs: Long = 50) {
    get("/rest/tickets/available") { ctx ->
        Thread.sleep(serviceDurationMs) // simulate delay
        ctx.result("25")
    }
    post("/rest/tickets/purchase") { ctx ->
        Thread.sleep(serviceDurationMs) // simulate delay
        println("""
            ===================
            PURCHASED
            ===================
        """.trimIndent())
    }
}
