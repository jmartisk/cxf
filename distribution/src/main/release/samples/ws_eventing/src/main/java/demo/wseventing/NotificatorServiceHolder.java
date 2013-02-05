package demo.wseventing;

import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForNotificators;
import org.apache.cxf.ws.eventing.backend.notification.NotificatorService;

import demo.wseventing.eventapi.CatastrophicEventSinkImpl;

@WebListener
public class NotificatorServiceHolder implements ServletContextListener {

    private Logger logger = Logger.getLogger(NotificatorServiceHolder.class.getName());

    private static NotificatorService instance;

    public static NotificatorService getInstance() {
        return instance;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing and starting NotificatorService");
        instance = new NotificatorService() {
            @Override
            protected SubscriptionManagerInterfaceForNotificators obtainManager() {
                return SingletonSubscriptionManagerContainer.getInstance();
            }

            @Override
            protected Class getEventSinkInterface() {
                return null; // TODO
            }
        };
        instance.start();
        ApplicationSingleton.getInstance().createEventSink("hoho");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping NotificatorService");
        instance.stop();     // very important!
        SingletonSubscriptionManagerContainer.destroy();
    }
}
