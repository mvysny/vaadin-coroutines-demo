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
        test("clicking yes") {
            coroutineScope.launch {
                if (confirmDialog("Foo")) {
                    Notification.show("Yes!")
                } else {
                    Notification.show("No!")
                }
            }

            _expectOne<ConfirmDialog>()
            _get<Button> { caption = "Yes" } ._click()
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
            _get<Button> { caption = "No" } ._click()
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
