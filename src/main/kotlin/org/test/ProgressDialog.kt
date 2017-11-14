package org.test

import com.github.vok.karibudsl.*
import com.vaadin.ui.UI
import com.vaadin.ui.Window

inline fun <T> withProgressDialog(message: String, block: ()->T): T {
    val dlg = ProgressDialog(message)
    dlg.show()
    return try {
        block()
    } finally {
        dlg.close()
    }
}

/**
 * A simple progress dialog.
 */
class ProgressDialog(message: String) : Window() {
    init {
        center(); isResizable = false; isModal = false; isClosable = false
        horizontalLayout {
            isMargin = true
            spinner()
            label(message)
        }
    }

    fun show() {
        UI.getCurrent().addWindow(this)
    }
}
