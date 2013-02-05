package demo.wseventing.services;

import javax.jws.WebService;

import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForManagers;
import org.apache.cxf.ws.eventing.manager.AbstractSubscriptionManager;

import demo.wseventing.SingletonSubscriptionManagerContainer;

@WebService(endpointInterface = "org.apache.cxf.ws.eventing.manager.SubscriptionManagerEndpoint")
public class TestSubscriptionManager extends AbstractSubscriptionManager {

    @Override
    protected SubscriptionManagerInterfaceForManagers getSubscriptionManagerBackend() {
        return SingletonSubscriptionManagerContainer.getInstance();
    }
}
