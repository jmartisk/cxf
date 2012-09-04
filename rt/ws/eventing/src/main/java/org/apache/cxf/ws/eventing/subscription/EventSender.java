package org.apache.cxf.ws.eventing.subscription;

import org.w3c.dom.Element;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author jmartisk
 * @since 9/3/12
 */
public class EventSender {

    public static final int CORE_POOL_SIZE = 15;

    private ExecutorService service;

    public EventSender() {
        service = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    }

    public void dispatch(URI targetDestination, URI eventAction, Element message) {
        service.submit(new SenderTask(targetDestination, eventAction, message));
    }

    class SenderTask implements Runnable {

        URI target;
        URI action;
        Element message;

        SenderTask(URI targetDestination, URI eventAction, Element message) {
            this.target = targetDestination;
            this.action = eventAction;
            this.message = message;
        }

        @Override
        public void run() {

        }
    }


}
