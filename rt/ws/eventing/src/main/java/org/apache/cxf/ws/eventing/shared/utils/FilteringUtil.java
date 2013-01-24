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

package org.apache.cxf.ws.eventing.shared.utils;

import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.eventing.FilterType;
import org.apache.cxf.ws.eventing.shared.faults.EmptyFilter;

public final class FilteringUtil {

    public static final String NAMESPACE_XPATH10 = "http://www.w3.org/2011/03/ws-evt/Dialects/XPath10";
    public static final String NAMESPACE_XPATH20 = "http://www.w3.org/2011/03/ws-evt/Dialects/XPath20";

    private static final Logger LOG = LogUtils.getLogger(FilteringUtil.class);

    private FilteringUtil() {

    }

    public static boolean isFilteringDialectSupported(String namespace) {
        return namespace.equals(NAMESPACE_XPATH10) || namespace.equals(NAMESPACE_XPATH20);
    }

    public static boolean doesConformToFilter(Element elm, FilterType filter) {
        if ((filter == null) || (filter.getContent() == null)) {
            return true;
        }
        String xPathString = (String)filter.getContent().get(0);
        try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression xPathExpression = xPath.compile(xPathString);
            return (Boolean)xPathExpression.evaluate(elm, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException ex) {
            LOG.severe(ex.toString());
            throw new EmptyFilter();
        }
    }
}
