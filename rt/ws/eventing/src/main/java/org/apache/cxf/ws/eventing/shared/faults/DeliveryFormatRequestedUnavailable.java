package org.apache.cxf.ws.eventing.shared.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class DeliveryFormatRequestedUnavailable extends WSEventingException {

    public DeliveryFormatRequestedUnavailable() {
        super("The requested delivery format is not supported.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "DeliveryFormatRequestedUnavailable"));
    }

}
