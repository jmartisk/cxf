package org.apache.cxf.ws.eventing.jaxbutils;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author jmartisk
 * @since 9/11/12
 */
@XmlJavaTypeAdapter(DurationDateTimeAdapter.class)
@Deprecated
public class DurationDateTime {

    private final Object value;

    public DurationDateTime(Object value) {
        this.value = value;
    }

    public Object getDateTime() {
        return value;
    }
}
