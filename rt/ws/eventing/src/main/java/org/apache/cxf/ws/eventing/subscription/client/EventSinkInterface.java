package org.apache.cxf.ws.eventing.subscription.client;

import org.apache.cxf.ws.eventing.SubscriptionEnd;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.soap.Addressing;

/**
 * @author jmartisk
 * @since 9/3/12
 */
@WebService
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Addressing(enabled = true, required = true)
public interface EventSinkInterface {

    @Oneway
    public void notification(@WebParam Object notification);

    @Oneway
    @Action(
            input = "http://www.w3.org/2011/03/ws-evt/SubscriptionEnd"
    )
    public void subscriptionEnd(@WebParam SubscriptionEnd subscriptionEnd);


}
