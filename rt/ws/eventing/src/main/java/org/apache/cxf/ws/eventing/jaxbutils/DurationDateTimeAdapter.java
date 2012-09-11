package org.apache.cxf.ws.eventing.jaxbutils;

import org.apache.cxf.ws.eventing.faults.UnsupportedExpirationType;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author jmartisk
 * @since 9/11/12
 */
public class DurationDateTimeAdapter extends XmlAdapter<String,DurationDateTime> {


    @Override
    public DurationDateTime unmarshal(String v) throws Exception {
        try {
            // does the received string conform to xs:dateTime ? if so, parse a XMLGregorianCalendar out of it
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(v);
            return new DurationDateTime(calendar);
        } catch (IllegalArgumentException ex) {
            DatatypeFactory factory = DatatypeFactory.newInstance();
            try {
                return new DurationDateTime(factory.newDuration(v));
            } catch(IllegalArgumentException e) {
                throw new UnsupportedExpirationType();
            }
        }
    }

    @Override
    public String marshal(DurationDateTime v) throws Exception {
        return v.getDateTime().toString();
    }
}
