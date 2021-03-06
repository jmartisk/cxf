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

package org.apache.cxf.jaxrs;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils.ClassLoaderHolder;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.ClassHelper;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorChain.State;
import org.apache.cxf.jaxrs.impl.AsyncResponseImpl;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.apache.cxf.jaxrs.provider.ServerProviderFactory;
import org.apache.cxf.jaxrs.utils.InjectionUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.invoker.AbstractInvoker;

public class JAXRSInvoker extends AbstractInvoker {
    private static final Logger LOG = LogUtils.getL7dLogger(JAXRSInvoker.class);
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JAXRSInvoker.class);
    private static final String SERVICE_LOADER_AS_CONTEXT = "org.apache.cxf.serviceloader-context";
    private static final String SERVICE_OBJECT_SCOPE = "org.apache.cxf.service.scope";
    private static final String REQUEST_SCOPE = "request";    
    private static final String LAST_SERVICE_OBJECT = "org.apache.cxf.service.object.last";
    private static final String REQUEST_WAS_SUSPENDED = "org.apache.cxf.service.request.suspended";
    private static final String PROXY_INVOCATION_ERROR_FRAGMENT 
        = "object is not an instance of declaring class"; 
    
    public JAXRSInvoker() {
    }

    public Object invoke(Exchange exchange, Object request) {
        MessageContentsList responseList = checkExchangeForResponse(exchange);
        if (responseList != null) {
            return responseList; 
        }
        AsyncResponse asyncResp = exchange.get(AsyncResponse.class);
        if (asyncResp != null) {
            AsyncResponseImpl asyncImpl = (AsyncResponseImpl)asyncResp;
            asyncImpl.prepareContinuation();
            try {
                asyncImpl.handleTimeout();
                return handleAsyncResponse(exchange, asyncImpl);
            } catch (Throwable t) {
                return handleAsyncFault(exchange, asyncImpl, t);
            }
        }
        
        ResourceProvider provider = getResourceProvider(exchange);
        Object rootInstance = null;
        try {
            rootInstance = getServiceObject(exchange);
            Object serviceObject = getActualServiceObject(exchange, rootInstance);
            
            return invoke(exchange, request, serviceObject);
        } catch (WebApplicationException ex) {
            responseList = checkExchangeForResponse(exchange);
            if (responseList != null) {
                return responseList; 
            }
            return handleFault(ex, exchange.getInMessage());
        } finally {
            boolean suspended = exchange.getInMessage().getInterceptorChain().getState() == State.SUSPENDED;
            if (!suspended) {
                if (exchange.isOneWay()) {
                    ServerProviderFactory.getInstance(exchange.getInMessage()).clearThreadLocalProxies();
                }
                if (!isServiceObjectRequestScope(exchange.getInMessage())) {
                    provider.releaseInstance(exchange.getInMessage(), rootInstance);
                } else {
                    persistRoots(exchange, rootInstance, provider);
                }
            } else {
                persistRoots(exchange, rootInstance, provider);
                exchange.put(REQUEST_WAS_SUSPENDED, true);
            }
        }
    }

    private Object handleAsyncResponse(Exchange exchange, AsyncResponseImpl ar) {
        Object asyncObj = ar.getResponseObject();
        if (asyncObj instanceof Throwable) {
            return handleAsyncFault(exchange, ar, (Throwable)asyncObj);
        } else {
            setResponseContentTypeIfNeeded(exchange.getInMessage(), asyncObj);
            return new MessageContentsList(asyncObj);
        }
    }
    
    private Object handleAsyncFault(Exchange exchange, AsyncResponseImpl ar, Throwable t) {
        try {
            return handleFault(new Fault(t), exchange.getInMessage(), null, null);
        } catch (Fault ex) {
            ar.setUnmappedThrowable(ex.getCause());
            return new MessageContentsList(Response.serverError().build());
        }
    }
    
    private void persistRoots(Exchange exchange, Object rootInstance, Object provider) {
        exchange.put(JAXRSUtils.ROOT_INSTANCE, rootInstance);
        exchange.put(JAXRSUtils.ROOT_PROVIDER, provider);
    }
    
    @SuppressWarnings("unchecked")
    public Object invoke(Exchange exchange, Object request, Object resourceObject) {

        final OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);
        final ClassResourceInfo cri = ori.getClassResourceInfo();
        final Message inMessage = exchange.getInMessage();
        final ServerProviderFactory providerFactory = ServerProviderFactory.getInstance(inMessage);

        boolean wasSuspended = exchange.remove(REQUEST_WAS_SUSPENDED) != null;
        
        if (!wasSuspended) {
            
            final boolean contextsAvailable = cri.contextsAvailable();
            final boolean paramsAvailable = cri.paramsAvailable();
            if (contextsAvailable || paramsAvailable) {
                Object realResourceObject = ClassHelper.getRealObject(resourceObject);
                if (paramsAvailable) {
                    JAXRSUtils.injectParameters(ori, realResourceObject, inMessage);
                }
                if (contextsAvailable) {
                    InjectionUtils.injectContexts(realResourceObject, cri, inMessage);
                }
            }
            if (cri.isRoot()) {
                ProviderInfo<Application> appProvider = providerFactory.getApplicationProvider();
                if (appProvider != null) {
                    InjectionUtils.injectContexts(appProvider.getProvider(),
                                                  appProvider,
                                                  inMessage);
                }
            }
        }
        

        Method methodToInvoke = InjectionUtils.checkProxy(
            cri.getMethodDispatcher().getMethod(ori), resourceObject);
        
        List<Object> params = null;
        if (request instanceof List) {
            params = CastUtils.cast((List<?>)request);
        } else if (request != null) {
            params = new MessageContentsList(request);
        }

        Object result = null;
        ClassLoaderHolder contextLoader = null;
        AsyncResponseImpl asyncResponse = null;
        try {
            if (setServiceLoaderAsContextLoader(inMessage)) {
                contextLoader = ClassLoaderUtils
                    .setThreadContextClassloader(resourceObject.getClass().getClassLoader());
            }
            if (!ori.isSubResourceLocator()) {
                asyncResponse = (AsyncResponseImpl)inMessage.get(AsyncResponse.class);
            }
            result = invoke(exchange, resourceObject, methodToInvoke, params);
            if (asyncResponse != null && !asyncResponse.suspendContinuationIfNeeded()) {
                result = handleAsyncResponse(exchange, asyncResponse);
            }
        } catch (Fault ex) {
            Object faultResponse;
            if (asyncResponse != null && !asyncResponse.suspendContinuationIfNeeded()) {
                faultResponse = handleAsyncFault(exchange, asyncResponse, ex.getCause());    
            } else {
                faultResponse = handleFault(ex, inMessage, cri, methodToInvoke);
            }
            return faultResponse;
        } finally {
            exchange.put(LAST_SERVICE_OBJECT, resourceObject);
            if (contextLoader != null) {
                contextLoader.reset();
            }
        }
        ClassResourceInfo subCri = null;
        if (ori.isSubResourceLocator()) {
            try {
                MultivaluedMap<String, String> values = getTemplateValues(inMessage);
                String subResourcePath = values.getFirst(URITemplate.FINAL_MATCH_GROUP);
                String httpMethod = (String)inMessage.get(Message.HTTP_REQUEST_METHOD);
                String contentType = (String)inMessage.get(Message.CONTENT_TYPE);
                if (contentType == null) {
                    contentType = "*/*";
                }
                List<MediaType> acceptContentType =
                    (List<MediaType>)exchange.get(Message.ACCEPT_CONTENT_TYPE);

                result = checkResultObject(result, subResourcePath);

                subCri = cri.getSubResource(methodToInvoke.getReturnType(),
                    ClassHelper.getRealClass(result), result);
                if (subCri == null) {
                    org.apache.cxf.common.i18n.Message errorM =
                        new org.apache.cxf.common.i18n.Message("NO_SUBRESOURCE_FOUND",
                                                               BUNDLE,
                                                               subResourcePath);
                    LOG.severe(errorM.toString());
                    throw new NotFoundException();
                }

                OperationResourceInfo subOri = JAXRSUtils.findTargetMethod(
                                                         Collections.singletonMap(subCri, values),
                                                         inMessage,
                                                         httpMethod,
                                                         values,
                                                         contentType,
                                                         acceptContentType);
                exchange.put(OperationResourceInfo.class, subOri);
                inMessage.put(URITemplate.TEMPLATE_PARAMETERS, values);
            
                if (JAXRSUtils.runContainerRequestFilters(providerFactory,
                                                      inMessage,
                                                      false, 
                                                      subOri.getNameBindings(),
                                                      true)) {
                    return new MessageContentsList(exchange.get(Response.class));
                }
                
                // work out request parameters for the sub-resource class. Here we
                // presume InputStream has not been consumed yet by the root resource class.
                List<Object> newParams = JAXRSUtils.processParameters(subOri, values, inMessage);
                inMessage.setContent(List.class, newParams);

                return this.invoke(exchange, newParams, result);
            } catch (IOException ex) {
                Response resp = JAXRSUtils.convertFaultToResponse(ex, inMessage);
                if (resp == null) {
                    resp = JAXRSUtils.convertFaultToResponse(ex, inMessage);
                }
                return new MessageContentsList(resp);
            } catch (WebApplicationException ex) {
                Response excResponse;
                if (JAXRSUtils.noResourceMethodForOptions(ex.getResponse(), 
                        (String)inMessage.get(Message.HTTP_REQUEST_METHOD))) {
                    excResponse = JAXRSUtils.createResponse(Collections.singletonList(subCri), 
                                                            null, null, 200, true);
                } else {
                    excResponse = JAXRSUtils.convertFaultToResponse(ex, inMessage);
                }
                return new MessageContentsList(excResponse);
            }
        }
        setResponseContentTypeIfNeeded(inMessage, result);
        return result;
    }
    
    private MessageContentsList checkExchangeForResponse(Exchange exchange) {
        Response r = exchange.get(Response.class);
        if (r != null) {
            JAXRSUtils.setMessageContentType(exchange.getInMessage(), r);
            return new MessageContentsList(r);
        } else {
            return null;
        }
    }
    
    private void setResponseContentTypeIfNeeded(Message inMessage, Object response) {
        if (response instanceof Response) {
            JAXRSUtils.setMessageContentType(inMessage, (Response)response);
        }
    }
    private Object handleFault(Throwable ex, Message inMessage) {
        return handleFault(new Fault(ex), inMessage, null, null);
    }
    private Object handleFault(Fault ex, Message inMessage, 
                               ClassResourceInfo cri, Method methodToInvoke) {
        String errorMessage = ex.getMessage();
        if (errorMessage != null && cri != null 
            && errorMessage.contains(PROXY_INVOCATION_ERROR_FRAGMENT)) {
            org.apache.cxf.common.i18n.Message errorM =
                new org.apache.cxf.common.i18n.Message("PROXY_INVOCATION_FAILURE",
                                                       BUNDLE,
                                                       methodToInvoke,
                                                       cri.getServiceClass().getName());
            LOG.severe(errorM.toString());
        }
        Response excResponse = JAXRSUtils.convertFaultToResponse(ex.getCause(), inMessage);
        if (excResponse == null) {
            ServerProviderFactory.getInstance(inMessage).clearThreadLocalProxies();
            ClassResourceInfo criRoot =
                (ClassResourceInfo)inMessage.getExchange().get(JAXRSUtils.ROOT_RESOURCE_CLASS);
            if (criRoot != null) {
                criRoot.clearThreadLocalProxies();
            }
            inMessage.getExchange().put(Message.PROPOGATE_EXCEPTION, 
                                        JAXRSUtils.propogateException(inMessage));
            throw ex;
        }
        return new MessageContentsList(excResponse);
    }

    @SuppressWarnings("unchecked")
    protected MultivaluedMap<String, String> getTemplateValues(Message msg) {
        MultivaluedMap<String, String> values = new MetadataMap<String, String>();
        MultivaluedMap<String, String> oldValues = 
            (MultivaluedMap<String, String>)msg.get(URITemplate.TEMPLATE_PARAMETERS);
        if (oldValues != null) {
            values.putAll(oldValues);
        }
        return values;
    }
    
    private boolean setServiceLoaderAsContextLoader(Message inMessage) {
        Object en = inMessage.getContextualProperty(SERVICE_LOADER_AS_CONTEXT);
        return Boolean.TRUE.equals(en) || "true".equals(en);
    }
    
    private boolean isServiceObjectRequestScope(Message inMessage) {
        Object scope = inMessage.getContextualProperty(SERVICE_OBJECT_SCOPE);
        return REQUEST_SCOPE.equals(scope);
    }
    
    private ResourceProvider getResourceProvider(Exchange exchange) {
        Object provider = exchange.remove(JAXRSUtils.ROOT_PROVIDER);
        if (provider == null) {
            OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);
            ClassResourceInfo cri = ori.getClassResourceInfo();
            return cri.getResourceProvider();
        } else {
            return (ResourceProvider)provider;
        }
    }
    
    public Object getServiceObject(Exchange exchange) {
        
        Object root = exchange.remove(JAXRSUtils.ROOT_INSTANCE);
        if (root != null) {
            return root;
        }
        
        OperationResourceInfo ori = exchange.get(OperationResourceInfo.class);
        ClassResourceInfo cri = ori.getClassResourceInfo();

        return cri.getResourceProvider().getInstance(exchange.getInMessage());
    }
    
    protected Object getActualServiceObject(Exchange exchange, Object rootInstance) {
        
        Object last = exchange.get(LAST_SERVICE_OBJECT);
        return last !=  null ? last : rootInstance;
    }
    
    
    
    private static Object checkResultObject(Object result, String subResourcePath) {
        

        //the result becomes the object that will handle the request
        if (result != null) {
            if (result instanceof MessageContentsList) {
                result = ((MessageContentsList)result).get(0);
            } else if (result instanceof List) {
                result = ((List<?>)result).get(0);
            } else if (result.getClass().isArray()) {
                result = ((Object[])result)[0];
            }
        }
        if (result == null) {
            org.apache.cxf.common.i18n.Message errorM =
                new org.apache.cxf.common.i18n.Message("NULL_SUBRESOURCE",
                                                       BUNDLE,
                                                       subResourcePath);
            LOG.info(errorM.toString());
            throw new NotFoundException();
        }

        return result;
    }

    
}
