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

package org.apache.cxf.binding.soap.interceptor;

import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;

public class Soap11FaultOutInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(Soap11FaultOutInterceptor.class);

    public Soap11FaultOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(SoapMessage message) throws Fault {
        message.put(org.apache.cxf.message.Message.RESPONSE_CODE, new Integer(500));

        XMLStreamWriter writer = message.getContent(XMLStreamWriter.class);
        Fault f = (Fault) message.getContent(Exception.class);

        SoapFault fault = SoapFault.createFault(f, message.getVersion());

        try {
            Map<String, String> namespaces = fault.getNamespaces();
            for (Map.Entry<String, String> e : namespaces.entrySet()) {
                writer.writeNamespace(e.getKey(), e.getValue());
            }

            String ns = message.getVersion().getNamespace();
            String defaultPrefix = StaxUtils.getUniquePrefix(writer, ns, true);

            writer.writeStartElement(defaultPrefix, "Fault", ns);

            writer.writeStartElement("faultcode");

            String codeString = fault.getCodeString(getFaultCodePrefix(writer, fault.getFaultCode()),
                    defaultPrefix);

            writer.writeCharacters(codeString);
            writer.writeEndElement();

            writer.writeStartElement("faultstring");
            if (fault.getMessage() != null) {
                writer.writeCharacters(fault.getMessage());
            } else {
                writer.writeCharacters("Fault occurred while processing.");
            }
            writer.writeEndElement();
            Object config = message.getContextualProperty(
                    org.apache.cxf.message.Message.FAULT_STACKTRACE_ENABLED);
            if (config != null && Boolean.TRUE.equals(config) && fault.getCause() != null) {                
                StringBuffer sb = new StringBuffer();
                for (StackTraceElement stk : fault.getCause().getStackTrace()) {
                    sb.append(stk.toString());
                }
                try {
                    if (fault.getDetail() == null) {
                        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                                .newDocument();
                        Element detail = doc.createElementNS(Soap11.SOAP_NAMESPACE, "detail");
                        Element stackTrace = doc.createElementNS(Soap11.SOAP_NAMESPACE, "stackTrace");
                        stackTrace.setTextContent(sb.toString());
                        detail.appendChild(stackTrace);
                        fault.setDetail(detail);
                    }
                } catch (ParserConfigurationException pe) {
                    // move on...
                }
            }

            if (fault.hasDetails()) {
                Element detail = fault.getDetail();
                writer.writeStartElement("detail");

                NodeList details = detail.getChildNodes();
                for (int i = 0; i < details.getLength(); i++) {
                    StaxUtils.writeNode(details.item(i), writer, true);
                }

                // Details
                writer.writeEndElement();
            }

            if (fault.getRole() != null) {
                writer.writeStartElement("faultactor");
                writer.writeCharacters(fault.getRole());
                writer.writeEndElement();
            }

            // Fault
            writer.writeEndElement();
        } catch (XMLStreamException xe) {
            throw new Fault(new Message("XML_WRITE_EXC", BUNDLE), xe);
        }
    }
}
