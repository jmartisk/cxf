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

package org.apache.cxf.ws.eventing.shared.handlers;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.backend.manager.SubscriptionManagerImpl;

public class SubscriptionReferenceParsingHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOG = LogUtils.getLogger(SubscriptionReferenceParsingHandler.class);

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        // we are interested only in inbound messages here
        if ((Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            return true;
        }
        try {
            // read headers
            Iterator headerElements = context.getMessage().getSOAPHeader().examineAllHeaderElements();
            Element o;
            LOG.finer("Examining header elements");
            while (headerElements.hasNext()) {
                o = (Element)headerElements.next();
                if (o.getNamespaceURI().equals(SubscriptionManagerImpl.SUBSCRIPTION_ID_NAMESPACE)
                        && o.getLocalName().equals(SubscriptionManagerImpl.SUBSCRIPTION_ID)) {
                    LOG.fine("found UUID parameter in header, uuid=" + o.getTextContent());
                    context.put("uuid", o.getTextContent());
                }
            }
        } catch (SOAPException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }
}
