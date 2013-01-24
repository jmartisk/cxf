package org.apache.cxf.ws.eventing.base.services;

import org.apache.cxf.ws.eventing.SubscriptionEnd;
import org.apache.cxf.ws.eventing.client.EventSinkInterface;

public class TestingEventSink implements EventSinkInterface {

    @Override
    public void notification(Object notification) {
        System.out.println(notification);
    }

    @Override
    public void subscriptionEnd(SubscriptionEnd subscriptionEnd) {

    }

}
