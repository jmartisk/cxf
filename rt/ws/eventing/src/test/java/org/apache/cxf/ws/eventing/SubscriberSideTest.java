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

package org.apache.cxf.ws.eventing;

import junit.framework.Assert;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.ws.eventing.eventsource.AbstractEventSource;
import org.apache.cxf.ws.eventing.manager.AbstractSubscriptionManager;
import org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForManagers;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerInterfaceForEventSources;
import org.apache.cxf.ws.eventing.backend.manager.aux.SingletonSubscriptionManagerContainer;
import org.apache.cxf.ws.eventing.shared.utils.DurationAndDateUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jws.WebService;
import java.io.IOException;


/**
 * 
 */
public class SubscriberSideTest {

    static Server eventSource;
    static Server subscriptionManager;

    private EventSourceEndpoint client;

    @WebService(endpointInterface = "org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint")
    public static class TestingEventSource extends AbstractEventSource {


        @Override
        protected SubscriptionManagerInterfaceForEventSources getSubscriptionManagerBackend() {
            return SingletonSubscriptionManagerContainer.getInstance();
        }

        @Override
        protected String getSubscriptionManagerURL() {
            return "local://SimpleSubscriptionManager";
        }
    }

    @WebService(endpointInterface = "org.apache.cxf.ws.eventing.manager.SubscriptionManagerEndpoint")
    public static class TestingSubscriptionManager extends AbstractSubscriptionManager {

        @Override
        protected SubscriptionManagerInterfaceForManagers getSubscriptionManagerBackend() {
            return SingletonSubscriptionManagerContainer.getInstance();
        }

    }

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingEventSource());
        factory.setAddress("local://SimpleEventSource");
        factory.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        eventSource = factory.create();

        factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingSubscriptionManager());
        factory.setAddress("local://SimpleSubscriptionManager");
        factory.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        subscriptionManager = factory.create();
    }

    /**
     * @throws Exception
     */
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
        factory.setAddress("local://SimpleEventSource");
        client = (EventSourceEndpoint)factory.create();
    }

    /**
     * specification:
     * The expiration time MAY be either a specific time or a duration but MUST
     * be of the same type as the wse:Expires element of the corresponding request.
     * If the corresponding request did not contain a wse:Expires element, this
     * element MUST be a duration (xs:duration).
     * @throws IOException
     */
    @Test
    public void testExpirationGranting() throws IOException {
        // we specify a xs:duration
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);
        DeliveryType delivery = new DeliveryType();
        subscribe.setDelivery(delivery);
        SubscribeResponse resp = client.subscribeOp(subscribe);
        Assert.assertTrue("Specification requires that EventSource return a xs:duration expirationType if a xs:duration was requested by client",
                DurationAndDateUtil.isDuration(resp.getGrantedExpires().getValue()));

        // we specify a xs:dateTime
        subscribe = new Subscribe();
        exp = new ExpirationType();
        exp.setValue(DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("2138-06-26T12:23:12.000-01:00")));
        subscribe.setExpires(exp);
        delivery = new DeliveryType();
        subscribe.setDelivery(delivery);
        resp = client.subscribeOp(subscribe);
        Assert.assertTrue("Specification requires that EventSource return a xs:dateTime expirationType if a xs:dateTime was requested by client",
                DurationAndDateUtil.isXMLGregorianCalendar(resp.getGrantedExpires().getValue()));

        // we don't specify anything
        subscribe = new Subscribe();
        delivery = new DeliveryType();
        subscribe.setDelivery(delivery);
        resp = client.subscribeOp(subscribe);
        Assert.assertTrue("Specification requires that EventSource return a xs:duration expirationType if no specific expirationType was requested by client",
                DurationAndDateUtil.isDuration(resp.getGrantedExpires().getValue()));
    }

}
