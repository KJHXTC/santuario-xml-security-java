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
package com.kjhxtc.internal.apache.xml.security.stax.securityEvent;

import java.util.List;

import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityConstants;
import com.kjhxtc.internal.apache.xml.security.stax.securityToken.InboundSecurityToken;

/**
 */
public class SignedElementSecurityEvent extends AbstractSecuredElementSecurityEvent {

    public SignedElementSecurityEvent(
            InboundSecurityToken inboundSecurityToken, boolean signed,
            List<XMLSecurityConstants.ContentType> protectionOrder) {
        super(SecurityEventConstants.SignedElement, inboundSecurityToken, protectionOrder, signed, false);
    }
}
