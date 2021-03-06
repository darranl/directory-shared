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
package org.apache.directory.api.ldap.model.constants;


import org.apache.directory.api.util.Strings;


/**
 * An enum to store all the security constants used in the server
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum LdapSecurityConstants
{
    /** The SHA encryption method */
    HASH_METHOD_SHA("SHA", "SHA", "sha"),

    /** The Salted SHA encryption method */
    HASH_METHOD_SSHA("SSHA", "SHA", "ssha"),

    /** The SHA-256 encryption method */
    HASH_METHOD_SHA256("SHA-256", "SHA-256", "sha256"),

    /** The salted SHA-256 encryption method */
    HASH_METHOD_SSHA256("SSHA-256", "SHA-256", "ssha256"),

    /** The SHA-384 encryption method */
    HASH_METHOD_SHA384("SHA-384", "SHA-384", "sha384"),

    /** The salted SHA-384 encryption method */
    HASH_METHOD_SSHA384("SSHA-384", "SHA-384", "ssha384"),

    /** The SHA-512 encryption method */
    HASH_METHOD_SHA512("SHA-512", "SHA-512", "sha512"),

    /** The salted SHA-512 encryption method */
    HASH_METHOD_SSHA512("SSHA-512", "SHA-512", "ssha512"),

    /** The MD5 encryption method */
    HASH_METHOD_MD5("MD5", "MD5", "md5"),

    /** The Salter MD5 encryption method */
    HASH_METHOD_SMD5("SMD5", "MD5", "smd5"),

    /** The crypt encryption method */
    HASH_METHOD_CRYPT("CRYPT", "CRYPT", "crypt"),

    /** The PBKDF2-based encryption method */
    HASH_METHOD_PKCS5S2("PKCS5S2", "PBKDF2WithHmacSHA1", "PKCS5S2");

    /* These encryption types are not yet supported 
    ** The AES encryption method *
    ENC_METHOD_AES("aes"),
    
    ** The 3DES encryption method *
    ENC_METHOD_3DES("3des"),
    
    ** The Blowfish encryption method *
    ENC_METHOD_BLOWFISH("blowfish"),
    
    ** The RC4 encryption method *
    ENC_METHOD_RC4("rc4");
    */

    /** The associated name */
    private String name;

    /** The associated algorithm */
    private String algorithm;

    /** The associated prefix */
    private String prefix;


    /**
     * Creates a new instance of LdapSecurityConstants.
     * 
     * @param name the associated name
     * @param algorithm the associated algorithm
     * @param prefix the associated prefix
     */
    private LdapSecurityConstants( String name, String algorithm, String prefix )
    {
        this.name = name;
        this.algorithm = algorithm;
        this.prefix = prefix;
    }


    /**
     * @return the name associated with the constant.
     */
    public String getName()
    {
        return name;
    }


    /**
     * @return the prefix associated with the constant.
     */
    public String getAlgorithm()
    {
        return algorithm;
    }


    /**
     * @return the prefix associated with the constant.
     */
    public String getPrefix()
    {
        return prefix;
    }


    /**
     * Get the associated constant from a string
     *
     * @param name The algorithm's name
     * @return The associated constant
     */
    public static LdapSecurityConstants getAlgorithm( String name )
    {
        String algorithm = "";

        if ( name != null )
        {
            algorithm = Strings.toLowerCase( name );
        }

        if ( HASH_METHOD_SHA.getName().equalsIgnoreCase( algorithm )
            || HASH_METHOD_SHA.getPrefix().equalsIgnoreCase( algorithm ) )
        {
            return HASH_METHOD_SHA;
        }

        if ( HASH_METHOD_SSHA.getName().equalsIgnoreCase( algorithm )
            || HASH_METHOD_SSHA.getPrefix().equalsIgnoreCase( algorithm ) )
        {
            return HASH_METHOD_SSHA;
        }

        if ( HASH_METHOD_MD5.getName().equalsIgnoreCase( algorithm )
            || HASH_METHOD_MD5.getPrefix().equalsIgnoreCase( algorithm ))
        {
            return HASH_METHOD_MD5;
        }

        if ( HASH_METHOD_SMD5.getName().equalsIgnoreCase( algorithm )
            || HASH_METHOD_SMD5.getPrefix().equalsIgnoreCase( algorithm ))
        {
            return HASH_METHOD_SMD5;
        }

        if ( HASH_METHOD_CRYPT.getName().equalsIgnoreCase( algorithm )
            || HASH_METHOD_CRYPT.getPrefix().equalsIgnoreCase( algorithm ))
        {
            return HASH_METHOD_CRYPT;
        }

        if ( ( HASH_METHOD_SHA256.getName().equalsIgnoreCase( algorithm ) )
            || ( HASH_METHOD_SHA256.getPrefix().equalsIgnoreCase( algorithm ) )
            || ( "sha-256".equalsIgnoreCase( algorithm ) ) ) // "sha-256" used for backwards compatibility
        {
            return HASH_METHOD_SHA256;
        }

        if ( ( HASH_METHOD_SSHA256.getName().equalsIgnoreCase( algorithm ) )
            || ( HASH_METHOD_SSHA256.getPrefix().equalsIgnoreCase( algorithm ) )
            || ( "ssha-256".equalsIgnoreCase( algorithm ) ) ) // "ssha-256" used for backwards compatibility
        {
            return HASH_METHOD_SSHA256;
        }

        if ( ( HASH_METHOD_SHA384.getName().equalsIgnoreCase( algorithm ) )
            || ( HASH_METHOD_SHA384.getPrefix().equalsIgnoreCase( algorithm ) )
            || ( "sha-384".equalsIgnoreCase( algorithm ) ) ) // "sha-384" used for backwards compatibility
        {
            return HASH_METHOD_SHA384;
        }

        if ( ( HASH_METHOD_SSHA384.getName().equalsIgnoreCase( algorithm ) )
            ||  ( HASH_METHOD_SSHA384.getPrefix().equalsIgnoreCase( algorithm ) )
            || ( "ssha-384".equalsIgnoreCase( algorithm ) ) ) // "ssha-384" used for backwards compatibility
        {
            return HASH_METHOD_SSHA384;
        }

        if ( ( HASH_METHOD_SHA512.getName().equalsIgnoreCase( algorithm ) )
            ||  ( HASH_METHOD_SHA512.getPrefix().equalsIgnoreCase( algorithm ) )
            || ( "sha-512".equalsIgnoreCase( algorithm ) ) ) // "sha-512" used for backwards compatibility
        {
            return HASH_METHOD_SHA512;
        }

        if ( ( HASH_METHOD_SSHA512.getName().equalsIgnoreCase( algorithm ) )
            ||  ( HASH_METHOD_SSHA512.getPrefix().equalsIgnoreCase( algorithm ) )
            || ( "ssha-512".equalsIgnoreCase( algorithm ) ) ) // "ssha-512" used for backwards compatibility
        {
            return HASH_METHOD_SSHA512;
        }

        if ( HASH_METHOD_PKCS5S2.getName().equalsIgnoreCase( algorithm )
            || HASH_METHOD_PKCS5S2.getPrefix().equalsIgnoreCase( algorithm ) )
        {
            return HASH_METHOD_PKCS5S2;
        }

        /*
        if ( ENC_METHOD_AES.getName().equalsIgnoreCase( algorithm ) )
        {
            return ENC_METHOD_AES;
        }

        if ( ENC_METHOD_3DES.getName().equalsIgnoreCase( algorithm ) )
        {
            return ENC_METHOD_3DES;
        }

        if ( ENC_METHOD_BLOWFISH.getName().equalsIgnoreCase( algorithm ) )
        {
            return ENC_METHOD_BLOWFISH;
        }

        if ( ENC_METHOD_RC4.getName().equalsIgnoreCase( algorithm ) )
        {
            return ENC_METHOD_RC4;
        }
        */

        return null;
    }
}
