package org.test

import com.github.mvysny.kaributesting.v10.MockVaadin
import org.eclipse.jetty.util.resource.Resource
import java.net.URI
import java.nio.file.Path

/**
 * Retries given [block] for at most [maxDuration] millis until the block
 * finishes successfully (doesn't throw an exception).
 */
fun retry(propagateExceptionToHadler: Boolean = false, maxDuration: Long = 2000, block: ()->Unit) {
    check(maxDuration > 10) { "$maxDuration must be greater than 10" }
    val start = System.currentTimeMillis()
    var lastThrowable: Throwable? = null
    while (System.currentTimeMillis() < start + maxDuration) {
        try {
            if (propagateExceptionToHadler) {
                MockVaadin.runUIQueue(propagateExceptionToHadler)
            } else {
                MockVaadin.clientRoundtrip() // runUIQueue() doesn't clean up dialogs
            }
            block()
            return
        } catch (t: Throwable) {
            lastThrowable = t
        }
        Thread.sleep(50)
    }
    throw lastThrowable!!
}

class EmptyResource : Resource() {
    override fun getPath(): Path? = null
    override fun isDirectory(): Boolean = true
    override fun isReadable(): Boolean = true
    override fun getURI(): URI? = null
    override fun getName(): String = "EmptyResource"
    override fun getFileName(): String? = null
    override fun resolve(subUriPath: String?): Resource? = null
}
