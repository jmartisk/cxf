package org.apache.cxf.ws.eventing.subscription.manager;

import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.utils.FilteringUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.UUID;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class SubscriptionTicketGrantingResponse {

    public SubscriptionTicketGrantingResponse() {
    }

    private EndpointReferenceType endTo;
    private DeliveryType delivery;
    private XMLGregorianCalendar expires;
    private FilterType filter;
    private UUID uuid;
    private EndpointReferenceType subscriptionManagerReference;


    public EndpointReferenceType getEndTo() {
        return endTo;
    }

    public void setEndTo(EndpointReferenceType endTo) {
        this.endTo = endTo;
    }

    public DeliveryType getDelivery() {
        return delivery;
    }

    public void setDelivery(DeliveryType delivery) {
        this.delivery = delivery;
    }

    public FilterType getFilter() {
        return filter;
    }

    public void setFilter(FilterType filter) {
        if(!FilteringUtil.isFilteringDialectSupported(filter.getDialect()))
            throw new FilteringRequestedUnavailable();
        this.filter = filter;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public EndpointReferenceType getSubscriptionManagerReference() {
        return subscriptionManagerReference;
    }

    public void setSubscriptionManagerReference(EndpointReferenceType subscriptionManagerReference) {
        this.subscriptionManagerReference = subscriptionManagerReference;
    }

    public XMLGregorianCalendar getExpires() {
        return expires;
    }

    public void setExpires(XMLGregorianCalendar expires) {
        this.expires = expires;
    }
}
