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
package org.apache.cxf.systest.ws.action;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class SecretKeyPasswordCallback implements CallbackHandler {
    
    private static final byte[] KEY = {
        (byte)0x31, (byte)0xfd,
        (byte)0xcb, (byte)0xda,
        (byte)0xfb, (byte)0xcd,
        (byte)0x6b, (byte)0xa8,
        (byte)0xe6, (byte)0x19,
        (byte)0xa7, (byte)0xbf,
        (byte)0x51, (byte)0xf7,
        (byte)0xc7, (byte)0x3e,
        (byte)0x80, (byte)0xae,
        (byte)0x98, (byte)0x51,
        (byte)0xc8, (byte)0x51,
        (byte)0x34, (byte)0x04,
    };
    
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback)callbacks[i];
            if (pc.getUsage() == WSPasswordCallback.Usage.SECRET_KEY) {
                pc.setKey(KEY);
            }
        }
    }
    
}
