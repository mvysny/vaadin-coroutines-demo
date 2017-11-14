package org.test

import com.github.vok.karibudsl.*
import com.vaadin.ui.Alignment
import com.vaadin.ui.UI
import com.vaadin.ui.Window
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Runs given block with a progress dialog being shown. When the block finishes, the dialog is automatically hidden.
 */
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
 */
class ConfirmDialog(message: String, private val response: (confirmed: Boolean) -> Unit) : Window() {
    init {
        caption = "Confirm"; center(); isResizable = true; isModal = false
        val registration = addCloseListener({ _ -> response(false) })
        verticalLayout {
            label(message)
            horizontalLayout {
                alignment = Alignment.MIDDLE_RIGHT
                button("Yes", { registration.remove(); close(); response(true) }) {
                    setPrimary()
                }
                button("No", { registration.remove(); close(); response(false) })
            }
        }
    }

    fun show() {
        UI.getCurrent().addWindow(this)
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
        val dlg = ConfirmDialog(message, { response -> cont.resume(response) })
        dlg.show()
        cont.invokeOnCompletion { checkUIThread(); dlg.close() }
    }
}
