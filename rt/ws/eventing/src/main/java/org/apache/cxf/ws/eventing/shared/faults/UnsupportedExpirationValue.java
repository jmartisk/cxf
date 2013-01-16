package org.apache.cxf.ws.eventing.shared.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class UnsupportedExpirationValue extends WSEventingFault {

    public UnsupportedExpirationValue() {
        super("The expiration time requested is not within the min/max range.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "UnsupportedExpirationValue"));
    }

}
