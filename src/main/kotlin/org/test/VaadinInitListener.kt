package org.test

import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class Bootstrap : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        asyncHttpClient = DefaultAsyncHttpClient()
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        asyncHttpClient?.close()
        asyncHttpClient = null
    }

    companion object {
        @Volatile
        var asyncHttpClient: AsyncHttpClient? = null
    }
}

/**
 * Initializes Vaadin app.
 */
class VaadinInitListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addSessionInitListener {
            it.session.errorHandler = ErrorHandler { event ->
                log.error("UI error", event.throwable)
                Notification("Internal error: ${event.throwable}", 5000, Notification.Position.TOP_CENTER).apply {
                    addThemeVariants(NotificationVariant.LUMO_ERROR)
                }.open()
            }
        }
    }

    companion object {
        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(VaadinInitListener::class.java)
    }
}

@Push
class MyApp : AppShellConfigurator
