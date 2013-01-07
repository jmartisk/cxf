package org.apache.cxf.ws.eventing.manager;

import org.apache.cxf.feature.Features;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.interceptor.OutInterceptors;
import org.apache.cxf.ws.eventing.GetStatus;
import org.apache.cxf.ws.eventing.GetStatusResponse;
import org.apache.cxf.ws.eventing.Renew;
import org.apache.cxf.ws.eventing.RenewResponse;
import org.apache.cxf.ws.eventing.Unsubscribe;
import org.apache.cxf.ws.eventing.UnsubscribeResponse;
import org.apache.cxf.ws.eventing.shared.EventingConstants;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.soap.Addressing;

/**
 * @author jmartisk
 * @since 8/28/12
 */
@WebService(targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Addressing(enabled=true, required=true)
@Features(features = {"org.apache.cxf.ws.eventing.shared.faulthandling.EventingFaultHandlingFeature"})    // TODO:remove? :(
        // ^ the preferred solution is with @FaultAction-s, why doesn't it work? ^
@InInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingInInterceptor") // TODO for debugging purposes. To be removed later
@OutInterceptors(interceptors = "org.apache.cxf.interceptor.LoggingOutInterceptor") // TODO for debugging purposes. To be removed later
@HandlerChain(file = "/eventing-handler-chain.xml")
public interface SubscriptionManagerEndpoint {

    @Action(
            input = EventingConstants.ACTION_RENEW,
            output = EventingConstants.ACTION_RENEW_RESPONSE/*,
              fault = @FaultAction(
                    className = SoapFault.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = EventingConstants.RESPONSE_RENEW)
    RenewResponse renewOp(
            @WebParam(name = EventingConstants.OPERATION_RENEW, targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE, partName = "body")
            Renew body
    );

    @Action(
            input = EventingConstants.ACTION_GET_STATUS,
            output = EventingConstants.ACTION_GET_STATUS_RESPONSE/*,
              fault = @FaultAction(
                    className = SoapFault.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = EventingConstants.RESPONSE_GET_STATUS)
    GetStatusResponse getStatusOp(
            @WebParam(name = EventingConstants.OPERATION_GET_STATUS, targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE, partName = "body")
            GetStatus body
    );

    @Action(
            input = EventingConstants.ACTION_UNSUBSCRIBE,
            output = EventingConstants.ACTION_UNSUBSCRIBE_RESPONSE/*,
            fault = @FaultAction(
                    className = SoapFault.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = EventingConstants.RESPONSE_UNSUBSCRIBE)
    UnsubscribeResponse unsubscribeOp(
            @WebParam(name = EventingConstants.OPERATION_UNSUBSCRIBE, targetNamespace = EventingConstants.EVENTING_2011_03_NAMESPACE, partName = "body")
            Unsubscribe body
    );

}
