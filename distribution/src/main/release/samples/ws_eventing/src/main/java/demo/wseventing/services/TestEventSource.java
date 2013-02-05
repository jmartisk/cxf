package demo.wseventing.services;


import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForEventSources;
import org.apache.cxf.ws.eventing.eventsource.AbstractEventSource;

import demo.wseventing.SingletonSubscriptionManagerContainer;

@WebService(endpointInterface = "org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint")
public class TestEventSource extends AbstractEventSource {

    @Resource
    WebServiceContext ctx;

    @Override
    protected SubscriptionManagerInterfaceForEventSources getSubscriptionManagerBackend() {
        return SingletonSubscriptionManagerContainer.getInstance();
    }

}
