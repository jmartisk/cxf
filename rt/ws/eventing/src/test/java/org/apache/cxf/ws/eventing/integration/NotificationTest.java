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
import org.apache.cxf.ws.eventing.NotifyTo;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.backend.notification.NotificatorService;
import org.apache.cxf.ws.eventing.backend.notification.emitters.Emitter;
import org.apache.cxf.ws.eventing.backend.notification.emitters.EmitterImpl;
import org.apache.cxf.ws.eventing.base.SimpleEventingIntegrationTest;
import org.apache.cxf.ws.eventing.base.aux.SingletonSubscriptionManagerContainer;
import org.apache.cxf.ws.eventing.integration.eventsink.TestingEventSinkImpl;
import org.apache.cxf.ws.eventing.integration.notificationapi.CatastrophicEventSink;
import org.apache.cxf.ws.eventing.integration.notificationapi.FireEvent;
import org.apache.cxf.ws.eventing.shared.utils.DurationAndDateUtil;
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

    private Server createEventSink() {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new TestingEventSinkImpl());
        factory.setAddress("local://EventSink");
        return factory.create();
    }


    @Test
    public void doit() throws IOException {
        NotificatorService service = createService();
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(
                DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);

        EndpointReferenceType eventSink = new EndpointReferenceType();

        JAXBElement idqn = new JAXBElement(new QName("http://www.example.org", "MyReferenceParameter"),
                String.class,
                "380");
        eventSink.setReferenceParameters(new ReferenceParametersType());
        eventSink.getReferenceParameters().getAny().add(idqn);
        AttributedURIType eventSinkAddr = new AttributedURIType();
        eventSinkAddr.setValue("local://EventSink");
        eventSink.setAddress(eventSinkAddr);
        subscribe.setDelivery(new DeliveryType());
        subscribe.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)subscribe.getDelivery().getContent().get(0)).setValue(eventSink);


        eventSourceClient.subscribeOp(subscribe);
        eventSourceClient.subscribeOp(subscribe);
        eventSourceClient.subscribeOp(subscribe);

        Server eventSinkServer = createEventSink();

        service.start();
        Emitter emitter = new EmitterImpl(service);
        emitter.dispatch(new FireEvent());
        for (int i = 0; i < 10; i++) {
            if (TestingEventSinkImpl.RECEIVED_FIRES.get() == 3) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        Assert.fail("TestingEventSinkImpl should have received 3 events but received "
                + TestingEventSinkImpl.RECEIVED_FIRES.get());
        eventSinkServer.stop();
        eventSinkServer.destroy();
    }

}
