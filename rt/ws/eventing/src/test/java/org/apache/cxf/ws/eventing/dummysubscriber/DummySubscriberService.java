package org.apache.cxf.ws.eventing.dummysubscriber;

import org.apache.cxf.ws.eventing.SubscriptionEnd;
import org.apache.cxf.ws.eventing.subscription.client.EventSinkInterface;

/**
 * @author jmartisk
 * @since 9/3/12
 */
public class DummySubscriberService implements EventSinkInterface {

    @Override
    public void notification(Object notification) {
        System.out.println(notification);
    }

    @Override
    public void subscriptionEnd(SubscriptionEnd subscriptionEnd) {

    }

}
