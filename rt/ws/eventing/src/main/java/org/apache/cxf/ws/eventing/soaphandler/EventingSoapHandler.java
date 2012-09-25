package org.apache.cxf.ws.eventing.soaphandler;

import org.apache.cxf.common.logging.LogUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 9/12/12
 */
public class EventingSoapHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOG = LogUtils.getLogger(EventingSoapHandler.class);

    // is outbound/inbound?
    /*
    Boolean outboundProperty = (Boolean)
         messageContext.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
     */

    @Override
    public Set<QName> getHeaders() {
        System.out.println("getheaders");
        return null;

    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        try {
           Iterator headerElements = context.getMessage().getSOAPHeader().examineAllHeaderElements();
           Element o;
           while(headerElements.hasNext()) {
               o = (Element)headerElements.next();
               System.out.println("class: " + o.getClass());
               System.out.println("namespace :" + o.getNamespaceURI());
               System.out.println("name :" + o.getTagName());
               System.out.println("text content :" + o.getTextContent());
           }
        } catch (SOAPException e) {
            throw new RuntimeException(e);
        }
        context.put("test", "WIN!!!!!!!!!!!!1");
        System.out.println("handlemessage: "+ context.toString());
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        System.out.println("handlefault: " + context.toString());
        return true;
    }

    @Override
    public void close(MessageContext context) {
        System.out.println("close message context: " + context.toString());
    }
}
