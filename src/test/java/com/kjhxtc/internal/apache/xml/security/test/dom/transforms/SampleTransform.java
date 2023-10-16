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
package com.kjhxtc.internal.apache.xml.security.test.dom.transforms;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import com.kjhxtc.internal.apache.xml.security.c14n.CanonicalizationException;
import com.kjhxtc.internal.apache.xml.security.c14n.InvalidCanonicalizerException;
import com.kjhxtc.internal.apache.xml.security.exceptions.AlgorithmAlreadyRegisteredException;
import com.kjhxtc.internal.apache.xml.security.signature.XMLSignatureInput;
import com.kjhxtc.internal.apache.xml.security.transforms.Transform;
import com.kjhxtc.internal.apache.xml.security.transforms.TransformSpi;
import com.kjhxtc.internal.apache.xml.security.transforms.TransformationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class SampleTransform extends TransformSpi {

    public static final String uri =
        "http://com.kjhxtc.internal.apache.xml.security.test.transforms.SampleTransform";

    public SampleTransform() throws Exception {
        try {
            Transform.register
               (uri, "com.kjhxtc.internal.apache.xml.security.test.transforms.SampleTransform");
        } catch (AlgorithmAlreadyRegisteredException e) { }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String engineGetURI() {
        return uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, OutputStream os,
                                                    Element transformElement, String baseURI,
                                                    boolean secureValidation)
        throws IOException, CanonicalizationException, InvalidCanonicalizerException,
        TransformationException, ParserConfigurationException, SAXException {
        throw new UnsupportedOperationException();
    }
}
