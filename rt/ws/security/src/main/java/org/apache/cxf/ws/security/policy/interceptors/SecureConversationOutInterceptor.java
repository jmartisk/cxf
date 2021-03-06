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

package org.apache.cxf.ws.security.policy.interceptors;

import java.util.Collection;
import java.util.Map;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.cxf.ws.security.trust.STSUtils;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.policy.SPConstants;
import org.apache.wss4j.policy.model.SecureConversationToken;
import org.apache.wss4j.policy.model.Trust10;
import org.apache.wss4j.policy.model.Trust13;

class SecureConversationOutInterceptor extends AbstractPhaseInterceptor<SoapMessage> {
    public SecureConversationOutInterceptor() {
        super(Phase.PREPARE_SEND);
    }
    public void handleMessage(SoapMessage message) throws Fault {
        AssertionInfoMap aim = message.get(AssertionInfoMap.class);
        // extract Assertion information
        if (aim != null) {
            Collection<AssertionInfo> ais = 
                NegotiationUtils.getAllAssertionsByLocalname(aim, SPConstants.SECURE_CONVERSATION_TOKEN);
            if (ais.isEmpty()) {
                return;
            }
            if (isRequestor(message)) {
                SecureConversationToken itok = (SecureConversationToken)ais.iterator()
                    .next().getAssertion();
                
                SecurityToken tok = (SecurityToken)message.getContextualProperty(SecurityConstants.TOKEN);
                if (tok == null) {
                    String tokId = (String)message.getContextualProperty(SecurityConstants.TOKEN_ID);
                    if (tokId != null) {
                        tok = NegotiationUtils
                            .getTokenStore(message).getToken(tokId);
                    }
                }
                if (tok == null) {
                    tok = issueToken(message, aim, itok);
                } else {
                    tok = renewToken(message, aim, tok, itok);
                }
                if (tok != null) {
                    for (AssertionInfo ai : ais) {
                        ai.setAsserted(true);
                    }
                    message.getExchange().get(Endpoint.class).put(SecurityConstants.TOKEN, tok);
                    message.getExchange().get(Endpoint.class).put(SecurityConstants.TOKEN_ID, tok.getId());
                    message.getExchange().put(SecurityConstants.TOKEN_ID, tok.getId());
                    message.getExchange().put(SecurityConstants.TOKEN, tok);
                    NegotiationUtils.getTokenStore(message).add(tok);
                }
                NegotiationUtils.assertPolicy(aim, SPConstants.BOOTSTRAP_POLICY);
            } else {
                //server side should be checked on the way in
                for (AssertionInfo ai : ais) {
                    ai.setAsserted(true);
                }                    
            }
        }
    }
    
    
    private SecurityToken renewToken(SoapMessage message,
                            AssertionInfoMap aim, 
                            SecurityToken tok,
                            SecureConversationToken itok) {
        if (!tok.isExpired()) {
            return tok;
        }
        
        
        // Remove the old token
        message.getExchange().get(Endpoint.class).remove(SecurityConstants.TOKEN);
        message.getExchange().get(Endpoint.class).remove(SecurityConstants.TOKEN_ID);
        message.getExchange().remove(SecurityConstants.TOKEN_ID);
        message.getExchange().remove(SecurityConstants.TOKEN);
        NegotiationUtils.getTokenStore(message).remove(tok.getId());
        
        STSClient client = STSUtils.getClient(message, "sct");
        AddressingProperties maps =
            (AddressingProperties)message
                .get("javax.xml.ws.addressing.context.outbound");
        if (maps == null) {
            maps = (AddressingProperties)message
                .get("javax.xml.ws.addressing.context");
        } else if (maps.getAction().getValue().endsWith("Renew")) {
            return tok;
        }
        synchronized (client) {
            try {
                SecureConversationTokenInterceptorProvider.setupClient(client, message, aim, itok, true);

                String s = message
                    .getContextualProperty(Message.ENDPOINT_ADDRESS).toString();
                client.setLocation(s);
                
                Map<String, Object> ctx = client.getRequestContext();
                ctx.put(SecurityConstants.TOKEN_ID, tok.getId());
                if (maps != null) {
                    client.setAddressingNamespace(maps.getNamespaceURI());
                }
                return client.renewSecurityToken(tok);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new Fault(e);
            } finally {
                client.setTrust((Trust10)null);
                client.setTrust((Trust13)null);
                client.setTemplate(null);
                client.setLocation(null);
                client.setAddressingNamespace(null);
            }
        }            
    }
    private SecurityToken issueToken(SoapMessage message,
                                     AssertionInfoMap aim,
                                     SecureConversationToken itok) {
        STSClient client = STSUtils.getClient(message, "sct");
        AddressingProperties maps =
            (AddressingProperties)message
                .get("javax.xml.ws.addressing.context.outbound");
        if (maps == null) {
            maps = (AddressingProperties)message
                .get("javax.xml.ws.addressing.context");
        }
        synchronized (client) {
            try {
                String s = SecureConversationTokenInterceptorProvider
                    .setupClient(client, message, aim, itok, false);

                SecurityToken tok = null;
                if (maps != null) {
                    client.setAddressingNamespace(maps.getNamespaceURI());
                }
                tok = client.requestSecurityToken(s);
                String tokenType = tok.getTokenType();
                tok.setTokenType(tokenType);
                if (tokenType == null || "".equals(tokenType)) {
                    tok.setTokenType(WSConstants.WSC_SCT);
                }
                return tok;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new Fault(e);
            } finally {
                client.setTrust((Trust10)null);
                client.setTrust((Trust13)null);
                client.setTemplate(null);
                client.setLocation(null);
                client.setAddressingNamespace(null);
            }
        }
    }

}