package org.apache.cxf.ws.eventing.subscription;

import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.utils.FilteringUtil;

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
    private FilterType filter;

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
        return false; // TODO
    }
}
