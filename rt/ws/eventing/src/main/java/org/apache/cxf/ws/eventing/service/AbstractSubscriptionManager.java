package org.apache.cxf.ws.eventing.service;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.ws.eventing.*;
import org.apache.cxf.ws.eventing.faults.UnknownSubscription;
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
        RenewResponse response = new RenewResponse();
        String uuid = retrieveSubscriptionUUID();
        LOG.info("received Renew message for UUID=" + uuid);
        ExpirationType expiration = getSubscriptionManagerBackend().renew(UUID.fromString(uuid), body.getExpires());
        response.setGrantedExpires(expiration);
        LOG.info("Extended subscription for UUID=" + uuid + " to " + expiration.getValue());
        return response;
    }

    @Override
    public GetStatusResponse getStatusOp(GetStatus body) {
        String uuid = retrieveSubscriptionUUID();
        LOG.info("received GetStatus message for UUID=" + uuid);
        SubscriptionTicket ticket = obtainTicketFromDatabaseOrThrowFault(uuid);
        GetStatusResponse response = new GetStatusResponse();
        response.setGrantedExpires(DurationAndDateUtil.toExpirationTypeContainingGregorianCalendar(ticket.getExpires()));
        return response;
    }

    @Override
    public UnsubscribeResponse unsubscribeOp(Unsubscribe body) {
        String uuid = retrieveSubscriptionUUID();
        LOG.info("received Unsubscribe message for UUID=" + uuid);
        getSubscriptionManagerBackend().unsubscribeTicket(UUID.fromString(uuid));
        LOG.info("successfully removed subscription with UUID " + uuid);
        return new UnsubscribeResponse();
    }

    protected abstract SubscriptionManagerInterfaceForManagers getSubscriptionManagerBackend();

    /**
     * Retrieves the subscription's uuid as it was specified in SOAP header.
     * Messages sent to SubscriptionManager by clients always need to specify the uuid.
     *
     * @return the uuid of the subscription specified in this message's headers. Note:
     *         obtaining this doesn't yet make sure that this subscription actually exists.
     */
    protected String retrieveSubscriptionUUID() {
        Object uuid = (((WrappedMessageContext) context.getMessageContext()).getWrappedMessage().getContextualProperty("uuid"));
        if(uuid==null)
            throw new UnknownSubscription();
        if(uuid.getClass() != String.class)
            throw new Error("Susbcription ID should be a String but is " + uuid.getClass().getName());
        return (String) uuid;
    }

    /**
     * searches the subscription database for a subscription by the given UUID
     * @param uuid
     * @return the SubscriptionTicket, or throws UnknownSubscription fault if no such subscription exists
     */
    protected SubscriptionTicket obtainTicketFromDatabaseOrThrowFault(String uuid) {
        SubscriptionTicket ticket = getSubscriptionManagerBackend().findTicket(UUID.fromString(uuid));
        if (ticket == null) {
            LOG.severe("Unknown ticket UUID: " + uuid);
            throw new UnknownSubscription();
        }
        return ticket;
    }

}
