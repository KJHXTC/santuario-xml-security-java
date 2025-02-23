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
package com.kjhxtc.internal.apache.xml.security.stax.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.stax.config.ConfigurationProperties;
import com.kjhxtc.internal.apache.xml.security.stax.ext.InboundSecurityContext;
import com.kjhxtc.internal.apache.xml.security.stax.securityEvent.AlgorithmSuiteSecurityEvent;
import com.kjhxtc.internal.apache.xml.security.stax.securityEvent.SecurityEvent;
import com.kjhxtc.internal.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import com.kjhxtc.internal.apache.xml.security.stax.securityToken.InboundSecurityToken;
import com.kjhxtc.internal.apache.xml.security.stax.securityToken.SecurityTokenProvider;

/**
 * Concrete security context implementation
 *
 */
public class InboundSecurityContextImpl extends AbstractSecurityContextImpl implements InboundSecurityContext {

    private static final Boolean allowMD5Algorithm = Boolean.valueOf(ConfigurationProperties.getProperty("AllowMD5Algorithm"));
    private final Map<String, SecurityTokenProvider<? extends InboundSecurityToken>> securityTokenProviders =
            new HashMap<>();

    @Override
    protected void forwardSecurityEvent(SecurityEvent securityEvent) throws XMLSecurityException {
        if (!InboundSecurityContextImpl.allowMD5Algorithm && SecurityEventConstants.AlgorithmSuite.equals(securityEvent.getSecurityEventType())) {
            AlgorithmSuiteSecurityEvent algorithmSuiteSecurityEvent = (AlgorithmSuiteSecurityEvent)securityEvent;
            if (algorithmSuiteSecurityEvent.getAlgorithmURI().contains("md5") ||
                    algorithmSuiteSecurityEvent.getAlgorithmURI().contains("MD5")) {
                throw new XMLSecurityException("secureProcessing.AllowMD5Algorithm");
            }
        }
        super.forwardSecurityEvent(securityEvent);
    }

    @Override
    public void registerSecurityTokenProvider(String id, SecurityTokenProvider<? extends InboundSecurityToken> securityTokenProvider) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        securityTokenProviders.put(id, securityTokenProvider);
    }

    @Override
    public SecurityTokenProvider<? extends InboundSecurityToken> getSecurityTokenProvider(String id) {
        return securityTokenProviders.get(id);
    }

    @Override
    public List<SecurityTokenProvider<? extends InboundSecurityToken>> getRegisteredSecurityTokenProviders() {
        return new ArrayList<>(securityTokenProviders.values());
    }
}
