package org.apache.cxf.ws.eventing.shared.handlers;

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
public class SubscriptionReferenceAddingHandler implements SOAPHandler<SOAPMessageContext> {

    private final ReferenceParametersType params;

    public SubscriptionReferenceAddingHandler(ReferenceParametersType parametersType) {
        this.params = parametersType;
    }


    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        // we are interested only in outbound messages here
        if (!(Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            return true;
        }
        try {
            SOAPFactory factory = SOAPFactory.newInstance();
            for (Object o : params.getAny()) {
                SOAPElement elm = factory.createElement((Element)o);
                context.getMessage().getSOAPHeader()
                        .addChildElement(SOAPFactory.newInstance().createElement(elm));
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