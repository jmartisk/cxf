package org.apache.cxf.ws.eventing.shared.faulthandling;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

/**
 * @author jmartisk
 * @since 8/14/12
 */
@Deprecated
public class EventingFaultHandlingFeature extends AbstractFeature {

    private EventingFaultHandler faultHandler = new EventingFaultHandler();

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        super.initializeProvider(provider, bus);
        provider.getOutFaultInterceptors().add(faultHandler);
        System.out.println("Initialized EventingFaultHandlingFeature...");
    }
}
