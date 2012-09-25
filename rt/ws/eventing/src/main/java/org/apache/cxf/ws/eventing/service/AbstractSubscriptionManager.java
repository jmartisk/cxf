package org.apache.cxf.ws.eventing.service;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.*;
import org.apache.cxf.ws.eventing.faults.UnsupportedExpirationValue;
import org.apache.cxf.ws.eventing.subscription.manager.SubscriptionManagerInterfaceForManagers;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public abstract class AbstractSubscriptionManager implements SubscriptionManagerEndpoint {

    public AbstractSubscriptionManager() {
        System.out.println("CONSTRUCTOR");
    }

    @Resource
    protected WebServiceContext context;

    protected static final Logger LOG = LogUtils.getLogger(AbstractSubscriptionManager.class);


    @Override
    public RenewResponse renewOp(Renew body) {
        throw new UnsupportedExpirationValue();
    }

    @Override
    public GetStatusResponse getStatusOp(GetStatus body) {
        throw new UnsupportedOperationException("AbstractEventSource.getStatusOp not implemented yet");
    }

    @Override
    public UnsubscribeResponse unsubscribeOp(Unsubscribe body) {
        throw new UnsupportedOperationException("AbstractEventSource.unsubscribeOp not implemented yet");
    }

    protected abstract SubscriptionManagerInterfaceForManagers getSubscriptionManagerBackend();

}
