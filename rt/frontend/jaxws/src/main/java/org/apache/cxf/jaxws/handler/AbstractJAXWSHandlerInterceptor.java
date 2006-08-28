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

package org.apache.cxf.jaxws.handler;

import javax.xml.ws.Binding;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public abstract class AbstractJAXWSHandlerInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {

    public static final String HANDLER_CHAIN_INVOKER = "org.apache.cxf.jaxws.handlers.invoker";
    
    private Binding binding;
    
    protected AbstractJAXWSHandlerInterceptor(Binding b) {
        binding = b;
    }
    
    boolean isOneway(T message) {
        return true;
    }
    
    boolean isOutbound(T message) {
        return message == message.getExchange().getOutMessage();
    }
    
    boolean isRequestor(T message) {
        return true;
    }
    
    protected HandlerChainInvoker getInvoker(T message) {
        HandlerChainInvoker invoker = 
            (HandlerChainInvoker)message.getExchange().get(HANDLER_CHAIN_INVOKER);
        if (null == invoker) {
            invoker = new HandlerChainInvoker(binding.getHandlerChain());
            message.getExchange().put(HANDLER_CHAIN_INVOKER, invoker);
        }
        return invoker;
    }
    
    protected Binding getBinding() {
        return binding;
    }
}
