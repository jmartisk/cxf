package org.apache.cxf.ws.eventing.subscription.database;

import java.util.List;
import java.util.UUID;

/**
 * @author jmartisk
 * @since 9/17/12
 */
public interface SubscriptionDatabase {

    public void addTicket(SubscriptionTicket ticket);

    public List<SubscriptionTicket> getTickets();

    public SubscriptionTicket findById(UUID id);

    public void removeTicketByUUID(UUID id);

}
