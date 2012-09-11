package org.apache.cxf.ws.eventing;

/**
 * @author jmartisk
 * @since 8/27/12
 */
public class EventingConstants {

    public static final String ACTION_SUBSCRIBE = "http://www.w3.org/2011/03/ws-evt/Subscribe";
    public static final String ACTION_SUBSCRIBE_RESPONSE = "http://www.w3.org/2011/03/ws-evt/SubscribeResponse";

    public static final String ACTION_RENEW = "http://www.w3.org/2011/03/ws-evt/Renew";
    public static final String ACTION_RENEW_RESPONSE = "http://www.w3.org/2011/03/ws-evt/RenewResponse";

    public static final String ACTION_GET_STATUS = "http://www.w3.org/2011/03/ws-evt/GetStatus";
    public static final String ACTION_GET_STATUS_RESPONSE = "http://www.w3.org/2011/03/ws-evt/GetStatusResponse";

    public static final String ACTION_UNSUBSCRIBE = "http://www.w3.org/2011/03/ws-evt/Unsubscribe";
    public static final String ACTION_UNSUBSCRIBE_RESPONSE = "http://www.w3.org/2011/03/ws-evt/UnsubscribeResponse";

    public static final String ACTION_FAULT = "http://www.w3.org/2011/03/ws-evt/fault";

    public static final String EVENTING_2011_03_NAMESPACE = "http://www.w3.org/2011/03/ws-evt";

    public static final String RESPONSE_RENEW = "RenewResponse";


    public static final String RESPONSE_SUBSCRIBE = "SubscribeResponse";
    public static final String OPERATION_SUBSCRIBE = "Subscribe";
    public static final String OPERATION_RENEW = "Renew";
    public static final String RESPONSE_GET_STATUS = "GetStatusResponse";
    public static final String OPERATION_GET_STATUS = "GetStatus";
    public static final String RESPONSE_UNSUBSCRIBE = "UnsubscribeResponse";
    public static final String OPERATION_UNSUBSCRIBE = "Unsubscribe";
    public static final String ACTION_SUBSCRIPTION_END = "http://www.w3.org/2011/03/ws-evt/SubscriptionEnd";
}
