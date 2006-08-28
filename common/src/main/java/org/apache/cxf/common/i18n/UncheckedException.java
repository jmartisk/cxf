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

package org.apache.cxf.common.i18n;



public class UncheckedException extends java.lang.RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final Message message;
    
    public UncheckedException(Message msg) {
        message = msg;
    }
    
    public UncheckedException(Message msg, Throwable t) {
        super(t);
        message = msg;
    }
    
    public UncheckedException(Throwable cause) {
        super(cause);
        message = null;
    } 
    
    // the above constructors should be preferred to the following ones
    
    @Deprecated
    public UncheckedException() {
        super();
        message = null;
    }
    
    @Deprecated
    public UncheckedException(String msg) {
        super(msg);
        message = null;
    }
    
    @Deprecated
    public UncheckedException(String msg, Throwable t) {
        super(msg, t);
        message = null;
    }

    public String getCode() {
        if (null != message) {
            return message.getCode();
        }
        return null;
    }
    
    public String getMessage() {
        if (null != message) {
            return message.toString();
        }
        return null;
    }
}
