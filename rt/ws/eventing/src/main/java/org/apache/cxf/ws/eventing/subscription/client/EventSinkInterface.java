package org.apache.cxf.ws.eventing.subscription.client;

import org.apache.cxf.feature.Features;
import org.apache.cxf.ws.eventing.SubscriptionEnd;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;

/**
 * @author jmartisk
 * @since 9/3/12
 */
@WebService
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Features(features = "org.apache.cxf.ws.addressing.WSAddressingFeature")
public interface EventSinkInterface {

    public void notification(@WebParam Object notification);

    @Action(
            output = "http://www.w3.org/2011/03/ws-evt/SubscriptionEnd"
    )
    public void subscriptionEnd(@WebParam SubscriptionEnd subscriptionEnd);


}
