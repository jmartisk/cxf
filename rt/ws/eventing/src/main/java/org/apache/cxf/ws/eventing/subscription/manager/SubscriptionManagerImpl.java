package org.apache.cxf.ws.eventing.subscription.manager;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.*;
import org.apache.cxf.ws.eventing.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.faults.NoDeliveryMechanismEstablished;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionDatabase;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionDatabaseImpl;
import org.apache.cxf.ws.eventing.subscription.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.utils.DurationAndDateUtil;
import org.apache.cxf.ws.eventing.utils.FilteringUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 9/20/12
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    public static final String SUBSCRIPTION_ID_NAMESPACE = "http://www.example.com";
    public static final String SUBSCRIPTION_ID = "SubscriptionID";

    private SubscriptionDatabase database;

    public SubscriptionManagerImpl() {
        database = new SubscriptionDatabaseImpl();
    }

    protected static final Logger LOG = LogUtils.getLogger(SubscriptionManagerImpl.class);


    @Override
    public SubscriptionTicketGrantingResponse subscribe(DeliveryType delivery, EndpointReferenceType endTo, ExpirationType expires, FilterType filter) {
        SubscriptionTicket ticket = new SubscriptionTicket();
        SubscriptionTicketGrantingResponse response = new SubscriptionTicketGrantingResponse();
        processDelivery(delivery, ticket, response);
        processEndTo(endTo, ticket, response);
        processExpiration(expires, ticket, response);
        processFilters(filter, ticket, response);
        grantSubscriptionManagerReference(ticket, response);
        getDatabase().addTicket(ticket);
        return response;
    }


    @Override
    public SubscriptionDatabase getDatabase() {
        return database;
    }

    protected void processFilters(FilterType request, SubscriptionTicket ticket, SubscriptionTicketGrantingResponse response) {
        if (request != null) {
            // test if the requested filtering dialect is supported
            if (FilteringUtil.isFilteringDialectSupported(request.getDialect())) {
                // TODO test if the requested filter is valid
                for (Object o : request.getContent()) {
                    LOG.fine("Found filter content: " + o.getClass() + " " + o.toString());
                }
                ticket.setFilter(request);
            } else {
                throw new FilteringRequestedUnavailable();
            }
        }
    }

    /**
     * process the stuff concerning expiration request (wse:Expires)
     */
    protected void processExpiration(ExpirationType request, SubscriptionTicket ticket, SubscriptionTicketGrantingResponse response) {
        XMLGregorianCalendar granted;
        if(request != null) {
            Object expirationTypeValue = DurationAndDateUtil.parseDurationOrTimestamp(request.getValue());
            if (expirationTypeValue instanceof javax.xml.datatype.Duration) {
                granted = grantExpirationFor((javax.xml.datatype.Duration) expirationTypeValue);
                ticket.setExpires(granted);
                response.setExpires(granted);
            } else if (expirationTypeValue instanceof XMLGregorianCalendar) {
                granted = grantExpirationFor((XMLGregorianCalendar) expirationTypeValue);
                ticket.setExpires(granted);
                response.setExpires(granted);
            } else {
                throw new Error("expirationTypeValue of unexpected type: " + expirationTypeValue.getClass());
            }
        } else {
            granted = grantExpiration();
            ticket.setExpires(grantExpiration());
            response.setExpires(granted);
        }
        LOG.info("Granted Expiration date: " + granted.toString());
    }

    protected void processEndTo(EndpointReferenceType request, SubscriptionTicket ticket, SubscriptionTicketGrantingResponse response) {
        if (request != null) {
            ticket.setEndTo(request);
        }
    }

    protected void processDelivery(DeliveryType request, SubscriptionTicket ticket, SubscriptionTicketGrantingResponse response) {
        if(request == null) {
            throw new NoDeliveryMechanismEstablished();
        }
        ticket.setDelivery(request);
    }

    protected void grantSubscriptionManagerReference(SubscriptionTicket ticket, SubscriptionTicketGrantingResponse response) {
        EndpointReferenceType subscriptionManagerReference = new EndpointReferenceType();
        subscriptionManagerReference.setAddress(getSubscriptionManagerAddress());
        // generate a ID for this subscription
        UUID uuid = UUID.randomUUID();
        JAXBElement idqn = new JAXBElement(new QName(SUBSCRIPTION_ID_NAMESPACE, SUBSCRIPTION_ID), String.class,
                uuid.toString());
        subscriptionManagerReference.setReferenceParameters(new ReferenceParametersType());
        subscriptionManagerReference.getReferenceParameters().getAny().add(idqn);
        ticket.setSubscriptionManagerReference(subscriptionManagerReference);
        ticket.setUuid(uuid);
        response.setSubscriptionManagerReference(subscriptionManagerReference);
        response.setUUID(uuid);
    }


    protected XMLGregorianCalendar grantExpirationFor(XMLGregorianCalendar requested) {
        return requested;   // default
    }

    protected XMLGregorianCalendar grantExpirationFor(javax.xml.datatype.Duration requested) {
        XMLGregorianCalendar granted;
        try {
            granted = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            throw new Error(e);
        }
        granted.add(requested); // default
        return granted;
    }

    protected XMLGregorianCalendar grantExpiration() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());  // default
        } catch (DatatypeConfigurationException ex) {
            throw new Error(ex);
        }
    }


    public AttributedURIType getSubscriptionManagerAddress() {
        AttributedURIType ret = new AttributedURIType();
        ret.setValue("http://localhost:8080/test1/TestSubscriptionManager");
        return ret;
    }

}
