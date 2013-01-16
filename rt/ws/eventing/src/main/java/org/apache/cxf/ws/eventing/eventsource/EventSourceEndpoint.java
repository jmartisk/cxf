package org.apache.cxf.ws.eventing.eventsource;

import java.io.IOException;

import org.apache.cxf.feature.Features;
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

/**
 * The interface definition of an Event Source web service, according to the specification.
 * See http://www.w3.org/TR/ws-eventing/#Subscribe
 */
@WebService(targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Addressing(enabled = true, required = true)
//@Features(features = {"org.apache.cxf.ws.eventing.shared.faulthandling.EventingFaultHandlingFeature"})
@InInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingInInterceptor")
// TODO for debugging purposes. To be removed later
@OutInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingOutInterceptor")
// TODO for debugging purposes. To be removed later
public interface EventSourceEndpoint {


    /**
     * The Subscribe operation of the Event Source.
     * See http://www.w3.org/TR/ws-eventing/#Subscribe
     * @param body JAXB class Subscribe representing the body of the subscription request
     * @return JAXB class SubscribeResponse representing the response for the requester
     */
    @Action(
            input = EventingConstants.ACTION_SUBSCRIBE,
            output = EventingConstants.ACTION_SUBSCRIBE_RESPONSE
    )
    public
    @WebResult(name = EventingConstants.RESPONSE_SUBSCRIBE)
    SubscribeResponse subscribeOp(
            @WebParam(name = EventingConstants.OPERATION_SUBSCRIBE,
                    targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE, partName = "body")
            Subscribe body) throws IOException;


}
