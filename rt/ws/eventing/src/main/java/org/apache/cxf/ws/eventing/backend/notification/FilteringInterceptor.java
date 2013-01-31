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

import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.shared.utils.FilteringUtil;

public class FilteringInterceptor  extends AbstractPhaseInterceptor<SoapMessage> {

    private static final Logger LOG = LogUtils.getLogger(FilteringInterceptor.class);

    private FilterType filter;

    public FilteringInterceptor(FilterType filter) {
        super(Phase.POST_MARSHAL);
        this.filter = filter;
    }


    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        if (filter == null || filter.getContent() == null) {
            LOG.info("No filter for this subscription");
            return;
        }
        String xPath = (String)filter.getContent().get(0);
        javax.xml.soap.SOAPMessage msg = message.getContent(javax.xml.soap.SOAPMessage.class);
        try {
            Iterator i  = msg.getSOAPBody().getChildElements();
            while (i.hasNext()) {
                Element elm = (Element)i.next();
                System.out.println(elm.getLocalName());
                if (FilteringUtil.doesConformToFilter(elm, filter)) {
                    LOG.info("Event passed through filter: " + xPath);
                } else {
                    LOG.info("Filter " + xPath + " filtered out this notification.");
                    message.getInterceptorChain().abort();
                }
            }
        } catch (SOAPException e) {
            LOG.severe("SOAPException in FilteringInterceptor's handleMessage");
            message.getInterceptorChain().abort();
        }
    }
}
