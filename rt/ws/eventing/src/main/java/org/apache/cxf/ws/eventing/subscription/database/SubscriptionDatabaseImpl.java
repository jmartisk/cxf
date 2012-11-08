package org.apache.cxf.ws.eventing.subscription.database;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.faults.UnknownSubscription;

import java.util.List;
import java.util.UUID;
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

    /**
     * Searches the database for a ticket with the specified UUID
     * @param id
     * @return the ticket, or null of no ticket with this UUID exists
     */
    @Override
    public SubscriptionTicket findById(UUID id) {
        for(SubscriptionTicket ticket : ticketList) {
            if(ticket.getUuid().equals(id))
                return ticket;
        }
        return null;
    }

    @Override
    public void removeTicketByUUID(UUID id) {
        boolean removed = false;
        for(SubscriptionTicket ticket : ticketList) {
            if(ticket.getUuid().equals(id)) {
                ticketList.remove(ticket);
                removed = true;
            }
        }
        if(!removed)
            throw new UnknownSubscription();
    }

}
