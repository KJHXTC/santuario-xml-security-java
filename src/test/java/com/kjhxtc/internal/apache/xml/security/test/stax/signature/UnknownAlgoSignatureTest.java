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
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.stax.config.Init;
import com.kjhxtc.internal.apache.xml.security.stax.ext.InboundXMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityProperties;
import com.kjhxtc.internal.apache.xml.security.test.stax.utils.StAX2DOM;
import com.kjhxtc.internal.apache.xml.security.test.stax.utils.XMLSecEventAllocator;
import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Tests cases where signature algorithms are unknown.
 * <p>
 * The source documents are based on that created by the class <code>
 * com.kjhxtc.internal.apache.xml.security.samples.signature.CreateEnvelopingSignature</code>
 * </p>
 */
public class UnknownAlgoSignatureTest {

    private XMLInputFactory xmlInputFactory;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    @BeforeEach
    public void setUp() throws Exception {
        Init.init(UnknownAlgoSignatureTest.class.getClassLoader().getResource("security-config.xml").toURI(),
                this.getClass());
        com.kjhxtc.internal.apache.xml.security.Init.init();

        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setEventAllocator(new XMLSecEventAllocator());
    }


    @Test
    public void testGood() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "com/kjhxtc/internal/apache./xml/security/temp/signature/signature-good.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(
            this.getClass().getClassLoader().getResource(
                "com/kjhxtc/internal/apache./xml/security/samples/input/keystore.jks").openStream(), null
        );
        X509Certificate cert = (X509Certificate)keyStore.getCertificate("test");

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));
        final XMLStreamReader xmlStreamReader =
                xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(baos.toByteArray()));

        // Verify signature
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setSignatureVerificationKey(cert.getPublicKey());
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        XMLStreamReader securityStreamReader = inboundXMLSec.processInMessage(xmlStreamReader);

        document = StAX2DOM.readDoc(securityStreamReader);

        // XMLUtils.outputDOM(document, System.out);
    }

    @Test
    public void testBadC14nAlgo() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "com/kjhxtc/internal/apache./xml/security/temp/signature/signature-bad-c14n-algo.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(
            this.getClass().getClassLoader().getResource(
                "com/kjhxtc/internal/apache./xml/security/samples/input/keystore.jks").openStream(), null
        );
        X509Certificate cert = (X509Certificate)keyStore.getCertificate("test");

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
        properties.setSignatureVerificationKey(cert.getPublicKey());
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        XMLStreamReader securityStreamReader = inboundXMLSec.processInMessage(xmlStreamReader);

        try {
            StAX2DOM.readDoc(securityStreamReader);
            fail("Failure expected on a bad c14n algorithm");
        } catch (XMLStreamException ex) {
            assertTrue(ex.getCause() instanceof XMLSecurityException);
            assertEquals("Unknown transformation. No handler installed for URI http://www.apache.org/bad-c14n-algo", ex.getCause().getMessage());
        }

        // XMLUtils.outputDOM(document, System.out);
    }

    @Test
    public void testBadSigAlgo() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "com/kjhxtc/internal/apache./xml/security/temp/signature/signature-bad-sig-algo.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(
            this.getClass().getClassLoader().getResource(
                "com/kjhxtc/internal/apache./xml/security/samples/input/keystore.jks").openStream(), null
        );
        X509Certificate cert = (X509Certificate)keyStore.getCertificate("test");

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
        properties.setSignatureVerificationKey(cert.getPublicKey());
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        XMLStreamReader securityStreamReader = inboundXMLSec.processInMessage(xmlStreamReader);

        try {
            StAX2DOM.readDoc(securityStreamReader);
            fail("Failure expected on a bad signature algorithm");
        } catch (XMLStreamException ex) {
            assertTrue(ex.getCause() instanceof XMLSecurityException);
            assertEquals("The algorithm URI \"http://www.apache.org/bad-sig-algo\" could not be mapped to a JCE algorithm",
                    ex.getCause().getMessage());
        }

        // XMLUtils.outputDOM(document, System.out);
    }

    @Test
    public void testBadTransformAlgo() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "com/kjhxtc/internal/apache./xml/security/temp/signature/signature-bad-transform-algo.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(
            this.getClass().getClassLoader().getResource(
                "com/kjhxtc/internal/apache./xml/security/samples/input/keystore.jks").openStream(), null
        );
        X509Certificate cert = (X509Certificate)keyStore.getCertificate("test");

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
        properties.setSignatureVerificationKey(cert.getPublicKey());
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        XMLStreamReader securityStreamReader = inboundXMLSec.processInMessage(xmlStreamReader);

        try {
            StAX2DOM.readDoc(securityStreamReader);
            fail("Failure expected on a bad transform algorithm");
        } catch (XMLStreamException ex) {
            assertTrue(ex.getCause() instanceof XMLSecurityException);
            assertEquals("INVALID signature -- core validation failed.", ex.getCause().getMessage());
        }

        // XMLUtils.outputDOM(document, System.out);
    }


}