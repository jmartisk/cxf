package org.apache.cxf.ws.eventing.backend.notification.emitters;

import org.apache.cxf.ws.eventing.backend.notification.NotificatorService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

/**
 * @author jmartisk
 * @since 11/8/12
 */
public abstract class EmitterServlet extends HttpServlet {

    public static final String PARAM_PAYLOAD = "payload";

    public abstract NotificatorService getService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Element elm;
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(req.getParameter(PARAM_PAYLOAD)));
            Document doc = db.parse(is);
            getService().dispatch(new java.net.URI("http://awesome-action-TODO"), doc.getDocumentElement());
        } catch (ParserConfigurationException e) {

        } catch (SAXException e) {

        } catch (URISyntaxException e) {

        }

    }

}
