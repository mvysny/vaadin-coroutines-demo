package org.test

import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.verticalLayout
import com.vaadin.annotations.Push
import com.vaadin.annotations.Theme
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.ErrorHandler
import com.vaadin.server.Page
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinServlet
import com.vaadin.shared.Position
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.themes.ValoTheme
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import javax.servlet.annotation.WebServlet
import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 *
 * The UI is initialized using [init]. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Push
class MyUI : UI() {

    companion object {
        private val log = LoggerFactory.getLogger(MyUI::class.java)
    }

    @Transient
    private lateinit var job: Job

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
            button("Cancel Reservation", {
                job = launch(vaadin()) {
                    val isReservationValid = isReservationValid()
                    if (!isReservationValid) {
                        Notification.show("Not valid anymore")
                    } else {
                        if (confirmDialog("The reservation is still valid. Do you really wish to cancel?")) {
                            cancelReservation()
                            Notification(null, "The reservation has been canceled", Notification.Type.HUMANIZED_MESSAGE).apply {
                                position = Position.MIDDLE_CENTER
                                styleName = "${ValoTheme.NOTIFICATION_SUCCESS} ${ValoTheme.NOTIFICATION_CLOSABLE}"
                                this.delayMsec = -1
                                show(Page.getCurrent())
                            }
                        } else {
                            throw RuntimeException("Unimplemented ;)")
                        }
                    }
                }
            })
            button("Cancel The Cancelation Job", { job.cancel() })
        }
    }
}

@WebServlet(urlPatterns = arrayOf("/*"), name = "MyUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = MyUI::class, productionMode = false)
class MyUIServlet : VaadinServlet()

@ApplicationPath("/rest")
class ApplicationConfig : Application()
