package org.test

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.VaadinSession
import kotlinx.coroutines.*
import java.util.concurrent.ExecutionException
import kotlin.coroutines.CoroutineContext
import kotlin.test.expect

/**
 * Tests the basic properties of Vaadin+Coroutine integration
 */
class MyCoroutinesTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    lateinit var coroutineScope: CoroutineScope
    beforeEach {
        val uiCoroutineScope = SupervisorJob()
        val uiCoroutineContext = vaadin()
        coroutineScope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = uiCoroutineContext + uiCoroutineScope
        }
    }

    group("VaadinExceptionHandler") {
        test("exception thrown by launch() caught by VaadinExceptionHandler") {
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

        test("exception thrown by launch() caught by Karibu-Testing") {
            coroutineScope.launch {
                throw RuntimeException("expected")
            }

            // run the UI queue, which will run the launch{} block, throwing any exceptions out
            // runUIQueue() wraps the exception in ExecutionException
            expectThrows(ExecutionException::class, "expected") {
                MockVaadin.runUIQueue()
            }
        }

        test("exceptions thrown by suspendCancellableCoroutine() caught") {
            coroutineScope.launch {
                suspendCancellableCoroutine { cont: CancellableContinuation<Boolean> ->
                    throw RuntimeException("expected")
                }
            }

            // run the UI queue, which will run the launch{} block, throwing any exceptions out
            // runUIQueue() wraps the exception in ExecutionException
            expectThrows(ExecutionException::class, "expected") {
                MockVaadin.runUIQueue()
            }
        }
    }
})
