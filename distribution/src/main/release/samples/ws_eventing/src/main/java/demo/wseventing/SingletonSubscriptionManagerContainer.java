package demo.wseventing;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManager;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerImpl;

public class SingletonSubscriptionManagerContainer {

    private static SubscriptionManager INSTANCE;

    public static synchronized SubscriptionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SubscriptionManagerImpl("http://localhost:8080/ws-eventing-server/TestSubscriptionManager");
        }
        return INSTANCE;
    }

    public static synchronized void destroy() {
        INSTANCE = null;
    }

}
