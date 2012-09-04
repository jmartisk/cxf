package org.apache.cxf.ws.eventing.dummysubscriber;

import org.apache.cxf.ws.eventing.subscription.client.SubscriberService;
import org.w3c.dom.Element;

import javax.jws.WebParam;

/**
 * @author jmartisk
 * @since 9/3/12
 */
public class DummySubscriberService implements SubscriberService {

    @Override
    public Object notification(@WebParam Object notification) {
        return notification;
    }

}
