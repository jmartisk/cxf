package org.apache.cxf.ws.eventing.shared.faulthandling;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.ContextUtils;
import org.apache.cxf.ws.addressing.soap.DecoupledFaultHandler;
import org.apache.cxf.ws.eventing.shared.EventingConstants;

import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
@Deprecated
public class EventingFaultHandler extends AbstractSoapInterceptor {

    private static final Logger LOG = LogUtils.getLogger(EventingFaultHandler.class);

    public EventingFaultHandler() {
        super(Phase.POST_LOGICAL);
        addAfter(DecoupledFaultHandler.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        AddressingProperties maps =
                ContextUtils.retrieveMAPs(message, false, true, true);
        AttributedURIType action = new AttributedURIType();
        action.setValue(EventingConstants.ACTION_FAULT);
        maps.setAction(action);
        ContextUtils.storeMAPs(maps, message, true);

//        LOG.info("found content formats: " + Arrays.toString(message.getContentFormats().toArray()));
//        Exception faultCause =message.getContent(Exception.class);
    }
}
