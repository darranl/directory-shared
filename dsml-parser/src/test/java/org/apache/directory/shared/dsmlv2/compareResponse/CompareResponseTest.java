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

package org.apache.directory.shared.dsmlv2.compareResponse;


import java.util.List;

import javax.naming.NamingException;

import org.apache.directory.shared.dsmlv2.AbstractResponseTest;
import org.apache.directory.shared.dsmlv2.Dsmlv2ResponseParser;
import org.apache.directory.shared.ldap.codec.ControlCodec;
import org.apache.directory.shared.ldap.codec.LdapResultCodec;
import org.apache.directory.shared.ldap.codec.compare.CompareResponseCodec;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.util.StringTools;

import com.sun.jndi.ldap.LdapURL;


/**
 * Tests for the Compare Response parsing
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class CompareResponseTest extends AbstractResponseTest
{

    /**
     * Test parsing of a Response with the (optional) requestID attribute
     */
    public void testResponseWithRequestId()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_requestID_attribute.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( 456, compareResponse.getMessageId() );
    }


    /**
     * Test parsing of a Response with the (optional) requestID attribute equals 0
     */
    public void testResponseWithRequestIdEquals0()
    {
        testParsingFail( CompareResponseTest.class, "response_with_requestID_equals_0.xml" );
    }


    /**
     * Test parsing of a response with a (optional) Control element
     */
    public void testResponseWith1Control()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_1_control.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( 1, compareResponse.getControls().size() );

        ControlCodec control = compareResponse.getCurrentControl();

        assertTrue( control.getCriticality() );

        assertEquals( "1.2.840.113556.1.4.643", control.getControlType() );

        assertEquals( "Some text", StringTools.utf8ToString( ( byte[] ) control.getControlValue() ) );
    }


    /**
     * Test parsing of a response with a (optional) Control element with empty value
     */
    public void testResponseWith1ControlEmptyValue()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_1_control_empty_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();
        ControlCodec control = compareResponse.getCurrentControl();

        assertEquals( 1, compareResponse.getControls().size() );
        assertTrue( control.getCriticality() );
        assertEquals( "1.2.840.113556.1.4.643", control.getControlType() );
        assertEquals( StringTools.EMPTY_BYTES, ( byte[] ) control.getControlValue() );
    }


    /**
     * Test parsing of a response with 2 (optional) Control elements
     */
    public void testResponseWith2Controls()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_2_controls.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( 2, compareResponse.getControls().size() );

        ControlCodec control = compareResponse.getCurrentControl();

        assertFalse( control.getCriticality() );

        assertEquals( "1.2.840.113556.1.4.789", control.getControlType() );

        assertEquals( "Some other text", StringTools.utf8ToString( ( byte[] ) control.getControlValue() ) );
    }


    /**
     * Test parsing of a response with 3 (optional) Control elements without value
     */
    public void testResponseWith3ControlsWithoutValue()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_3_controls_without_value.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        assertEquals( 3, compareResponse.getControls().size() );

        ControlCodec control = compareResponse.getCurrentControl();

        assertTrue( control.getCriticality() );

        assertEquals( "1.2.840.113556.1.4.456", control.getControlType() );

        assertEquals( StringTools.EMPTY_BYTES, control.getControlValue() );
    }


    /**
     * Test parsing of a response without Result Code element
     */
    public void testResponseWithoutResultCode()
    {
        testParsingFail( CompareResponseTest.class, "response_without_result_code.xml" );
    }


    /**
     * Test parsing of a response with Result Code element but a not integer value
     */
    public void testResponseWithResultCodeNotInteger()
    {
        testParsingFail( CompareResponseTest.class, "response_with_result_code_not_integer.xml" );
    }


    /**
     * Test parsing of a response with Result Code 
     */
    public void testResponseWithResultCode()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_result_code.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        assertEquals( ResultCodeEnum.PROTOCOL_ERROR, ldapResult.getResultCode() );
    }


    /**
     * Test parsing of a response with Error Message
     */
    public void testResponseWithErrorMessage()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_error_message.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        assertEquals( "Unrecognized extended operation EXTENSION_OID: 1.2.6.1.4.1.18060.1.1.1.100.2", ldapResult
            .getErrorMessage() );
    }


    /**
     * Test parsing of a response with empty Error Message
     */
    public void testResponseWithEmptyErrorMessage()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_empty_error_message.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        assertNull( ldapResult.getErrorMessage() );
    }


    /**
     * Test parsing of a response with a Referral
     */
    public void testResponseWith1Referral()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_1_referral.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        List referrals = ldapResult.getReferrals();

        assertEquals( 1, referrals.size() );

        Object referral = referrals.get( 0 );

        try
        {
            assertEquals( new LdapURL( "ldap://www.apache.org/" ).toString(), referral.toString() );
        }
        catch ( NamingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with an empty Referral
     */
    public void testResponseWith1EmptyReferral()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput(
                CompareResponseTest.class.getResource( "response_with_1_empty_referral.xml" ).openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        List referrals = ldapResult.getReferrals();

        assertEquals( 0, referrals.size() );
    }


    /**
     * Test parsing of a response with 2 Referral elements
     */
    public void testResponseWith2Referrals()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_2_referrals.xml" ).openStream(),
                "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        List referrals = ldapResult.getReferrals();

        assertEquals( 2, referrals.size() );

        Object referral = referrals.get( 0 );

        try
        {
            assertEquals( new LdapURL( "ldap://www.apache.org/" ).toString(), referral.toString() );
        }
        catch ( NamingException e )
        {
            fail();
        }

        Object referral2 = referrals.get( 1 );

        try
        {
            assertEquals( new LdapURL( "ldap://www.apple.com/" ).toString(), referral2.toString() );
        }
        catch ( NamingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with a Referral and an Error Message
     */
    public void testResponseWith1ReferralAndAnErrorMessage()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_1_referral_and_error_message.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        List referrals = ldapResult.getReferrals();

        assertEquals( 1, referrals.size() );

        Object referral = referrals.get( 0 );

        try
        {
            assertEquals( new LdapURL( "ldap://www.apache.org/" ).toString(), referral.toString() );
        }
        catch ( NamingException e )
        {
            fail();
        }
    }


    /**
     * Test parsing of a response with MatchedDN attribute
     */
    public void testResponseWithMatchedDNAttribute()
    {
        Dsmlv2ResponseParser parser = null;
        try
        {
            parser = new Dsmlv2ResponseParser();

            parser.setInput( CompareResponseTest.class.getResource( "response_with_matchedDN_attribute.xml" )
                .openStream(), "UTF-8" );

            parser.parse();
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }

        CompareResponseCodec compareResponse = ( CompareResponseCodec ) parser.getBatchResponse().getCurrentResponse();

        LdapResultCodec ldapResult = compareResponse.getLdapResult();

        assertEquals( "cn=Bob Rush,ou=Dev,dc=Example,dc=COM", ldapResult.getMatchedDN() );
    }


    /**
     * Test parsing of a response with wrong matched DN
     */
    public void testResponseWithWrongMatchedDN()
    {
        testParsingFail( CompareResponseTest.class, "response_with_wrong_matchedDN_attribute.xml" );
    }


    /**
     * Test parsing of a response with wrong Descr attribute
     */
    public void testResponseWithWrongDescr()
    {
        testParsingFail( CompareResponseTest.class, "response_with_wrong_descr.xml" );
    }
}