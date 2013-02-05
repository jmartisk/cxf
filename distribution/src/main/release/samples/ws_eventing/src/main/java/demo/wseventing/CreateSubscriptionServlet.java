package demo.wseventing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Node;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.eventing.AttributedURIType;
import org.apache.cxf.ws.eventing.DeliveryType;
import org.apache.cxf.ws.eventing.EndpointReferenceType;
import org.apache.cxf.ws.eventing.ExpirationType;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.NotifyTo;
import org.apache.cxf.ws.eventing.Subscribe;
import org.apache.cxf.ws.eventing.SubscribeResponse;
import org.apache.cxf.ws.eventing.eventsource.EventSourceEndpoint;

@WebServlet(urlPatterns = "/CreateSubscriptionServlet")
public class CreateSubscriptionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            resp.getWriter().append("<html><body>");


            JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.setServiceClass(EventSourceEndpoint.class);
            factory.setAddress("http://localhost:8080/ws_eventing/services/EventSource");
            EventSourceEndpoint requestorClient = (EventSourceEndpoint)factory.create();

            Subscribe sub = createSubscribeMessage(req.getParameter("targeturl"), req.getParameter("filter"));

            resp.getWriter().append("<h3>Subscription request</h3>");
            resp.getWriter().append(convertJAXBElementToStringAndEscapeHTML(sub));

            SubscribeResponse subscribeResponse = requestorClient.subscribeOp(sub);

            resp.getWriter().append("<h3>Response from Event Source</h3>");
            resp.getWriter().append(convertJAXBElementToStringAndEscapeHTML(subscribeResponse));

            resp.getWriter().append("<br/><a href=\"index.jsp\">Back to main page</a>");
            resp.getWriter().append("</body></html>");
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    public Subscribe createSubscribeMessage(String targetURL, String filter)
            throws DatatypeConfigurationException {
        Subscribe sub = new Subscribe();

        // expires
        XMLGregorianCalendar calendar;
        calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar("2014-06-26T12:00:00.000-01:00");
        sub.setExpires(new ExpirationType());
        sub.getExpires().setValue(calendar.toXMLFormat());

        // delivery
        EndpointReferenceType eventSink = new EndpointReferenceType();
        AttributedURIType eventSinkAddr = new AttributedURIType();
        eventSinkAddr.setValue(targetURL);
        eventSink.setAddress(eventSinkAddr);
        sub.setDelivery(new DeliveryType());
        sub.getDelivery().getContent().add(new NotifyTo());
        ((NotifyTo)sub.getDelivery().getContent().get(0)).setValue(eventSink);

        // filter
        if (filter != null && filter.length() > 0) {
            sub.setFilter(new FilterType());
            sub.getFilter().getContent().add(filter);
        }


        return sub;
    }

    public String convertJAXBElementToStringAndEscapeHTML(Object o) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Subscribe.class.getPackage().getName());
        Marshaller m = jc.createMarshaller();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.marshal(o, baos);
        String unescaped = baos.toString();
        return StringEscapeUtils.escapeHtml(unescaped);
    }

}
