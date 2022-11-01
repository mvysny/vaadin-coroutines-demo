package org.test

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.test.expect

class DialogsTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    lateinit var job: Job
    lateinit var coroutineScope: CoroutineScope
    beforeEach {
        job = SupervisorJob()
        val uiCoroutineContext = vaadin()
        coroutineScope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = uiCoroutineContext + job
        }
    }

    group("ConfirmDialog") {
        test("smoke") {
            ConfirmDialog("Foo") {}.open()
            _expectOne<ConfirmDialog>()
        }
        test("clicking yes") {
            var outcome: Boolean? = null
            ConfirmDialog("Foo") { outcome = it }.open()
            _get<Button> { text = "Yes" } ._click()

            _expectNone<ConfirmDialog>()
            expect(true) { outcome }
        }
    }

    group("confirmDialog") {
        test("clicking yes") {
            coroutineScope.launch {
                if (confirmDialog("Foo")) {
                    Notification.show("Yes!")
                } else {
                    Notification.show("No!")
                }
            }

            _expectOne<ConfirmDialog>()
            _get<Button> { text = "Yes" } ._click()
            expectNotifications("Yes!")
        }

        test("clicking no") {
            coroutineScope.launch {
                if (confirmDialog("Foo")) {
                    Notification.show("Yes!")
                } else {
                    Notification.show("No!")
                }
            }

            _expectOne<ConfirmDialog>()
            _get<Button> { text = "No" } ._click()
            expectNotifications("No!")
        }

        test("canceling job hides the dialog") {
            coroutineScope.launch {
                if (confirmDialog("Foo")) {
                    Notification.show("Yes!")
                } else {
                    Notification.show("No!")
                }
            }

            _expectOne<ConfirmDialog>()
            job.cancel()
            _expectNone<ConfirmDialog>()
        }
    }
})
