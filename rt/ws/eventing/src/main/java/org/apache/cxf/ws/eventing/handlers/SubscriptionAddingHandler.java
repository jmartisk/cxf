package org.apache.cxf.ws.eventing.handlers;

import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/**
 * @author jmartisk
 * @since 11/8/12
 */
public class SubscriptionAddingHandler implements SOAPHandler<SOAPMessageContext> {

    private final ReferenceParametersType params;

    public SubscriptionAddingHandler(ReferenceParametersType parametersType) {
        this.params = parametersType;
    }


    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        try {
            SOAPFactory factory = SOAPFactory.newInstance();
            for(Object o : params.getAny()) {
                SOAPElement elm = factory.createElement((Element)o);
                context.getMessage().getSOAPHeader().addChildElement(SOAPFactory.newInstance().createElement(elm));
            }
        } catch (SOAPException e) {
            throw new RuntimeException(e);
        }
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