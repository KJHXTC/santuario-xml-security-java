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
package com.kjhxtc.internal.apache.xml.security.stax.securityToken;

import com.kjhxtc.internal.apache.xml.security.binding.xmldsig.KeyInfoType;
import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.stax.config.ConfigurationProperties;
import com.kjhxtc.internal.apache.xml.security.stax.ext.InboundSecurityContext;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityProperties;
import com.kjhxtc.internal.apache.xml.security.utils.ClassLoaderUtils;
import com.kjhxtc.internal.apache.xml.security.utils.JavaUtils;

/**
 * Factory to create SecurityToken Objects from keys in XML
 *
 */
public abstract class SecurityTokenFactory {

    private static SecurityTokenFactory instance;

    public static synchronized SecurityTokenFactory getInstance() throws XMLSecurityException {
        if (instance == null) {
            String stf = ConfigurationProperties.getProperty("securityTokenFactory");
            if (stf == null) {
                throw new XMLSecurityException("algorithm.ClassDoesNotExist",
                                               new Object[] {"null"});
            }
            Class<?> callingClass = ConfigurationProperties.getCallingClass();
            if (callingClass == null) {
                callingClass = SecurityTokenFactory.class;
            }

            try {
                @SuppressWarnings("unchecked")
                Class<SecurityTokenFactory> securityTokenFactoryClass =
                        (Class<SecurityTokenFactory>) ClassLoaderUtils.loadClass(stf, callingClass);
                instance = JavaUtils.newInstanceWithEmptyConstructor(securityTokenFactoryClass);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new XMLSecurityException(e, "algorithm.ClassDoesNotExist", new Object[]{stf});
            }
        }
        return instance;
    }

    public abstract InboundSecurityToken getSecurityToken(
            KeyInfoType keyInfoType, SecurityTokenConstants.KeyUsage keyUsage,
            XMLSecurityProperties securityProperties, InboundSecurityContext inboundSecurityContext)
            throws XMLSecurityException;
}
