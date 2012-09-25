package org.apache.cxf.ws.eventing.eventing;

import org.apache.cxf.ws.eventing.subscription.database.SubscriptionDatabase;
import org.w3c.dom.Element;

import java.net.URI;

/**
 * @author jmartisk
 * @since 9/17/12
 */
public class RudimentarySingletonEventDispatcher implements EventDispatcher {

    private SubscriptionDatabase database;

    public RudimentarySingletonEventDispatcher() {

    }




    @Override
    public void dispatch(URI eventSourceNS, Element event) {
//        SubscriptionDatabaseImpl.getInstance().dispatchEvent(eventSourceNS, event);
    }
}
