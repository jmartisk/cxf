package org.apache.cxf.ws.eventing.client;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.SubscribeResponse;
import org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint;

import java.io.IOException;

/**
 * This is a possible client for communicating with a remote Event Source. It is used to request subscriptions from the Event Source.
 */
public class SubscriptionRequestorClient {

    private final String EVENT_SOURCE_URL;

    private EventSourceEndpoint endpoint;

    public SubscriptionRequestorClient(String EVENT_SOURCE_URL) {
        this.EVENT_SOURCE_URL = EVENT_SOURCE_URL;
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(EventSourceEndpoint.class);
        factory.setAddress(EVENT_SOURCE_URL);
        factory.getOutInterceptors().add(new LoggingOutInterceptor()); //debug
        factory.getInInterceptors().add(new LoggingInInterceptor());          // debug
        endpoint = (EventSourceEndpoint)factory.create();
    }

    public SubscribeResponse subscribe(Subscribe message) throws IOException {
        return endpoint.subscribeOp(message);
    }

}
