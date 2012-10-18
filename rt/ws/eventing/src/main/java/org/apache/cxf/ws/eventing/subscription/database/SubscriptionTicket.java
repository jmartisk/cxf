package org.apache.cxf.ws.eventing.subscription.database;

import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.utils.FilteringUtil;

import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class SubscriptionTicket {

    public SubscriptionTicket() {
    }

    private EndpointReferenceType endTo;
    private DeliveryType delivery;
    private ExpirationType expires;
    private GregorianCalendar effectiveExpiresDate;
    private FilterType filter;
    private UUID uuid;


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

    public ExpirationType getExpires() {
        return expires;
    }

    public void setExpires(ExpirationType expires) {
        this.expires = expires;
    }

    public FilterType getFilter() {
        return filter;
    }

    public void setFilter(FilterType filter) {
        if(!FilteringUtil.isFilteringDialectSupported(filter.getDialect()))
            throw new FilteringRequestedUnavailable();
        this.filter = filter;
    }

    public boolean isExpired() {
        return effectiveExpiresDate.after(new GregorianCalendar());
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public GregorianCalendar getEffectiveExpiresDate() {
        return effectiveExpiresDate;
    }

    public void setEffectiveExpiresDate(GregorianCalendar effectiveExpiresDate) {
        this.effectiveExpiresDate = effectiveExpiresDate;
    }
}
