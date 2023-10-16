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
package com.kjhxtc.internal.apache.xml.security.stax.impl.processor.input;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.stax.ext.AbstractInputProcessor;
import com.kjhxtc.internal.apache.xml.security.stax.ext.InputProcessorChain;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityConstants;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityProperties;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class LogInputProcessor extends AbstractInputProcessor {

    private static final transient Logger LOG = LoggerFactory.getLogger(LogInputProcessor.class);

    public LogInputProcessor(XMLSecurityProperties securityProperties) {
        super(securityProperties);
        setPhase(XMLSecurityConstants.Phase.POSTPROCESSING);
    }

    @Override
    public XMLSecEvent processHeaderEvent(InputProcessorChain inputProcessorChain)
            throws XMLStreamException, XMLSecurityException {
        return inputProcessorChain.processHeaderEvent();
    }

    @Override
    public XMLSecEvent processEvent(InputProcessorChain inputProcessorChain)
            throws XMLStreamException, XMLSecurityException {
        XMLSecEvent xmlSecEvent = inputProcessorChain.processEvent();
        StringWriter stringWriter = new StringWriter();
        xmlSecEvent.writeAsEncodedUnicode(stringWriter);
        LOG.trace(stringWriter.toString());
        return xmlSecEvent;
    }
}
