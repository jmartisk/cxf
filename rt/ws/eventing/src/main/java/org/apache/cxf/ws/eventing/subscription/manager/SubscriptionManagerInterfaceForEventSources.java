package org.apache.cxf.ws.eventing.subscription.manager;

import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionTicket;

/**
 * @author jmartisk
 * @since 9/20/12
 */
public interface SubscriptionManagerInterfaceForEventSources {

    SubscriptionTicket subscribe(DeliveryType delivery, EndpointReferenceType endTo, ExpirationType expires, FilterType filter);

}
