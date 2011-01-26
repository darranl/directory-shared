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
package org.apache.directory.shared.ldap.codec.controls;


import org.apache.directory.shared.asn1.AbstractAsn1Object;
import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.asn1.ber.tlv.UniversalTag;
import org.apache.directory.shared.asn1.ber.tlv.Value;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.model.message.Control;
import org.apache.directory.shared.util.Strings;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;


/**
 * Decorates Control objects by wrapping them, and enabling them as CodecControls
 * so the codec to store transient information associated with the Control in the
 * decorator while processing.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ControlDecorator extends AbstractAsn1Object implements Control, CodecControl
{
    /** The decorated Control */
    private final Control decorated;

    /** The encoded value length */
    protected int valueLength;

    /** The control length */
    private int controlLength;

    /** The control decoder */
    private ControlDecoder decoder;


    /**
     * Creates a ControlDecorator to codec enable it.
     *
     * @param decoratedControl The Control to decorate.
     */
    public ControlDecorator( Control decoratedControl )
    {
        this.decorated = decoratedControl;
    }


    /**
     * Creates a ControlDecorator to codec enable it.
     *
     * @param decoratedControl The Control to decorate.
     * @param decoder The Control's decoder.
     */
    public ControlDecorator( Control decoratedControl, ControlDecoder decoder )
    {
        this.decorated = decoratedControl;
        this.decoder = decoder;
    }


    /**
     * Computes the length of the Control given the length of its value.
     *
     * @param valueLength The length of the Control's value.
     * @return The length of the Control including its value.
     */
    public int computeLength( int valueLength )
    {
        // The OID
        int oidLengh = Strings.getBytesUtf8( getOid() ).length;
        controlLength = 1 + TLV.getNbBytes( oidLengh ) + oidLengh;

        // The criticality, only if true
        if ( isCritical() )
        {
            controlLength += 1 + 1 + 1; // Always 3 for a boolean
        }

        this.valueLength = valueLength;

        if ( valueLength != 0 )
        {
            controlLength += 1 + TLV.getNbBytes( valueLength ) + valueLength;
        }

        return 1 + TLV.getNbBytes( controlLength ) + controlLength;
    }


    // ------------------------------------------------------------------------
    // Control Methods
    // ------------------------------------------------------------------------


    /**
     * Get the OID
     * 
     * @return A string which represent the control oid
     */
    public String getOid()
    {
        return decorated.getOid();
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasValue()
    {
        return decorated.hasValue();
    }


    /**
     * Get the control value
     * 
     * @return The control value
     */
    public byte[] getValue()
    {
        return decorated.getValue();
    }


    /**
     * Set the encoded control value
     * 
     * @param value The encoded control value to store
     */
    public void setValue( byte[] value )
    {
        if ( value != null )
        {
            byte[] copy = new byte[ value.length ];
            System.arraycopy( value, 0, copy, 0, value.length );
            decorated.setValue( copy );
        } 
        else 
        {
            decorated.setValue( null );
        }
    }


    /**
     * Get the criticality
     * 
     * @return <code>true</code> if the criticality flag is true.
     */
    public boolean isCritical()
    {
        return decorated.isCritical();
    }


    /**
     * Set the criticality
     * 
     * @param criticality The criticality value
     */
    public void setCritical( boolean criticality )
    {
        decorated.setCritical( criticality );
    }

    
    // ------------------------------------------------------------------------
    // CodecControl Methods
    // ------------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    public int computeLength()
    {
        return 0;
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

        try
        {
            // The LdapMessage Sequence
            buffer.put( UniversalTag.SEQUENCE.getValue() );

            // The length has been calculated by the computeLength method
            buffer.put( TLV.getBytes( controlLength ) );
        }
        catch ( BufferOverflowException boe )
        {
            throw new EncoderException( I18n.err( I18n.ERR_04005 ) );
        }

        // The control type
        Value.encode( buffer, getOid().getBytes() );

        // The control criticality, if true
        if ( isCritical() )
        {
            Value.encode( buffer, isCritical() );
        }

        return buffer;
    }


    /**
     * {@inheritDoc}
     */
    public ControlDecoder getDecoder()
    {
        return decoder;
    }


    // ------------------------------------------------------------------------
    // Object Method Overrides
    // ------------------------------------------------------------------------


    /**
     * @see Object#equals(Object)
     */
    public boolean equals( Object o )
    {
        if ( o == this )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !( o instanceof Control) )
        {
            return false;
        }

        Control otherControl = ( Control ) o;
        
        if ( !getOid().equalsIgnoreCase( otherControl.getOid() ) )
        {
            return false;
        }

        //noinspection SimplifiableIfStatement
        if ( otherControl.isCritical() != isCritical() )
        {
            return false;
        }

        return hasValue() == otherControl.hasValue();
    }


    /**
     * Return a String representing a Control
     */
    public String toString()
    {
        return decorated.toString();
    }
}