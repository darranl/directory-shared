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
package org.apache.directory.api.ldap.codec.search;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.ldap.codec.api.CodecControl;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchResultReferenceDecorator;
import org.apache.directory.api.ldap.codec.osgi.AbstractCodecServiceTest;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.SearchResultReference;
import org.apache.directory.api.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test the SearchResultReference codec
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class SearchResultReferenceTest extends AbstractCodecServiceTest
{
    /**
     * Test the decoding of a SearchResultReference
     */
    @Test
    public void testDecodeSearchResultReferenceSuccess()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x3d8 );

        String[] ldapUrls = new String[]
            { "ldap:///", "ldap://directory.apache.org:80/", "ldap://d-a.org:80/", "ldap://1.2.3.4/",
                "ldap://1.2.3.4:80/", "ldap://1.1.1.100000.a/", "ldap://directory.apache.org:389/dc=example,dc=org/",
                "ldap://directory.apache.org:389/dc=example", "ldap://directory.apache.org:389/dc=example%202,dc=org",
                "ldap://directory.apache.org:389/dc=example,dc=org?ou",
                "ldap://directory.apache.org:389/dc=example,dc=org?ou,objectclass,dc",
                "ldap://directory.apache.org:389/dc=example,dc=org?ou,dc,ou",
                "ldap:///o=University%20of%20Michigan,c=US",
                "ldap://ldap.itd.umich.edu/o=University%20of%20Michigan,c=US",
                "ldap://ldap.itd.umich.edu/o=University%20of%20Michigan,c=US?postalAddress",
                "ldap://host.com:6666/o=University%20of%20Michigan,c=US??sub?(cn=Babs%20Jensen)",
                "ldap://ldap.itd.umich.edu/c=GB?objectClass?one", "ldap://ldap.question.com/o=Question%3f,c=US?mail",
                "ldap://ldap.netscape.com/o=Babsco,c=US???(int=%5c00%5c00%5c00%5c04)",
                "ldap:///??sub??bindname=cn=Manager%2co=Foo", "ldap:///??sub??!bindname=cn=Manager%2co=Foo" };

        stream.put( new byte[]
            {

                0x30, ( byte ) 0x82, 0x03, ( byte ) 0xd4, // LDAPMessage
                // ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x73,
                ( byte ) 0x82,
                0x03,
                ( byte ) 0xcd, // CHOICE { ...,
            // searchResEntry
            // SearchResultEntry,
            // ...
            // SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
        } );

        for ( int i = 0; i < ldapUrls.length; i++ )
        {
            stream.put( ( byte ) 0x04 );
            stream.put( ( byte ) ldapUrls[i].getBytes().length );

            byte[] bytes = Strings.getBytesUtf8( ldapUrls[i] );

            for ( int j = 0; j < bytes.length; j++ )
            {
                stream.put( bytes[j] );
            }
        }

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultReferenceDecorator> ldapMessageContainer = new LdapMessageContainer<SearchResultReferenceDecorator>(
            codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultReference searchResultReference = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultReference.getMessageId() );

        Set<String> ldapUrlsSet = new HashSet<String>();

        for ( int i = 0; i < ldapUrls.length; i++ )
        {
            ldapUrlsSet.add( Strings.utf8ToString( ldapUrls[i].getBytes() ) );
        }

        Referral referral = searchResultReference.getReferral();

        assertNotNull( referral );

        for ( String ldapUrl : referral.getLdapUrls() )
        {
            if ( ldapUrlsSet.contains( ldapUrl ) )
            {
                ldapUrlsSet.remove( ldapUrl );
            }
            else
            {
                fail( ldapUrl.toString() + " is not present" );
            }
        }

        assertTrue( ldapUrlsSet.size() == 0 );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultReference );

            // Check the length
            assertEquals( 0x3D8, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a SearchResultReference with controls
     */
    @Test
    public void testDecodeSearchResultReferenceSuccessWithControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x3F5 );

        String[] ldapUrls = new String[]
            { "ldap:///", "ldap://directory.apache.org:80/", "ldap://d-a.org:80/", "ldap://1.2.3.4/",
                "ldap://1.2.3.4:80/", "ldap://1.1.1.100000.a/", "ldap://directory.apache.org:389/dc=example,dc=org/",
                "ldap://directory.apache.org:389/dc=example", "ldap://directory.apache.org:389/dc=example%202,dc=org",
                "ldap://directory.apache.org:389/dc=example,dc=org?ou",
                "ldap://directory.apache.org:389/dc=example,dc=org?ou,objectclass,dc",
                "ldap://directory.apache.org:389/dc=example,dc=org?ou,dc,ou",
                "ldap:///o=University%20of%20Michigan,c=US",
                "ldap://ldap.itd.umich.edu/o=University%20of%20Michigan,c=US",
                "ldap://ldap.itd.umich.edu/o=University%20of%20Michigan,c=US?postalAddress",
                "ldap://host.com:6666/o=University%20of%20Michigan,c=US??sub?(cn=Babs%20Jensen)",
                "ldap://ldap.itd.umich.edu/c=GB?objectClass?one", "ldap://ldap.question.com/o=Question%3f,c=US?mail",
                "ldap://ldap.netscape.com/o=Babsco,c=US???(int=%5c00%5c00%5c00%5c04)",
                "ldap:///??sub??bindname=cn=Manager%2co=Foo", "ldap:///??sub??!bindname=cn=Manager%2co=Foo" };

        stream.put( new byte[]
            {

                0x30, ( byte ) 0x82, 0x03, ( byte ) 0xF1, // LDAPMessage
                // ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x73,
                ( byte ) 0x82,
                0x03,
                ( byte ) 0xcd, // CHOICE { ...,
            // searchResEntry
            // SearchResultEntry,
            // ...
            // SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
        } );

        for ( int i = 0; i < ldapUrls.length; i++ )
        {
            stream.put( ( byte ) 0x04 );
            stream.put( ( byte ) ldapUrls[i].getBytes().length );

            byte[] bytes = ldapUrls[i].getBytes();

            for ( int j = 0; j < bytes.length; j++ )
            {
                stream.put( bytes[j] );
            }
        }

        byte[] controlBytes = new byte[]
            { ( byte ) 0xA0,
                0x1B, // A control
                0x30,
                0x19,
                0x04,
                0x17,
                0x32,
                0x2E,
                0x31,
                0x36,
                0x2E,
                0x38,
                0x34,
                0x30,
                0x2E,
                0x31,
                0x2E,
                0x31,
                0x31,
                0x33,
                0x37,
                0x33,
                0x30,
                0x2E,
                0x33,
                0x2E,
                0x34,
                0x2E,
                0x32 };

        for ( int i = 0; i < controlBytes.length; i++ )
        {
            stream.put( controlBytes[i] );
        }

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultReferenceDecorator> ldapMessageContainer = new LdapMessageContainer<SearchResultReferenceDecorator>(
            codec );

        try
        {
            ldapMessageContainer.clean();
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        stream.flip();

        SearchResultReference searchResultReference = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultReference.getMessageId() );

        Set<String> ldapUrlsSet = new HashSet<String>();

        for ( int i = 0; i < ldapUrls.length; i++ )
        {
            ldapUrlsSet.add( Strings.utf8ToString( ldapUrls[i].getBytes() ) );
        }

        Referral referral = searchResultReference.getReferral();

        assertNotNull( referral );

        for ( String ldapUrl : referral.getLdapUrls() )
        {
            if ( ldapUrlsSet.contains( ldapUrl ) )
            {
                ldapUrlsSet.remove( ldapUrl );
            }
            else
            {
                fail( ldapUrl.toString() + " is not present" );
            }
        }

        assertTrue( ldapUrlsSet.size() == 0 );

        // Check the Control
        Map<String, Control> controls = searchResultReference.getControls();

        assertEquals( 1, controls.size() );

        @SuppressWarnings("unchecked")
        CodecControl<Control> control = ( org.apache.directory.api.ldap.codec.api.CodecControl<Control> ) controls
            .get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", Strings.dumpBytes( ( byte[] ) control.getValue() ) );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultReference );

            // Check the length
            assertEquals( 0x3F5, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a SearchResultReference with no reference
     */
    @Test
    public void testDecodeSearchResultReferenceNoReference()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            {

                0x30, 0x05, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x73,
                0x00 // CHOICE { ..., searchResEntry SearchResultEntry,
            // ...
            // SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
        } );

        stream.flip();

        // Allocate a LdapMessage Container
        LdapMessageContainer<SearchResultReferenceDecorator> ldapMessageContainer = new LdapMessageContainer<SearchResultReferenceDecorator>(
            codec );

        // Decode a SearchResultReference message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a SearchResultReference with one reference
     */
    @Test
    public void testDecodeSearchResultReferenceOneReference()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x11 );

        stream.put( new byte[]
            {

                0x30, 0x0F, // LDAPMessage ::=SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                0x73,
                0x0A, // CHOICE { ..., searchResEntry SearchResultEntry,
                // ...
                0x04,
                0x08,
                'l',
                'd',
                'a',
                'p',
                ':',
                '/',
                '/',
                '/' // SearchResultReference
            // ::=
            // [APPLICATION
            // 19]
            // SEQUENCE
            // OF
            // LDAPURL
        } );

        String decodedPdu = Strings.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a BindRequest Container
        LdapMessageContainer<SearchResultReferenceDecorator> ldapMessageContainer = new LdapMessageContainer<SearchResultReferenceDecorator>(
            codec );

        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        SearchResultReference searchResultReference = ldapMessageContainer.getMessage();

        assertEquals( 1, searchResultReference.getMessageId() );

        Referral referral = searchResultReference.getReferral();

        assertNotNull( referral );

        for ( String ldapUrl : referral.getLdapUrls() )
        {
            assertEquals( "ldap:///", ldapUrl );
        }

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( searchResultReference );

            // Check the length
            assertEquals( 0x11, bb.limit() );

            String encodedPdu = Strings.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }
}
