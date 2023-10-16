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

import java.util.List;

import javax.xml.namespace.QName;

import com.kjhxtc.internal.apache.xml.security.binding.xmldsig.ReferenceType;
import com.kjhxtc.internal.apache.xml.security.binding.xmldsig.SignatureType;
import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.stax.ext.DocumentContext;
import com.kjhxtc.internal.apache.xml.security.stax.ext.InputProcessorChain;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityProperties;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecEvent;
import com.kjhxtc.internal.apache.xml.security.stax.securityEvent.SignedElementSecurityEvent;
import com.kjhxtc.internal.apache.xml.security.stax.securityToken.InboundSecurityToken;

/**
 * A processor to verify XML Signature references.
 */
public class XMLSignatureReferenceVerifyInputProcessor extends AbstractSignatureReferenceVerifyInputProcessor {

    public XMLSignatureReferenceVerifyInputProcessor(
            InputProcessorChain inputProcessorChain, SignatureType signatureType, InboundSecurityToken inboundSecurityToken,
            XMLSecurityProperties securityProperties) throws XMLSecurityException {

        super(inputProcessorChain, signatureType, inboundSecurityToken, securityProperties);
        this.addAfterProcessor(XMLSignatureReferenceVerifyInputProcessor.class.getName());
    }

    @Override
    protected void processElementPath(
            List<QName> elementPath, InputProcessorChain inputProcessorChain, XMLSecEvent xmlSecEvent,
            ReferenceType referenceType) throws XMLSecurityException {
        final DocumentContext documentContext = inputProcessorChain.getDocumentContext();
        SignedElementSecurityEvent signedElementSecurityEvent =
                new SignedElementSecurityEvent(getInboundSecurityToken(), true, documentContext.getProtectionOrder());
        signedElementSecurityEvent.setElementPath(elementPath);
        signedElementSecurityEvent.setXmlSecEvent(xmlSecEvent);
        signedElementSecurityEvent.setCorrelationID(referenceType.getId());
        inputProcessorChain.getSecurityContext().registerSecurityEvent(signedElementSecurityEvent);
    }

}
