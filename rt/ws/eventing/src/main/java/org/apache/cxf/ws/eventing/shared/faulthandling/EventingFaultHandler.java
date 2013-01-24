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

package org.apache.cxf.ws.eventing.shared.faulthandling;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.soap.DecoupledFaultHandler;
import org.apache.cxf.ws.eventing.shared.EventingConstants;

@Deprecated
public class EventingFaultHandler extends AbstractSoapInterceptor {

    public EventingFaultHandler() {
        super(Phase.POST_LOGICAL);
        addAfter(DecoupledFaultHandler.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        AddressingProperties maps =
                ContextUtils.retrieveMAPs(message, false, true, true);
        AttributedURIType action = new AttributedURIType();
        action.setValue(EventingConstants.ACTION_FAULT);
        maps.setAction(action);
        ContextUtils.storeMAPs(maps, message, true);
    }
}
