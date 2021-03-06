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

package org.apache.cxf.transport.http.netty.server;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.transport.HttpUriMapper;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class NettyHttpServerEngine implements ServerEngine {

    /**
     * This is the network port for which this engine is allocated.
     */
    private int port;

    /**
     * This is the network address for which this engine is allocated.
     */
    private String host;

    /**
     * This field holds the protocol for which this engine is
     * enabled, i.e. "http" or "https".
     */
    private String protocol = "http";

    private volatile Channel serverChannel;

    private NettyHttpServletPipelineFactory servletPipeline;

    private Map<String, NettyHttpContextHandler> handlerMap = new ConcurrentHashMap<String, NettyHttpContextHandler>();
    
    /**
     * This field holds the TLS ServerParameters that are programatically
     * configured. The tlsServerParamers (due to JAXB) holds the struct
     * placed by SpringConfig.
     */
    private TLSServerParameters tlsServerParameters;
    

    public NettyHttpServerEngine(
            String host,
            int port) {
        this.host = host;
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    
    /**
     * This method is used to programmatically set the TLSServerParameters.
     * This method may only be called by the factory.
     * @throws IOException 
     */
    public void setTlsServerParameters(TLSServerParameters params) {
        
        tlsServerParameters = params;
        
    }
    
    /**
     * This method returns the programmatically set TLSServerParameters, not
     * the TLSServerParametersType, which is the JAXB generated type used 
     * in SpringConfiguration.
     * @return
     */
    public TLSServerParameters getTlsServerParameters() {
        return tlsServerParameters;
    } 
    
   
    protected Channel startServer() {
        // TODO Configure the server.
        final ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(Executors
                        .newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        servletPipeline = new NettyHttpServletPipelineFactory(tlsServerParameters, handlerMap);
        bootstrap.setPipelineFactory(servletPipeline);
        InetSocketAddress address = null;
        if (host == null) {
            address = new InetSocketAddress(port);
        } else {
            address = new InetSocketAddress(host, port);
        }
        // Bind and start to accept incoming connections.
        return bootstrap.bind(address);
    }


    @Override
    public void addServant(URL url, NettyHttpHandler handler) {
        if (serverChannel == null) {
            serverChannel = startServer();
        }
        // need to set the handler name for looking up
        handler.setName(url.getPath());
        String contextName = HttpUriMapper.getContextName(url.getPath());
        // need to check if the NettyContext is there
        NettyHttpContextHandler contextHandler = handlerMap.get(contextName);
        if (contextHandler == null) {
            contextHandler = new NettyHttpContextHandler(contextName);
            handlerMap.put(contextName, contextHandler);
        }
        contextHandler.addNettyHttpHandler(handler);
    }

    @Override
    public void removeServant(URL url) {
        final String contextName = HttpUriMapper.getContextName(url.getPath());
        NettyHttpContextHandler contextHandler = handlerMap.get(contextName);
        if (contextHandler != null) {
            contextHandler.removeNettyHttpHandler(url.getPath());
            if (contextHandler.isEmpty()) {
                // remove the contextHandler from handlerMap
                handlerMap.remove(contextName);
            }
        }
    }

    @Override
    public NettyHttpHandler getServant(URL url) {
        final String contextName = HttpUriMapper.getContextName(url.getPath());
        NettyHttpContextHandler contextHandler = handlerMap.get(contextName);
        if (contextHandler != null) {
            return contextHandler.getNettyHttpHandler(url.getPath());
        } else {
            return null;
        }
    }

    public void shutdown() {
        // just unbind the channel
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (servletPipeline != null) {
            servletPipeline.shutdown();
        }
    }
}
