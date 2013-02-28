/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.ws.eventing.backend.manager;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.AttributedURIType;
import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.FormatType;
import org.apache.cxf.ws.eventing.NotifyTo;
import org.apache.cxf.ws.eventing.ReferenceParametersType;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionDatabase;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionDatabaseImpl;
import org.apache.cxf.ws.eventing.backend.database.SubscriptionTicket;
import org.apache.cxf.ws.eventing.backend.notification.NotificatorService;
import org.apache.cxf.ws.eventing.backend.notification.SubscriptionEndStatus;
import org.apache.cxf.ws.eventing.shared.EventingConstants;
import org.apache.cxf.ws.eventing.shared.faults.CannotProcessFilter;
import org.apache.cxf.ws.eventing.shared.faults.DeliveryFormatRequestedUnavailable;
import org.apache.cxf.ws.eventing.shared.faults.FilteringRequestedUnavailable;
import org.apache.cxf.ws.eventing.shared.faults.NoDeliveryMechanismEstablished;
import org.apache.cxf.ws.eventing.shared.faults.UnknownSubscription;
import org.apache.cxf.ws.eventing.shared.utils.DurationAndDateUtil;
import org.apache.cxf.ws.eventing.shared.utils.EPRInspectionTool;
import org.apache.cxf.ws.eventing.shared.utils.FilteringUtil;

/**
 * The core class representing WS-Eventing backend. It holds an instance of a database and
 * acts as a layer for communicating with it.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    protected static final Logger LOG = LogUtils.getLogger(SubscriptionManagerImpl.class);

    protected final SubscriptionDatabase database;
    private final String subscriptionIdNamespace;
    private final String subscriptionIdElementName;
    private String url;
    private NotificatorService notificator;

    public SubscriptionManagerImpl(String url) {
        database = new SubscriptionDatabaseImpl();
        this.subscriptionIdNamespace = EventingConstants.SUBSCRIPTION_ID_DEFAULT_NAMESPACE;
        this.subscriptionIdElementName = EventingConstants.SUBSCRIPTION_ID_DEFAULT_ELEMENT_NAME;
        this.url = url;
    }

    public  SubscriptionManagerImpl(String url, String namespace, String elementName) {
        database = new SubscriptionDatabaseImpl();
        this.url = url;
        this.subscriptionIdNamespace = namespace;
        this.subscriptionIdElementName = elementName;
    }


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
            throw new DeliveryFormatRequestedUnavailable();
//            wrapped format not implemented yet
//            LOG.info("Wrapped delivery format was requested.");
//            ticket.setWrappedDelivery(true);
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
                String filter = (String)request.getContent().get(0);
                LOG.fine("Found filter content: " + filter);
                if (!FilteringUtil.isValidFilter(filter)) {
                    throw new CannotProcessFilter();
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
        // check if there is any usable EPR in the Delivery part
        try {
            NotifyTo notifyTo = (NotifyTo)request.getContent().get(0);
            if (!EPRInspectionTool.containsUsableEPR(notifyTo.getValue())) {
                throw new NoDeliveryMechanismEstablished();
            }
        } catch (NullPointerException npe) {
            throw new NoDeliveryMechanismEstablished();
        } catch (IndexOutOfBoundsException ioobe) {
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
        JAXBElement idqn = new JAXBElement(new QName(subscriptionIdNamespace, subscriptionIdElementName),
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
                    .isPT0S(requested)) { // The client requested a non-expiring subscription.
                    // We will give them 5 years.
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
            DatatypeFactory factory = DatatypeFactory.newInstance();
            XMLGregorianCalendar granted = factory.newXMLGregorianCalendar(new GregorianCalendar());
            granted.add(factory.newDurationYearMonth(true, 2, 0));
            return granted;
        } catch (DatatypeConfigurationException ex) {
            throw new Error(ex);
        }
    }


    public AttributedURIType getSubscriptionManagerAddress() {
        AttributedURIType ret = new AttributedURIType();
        ret.setValue(url);
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

    @Override
    public void subscriptionEnd(UUID subscriptionId, String reason, SubscriptionEndStatus status) {
        synchronized (database) {
            SubscriptionTicket ticket = database.findById(subscriptionId);
            if (ticket != null) {
                database.removeTicketByUUID(subscriptionId);
                if(ticket.getEndToURL() != null) {
                    notificator.subscriptionEnd(ticket, reason, status);
                }
            } else {
                LOG.severe("No such subscription: " + subscriptionId);
            }
        }
    }

    @Override
    public void registerNotificator(NotificatorService service) {
        this.notificator = service;
    }
}
