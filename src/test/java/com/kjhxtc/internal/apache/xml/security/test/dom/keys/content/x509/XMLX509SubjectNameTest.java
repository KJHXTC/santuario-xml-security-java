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
package com.kjhxtc.internal.apache.xml.security.test.dom.keys.content.x509;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.kjhxtc.internal.apache.xml.security.keys.content.x509.XMLX509SubjectName;
import com.kjhxtc.internal.apache.xml.security.test.dom.TestUtils;
import org.junit.jupiter.api.Test;

import static com.kjhxtc.internal.apache.xml.security.test.XmlSecTestEnvironment.resolveFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Certificate parsing test.
 *
 */
public class XMLX509SubjectNameTest {

    @Test
    public void testEqualsAndHashCode() throws Exception {
        File f = resolveFile("src/test/resources/ie/baltimore/merlin-examples/merlin-xmldsig-twenty-three/certs/lugh.crt");
        X509Certificate cert;
        try (FileInputStream fis = new FileInputStream(f)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(fis);
        }

        XMLX509SubjectName x509SubjectName1 = new XMLX509SubjectName(TestUtils.newDocument(), cert);
        assertNotNull(x509SubjectName1.getSubjectName());
        XMLX509SubjectName x509SubjectName2 = new XMLX509SubjectName(TestUtils.newDocument(), cert);

        assertEquals(x509SubjectName1, x509SubjectName2);
        assertEquals(x509SubjectName1.hashCode(), x509SubjectName2.hashCode());
    }


}
