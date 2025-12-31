package org.test

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.theme.lumo.Lumo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Initializes Vaadin app.
 */
class VaadinInitListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addSessionInitListener {
            it.session.errorHandler = ErrorHandler { event ->
                log.error("UI error", event.throwable)
                if (UI.getCurrent() != null) {
                    Notification("Internal error: ${event.throwable}", 5000, Notification.Position.TOP_CENTER).apply {
                        addThemeVariants(NotificationVariant.LUMO_ERROR)
                    }.open()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(VaadinInitListener::class.java)
    }
}

@Push
@StyleSheet(Lumo.STYLESHEET)
class MyApp : AppShellConfigurator
