package org.apache.cxf.ws.eventing.shared.jaxbutils;

import org.apache.cxf.ws.eventing.shared.faults.UnsupportedExpirationType;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author jmartisk
 * @since 9/11/12
 */
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
