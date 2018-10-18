package org.test

import com.github.karibu.testing.MockVaadin
import com.github.karibu.testing._click
import com.github.karibu.testing._get
import com.github.mvysny.dynatest.DynaTest
import com.vaadin.server.VaadinSession
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import kotlin.test.expect

class MyUITest : DynaTest({
    beforeEach { MockVaadin.setup { MyUI() } }
    afterEach { MockVaadin.tearDown() }

    test("canceling purchase does nothing if the purchase is not ongoing") {
        _get<Button> { caption = "Cancel Purchase" } ._click()
    }

    test("purchase ticket shows a dialog") {
        _get<Button> { caption = "Buy Ticket" } ._click()
        MockVaadin.runUIQueue()
        expect("Checking Available Tickets, Please Wait") { _get<ProgressDialog>()._get<Label>().value }
    }
})

/**
 * Runs all tasks scheduled by [UI.access].
 */
fun MockVaadin.runUIQueue() {
    VaadinSession.getCurrent()!!.apply {
        unlock()  // this will process all Runnables registered via ui.access()
        // lock the session back, so that the test can continue running as-if in the UI thread.
        lock()
    }
}
