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
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 */
package com.kjhxtc.internal.apache.jcp.xml.dsig.internal.dom;

import java.math.BigInteger;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;

import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * A DOM-based representation of the XML <code>CryptoBinary</code> simple type
 * as defined in the W3C specification for XML-Signature Syntax and Processing.
 * The XML Schema Definition is defined as:
 *
 * <xmp>
 * <simpleType name="CryptoBinary">
 *   <restriction base = "base64Binary">
 *   </restriction>
 * </simpleType>
 * </xmp>
 *
 * @author Sean Mullan
 */
public final class DOMCryptoBinary extends DOMStructure {

    private final BigInteger bigNum;
    private final String value;

    /**
     * Create a <code>DOMCryptoBinary</code> instance from the specified
     * <code>BigInteger</code>
     *
     * @param bigNum the arbitrary-length integer
     * @throws NullPointerException if <code>bigNum</code> is <code>null</code>
     */
    public DOMCryptoBinary(BigInteger bigNum) {
        if (bigNum == null) {
            throw new NullPointerException("bigNum is null");
        }
        this.bigNum = bigNum;
        // convert to bitstring
        byte[] bytes = XMLUtils.getBytes(bigNum, bigNum.bitLength());
        value = XMLUtils.encodeToString(bytes);
    }

    /**
     * Creates a <code>DOMCryptoBinary</code> from a node.
     *
     * @param cbNode a CryptoBinary text node
     * @throws MarshalException if value cannot be decoded (invalid format)
     */
    public DOMCryptoBinary(Node cbNode) throws MarshalException {
        value = cbNode.getNodeValue();
        try {
            bigNum = new BigInteger(1, XMLUtils.decode(((Text) cbNode).getData()));
        } catch (Exception ex) {
            throw new MarshalException(ex);
        }
    }

    /**
     * Returns the <code>BigInteger</code> that this object contains.
     *
     * @return the <code>BigInteger</code> that this object contains
     */
    public BigInteger getBigNum() {
        return bigNum;
    }

    @Override
    public void marshal(Node parent, String prefix, DOMCryptoContext context)
        throws MarshalException {
        parent.appendChild
            (DOMUtils.getOwnerDocument(parent).createTextNode(value));
    }
}
