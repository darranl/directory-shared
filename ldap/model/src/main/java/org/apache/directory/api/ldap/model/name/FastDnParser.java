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
package org.apache.directory.api.ldap.model.name;


import java.util.List;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.entry.StringValue;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.util.Position;
import org.apache.directory.api.util.Strings;


/**
 * A fast LDAP Dn parser that handles only simple DNs. If the Dn contains
 * any special character an {@link TooComplexException} is thrown.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
/* No protection*/enum FastDnParser
{
    INSTANCE;

    /**
     * Parses a Dn from a String
     *
     * @param name The Dn to parse
     * @return A valid Dn
     * @throws org.apache.directory.api.ldap.model.exception.LdapException If the Dn was invalid
     */
    /* No protection*/static Dn parse( String name ) throws LdapException
    {
        Dn dn = new Dn();
        parseDn( name, dn );
        return dn;
    }


    /**
     * Parses the given name string and fills the given Dn object.
     * 
     * @param name the name to parse
     * @param dn the Dn to fill
     * 
     * @throws LdapInvalidDnException the invalid name exception
     */
    /* No protection*/static void parseDn( String name, Dn dn ) throws LdapInvalidDnException
    {
        parseDn( name, dn.rdns );
        dn.setUpName( name );
        dn.apply( null );
    }


    /* No protection*/static void parseDn( String name, List<Rdn> rdns ) throws LdapInvalidDnException
    {
        if ( ( name == null ) || ( name.trim().length() == 0 ) )
        {
            // We have an empty Dn, just get out of the function.
            return;
        }

        Position pos = new Position();
        pos.start = 0;
        pos.length = name.length();

        while ( true )
        {
            Rdn rdn = new Rdn();
            parseRdnInternal( name, pos, rdn );
            rdns.add( rdn );

            if ( !hasMoreChars( pos ) )
            {
                // end of line reached
                break;
            }
            char c = nextChar( name, pos, true );
            switch ( c )
            {
                case ',':
                case ';':
                    // another Rdn to parse
                    break;

                default:
                    throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04192, c,
                        pos.start ) );
            }
        }
    }


    /**
     * Parses the given name string and fills the given Rdn object.
     * 
     * @param name the name to parse
     * @param rdn the Rdn to fill
     * 
     * @throws LdapInvalidDnException the invalid name exception
     */
    /* No protection*/static void parseRdn( String name, Rdn rdn ) throws LdapInvalidDnException
    {
        if ( name == null || name.length() == 0 )
        {
            throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04193 ) );
        }
        if ( rdn == null )
        {
            throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04194 ) );
        }

        Position pos = new Position();
        pos.start = 0;
        pos.length = name.length();

        parseRdnInternal( name, pos, rdn );
    }


    private static void parseRdnInternal( String name, Position pos, Rdn rdn ) throws LdapInvalidDnException
    {
        int rdnStart = pos.start;

        // SPACE*
        matchSpaces( name, pos );

        // attributeType: ALPHA (ALPHA|DIGIT|HYPEN) | NUMERICOID
        String type = matchAttributeType( name, pos );

        // SPACE*
        matchSpaces( name, pos );

        // EQUALS
        matchEquals( name, pos );

        // SPACE*
        matchSpaces( name, pos );

        // here we only match "simple" values
        // stops at \ + # " -> Too Complex Exception
        String upValue = matchValue( name, pos );
        String value = Strings.trimRight( upValue );
        // TODO: trim, normalize, etc

        // SPACE*
        matchSpaces( name, pos );

        String upName = name.substring( rdnStart, pos.start );

        Ava ava = new Ava( type, type, new StringValue( upValue ),
            new StringValue( value ), upName );
        rdn.addAVA( null, ava );

        rdn.setUpName( upName );
        rdn.normalize();
    }


    /**
     * Matches and forgets optional spaces.
     * 
     * @param name the name
     * @param pos the pos
     * @throws LdapInvalidDnException 
     */
    private static void matchSpaces( String name, Position pos ) throws LdapInvalidDnException
    {
        while ( hasMoreChars( pos ) )
        {
            char c = nextChar( name, pos, true );
            if ( c != ' ' )
            {
                pos.start--;
                break;
            }
        }
    }


    /**
     * Matches attribute type.
     * 
     * @param name the name
     * @param pos the pos
     * 
     * @return the matched attribute type
     * 
     * @throws LdapInvalidDnException the invalid name exception
     */
    private static String matchAttributeType( String name, Position pos ) throws LdapInvalidDnException
    {
        char c = nextChar( name, pos, false );
        switch ( c )
        {
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                // descr
                return matchAttributeTypeDescr( name, pos );

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                // numericoid
                return matchAttributeTypeNumericOid( name, pos );

            default:
                // error
                throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04195, c,
                    pos.start ) );
        }
    }


    /**
     * Matches attribute type descr.
     * 
     * @param name the name
     * @param pos the pos
     * 
     * @return the attribute type descr
     * 
     * @throws LdapInvalidDnException the invalid name exception
     */
    private static String matchAttributeTypeDescr( String name, Position pos ) throws LdapInvalidDnException
    {
        StringBuilder descr = new StringBuilder();
        while ( hasMoreChars( pos ) )
        {
            char c = nextChar( name, pos, true );
            switch ( c )
            {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                case '_': // Violation of the RFC, just because those idiots at Microsoft decided to support it...
                    descr.append( c );
                    break;

                case ' ':
                case '=':
                    pos.start--;
                    return descr.toString();

                case '.':
                    // occurs for RDNs of form "oid.1.2.3=test"
                    throw TooComplexDnException.INSTANCE;

                default:
                    // error
                    throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04196, c,
                        pos.start ) );
            }
        }
        return descr.toString();
    }


    /**
     * Matches attribute type numeric OID.
     * 
     * @param name the name
     * @param pos the pos
     * 
     * @return the attribute type OID
     * 
     * @throws org.apache.directory.api.ldap.model.exception.LdapInvalidDnException the invalid name exception
     */
    private static String matchAttributeTypeNumericOid( String name, Position pos ) throws LdapInvalidDnException
    {
        StringBuilder numericOid = new StringBuilder();
        int dotCount = 0;
        while ( true )
        {
            char c = nextChar( name, pos, true );
            switch ( c )
            {
                case '0':
                    // leading '0', no other digit may follow!
                    numericOid.append( c );
                    c = nextChar( name, pos, true );
                    switch ( c )
                    {
                        case '.':
                            numericOid.append( c );
                            dotCount++;
                            break;
                        case ' ':
                        case '=':
                            pos.start--;
                            break;
                        default:
                            throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err(
                                I18n.ERR_04197, c, pos.start ) );
                    }
                    break;

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    numericOid.append( c );
                    boolean inInnerLoop = true;
                    while ( inInnerLoop )
                    {
                        c = nextChar( name, pos, true );
                        switch ( c )
                        {
                            case ' ':
                            case '=':
                                inInnerLoop = false;
                                pos.start--;
                                break;
                            case '.':
                                inInnerLoop = false;
                                dotCount++;
                                // no break!
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                numericOid.append( c );
                                break;
                            default:
                                throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err(
                                    I18n.ERR_04197, c, pos.start ) );
                        }
                    }
                    break;
                case ' ':
                case '=':
                    pos.start--;
                    if ( dotCount > 0 )
                    {
                        return numericOid.toString();
                    }
                    else
                    {
                        throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04198 ) );
                    }
                default:
                    throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04199, c,
                        pos.start ) );
            }
        }
    }


    /**
     * Matches the equals character.
     * 
     * @param name the name
     * @param pos the pos
     * 
     * @throws LdapInvalidDnException the invalid name exception
     */
    private static void matchEquals( String name, Position pos ) throws LdapInvalidDnException
    {
        char c = nextChar( name, pos, true );
        if ( c != '=' )
        {
            throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04200, c, pos.start ) );
        }
    }


    /**
     * Matches the assertion value. This method only handles simple values.
     * If we find any special character (BACKSLASH, PLUS, SHARP or DQUOTE),
     * a TooComplexException will be thrown.
     * 
     * @param name the name
     * @param pos the pos
     * 
     * @return the string
     * 
     * @throws LdapInvalidDnException the invalid name exception
     */
    private static String matchValue( String name, Position pos ) throws LdapInvalidDnException
    {
        StringBuilder value = new StringBuilder();
        int numTrailingSpaces = 0;
        
        while ( true )
        {
            if ( !hasMoreChars( pos ) )
            {
                pos.start -= numTrailingSpaces;
                return value.substring( 0, value.length() - numTrailingSpaces );
            }
            
            char c = nextChar( name, pos, true );
            
            switch ( c )
            {
                case '\\':
                case '+':
                case '#':
                case '"':
                    throw TooComplexDnException.INSTANCE;
                    
                case ',':
                case ';':
                    pos.start--;
                    pos.start -= numTrailingSpaces;
                    return value.substring( 0, value.length() - numTrailingSpaces );
                    
                case ' ':
                    numTrailingSpaces++;
                    value.append( c );
                    break;
                default:
                    numTrailingSpaces = 0;
                    value.append( c );
            }
        }
    }


    /**
     * Gets the next character.
     * 
     * @param name the name
     * @param pos the pos
     * @param increment true to increment the position
     * 
     * @return the character
     * @throws LdapInvalidDnException If no more characters are available
     */
    private static char nextChar( String name, Position pos, boolean increment ) throws LdapInvalidDnException
    {
        if ( !hasMoreChars( pos ) )
        {
            throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, I18n.err( I18n.ERR_04201, pos.start ) );
        }
        char c = name.charAt( pos.start );
        if ( increment )
        {
            pos.start++;
        }
        return c;
    }


    /**
     * Checks if there are more characters.
     * 
     * @param pos the pos
     * 
     * @return true, if more characters are available
     */
    private static boolean hasMoreChars( Position pos )
    {
        return pos.start < pos.length;
    }
}
