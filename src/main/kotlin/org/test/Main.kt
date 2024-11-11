package org.test

import com.github.mvysny.vaadinboot.VaadinBoot

var port: Int = -1

/**
 * Run this function to launch your app in Embedded Jetty.
 */
fun main() {
    val boot = VaadinBoot()
    port = boot.port
    boot.run()
}
