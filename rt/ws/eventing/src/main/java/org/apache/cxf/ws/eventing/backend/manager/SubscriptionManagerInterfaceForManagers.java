package org.apache.cxf.ws.eventing.backend.manager;

import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;

import java.util.UUID;

/**
 * @author jmartisk
 * @since 9/20/12
 */
public interface SubscriptionManagerInterfaceForManagers extends SubscriptionManagerInterfaceForEventSources {

    public void unsubscribeTicket(UUID uuid);

    public SubscriptionTicket findTicket(UUID uuid);

    public ExpirationType renew(UUID uuid, ExpirationType requestedExpiration);

}
