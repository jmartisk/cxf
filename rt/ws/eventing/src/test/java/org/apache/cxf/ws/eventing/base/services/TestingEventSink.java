package org.apache.cxf.ws.eventing.base.services;

import org.apache.cxf.ws.eventing.SubscriptionEnd;
import org.apache.cxf.ws.eventing.client.EventSinkInterface;

/**
 * @author jmartisk
 * @since 9/3/12
 */
public class TestingEventSink implements EventSinkInterface {

    @Override
    public void notification(Object notification) {
        System.out.println(notification);
    }

    @Override
    public void subscriptionEnd(SubscriptionEnd subscriptionEnd) {

    }

}
