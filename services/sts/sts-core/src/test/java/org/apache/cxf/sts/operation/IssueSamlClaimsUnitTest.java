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
package org.apache.cxf.sts.operation;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.sts.QNameConstants;
import org.apache.cxf.sts.STSConstants;
import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.claims.ClaimTypes;
import org.apache.cxf.sts.claims.ClaimsAttributeStatementProvider;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsManager;
import org.apache.cxf.sts.claims.ClaimsMapper;
import org.apache.cxf.sts.claims.RequestClaim;
import org.apache.cxf.sts.claims.RequestClaimCollection;
import org.apache.cxf.sts.common.CustomAttributeProvider;
import org.apache.cxf.sts.common.CustomClaimsHandler;
import org.apache.cxf.sts.common.PasswordCallbackHandler;
import org.apache.cxf.sts.request.KeyRequirements;
import org.apache.cxf.sts.request.TokenRequirements;
import org.apache.cxf.sts.service.EncryptionProperties;
import org.apache.cxf.sts.service.ServiceMBean;
import org.apache.cxf.sts.service.StaticService;
import org.apache.cxf.sts.token.provider.AttributeStatementProvider;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.TokenProvider;
import org.apache.cxf.sts.token.provider.TokenProviderParameters;
import org.apache.cxf.sts.token.provider.TokenProviderResponse;
import org.apache.cxf.sts.token.realm.Relationship;
import org.apache.cxf.sts.token.realm.SAMLRealm;
import org.apache.cxf.sts.token.validator.IssuerSAMLRealmCodec;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.sts.token.validator.TokenValidator;
import org.apache.cxf.ws.security.sts.provider.model.ClaimsType;
import org.apache.cxf.ws.security.sts.provider.model.OnBehalfOfType;
import org.apache.cxf.ws.security.sts.provider.model.RequestSecurityTokenResponseCollectionType;
import org.apache.cxf.ws.security.sts.provider.model.RequestSecurityTokenResponseType;
import org.apache.cxf.ws.security.sts.provider.model.RequestSecurityTokenType;
import org.apache.cxf.ws.security.sts.provider.model.RequestedSecurityTokenType;
import org.apache.ws.security.CustomTokenPrincipal;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.saml.ext.builder.SAML1Constants;
import org.apache.ws.security.saml.ext.builder.SAML2Constants;
import org.apache.ws.security.util.DOM2Writer;

/**
 * Some unit tests for the issue operation to issue SAML tokens with Claims information.
 */
public class IssueSamlClaimsUnitTest extends org.junit.Assert {
    
    public static final QName REQUESTED_SECURITY_TOKEN = 
        QNameConstants.WS_TRUST_FACTORY.createRequestedSecurityToken(null).getName();
    
    /**
     * Test to successfully issue a Saml 1.1 token.
     */
    @org.junit.Test
    public void testIssueSaml1Token() throws Exception {
        TokenIssueOperation issueOperation = new TokenIssueOperation();
        
        // Add Token Provider
        List<TokenProvider> providerList = new ArrayList<TokenProvider>();
        
        List<AttributeStatementProvider> customProviderList = 
            new ArrayList<AttributeStatementProvider>();
        customProviderList.add(new CustomAttributeProvider());
        SAMLTokenProvider samlTokenProvider = new SAMLTokenProvider();
        samlTokenProvider.setAttributeStatementProviders(customProviderList);
        providerList.add(samlTokenProvider);
        issueOperation.setTokenProviders(providerList);
        
        // Add Service
        ServiceMBean service = new StaticService();
        service.setEndpoints(Collections.singletonList("http://dummy-service.com/dummy"));
        issueOperation.setServices(Collections.singletonList(service));
        
        // Add STSProperties object
        STSPropertiesMBean stsProperties = new StaticSTSProperties();
        Crypto crypto = CryptoFactory.getInstance(getEncryptionProperties());
        stsProperties.setEncryptionCrypto(crypto);
        stsProperties.setSignatureCrypto(crypto);
        stsProperties.setEncryptionUsername("myservicekey");
        stsProperties.setSignatureUsername("mystskey");
        stsProperties.setCallbackHandler(new PasswordCallbackHandler());
        stsProperties.setIssuer("STS");
        issueOperation.setStsProperties(stsProperties);
        
        // Set the ClaimsManager
        ClaimsManager claimsManager = new ClaimsManager();
        ClaimsHandler claimsHandler = new CustomClaimsHandler();
        claimsManager.setClaimHandlers(Collections.singletonList(claimsHandler));
        issueOperation.setClaimsManager(claimsManager);
        
        // Mock up a request
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        JAXBElement<String> tokenType = 
            new JAXBElement<String>(
                QNameConstants.TOKEN_TYPE, String.class, WSConstants.WSS_SAML_TOKEN_TYPE
            );
        request.getAny().add(tokenType);
        Element secondaryParameters = createSecondaryParameters();
        request.getAny().add(secondaryParameters);
        request.getAny().add(createAppliesToElement("http://dummy-service.com/dummy"));
        
        // Mock up message context
        MessageImpl msg = new MessageImpl();
        WrappedMessageContext msgCtx = new WrappedMessageContext(msg);
        msgCtx.put(
            SecurityContext.class.getName(), 
            createSecurityContext(new CustomTokenPrincipal("alice"))
        );
        WebServiceContextImpl webServiceContext = new WebServiceContextImpl(msgCtx);
        
        // Issue a token
        RequestSecurityTokenResponseCollectionType response = 
            issueOperation.issue(request, webServiceContext);
        List<RequestSecurityTokenResponseType> securityTokenResponse = 
            response.getRequestSecurityTokenResponse();
        assertTrue(!securityTokenResponse.isEmpty());
        
        // Test the generated token.
        Element assertion = null;
        for (Object tokenObject : securityTokenResponse.get(0).getAny()) {
            if (tokenObject instanceof JAXBElement<?>
                && REQUESTED_SECURITY_TOKEN.equals(((JAXBElement<?>)tokenObject).getName())) {
                RequestedSecurityTokenType rstType = 
                    (RequestedSecurityTokenType)((JAXBElement<?>)tokenObject).getValue();
                assertion = (Element)rstType.getAny();
                break;
            }
        }
        
        assertNotNull(assertion);
        String tokenString = DOM2Writer.nodeToString(assertion);
        assertTrue(tokenString.contains("AttributeStatement"));
        assertTrue(tokenString.contains("alice"));
        assertTrue(tokenString.contains(SAML1Constants.CONF_BEARER));
        assertTrue(tokenString.contains(ClaimTypes.LASTNAME.toString()));
    }
    
    /**
     * Test to successfully issue a Saml 2 token.
     */
    @org.junit.Test
    public void testIssueSaml2Token() throws Exception {
        TokenIssueOperation issueOperation = new TokenIssueOperation();
        
        // Add Token Provider
        List<TokenProvider> providerList = new ArrayList<TokenProvider>();
        
        List<AttributeStatementProvider> customProviderList = 
            new ArrayList<AttributeStatementProvider>();
        customProviderList.add(new CustomAttributeProvider());
        SAMLTokenProvider samlTokenProvider = new SAMLTokenProvider();
        samlTokenProvider.setAttributeStatementProviders(customProviderList);
        providerList.add(samlTokenProvider);
        issueOperation.setTokenProviders(providerList);
        
        // Add Service
        ServiceMBean service = new StaticService();
        service.setEndpoints(Collections.singletonList("http://dummy-service.com/dummy"));
        issueOperation.setServices(Collections.singletonList(service));
        
        // Add STSProperties object
        STSPropertiesMBean stsProperties = new StaticSTSProperties();
        Crypto crypto = CryptoFactory.getInstance(getEncryptionProperties());
        stsProperties.setEncryptionCrypto(crypto);
        stsProperties.setSignatureCrypto(crypto);
        stsProperties.setEncryptionUsername("myservicekey");
        stsProperties.setSignatureUsername("mystskey");
        stsProperties.setCallbackHandler(new PasswordCallbackHandler());
        stsProperties.setIssuer("STS");
        issueOperation.setStsProperties(stsProperties);
        
        // Set the ClaimsManager
        ClaimsManager claimsManager = new ClaimsManager();
        ClaimsHandler claimsHandler = new CustomClaimsHandler();
        claimsManager.setClaimHandlers(Collections.singletonList(claimsHandler));
        issueOperation.setClaimsManager(claimsManager);
        
        // Mock up a request
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        JAXBElement<String> tokenType = 
            new JAXBElement<String>(
                QNameConstants.TOKEN_TYPE, String.class, WSConstants.WSS_SAML2_TOKEN_TYPE
            );
        request.getAny().add(tokenType);
        Element secondaryParameters = createSecondaryParameters();
        request.getAny().add(secondaryParameters);
        request.getAny().add(createAppliesToElement("http://dummy-service.com/dummy"));
        
        // Mock up message context
        MessageImpl msg = new MessageImpl();
        WrappedMessageContext msgCtx = new WrappedMessageContext(msg);
        msgCtx.put(
            SecurityContext.class.getName(), 
            createSecurityContext(new CustomTokenPrincipal("alice"))
        );
        WebServiceContextImpl webServiceContext = new WebServiceContextImpl(msgCtx);
        
        // Issue a token
        RequestSecurityTokenResponseCollectionType response = 
            issueOperation.issue(request, webServiceContext);
        List<RequestSecurityTokenResponseType> securityTokenResponse = 
            response.getRequestSecurityTokenResponse();
        assertTrue(!securityTokenResponse.isEmpty());
        
        // Test the generated token.
        Element assertion = null;
        for (Object tokenObject : securityTokenResponse.get(0).getAny()) {
            if (tokenObject instanceof JAXBElement<?>
                && REQUESTED_SECURITY_TOKEN.equals(((JAXBElement<?>)tokenObject).getName())) {
                RequestedSecurityTokenType rstType = 
                    (RequestedSecurityTokenType)((JAXBElement<?>)tokenObject).getValue();
                assertion = (Element)rstType.getAny();
                break;
            }
        }
        
        assertNotNull(assertion);
        String tokenString = DOM2Writer.nodeToString(assertion);
        assertTrue(tokenString.contains("AttributeStatement"));
        assertTrue(tokenString.contains("alice"));
        assertTrue(tokenString.contains(SAML2Constants.CONF_BEARER));
        assertTrue(tokenString.contains(ClaimTypes.LASTNAME.toString()));
    }
    
    /**
     * Test to successfully issue a Saml 1.1 token. The claims information is included as a 
     * JAXB Element under RequestSecurityToken, rather than as a child of SecondaryParameters.
     */
    @org.junit.Test
    public void testIssueJaxbSaml1Token() throws Exception {
        TokenIssueOperation issueOperation = new TokenIssueOperation();
        
        // Add Token Provider
        List<TokenProvider> providerList = new ArrayList<TokenProvider>();
        
        List<AttributeStatementProvider> customProviderList = 
            new ArrayList<AttributeStatementProvider>();
        customProviderList.add(new CustomAttributeProvider());
        SAMLTokenProvider samlTokenProvider = new SAMLTokenProvider();
        samlTokenProvider.setAttributeStatementProviders(customProviderList);
        providerList.add(samlTokenProvider);
        issueOperation.setTokenProviders(providerList);
        
        // Add Service
        ServiceMBean service = new StaticService();
        service.setEndpoints(Collections.singletonList("http://dummy-service.com/dummy"));
        issueOperation.setServices(Collections.singletonList(service));
        
        // Add STSProperties object
        STSPropertiesMBean stsProperties = new StaticSTSProperties();
        Crypto crypto = CryptoFactory.getInstance(getEncryptionProperties());
        stsProperties.setEncryptionCrypto(crypto);
        stsProperties.setSignatureCrypto(crypto);
        stsProperties.setEncryptionUsername("myservicekey");
        stsProperties.setSignatureUsername("mystskey");
        stsProperties.setCallbackHandler(new PasswordCallbackHandler());
        stsProperties.setIssuer("STS");
        issueOperation.setStsProperties(stsProperties);
        
        // Set the ClaimsManager
        ClaimsManager claimsManager = new ClaimsManager();
        ClaimsHandler claimsHandler = new CustomClaimsHandler();
        claimsManager.setClaimHandlers(Collections.singletonList(claimsHandler));
        issueOperation.setClaimsManager(claimsManager);
        
        // Mock up a request
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        JAXBElement<String> tokenType = 
            new JAXBElement<String>(
                QNameConstants.TOKEN_TYPE, String.class, WSConstants.WSS_SAML_TOKEN_TYPE
            );
        request.getAny().add(tokenType);
        
        // Add a ClaimsType
        ClaimsType claimsType = new ClaimsType();
        claimsType.setDialect(STSConstants.IDT_NS_05_05);
        Document doc = DOMUtils.createDocument();
        Element claimType = createClaimsType(doc);
        claimsType.getAny().add(claimType);
        
        JAXBElement<ClaimsType> claimsTypeJaxb = 
            new JAXBElement<ClaimsType>(
                QNameConstants.CLAIMS, ClaimsType.class, claimsType
            );
        request.getAny().add(claimsTypeJaxb);
        
        request.getAny().add(createAppliesToElement("http://dummy-service.com/dummy"));
        
        // Mock up message context
        MessageImpl msg = new MessageImpl();
        WrappedMessageContext msgCtx = new WrappedMessageContext(msg);
        msgCtx.put(
            SecurityContext.class.getName(), 
            createSecurityContext(new CustomTokenPrincipal("alice"))
        );
        WebServiceContextImpl webServiceContext = new WebServiceContextImpl(msgCtx);
        
        // Issue a token
        RequestSecurityTokenResponseCollectionType response = 
            issueOperation.issue(request, webServiceContext);
        List<RequestSecurityTokenResponseType> securityTokenResponse = 
            response.getRequestSecurityTokenResponse();
        assertTrue(!securityTokenResponse.isEmpty());
        
        // Test the generated token.
        Element assertion = null;
        for (Object tokenObject : securityTokenResponse.get(0).getAny()) {
            if (tokenObject instanceof JAXBElement<?>
                && REQUESTED_SECURITY_TOKEN.equals(((JAXBElement<?>)tokenObject).getName())) {
                RequestedSecurityTokenType rstType = 
                    (RequestedSecurityTokenType)((JAXBElement<?>)tokenObject).getValue();
                assertion = (Element)rstType.getAny();
                break;
            }
        }
        
        assertNotNull(assertion);
        String tokenString = DOM2Writer.nodeToString(assertion);
        assertTrue(tokenString.contains("AttributeStatement"));
        assertTrue(tokenString.contains("alice"));
        assertTrue(tokenString.contains(SAML1Constants.CONF_BEARER));
        assertTrue(tokenString.contains(ClaimTypes.LASTNAME.toString()));
    }
    
    /**
     * Test to successfully issue a SAML 2 token (realm "B") on-behalf-of a SAML 2 token
     * which was issued by realm "A".
     * The relationship type between realm A and B is: FederateClaims
     */
    @org.junit.Test
    public void testIssueSaml2TokenOnBehalfOfSaml2DifferentRealmFederateClaims() throws Exception {
        TokenIssueOperation issueOperation = new TokenIssueOperation();
        
        Map<String, SAMLRealm> realms = createSamlRealms();
        
        // Add Token Provider
        List<TokenProvider> providerList = new ArrayList<TokenProvider>();
        SAMLTokenProvider samlTokenProvider = new SAMLTokenProvider();
        samlTokenProvider.setRealmMap(realms);
        List<AttributeStatementProvider> customProviderList = 
            new ArrayList<AttributeStatementProvider>();
        customProviderList.add(new ClaimsAttributeStatementProvider());
        samlTokenProvider.setAttributeStatementProviders(customProviderList);
        providerList.add(samlTokenProvider);
        issueOperation.setTokenProviders(providerList);
        
        // Add Token Validator
        List<TokenValidator> validatorList = new ArrayList<TokenValidator>();
        SAMLTokenValidator samlTokenValidator = new SAMLTokenValidator();
        samlTokenValidator.setSamlRealmCodec(new IssuerSAMLRealmCodec());
        validatorList.add(samlTokenValidator);
        issueOperation.setTokenValidators(validatorList);

        // Add Service
        ServiceMBean service = new StaticService();
        service.setEndpoints(Collections.singletonList("http://dummy-service.com/dummy"));
        issueOperation.setServices(Collections.singletonList(service));
        
        // Add Relationship list
        List<Relationship> relationshipList = new ArrayList<Relationship>();
        Relationship rs = createRelationship();
        relationshipList.add(rs);
        
        // Add STSProperties object
        Crypto crypto = CryptoFactory.getInstance(getEncryptionProperties());
        STSPropertiesMBean stsProperties = createSTSPropertiesMBean(crypto);
        stsProperties.setRealmParser(new CustomRealmParser());
        stsProperties.setIdentityMapper(new CustomIdentityMapper());
        stsProperties.setRelationships(relationshipList);
        issueOperation.setStsProperties(stsProperties);
        
        // Set the ClaimsManager
        ClaimsManager claimsManager = new ClaimsManager();
        ClaimsHandler claimsHandler = new CustomClaimsHandler();
        claimsManager.setClaimHandlers(Collections.singletonList(claimsHandler));
        issueOperation.setClaimsManager(claimsManager);
        
        // Mock up a request
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        JAXBElement<String> tokenType = 
            new JAXBElement<String>(
                QNameConstants.TOKEN_TYPE, String.class, WSConstants.WSS_SAML2_TOKEN_TYPE
            );
        request.getAny().add(tokenType);
        
        // Add a ClaimsType
        ClaimsType claimsType = new ClaimsType();
        claimsType.setDialect(STSConstants.IDT_NS_05_05);
        
        Document doc = DOMUtils.createDocument();
        Element claimType = createClaimsType(doc);
        claimsType.getAny().add(claimType);
        
        JAXBElement<ClaimsType> claimsTypeJaxb = 
            new JAXBElement<ClaimsType>(
                QNameConstants.CLAIMS, ClaimsType.class, claimsType
            );
        request.getAny().add(claimsTypeJaxb);
        
        //request.getAny().add(createAppliesToElement("http://dummy-service.com/dummy"));
        
        // create a SAML Token via the SAMLTokenProvider which contains claims
        CallbackHandler callbackHandler = new PasswordCallbackHandler();
        Element samlToken = 
            createSAMLAssertion(WSConstants.WSS_SAML2_TOKEN_TYPE, crypto, "mystskey",
                    callbackHandler, realms);
        Document docToken = samlToken.getOwnerDocument();
        samlToken = (Element)docToken.appendChild(samlToken);
        String samlString = DOM2Writer.nodeToString(samlToken);
        assertTrue(samlString.contains("AttributeStatement"));
        assertTrue(samlString.contains("alice"));
        assertTrue(samlString.contains("doe"));
        assertTrue(samlString.contains(SAML2Constants.CONF_BEARER));
        
        // add SAML token as On-Behalf-Of element
        OnBehalfOfType onbehalfof = new OnBehalfOfType();
        onbehalfof.setAny(samlToken);
        JAXBElement<OnBehalfOfType> onbehalfofType = 
            new JAXBElement<OnBehalfOfType>(
                    QNameConstants.ON_BEHALF_OF, OnBehalfOfType.class, onbehalfof
            );
        request.getAny().add(onbehalfofType);
        
        // Mock up message context
        MessageImpl msg = new MessageImpl();
        WrappedMessageContext msgCtx = new WrappedMessageContext(msg);
        msgCtx.put("url", "https");
        WebServiceContextImpl webServiceContext = new WebServiceContextImpl(msgCtx);
        
        // run the test
        RequestSecurityTokenResponseCollectionType response = 
            issueOperation.issue(request, webServiceContext);
        List<RequestSecurityTokenResponseType> securityTokenResponseList = 
            response.getRequestSecurityTokenResponse();

        assertTrue(!securityTokenResponseList.isEmpty());       
        RequestSecurityTokenResponseType securityTokenResponse = securityTokenResponseList.get(0);
        
        // Test the generated token.
        Element assertion = null;
        for (Object tokenObject : securityTokenResponse.getAny()) {
            if (tokenObject instanceof JAXBElement<?>
                && REQUESTED_SECURITY_TOKEN.equals(((JAXBElement<?>)tokenObject).getName())) {
                RequestedSecurityTokenType rstType = 
                    (RequestedSecurityTokenType)((JAXBElement<?>)tokenObject).getValue();
                assertion = (Element)rstType.getAny();
                break;
            }
        }
        
        assertNotNull(assertion);
        String tokenString = DOM2Writer.nodeToString(assertion);
        assertTrue(tokenString.contains("AttributeStatement"));
        assertTrue(tokenString.contains("alice"));  //subject unchanged
        assertTrue(tokenString.contains("DOE")); //transformed claim (to uppercase)
        assertTrue(tokenString.contains(SAML2Constants.CONF_BEARER));
    }
    
    /**
     * Test to successfully issue a SAML 2 token (realm "B") on-behalf-of a SAML 2 token
     * which was issued by realm "A".
     * The relationship type between realm A and B is: FederateIdentity
     * IdentityMapper is configured globally in STSPropertiesMBean
     */
    @org.junit.Test
    public void testIssueSaml2TokenOnBehalfOfSaml2DifferentRealmFederateIdentityGlobalConfig()
        throws Exception {
        runIssueSaml2TokenOnBehalfOfSaml2DifferentRealmFederateIdentity(true);
    }
    
    
    /**
     * Test to successfully issue a SAML 2 token (realm "B") on-behalf-of a SAML 2 token
     * which was issued by realm "A".
     * The relationship type between realm A and B is: FederateIdentity
     * IdentityMapper is configured in the Relationship
     */
    @org.junit.Test
    public void testIssueSaml2TokenOnBehalfOfSaml2DifferentRealmFederateIdentityRelationshipConfig()
        throws Exception {
        runIssueSaml2TokenOnBehalfOfSaml2DifferentRealmFederateIdentity(false);
    }

    private void runIssueSaml2TokenOnBehalfOfSaml2DifferentRealmFederateIdentity(
            boolean useGlobalIdentityMapper) throws WSSecurityException {
        TokenIssueOperation issueOperation = new TokenIssueOperation();
        
        Map<String, SAMLRealm> realms = createSamlRealms();
        
        // Add Token Provider
        List<TokenProvider> providerList = new ArrayList<TokenProvider>();
        SAMLTokenProvider samlTokenProvider = new SAMLTokenProvider();
        samlTokenProvider.setRealmMap(realms);
        List<AttributeStatementProvider> customProviderList = 
            new ArrayList<AttributeStatementProvider>();
        customProviderList.add(new ClaimsAttributeStatementProvider());
        samlTokenProvider.setAttributeStatementProviders(customProviderList);
        providerList.add(samlTokenProvider);
        issueOperation.setTokenProviders(providerList);
        
        // Add Token Validator
        List<TokenValidator> validatorList = new ArrayList<TokenValidator>();
        SAMLTokenValidator samlTokenValidator = new SAMLTokenValidator();
        samlTokenValidator.setSamlRealmCodec(new IssuerSAMLRealmCodec());
        validatorList.add(samlTokenValidator);
        issueOperation.setTokenValidators(validatorList);

        // Add Service
        ServiceMBean service = new StaticService();
        service.setEndpoints(Collections.singletonList("http://dummy-service.com/dummy"));
        issueOperation.setServices(Collections.singletonList(service));
        
        // Add Relationship list
        List<Relationship> relationshipList = new ArrayList<Relationship>();
        Relationship rs = createRelationship();
        rs.setType(Relationship.FED_TYPE_IDENTITY);
        rs.setIdentityMapper(new CustomIdentityMapper());
        relationshipList.add(rs);
        
        // Add STSProperties object
        Crypto crypto = CryptoFactory.getInstance(getEncryptionProperties());
        STSPropertiesMBean stsProperties = createSTSPropertiesMBean(crypto);
        stsProperties.setRealmParser(new CustomRealmParser());
        
        if (useGlobalIdentityMapper) {
            stsProperties.setIdentityMapper(new CustomIdentityMapper());
        } else { 
            stsProperties.setRelationships(relationshipList);
        }  

        issueOperation.setStsProperties(stsProperties);
        
        // Set the ClaimsManager
        ClaimsManager claimsManager = new ClaimsManager();
        claimsManager.setClaimHandlers(Collections.singletonList((ClaimsHandler)new CustomClaimsHandler()));
        issueOperation.setClaimsManager(claimsManager);
        
        // Mock up a request
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        JAXBElement<String> tokenType = 
            new JAXBElement<String>(
                QNameConstants.TOKEN_TYPE, String.class, WSConstants.WSS_SAML2_TOKEN_TYPE
            );
        request.getAny().add(tokenType);
        
        // Add a ClaimsType
        ClaimsType claimsType = new ClaimsType();
        claimsType.setDialect(STSConstants.IDT_NS_05_05);
        
        Document doc = DOMUtils.createDocument();
        Element claimType = createClaimsType(doc);
        claimsType.getAny().add(claimType);
        
        JAXBElement<ClaimsType> claimsTypeJaxb = 
            new JAXBElement<ClaimsType>(
                QNameConstants.CLAIMS, ClaimsType.class, claimsType
            );
        request.getAny().add(claimsTypeJaxb);
        
        //request.getAny().add(createAppliesToElement("http://dummy-service.com/dummy"));
        
        // create a SAML Token via the SAMLTokenProvider which contains claims
        CallbackHandler callbackHandler = new PasswordCallbackHandler();
        Element samlToken = 
            createSAMLAssertion(WSConstants.WSS_SAML2_TOKEN_TYPE, crypto, "mystskey",
                    callbackHandler, realms);
        Document docToken = samlToken.getOwnerDocument();
        samlToken = (Element)docToken.appendChild(samlToken);
        String samlString = DOM2Writer.nodeToString(samlToken);
        assertTrue(samlString.contains("AttributeStatement"));
        assertTrue(samlString.contains("alice"));
        assertTrue(samlString.contains("doe"));
        assertTrue(samlString.contains(SAML2Constants.CONF_BEARER));
        
        // add SAML token as On-Behalf-Of element
        OnBehalfOfType onbehalfof = new OnBehalfOfType();
        onbehalfof.setAny(samlToken);
        JAXBElement<OnBehalfOfType> onbehalfofType = 
            new JAXBElement<OnBehalfOfType>(
                    QNameConstants.ON_BEHALF_OF, OnBehalfOfType.class, onbehalfof
            );
        request.getAny().add(onbehalfofType);
        
        // Mock up message context
        MessageImpl msg = new MessageImpl();
        WrappedMessageContext msgCtx = new WrappedMessageContext(msg);
        msgCtx.put("url", "https");
        WebServiceContextImpl webServiceContext = new WebServiceContextImpl(msgCtx);
        
        // run the test
        RequestSecurityTokenResponseCollectionType response = 
            issueOperation.issue(request, webServiceContext);
        List<RequestSecurityTokenResponseType> securityTokenResponseList = 
            response.getRequestSecurityTokenResponse();

        assertTrue(!securityTokenResponseList.isEmpty());       
        RequestSecurityTokenResponseType securityTokenResponse = securityTokenResponseList.get(0);
        
        // Test the generated token.
        Element assertion = null;
        for (Object tokenObject : securityTokenResponse.getAny()) {
            if (tokenObject instanceof JAXBElement<?>
                && REQUESTED_SECURITY_TOKEN.equals(((JAXBElement<?>)tokenObject).getName())) {
                RequestedSecurityTokenType rstType = 
                    (RequestedSecurityTokenType)((JAXBElement<?>)tokenObject).getValue();
                assertion = (Element)rstType.getAny();
                break;
            }
        }
        
        assertNotNull(assertion);
        String tokenString = DOM2Writer.nodeToString(assertion);
        assertTrue(tokenString.contains("AttributeStatement"));
        assertTrue(tokenString.contains("ALICE"));  //subject changed (to uppercase)
        assertTrue(tokenString.contains("doe"));  //claim unchanged but requested
        assertTrue(tokenString.contains(SAML2Constants.CONF_BEARER));
    }
    

    private Relationship createRelationship() {
        Relationship rs = new Relationship();
        ClaimsMapper claimsMapper = new CustomClaimsMapper();
        rs.setClaimsMapper(claimsMapper);
        rs.setSourceRealm("A");
        rs.setTargetRealm("B");
        rs.setType(Relationship.FED_TYPE_CLAIMS);
        return rs;
    }
    
    
    /*
     * Create STSPropertiesMBean object
     */
    private STSPropertiesMBean createSTSPropertiesMBean(Crypto crypto) throws WSSecurityException {
        STSPropertiesMBean stsProperties = new StaticSTSProperties();
        stsProperties.setEncryptionCrypto(crypto);
        stsProperties.setSignatureCrypto(crypto);
        stsProperties.setEncryptionUsername("myservicekey");
        stsProperties.setSignatureUsername("mystskey");
        stsProperties.setCallbackHandler(new PasswordCallbackHandler());
        stsProperties.setIssuer("STS");
        return stsProperties;
    }
    
    
    /*
     * Create a security context object
     */
    private SecurityContext createSecurityContext(final Principal p) {
        return new SecurityContext() {
            public Principal getUserPrincipal() {
                return p;
            }
            public boolean isUserInRole(String role) {
                return false;
            }
        };
    }
    
    /*
     * Mock up an AppliesTo element using the supplied address
     */
    private Element createAppliesToElement(String addressUrl) {
        Document doc = DOMUtils.createDocument();
        Element appliesTo = doc.createElementNS(STSConstants.WSP_NS, "wsp:AppliesTo");
        appliesTo.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsp", STSConstants.WSP_NS);
        Element endpointRef = doc.createElementNS(STSConstants.WSA_NS_05, "wsa:EndpointReference");
        endpointRef.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsa", STSConstants.WSA_NS_05);
        Element address = doc.createElementNS(STSConstants.WSA_NS_05, "wsa:Address");
        address.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsa", STSConstants.WSA_NS_05);
        address.setTextContent(addressUrl);
        endpointRef.appendChild(address);
        appliesTo.appendChild(endpointRef);
        return appliesTo;
    }
    
    private Properties getEncryptionProperties() {
        Properties properties = new Properties();
        properties.put(
            "org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin"
        );
        properties.put("org.apache.ws.security.crypto.merlin.keystore.password", "stsspass");
        properties.put("org.apache.ws.security.crypto.merlin.keystore.file", "stsstore.jks");
        
        return properties;
    }
    
    /*
     * Mock up a SecondaryParameters DOM Element containing some claims
     */
    private Element createSecondaryParameters() {
        Document doc = DOMUtils.createDocument();
        Element secondary = doc.createElementNS(STSConstants.WST_NS_05_12, "SecondaryParameters");
        secondary.setAttributeNS(WSConstants.XMLNS_NS, "xmlns", STSConstants.WST_NS_05_12);
        
        Element claims = doc.createElementNS(STSConstants.WST_NS_05_12, "Claims");
        claims.setAttributeNS(null, "Dialect", STSConstants.IDT_NS_05_05);
        
        Element claimType = createClaimsType(doc);
        
        claims.appendChild(claimType);
        secondary.appendChild(claims);

        return secondary;
    }
    
    private Element createClaimsType(Document doc) {
        Element claimType = doc.createElementNS(STSConstants.IDT_NS_05_05, "ClaimType");
        claimType.setAttributeNS(
            null, "Uri", ClaimTypes.LASTNAME.toString()
        );
        claimType.setAttributeNS(WSConstants.XMLNS_NS, "xmlns", STSConstants.IDT_NS_05_05);
        
        return claimType;
    }
    
    private Map<String, SAMLRealm> createSamlRealms() {
        // Create Realms
        Map<String, SAMLRealm> samlRealms = new HashMap<String, SAMLRealm>();
        SAMLRealm samlRealm = new SAMLRealm();
        samlRealm.setIssuer("A-Issuer");
        samlRealms.put("A", samlRealm);
        samlRealm = new SAMLRealm();
        samlRealm.setIssuer("B-Issuer");
        samlRealms.put("B", samlRealm);
        return samlRealms;
    }
    
    /*
     * Mock up an SAML assertion element
     */
    private Element createSAMLAssertion(
            String tokenType, Crypto crypto, String signatureUsername, CallbackHandler callbackHandler,
            Map<String, SAMLRealm> realms
    ) throws WSSecurityException {
        
        SAMLTokenProvider samlTokenProvider = new SAMLTokenProvider();
        samlTokenProvider.setRealmMap(realms);
        List<AttributeStatementProvider> customProviderList = 
            new ArrayList<AttributeStatementProvider>();
        customProviderList.add(new ClaimsAttributeStatementProvider());
        samlTokenProvider.setAttributeStatementProviders(customProviderList);
        
        TokenProviderParameters providerParameters = 
            createProviderParameters(
                    tokenType, STSConstants.BEARER_KEY_KEYTYPE, crypto, signatureUsername, callbackHandler
            );
        if (realms != null) {
            providerParameters.setRealm("A");
        }
        
        // Set the ClaimsManager
        ClaimsManager claimsManager = new ClaimsManager();
        ClaimsHandler claimsHandler = new CustomClaimsHandler();
        claimsManager.setClaimHandlers(Collections.singletonList(claimsHandler));
        providerParameters.setClaimsManager(claimsManager);
        
        RequestClaimCollection requestedClaims = new RequestClaimCollection();
        RequestClaim requestClaim = new RequestClaim();
        requestClaim.setClaimType(ClaimTypes.LASTNAME);
        requestClaim.setOptional(false);
        requestedClaims.add(requestClaim);
        providerParameters.setRequestedClaims(requestedClaims);
        
        TokenProviderResponse providerResponse = samlTokenProvider.createToken(providerParameters);
        assertTrue(providerResponse != null);
        assertTrue(providerResponse.getToken() != null && providerResponse.getTokenId() != null);

        return providerResponse.getToken();
    }
    
    private TokenProviderParameters createProviderParameters(
            String tokenType, String keyType, Crypto crypto, 
            String signatureUsername, CallbackHandler callbackHandler
    ) throws WSSecurityException {
        TokenProviderParameters parameters = new TokenProviderParameters();

        TokenRequirements tokenRequirements = new TokenRequirements();
        tokenRequirements.setTokenType(tokenType);
        parameters.setTokenRequirements(tokenRequirements);

        KeyRequirements keyRequirements = new KeyRequirements();
        keyRequirements.setKeyType(keyType);
        parameters.setKeyRequirements(keyRequirements);

        parameters.setPrincipal(new CustomTokenPrincipal("alice"));
        // Mock up message context
        MessageImpl msg = new MessageImpl();
        WrappedMessageContext msgCtx = new WrappedMessageContext(msg);
        WebServiceContextImpl webServiceContext = new WebServiceContextImpl(msgCtx);
        parameters.setWebServiceContext(webServiceContext);

        parameters.setAppliesToAddress("http://dummy-service.com/dummy");

        // Add STSProperties object
        StaticSTSProperties stsProperties = new StaticSTSProperties();
        stsProperties.setSignatureCrypto(crypto);
        stsProperties.setSignatureUsername(signatureUsername);
        stsProperties.setCallbackHandler(callbackHandler);
        stsProperties.setIssuer("STS");
        parameters.setStsProperties(stsProperties);

        parameters.setEncryptionProperties(new EncryptionProperties());

        return parameters;
    }
    
}
