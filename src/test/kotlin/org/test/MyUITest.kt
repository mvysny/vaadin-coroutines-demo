package org.test

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.kaributesting.mockhttp.MockHttpEnvironment
import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.button.Button
import io.javalin.Javalin
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.EmptyResource
import org.eclipse.jetty.webapp.WebAppContext
import kotlin.test.expect

@DynaTestDsl
fun DynaNodeGroup.usingJavalin() {
    lateinit var server: Server
    beforeGroup {
        MyRestServlet.serviceDurationMs = 50
        val ctx = WebAppContext()
        ctx.baseResource = EmptyResource.INSTANCE
        ctx.addServlet(MyRestServlet::class.java, "/rest/*")
        server = Server(23442)
        server.handler = ctx
        server.start()
        MockHttpEnvironment.localPort = 23442
    }
    afterGroup { server.stop() }
}

class MyUITest : DynaTest({
    usingJavalin()
    lateinit var routes: Routes
    beforeGroup {
        routes = Routes().autoDiscoverViews("org.test")
    }
    beforeEach { MockVaadin.setup(routes) }
    afterEach { MockVaadin.tearDown() }

    test("canceling purchase does nothing if the purchase is not ongoing") {
        _get<Button> { text = "Cancel Purchase" } ._click()
    }

    test("purchase ticket shows a dialog") {
        _get<Button> { text = "Buy Ticket" } ._click()
        expect("Checking Available Tickets, Please Wait") { _get<ProgressDialog>().message }
    }

    test("purchase ticket shows a purchase confirmation dialog") {
        _get<Button> { text = "Buy Ticket" } ._click()
        Thread.sleep(50)
        retry {
            expect("There are 25 available tickets. Would you like to purchase one?") { _get<ConfirmDialog>().message }
        }
    }

    test("clicking No throws an exception but doesn't prevent another coroutine to be created") {
        _get<Button> { text = "Buy Ticket" } ._click()
        Thread.sleep(50)
        retry {
            _get<ConfirmDialog>()._get<Button> { text = "No" }._click()
        }
        MockVaadin.runUIQueue(true)
        _get<Button> { text = "Buy Ticket" } ._click()
        MockVaadin.clientRoundtrip()
        expect("Checking Available Tickets, Please Wait") { _get<ProgressDialog>().message }
    }
})
