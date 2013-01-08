package org.apache.cxf.ws.eventing.backend.manager;

import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.FormatType;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;

import java.util.List;

/**
 * @author jmartisk
 * @since 9/20/12
 */
public interface SubscriptionManagerInterfaceForEventSources {

    SubscriptionTicketGrantingResponse subscribe(DeliveryType delivery, EndpointReferenceType endTo, ExpirationType expires, FilterType filter, FormatType format);

    /**
     * READ ONLY. Returns an unmodifiable list of the subscriptions in database.
     */
    List<SubscriptionTicket> getTickets();

}
