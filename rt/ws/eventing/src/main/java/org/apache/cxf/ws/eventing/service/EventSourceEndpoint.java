package org.apache.cxf.ws.eventing.service;

import org.apache.cxf.feature.Features;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.OutInterceptors;
import org.apache.cxf.ws.eventing.*;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.soap.Addressing;
import java.io.IOException;

/**
 * @author jmartisk
 * @since 8/28/12
 */
@WebService(targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Addressing(enabled=true, required=true)
@Features(features = {"org.apache.cxf.ws.eventing.faulthandling.EventingFaultHandlingFeature"})    // TODO:remove? :(
        // ^ the preferred solution is with @FaultAction-s, why doesn't it work? ^ // ONLY FOR SERVER
@InInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingInInterceptor") // TODO for debugging purposes. To be removed later
@OutInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingOutInterceptor") // TODO for debugging purposes. To be removed later
public interface EventSourceEndpoint {


    @Action(
            input = EventingConstants.ACTION_SUBSCRIBE,
            output = EventingConstants.ACTION_SUBSCRIBE_RESPONSE/*,
              fault = @FaultAction(
                    className = IOException.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = EventingConstants.RESPONSE_SUBSCRIBE)
    SubscribeResponse subscribeOp(
            @WebParam(name = EventingConstants.OPERATION_SUBSCRIBE, targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE, partName = "body")
            Subscribe body) throws IOException;


}
