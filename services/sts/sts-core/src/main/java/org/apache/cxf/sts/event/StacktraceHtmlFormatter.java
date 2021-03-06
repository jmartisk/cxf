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

package org.apache.cxf.sts.event;

public class StacktraceHtmlFormatter implements StacktraceFormatter {

    public static final String NEW_LINE = "<br>";
    
    @Override
    public String format(Throwable t) {
        final StringBuilder result = new StringBuilder("<html>");
        result.append(t.toString());
        result.append(NEW_LINE);

        for (StackTraceElement element : t.getStackTrace()) {
            result.append(element);
            result.append(NEW_LINE);
        }
        result.append("</html>");
        return result.toString();
    }

}
