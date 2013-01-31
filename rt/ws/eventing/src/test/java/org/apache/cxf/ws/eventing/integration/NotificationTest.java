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

package org.apache.cxf.ws.eventing.integration;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.ws.eventing.AttributedURIType;
import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.FormatType;
import org.apache.cxf.ws.eventing.NotifyTo;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.backend.notification.NotificatorService;
import org.apache.cxf.ws.eventing.backend.notification.emitters.Emitter;
import org.apache.cxf.ws.eventing.backend.notification.emitters.EmitterImpl;
import org.apache.cxf.ws.eventing.base.SimpleEventingIntegrationTest;
import org.apache.cxf.ws.eventing.base.TestUtil;
import org.apache.cxf.ws.eventing.base.aux.SingletonSubscriptionManagerContainer;
import org.apache.cxf.ws.eventing.integration.eventsink.TestingEventSinkImpl;
import org.apache.cxf.ws.eventing.integration.notificationapi.CatastrophicEventSink;
import org.apache.cxf.ws.eventing.integration.notificationapi.FireEvent;
import org.apache.cxf.ws.eventing.integration.notificationapi.assertions.ReferenceParametersAssertingHandler;
import org.apache.cxf.ws.eventing.integration.notificationapi.assertions.WSAActionAssertingHandler;
import org.apache.cxf.ws.eventing.shared.utils.DurationAndDateUtil;
import org.junit.Ignore;
import org.junit.Test;

public class NotificationTest extends SimpleEventingIntegrationTest {

    private NotificatorService createService() {
        return new NotificatorService() {

            @Override
            protected List<SubscriptionTicket> obtainSubscriptions() {
                return SingletonSubscriptionManagerContainer.getInstance().getTickets();
            }

            @Override
            protected Class getEventSinkInterface() {
                return CatastrophicEventSink.class;
            }
        };
    }

    private Server createEventSink(String address) {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingEventSinkImpl());
        factory.setAddress(address);
        return factory.create();
    }

    private Server createEventSinkWithWSAActionAssertion(String address, String action) {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingEventSinkImpl());
        factory.setAddress(address);
        factory.getHandlers().add(new WSAActionAssertingHandler(action));
        return factory.create();
    }

    private Server createEventSinkWithReferenceParametersAssertion(String address, ReferenceParametersType params) {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingEventSinkImpl());
        factory.setAddress(address);
        factory.getHandlers().add(new ReferenceParametersAssertingHandler(params));
        return factory.create();
    }


    @Test
    public void basicReceptionOfEvents() throws IOException {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSinkERT = new EndpointReferenceType();

        AttributedURIType eventSinkAddr = new AttributedURIType();
        String url = TestUtil.generateRandomURLWithLocalTransport();
        eventSinkAddr.setValue(url);
        eventSinkERT.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSinkERT);


        eventSourceClient.subscribeOp(subscribe);
        eventSourceClient.subscribeOp(subscribe);
        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSink(url);
        TestingEventSinkImpl.RECEIVED_FIRES.set(0);

        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent("Canada", 8));
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 3) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        eventSinkServer.stop();
        if (TestingEventSinkImpl.RECEIVED_FIRES.get() != 3) {
            Assert.fail("TestingEventSinkImpl should have received 3 events but received "
                + TestingEventSinkImpl.RECEIVED_FIRES.get());
        }
    }

    @Test
    public void withWSAAction() throws Exception {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSinkERT = new EndpointReferenceType();

        AttributedURIType eventSinkAddr = new AttributedURIType();
        String url = TestUtil.generateRandomURLWithLocalTransport();
        eventSinkAddr.setValue(url);
        eventSinkERT.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSinkERT);


        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSinkWithWSAActionAssertion(url, "http://www.fire.com");
        TestingEventSinkImpl.RECEIVED_FIRES.set(0);
        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent("Canada", 8));
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 1) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        eventSinkServer.stop();
        if (TestingEventSinkImpl.RECEIVED_FIRES.get() != 1) {
            Assert.fail("TestingEventSinkImpl should have received 1 events but received "
                    + TestingEventSinkImpl.RECEIVED_FIRES.get());
        }
    }

    @Test
    public void withReferenceParameters() throws Exception {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSinkERT = new EndpointReferenceType();

        JAXBElement idqn = new JAXBElement(new QName("http://www.example.org", "MyReferenceParameter"),
                String.class,
                "380");
        JAXBElement idqn2 = new JAXBElement(new QName("http://www.example.org", "MyReferenceParameter2"),
                String.class,
                "381");
        eventSinkERT.setReferenceParameters(new ReferenceParametersType());
        eventSinkERT.getReferenceParameters().getAny().add(idqn);
        eventSinkERT.getReferenceParameters().getAny().add(idqn2);
        AttributedURIType eventSinkAddr = new AttributedURIType();
        String url = TestUtil.generateRandomURLWithLocalTransport();
        eventSinkAddr.setValue(url);
        eventSinkERT.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSinkERT);


        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSinkWithReferenceParametersAssertion(url,
                eventSinkERT.getReferenceParameters());
        TestingEventSinkImpl.RECEIVED_FIRES.set(0);
        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent("Canada", 8));
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 1) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        eventSinkServer.stop();
        int received = TestingEventSinkImpl.RECEIVED_FIRES.get();
        if (received != 1) {
            Assert.fail("TestingEventSinkImpl should have received 1 events but received "
                    + received);
        }
    }

    @Test
    @Ignore
    public void withWSAActionWrapped() throws Exception {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSinkERT = new EndpointReferenceType();

        AttributedURIType eventSinkAddr = new AttributedURIType();
        String url = TestUtil.generateRandomURLWithLocalTransport();
        eventSinkAddr.setValue(url);
        eventSinkERT.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.setFormat(new FormatType());
        subscribe.getFormat().setName("http://www.w3.org/2011/03/ws-evt/DeliveryFormats/Wrap");
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSinkERT);


        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSinkWithWSAActionAssertion(url, "http://www.fire.com");
        TestingEventSinkImpl.RECEIVED_FIRES.set(0);
        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent("Canada", 8));
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 1) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        eventSinkServer.stop();
        if (TestingEventSinkImpl.RECEIVED_FIRES.get() != 1) {
            Assert.fail("TestingEventSinkImpl should have received 1 events but received "
                    + TestingEventSinkImpl.RECEIVED_FIRES.get());
        }
    }



    /**
     * We request only to receive notifications about fires in Canada
     * and there will be a fire in Canada. We should receive
     * this notification.
     */
    @Test
    public void withFilter() throws IOException {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSinkERT = new EndpointReferenceType();

        AttributedURIType eventSinkAddr = new AttributedURIType();
        String url = TestUtil.generateRandomURLWithLocalTransport();
        eventSinkAddr.setValue(url);
        eventSinkERT.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSinkERT);

        subscribe.setFilter(new FilterType());
        subscribe.getFilter().getContent().add("//location[text()='Canada']");


        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSink(url);
        TestingEventSinkImpl.RECEIVED_FIRES.set(0);

        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent("Canada", 8));
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 1) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        eventSinkServer.stop();
        if (TestingEventSinkImpl.RECEIVED_FIRES.get() != 1) {
            Assert.fail("TestingEventSinkImpl should have received 1 events but received "
                    + TestingEventSinkImpl.RECEIVED_FIRES.get());
        }
    }

    /**
     * We request only to receive notifications about fires in Russia
     * and there will be only a fire in Canada. We should not receive
     * this notification.
     */
    @Test
    public void withFilterNegative() throws IOException {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSinkERT = new EndpointReferenceType();

        AttributedURIType eventSinkAddr = new AttributedURIType();
        String url = TestUtil.generateRandomURLWithLocalTransport();
        eventSinkAddr.setValue(url);
        eventSinkERT.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSinkERT);

        subscribe.setFilter(new FilterType());
        subscribe.getFilter().getContent().add("//location[text()='Russia']");


        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSink(url);
        TestingEventSinkImpl.RECEIVED_FIRES.set(0);

        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent("Canada", 8));
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 0) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        eventSinkServer.stop();
        if (TestingEventSinkImpl.RECEIVED_FIRES.get() != 0) {
            Assert.fail("TestingEventSinkImpl should have received 0 events but received "
                    + TestingEventSinkImpl.RECEIVED_FIRES.get());
        }
    }


    /**
     * We request only to receive notifications about fires in Russia
     * and there will be one fire in Canada and one in Russia. We should
     * receive only one notification.
     */
    @Test
    public void withFilter2() throws IOException {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSinkERT = new EndpointReferenceType();

        AttributedURIType eventSinkAddr = new AttributedURIType();
        String url = TestUtil.generateRandomURLWithLocalTransport();
        eventSinkAddr.setValue(url);
        eventSinkERT.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSinkERT);

        subscribe.setFilter(new FilterType());
        subscribe.getFilter().getContent().add("//location[text()='Russia']");


        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSink(url);
        TestingEventSinkImpl.RECEIVED_FIRES.set(0);

        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent("Canada", 8));
        emitter.dispatch(new FireEvent("Russia", 8));
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 1) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        eventSinkServer.stop();
        if (TestingEventSinkImpl.RECEIVED_FIRES.get() != 1) {
            Assert.fail("TestingEventSinkImpl should have received 0 events but received "
                    + TestingEventSinkImpl.RECEIVED_FIRES.get());
        }
    }

}
