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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.binding.soap.JaxWsSoapBindingConfiguration;
import org.apache.cxf.jaxws.support.JaxWsServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.shared.handlers.ReferenceParametersAddingHandler;

/**
 * Represents the task to send a notification about a particular event to a particular subscribed client.
 */
class EventNotificationTask implements Runnable {

    protected static final Logger LOG = LogUtils.getLogger(EventNotificationTask.class);

    SubscriptionTicket target;
    URI action;
    Element message;
    Object event;
    Class endpointInterface;

    @Deprecated
    EventNotificationTask(SubscriptionTicket subscription, URI eventAction, Element message) {
        this.target = subscription;
        this.action = eventAction;
        this.message = message;
    }

    public EventNotificationTask(SubscriptionTicket ticket, Object event, Class endpointInterface) {
        this.target = ticket;
        this.event = event;
        this.endpointInterface = endpointInterface;

    }

    /**
     * Logic needed to actually send the notification to the subscribed client.
     */
    @Override
    public void run() {
        try {
            LOG.info("Starting notification task for subscription UUID " + target.getUuid());

            // needed SOAP handlers
            ReferenceParametersAddingHandler handler = new
                    ReferenceParametersAddingHandler(
                    target.getNotificationReferenceParams());
            // register filtering interceptors TODO


            if(target.isWrappedDelivery()) {
                // TODO wrapped delivery
                // service.getOutInterceptors().add(new EventingWrapperClassOutInterceptor());
                System.out.println("WRAPPED :)");
            } else {
                JaxWsProxyFactoryBean service = new JaxWsProxyFactoryBean();
                service.getOutInterceptors().add(new LoggingOutInterceptor());
                service.setServiceClass(endpointInterface);
                service.setAddress(target.getTargetURL());
                service.setTransportId(LocalTransportFactory.TRANSPORT_ID); // TODO generalize
                service.getHandlers().add(handler);

                Object endpoint = service.create();
                System.out.println(service.getBindingConfig() + ", " + service.getBindingConfig().getClass());
                JaxWsSoapBindingConfiguration f;
                System.out.println(((JaxWsSoapBindingConfiguration)service.getBindingConfig()).getStyle());
                System.out.println(((JaxWsSoapBindingConfiguration)service.getBindingConfig()).getUse());
                System.out.println(((JaxWsSoapBindingConfiguration)service.getBindingConfig()).getBindingName());
                System.out.println(((JaxWsSoapBindingConfiguration)service.getBindingConfig()).getBindingId());

                LOG.info("client CREATED");

                // find the method to use
                Method[] methods = endpointInterface.getMethods();
                Method method = null;
                for (Method i : methods) {
                    if (Arrays.equals(i.getParameterTypes(), new Class<?>[] {event.getClass()})) {
                        method = i;
                    }
                }

                if (method != null) {
                    try {
                        method.invoke(endpoint, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    LOG.severe("Couldn't find corresponding method for event of type "
                            + event.getClass().getCanonicalName() + " in event sink interface"
                            + endpointInterface.getCanonicalName());
                }
            }



//            JaxWsServiceFactoryBean iq = new JaxWsServiceFactoryBean();
//            iq.setServiceClass(endpointInterface);
//            Service s = i.create();
//            JaxWsServiceConfiguration config = (JaxWsServiceConfiguration )iq
// .getServiceConfigurations().get(0);
//            JaxWsServiceConfiguration customConfig  = new JaxWsServiceConfiguration();




        } catch (Throwable e) {
            e.printStackTrace();
        }

        LOG.info("Done.");
    }


}
