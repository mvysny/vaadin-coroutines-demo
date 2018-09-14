package org.test

import com.github.vok.karibudsl.*
import com.vaadin.shared.Registration
import com.vaadin.ui.Alignment
import com.vaadin.ui.UI
import com.vaadin.ui.Window
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Runs given block with a progress dialog being shown. When the block finishes, the dialog is automatically hidden.
 */
inline fun <T> withProgressDialog(message: String, block: ()->T): T {
    checkUIThread()
    val dlg = ProgressDialog(message)
    dlg.show()
    try {
        return block()
    } finally {
        dlg.close()
    }
}

/**
 * A simple progress dialog. Use [withProgressDialog] to show the dialog.
 */
class ProgressDialog(message: String) : Window() {
    init {
        // the dialog is not modal on purpose, so that you can try the "Cancel" button.
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

/**
 * A simple confirmation dialog. Use [confirmDialog] to show the dialog.
 * @property response invoked with the user's response: true if the user pressed yes, false if the user pressed no or closed the dialog.
 * When this closure is invoked, the dialog is already closed.
 */
class ConfirmDialog(message: String, private val response: (confirmed: Boolean) -> Unit) : Window() {
    private val responseRegistration: Registration
    init {
        caption = "Confirm"; center(); isResizable = true; isModal = false
        responseRegistration = addCloseListener({ _ -> response(false) })
        verticalLayout {
            label(message)
            horizontalLayout {
                alignment = Alignment.MIDDLE_RIGHT
                button("Yes") {
                    setPrimary()
                    onLeftClick { cancel(); response(true) }
                }
                button("No") {
                    onLeftClick { cancel(); response(false) }
                }
            }
        }
    }

    fun show() {
        UI.getCurrent().addWindow(this)
    }

    fun cancel() {
        responseRegistration.remove()
        close()
    }
}

/**
 * Opens a confirmation dialog and suspends; resumes when the dialog is closed or a button is clicked inside of the dialog.
 * Supports cancelation - closes the dialog automatically.
 * @return true if the user pressed yes, false if the user pressed no or closed the dialog.
 */
suspend fun confirmDialog(message: String = "Are you sure?"): Boolean {
    return suspendCancellableCoroutine { cont: CancellableContinuation<Boolean> ->
        checkUIThread()
        val dlg = ConfirmDialog(message) { response -> cont.resume(response) }
        dlg.show()
        cont.invokeOnCancellation { checkUIThread(); dlg.cancel() }
    }
}
