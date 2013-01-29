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

package org.apache.cxf.ws.eventing.backend.notification.emitters;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.apache.cxf.ws.eventing.backend.notification.NotificatorService;

/**
 * An example Emitter for WS-Eventing. Emitter classes are meant to pass events
 * to the NotificatorService, which then takes care of notifying subscribers.
 * To use this Emitter:
 * - extend it, implement getService appropriately to return a NotificatorService which
 *   takes care of notifications to subscribers
 * - use it as a usual servlet, eg. publish it on HTTP using a servlet container
 * - invoke it with these parameters:
 *   - 'payload' - XML string representing the event (it should be possible to parse an XML Document out of it)
 *   - 'action'  - the WS-Addressing action corresponding to this event
 */
@Deprecated
public abstract class EmitterServlet extends HttpServlet {

    public static final String PARAM_PAYLOAD = "payload";
    public static final String PARAM_ACTION = "action";

    public abstract NotificatorService getService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        doDispatch(req.getParameter(PARAM_ACTION), req.getParameter(PARAM_PAYLOAD));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        doDispatch(req.getParameter(PARAM_ACTION), req.getParameter(PARAM_PAYLOAD));
    }

    private void doDispatch(String action, String payload) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(payload));
            Document doc = db.parse(is);
            getService().dispatch(new java.net.URI(action), doc.getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
