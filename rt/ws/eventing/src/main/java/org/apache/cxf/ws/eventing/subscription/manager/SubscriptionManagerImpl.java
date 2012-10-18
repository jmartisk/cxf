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

    private SubscriptionDatabase database;

    public SubscriptionManagerImpl() {
        database = new SubscriptionDatabaseImpl();
    }

    protected static final Logger LOG = LogUtils.getLogger(SubscriptionManagerImpl.class);


    @Override
    public SubscriptionTicket subscribe(DeliveryType delivery, EndpointReferenceType endTo, ExpirationType expires, FilterType filter) {
        SubscriptionTicket ticket = new SubscriptionTicket();
        processDelivery(delivery, ticket);
        processEndTo(endTo, ticket);
        processExpiration(expires, ticket);
        processFilters(filter, ticket);
        grantSubscriptionManagerReference(ticket);
        getDatabase().addTicket(ticket);
        return ticket;
    }


    @Override
    public SubscriptionDatabase getDatabase() {
        return database;
    }

    protected void processFilters(FilterType request, SubscriptionTicket ticket) {
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
    protected void processExpiration(ExpirationType request, SubscriptionTicket ticket) {
        ExpirationType grantedExpiration = new ExpirationType();
        if (request != null) {
            String expirationTypeString = request.getValue();
            Object expirationTypeValue = DurationAndDateUtil.parseDurationOrTimestamp(expirationTypeString);
            LOG.info("ExpirationType's class: " + expirationTypeValue.getClass());
            LOG.info("ExpirationType's toString: " + expirationTypeValue.toString());
            if (expirationTypeValue instanceof javax.xml.datatype.Duration) {
                grantedExpiration.setValue(grantExpirationFor((javax.xml.datatype.Duration) expirationTypeValue).toString());

                DatatypeFactory factory;
                try {
                    factory = DatatypeFactory.newInstance();
                } catch (DatatypeConfigurationException e) {
                    throw new RuntimeException(e);
                }                             // TODO cleanup this mess:)
                XMLGregorianCalendar effectiveExpiration = factory.newXMLGregorianCalendar(new GregorianCalendar());
                effectiveExpiration.add((javax.xml.datatype.Duration)expirationTypeValue);
                GregorianCalendar effective = effectiveExpiration.toGregorianCalendar();
                ticket.setEffectiveExpiresDate(effective);
            }
            else if (expirationTypeValue instanceof XMLGregorianCalendar) {
                grantedExpiration.setValue(grantExpirationFor((XMLGregorianCalendar) expirationTypeValue).toXMLFormat());
                ticket.setEffectiveExpiresDate(((XMLGregorianCalendar)expirationTypeValue).toGregorianCalendar());
            }
        } else { // no expirationTime request was made
            grantedExpiration.setValue(grantExpiration().toString());
        }
        ticket.setExpires(grantedExpiration);
    }

    protected void processEndTo(EndpointReferenceType request, SubscriptionTicket ticket) {
        if (request != null) {
            ticket.setEndTo(request);
        }
    }

    protected void processDelivery(DeliveryType request, SubscriptionTicket ticket) {
        if(request == null) {
            throw new NoDeliveryMechanismEstablished();
        }
        ticket.setDelivery(request);
    }

    protected void grantSubscriptionManagerReference(SubscriptionTicket ticket) {
        EndpointReferenceType subscriptionManagerReference = new EndpointReferenceType();
        subscriptionManagerReference.setAddress(getSubscriptionManagerAddress());
        // generate a ID for this subscription
        UUID uuid = UUID.randomUUID();
        JAXBElement idqn = new JAXBElement(new QName("http://www.example.com", "SubscriptionID"), String.class,
                uuid.toString());
        subscriptionManagerReference.setReferenceParameters(new ReferenceParametersType());
        subscriptionManagerReference.getReferenceParameters().getAny().add(idqn);
        ticket.setUUID(uuid);
    }


    protected XMLGregorianCalendar grantExpirationFor(XMLGregorianCalendar requested) {
        return requested;   // default
    }

    protected javax.xml.datatype.Duration grantExpirationFor(javax.xml.datatype.Duration requested) {
        return requested;   // default
    }

    protected javax.xml.datatype.Duration grantExpiration() {
        try {
            return DatatypeFactory.newInstance().newDuration("PT0S");  // default
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
