package org.apache.cxf.ws.eventing.backend.notification;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.client.EventSinkInterface;
import org.apache.cxf.ws.eventing.shared.handlers.SubscriptionReferenceAddingHandler;
import org.apache.cxf.ws.eventing.shared.handlers.WSAActionSettingHandler;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Represents the task to send a notification about a particular event to a particular subscribed client.
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

    /**
     * Logic needed to actually send the notification to the subscribed client.
     */
    @Override
    public void run() {
        LOG.info("Starting notification task for subscription UUID " + target.getUuid());

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(EventSinkInterface.class);
        factory.setAddress("http://localhost:8060"); // TODO <- subscriber address

        // needed SOAP handlers
        SubscriptionReferenceAddingHandler handler = new SubscriptionReferenceAddingHandler(new ReferenceParametersType());
        factory.getHandlers().add(handler);

        WSAActionSettingHandler actionSettingHandler = new WSAActionSettingHandler("http://ACTION");
        factory.getHandlers().add(actionSettingHandler);

        factory.getOutInterceptors().add(new LoggingOutInterceptor()); //debug
        factory.getInInterceptors().add(new LoggingInInterceptor());          // debug

        EventSinkInterface endpoint = (EventSinkInterface)factory.create();
        endpoint.notification(message);


       /* String eventXML = DOMWriter.printNode(event, false);
        MessageFactory msgFactory = MessageFactory.newInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/' ");
        sb.append("xmlns:wse='").append(EventingConstants.EVENTING_2011_03_NAMESPACE).append("' ");
        sb.append("xmlns:wsa='").append("http://www.w3.org/2005/08/addressing").append("'>");
        sb.append("<env:Header>");
        sb.append("<wsa:Action>").append("http://action").append("</wsa:Action>");
        sb.append("<wsa:To>").append("http://notify-to").append("</wsa:To>");
        sb.append("</env:Header>");
        sb.append("<env:Body>");
        sb.append(eventXML);
        sb.append("</env:Body>");
        sb.append("</env:Envelope>");

        SOAPConnectionFactory.newInstance().createConnection().call(, "http://localhost:8060");*/
        LOG.info("Done.");
    }


}
