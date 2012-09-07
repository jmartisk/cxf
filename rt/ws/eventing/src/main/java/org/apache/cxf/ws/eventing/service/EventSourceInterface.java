package org.apache.cxf.ws.eventing.service;

import org.apache.cxf.feature.Features;
import org.apache.cxf.ws.eventing.*;

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
@WebService(targetNamespace = "http://www.w3.org/2011/03/ws-evt")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Addressing(enabled=true, required=true)
@Features(features = {"org.apache.cxf.ws.eventing.EventingFeature"})    // TODO:remove? :(
public interface EventSourceInterface {


    @Action(
            input = EventingConstants.ACTION_SUBSCRIBE,
            output = EventingConstants.ACTION_SUBSCRIBE_RESPONSE/*,
              fault = @FaultAction(
                    className = IOException.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = "SubscribeResponse")
    SubscribeResponse subscribeOp(
            @WebParam(name = "Subscribe", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
            Subscribe body) throws IOException;

    @Action(
            input = EventingConstants.ACTION_RENEW,
            output = EventingConstants.ACTION_RENEW_RESPONSE/*,
              fault = @FaultAction(
                    className = SoapFault.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = "RenewResponse")
    RenewResponse renewOp(
           @WebParam(name = "Renew", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
           Renew body
    );

    @Action(
            input = EventingConstants.ACTION_GET_STATUS,
            output = EventingConstants.ACTION_GET_STATUS_RESPONSE/*,
              fault = @FaultAction(
                    className = SoapFault.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = "GetStatusResponse") GetStatusResponse getStatusOp(
            @WebParam(name = "GetStatus", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
            GetStatus body
    );

    @Action(
            input = EventingConstants.ACTION_UNSUBSCRIBE,
            output = EventingConstants.ACTION_UNSUBSCRIBE_RESPONSE/*,
            fault = @FaultAction(
                    className = SoapFault.class, value = EventingConstants.ACTION_FAULT
            )*/
    )
    public @WebResult(name = "UnsubscribeResponse") UnsubscribeResponse unsubscribeOp(
            @WebParam(name = "Unsubscribe", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
            Unsubscribe body
    );

}
