package org.test

import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.textField
import com.github.vok.karibudsl.verticalLayout
import com.vaadin.annotations.Push
import javax.servlet.annotation.WebServlet

import com.vaadin.annotations.Theme
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.*
import com.vaadin.shared.Position
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory

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
                show(Page.getCurrent())
            }
        }
        verticalLayout {
            button("Click Me", {
                job = launch(vaadin()) {
                    println(getGoogleCom())
                    if (confirmDialog()) {
                        println(getGoogleCom())
                        Notification.show("Done!")
                    } else {
                        throw RuntimeException("Unheard of!")
                    }
                }
            })
            button("Cancel", { job.cancel() })
        }
    }
}

@WebServlet(urlPatterns = arrayOf("/*"), name = "MyUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = MyUI::class, productionMode = false)
class MyUIServlet : VaadinServlet()
