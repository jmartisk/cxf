package org.apache.cxf.ws.eventing.shared.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class UnknownSubscription extends WSEventingFault {

    public UnknownSubscription() {
        super("The subscription is not known.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "UnknownSubscription"));
    }

}
