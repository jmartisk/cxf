package org.apache.cxf.ws.eventing.subscription.database;

import java.util.List;

/**
 * @author jmartisk
 * @since 9/17/12
 */
public interface SubscriptionDatabase {

    public void addTicket(SubscriptionTicket ticket);

    public List<SubscriptionTicket> getTickets();

}
