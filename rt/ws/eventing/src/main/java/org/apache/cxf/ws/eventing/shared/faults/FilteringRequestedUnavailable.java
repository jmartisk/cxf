package org.apache.cxf.ws.eventing.shared.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class FilteringRequestedUnavailable extends WSEventingException {

    public FilteringRequestedUnavailable() {
        super("The requested filter dialect is not supported.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "FilteringRequestedUnavailable"));
    }

}
