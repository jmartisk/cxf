/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.ws.eventing.backend.notification;


import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.shared.utils.FilteringUtil;


/**
 * The service which takes care of notifying subscribers about events. Has access to the subscription database.
 * Receives events from compliant Emitters, eg. EmitterServlet / EmitterMBean,..
 * Don't forget to use the 'stop' method, especially if running inside a servlet container!!
 * Suggested approach for a web container is to instantiate this class in a ServletContextListener
 * and then have it stopped using the same listener. If you don't call 'stop' upon undeployment,
 * the underlying ExecutorService will not be shut down, leaking resources.
 */
public abstract class NotificatorService {

    public static final int CORE_POOL_SIZE = 15;
    protected static final Logger LOG = LogUtils.getLogger(NotificatorService.class);
    private ExecutorService service;

    public NotificatorService() {

    }

    /**
     * The NotificatorService will always ask for the current list of subscriptions after it
     * receives an event to handle. The list can contain expired subscriptions or those
     * not conforming to current event's filters. NotificatorService will take care of filtering them.
     */
    protected abstract List<SubscriptionTicket> obtainSubscriptions();

    protected abstract Class getEventSinkInterface();

    /**
     * Call this method when an WS-Eventing event appears. It will pass the event to this NotificatorService,
     * which will then take care of notifying the subscribers.
     *
     * @param eventAction the WS-Addressing action associated with the event
     * @param message     the actual XML payload of the event
     * @throws IllegalStateException if this NotificatorService is not started
     */
    @Deprecated
    public void dispatch(URI eventAction, Element message) {
        LOG.info("NotificatorService received an event with payload: " + message);
        if (service == null) {
            throw new IllegalStateException("NotificatorService is not started. "
                    + "Please call the start() method before passing any events to it.");
        }
        for (SubscriptionTicket ticket : obtainSubscriptions()) {
            LOG.info("ticket: " + ticket.getUuid());
            if (FilteringUtil.doesConformToFilter(message, ticket.getFilter())) {
                if (!ticket.isExpired()) {
                    service.submit(new NotificationTask(ticket, eventAction, message));
                } else {
                    LOG.info("Ticket expired at " + ticket.getExpires().toXMLFormat());
                }
            } else {
                LOG.info("Filter " + ticket.getFilter() + " doesn't apply to this message.");
            }
        }
    }

    public void dispatch(Object event) {
        LOG.info("NotificatorService received an event: " + event);
        if (service == null) {
            throw new IllegalStateException("NotificatorService is not started. "
                    + "Please call the start() method before passing any events to it.");
        }
        for (SubscriptionTicket ticket : obtainSubscriptions()) {
            if (!ticket.isExpired()) {
                service.submit(new NotificationTask(ticket, event, getEventSinkInterface()));
            } else {
                LOG.info("Ticket expired at " + ticket.getExpires().toXMLFormat());
            }
        }
    }

    /**
     * Starts this NotificatorService. You MUST run this method on every instance
     * before starting to pass any events to it. Run it only once.
     */
    public void start() {
        service = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    }

    /**
     * Shuts down the NotificatorService. This method is a MUST if you are running it inside a servlet container,
     * because it will shutdown the underlying ExecutorService.
     */
    public void stop() {
        service.shutdown();
    }

}
