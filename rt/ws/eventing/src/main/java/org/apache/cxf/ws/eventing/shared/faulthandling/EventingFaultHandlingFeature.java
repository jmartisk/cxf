package org.apache.cxf.ws.eventing.shared.faulthandling;

import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

@Deprecated
public class EventingFaultHandlingFeature extends AbstractFeature {

    private EventingFaultHandler faultHandler = new EventingFaultHandler();

    private Logger logger = Logger.getLogger(EventingFaultHandlingFeature.class.getName());

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        super.initializeProvider(provider, bus);
        provider.getOutFaultInterceptors().add(faultHandler);
        logger.fine("Initialized EventingFaultHandlingFeature...");
    }
}
