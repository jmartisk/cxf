package org.apache.cxf.ws.eventing.service;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.ws.eventing.*;
import org.apache.cxf.ws.eventing.faults.UnknownSubscription;
import org.apache.cxf.ws.eventing.faults.UnsupportedExpirationValue;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.subscription.manager.SubscriptionManagerInterfaceForManagers;
import org.apache.cxf.ws.eventing.utils.DurationAndDateUtil;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public abstract class AbstractSubscriptionManager implements SubscriptionManagerEndpoint {

    public AbstractSubscriptionManager() {
    }

    @Resource
    protected WebServiceContext context;

    protected static final Logger LOG = LogUtils.getLogger(AbstractSubscriptionManager.class);


    @Override
    public RenewResponse renewOp(Renew body) {
        throw new UnsupportedExpirationValue();
    }

    @Override
    public GetStatusResponse getStatusOp(GetStatus body) {
        // obtain things stored in EventingSoapHandler
        String uuid = (String)(((WrappedMessageContext) context.getMessageContext()).getWrappedMessage().getContextualProperty("uuid"));
        LOG.info("received GetStatus message for UUID="+uuid);
        SubscriptionTicket ticket = getSubscriptionManagerBackend().getDatabase().findById(UUID.fromString(uuid));
        if (ticket != null) {
            GetStatusResponse response =  new GetStatusResponse();
            response.setGrantedExpires(DurationAndDateUtil.toExpirationTypeContainingGregorianCalendar(ticket.getExpires()));
            return response;
        } else {
            LOG.severe("Unknown ticket UUID: "+uuid);
            throw new UnknownSubscription();
        }
    }

    @Override
    public UnsubscribeResponse unsubscribeOp(Unsubscribe body) {
        throw new UnsupportedOperationException("AbstractEventSource.unsubscribeOp not implemented yet");
    }

    protected abstract SubscriptionManagerInterfaceForManagers getSubscriptionManagerBackend();

}
