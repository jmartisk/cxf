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
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;

/**
 * Represents the task to send a notification about a particular event to a particular subscribed client.
 */
class NotificationTask implements Runnable {

    protected static final Logger LOG = LogUtils.getLogger(NotificationTask.class);

    SubscriptionTicket target;
    URI action;
    Element message;

    NotificationTask(SubscriptionTicket subscription, URI eventAction, Element message) {
        this.target = subscription;
        this.action = eventAction;
        this.message = message;
    }

    /**
     * Logic needed to actually send the notification to the subscribed client.
     */
    @Override
    public void run() {
        LOG.info("Starting notification task for subscription UUID " + target.getUuid());

 /*       JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(EventSinkInterface.class);
        factory.setAddress("http://localhost:8060"); // TODO <- subscriber address

        // needed SOAP handlers
        SubscriptionReferenceAddingHandler handler = new
            SubscriptionReferenceAddingHandler(new ReferenceParametersType());
        factory.getHandlers().add(handler);

        WSAActionSettingHandler actionSettingHandler = new WSAActionSettingHandler("http://ACTION");
        factory.getHandlers().add(actionSettingHandler);

        factory.getOutInterceptors().add(new LoggingOutInterceptor()); //debug
        factory.getInInterceptors().add(new LoggingInInterceptor());          // debug

        EventSinkInterface endpoint = (EventSinkInterface)factory.create();
        endpoint.notification(message);*/
        try {
            Document document = message.getOwnerDocument();
            DOMImplementationLS domImplLS = (DOMImplementationLS)document
                    .getImplementation();
            LSSerializer serializer = domImplLS.createLSSerializer();
            String str = serializer.writeToString(message);
            LOG.info("SENDING TO " + target.getTargetURL() + ": \n" + str);
        } catch (Exception e) {
            e.printStackTrace();
        }





       /* String eventXML = DOMWriter.printNode(event, false);
        MessageFactory msgFactory = MessageFactory.newInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/' ");
        sb.append("xmlns:wse='").append(EventingConstants.EVENTING_2011_03_NAMESPACE).append("' ");
        sb.append("xmlns:wsa='").append("http://www.w3.org/2005/08/addressing").append("'>");
        sb.append("<env:Header>");
        sb.append("<wsa:Action>").append("http://action").append("</wsa:Action>");
        sb.append("<wsa:To>").append("http://notify-to").append("</wsa:To>");
        sb.append("</env:Header>");
        sb.append("<env:Body>");
        sb.append(eventXML);
        sb.append("</env:Body>");
        sb.append("</env:Envelope>");

        SOAPConnectionFactory.newInstance().createConnection().call(, "http://localhost:8060");*/
        LOG.info("Done.");
    }


}
