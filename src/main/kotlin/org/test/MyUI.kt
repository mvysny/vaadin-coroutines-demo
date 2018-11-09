package org.test

import com.github.mvysny.karibudsl.v8.button
import com.github.mvysny.karibudsl.v8.onLeftClick
import com.github.mvysny.karibudsl.v8.verticalLayout
import com.vaadin.annotations.Push
import com.vaadin.annotations.Theme
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.*
import com.vaadin.shared.Position
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.themes.ValoTheme
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import javax.servlet.annotation.WebServlet
import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application
import kotlin.coroutines.CoroutineContext

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 *
 * The UI is initialized using [init]. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Push
class MyUI : UI(), CoroutineScope {

    companion object {
        private val log = LoggerFactory.getLogger(MyUI::class.java)
    }

    /**
     * I must use the [SupervisorJob] here; regular [Job] would cancel itself if any of the child coroutines failed, and that would
     * prevent launching more coroutines.
     */
    private val uiCoroutineScope = SupervisorJob()
    private val uiCoroutineContext = vaadin(this)

    @Transient
    private var job: Job? = null

    @Override
    override fun init(vaadinRequest: VaadinRequest?) {
        errorHandler = ErrorHandler { event ->
            log.error("UI error", event.throwable)
            Notification("Internal error", "Sorry! ${event.throwable}", Notification.Type.ERROR_MESSAGE).apply {
                position = Position.TOP_CENTER
                show(page)
            }
        }
        verticalLayout {
            button("Buy Ticket") {
                onLeftClick { job = purchaseTicket() }
            }
            button("Cancel Purchase") {
                onLeftClick { job?.cancel() }
            }
            button("Close session (must cancel all ongoing jobs)") {
                onLeftClick { VaadinSession.getCurrent().close(); Page.getCurrent().reload() }
            }
        }
    }

    /**
     * Starts the ticket purchase asynchronously.
     * @return cancelable ongoing job
     */
    private fun purchaseTicket(): Job {
        check(coroutineContext.isActive)
        return launch {
            // query the server for the number of available tickets. Wrap the long-running REST call in a nice progress dialog.
            val availableTickets = withProgressDialog("Checking Available Tickets, Please Wait") {
                RestClient.getNumberOfAvailableTickets()
            }

            if (availableTickets <= 0) {
                Notification.show("No tickets available")
            } else {

                // there seem to be tickets available. Ask the user for confirmation.
                if (confirmDialog("There are $availableTickets available tickets. Would you like to purchase one?")) {

                    // let's go ahead and purchase a ticket. Wrap the long-running REST call in a nice progress dialog.
                    withProgressDialog("Purchasing") { RestClient.buyTicket() }

                    // show an info box that the purchase has been completed
                    confirmationInfoBox("The ticket has been purchased, thank you!")
                } else {

                    // demonstrates the proper exception handling.
                    throw RuntimeException("Unimplemented ;)")
                }
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = uiCoroutineContext + uiCoroutineScope

    override fun detach() {
        uiCoroutineScope.cancel()
        log.info("Canceled all coroutines started from the UI")
        super.detach()
    }
}

private fun confirmationInfoBox(msg: String) {
    Notification(null, msg, Notification.Type.HUMANIZED_MESSAGE).apply {
        position = Position.MIDDLE_CENTER
        styleName = "${ValoTheme.NOTIFICATION_SUCCESS} ${ValoTheme.NOTIFICATION_CLOSABLE}"
        this.delayMsec = -1
        show(Page.getCurrent())
    }
}

@WebServlet(urlPatterns = ["/*"], name = "MyUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = MyUI::class, productionMode = false)
class MyUIServlet : VaadinServlet()

@ApplicationPath("/rest")
class ApplicationConfig : Application()
