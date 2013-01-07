package org.apache.cxf.ws.eventing.backend.manager.aux;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManager;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerImpl;

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
