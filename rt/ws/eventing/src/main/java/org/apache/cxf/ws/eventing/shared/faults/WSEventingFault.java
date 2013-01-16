package org.apache.cxf.ws.eventing.shared.faults;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.addressing.impl.ActionOnFault;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.logging.Logger;

/**
 * The parent for all WS-Eventing-specific faults.
 */
@ActionOnFault("http://www.blablabla.com")
public abstract class WSEventingFault extends SoapFault {

    public WSEventingFault(String reason, Element detail, QName faultCode) {
        super(reason, faultCode);
        if (detail != null) {
            setDetail(detail);
        }
    }

}
