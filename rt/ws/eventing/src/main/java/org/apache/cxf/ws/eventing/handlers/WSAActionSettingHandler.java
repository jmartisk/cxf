package org.apache.cxf.ws.eventing.handlers;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/**
 * @author jmartisk
 * @since 11/8/12
 */
public class WSAActionSettingHandler implements SOAPHandler<SOAPMessageContext> {

    public final String ACTION;

    public WSAActionSettingHandler(String action) {
        ACTION = action;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        //TODO
       return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }
}
