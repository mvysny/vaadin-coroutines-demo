package org.test

import com.github.karibu.testing.MockVaadin

/**
 * Retries given [block] for at most [maxDuration] millis until the block
 * finishes successfully (doesn't throw an exception).
 */
fun retry(maxDuration: Long = 500, block: ()->Unit) {
    check(maxDuration > 10) { "$maxDuration must be greater than 10" }
    val start = System.currentTimeMillis()
    var lastThrowable: Throwable? = null
    while (System.currentTimeMillis() < start + maxDuration) {
        try {
            MockVaadin.runUIQueue()
            block()
            return
        } catch (t: Throwable) {
            lastThrowable = t
        }
        Thread.sleep(50)
    }
    throw lastThrowable!!
}
