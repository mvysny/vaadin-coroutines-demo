package org.test

import com.github.vok.karibudsl.button
import com.github.vok.karibudsl.textField
import com.github.vok.karibudsl.verticalLayout
import com.vaadin.annotations.Push
import javax.servlet.annotation.WebServlet

import com.vaadin.annotations.Theme
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinServlet
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

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

    private lateinit var job: Job

    @Override
    override fun init(vaadinRequest: VaadinRequest?) {
        verticalLayout {
            val name = textField {
                caption = "Type your name here:"
            }
            button("Click Me", {
                job = launch(Vaadin()) {
                    println(getGoogleCom())
                    if (confirmDialog()) {
                        println(getGoogleCom())
                        Notification.show("Done!")
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
