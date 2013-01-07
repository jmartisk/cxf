package org.apache.cxf.ws.eventing.eventsource;


import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.SubscribeResponse;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForEventSources;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionTicketGrantingResponse;
import org.apache.cxf.ws.eventing.shared.utils.DurationAndDateUtil;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public abstract class AbstractEventSource implements EventSourceEndpoint {

    public AbstractEventSource() {
    }

    @Resource
    protected WebServiceContext context;

    protected static final Logger LOG = LogUtils.getLogger(AbstractEventSource.class);

    @Override
    public SubscribeResponse subscribeOp(Subscribe body) {
        SubscriptionTicketGrantingResponse databaseResponse = getSubscriptionManagerBackend().subscribe(body.getDelivery(), body.getEndTo(), body.getExpires(), body.getFilter());
        boolean shouldConvertToDuration;
        if(body.getExpires() != null) {
            shouldConvertToDuration = DurationAndDateUtil.isDuration(body.getExpires().getValue());
        } else {
            shouldConvertToDuration = true;
        }
        return generateResponseMessageFor(databaseResponse, shouldConvertToDuration);
    }

    protected abstract SubscriptionManagerInterfaceForEventSources getSubscriptionManagerBackend();

    protected abstract String getSubscriptionManagerURL();

    protected SubscribeResponse generateResponseMessageFor(SubscriptionTicketGrantingResponse dbResponse, boolean shouldConvertToDuration) {
        SubscribeResponse ret = new SubscribeResponse();
        // SubscriptionManager part
        ret.setSubscriptionManager(dbResponse.getSubscriptionManagerReference());
        // Expires part
        if(shouldConvertToDuration) {
            ret.setGrantedExpires(DurationAndDateUtil.toExpirationTypeContainingDuration(dbResponse.getExpires()));
        } else {
            ret.setGrantedExpires(DurationAndDateUtil.toExpirationTypeContainingGregorianCalendar(dbResponse.getExpires()));
        }
        return ret;
    }


}
