package org.apache.cxf.ws.eventing.soaphandler;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.subscription.manager.SubscriptionManagerImpl;
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
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        // we are interested only in inbound messages here
        if((Boolean)context.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY))
            return true;
        try {
//            List referenceParams = (List)context.get(MessageContext.REFERENCE_PARAMETERS);              TODO: this is better

            // read headers
           Iterator headerElements = context.getMessage().getSOAPHeader().examineAllHeaderElements();
           Element o;
//           boolean found_uuid = false;
           LOG.finer("Examining header elements");
           while(headerElements.hasNext()) {
               o = (Element)headerElements.next();
               if(o.getNamespaceURI().equals(SubscriptionManagerImpl.SUBSCRIPTION_ID_NAMESPACE) &&
                  o.getLocalName().equals(SubscriptionManagerImpl.SUBSCRIPTION_ID)) {
                   LOG.fine("found UUID parameter in header, uuid="+o.getTextContent());
                   context.put("uuid", o.getTextContent());
//                   found_uuid = true;
               }
           }
//           if(!found_uuid)
//               throw new UnknownSubscription();
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
