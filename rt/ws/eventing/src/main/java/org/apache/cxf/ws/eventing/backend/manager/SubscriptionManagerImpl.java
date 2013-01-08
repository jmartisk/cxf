package org.apache.cxf.ws.eventing.backend.manager;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.FormatType;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.AttributedURIType;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionDatabase;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionDatabaseImpl;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.shared.EventingConstants;
import org.apache.cxf.ws.eventing.shared.faults.DeliveryFormatRequestedUnavailable;
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
    public SubscriptionTicketGrantingResponse subscribe(DeliveryType delivery, EndpointReferenceType endTo,
                                                        ExpirationType expires, FilterType filter,
                                                        FormatType format) {
        SubscriptionTicket ticket = new SubscriptionTicket();
        SubscriptionTicketGrantingResponse response = new SubscriptionTicketGrantingResponse();
        processDelivery(delivery, ticket, response);
        processEndTo(endTo, ticket, response);
        processExpiration(expires, ticket, response);
        processFilters(filter, ticket, response);
        processFormat(format, ticket, response);
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

    protected void processFormat(FormatType format, SubscriptionTicket ticket,
                                 SubscriptionTicketGrantingResponse response) {
        if (format == null) {
            ticket.setWrappedDelivery(false);
            return;
        }
        if (format.getName().equals(EventingConstants.DELIVERY_FORMAT_WRAPPED)) {
            LOG.info("Wrapped delivery format was requested.");
            ticket.setWrappedDelivery(true);
        } else if (format.getName().equals(EventingConstants.DELIVERY_FORMAT_UNWRAPPED)) {
            LOG.info("Wrapped delivery format was NOT requested.");
            ticket.setWrappedDelivery(false);
        } else {
            LOG.info("Unknown delivery format: " + format.getName());
            throw new DeliveryFormatRequestedUnavailable();
        }
    }

    protected void processFilters(FilterType request, SubscriptionTicket ticket,
                                  SubscriptionTicketGrantingResponse response) {
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
    protected void processExpiration(ExpirationType request, SubscriptionTicket ticket,
                                     SubscriptionTicketGrantingResponse response) {
        XMLGregorianCalendar granted;
        if (request != null) {
            Object expirationTypeValue = DurationAndDateUtil.parseDurationOrTimestamp(request.getValue());
            if (expirationTypeValue instanceof javax.xml.datatype.Duration) {
                granted = grantExpirationFor((javax.xml.datatype.Duration)expirationTypeValue);
                ticket.setExpires(granted);
                response.setExpires(granted);
            } else if (expirationTypeValue instanceof XMLGregorianCalendar) {
                granted = grantExpirationFor((XMLGregorianCalendar)expirationTypeValue);
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

    protected void processEndTo(EndpointReferenceType request, SubscriptionTicket ticket,
                                SubscriptionTicketGrantingResponse response) {
        if (request != null) {
            ticket.setEndTo(request);
        }
    }

    protected void processDelivery(DeliveryType request, SubscriptionTicket ticket,
                                   SubscriptionTicketGrantingResponse response) {
        if (request == null) {
            throw new NoDeliveryMechanismEstablished();
        }
        ticket.setDelivery(request);
    }

    protected void grantSubscriptionManagerReference(SubscriptionTicket ticket,
                                                     SubscriptionTicketGrantingResponse response) {
        EndpointReferenceType subscriptionManagerReference = new EndpointReferenceType();
        subscriptionManagerReference.setAddress(getSubscriptionManagerAddress());
        // generate a ID for this subscription
        UUID uuid = UUID.randomUUID();
        JAXBElement idqn = new JAXBElement(new QName(SUBSCRIPTION_ID_NAMESPACE, SUBSCRIPTION_ID),
                String.class,
                uuid.toString());
        subscriptionManagerReference.setReferenceParameters(new ReferenceParametersType());
        subscriptionManagerReference.getReferenceParameters().getAny().add(idqn);
        ticket.setSubscriptionManagerReference(subscriptionManagerReference);
        ticket.setUuid(uuid);
        response.setSubscriptionManagerReference(subscriptionManagerReference);
        response.setUUID(uuid);
    }


    /**
     * Decide what expiration time to grant to the subscription, if
     * the client specified a calendar time in the request.
     */
    public XMLGregorianCalendar grantExpirationFor(XMLGregorianCalendar requested) {
        return requested;   // default
    }

    /**
     * Decide what expiration time to grant to the subscription, if
     * the client specified a duration in the request.
     */
    public XMLGregorianCalendar grantExpirationFor(javax.xml.datatype.Duration requested) {
        XMLGregorianCalendar granted;
        try {
            granted = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
            if (DurationAndDateUtil
                    .isPT0S(requested)) { // The client requested a non-expiring subscription. We will give them 5 years.
                granted.add(DatatypeFactory.newInstance().newDurationYearMonth(true, 5, 0));
            } else {
                granted.add(requested); // default
            }
            return granted;
        } catch (DatatypeConfigurationException e) {
            throw new Error(e);
        }
    }

    /**
     * Decide what expiration time to grant to the subscription, if
     * the client did not specify any particular wish for subscription length.
     */
    public XMLGregorianCalendar grantExpiration() {
        try { // by default, we grant an expiration time of 2 years
            GregorianCalendar granted = new GregorianCalendar();
            granted.add(GregorianCalendar.YEAR, 2);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(granted);
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
        if (ticket == null) {
            throw new UnknownSubscription();
        }
        LOG.info("Current expiration: " + ticket.getExpires().toXMLFormat());
        ExpirationType response = new ExpirationType();
        XMLGregorianCalendar grantedExpires;
        if (DurationAndDateUtil.isDuration(requestedExpiration.getValue())) {
            // duration was requested
            javax.xml.datatype.Duration requestedDuration = DurationAndDateUtil
                    .parseDuration(requestedExpiration.getValue());
            javax.xml.datatype.Duration grantedDuration = requestedDuration;
            LOG.info("Granted renewal duration: " + grantedDuration.toString());
            grantedExpires = getDatabase().findById(uuid)
                    .getExpires();       // NOW() or current Expires() ????
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
