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

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.AbstractPolicyInterceptorProvider;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.kerberos.KerberosClient;
import org.apache.cxf.ws.security.kerberos.KerberosUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.cxf.ws.security.tokenstore.TokenStoreFactory;
import org.apache.cxf.ws.security.wss4j.KerberosTokenInterceptor;
import org.apache.cxf.ws.security.wss4j.PolicyBasedWSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.PolicyStaxActionInInterceptor;
import org.apache.cxf.ws.security.wss4j.StaxSecurityContextInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.policyvalidators.KerberosTokenPolicyValidator;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.apache.wss4j.dom.message.token.BinarySecurity;
import org.apache.wss4j.dom.message.token.KerberosSecurity;
import org.apache.wss4j.policy.SP11Constants;
import org.apache.wss4j.policy.SP12Constants;
import org.apache.wss4j.policy.SPConstants;
import org.apache.wss4j.stax.securityEvent.KerberosTokenSecurityEvent;
import org.apache.wss4j.stax.securityEvent.WSSecurityEventConstants;
import org.apache.wss4j.stax.securityToken.KerberosServiceSecurityToken;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.stax.securityEvent.SecurityEvent;

/**
 * 
 */
public class KerberosTokenInterceptorProvider extends AbstractPolicyInterceptorProvider {

    private static final long serialVersionUID = 5922028830873137490L;

    public KerberosTokenInterceptorProvider() {
        super(Arrays.asList(SP11Constants.KERBEROS_TOKEN, SP12Constants.KERBEROS_TOKEN));
       
        this.getOutInterceptors().add(new KerberosTokenOutInterceptor());
        this.getOutFaultInterceptors().add(new KerberosTokenOutInterceptor());
        this.getInInterceptors().add(new KerberosTokenDOMInInterceptor());
        this.getInFaultInterceptors().add(new KerberosTokenDOMInInterceptor());
        
        this.getInInterceptors().add(new KerberosTokenStaxInInterceptor());
        this.getInFaultInterceptors().add(new KerberosTokenStaxInInterceptor());
        
        this.getOutInterceptors().add(new KerberosTokenInterceptor());
        this.getInInterceptors().add(new KerberosTokenInterceptor());
    }
    
    
    static final TokenStore getTokenStore(Message message) {
        EndpointInfo info = message.getExchange().get(Endpoint.class).getEndpointInfo();
        synchronized (info) {
            TokenStore tokenStore = 
                (TokenStore)message.getContextualProperty(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE);
            if (tokenStore == null) {
                tokenStore = (TokenStore)info.getProperty(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE);
            }
            if (tokenStore == null) {
                TokenStoreFactory tokenStoreFactory = TokenStoreFactory.newInstance();
                String cacheKey = SecurityConstants.TOKEN_STORE_CACHE_INSTANCE;
                if (info.getName() != null) {
                    cacheKey += "-" + info.getName().toString().hashCode();
                }
                tokenStore = tokenStoreFactory.newTokenStore(cacheKey, message);
                info.setProperty(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, tokenStore);
            }
            return tokenStore;
        }
    }

    static class KerberosTokenOutInterceptor extends AbstractPhaseInterceptor<Message> {
        public KerberosTokenOutInterceptor() {
            super(Phase.PREPARE_SEND);
        }
        public void handleMessage(Message message) throws Fault {
            AssertionInfoMap aim = message.get(AssertionInfoMap.class);
            // extract Assertion information
            if (aim != null) {
                Collection<AssertionInfo> ais = 
                    NegotiationUtils.getAllAssertionsByLocalname(aim, SPConstants.KERBEROS_TOKEN);
                if (ais.isEmpty()) {
                    return;
                }
                if (isRequestor(message)) {
                    SecurityToken tok = null;
                    try {
                        KerberosClient client = KerberosUtils.getClient(message, "kerberos");
                        synchronized (client) {
                            tok = client.requestSecurityToken();
                        }
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new Fault(e);
                    }
                    if (tok != null) {
                        for (AssertionInfo ai : ais) {
                            ai.setAsserted(true);
                        }
                        message.getExchange().get(Endpoint.class).put(SecurityConstants.TOKEN_ID, 
                                                                      tok.getId());
                        message.getExchange().put(SecurityConstants.TOKEN_ID, 
                                                  tok.getId());
                        getTokenStore(message).add(tok);
                    }
                } else {
                    //server side should be checked on the way in
                    for (AssertionInfo ai : ais) {
                        ai.setAsserted(true);
                    }                    
                }
                
                NegotiationUtils.assertPolicy(aim, "WssKerberosV5ApReqToken11");
                NegotiationUtils.assertPolicy(aim, "WssGssKerberosV5ApReqToken11");
            }
        }
        
    }
    
    static class KerberosTokenDOMInInterceptor extends AbstractPhaseInterceptor<Message> {
        public KerberosTokenDOMInInterceptor() {
            super(Phase.PRE_PROTOCOL);
            addAfter(WSS4JInInterceptor.class.getName());
            addAfter(PolicyBasedWSS4JInInterceptor.class.getName());
        }

        public void handleMessage(Message message) throws Fault {
            AssertionInfoMap aim = message.get(AssertionInfoMap.class);
            // extract Assertion information
            
            boolean enableStax = 
                MessageUtils.isTrue(message.getContextualProperty(SecurityConstants.ENABLE_STREAMING_SECURITY));
            if (aim != null && !enableStax) {
                Collection<AssertionInfo> ais = 
                    NegotiationUtils.getAllAssertionsByLocalname(aim, SPConstants.KERBEROS_TOKEN);
                if (ais.isEmpty()) {
                    return;
                }
                if (!isRequestor(message)) {
                    List<WSHandlerResult> results = 
                        CastUtils.cast((List<?>)message.get(WSHandlerConstants.RECV_RESULTS));
                    if (results != null && results.size() > 0) {
                        parseHandlerResults(results.get(0), message, aim);
                    }
                } else {
                    //client side should be checked on the way out
                    for (AssertionInfo ai : ais) {
                        ai.setAsserted(true);
                    }                    
                }
                
                NegotiationUtils.assertPolicy(aim, "WssKerberosV5ApReqToken11");
                NegotiationUtils.assertPolicy(aim, "WssGssKerberosV5ApReqToken11");
            }
        }
        
        private void parseHandlerResults(
            WSHandlerResult rResult,
            Message message,
            AssertionInfoMap aim
        ) {
            List<WSSecurityEngineResult> kerberosResults = findKerberosResults(rResult.getResults());
            for (WSSecurityEngineResult wser : kerberosResults) {
                KerberosSecurity kerberosToken = 
                    (KerberosSecurity)wser.get(WSSecurityEngineResult.TAG_BINARY_SECURITY_TOKEN);
                KerberosTokenPolicyValidator kerberosValidator = 
                    new KerberosTokenPolicyValidator(message);
                boolean valid = kerberosValidator.validatePolicy(aim, kerberosToken);
                if (valid) {
                    SecurityToken token = createSecurityToken(kerberosToken);
                    token.setSecret((byte[])wser.get(WSSecurityEngineResult.TAG_SECRET));
                    getTokenStore(message).add(token);
                    message.getExchange().put(SecurityConstants.TOKEN_ID, token.getId());
                    return;
                }
            }
        }
        
        private List<WSSecurityEngineResult> findKerberosResults(
            List<WSSecurityEngineResult> wsSecEngineResults
        ) {
            List<WSSecurityEngineResult> results = new ArrayList<WSSecurityEngineResult>();
            for (WSSecurityEngineResult wser : wsSecEngineResults) {
                Integer actInt = (Integer)wser.get(WSSecurityEngineResult.TAG_ACTION);
                if (actInt.intValue() == WSConstants.BST) {
                    BinarySecurity binarySecurity = 
                        (BinarySecurity)wser.get(WSSecurityEngineResult.TAG_BINARY_SECURITY_TOKEN);
                    if (binarySecurity instanceof KerberosSecurity) {
                        results.add(wser);
                    }
                }
            }
            return results;
        }
    }
    
    static class KerberosTokenStaxInInterceptor extends AbstractPhaseInterceptor<Message> {
        
        private static final Logger LOG = 
            LogUtils.getL7dLogger(KerberosTokenStaxInInterceptor.class);
        
        public KerberosTokenStaxInInterceptor() {
            super(Phase.PRE_PROTOCOL);
            addAfter(PolicyStaxActionInInterceptor.class.getName());
            getBefore().add(StaxSecurityContextInInterceptor.class.getName());
        }

        public void handleMessage(Message message) throws Fault {
            AssertionInfoMap aim = message.get(AssertionInfoMap.class);
            // extract Assertion information
            
            boolean enableStax = 
                MessageUtils.isTrue(message.getContextualProperty(SecurityConstants.ENABLE_STREAMING_SECURITY));
            if (aim != null && enableStax) {
                Collection<AssertionInfo> ais = 
                    NegotiationUtils.getAllAssertionsByLocalname(aim, SPConstants.KERBEROS_TOKEN);
                if (ais.isEmpty()) {
                    return;
                }
                if (!isRequestor(message)) {
                    SecurityEvent event = findKerberosEvent(message);
                    if (event != null) {
                        for (AssertionInfo ai : ais) {
                            ai.setAsserted(true);
                        }
                        KerberosServiceSecurityToken kerberosToken = 
                            ((KerberosTokenSecurityEvent)event).getSecurityToken();
                        if (kerberosToken != null) {
                            SecurityToken token = new SecurityToken(kerberosToken.getId());
                            token.setTokenType(kerberosToken.getKerberosTokenValueType());

                            byte[] secret = getSecretKeyFromToken(kerberosToken);
                            token.setSecret(secret);
                            getTokenStore(message).add(token);
                            message.getExchange().put(SecurityConstants.TOKEN_ID, token.getId());
                        }
                    }
                } else {
                    //client side should be checked on the way out
                    for (AssertionInfo ai : ais) {
                        ai.setAsserted(true);
                    }                    
                }
                
                NegotiationUtils.assertPolicy(aim, "WssKerberosV5ApReqToken11");
                NegotiationUtils.assertPolicy(aim, "WssGssKerberosV5ApReqToken11");
            }
        }
        
        private SecurityEvent findKerberosEvent(Message message) {
            @SuppressWarnings("unchecked")
            final List<SecurityEvent> incomingEventList = 
                (List<SecurityEvent>)message.get(SecurityEvent.class.getName() + ".in");
            if (incomingEventList != null) {
                for (SecurityEvent incomingEvent : incomingEventList) {
                    if (WSSecurityEventConstants.KerberosToken 
                        == incomingEvent.getSecurityEventType()) {
                        return incomingEvent;
                    }
                }
            }
            return null;
        }
        
        private byte[] getSecretKeyFromToken(KerberosServiceSecurityToken kerberosToken) {
            try {
                Map<String, Key> secretKeys = kerberosToken.getSecretKey();
                if (secretKeys != null) {
                    for (String key : kerberosToken.getSecretKey().keySet()) {
                        if (secretKeys.get(key) instanceof SecretKey) {
                            return ((SecretKey)secretKeys.get(key)).getEncoded();
                        }
                    }
                }
            } catch (XMLSecurityException e) {
                LOG.fine(e.getMessage());
            }
            return null;
        }
    }
    
    private static SecurityToken createSecurityToken(KerberosSecurity binarySecurityToken) {
        SecurityToken token = new SecurityToken(binarySecurityToken.getID());
        token.setToken(binarySecurityToken.getElement());
        token.setTokenType(binarySecurityToken.getValueType());
        return token;
    }
        
}
