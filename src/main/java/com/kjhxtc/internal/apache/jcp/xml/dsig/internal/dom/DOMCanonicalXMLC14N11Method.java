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
/*
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.kjhxtc.internal.apache.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;

import javax.xml.crypto.Data;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import com.kjhxtc.internal.apache.xml.security.c14n.Canonicalizer;
import com.kjhxtc.internal.apache.xml.security.c14n.InvalidCanonicalizerException;

/**
 * DOM-based implementation of CanonicalizationMethod for Canonical XML 1.1
 * (with or without comments). Uses Apache XML-Sec Canonicalizer.
 *
 */
public final class DOMCanonicalXMLC14N11Method extends ApacheCanonicalizer {

    public static final String C14N_11 = "http://www.w3.org/2006/12/xml-c14n11";
    public static final String C14N_11_WITH_COMMENTS
        = "http://www.w3.org/2006/12/xml-c14n11#WithComments";

    @Override
    public void init(TransformParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("no parameters " +
                "should be specified for Canonical XML 1.1 algorithm");
        }
    }

    @Override
    public Data transform(Data data, XMLCryptoContext xc)
        throws TransformException {

        // ignore comments if dereferencing same-document URI that requires
        // you to omit comments, even if the Transform says otherwise -
        // this is to be compliant with section 4.3.3.3 of W3C Rec.
        if (data instanceof DOMSubTreeData) {
            DOMSubTreeData subTree = (DOMSubTreeData) data;
            if (subTree.excludeComments()) {
                try {
                    canonicalizer = Canonicalizer.getInstance(C14N_11);
                } catch (InvalidCanonicalizerException ice) {
                    throw new TransformException
                        ("Couldn't find Canonicalizer for: " +
                         C14N_11 + ": " + ice.getMessage(), ice);
                }
            }
        }

        return canonicalize(data, xc);
    }
}
