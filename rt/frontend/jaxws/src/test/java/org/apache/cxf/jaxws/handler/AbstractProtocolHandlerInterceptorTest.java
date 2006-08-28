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
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.apache.cxf.message.AbstractWrappedMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.easymock.classextension.IMocksControl;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceControl;


public class AbstractProtocolHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private Binding binding;
    private HandlerChainInvoker invoker;
    private IIOPMessage message;
    private Exchange exchange;
    
    public void setUp() {
        control = createNiceControl();
        invoker = control.createMock(HandlerChainInvoker.class);
        message = control.createMock(IIOPMessage.class);
        exchange = control.createMock(Exchange.class);
        
    }
    
    public void tearDown() {
        control.verify();
    }

    public void testInterceptSuccess() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeProtocolHandlers(eq(true), isA(MessageContext.class))).andReturn(true);
        control.replay();
        IIOPHandlerInterceptor pi = new IIOPHandlerInterceptor(binding);
        assertEquals("unexpected phase", "user-protocol", pi.getPhase());
        pi.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(AbstractProtocolHandlerInterceptor.HANDLER_CHAIN_INVOKER)).andReturn(invoker);
        expect(invoker.invokeProtocolHandlers(eq(true), isA(MessageContext.class))).andReturn(false);
        control.replay();
        IIOPHandlerInterceptor pi = new IIOPHandlerInterceptor(binding);
        pi.handleMessage(message);  
    }

    class IIOPMessage extends AbstractWrappedMessage {
        public IIOPMessage(Message m) {
            super(m);
        }
    }
    
    interface IIOPMessageContext extends MessageContext {
        
    }
     
    interface IIOPHandler<T extends IIOPMessageContext> extends Handler<IIOPMessageContext> {
        
    }
    
    class IIOPHandlerInterceptor extends AbstractProtocolHandlerInterceptor<IIOPMessage> {
        IIOPHandlerInterceptor(Binding binding) {
            super(binding);
        }
    }
    
    
}
