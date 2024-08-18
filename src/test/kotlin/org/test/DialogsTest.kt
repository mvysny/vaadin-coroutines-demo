package org.test

import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.expect

class DialogsTest {
    @BeforeEach @Order(0) fun setupVaadin() { MockVaadin.setup() }
    @AfterEach fun teardownVaadin() { MockVaadin.tearDown() }

    private lateinit var job: Job
    private lateinit var coroutineScope: CoroutineScope
    @BeforeEach @Order(1) fun setupCoroutines() {
        job = SupervisorJob()
        val uiCoroutineContext = vaadin()
        coroutineScope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = uiCoroutineContext + job
        }
    }

    @Nested inner class ConfirmDialogTests {
        @Test fun smoke() {
            ConfirmDialog("Foo") {}.open()
            _expectOne<ConfirmDialog>()
        }
        @Test fun clickingYes() {
            var outcome: Boolean? = null
            ConfirmDialog("Foo") { outcome = it }.open()
            _get<Button> { text = "Yes" } ._click()

            _expectNone<ConfirmDialog>()
            expect(true) { outcome }
        }
    }

    @Nested inner class ConfirmDialog_Coroutines {
        @Test fun clickingYes() {
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

        @Test fun clickingNo() {
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

        @Test fun `canceling job hides the dialog`() {
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
}
