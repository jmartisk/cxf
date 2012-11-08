package org.apache.cxf.ws.eventing.notification;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.client.EventSinkInterface;
import org.apache.cxf.ws.eventing.handlers.SubscriptionAddingHandler;
import org.apache.cxf.ws.eventing.handlers.WSAActionSettingHandler;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionTicket;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 11/8/12
 */
class NotificationTask implements Runnable {

    protected static final Logger LOG = LogUtils.getLogger(NotificationTask.class);

    SubscriptionTicket target;
    URI action;
    Element message;

    NotificationTask(SubscriptionTicket subscription, URI eventAction, Element message) {
        this.target = subscription;
        this.action = eventAction;
        this.message = message;
    }

    @Override
    public void run() {
        LOG.info("Starting notification task for subscription UUID " + target.getUuid());
        // TODO send notification
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(EventSinkInterface.class);
        factory.setAddress("http://localhost:8060"); // TODO <- subscriber address
        WSAActionSettingHandler actionSettingHandler = new WSAActionSettingHandler("http://ACTION");
        SubscriptionAddingHandler handler = new SubscriptionAddingHandler(new ReferenceParametersType());
        factory.getHandlers().add(handler);
        factory.getHandlers().add(actionSettingHandler);
        factory.getOutInterceptors().add(new LoggingOutInterceptor()); //debug
        factory.getInInterceptors().add(new LoggingInInterceptor());          // debug
        EventSinkInterface endpoint = (EventSinkInterface)factory.create();
        endpoint.notification(message);
        LOG.info("Done. (nothing yet)");
    }


}
