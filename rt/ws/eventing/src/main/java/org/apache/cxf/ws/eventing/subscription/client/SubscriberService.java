package org.apache.cxf.ws.eventing.subscription.client;

import org.apache.cxf.feature.Features;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author jmartisk
 * @since 9/3/12
 */
@WebService
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Features(features = "org.apache.cxf.ws.addressing.WSAddressingFeature")
public interface SubscriberService {

    public Object notification(@WebParam Object notification);

}
