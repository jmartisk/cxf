package org.apache.cxf.ws.eventing.shared.faults;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/28/12
 */
public abstract class WSEventingException extends SoapFault {

    private static final Logger LOG = LogUtils.getLogger(WSEventingException.class);


    public WSEventingException(String reason, Element detail, QName faultCode) {
        super(reason, faultCode);
        if (detail != null) {
            setDetail(detail);
        }
        /*if (faultCode != null) {
            setFaultCode(faultCode);
        }*/
    }

}
