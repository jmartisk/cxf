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

package org.apache.cxf.ws.policy.attachment;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.neethi.Policy;

@NoJSR250Annotations
public class ServiceModelPolicyProvider extends AbstractPolicyProvider {

    public ServiceModelPolicyProvider(Bus b) {
        super(b);
    }

    public Policy getEffectivePolicy(BindingFaultInfo bfi) {
        return bfi.getExtensor(Policy.class);
    }

    public Policy getEffectivePolicy(BindingMessageInfo bmi) {
        return bmi.getExtensor(Policy.class);
    }

    public Policy getEffectivePolicy(BindingOperationInfo bi) {
        return bi.getExtensor(Policy.class);
    }

    public Policy getEffectivePolicy(EndpointInfo ei) {
        return ei.getExtensor(Policy.class);
    }

    public Policy getEffectivePolicy(ServiceInfo si) {
        return si.getExtensor(Policy.class);
    }

}
