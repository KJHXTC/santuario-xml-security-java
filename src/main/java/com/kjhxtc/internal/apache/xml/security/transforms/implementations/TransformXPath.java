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
package com.kjhxtc.internal.apache.xml.security.transforms.implementations;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;

import com.kjhxtc.internal.apache.xml.security.parser.XMLParserException;
import com.kjhxtc.internal.apache.xml.security.signature.NodeFilter;
import com.kjhxtc.internal.apache.xml.security.signature.XMLSignatureInput;
import com.kjhxtc.internal.apache.xml.security.transforms.TransformSpi;
import com.kjhxtc.internal.apache.xml.security.transforms.TransformationException;
import com.kjhxtc.internal.apache.xml.security.transforms.Transforms;
import com.kjhxtc.internal.apache.xml.security.utils.Constants;
import com.kjhxtc.internal.apache.xml.security.utils.JDKXPathFactory;
import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;
import com.kjhxtc.internal.apache.xml.security.utils.XPathAPI;
import com.kjhxtc.internal.apache.xml.security.utils.XPathFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class TransformXPath
 *
 * Implements the <CODE>http://www.w3.org/TR/1999/REC-xpath-19991116</CODE>
 * transform.
 *
 * @see <a href="http://www.w3.org/TR/1999/REC-xpath-19991116">XPath</a>
 *
 */
public class TransformXPath extends TransformSpi {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String engineGetURI() {
        return Transforms.TRANSFORM_XPATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected XMLSignatureInput enginePerformTransform(
        XMLSignatureInput input, OutputStream os, Element transformElement,
        String baseURI, boolean secureValidation
    ) throws TransformationException {
        try {
            /**
             * If the actual input is an octet stream, then the application MUST
             * convert the octet stream to an XPath node-set suitable for use by
             * Canonical XML with Comments. (A subsequent application of the
             * REQUIRED Canonical XML algorithm would strip away these comments.)
             *
             * ...
             *
             * The evaluation of this expression includes all of the document's nodes
             * (including comments) in the node-set representing the octet stream.
             */
            Element xpathElement =
                XMLUtils.selectDsNode(
                    transformElement.getFirstChild(), Constants._TAG_XPATH, 0);

            if (xpathElement == null) {
                Object[] exArgs = { "ds:XPath", "Transform" };

                throw new TransformationException("xml.WrongContent", exArgs);
            }
            Node xpathnode = xpathElement.getFirstChild();
            if (xpathnode == null) {
                throw new DOMException(
                    DOMException.HIERARCHY_REQUEST_ERR, "Text must be in ds:Xpath"
                );
            }
            String str = XMLUtils.getStrFromNode(xpathnode);
            input.setNeedsToBeExpanded(needsCircumvent(str));

            XPathFactory xpathFactory = getXPathFactory();
            XPathAPI xpathAPIInstance = xpathFactory.newXPathAPI();
            input.addNodeFilter(new XPathNodeFilter(xpathElement, xpathnode, str, xpathAPIInstance));
            input.setNodeSet(true);
            return input;
        } catch (XMLParserException | IOException | DOMException ex) {
            throw new TransformationException(ex);
        }
    }

    protected XPathFactory getXPathFactory() {
        return new JDKXPathFactory();
    }

    /**
     * @param str
     * @return true if needs to be circumvent for bug.
     */
    private boolean needsCircumvent(String str) {
        return str.indexOf("namespace") != -1 || str.indexOf("name()") != -1;
    }

    private static class XPathNodeFilter implements NodeFilter {

        private final XPathAPI xPathAPI;
        private final Node xpathnode;
        private final Element xpathElement;
        private final String str;

        XPathNodeFilter(Element xpathElement, Node xpathnode, String str, XPathAPI xPathAPI) {
            this.xpathnode = xpathnode;
            this.str = str;
            this.xpathElement = xpathElement;
            this.xPathAPI = xPathAPI;
        }

        /**
         * @see com.kjhxtc.internal.apache.xml.security.signature.NodeFilter#isNodeInclude(org.w3c.dom.Node)
         */
        @Override
        public int isNodeInclude(Node currentNode) throws TransformationException {
            try {
                boolean include = xPathAPI.evaluate(currentNode, xpathnode, str, xpathElement);
                if (include) {
                    return 1;
                }
                return 0;
            } catch (TransformerException ex) {
                throw new TransformationException(ex);
            }
        }

        @Override
        public int isNodeIncludeDO(Node n, int level) throws TransformationException {
            return isNodeInclude(n);
        }

    }
}
