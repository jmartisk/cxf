package org.apache.cxf.ws.eventing.base.aux;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManager;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerImpl;
import org.apache.cxf.ws.eventing.base.SimpleEventingIntegrationTest;

/**
 * @author jmartisk
 * @since 9/20/12
 */
public class SingletonSubscriptionManagerContainer {

    private static SubscriptionManager INSTANCE;

    public static synchronized SubscriptionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SubscriptionManagerImpl(SimpleEventingIntegrationTest.URL_SUBSCRIPTION_MANAGER);
        }
        return INSTANCE;
    }

    public static synchronized void destroy() {
        INSTANCE = null;
    }

}
