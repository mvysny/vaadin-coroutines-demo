package org.test

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
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
@Push
@Route("")
class MainRoute : KComposite(), CoroutineScope {

    companion object {
        private val log = LoggerFactory.getLogger(MainRoute::class.java)
    }

    /**
     * I must use the [SupervisorJob] here; regular [Job] would cancel itself if any of the child coroutines failed, and that would
     * prevent launching more coroutines.
     */
    private val uiCoroutineScope = SupervisorJob()
    private val uiCoroutineContext = vaadin()

    @Transient
    private var job: Job? = null

    private val root = ui {
        verticalLayout {
            button("Buy Ticket") {
                onLeftClick { job = purchaseTicket() }
            }
            button("Cancel Purchase") {
                onLeftClick { job?.cancel() }
            }
            button("Close session (must cancel all ongoing jobs)") {
                onLeftClick { VaadinSession.getCurrent().close(); UI.getCurrent().page.reload() }
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
                Notification.show("No tickets available", 0, Notification.Position.MIDDLE)
            } else {

                // there seem to be tickets available. Ask the user for confirmation.
                if (confirmDialog("There are $availableTickets available tickets. Would you like to purchase one?")) {

                    // let's go ahead and purchase a ticket. Wrap the long-running REST call in a nice progress dialog.
                    withProgressDialog("Purchasing") { RestClient.buyTicket() }

                    // show an info box that the purchase has been completed
                    confirmationInfoBox("The ticket has been purchased, thank you!")
                } else {

                    // demonstrates the proper exception handling.
                    throw RuntimeException("Unimplemented - should bubble towards the VaadinSession errorHandler")
                }
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = uiCoroutineContext + uiCoroutineScope

    override fun onDetach(detachEvent: DetachEvent) {
        uiCoroutineScope.cancel()
        log.info("Canceled all coroutines started from the UI")
        super.onDetach(detachEvent)
    }
}

private fun confirmationInfoBox(msg: String) {
    Notification(msg, 3000, Notification.Position.MIDDLE).apply {
        addThemeVariants(NotificationVariant.LUMO_SUCCESS, NotificationVariant.LUMO_PRIMARY)
    }.open()
}

@ApplicationPath("/rest")
class ApplicationConfig : Application()
