package org.apache.cxf.ws.eventing.client;

import org.apache.cxf.ws.eventing.shared.EventingConstants;
import org.apache.cxf.ws.eventing.SubscriptionEnd;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.soap.Addressing;

/**
 *
 */
@WebService
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Addressing(enabled = true, required = true)
public interface EventSinkInterface {

    @Oneway                              // TODO: JAXBElement
    public void notification(@WebParam Object notification);

    @Oneway
    @Action(
            input = EventingConstants.ACTION_SUBSCRIPTION_END
    )
    public void subscriptionEnd(@WebParam SubscriptionEnd subscriptionEnd);


}
