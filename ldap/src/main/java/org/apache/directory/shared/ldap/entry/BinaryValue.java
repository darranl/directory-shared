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
package org.apache.directory.shared.ldap.entry;


import org.apache.directory.shared.ldap.schema.ByteArrayComparator;
import org.apache.directory.shared.ldap.util.StringTools;

import java.util.Arrays;
import java.util.Comparator;


/**
 * A wrapper around byte[] values in entries.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class BinaryValue implements Value<byte[]>
{
    /** A byte array comparator instance */
	@SuppressWarnings ( { "unchecked" } )
    private static final Comparator<byte[]> BYTE_ARRAY_COMPARATOR = new ByteArrayComparator();
    
    /** the wrapped binary value */
    private byte[] wrapped;


    /**
     * Creates a new instance of BinaryValue with no initial wrapped value.
     */
    public BinaryValue()
    {
    }


    /**
     * Creates a new instance of BinaryValue with a wrapped value.
     *
     * @param wrapped the binary value to wrap
     */
    public BinaryValue( byte[] wrapped )
    {
        set( wrapped );
    }


    /**
     * Dumps binary in hex with label.
     *
     * @see Object#toString()
     */
    public String toString()
    {
        return "BinaryValue : " + StringTools.dumpBytes( wrapped );
    }


    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return Arrays.hashCode( wrapped );
    }


    /**
     * Gets a copy of the binary value.
     *
     * @return a copy of the binary value
     */
    public byte[] getCopy()
    {
        if ( wrapped == null )
        {
            return null;
        }

        final byte[] copy = new byte[ wrapped.length ];
        System.arraycopy( wrapped, 0, copy, 0, wrapped.length );
        return copy;
    }

    
    /**
     * Gets a reference to the wrapped binary value.
     * 
     * Warning ! The value is not copied !!!
     *
     * @return a direct handle on the binary value that is wrapped
     */
    public byte[] getReference()
    {
        return wrapped;
    }

    
    /**
     * Returns <code>true</code> if the wrapper contains no value.
     */
    public final boolean isNull()
    {
        return wrapped == null; 
    }
    
    
    /**
     * Sets this value's wrapped value to a copy of the src array.
     *
     * @param wrapped the byte array to use as the wrapped value
     */
    public void set( byte[] wrapped )
    {
        if ( wrapped != null )
        {
            this.wrapped = new byte[ wrapped.length ];
            System.arraycopy( wrapped, 0, this.wrapped, 0, wrapped.length );
        }
        else
        {
            this.wrapped = null;
        }
    }


    /**
     * Makes a deep copy of the BinaryValue.
     *
     * @return a deep copy of the Value.
     */
    public BinaryValue clone() throws CloneNotSupportedException
    {
        BinaryValue cloned = (BinaryValue)super.clone();
        
        if ( wrapped != null )
        {
            cloned.wrapped = new byte[ wrapped.length ];
            System.arraycopy( wrapped, 0, cloned.wrapped, 0, wrapped.length );
        }
        
        return cloned;
    }


    /**
     * @see Object#equals(Object)
     */
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null )
        {
            return false;
        }

        if ( obj.getClass() != this.getClass() )
        {
            return false;
        }

        BinaryValue binaryValue = ( BinaryValue ) obj;
        
        if ( ( wrapped == null ) && ( binaryValue.wrapped == null ) )
        {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if ( isNull() != binaryValue.isNull() )
        {
            return false;
        }

        return Arrays.equals( wrapped, binaryValue.wrapped );
    }


    /**
     * Compare with the current BinaryValue 
     *
     * @see Comparable#compareTo(Object) 
     */
    @SuppressWarnings ( { "JavaDoc" } )
    public int compareTo( BinaryValue value )
    {
        if ( value == null )
        {
            if ( wrapped == null )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }

        if ( wrapped == null )
        {
            return -1;
        }

        return BYTE_ARRAY_COMPARATOR.compare( wrapped, value.getReference() );
    }
}