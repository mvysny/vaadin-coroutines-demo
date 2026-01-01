package org.test

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.fakeservlet.FakeHttpEnvironment
import com.vaadin.flow.component.button.Button
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.expect

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MyUITest {
    companion object {
        private lateinit var javalinServer: Server
        @BeforeAll @JvmStatic fun startJavalin() {
            MyRestServlet.serviceDurationMs = 50
            val ctx = WebAppContext()
            ctx.baseResource = EmptyResource()
            ctx.addServlet(MyRestServlet::class.java, "/rest/*")
            javalinServer = Server(23442)
            javalinServer.handler = ctx
            javalinServer.start()
            FakeHttpEnvironment.localPort = 23442
            port = 23442
        }

        @AfterAll @JvmStatic fun stopJavalin() {
            javalinServer.stop()
        }

        private lateinit var routes: Routes
        @BeforeAll @JvmStatic fun discoverRoutes() {
            routes = Routes().autoDiscoverViews("org.test")
        }
    }
    @BeforeEach fun mockVaadin() { MockVaadin.setup(routes) }
    @AfterEach fun teardownVaadin() { MockVaadin.tearDown() }

    @Test fun `canceling purchase does nothing if the purchase is not ongoing`() {
        _get<Button> { text = "Cancel Purchase" } ._click()
    }

    @Test fun `purchase ticket shows a dialog`() {
        _get<Button> { text = "Buy Ticket" } ._click()
        expect("Checking Available Tickets, Please Wait") { _get<ProgressDialog>().message }
    }

    @Test fun `purchase ticket shows a purchase confirmation dialog`() {
        _get<Button> { text = "Buy Ticket" } ._click()
        Thread.sleep(50)
        retry {
            expect("There are 25 available tickets. Would you like to purchase one?") { _get<ConfirmDialog>().message }
        }
    }

    @Test fun `clicking No throws an exception but doesn't prevent another coroutine to be created`() {
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
}
