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
package com.kjhxtc.internal.apache.xml.security.stax.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.kjhxtc.internal.apache.xml.security.configuration.HandlerType;
import com.kjhxtc.internal.apache.xml.security.configuration.SecurityHeaderHandlersType;
import com.kjhxtc.internal.apache.xml.security.utils.ClassLoaderUtils;

/**
 * Security-header handler mapper
 *
 */
public final class SecurityHeaderHandlerMapper {

    private static Map<QName, Class<?>> handlerClassMap;

    private SecurityHeaderHandlerMapper() {
    }

    protected static synchronized void init(SecurityHeaderHandlersType securityHeaderHandlersType,
            Class<?> callingClass) throws Exception {
        List<HandlerType> handlerList = securityHeaderHandlersType.getHandler();
        final int handlerListSize = handlerList.size();
        handlerClassMap = new HashMap<>((int) Math.ceil(handlerListSize / 0.75));
        for (int i = 0; i < handlerListSize; i++) {
            HandlerType handlerType = handlerList.get(i);
            QName qName = new QName(handlerType.getURI(), handlerType.getNAME());
            handlerClassMap.put(qName,
                    ClassLoaderUtils.loadClass(handlerType.getJAVACLASS(), callingClass));
        }
    }

    public static Class<?> getSecurityHeaderHandler(QName name) {
        return handlerClassMap.get(name);
    }
}
