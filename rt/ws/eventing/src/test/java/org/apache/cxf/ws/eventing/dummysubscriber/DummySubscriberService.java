package org.apache.cxf.ws.eventing.dummysubscriber;

import org.apache.cxf.ws.eventing.subscription.client.EventSinkInterface;

import javax.jws.WebParam;

/**
 * @author jmartisk
 * @since 9/3/12
 */
public class DummySubscriberService implements EventSinkInterface {

    @Override
    public void notification(@WebParam Object notification) {
        System.out.println(notification);
    }

}
