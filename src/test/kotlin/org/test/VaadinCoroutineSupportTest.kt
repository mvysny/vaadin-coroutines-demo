package org.test

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.VaadinSession
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import java.util.concurrent.ExecutionException
import kotlin.coroutines.CoroutineContext
import kotlin.test.expect

/**
 * Tests the basic properties of Vaadin+Coroutine integration
 */
class VaadinCoroutineSupportTest {
    @BeforeEach @Order(0) fun setup() { MockVaadin.setup() }
    @AfterEach fun teardown() { MockVaadin.tearDown() }

    private lateinit var coroutineScope: CoroutineScope
    @BeforeEach @Order(1) fun setupCoroutines() {
        val uiCoroutineScope = SupervisorJob()
        val uiCoroutineContext = vaadin()
        coroutineScope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = uiCoroutineContext + uiCoroutineScope
        }
    }

    @Nested inner class VaadinExceptionHandler() {
        @Test fun `exception thrown by launch() caught by VaadinExceptionHandler`() {
            // prepare the ErrorHandler
            lateinit var caught: Throwable
            VaadinSession.getCurrent().errorHandler =
                ErrorHandler { event -> caught = event.throwable }
            val expected = Error("simulated")
            coroutineScope.launch {
                throw expected
            }

            // run the UI queue, which will run the launch{} block. Make sure
            // to propagate any exceptions to the errorHandler defined above, in order
            // to populate the `caught` variable properly.
            MockVaadin.runUIQueue(propagateExceptionToHandler = true)

            expect(expected) { caught }
        }

        @Test fun `exception thrown by launch() caught by Karibu-Testing`() {
            coroutineScope.launch {
                throw RuntimeException("expected")
            }

            // run the UI queue, which will run the launch{} block, throwing any exceptions out
            // runUIQueue() wraps the exception in ExecutionException
            val ex = assertThrows<ExecutionException> {
                MockVaadin.runUIQueue()
            }
            expect("expected") { ex.message }
        }

        @Test fun `exceptions thrown by suspendCancellableCoroutine() caught`() {
            coroutineScope.launch {
                suspendCancellableCoroutine { _ ->
                    throw RuntimeException("expected")
                }
            }

            // run the UI queue, which will run the launch{} block, throwing any exceptions out
            // runUIQueue() wraps the exception in ExecutionException
            val ex = assertThrows<ExecutionException> {
                MockVaadin.runUIQueue()
            }
            expect("expected") { ex.message }
        }
    }
}
