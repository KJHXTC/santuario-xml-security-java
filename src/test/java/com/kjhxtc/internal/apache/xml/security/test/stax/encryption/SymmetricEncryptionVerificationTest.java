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
package com.kjhxtc.internal.apache.xml.security.test.stax.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.Key;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.kjhxtc.internal.apache.xml.security.encryption.EncryptedData;
import com.kjhxtc.internal.apache.xml.security.encryption.EncryptedKey;
import com.kjhxtc.internal.apache.xml.security.encryption.XMLCipher;
import com.kjhxtc.internal.apache.xml.security.keys.KeyInfo;
import com.kjhxtc.internal.apache.xml.security.stax.ext.InboundXMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityConstants;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityProperties;
import com.kjhxtc.internal.apache.xml.security.test.dom.DSNamespaceContext;
import com.kjhxtc.internal.apache.xml.security.test.stax.signature.TestSecurityEventListener;
import com.kjhxtc.internal.apache.xml.security.test.stax.utils.StAX2DOM;
import com.kjhxtc.internal.apache.xml.security.test.stax.utils.XMLSecEventAllocator;
import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test to make sure that the various Symmetric Encryption algorithms are working
 */
public class SymmetricEncryptionVerificationTest {

    private static boolean bcInstalled;
    private final XMLInputFactory xmlInputFactory;
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    @BeforeAll
    public static void setup() throws Exception {
        //
        // If the BouncyCastle provider is not installed, then try to load it
        // via reflection.
        //
        if (Security.getProvider("BC") == null) {
            Constructor<?> cons = null;
            try {
                Class<?> c = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                cons = c.getConstructor(new Class[] {});
            } catch (Exception e) {
                //ignore
            }
            if (cons != null) {
                Provider provider = (Provider)cons.newInstance();
                Security.insertProviderAt(provider, 2);
                bcInstalled = true;
            }
        }
    }

    public SymmetricEncryptionVerificationTest() throws Exception {
        com.kjhxtc.internal.apache.xml.security.Init.init();

        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setEventAllocator(new XMLSecEventAllocator());
    }

    @AfterAll
    public static void cleanup() throws Exception {
        Security.removeProvider("BC");
    }

    @Test
    public void testAES128() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testAES128_GCM() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2009/xmlenc11#aes128-gcm";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testAES192() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(192);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testAES192_GCM() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(192);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2009/xmlenc11#aes192-gcm";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testAES256() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testAES256_GCM() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2009/xmlenc11#aes256-gcm";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testTRIPLE_DES() throws Exception {
        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("DESede");
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testSEED_128() throws Exception {
        Assumptions.assumeTrue(bcInstalled);

        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("SEED");
        keygen.init(128);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2007/05/xmldsig-more#seed128-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testCAMELLIA_128() throws Exception {
        Assumptions.assumeTrue(bcInstalled);

        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("Camellia");
        keygen.init(128);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2001/04/xmldsig-more#camellia128-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testCAMELLIA_192() throws Exception {
        Assumptions.assumeTrue(bcInstalled);

        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("Camellia");
        keygen.init(192);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2001/04/xmldsig-more#camellia192-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    @Test
    public void testCAMELLIA_256() throws Exception {
        Assumptions.assumeTrue(bcInstalled);

        // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        Document document = XMLUtils.read(sourceDocument, false);

        // Set up the Key
        KeyGenerator keygen = KeyGenerator.getInstance("Camellia");
        keygen.init(256);
        SecretKey key = keygen.generateKey();

        // Encrypt using DOM
        List<String> localNames = new ArrayList<>();
        localNames.add("PaymentInfo");
        String algorithm = "http://www.w3.org/2001/04/xmldsig-more#camellia256-cbc";
        encryptUsingDOM(
            algorithm, key, null, null, document, localNames, false
        );

        // Check the CreditCard encrypted ok
        NodeList nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 0);

        // XMLUtils.outputDOM(document, System.out);

        // Convert Document to a Stream Reader
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(baos));

        XMLStreamReader xmlStreamReader = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
           xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        }

        // Decrypt
        XMLSecurityProperties properties = new XMLSecurityProperties();
        properties.setDecryptionKey(key);
        InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);
        TestSecurityEventListener securityEventListener = new TestSecurityEventListener();
        XMLStreamReader securityStreamReader =
                inboundXMLSec.processInMessage(xmlStreamReader, null, securityEventListener);

        document = StAX2DOM.readDoc(securityStreamReader);

        // Check the CreditCard decrypted ok
        nodeList = document.getElementsByTagNameNS("urn:example:po", "CreditCard");
        assertEquals(nodeList.getLength(), 1);
    }

    private void encryptUsingDOM(
            String algorithm,
            SecretKey secretKey,
            String keyTransportAlgorithm,
            Key wrappingKey,
            Document document,
            List<String> localNames,
            boolean content
        ) throws Exception {
        encryptUsingDOM(algorithm, secretKey, keyTransportAlgorithm, wrappingKey, false,
                document, localNames, content);
    }

    /**
     * Encrypt the document using DOM APIs and run some tests on the encrypted Document.
     */
    private void encryptUsingDOM(
        String algorithm,
        SecretKey secretKey,
        String keyTransportAlgorithm,
        Key wrappingKey,
        boolean includeWrappingKeyInfo,
        Document document,
        List<String> localNames,
        boolean content
    ) throws Exception {
        XMLCipher cipher = XMLCipher.getInstance(algorithm);
        cipher.init(XMLCipher.ENCRYPT_MODE, secretKey);

        if (wrappingKey != null) {
            XMLCipher newCipher = XMLCipher.getInstance(keyTransportAlgorithm);
            newCipher.init(XMLCipher.WRAP_MODE, wrappingKey);
            EncryptedKey encryptedKey = newCipher.encryptKey(document, secretKey);
            if (includeWrappingKeyInfo && wrappingKey instanceof PublicKey) {
                // Create a KeyInfo for the EncryptedKey
                KeyInfo encryptedKeyKeyInfo = encryptedKey.getKeyInfo();
                if (encryptedKeyKeyInfo == null) {
                    encryptedKeyKeyInfo = new KeyInfo(document);
                    encryptedKeyKeyInfo.getElement().setAttributeNS(
                        "http://www.w3.org/2000/xmlns/", "xmlns:dsig", "http://www.w3.org/2000/09/xmldsig#"
                    );
                    encryptedKey.setKeyInfo(encryptedKeyKeyInfo);
                }
                encryptedKeyKeyInfo.add((PublicKey)wrappingKey);
            }

            EncryptedData builder = cipher.getEncryptedData();

            KeyInfo builderKeyInfo = builder.getKeyInfo();
            if (builderKeyInfo == null) {
                builderKeyInfo = new KeyInfo(document);
                builderKeyInfo.getElement().setAttributeNS(
                    "http://www.w3.org/2000/xmlns/", "xmlns:dsig", "http://www.w3.org/2000/09/xmldsig#"
                );
                builder.setKeyInfo(builderKeyInfo);
            }

            builderKeyInfo.add(encryptedKey);
        }

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new DSNamespaceContext());

        for (String localName : localNames) {
            String expression = "//*[local-name()='" + localName + "']";
            Element elementToEncrypt =
                    (Element) xpath.evaluate(expression, document, XPathConstants.NODE);
            assertNotNull(elementToEncrypt);

            document = cipher.doFinal(document, elementToEncrypt, content);
        }

        NodeList nodeList = document.getElementsByTagNameNS(
                XMLSecurityConstants.TAG_xenc_EncryptedData.getNamespaceURI(),
                XMLSecurityConstants.TAG_xenc_EncryptedData.getLocalPart()
            );
        assertTrue(nodeList.getLength() > 0);
    }

}