package org.apache.cxf.ws.eventing.backend.database;

import java.util.List;
import java.util.UUID;

public interface SubscriptionDatabase {

    public void addTicket(SubscriptionTicket ticket);

    public List<SubscriptionTicket> getTickets();

    public SubscriptionTicket findById(UUID id);

    public void removeTicketByUUID(UUID id);

}
