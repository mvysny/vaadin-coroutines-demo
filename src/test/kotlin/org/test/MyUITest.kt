package org.test

import com.github.mvysny.kaributesting.mockhttp.MockHttpEnvironment
import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.dynatest.DynaTest
import com.vaadin.flow.component.button.Button
import io.javalin.Javalin
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
    lateinit var routes: Routes
    beforeGroup {
        routes = Routes().autoDiscoverViews("org.test")
    }
    beforeEach { MockVaadin.setup(routes) }
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
        Thread.sleep(50)
        retry {
            expect("There are 25 available tickets. Would you like to purchase one?") { _get<ConfirmDialog>().message }
        }
    }

    test("clicking No throws an exception but doesn't prevent another coroutine to be created") {
        _get<Button> { caption = "Buy Ticket" } ._click()
        MockVaadin.runUIQueue()
        Thread.sleep(50)
        retry {
            _get<ConfirmDialog>()._get<Button> { caption = "No" }._click()
        }
        MockVaadin.runUIQueue(true)
        _get<Button> { caption = "Buy Ticket" } ._click()
        MockVaadin.runUIQueue()
        expect("Checking Available Tickets, Please Wait") { _get<ProgressDialog>().message }
    }
})
