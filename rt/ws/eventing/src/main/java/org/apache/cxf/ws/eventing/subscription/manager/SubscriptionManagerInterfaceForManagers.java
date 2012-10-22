package org.apache.cxf.ws.eventing.subscription.manager;

import org.apache.cxf.ws.eventing.subscription.database.SubscriptionDatabase;

/**
 * @author jmartisk
 * @since 9/20/12
 */
public interface SubscriptionManagerInterfaceForManagers extends SubscriptionManagerInterfaceForEventSources {

    public SubscriptionDatabase getDatabase();

}
