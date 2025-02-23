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
package com.kjhxtc.internal.apache.xml.security.test.dom.algorithms;

import com.kjhxtc.internal.apache.xml.security.algorithms.JCEMapper;
import com.kjhxtc.internal.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JCEMapperTest {

    static {
        com.kjhxtc.internal.apache.xml.security.Init.init();
    }

    @Test
    public void testSHA1() throws Exception {
        assertEquals("MessageDigest", JCEMapper.getAlgorithmClassFromURI(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1));
        assertEquals("SHA-1", JCEMapper.translateURItoJCEID(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1));
    }


}