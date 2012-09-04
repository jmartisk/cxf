package org.apache.cxf.ws.eventing.filter;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.faults.EmptyFilter;
import org.w3c.dom.Element;

import javax.xml.xpath.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jmartisk
 * @since 8/29/12
 */
public class FilteringUtil {

    public static final String NAMESPACE_XPATH10 = "http://www.w3.org/2011/03/ws-evt/Dialects/XPath10";
    public static final String NAMESPACE_XPATH20 = "http://www.w3.org/2011/03/ws-evt/Dialects/XPath20";

    private static final Logger LOG = LogUtils.getLogger(FilteringUtil.class);

    public static boolean isFilteringDialectSupported(String namespace) {
        return (namespace.equals(NAMESPACE_XPATH10) || namespace.equals(NAMESPACE_XPATH20));
    }

    public static boolean doesConformToFilter(Element elm, List<String> filter) {
        if(filter.size() == 0)
            return true;
        try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            for (String xpathString : filter) {
                XPathExpression xPathExpression = xPath.compile(xpathString);
                boolean ok = (Boolean) xPathExpression.evaluate(elm, XPathConstants.BOOLEAN);
                if(!ok)
                    return false;
            }
            return true;
        } catch (XPathExpressionException ex) {
            LOG.severe(ex.toString());
            throw new EmptyFilter();
        }
    }
}
