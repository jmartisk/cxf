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

package org.apache.cxf.ws.eventing.base;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.ws.eventing.AttributedURIType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.NotifyTo;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.base.services.TestingEventSource;
import org.apache.cxf.ws.eventing.base.services.TestingSubscriptionManager;
import org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint;
import org.apache.cxf.ws.eventing.manager.SubscriptionManagerEndpoint;
import org.apache.cxf.ws.eventing.shared.handlers.ReferenceParametersAddingHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Prepared class for simple development of integration tests for WS-Eventing. Extend it and you will get:
 * - Event Source web service available at local://SimpleEventSource
 * - Subscription Manager web service available at local://SimpleSubscriptionManager
 * - These two services are connected together, using an IN-VM backend/database instance
 * - JAX-WS client for the Event Source [the eventSourceClient property]
 * - ability to create a JAX-WS client for the Subscription Manager (the createSubscriptionManagerClient method)
 */
public abstract class SimpleEventingIntegrationTest {

    public static final String URL_EVENT_SOURCE = "local://SimpleEventSource";
    public static final String URL_SUBSCRIPTION_MANAGER = "local://SimpleSubscriptionManager";

    static Server eventSource;
    static Server subscriptionManager;
    protected EventSourceEndpoint eventSourceClient;

    /**
     * Prepares the Event Source and Subscription Manager services
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // create and publish event source
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingEventSource());
        factory.setAddress(URL_EVENT_SOURCE);
        factory.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        eventSource = factory.create();

        // create and publish subscription manager
        factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingSubscriptionManager());
        factory.setAddress(URL_SUBSCRIPTION_MANAGER);
        factory.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        subscriptionManager = factory.create();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        eventSource.destroy();
        subscriptionManager.destroy();
    }

    @Before
    public void createClient() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        factory.setServiceClass(EventSourceEndpoint.class);
        factory.setAddress(URL_EVENT_SOURCE);
        eventSourceClient = (EventSourceEndpoint)factory.create();
    }

    /**
     * Convenience method to create a client for the testing Subscription Manager
     * which is located at local://SimpleSubscriptionManager.
     * You have to specify the reference parameters you obtained from the Event Source
     * when your subscription was created.
     *
     * @return a JAX-WS client set up for managing the subscription you had created using the Event Source
     */
    public SubscriptionManagerEndpoint createSubscriptionManagerClient(ReferenceParametersType refs) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(SubscriptionManagerEndpoint.class);
        factory.setAddress(URL_SUBSCRIPTION_MANAGER);
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        ReferenceParametersAddingHandler handler = new ReferenceParametersAddingHandler(refs);
        factory.getHandlers().add(handler);
        return (SubscriptionManagerEndpoint)factory.create();
    }

    protected NotifyTo createDummyNotifyTo() {
        NotifyTo ret = new NotifyTo();
        EndpointReferenceType eventSinkERT = new EndpointReferenceType();
        AttributedURIType eventSinkAddr = new AttributedURIType();
        eventSinkAddr.setValue("local://dummy-sink");
        eventSinkERT.setAddress(eventSinkAddr);
        ret.setValue(eventSinkERT);
        return ret;
    }

}
