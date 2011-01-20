/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.shared.asn1;


/**
 * A decoder which decodes encoded data as it arrives in pieces while
 * maintaining the state of the decode operation between the arrival of encoded
 * chunks. As chunks of encoded data arrive the decoder processes each chunk of
 * encoded data and maintains decoding state in between arrivals: it is hence
 * stateful and should be associated with a single channel or encoded data
 * producer. When an arbitrary unit of encoding, to be determined by the
 * encoding scheme, has been decoded, the <code>decode()</code> method of the
 * registered DecoderCallback is called.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface StatefulDecoder
{
    /**
     * Decodes a piece of encoded data. The nature of this call, synchronous
     * verses asyncrhonous, with respect to driving the actual decoding of the
     * encoded data argument is determined by an implementation. A return from
     * this method does not guarantee any callbacks: zero or more callbacks may
     * occur during this call.
     * 
     * @param encoded an object representing a piece of encoded data
     * @throws org.apache.directory.shared.asn1.DecoderException if the encoded element can't be decoded
     */
    void decode( Object encoded ) throws DecoderException;


    /**
     * Sets the callback for this StatefulDecoder.
     * 
     * @param cb the callback to inform of a complete decode operation
     */
    void setCallback( DecoderCallback cb );


    /**
     * @return The decoder callback
     */
    DecoderCallback getCallback();
}