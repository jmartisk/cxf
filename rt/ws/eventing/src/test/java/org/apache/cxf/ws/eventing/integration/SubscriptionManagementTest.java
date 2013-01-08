package org.apache.cxf.ws.eventing.integration;

import junit.framework.Assert;
import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.GetStatusResponse;
import org.apache.cxf.ws.eventing.Renew;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.SubscribeResponse;
import org.apache.cxf.ws.eventing.UnsubscribeResponse;
import org.apache.cxf.ws.eventing.base.SimpleEventingIntegrationTest;
import org.apache.cxf.ws.eventing.client.SubscriptionManagerClient;
import org.apache.cxf.ws.eventing.shared.utils.DurationAndDateUtil;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests to verify that a Subscription Manager can be properly used to manage existing subscriptions.
 * Typically, such test will create a subscription using the Event Source and then invoke
 * possible operations on the Subscription Manager to manage it.
 */
public class SubscriptionManagementTest extends SimpleEventingIntegrationTest {

    /**
     * Creates a subscription and then retrieves its status from the Subscription Manager.
     */
    @Test
    public void getStatus() throws Exception {
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);
        DeliveryType delivery = new DeliveryType();
        subscribe.setDelivery(delivery);
        SubscribeResponse resp = eventSourceClient.subscribeOp(subscribe);

        SubscriptionManagerClient client = createSubscriptionManagerClient(resp.getSubscriptionManager().getReferenceParameters());
        GetStatusResponse response = client.getStatus();
        System.out.println("EXPIRES: " + response.getGrantedExpires().getValue());
        Assert.assertTrue("GetStatus operation should return a XMLGregorianCalendar",
                DurationAndDateUtil.isXMLGregorianCalendar(response.getGrantedExpires().getValue()));
    }

    /**
     * Tries to create a subscription, then cancel it, then obtain its status.
     * The last mentioned operation should fail.
     */
    @Test
    public void unsubscribeAndThenGetStatus() throws Exception {
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT0S")));
        subscribe.setExpires(exp);
        DeliveryType delivery = new DeliveryType();
        subscribe.setDelivery(delivery);
        SubscribeResponse subscribeResponse = eventSourceClient.subscribeOp(subscribe);

        SubscriptionManagerClient client = createSubscriptionManagerClient(subscribeResponse.getSubscriptionManager().getReferenceParameters());
        UnsubscribeResponse unsubscribeResponse = client.unsubscribe();
        Assert.assertNotNull(unsubscribeResponse);

        try {
            GetStatusResponse getStatusResponse = client.getStatus();
        } catch(javax.xml.ws.soap.SOAPFaultException ex) {
            // ok
            return;
        }
        Assert.fail("The subscription manager should have refused to send status of a cancelled subscription");
    }

    @Test
    public void renewWithDuration() throws IOException {
        Subscribe subscribe = new Subscribe();
        ExpirationType exp = new ExpirationType();
        exp.setValue(DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT5M0S")));  // 5 minutes
        subscribe.setExpires(exp);
        DeliveryType delivery = new DeliveryType();
        subscribe.setDelivery(delivery);
        SubscribeResponse resp = eventSourceClient.subscribeOp(subscribe);

        SubscriptionManagerClient client = createSubscriptionManagerClient(resp.getSubscriptionManager().getReferenceParameters());
        GetStatusResponse response = client.getStatus();
        String expirationBefore = response.getGrantedExpires().getValue();
        System.out.println("EXPIRES before renew: " + expirationBefore);
        Assert.assertTrue(expirationBefore.length() > 0);

        Renew renewRequest = new Renew();
        ExpirationType renewExp = new ExpirationType();
        renewExp.setValue(DurationAndDateUtil.convertToXMLString(DurationAndDateUtil.parseDurationOrTimestamp("PT10M0S")));  // 10 minutes
        renewRequest.setExpires(renewExp);
        client.renew(renewRequest);
        response = client.getStatus();
        String expirationAfter = response.getGrantedExpires().getValue();
        System.out.println("EXPIRES after renew: " + expirationAfter);

        Assert.assertFalse("Renew request should change the expiration time at least a bit", expirationAfter.equals(expirationBefore));
    }



}
