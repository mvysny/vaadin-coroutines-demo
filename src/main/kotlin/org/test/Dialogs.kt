package org.test

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.shared.Registration
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Runs given block with a progress dialog being shown. When the block finishes, the dialog is automatically closed.
 */
inline fun <T> withProgressDialog(message: String, block: ()->T): T {
    checkUIThread()
    val dlg = ProgressDialog(message)
    dlg.open()
    try {
        return block()
    } finally {
        dlg.close()
    }
}

/**
 * https://vaadin.com/directory/component/vaadin-component-factoryvcf-progress-spinner
 */
@Tag("vcf-progress-spinner")
@NpmPackage("@vaadin-component-factory/vcf-progress-spinner", version = "1.0.1")
@JsModule("@vaadin-component-factory/vcf-progress-spinner")
class Spinner : Component()

@VaadinDsl
fun (@VaadinDsl HasComponents).spinner(block: (@VaadinDsl Spinner).() -> Unit = {}): Spinner
        = init(Spinner(), block)

/**
 * A simple progress dialog. Use [withProgressDialog] to show the dialog.
 */
class ProgressDialog(val message: String) : Dialog() {
    init {
        // the dialog is not modal on purpose, so that you can try the "Cancel" button.
        isResizable = false; isModal = false;
        horizontalLayout {
            isMargin = true
            spinner()
            span(message)
        }
    }
}

/**
 * A simple confirmation dialog. Use [confirmDialog] to show the dialog.
 * @property response invoked with the user's response: true if the user pressed yes, false if the user pressed no or closed the dialog.
 * When this closure is invoked, the dialog is already closed.
 */
class ConfirmDialog(val message: String, private val response: (confirmed: Boolean) -> Unit) : Dialog() {
    private val responseRegistration: Registration
    init {
        isResizable = true; isModal = false
        responseRegistration = addDialogCloseActionListener { response(false) }
        verticalLayout {
            span(message)
            horizontalLayout {
                content { align(right, middle) }
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
        dlg.open()
        cont.invokeOnCancellation { checkUIThread(); dlg.cancel() }
    }
}
