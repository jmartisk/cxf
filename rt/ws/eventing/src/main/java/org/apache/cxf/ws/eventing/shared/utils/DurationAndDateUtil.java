package org.apache.cxf.ws.eventing.shared.utils;

import org.apache.cxf.ws.eventing.ExpirationType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * @author jmartisk
 * @since 9/12/12
 */
public class DurationAndDateUtil {

    private static DatatypeFactory factory;

    static {
        try {
            factory =  DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            throw new RuntimeException("Cannot instantiate a DatatypeFactory required for unmarshalling to XMLGregorianCalendar and Duration", ex);
        }
    }

    public static Duration parseDuration(String input) throws IllegalArgumentException {
        return factory.newDuration(input);
    }

    public static XMLGregorianCalendar parseXMLGregorianCalendar(String input) throws IllegalArgumentException {
        return factory.newXMLGregorianCalendar(input);
    }

    public static boolean isXMLGregorianCalendar(String input) {
        try {
            factory.newXMLGregorianCalendar(input);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isDuration(String input) {
        try {
            factory.newDuration(input);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static Object parseDurationOrTimestamp(String input) throws IllegalArgumentException {
        Object ret;
        try {
            ret = factory.newDuration(input);
        } catch(Exception e) {
            ret = factory.newXMLGregorianCalendar(input);
        }
        return ret;
    }

    public static String convertToXMLString(Object input) {
        if(input instanceof XMLGregorianCalendar)
            return ((XMLGregorianCalendar)input).toXMLFormat();
        if(input instanceof Duration)
            return ((Duration)input).toString();
        throw new IllegalArgumentException("convertToXMLString requires either an instance of XMLGregorianCalendar or Duration");
    }

    public static ExpirationType toExpirationTypeContainingGregorianCalendar(XMLGregorianCalendar date) {
        ExpirationType et = new ExpirationType();
        et.setValue(date.toXMLFormat());
        return et;
    }

    public static ExpirationType toExpirationTypeContainingDuration(XMLGregorianCalendar date) {
        ExpirationType et = new ExpirationType();
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar then = date.toGregorianCalendar();
        long durationMillis = then.getTimeInMillis() - now.getTimeInMillis();
        Duration duration = factory.newDuration(durationMillis);
        et.setValue(duration.toString());
        return et;
    }

}
