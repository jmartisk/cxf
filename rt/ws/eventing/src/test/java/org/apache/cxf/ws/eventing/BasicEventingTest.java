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

package org.apache.cxf.ws.eventing;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Features;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.OutInterceptors;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.ws.eventing.service.AbstractEventSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jws.WebService;


/**
 * 
 */
public class BasicEventingTest {

    static Server server;

    @WebService(endpointInterface = "org.apache.cxf.ws.eventing.service.EventSourceEndpoint")
    @InInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingInInterceptor")
    @OutInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingOutInterceptor")
    @Features(features = "org.apache.cxf.ws.eventing.EventingFeature")
    public static class HelloServiceImpl extends AbstractEventSource {

    }

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceBean(new HelloServiceImpl());
        factory.setAddress("local://EventSource");
        factory.setTransportId(LocalTransportFactory.TRANSPORT_ID);
        server = factory.create();
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        server.destroy();
    }

    @Test
    public void testGet() {
//        JaxWsClientFactoryBean client = new JaxWsClientFactoryBean();

    }

}
