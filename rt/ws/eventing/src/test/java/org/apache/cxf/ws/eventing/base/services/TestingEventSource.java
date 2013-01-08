package org.apache.cxf.ws.eventing.base.services;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForEventSources;
import org.apache.cxf.ws.eventing.backend.manager.aux.SingletonSubscriptionManagerContainer;
import org.apache.cxf.ws.eventing.base.SimpleEventingIntegrationTest;
import org.apache.cxf.ws.eventing.eventsource.AbstractEventSource;

import javax.jws.WebService;

/**
 * Simple Event Source webservice for integration tests
 */
@WebService(endpointInterface = "org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint")
public class TestingEventSource extends AbstractEventSource {


    @Override
    protected SubscriptionManagerInterfaceForEventSources getSubscriptionManagerBackend() {
        return SingletonSubscriptionManagerContainer.getInstance();
    }

    @Override
    protected String getSubscriptionManagerURL() {
        return SimpleEventingIntegrationTest.URL_SUBSCRIPTION_MANAGER;
    }
}
