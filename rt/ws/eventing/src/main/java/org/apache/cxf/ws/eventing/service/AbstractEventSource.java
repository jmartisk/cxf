package org.apache.cxf.ws.eventing.service;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.*;
import org.apache.cxf.ws.eventing.faults.CannotProcessFilter;
import org.apache.cxf.ws.eventing.faults.UnsupportedExpirationValue;

import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class AbstractEventSource implements EventSourceInterface {

    private static final Logger LOG = LogUtils.getLogger(AbstractEventSource.class);


    //    public SubscribeResponse subscribeOp(@WebParam(name = "Subscribe", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body") Subscribe body) {
    @Override
    public SubscribeResponse subscribeOp(Subscribe body) {

        String dialect  = body.getFilter().getDialect();
        LOG.severe("dialect: " + dialect);

        for(Object o  : body.getFilter().getContent()) {
            LOG.severe("filter content: " + o.getClass() + " " + o.toString());
        }

        SubscribeResponse r = new SubscribeResponse();
        r.setGrantedExpires(new MiniExpirationType());
        r.getGrantedExpires().setValue("asdfg");
/*        SubscriptionTicket ticket = new SubscriptionTicket();
        ticket.setDelivery(body.getDelivery());
        ticket.setEndTo(body.getEndTo());
        ticket.setExpires(body.getExpires());
        ticket.setFilter(body.getFilter());
        EventingSubscriptionManager.getInstance().addTicket(ticket);
        MiniExpirationType met = new MiniExpirationType();
        met.setValue("21541");
        r.setGrantedExpires(met);*/
        throw new CannotProcessFilter();
//        return r;
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
}
