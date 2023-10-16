/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.kjhxtc.internal.apache.xml.security.keys.keyresolver.implementations;

import com.kjhxtc.internal.apache.xml.security.exceptions.XMLSecurityException;
import com.kjhxtc.internal.apache.xml.security.keys.content.keyvalues.SM2KeyValue;
import com.kjhxtc.internal.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import com.kjhxtc.internal.apache.xml.security.keys.storage.StorageResolver;
import com.kjhxtc.internal.apache.xml.security.utils.Constants;
import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class SM2KeyValueResolver extends KeyResolverSpi {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(SM2KeyValueResolver.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean engineCanResolve(Element element, String baseURI, StorageResolver storage) {
        return XMLUtils.elementIsInSignatureSpace(element, Constants._TAG_KEYVALUE)
                || XMLUtils.elementIsInSignatureSpace(element, Constants._TAG_SM2KEYVALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PublicKey engineResolvePublicKey(
            Element element, String baseURI, StorageResolver storage, boolean secureValidation
    ) {
        if (element == null) {
            return null;
        }
        Element ecKeyElement = null;
        boolean isKeyValue =
                XMLUtils.elementIsInSignatureSpace(element, Constants._TAG_KEYVALUE);
        if (isKeyValue) {
            ecKeyElement =
                    XMLUtils.selectDs11Node(element.getFirstChild(), Constants._TAG_SM2KEYVALUE, 0);
        } else if (XMLUtils.elementIsInSignature11Space(element, Constants._TAG_SM2KEYVALUE)) {
            // this trick is needed to allow the RetrievalMethodResolver to eat a
            // ds:ECKeyValue directly (without KeyValue)
            ecKeyElement = element;
        }

        if (ecKeyElement == null) {
            return null;
        }

        try {
            SM2KeyValue ecKeyValue = new SM2KeyValue(ecKeyElement, baseURI);
            return ecKeyValue.getPublicKey();
        } catch (XMLSecurityException ex) {
            LOG.debug(ex.getMessage(), ex);
            //do nothing
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected X509Certificate engineResolveX509Certificate(
            Element element, String baseURI, StorageResolver storage, boolean secureValidation
    ) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected javax.crypto.SecretKey engineResolveSecretKey(
            Element element, String baseURI, StorageResolver storage, boolean secureValidation
    ) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PrivateKey engineResolvePrivateKey(
            Element element, String baseURI, StorageResolver storage, boolean secureValidation
    ) {
        return null;
    }
}
