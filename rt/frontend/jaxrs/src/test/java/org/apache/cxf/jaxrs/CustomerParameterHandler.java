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

package org.apache.cxf.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;


public class CustomerParameterHandler implements ParamConverterProvider, ParamConverter<Customer> {

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> cls, Type arg1, Annotation[] arg2) {
        if (Customer.class.isAssignableFrom(cls)) {
            return (ParamConverter<T>)this;
        } else {
            return null;
        }
    }

    public Customer fromString(String s) throws IllegalArgumentException {
        if ("noName".equals(s)) {
            throw new IllegalArgumentException();
        }
        Customer c = Character.isLowerCase(((CharSequence)s).charAt(0)) ? new Customer2() : new Customer();
        c.setName(s);
        return c;
    }

    @Override
    public String toString(Customer arg0) throws IllegalArgumentException {
        return null;
    }
}
