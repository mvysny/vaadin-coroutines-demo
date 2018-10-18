package org.test

import com.github.karibu.mockhttp.MockHttpEnvironment
import com.github.karibu.testing.MockVaadin
import com.github.karibu.testing._click
import com.github.karibu.testing._get
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinSession
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import io.javalin.Javalin
import java.util.concurrent.ExecutionException
import kotlin.test.expect

class MyUITest : DynaTest({
    lateinit var javalin: Javalin
    beforeGroup {
        javalin = Javalin.create()
        javalin.ticketsRestAPI(50)
        javalin.start(23442)
        MockHttpEnvironment.localPort = 23442
    }
    afterGroup { javalin.stop() }
    beforeEach { MockVaadin.setup { MyUI() } }
    afterEach { MockVaadin.tearDown() }

    test("canceling purchase does nothing if the purchase is not ongoing") {
        _get<Button> { caption = "Cancel Purchase" } ._click()
    }

    test("purchase ticket shows a dialog") {
        _get<Button> { caption = "Buy Ticket" } ._click()
        MockVaadin.runUIQueue()
        expect("Checking Available Tickets, Please Wait") { _get<ProgressDialog>().message }
    }

    test("purchase ticket shows a purchase confirmation dialog") {
        _get<Button> { caption = "Buy Ticket" } ._click()
        MockVaadin.runUIQueue()
        Thread.sleep(200)
        MockVaadin.runUIQueue()
        expect("There are 25 available tickets. Would you like to purchase one?") { _get<ConfirmDialog>().message }
    }

    test("clicking No throws an exception but doesn't prevent another coroutine to be created") {
        _get<Button> { caption = "Buy Ticket" } ._click()
        MockVaadin.runUIQueue()
        Thread.sleep(200)
        MockVaadin.runUIQueue()
        val buttonNo = _get<ConfirmDialog>()._get<Button> { caption = "No" } ._click()
        MockVaadin.runUIQueue(true)
        _get<Button> { caption = "Buy Ticket" } ._click()
        MockVaadin.runUIQueue()
        expect("Checking Available Tickets, Please Wait") { _get<ProgressDialog>().message }
    }
})
