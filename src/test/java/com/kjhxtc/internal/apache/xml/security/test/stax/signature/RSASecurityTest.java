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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.kjhxtc.internal.apache.xml.security.stax.ext.InboundXMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityProperties;
import com.kjhxtc.internal.apache.xml.security.stax.securityToken.SecurityTokenConstants;
import com.kjhxtc.internal.apache.xml.security.test.stax.utils.StAX2DOM;
import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * This test is to ensure interoperability with the examples provided by Blake Dournaee
 * from RSA Security using Cert-J 2.01. These test vectors are located in the directory
 * <CODE>data/com/rsasecurity/bdournaee/</CODE>.
 */
public class RSASecurityTest extends AbstractSignatureVerificationTest {

    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    @Test
    public void test_enveloping() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "com/rsasecurity/bdournaee/certj201_enveloping.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Verify signature
        XMLSecurityProperties properties = new XMLSecurityProperties();
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        StAX2DOM.readDoc(securityStreamReader);

        // Check the SecurityEvents
        checkSignatureToken(securityEventListener, null, null,
                            SecurityTokenConstants.KeyIdentifier_KeyValue);
    }

    // See SANTUARIO-320
    @Test
    public void test_enveloped() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "com/rsasecurity/bdournaee/certj201_enveloped.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Verify signature
        XMLSecurityProperties properties = new XMLSecurityProperties();
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        StAX2DOM.readDoc(securityStreamReader);

        // Check the SecurityEvents
        checkSignatureToken(securityEventListener, null, null,
                            SecurityTokenConstants.KeyIdentifier_KeyValue);
    }


}
