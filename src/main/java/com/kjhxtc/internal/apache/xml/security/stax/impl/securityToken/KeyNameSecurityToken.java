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
package com.kjhxtc.internal.apache.xml.security.stax.impl.securityToken;

import com.kjhxtc.internal.apache.xml.security.stax.ext.InboundSecurityContext;
import com.kjhxtc.internal.apache.xml.security.stax.impl.util.IDGenerator;
import com.kjhxtc.internal.apache.xml.security.stax.securityToken.SecurityTokenConstants;


/**
 */
public class KeyNameSecurityToken extends AbstractInboundSecurityToken {

    private String keyName;

    public KeyNameSecurityToken(String keyName, InboundSecurityContext inboundSecurityContext) {
        super(inboundSecurityContext, IDGenerator.generateID(null), SecurityTokenConstants.KeyIdentifier_KeyName, false);
        this.keyName = keyName;
    }

    @Override
    public SecurityTokenConstants.TokenType getTokenType() {
        return SecurityTokenConstants.KeyNameToken;
    }

    public String getKeyName() {
        return keyName;
    }
}
