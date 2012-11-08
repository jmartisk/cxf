package org.apache.cxf.ws.eventing.notification;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.utils.FilteringUtil;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

/**
 * The service which takes care of notifying subscribers about events. Has access to the subscription database.
 * Receives events from compliant Emitters, eg. EmitterServlet / EmitterMBean,..
 */
public abstract class NotificatorService {

    public static final int CORE_POOL_SIZE = 15;
    protected static final Logger LOG = LogUtils.getLogger(NotificatorService.class);
    private ExecutorService service;

    public NotificatorService() {
        service = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    }

    protected abstract List<SubscriptionTicket> obtainSubscriptions();

    public void dispatch(URI eventAction, Element message) {
        for(SubscriptionTicket ticket : obtainSubscriptions()) {
            if(FilteringUtil.doesConformToFilter(message, ticket.getFilter()) && !ticket.isExpired()) {
                service.submit(new NotificationTask(ticket, eventAction, message));
            }
        }
    }

}
