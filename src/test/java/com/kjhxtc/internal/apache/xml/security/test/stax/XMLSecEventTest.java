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
package com.kjhxtc.internal.apache.xml.security.test.stax;

import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecAttribute;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecCharacters;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecComment;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecEntityDeclaration;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecEntityReference;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecEvent;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecEventFactory;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecNamespace;
import com.kjhxtc.internal.apache.xml.security.stax.ext.stax.XMLSecProcessingInstruction;
import com.kjhxtc.internal.apache.xml.security.stax.impl.XMLSecurityEventReader;
import com.kjhxtc.internal.apache.xml.security.stax.impl.stax.XMLSecAttributeImpl;
import com.kjhxtc.internal.apache.xml.security.stax.impl.stax.XMLSecCharactersImpl;
import com.kjhxtc.internal.apache.xml.security.stax.impl.stax.XMLSecCommentImpl;
import com.kjhxtc.internal.apache.xml.security.stax.impl.stax.XMLSecEntityDeclarationImpl;
import com.kjhxtc.internal.apache.xml.security.stax.impl.stax.XMLSecEntityReferenceImpl;
import com.kjhxtc.internal.apache.xml.security.stax.impl.stax.XMLSecNamespaceImpl;
import com.kjhxtc.internal.apache.xml.security.stax.impl.stax.XMLSecProcessingInstructionImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
public class XMLSecEventTest {

    @Test
    public void testWriteCharactersEncoded() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLSecCharacters xmlSecCharacters = new XMLSecCharactersImpl("test", false, false, false, null);
        xmlSecCharacters.writeAsEncodedUnicode(stringWriter);
        assertEquals("test", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecCharacters = new XMLSecCharactersImpl("<", false, false, false, null);
        xmlSecCharacters.writeAsEncodedUnicode(stringWriter);
        assertEquals("&lt;", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecCharacters = new XMLSecCharactersImpl(">", false, false, false, null);
        xmlSecCharacters.writeAsEncodedUnicode(stringWriter);
        assertEquals("&gt;", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecCharacters = new XMLSecCharactersImpl("&", false, false, false, null);
        xmlSecCharacters.writeAsEncodedUnicode(stringWriter);
        assertEquals("&amp;", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecCharacters = new XMLSecCharactersImpl("<&>", false, false, false, null);
        xmlSecCharacters.writeAsEncodedUnicode(stringWriter);
        assertEquals("&lt;&amp;&gt;", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecCharacters = new XMLSecCharactersImpl(" < & > ", false, false, false, null);
        xmlSecCharacters.writeAsEncodedUnicode(stringWriter);
        assertEquals(" &lt; &amp; &gt; ", stringWriter.toString());
    }

    @Test
    public void testWriteAttributeEncoded() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLSecAttribute xmlSecAttribute = new XMLSecAttributeImpl(new QName("test", "test", "test"), "test");
        xmlSecAttribute.writeAsEncodedUnicode(stringWriter);
        assertEquals("test:test=\"test\"", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecAttribute = new XMLSecAttributeImpl(new QName("test"), "\"");
        xmlSecAttribute.writeAsEncodedUnicode(stringWriter);
        assertEquals("test=\"&quot;\"", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecAttribute = new XMLSecAttributeImpl(new QName("test"), "&");
        xmlSecAttribute.writeAsEncodedUnicode(stringWriter);
        assertEquals("test=\"&amp;\"", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecAttribute = new XMLSecAttributeImpl(new QName("test"), " & \" > < ");
        xmlSecAttribute.writeAsEncodedUnicode(stringWriter);
        assertEquals("test=\" &amp; &quot; > < \"", stringWriter.toString());
    }

    @Test
    public void testWriteComment() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLSecComment xmlSecAttribute = new XMLSecCommentImpl(" < > & \" '", null);
        xmlSecAttribute.writeAsEncodedUnicode(stringWriter);
        assertEquals("<!-- < > & \" '-->", stringWriter.toString());
    }

    @Test
    public void testWriteEntityDeclaration() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLSecEntityDeclaration xmlSecEntityDeclaration = new XMLSecEntityDeclarationImpl("test");
        xmlSecEntityDeclaration.writeAsEncodedUnicode(stringWriter);
        assertEquals("<!ENTITY test \"\">", stringWriter.toString());
    }

    @Test
    public void testWriteEntityReference() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLSecEntityReference xmlSecEntityReference = new XMLSecEntityReferenceImpl("test", null, null);
        xmlSecEntityReference.writeAsEncodedUnicode(stringWriter);
        assertEquals("&test;", stringWriter.toString());
    }

    @Test
    public void testWriteNamespaceEncoded() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLSecNamespace xmlSecNamespace = XMLSecNamespaceImpl.getInstance("test", "test");
        xmlSecNamespace.writeAsEncodedUnicode(stringWriter);
        assertEquals("xmlns:test=\"test\"", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecNamespace = XMLSecNamespaceImpl.getInstance("", "\"");
        xmlSecNamespace.writeAsEncodedUnicode(stringWriter);
        assertEquals("xmlns=\"&quot;\"", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecNamespace = XMLSecNamespaceImpl.getInstance("test", "&");
        xmlSecNamespace.writeAsEncodedUnicode(stringWriter);
        assertEquals("xmlns:test=\"&amp;\"", stringWriter.toString());

        stringWriter = new StringWriter();
        xmlSecNamespace = XMLSecNamespaceImpl.getInstance("test", " & \" > < ");
        xmlSecNamespace.writeAsEncodedUnicode(stringWriter);
        assertEquals("xmlns:test=\" &amp; &quot; > < \"", stringWriter.toString());
    }

    @Test
    public void testWriteProcessingInstruction() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLSecProcessingInstruction xmlSecProcessingInstruction =
                new XMLSecProcessingInstructionImpl("test", "test", null);
        xmlSecProcessingInstruction.writeAsEncodedUnicode(stringWriter);
        assertEquals("<?test test?>", stringWriter.toString());
    }

    @Test
    public void testwWiteAsEncodedUnicode() throws Exception {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader =
                xmlInputFactory.createXMLStreamReader(this.getClass().getClassLoader().getResourceAsStream(
                        "com/kjhxtc/internal/apache./xml/security/c14n/inExcl/plain-soap-1.1.xml"));

        Deque<XMLSecEvent> xmlSecEventDeque = new ArrayDeque<>();
        do {
            xmlSecEventDeque.push(XMLSecEventFactory.allocate(xmlStreamReader, null));
            xmlStreamReader.next();
        }
        while (xmlStreamReader.hasNext());
        xmlSecEventDeque.push(XMLSecEventFactory.allocate(xmlStreamReader, null));//EndDocumentEvent

        XMLSecurityEventReader xmlSecurityEventReader = new XMLSecurityEventReader(xmlSecEventDeque, 0);

        XMLEventReader xmlEventReader =
                xmlInputFactory.createXMLEventReader(this.getClass().getClassLoader().getResourceAsStream(
                        "com/kjhxtc/internal/apache./xml/security/c14n/inExcl/plain-soap-1.1.xml"));

        final StringWriter stdWriter = new StringWriter();
        final StringWriter secWriter = new StringWriter();
        while (xmlEventReader.hasNext()) {
            XMLEvent stdXmlEvent = xmlEventReader.nextEvent();
            XMLEvent secXmlEvent = xmlSecurityEventReader.nextEvent();

            stdXmlEvent.writeAsEncodedUnicode(stdWriter);
            secXmlEvent.writeAsEncodedUnicode(secWriter);
        }

        assertEquals(secWriter.toString(), stdWriter.toString());
    }
}