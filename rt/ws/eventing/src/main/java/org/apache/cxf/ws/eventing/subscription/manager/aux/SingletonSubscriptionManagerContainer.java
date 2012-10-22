package org.apache.cxf.ws.eventing.subscription.manager.aux;

import org.apache.cxf.ws.eventing.subscription.manager.SubscriptionManager;
import org.apache.cxf.ws.eventing.subscription.manager.SubscriptionManagerImpl;

/**
 * @author jmartisk
 * @since 9/20/12
 */
public class SingletonSubscriptionManagerContainer {

    private static SubscriptionManager INSTANCE;

    public static synchronized SubscriptionManager getInstance() {
        if(INSTANCE == null)
            INSTANCE = new SubscriptionManagerImpl();
        return INSTANCE;
    }

}
