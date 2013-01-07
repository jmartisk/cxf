package org.apache.cxf.ws.eventing.client;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.eventing.*;
import org.apache.cxf.ws.eventing.handlers.SubscriptionReferenceAddingHandler;
import org.apache.cxf.ws.eventing.service.SubscriptionManagerEndpoint;

/**
 * This is an example client for communicating with a remote Subscription Manager.
 */
public class SubscriptionManagerClient {

    private final String SUBSCRIPTION_MANAGER_URL;
    private SubscriptionManagerEndpoint endpoint;



    public SubscriptionManagerClient(String SUBSCRIPTION_MANAGER_URL, ReferenceParametersType subscriptionReferenceParams) {
        this.SUBSCRIPTION_MANAGER_URL = SUBSCRIPTION_MANAGER_URL;
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SubscriptionManagerEndpoint.class);
        factory.setAddress(SUBSCRIPTION_MANAGER_URL);
        SubscriptionReferenceAddingHandler handler = new SubscriptionReferenceAddingHandler(subscriptionReferenceParams);
        factory.getHandlers().add(handler);
        factory.getOutInterceptors().add(new LoggingOutInterceptor()); //debug
        factory.getInInterceptors().add(new LoggingInInterceptor());          // debug
        endpoint = (SubscriptionManagerEndpoint)factory.create();
    }

    public GetStatusResponse getStatus() {
        GetStatus request = new GetStatus();
        return endpoint.getStatusOp(request);
    }

    public RenewResponse renew(Renew renewRequest) {
        return endpoint.renewOp(renewRequest);
    }

    public UnsubscribeResponse unsubscribe() {
        Unsubscribe request = new Unsubscribe();
        return endpoint.unsubscribeOp(request);
    }

}
