package org.apache.cxf.ws.eventing.service;

import org.apache.cxf.ws.eventing.*;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;

/**
 * @author jmartisk
 * @since 8/28/12
 */
@WebService(targetNamespace = "http://www.w3.org/2011/03/ws-evt")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface EventSourceInterface {


    @Action(
            input = EventingConstants.ACTION_SUBSCRIBE,
            output = EventingConstants.ACTION_SUBSCRIBE_RESPONSE
    )
    public @WebResult(name = "SubscribeResponse") SubscribeResponse subscribeOp(
            @WebParam(name = "Subscribe", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
            Subscribe body);

    @Action(
            input = EventingConstants.ACTION_RENEW,
            output = EventingConstants.ACTION_RENEW_RESPONSE
    )
    public @WebResult(name = "RenewResponse") RenewResponse renewOp(
           @WebParam(name = "Renew", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
           Renew body
    );

    @Action(
            input = EventingConstants.ACTION_GET_STATUS,
            output = EventingConstants.ACTION_GET_STATUS_RESPONSE
    )
    public @WebResult(name = "GetStatusResponse") GetStatusResponse getStatusOp(
            @WebParam(name = "GetStatus", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
            GetStatus body
    );

    @Action(
            input = EventingConstants.ACTION_UNSUBSCRIBE,
            output = EventingConstants.ACTION_UNSUBSCRIBE_RESPONSE
    )
    public @WebResult(name = "UnsubscribeResponse") UnsubscribeResponse unsubscribeOp(
            @WebParam(name = "Unsubscribe", targetNamespace = "http://www.w3.org/2011/03/ws-evt", partName = "body")
            Unsubscribe body
    );

}
