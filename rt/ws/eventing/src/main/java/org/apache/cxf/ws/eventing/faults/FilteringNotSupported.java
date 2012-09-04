package org.apache.cxf.ws.eventing.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class FilteringNotSupported extends WSEventingException {

    public FilteringNotSupported() {
        super("Filtering is not supported.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "FilteringNotSupported"));
    }

}
