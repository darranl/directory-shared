/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.shared.ldap.codec;


import java.nio.ByteBuffer;

import org.apache.directory.shared.asn1.Asn1Object;
import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.asn1.ber.tlv.Value;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.codec.controls.ControlDecorator;
import org.apache.directory.shared.ldap.model.message.controls.BasicControl;


/**
 * A decorator for handling opaque Control objects where we know nothing about 
 * their encoded value. These Controls are generated by default when an 
 * {@link IControlFactory} for them has not been registered with the 
 * {@link ILdapCodecService}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class BasicControlDecorator extends ControlDecorator<BasicControl>
{
    /**
     * Creates a new instance of BasicControlDecorator, decorating a 
     * {@link BasicControl}.
     *
     * @param codec The LDAP codec service.
     * @param control The {@link BasicControl} to decorate.
     */
    public BasicControlDecorator( ILdapCodecService codec, BasicControl control )
    {
        super( codec, control );
    }


    @Override
    public Asn1Object decode( byte[] controlBytes ) throws DecoderException
    {
        return null;
    }

    
    /**
     * {@inheritDoc}
     */
    public int computeLength()
    {
        // Call the super class to compute the global control length
        if ( getValue() == null )
        {
            valueLength = 0;
        }
        else
        {
            valueLength = getValue().length;
        }
        
        return super.computeLength( valueLength );
    }


    /**
     * {@inheritDoc}
     */
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        if ( buffer == null )
        {
            throw new EncoderException( I18n.err( I18n.ERR_04023 ) );
        }

        // Encode the Control envelop
        super.encode( buffer );
        
        if ( valueLength != 0 )
        {
            Value.encode( buffer, getValue() );
        }

        return buffer;
    }
}
