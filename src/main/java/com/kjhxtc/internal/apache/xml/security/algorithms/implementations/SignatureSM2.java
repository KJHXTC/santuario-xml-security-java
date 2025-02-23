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
package com.kjhxtc.internal.apache.xml.security.algorithms.implementations;

import com.kjhxtc.internal.apache.xml.security.algorithms.JCEMapper;
import com.kjhxtc.internal.apache.xml.security.algorithms.SignatureAlgorithmSpi;
import com.kjhxtc.internal.apache.xml.security.signature.XMLSignature;
import com.kjhxtc.internal.apache.xml.security.signature.XMLSignatureException;
import com.kjhxtc.internal.apache.xml.security.utils.XMLUtils;

import java.io.IOException;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.AlgorithmParameterSpec;

/**
 *
 */
public abstract class SignatureSM2 extends SignatureAlgorithmSpi {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(SignatureSM2.class);

    private final Signature signatureAlgorithm;

    /** Length for each integer in signature */
    private int signIntLen = -1;

    /**
     * Converts an ASN.1 ECDSA value to a XML Signature ECDSA Value.
     *
     * The JAVA JCE ECDSA Signature algorithm creates ASN.1 encoded (r, s) value
     * pairs; the XML Signature requires the core BigInteger values.
     *
     * @param asn1Bytes
     * @param rawLen
     * @return the decode bytes
     *
     * @throws IOException
     * @see <A HREF="http://www.w3.org/TR/xmldsig-core/#dsa-sha1">6.4.1 DSA</A>
     * @see <A HREF="ftp://ftp.rfc-editor.org/in-notes/rfc4050.txt">3.3. ECDSA Signatures</A>
     */
    public static byte[] convertASN1toXMLDSIG(byte[] asn1Bytes, int rawLen) throws IOException {
        return ECDSAUtils.convertASN1toXMLDSIG(asn1Bytes, rawLen);
    }

    /**
     * Converts a XML Signature SM2 like ECDSA Value to an ASN.1 DSA value.
     *
     * The JAVA JCE ECDSA Signature algorithm creates ASN.1 encoded (r, s) value
     * pairs; the XML Signature requires the core BigInteger values.
     *
     * @param xmldsigBytes
     * @return the encoded ASN.1 bytes
     *
     * @throws IOException
     * @see <A HREF="http://www.w3.org/TR/xmldsig-core/#dsa-sha1">6.4.1 DSA</A>
     * @see <A HREF="ftp://ftp.rfc-editor.org/in-notes/rfc4050.txt">3.3. ECDSA Signatures</A>
     */
    public static byte[] convertXMLDSIGtoASN1(byte[] xmldsigBytes) throws IOException {
        return ECDSAUtils.convertXMLDSIGtoASN1(xmldsigBytes);
    }

    /**
     * Constructor SignatureRSA
     *
     * @throws XMLSignatureException
     */
    public SignatureSM2() throws XMLSignatureException {
        this(null);
    }

    public SignatureSM2(Provider provider) throws XMLSignatureException {
        String algorithmID = JCEMapper.translateURItoJCEID(this.engineGetURI());
        LOG.debug("Created Signature SM2 using {}", algorithmID);

        try {
            if (provider == null) {
                String providerId = JCEMapper.getProviderId();
                if (providerId == null) {
                    this.signatureAlgorithm = Signature.getInstance(algorithmID);

                } else {
                    this.signatureAlgorithm = Signature.getInstance(algorithmID, providerId);
                }

            } else {
                this.signatureAlgorithm = Signature.getInstance(algorithmID, provider);
            }

        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            Object[] exArgs = { algorithmID, ex.getLocalizedMessage() };
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        }
    }

    /** {@inheritDoc} */
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws XMLSignatureException {
        try {
            this.signatureAlgorithm.setParameter(params);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new XMLSignatureException(ex);
        }
    }

    /** {@inheritDoc} */
    protected boolean engineVerify(byte[] signature) throws XMLSignatureException {
        try {
            byte[] jcebytes = SignatureSM2.convertXMLDSIGtoASN1(signature);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Called EC(SM2).verify() on " + XMLUtils.encodeToString(signature));
            }

            return this.signatureAlgorithm.verify(jcebytes);
        } catch (SignatureException | IOException ex) {
            throw new XMLSignatureException(ex);
        }
    }

    /** {@inheritDoc} */
    protected void engineInitVerify(Key publicKey) throws XMLSignatureException {
        engineInitVerify(publicKey, signatureAlgorithm);
    }

    /** {@inheritDoc} */
    protected byte[] engineSign() throws XMLSignatureException {
        try {
            byte[] jcebytes = this.signatureAlgorithm.sign();

            return SignatureSM2.convertASN1toXMLDSIG(jcebytes, signIntLen);
        } catch (SignatureException | IOException ex) {
            throw new XMLSignatureException(ex);
        }
    }

    /** {@inheritDoc} */
    protected void engineInitSign(Key privateKey, SecureRandom secureRandom)
        throws XMLSignatureException {
        if (privateKey instanceof ECPrivateKey) {
            ECPrivateKey ecKey = (ECPrivateKey) privateKey;
            signIntLen = (ecKey.getParams().getCurve().getField().getFieldSize() + 7) / 8;
           // If not ECPrivateKey, signIntLen remains -1
        }
        engineInitSign(privateKey, secureRandom, this.signatureAlgorithm);
    }

    /** {@inheritDoc} */
    protected void engineInitSign(Key privateKey) throws XMLSignatureException {
        engineInitSign(privateKey, (SecureRandom)null);
    }

    /** {@inheritDoc} */
    protected void engineUpdate(byte[] input) throws XMLSignatureException {
        try {
            this.signatureAlgorithm.update(input);
        } catch (SignatureException ex) {
            throw new XMLSignatureException(ex);
        }
    }

    /** {@inheritDoc} */
    protected void engineUpdate(byte input) throws XMLSignatureException {
        try {
            this.signatureAlgorithm.update(input);
        } catch (SignatureException ex) {
            throw new XMLSignatureException(ex);
        }
    }

    /** {@inheritDoc} */
    protected void engineUpdate(byte[] buf, int offset, int len) throws XMLSignatureException {
        try {
            this.signatureAlgorithm.update(buf, offset, len);
        } catch (SignatureException ex) {
            throw new XMLSignatureException(ex);
        }
    }

    /** {@inheritDoc} */
    protected String engineGetJCEAlgorithmString() {
        return this.signatureAlgorithm.getAlgorithm();
    }

    /** {@inheritDoc} */
    protected String engineGetJCEProviderName() {
        return this.signatureAlgorithm.getProvider().getName();
    }

    /** {@inheritDoc} */
    protected void engineSetHMACOutputLength(int HMACOutputLength)
        throws XMLSignatureException {
        throw new XMLSignatureException("algorithms.HMACOutputLengthOnlyForHMAC");
    }

    /** {@inheritDoc} */
    protected void engineInitSign(
        Key signingKey, AlgorithmParameterSpec algorithmParameterSpec
    ) throws XMLSignatureException {
        throw new XMLSignatureException("algorithms.CannotUseAlgorithmParameterSpecOnRSA");
    }

    /**
     * Class SignatureECDSASHA1
     *
     */
    public static class SignatureSM2SM3 extends SignatureSM2 {
        /**
         * Constructor SignatureECDSASHA1
         *
         * @throws XMLSignatureException
         */
        public SignatureSM2SM3() throws XMLSignatureException {
            super();
        }

        public SignatureSM2SM3(Provider provider) throws XMLSignatureException {
            super(provider);
        }

        /** {@inheritDoc} */
        @Override
        public String engineGetURI() {
            return XMLSignature.ALGO_ID_SIGNATURE_SM2_SM3;
        }
    }


}
