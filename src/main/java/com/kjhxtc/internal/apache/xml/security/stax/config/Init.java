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

import java.net.URI;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.kjhxtc.internal.apache.xml.security.configuration.ConfigurationType;
import com.kjhxtc.internal.apache.xml.security.configuration.ObjectFactory;
import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.stax.ext.XMLSecurityConfigurationException;
import com.kjhxtc.internal.apache.xml.security.utils.ClassLoaderUtils;
import com.kjhxtc.internal.apache.xml.security.utils.I18n;

/**
 * Class to load the algorithms-mappings from a configuration file.
 * After the initialization the mapping is available through the JCEAlgorithmMapper
 *
 */
public class Init {

    private static URI initialized;

    @SuppressWarnings("unchecked")
    public static synchronized void init(URI uri, Class<?> callingClass) throws XMLSecurityException {
        if (initialized == null || uri != null && !uri.equals(initialized)) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
                final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
                Schema schema = schemaFactory.newSchema(
                        ClassLoaderUtils.getResource("schemas/security-config.xsd", Init.class));
                unmarshaller.setSchema(schema);
                final UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
                saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                saxParserFactory.setXIncludeAware(false);
                saxParserFactory.setNamespaceAware(true);
                SAXParser saxParser = saxParserFactory.newSAXParser();
                if (uri == null) {
                    URL resource = ClassLoaderUtils.getResource("security-config.xml", Init.class);
                    if (resource == null) {
                        //kind of chicken-egg problem here
                        I18n.init("en", "US");
                        throw new XMLSecurityConfigurationException("empty", "security-config.xml not found in classpath");
                    }
                    uri = resource.toURI();
                }
                saxParser.parse(uri.toURL().toExternalForm(), new XIncludeHandler(unmarshallerHandler));
                JAXBElement<ConfigurationType> configurationTypeJAXBElement = (JAXBElement<ConfigurationType>) unmarshallerHandler.getResult();

                ConfigurationProperties.init(configurationTypeJAXBElement.getValue().getProperties(), callingClass);
                SecurityHeaderHandlerMapper.init(configurationTypeJAXBElement.getValue().getSecurityHeaderHandlers(), callingClass);
                JCEAlgorithmMapper.init(configurationTypeJAXBElement.getValue().getJCEAlgorithmMappings());
                TransformerAlgorithmMapper.init(configurationTypeJAXBElement.getValue().getTransformAlgorithms(), callingClass);
                ResourceResolverMapper.init(configurationTypeJAXBElement.getValue().getResourceResolvers(), callingClass);

                I18n.init(ConfigurationProperties.getProperty("DefaultLanguageCode"), ConfigurationProperties.getProperty("DefaultCountryCode"));

            } catch (Exception e) {
                //kind of chicken-egg problem here
                I18n.init("en", "US");
                throw new XMLSecurityConfigurationException(e);
            }
            initialized = uri;
        }
    }
}
