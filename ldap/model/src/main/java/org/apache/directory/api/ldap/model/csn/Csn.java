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
package org.apache.directory.api.ldap.model.csn;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.util.Chars;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents 'Change Sequence Number' in LDUP specification.
 * 
 * A CSN is a composition of a timestamp, a replica ID and a 
 * operation sequence number.
 * 
 * It's described in http://tools.ietf.org/html/draft-ietf-ldup-model-09.
 * 
 * The CSN syntax is :
 * <pre>
 * <CSN>            ::= <timestamp> # <changeCount> # <replicaId> # <modifierNumber>
 * <timestamp>      ::= A GMT based time, YYYYmmddHHMMSS.uuuuuuZ
 * <changeCount>    ::= [000000-ffffff] 
 * <replicaId>      ::= [000-fff]
 * <modifierNumber> ::= [000000-ffffff]
 * </pre>
 *  
 * It distinguishes a change made on an object on a server,
 * and if two operations take place during the same timeStamp,
 * the operation sequence number makes those operations distinct.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Csn implements Comparable<Csn>
{
    /** The logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( Csn.class );

    /** The timeStamp of this operation */
    private final long timestamp;

    /** The server identification */
    private final int replicaId;

    /** The operation number in a modification operation */
    private final int operationNumber;

    /** The changeCount to distinguish operations done in the same second */
    private final int changeCount;

    /** Stores the String representation of the CSN */
    private String csnStr;

    /** Stores the byte array representation of the CSN */
    private byte[] bytes;

    /** The Timestamp syntax. The last 'z' is _not_ the Time Zone */
    private static final SimpleDateFormat SDF = new SimpleDateFormat( "yyyyMMddHHmmss" );

    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone( "UTC" );

    // Initialize the dateFormat with the UTC TZ
    static
    {
        SDF.setTimeZone( UTC_TIME_ZONE );
    }

    /** Padding used to format number with a fixed size */
    private static final String[] PADDING_6 = new String[]
        { "00000", "0000", "000", "00", "0", "" };

    /** Padding used to format number with a fixed size */
    private static final String[] PADDING_3 = new String[]
        { "00", "0", "" };


    /**
     * Creates a new instance.
     * <b>This method should be used only for deserializing a CSN</b> 
     * 
     * @param timestamp GMT timestamp of modification
     * @param changeCount The operation increment
     * @param replicaId Replica ID where modification occurred (<tt>[-_A-Za-z0-9]{1,16}</tt>)
     * @param operationNumber Operation number in a modification operation
     */
    public Csn( long timestamp, int changeCount, int replicaId, int operationNumber )
    {
        this.timestamp = timestamp;
        this.replicaId = replicaId;
        this.operationNumber = operationNumber;
        this.changeCount = changeCount;
    }


    /**
     * Creates a new instance of SimpleCSN from a String.
     * 
     * The string format must be :
     * &lt;timestamp> # &lt;changeCount> # &lt;replica ID> # &lt;operation number>
     *
     * @param value The String containing the CSN
     * @throws InvalidCSNException if the value doesn't contain a valid CSN
     */
    public Csn( String value ) throws InvalidCSNException
    {
        if ( Strings.isEmpty( value ) )
        {
            String message = I18n.err( I18n.ERR_04114 );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        if ( value.length() != 40 )
        {
            String message = I18n.err( I18n.ERR_04115 );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        // Get the Timestamp
        int sepTS = value.indexOf( '#' );

        if ( sepTS < 0 )
        {
            String message = I18n.err( I18n.ERR_04116 );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        String timestampStr = value.substring( 0, sepTS ).trim();

        if ( timestampStr.length() != 22 )
        {
            String message = I18n.err( I18n.ERR_04117 );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        // Let's transform the Timestamp by removing the mulliseconds and microseconds
        String realTimestamp = timestampStr.substring( 0, 14 );

        long tempTimestamp = 0L;

        synchronized ( SDF )
        {
            try
            {
                tempTimestamp = SDF.parse( realTimestamp ).getTime();
            }
            catch ( ParseException pe )
            {
                String message = I18n.err( I18n.ERR_04118, timestampStr );
                LOG.error( message );
                throw new InvalidCSNException( message );
            }
        }

        int millis = 0;

        // And add the milliseconds and microseconds now
        try
        {
            millis = Integer.valueOf( timestampStr.substring( 15, 21 ) );
        }
        catch ( NumberFormatException nfe )
        {
            String message = I18n.err( I18n.ERR_04119 );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        tempTimestamp += ( millis / 1000 );
        timestamp = tempTimestamp;

        // Get the changeCount. It should be an hex number prefixed with '0x'
        int sepCC = value.indexOf( '#', sepTS + 1 );

        if ( sepCC < 0 )
        {
            String message = I18n.err( I18n.ERR_04110, value );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        String changeCountStr = value.substring( sepTS + 1, sepCC ).trim();

        try
        {
            changeCount = Integer.parseInt( changeCountStr, 16 );
        }
        catch ( NumberFormatException nfe )
        {
            String message = I18n.err( I18n.ERR_04121, changeCountStr );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        // Get the replicaID
        int sepRI = value.indexOf( '#', sepCC + 1 );

        if ( sepRI < 0 )
        {
            String message = I18n.err( I18n.ERR_04122, value );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        String replicaIdStr = value.substring( sepCC + 1, sepRI ).trim();

        if ( Strings.isEmpty( replicaIdStr ) )
        {
            String message = I18n.err( I18n.ERR_04123 );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        try
        {
            replicaId = Integer.parseInt( replicaIdStr, 16 );
        }
        catch ( NumberFormatException nfe )
        {
            String message = I18n.err( I18n.ERR_04124, replicaIdStr );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        // Get the modification number
        if ( sepCC == value.length() )
        {
            String message = I18n.err( I18n.ERR_04125 );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        String operationNumberStr = value.substring( sepRI + 1 ).trim();

        try
        {
            operationNumber = Integer.parseInt( operationNumberStr, 16 );
        }
        catch ( NumberFormatException nfe )
        {
            String message = I18n.err( I18n.ERR_04126, operationNumberStr );
            LOG.error( message );
            throw new InvalidCSNException( message );
        }

        csnStr = value;
        bytes = Strings.getBytesUtf8( csnStr );
    }


    /**
     * Check if the given String is a valid CSN.
     * 
     * @param value The String to check
     * @return <code>true</code> if the String is a valid CSN
     */
    public static boolean isValid( String value )
    {
        if ( Strings.isEmpty( value ) )
        {
            return false;
        }

        if ( value.length() != 40 )
        {
            return false;
        }

        // Get the Timestamp
        int sepTS = value.indexOf( '#' );

        if ( sepTS < 0 )
        {
            return false;
        }

        String timestampStr = value.substring( 0, sepTS ).trim();

        if ( timestampStr.length() != 22 )
        {
            return false;
        }

        // Let's transform the Timestamp by removing the mulliseconds and microseconds
        String realTimestamp = timestampStr.substring( 0, 14 );

        synchronized ( SDF )
        {
            try
            {
                SDF.parse( realTimestamp ).getTime();
            }
            catch ( ParseException pe )
            {
                return false;
            }
        }

        // And add the milliseconds and microseconds now
        String millisStr = timestampStr.substring( 15, 21 );

        if ( Strings.isEmpty( millisStr ) )
        {
            return false;
        }

        for ( int i = 0; i < 6; i++ )
        {
            if ( !Chars.isDigit( millisStr, i ) )
            {
                return false;
            }
        }

        try
        {
            Integer.valueOf( millisStr );
        }
        catch ( NumberFormatException nfe )
        {
            return false;
        }

        // Get the changeCount. It should be an hex number prefixed with '0x'
        int sepCC = value.indexOf( '#', sepTS + 1 );

        if ( sepCC < 0 )
        {
            return false;
        }

        String changeCountStr = value.substring( sepTS + 1, sepCC ).trim();

        if ( Strings.isEmpty( changeCountStr ) )
        {
            return false;
        }

        if ( changeCountStr.length() != 6 )
        {
            return false;
        }

        try
        {
            for ( int i = 0; i < 6; i++ )
            {
                if ( !Chars.isHex( changeCountStr, i ) )
                {
                    return false;
                }
            }

            Integer.parseInt( changeCountStr, 16 );
        }
        catch ( NumberFormatException nfe )
        {
            return false;
        }

        // Get the replicaIDfalse
        int sepRI = value.indexOf( '#', sepCC + 1 );

        if ( sepRI < 0 )
        {
            return false;
        }

        String replicaIdStr = value.substring( sepCC + 1, sepRI ).trim();

        if ( Strings.isEmpty( replicaIdStr ) )
        {
            return false;
        }

        if ( replicaIdStr.length() != 3 )
        {
            return false;
        }

        for ( int i = 0; i < 3; i++ )
        {
            if ( !Chars.isHex( replicaIdStr, i ) )
            {
                return false;
            }
        }

        try
        {
            Integer.parseInt( replicaIdStr, 16 );
        }
        catch ( NumberFormatException nfe )
        {
            return false;
        }

        // Get the modification number
        if ( sepCC == value.length() )
        {
            return false;
        }

        String operationNumberStr = value.substring( sepRI + 1 ).trim();

        if ( operationNumberStr.length() != 6 )
        {
            return false;
        }

        for ( int i = 0; i < 6; i++ )
        {
            if ( !Chars.isHex( operationNumberStr, i ) )
            {
                return false;
            }
        }

        try
        {
            Integer.parseInt( operationNumberStr, 16 );
        }
        catch ( NumberFormatException nfe )
        {
            return false;
        }

        return true;
    }


    /**
     * Creates a new instance of SimpleCSN from the serialized data
     *
     * @param value The byte array which contains the serialized CSN
     */
    Csn( byte[] value )
    {
        csnStr = Strings.utf8ToString( value );
        Csn csn = new Csn( csnStr );
        timestamp = csn.timestamp;
        changeCount = csn.changeCount;
        replicaId = csn.replicaId;
        operationNumber = csn.operationNumber;
        bytes = Strings.getBytesUtf8( csnStr );
    }


    /**
     * Get the CSN as a byte array. The data are stored as :
     * bytes 1 to 8  : timestamp, big-endian
     * bytes 9 to 12 : change count, big endian
     * bytes 13 to ... : ReplicaId 
     * 
     * @return A copy of the byte array representing theCSN
     */
    public byte[] getBytes()
    {
        if ( bytes == null )
        {
            bytes = Strings.getBytesUtf8( csnStr );
        }

        byte[] copy = new byte[bytes.length];
        System.arraycopy( bytes, 0, copy, 0, bytes.length );
        return copy;
    }


    /**
     * @return The timestamp
     */
    public long getTimestamp()
    {
        return timestamp;
    }


    /**
     * @return The changeCount
     */
    public int getChangeCount()
    {
        return changeCount;
    }


    /**
     * @return The replicaId
     */
    public int getReplicaId()
    {
        return replicaId;
    }


    /**
     * @return The operation number
     */
    public int getOperationNumber()
    {
        return operationNumber;
    }


    /**
     * @return The CSN as a String
     */
    public String toString()
    {
        if ( csnStr == null )
        {
            StringBuilder buf = new StringBuilder( 40 );

            synchronized ( SDF )
            {
                buf.append( SDF.format( new Date( timestamp ) ) );
            }

            // Add the milliseconds part
            long millis = ( timestamp % 1000 ) * 1000;
            String millisStr = Long.toString( millis );

            buf.append( '.' ).append( PADDING_6[millisStr.length() - 1] ).append( millisStr ).append( "Z#" );

            String countStr = Integer.toHexString( changeCount );

            buf.append( PADDING_6[countStr.length() - 1] ).append( countStr );
            buf.append( '#' );

            String replicaIdStr = Integer.toHexString( replicaId );

            buf.append( PADDING_3[replicaIdStr.length() - 1] ).append( replicaIdStr );
            buf.append( '#' );

            String operationNumberStr = Integer.toHexString( operationNumber );

            buf.append( PADDING_6[operationNumberStr.length() - 1] ).append( operationNumberStr );

            csnStr = buf.toString();
        }

        return csnStr;
    }


    /**
     * Returns a hash code value for the object.
     * 
     * @return a hash code value for this object.
     */
    public int hashCode()
    {
        int h = 37;

        h = h * 17 + ( int ) ( timestamp ^ ( timestamp >>> 32 ) );
        h = h * 17 + changeCount;
        h = h * 17 + replicaId;
        h = h * 17 + operationNumber;

        return h;
    }


    /**
     * Indicates whether some other object is "equal to" this one
     * 
     * @param o the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument; 
     * <code>false</code> otherwise.
     */
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( !( o instanceof Csn ) )
        {
            return false;
        }

        Csn that = ( Csn ) o;

        return ( timestamp == that.timestamp ) && ( changeCount == that.changeCount )
            && ( replicaId == that.replicaId ) && ( operationNumber == that.operationNumber );
    }


    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     * 
     * @param   csn the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     */
    public int compareTo( Csn csn )
    {
        if ( csn == null )
        {
            return 1;
        }

        // Compares the timestamp first
        if ( this.timestamp < csn.timestamp )
        {
            return -1;
        }
        else if ( this.timestamp > csn.timestamp )
        {
            return 1;
        }

        // Then the change count
        if ( this.changeCount < csn.changeCount )
        {
            return -1;
        }
        else if ( this.changeCount > csn.changeCount )
        {
            return 1;
        }

        // Then the replicaId
        int replicaIdCompareResult = getReplicaIdCompareResult( csn );

        if ( replicaIdCompareResult != 0 )
        {
            return replicaIdCompareResult;
        }

        // Last, not least, compares the operation number
        if ( this.operationNumber < csn.operationNumber )
        {
            return -1;
        }
        else if ( this.operationNumber > csn.operationNumber )
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }


    private int getReplicaIdCompareResult( Csn csn )
    {
        if ( this.replicaId < csn.replicaId )
        {
            return -1;
        }
        if ( this.replicaId > csn.replicaId )
        {
            return 1;
        }
        return 0;
    }
}
