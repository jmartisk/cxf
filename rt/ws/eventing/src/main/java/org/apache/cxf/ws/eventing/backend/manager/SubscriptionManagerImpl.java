package org.apache.cxf.ws.eventing.backend.manager;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.AttributedURIType;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionDatabase;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionDatabaseImpl;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.shared.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.shared.faults.NoDeliveryMechanismEstablished;
import org.apache.cxf.ws.eventing.shared.faults.UnknownSubscription;
import org.apache.cxf.ws.eventing.shared.utils.DurationAndDateUtil;
import org.apache.cxf.ws.eventing.shared.utils.FilteringUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * The core class representing WS-Eventing backend. It holds an instance of a database and
 * acts as a layer for communicating with it.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    public static final String SUBSCRIPTION_ID_NAMESPACE = "http://www.example.com";
    public static final String SUBSCRIPTION_ID = "SubscriptionID";


    protected SubscriptionDatabase database;

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
    public List<SubscriptionTicket> getTickets() {
        return Collections.unmodifiableList(database.getTickets());
    }

    protected SubscriptionDatabase getDatabase() {
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
        if (request != null) {
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
        if (request == null) {
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


    public XMLGregorianCalendar grantExpirationFor(XMLGregorianCalendar requested) {
        return requested;   // default
    }

    public XMLGregorianCalendar grantExpirationFor(javax.xml.datatype.Duration requested) {
        XMLGregorianCalendar granted;
        try {
            granted = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            throw new Error(e);
        }
        granted.add(requested); // default
        return granted;
    }

    public XMLGregorianCalendar grantExpiration() {
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

    @Override
    public void unsubscribeTicket(UUID uuid) {
        getDatabase().removeTicketByUUID(uuid);
    }

    @Override
    public SubscriptionTicket findTicket(UUID uuid) {
        return getDatabase().findById(uuid);
    }

    @Override
    public ExpirationType renew(UUID uuid, ExpirationType requestedExpiration) {
        LOG.info("Requested renew expiration: " + requestedExpiration.getValue());
        SubscriptionTicket ticket = getDatabase().findById(uuid);
        if(ticket == null)
            throw new UnknownSubscription();
        LOG.info("Current expiration: " + ticket.getExpires().toXMLFormat());
        ExpirationType response = new ExpirationType();
        XMLGregorianCalendar grantedExpires;
        if (DurationAndDateUtil.isDuration(requestedExpiration.getValue())) {
            // duration was requested
            javax.xml.datatype.Duration requestedDuration = DurationAndDateUtil.parseDuration(requestedExpiration.getValue());
            javax.xml.datatype.Duration grantedDuration = requestedDuration;
            LOG.info("Granted renewal duration: " + grantedDuration.toString());
            grantedExpires = getDatabase().findById(uuid).getExpires();       // NOW() or current Expires() ????
            grantedExpires.add(grantedDuration);
            response.setValue(grantedDuration.toString());
        } else {
            // end-date was requested
            grantedExpires = DurationAndDateUtil.parseXMLGregorianCalendar(requestedExpiration.getValue());
            LOG.info("Granted expiration: " + grantedExpires.toXMLFormat());
            response.setValue(grantedExpires.toXMLFormat());
        }
        getDatabase().findById(uuid).setExpires(grantedExpires);
        return response;
    }

}
