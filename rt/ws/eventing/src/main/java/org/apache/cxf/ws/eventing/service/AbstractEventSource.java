package org.apache.cxf.ws.eventing.service;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.SubscribeResponse;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.subscription.manager.SubscriptionManagerInterfaceForEventSources;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public abstract class AbstractEventSource implements EventSourceEndpoint {

    public AbstractEventSource() {
        System.out.println("CONSTRUCTOR");
    }

    @Resource
    protected WebServiceContext context;


    protected static final Logger LOG = LogUtils.getLogger(AbstractEventSource.class);

    @Override
    public SubscribeResponse subscribeOp(Subscribe body) {
        MessageContext soapMsgContext = context.getMessageContext();
        LOG.severe("webservice context: " + context.toString());
        LOG.severe("soapMsgContext: " + soapMsgContext);

        System.out.println(((WrappedMessageContext) context.getMessageContext()).getWrappedMessage().getContextualProperty("test"));

        SubscriptionTicket ticket = getSubscriptionManagerBackend().subscribe(body.getDelivery(), body.getEndTo(), body.getExpires(), body.getFilter());
        return generateResponseMessageFor(ticket);
    }


    protected abstract SubscriptionManagerInterfaceForEventSources getSubscriptionManagerBackend();


    protected SubscribeResponse generateResponseMessageFor(SubscriptionTicket ticket) {
        SubscribeResponse ret = new SubscribeResponse();
        ret.setGrantedExpires(ticket.getExpires());
        return ret;
    }

}
