package org.apache.cxf.ws.eventing;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.ws.eventing.dummysubscriber.DummySubscriberService;
import org.apache.cxf.ws.eventing.subscription.client.SubscriberService;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Endpoint;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author jmartisk
 * @since 9/3/12
 */
public class NotificationGenerationTest {

    @Test
    public void testIt() throws IOException, SAXException, ParserConfigurationException {

        SubscriberService serverImpl = new DummySubscriberService();
        Endpoint.publish("local://subscriber", serverImpl);

        JaxWsProxyFactoryBean proxyFac = new JaxWsProxyFactoryBean();
        proxyFac.setAddress("local://subscriber");
        proxyFac.setServiceClass(SubscriberService.class);
        proxyFac.getInInterceptors().add(new LoggingInInterceptor());
        proxyFac.getOutInterceptors().add(new LoggingOutInterceptor());
        proxyFac.getClientFactoryBean().setTransportId(LocalTransportFactory.TRANSPORT_ID);
        SubscriberService client = (SubscriberService) proxyFac.create();

        Reader reader = new CharArrayReader("<tt><in>1</in></tt>".toCharArray());
        Document doc = DOMUtils.readXml(reader);

        Object o = client.notification(doc.getDocumentElement());
        Element elm = (Element) o;
        System.out.println(o.getClass());
        System.out.println(o.toString());
    }

}
