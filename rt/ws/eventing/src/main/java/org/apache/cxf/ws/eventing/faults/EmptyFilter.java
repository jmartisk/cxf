package org.apache.cxf.ws.eventing.faults;

import javax.xml.namespace.QName;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public class EmptyFilter extends WSEventingException {

    public EmptyFilter() {
        super("The wse:Filter would result in zero notifications.",
                null,
                new QName("http://www.w3.org/2011/03/ws-evt", "EmptyFilter"));
    }

}
