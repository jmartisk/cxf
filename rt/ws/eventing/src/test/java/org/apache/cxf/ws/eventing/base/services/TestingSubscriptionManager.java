package org.apache.cxf.ws.eventing.base.services;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForManagers;
import org.apache.cxf.ws.eventing.backend.manager.aux.SingletonSubscriptionManagerContainer;
import org.apache.cxf.ws.eventing.manager.AbstractSubscriptionManager;

import javax.jws.WebService;

/**
 * Simple Subscription Manager webservice for integration tests
 */
@WebService(endpointInterface = "org.apache.cxf.ws.eventing.manager.SubscriptionManagerEndpoint")
public class TestingSubscriptionManager extends AbstractSubscriptionManager {

    @Override
    protected SubscriptionManagerInterfaceForManagers getSubscriptionManagerBackend() {
        return SingletonSubscriptionManagerContainer.getInstance();
    }

}