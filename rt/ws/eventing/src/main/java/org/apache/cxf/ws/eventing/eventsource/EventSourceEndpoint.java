package org.apache.cxf.ws.eventing.eventsource;

import org.apache.cxf.feature.Features;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.OutInterceptors;
import org.apache.cxf.ws.eventing.shared.EventingConstants;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.SubscribeResponse;
import org.apache.cxf.ws.eventing.shared.faults.DeliveryFormatRequestedUnavailable;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.soap.Addressing;
import java.io.IOException;

/**
 * @author jmartisk
 * @since 8/28/12
 */
@WebService(targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Addressing(enabled = true, required = true)
@Features(features = {"org.apache.cxf.ws.eventing.shared.faulthandling.EventingFaultHandlingFeature"})
// TODO:remove? :(
// ^ the preferred solution is with @FaultAction-s, why doesn't it work? ^ // ONLY FOR SERVER
@InInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingInInterceptor")
// TODO for debugging purposes. To be removed later
@OutInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingOutInterceptor")
// TODO for debugging purposes. To be removed later
public interface EventSourceEndpoint {


    @Action(
            input = EventingConstants.ACTION_SUBSCRIBE,
            output = EventingConstants.ACTION_SUBSCRIBE_RESPONSE,
            fault = {
                    @FaultAction(className = DeliveryFormatRequestedUnavailable.class,
                            value = EventingConstants.ACTION_FAULT),
                    @FaultAction(className = javax.wsdl.Fault.class,
                            value = "javax.wsdl.Fault"),
                    @FaultAction(className = Exception.class,
                            value = "java.lang.Exception"),
                    @FaultAction(className = Fault.class,
                            value = "fault")
            }
    )
    public
    @WebResult(name = EventingConstants.RESPONSE_SUBSCRIBE)
    SubscribeResponse subscribeOp(
            @WebParam(name = EventingConstants.OPERATION_SUBSCRIBE,
                    targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE, partName = "body")
            Subscribe body) throws IOException, DeliveryFormatRequestedUnavailable;


}
