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
package com.kjhxtc.internal.apache.xml.security.stax.impl.transformer.canonicalizer;

/**
 */
public class Canonicalizer11_WithCommentsTransformer extends Canonicalizer11 {

    /**
     * Canonicalizer not complete. We are missing special handling for xml:base. But since
     * we don't support document subsets we don't need it!
     */
    public Canonicalizer11_WithCommentsTransformer() {
        super(true);
    }
}
