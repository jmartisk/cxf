package org.apache.cxf.ws.eventing.backend.manager;

/**
 * The core functionality representing WS-Eventing backend logic. It holds an instance of a database and
 * acts as a layer for communicating with it. There are two interfaces which are used to communicate
 * with a SubscriptionManager:
 * - SubscriptionManagerInterfaceForManagers is used by the manager Web Service
 * - SubscriptionManagerInterfaceForEventSources is used by the event source Web Service
 */
public interface SubscriptionManager
        extends SubscriptionManagerInterfaceForManagers, SubscriptionManagerInterfaceForEventSources {
}
