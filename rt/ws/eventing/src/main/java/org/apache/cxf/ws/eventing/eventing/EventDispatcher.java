package org.apache.cxf.ws.eventing.eventing;

import org.w3c.dom.Element;

import java.net.URI;

/**
 * @author jmartisk
 * @since 8/27/12
 */
public interface EventDispatcher {

    public void dispatch(URI eventSourceNS, Element event);

}
