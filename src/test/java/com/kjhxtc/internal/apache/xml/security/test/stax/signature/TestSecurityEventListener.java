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
package com.kjhxtc.internal.apache.xml.security.test.stax.signature;

import java.util.ArrayList;
import java.util.List;

import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.stax.securityEvent.SecurityEvent;
import com.kjhxtc.internal.apache.xml.security.stax.securityEvent.SecurityEventConstants.Event;
import com.kjhxtc.internal.apache.xml.security.stax.securityEvent.SecurityEventListener;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSecurityEventListener implements SecurityEventListener {
    private List<SecurityEvent> events = new ArrayList<>();

    @Override
    public void registerSecurityEvent(SecurityEvent securityEvent)
            throws XMLSecurityException {

        assertNotNull("CorrelationID of SecurityEvent is null: " + securityEvent.getSecurityEventType(), securityEvent.getCorrelationID());
        assertTrue(securityEvent.getCorrelationID().length() > 0, "CorrelationID of SecurityEvent is empty: " + securityEvent.getSecurityEventType());
        events.add(securityEvent);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSecurityEvent(Event securityEvent) {
        for (SecurityEvent event : events) {
            if (event.getSecurityEventType() == securityEvent) {
                return (T)event;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getSecurityEvents(Event securityEvent) {
        List<T> foundEvents = new ArrayList<>();
        for (SecurityEvent event : events) {
            if (event.getSecurityEventType() == securityEvent) {
                foundEvents.add((T)event);
            }
        }
        return foundEvents;
    }

    public List<SecurityEvent> getSecurityEvents() {
        return events;
    }
}