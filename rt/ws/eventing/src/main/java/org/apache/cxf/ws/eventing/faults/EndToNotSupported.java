package org.apache.cxf.ws.eventing.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class EndToNotSupported extends WSEventingException {

    public EndToNotSupported() {
        super("wse:EndTo semantics is not supported.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "EndToNotSupported"));
    }

}
