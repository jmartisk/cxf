package org.apache.cxf.ws.eventing.shared.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class UnusableEPR extends WSEventingFault {

    public UnusableEPR() {
        super("An EPR in the Subscribe request message is unusable.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "UnusableEPR"));
    }

}
