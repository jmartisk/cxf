package org.apache.cxf.ws.eventing.subscription;

import org.apache.cxf.common.logging.LogUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/27/12
 */
public class EventingSubscriptionManager {

    private static final Logger LOG = LogUtils.getLogger(EventingSubscriptionManager.class);
    private List<SubscriptionTicket> ticketList;

    public EventingSubscriptionManager() {
        LOG.info("Instantiating EventingSubscriptionManager");
        ticketList = new ArrayList<SubscriptionTicket>();
    }

    public void addTicket(SubscriptionTicket ticket) {
        synchronized (this) {
            ticketList.add(ticket);
        }
        LOG.info("EventingSubscriptionManager accepted ticket: " + ticket.toString());
    }

    public void dispatchEvent(Element message) {
        // evaluate filters
    }

}
