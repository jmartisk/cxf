package org.apache.cxf.ws.eventing.shared.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class  CannotProcessFilter extends WSEventingFault {

    public CannotProcessFilter() {
        super("Cannot filter as requested.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "CannotProcessFilter"));
    }

}
