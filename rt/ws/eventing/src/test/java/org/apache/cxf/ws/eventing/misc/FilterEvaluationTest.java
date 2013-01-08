package org.apache.cxf.ws.eventing.misc;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.shared.utils.FilteringUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.CharArrayReader;
import java.io.Reader;

/**
 * @author jmartisk
 * @since 8/29/12
 */
public class FilterEvaluationTest {

    @Test
    public void simpleFilterEvaluationPositive() throws Exception {
        Reader reader = new CharArrayReader("<tt><in>1</in></tt>".toCharArray());
        Document doc = DOMUtils.readXml(reader);
        FilterType filter = new FilterType();
        filter.getContent().add("//tt");
        Assert.assertTrue(FilteringUtil.doesConformToFilter(doc.getDocumentElement(), filter));
    }

    @Test
    public void simpleFilterEvaluationNegative() throws Exception {
        Reader reader = new CharArrayReader("<tt><in>1</in></tt>".toCharArray());
        Document doc = DOMUtils.readXml(reader);
        FilterType filter = new FilterType();
        filter.getContent().add("//ttx");
        Assert.assertFalse(FilteringUtil.doesConformToFilter(doc.getDocumentElement(), filter));
    }

}
