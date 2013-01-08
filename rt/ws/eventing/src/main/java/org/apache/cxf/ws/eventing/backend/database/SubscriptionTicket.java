package org.apache.cxf.ws.eventing.backend.database;

import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.shared.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.shared.utils.FilteringUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * This class represents a valid subscription granted to a requesting client. Instances of such tickets
 * are stored in a SubscriptionDatabase.
 */
public class SubscriptionTicket {

    public SubscriptionTicket() {
    }

    private EndpointReferenceType endTo;
    private DeliveryType delivery;

    public XMLGregorianCalendar getExpires() {
        return expires;
    }

    private XMLGregorianCalendar expires;
    private FilterType filter;
    private EndpointReferenceType subscriptionManagerReference;
    private UUID uuid;
    private boolean wrappedDelivery;


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
        if (!FilteringUtil.isFilteringDialectSupported(filter.getDialect())) {
            throw new FilteringRequestedUnavailable();
        }
        this.filter = filter;
    }

    public boolean isExpired() {
        return expires.toGregorianCalendar().before(new GregorianCalendar());
    }

    public EndpointReferenceType getSubscriptionManagerReference() {
        return subscriptionManagerReference;
    }

    public void setSubscriptionManagerReference(EndpointReferenceType subscriptionManagerReference) {
        this.subscriptionManagerReference = subscriptionManagerReference;
    }

    public void setExpires(XMLGregorianCalendar expires) {
        this.expires = expires;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Convenience method to extract the subscribed target URL.
     *
     * @return
     */
    public String getTargetURL() {
        return ((org.apache.cxf.ws.eventing.NotifyTo)this.getDelivery().getContent().get(0)).getValue()
                .getAddress().getValue().trim();
    }

    public boolean isWrappedDelivery() {
        return wrappedDelivery;
    }

    public void setWrappedDelivery(boolean wrappedDelivery) {
        this.wrappedDelivery = wrappedDelivery;
    }
}
