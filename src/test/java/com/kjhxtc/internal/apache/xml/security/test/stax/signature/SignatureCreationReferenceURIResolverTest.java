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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.kjhxtc.internal.apache.xml.security.stax.ext.OutboundXMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.SecurePart;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSec;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityConstants;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityProperties;
import com.kjhxtc.internal.apache.xml.security.stax.impl.resourceResolvers.ResolverHttp;
import com.kjhxtc.internal.apache.xml.security.test.stax.utils.HttpRequestRedirectorProxy;
import com.kjhxtc.internal.apache.xml.security.test.stax.utils.XmlReaderToWriter;
import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;
import com.kjhxtc.internal.apache.xml.security.utils.resolver.ResourceResolver;
import com.kjhxtc.internal.apache.xml.security.utils.resolver.implementations.ResolverDirectHTTP;
import com.kjhxtc.internal.apache.xml.security.utils.resolver.implementations.ResolverLocalFilesystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static com.kjhxtc.internal.apache.xml.security.test.XmlSecTestEnvironment.resolveFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 */
public class SignatureCreationReferenceURIResolverTest extends AbstractSignatureCreationTest {

    @BeforeAll
    public static void setup() throws Exception {
        AbstractSignatureCreationTest.setup();
        ResourceResolver.register(new ResolverLocalFilesystem(), false);
    }

    @Test
    public void testSignatureCreationWithExternalFilesystemXMLReference() throws Exception {
        // Set up the Configuration
        XMLSecurityProperties properties = new XMLSecurityProperties();
        List<XMLSecurityConstants.Action> actions = new ArrayList<>();
        actions.add(XMLSecurityConstants.SIGNATURE);
        properties.setActions(actions);

        // Set the key up
        KeyStore keyStore = getKeyStore();
        Key key = keyStore.getKey("transmitter", "default".toCharArray());
        properties.setSignatureKey(key);
        X509Certificate cert = (X509Certificate) keyStore.getCertificate("transmitter");
        properties.setSignatureCerts(new X509Certificate[]{cert});

        SecurePart securePart =
                new SecurePart(new QName("urn:example:po", "PaymentInfo"), SecurePart.Modifier.Element);
        properties.addSignaturePart(securePart);

        File file = resolveFile("src/test/resources/ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        securePart = new SecurePart(file.toURI().toString(),
                new String[]{"http://www.w3.org/TR/2001/REC-xml-c14n-20010315"},
                XMLSecurityConstants.NS_XMLDSIG_SHA1);
        properties.addSignaturePart(securePart);

        OutboundXMLSec outboundXMLSec = XMLSec.getOutboundXMLSec(properties);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = outboundXMLSec.processOutMessage(baos, StandardCharsets.UTF_8.name());

        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(sourceDocument);

        XmlReaderToWriter.writeAll(xmlStreamReader, xmlStreamWriter);
        xmlStreamWriter.close();

        Document document = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            document = XMLUtils.read(is, false);
        }

        // Verify using DOM
        verifyUsingDOM(document, cert, properties.getSignatureSecureParts(), false);
    }

    @Test
    public void testSignatureCreationWithExternalFilesystemBinaryReference() throws Exception {
        // Set up the Configuration
        XMLSecurityProperties properties = new XMLSecurityProperties();
        List<XMLSecurityConstants.Action> actions = new ArrayList<>();
        actions.add(XMLSecurityConstants.SIGNATURE);
        properties.setActions(actions);

        // Set the key up
        KeyStore keyStore = getKeyStore();
        Key key = keyStore.getKey("transmitter", "default".toCharArray());
        properties.setSignatureKey(key);
        X509Certificate cert = (X509Certificate) keyStore.getCertificate("transmitter");
        properties.setSignatureCerts(new X509Certificate[]{cert});

        SecurePart securePart =
                new SecurePart(new QName("urn:example:po", "PaymentInfo"), SecurePart.Modifier.Element);
        properties.addSignaturePart(securePart);

        File file = resolveFile(
            "target/test-classes/com/kjhxtc/internal/apache./xml/security/test/stax/signature/SignatureVerificationReferenceURIResolverTest.class");
        securePart = new SecurePart(file.toURI().toString(),
                null,
                XMLSecurityConstants.NS_XMLDSIG_SHA1);
        properties.addSignaturePart(securePart);

        OutboundXMLSec outboundXMLSec = XMLSec.getOutboundXMLSec(properties);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = outboundXMLSec.processOutMessage(baos, StandardCharsets.UTF_8.name());

        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(sourceDocument);

        XmlReaderToWriter.writeAll(xmlStreamReader, xmlStreamWriter);
        xmlStreamWriter.close();

        Document document = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            document = XMLUtils.read(is, false);
        }

        // Verify using DOM
        verifyUsingDOM(document, cert, properties.getSignatureSecureParts(), false);
    }

    @Test
    public void testSignatureCreationWithExternalHttpReference() throws Exception {

        Proxy proxy = HttpRequestRedirectorProxy.startHttpEngine();

        try {
            ResolverHttp.setProxy(proxy);

            Map<String, String> resolverProperties = new HashMap<>();
            resolverProperties.put("http.proxy.host", ((InetSocketAddress)proxy.address()).getAddress().getHostAddress());
            resolverProperties.put("http.proxy.port", "" + ((InetSocketAddress)proxy.address()).getPort());
            ResolverDirectHTTP resolverDirectHTTP = new ResolverDirectHTTP(resolverProperties);

            // Set up the Configuration
            XMLSecurityProperties properties = new XMLSecurityProperties();
            List<XMLSecurityConstants.Action> actions = new ArrayList<>();
            actions.add(XMLSecurityConstants.SIGNATURE);
            properties.setActions(actions);

            // Set the key up
            KeyStore keyStore = getKeyStore();
            Key key = keyStore.getKey("transmitter", "default".toCharArray());
            properties.setSignatureKey(key);
            X509Certificate cert = (X509Certificate) keyStore.getCertificate("transmitter");
            properties.setSignatureCerts(new X509Certificate[]{cert});

            SecurePart securePart =
                    new SecurePart(new QName("urn:example:po", "PaymentInfo"), SecurePart.Modifier.Element);
            properties.addSignaturePart(securePart);

            securePart = new SecurePart("http://www.w3.org/Signature/2002/04/xml-stylesheet.b64", null, XMLSecurityConstants.NS_XMLDSIG_SHA1);
            properties.addSignaturePart(securePart);

            OutboundXMLSec outboundXMLSec = XMLSec.getOutboundXMLSec(properties);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = outboundXMLSec.processOutMessage(baos, StandardCharsets.UTF_8.name());

            InputStream sourceDocument =
                    this.getClass().getClassLoader().getResourceAsStream(
                            "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(sourceDocument);

            XmlReaderToWriter.writeAll(xmlStreamReader, xmlStreamWriter);
            xmlStreamWriter.close();

            Document document = null;
            try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
                document = XMLUtils.read(is, false);
            }

            // Verify using DOM
            verifyUsingDOM(document, cert, properties.getSignatureSecureParts(), resolverDirectHTTP);
        } finally {
            HttpRequestRedirectorProxy.stopHttpEngine();
        }
    }

    @Test
    public void testSignatureCreationWithSameDocumentXPointerIdApostropheReference() throws Exception {
        // Set up the Configuration
        XMLSecurityProperties properties = new XMLSecurityProperties();
        List<XMLSecurityConstants.Action> actions = new ArrayList<>();
        actions.add(XMLSecurityConstants.SIGNATURE);
        properties.setActions(actions);

        // Set the key up
        KeyStore keyStore = getKeyStore();
        Key key = keyStore.getKey("transmitter", "default".toCharArray());
        properties.setSignatureKey(key);
        X509Certificate cert = (X509Certificate) keyStore.getCertificate("transmitter");
        properties.setSignatureCerts(new X509Certificate[]{cert});

        SecurePart securePart =
                new SecurePart(new QName("urn:example:po", "PaymentInfo"), true, SecurePart.Modifier.Element);
        properties.addSignaturePart(securePart);

        OutboundXMLSec outboundXMLSec = XMLSec.getOutboundXMLSec(properties);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = outboundXMLSec.processOutMessage(baos, StandardCharsets.UTF_8.name());

        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ie/baltimore/merlin-examples/merlin-xmlenc-five/plaintext.xml");
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(sourceDocument);

        XmlReaderToWriter.writeAll(xmlStreamReader, xmlStreamWriter);
        xmlStreamWriter.close();

        Document document = null;
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            document = XMLUtils.read(is, false);
        }

        NodeList nodeList = document.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Reference");
        assertEquals(1, nodeList.getLength());

        String uri = ((Element) nodeList.item(0)).getAttribute("URI");
        assertNotNull(uri);
        assertTrue(uri.startsWith("#xpointer"));

        // Verify using DOM
        verifyUsingDOM(document, cert, properties.getSignatureSecureParts());
    }


    private KeyStore getKeyStore() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("jks");
        try (InputStream inputStream = getClass().getClassLoader().getResource("transmitter.jks").openStream()) {
            keyStore.load(inputStream, "default".toCharArray());
        }
        return keyStore;
    }
}