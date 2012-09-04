package org.apache.cxf.ws.eventing;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.ws.eventing.filter.FilteringUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jmartisk
 * @since 8/29/12
 */
public class FilterEvaluationTest {

    @Test
    public void simpleFilterEvaluationPositive() throws Exception {
        Reader reader = new CharArrayReader("<tt><in>1</in></tt>".toCharArray());
        Document doc = DOMUtils.readXml(reader);
        List<String> filters = new ArrayList<String>();
        filters.add("//tt");
        Assert.assertTrue(FilteringUtil.doesConformToFilter(doc.getDocumentElement(), filters));
    }

    @Test
    public void simpleFilterEvaluationNegative() throws Exception {
        Reader reader = new CharArrayReader("<tt><in>1</in></tt>".toCharArray());
        Document doc = DOMUtils.readXml(reader);
        List<String> filters = new ArrayList<String>();
        filters.add("//ttx");
        Assert.assertFalse(FilteringUtil.doesConformToFilter(doc.getDocumentElement(), filters));
    }

}
