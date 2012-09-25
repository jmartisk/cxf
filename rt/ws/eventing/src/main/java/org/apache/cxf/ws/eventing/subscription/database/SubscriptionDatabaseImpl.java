package org.apache.cxf.ws.eventing.subscription.database;

import org.apache.cxf.common.logging.LogUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/27/12
 */
public class SubscriptionDatabaseImpl implements SubscriptionDatabase {

    private static final Logger LOG = LogUtils.getLogger(SubscriptionDatabaseImpl.class);
    private final List<SubscriptionTicket> ticketList;

    public SubscriptionDatabaseImpl() {
        LOG.info("Instantiating SubscriptionDatabaseImpl");
        ticketList = new CopyOnWriteArrayList<SubscriptionTicket>();
    }


    @Override
    public void addTicket(SubscriptionTicket ticket) {
        ticketList.add(ticket);
        LOG.info("SubscriptionDatabaseImpl accepted ticket: " + ticket.toString());
    }

    @Override
    public List<SubscriptionTicket> getTickets() {
        return ticketList;
    }

}
