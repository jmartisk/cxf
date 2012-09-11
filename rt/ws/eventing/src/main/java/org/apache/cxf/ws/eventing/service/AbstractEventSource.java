package org.apache.cxf.ws.eventing.service;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.*;
import org.apache.cxf.ws.eventing.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.faults.UnsupportedExpirationValue;
import org.apache.cxf.ws.eventing.jaxbutils.DurationDateTime;
import org.apache.cxf.ws.eventing.subscription.EventingSubscriptionManager;
import org.apache.cxf.ws.eventing.subscription.SubscriptionTicket;
import org.apache.cxf.ws.eventing.utils.FilteringUtil;

import javax.xml.datatype.*;
import javax.xml.datatype.Duration;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class AbstractEventSource implements EventSourceInterface {

    private static final Logger LOG = LogUtils.getLogger(AbstractEventSource.class);

    @Override
    public SubscribeResponse subscribeOp(Subscribe body) {
        LOG.info("Received subscription request, starting processing.");
        SubscribeResponse response = new SubscribeResponse();
        SubscriptionTicket ticket = new SubscriptionTicket();

        processFilters(body, ticket, response);
        processExpiration(body, ticket, response);
        processEndTo(body, ticket, response);
        processDelivery(body, ticket, response);

        obtainSubscriptionManager().addTicket(ticket);

        return response;
    }

    @Override
    public RenewResponse renewOp(Renew body) {
        throw new UnsupportedExpirationValue();
    }

    @Override
    public GetStatusResponse getStatusOp(GetStatus body) {
        throw new UnsupportedOperationException("AbstractEventSource.getStatusOp not implemented yet");
    }

    @Override
    public UnsubscribeResponse unsubscribeOp(Unsubscribe body) {
        throw new UnsupportedOperationException("AbstractEventSource.unsubscribeOp not implemented yet");
    }

    private void processFilters(Subscribe request, SubscriptionTicket ticket, SubscribeResponse response) {
        if (request.getFilter() != null) {
            // test if the requested filtering dialect is supported
            if(FilteringUtil.isFilteringDialectSupported(request.getFilter().getDialect())) {
                // TODO test if the requested filter is valid
                for(Object o  : request.getFilter().getContent()) {
                    LOG.fine("Found filter content: " + o.getClass() + " " + o.toString());
                }
                ticket.setFilter(request.getFilter());
            } else {
                throw new FilteringRequestedUnavailable();
            }

        }
    }

    /*
     * process the stuff concerning expiration request (wse:Expires)
     */
    private void processExpiration(Subscribe body, SubscriptionTicket ticket, SubscribeResponse response) {
        ExpirationType requestedExpiration = body.getExpires();
        ExpirationType grantedExpiration = new ExpirationType();
        if (requestedExpiration != null) {
            Class expirationTypeClass = requestedExpiration.getValue().getDateTime().getClass();
            Object expirationTypeValue = requestedExpiration.getValue().getDateTime();
            LOG.info("ExpirationType's class: " + expirationTypeClass);
            LOG.info("ExpirationType's toString: " + expirationTypeValue.toString());
            if(expirationTypeValue instanceof XMLGregorianCalendar)
                grantedExpiration.setValue(new DurationDateTime(grantExpirationFor((XMLGregorianCalendar)expirationTypeValue)));
            else if(expirationTypeValue instanceof javax.xml.datatype.Duration)
                grantedExpiration.setValue(new DurationDateTime(grantExpirationFor((Duration)expirationTypeValue)));
        } else { // no expirationTime request was made
            grantedExpiration.setValue(new DurationDateTime(grantExpiration()));
        }
        response.setGrantedExpires(grantedExpiration);
        ticket.setExpires(grantedExpiration);
    }

    private void processEndTo(Subscribe body, SubscriptionTicket ticket, SubscribeResponse response) {
        EndpointReferenceType endTo = body.getEndTo();
        if(endTo != null) {
            // todo check it
            ticket.setEndTo(endTo);
        }
    }

    private void processDelivery(Subscribe body, SubscriptionTicket ticket, SubscribeResponse response) {
        // todo
    }

    protected XMLGregorianCalendar grantExpirationFor(XMLGregorianCalendar requested) {
        return requested;   // default
    }

    protected Duration grantExpirationFor(Duration requested) {
        return requested;   // default
    }

    protected javax.xml.datatype.Duration grantExpiration() {
        try {
            return DatatypeFactory.newInstance().newDuration("PT0S");  // default
        } catch(DatatypeConfigurationException ex) {
            throw new Error(ex);
        }
    }



    // temporary
    private final EventingSubscriptionManager manager = new EventingSubscriptionManager();

    public EventingSubscriptionManager obtainSubscriptionManager() {
        return manager; // temporary
    }

}
