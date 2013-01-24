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

package org.apache.cxf.ws.eventing.shared.jaxbutils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.cxf.ws.eventing.shared.faults.UnsupportedExpirationType;

@Deprecated
public class DurationDateTimeAdapter extends XmlAdapter<String, DurationDateTime> {


    @Override
    public DurationDateTime unmarshal(String v) throws Exception {
        DatatypeFactory factory = DatatypeFactory.newInstance();
        try {
            // does the received string conform to xs:dateTime ? if so, parse a XMLGregorianCalendar out of it
            XMLGregorianCalendar calendar = factory.newXMLGregorianCalendar(v);
            return new DurationDateTime(calendar);
        } catch (IllegalArgumentException ex) {
            try {
                return new DurationDateTime(factory.newDuration(v));
            } catch (IllegalArgumentException e) {
                throw new UnsupportedExpirationType();
            }
        }
    }

    @Override
    public String marshal(DurationDateTime v) throws Exception {
        return v.getDateTime().toString();
    }
}
