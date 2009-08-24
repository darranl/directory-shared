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
package org.apache.directory.shared.ldap.schema.parsers;


import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import antlr.RecognitionException;
import antlr.TokenStreamException;


/**
 * A parser for RFC 4512 matching rule use descriptions.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class MatchingRuleUseDescriptionSchemaParser extends AbstractSchemaParser
{
    /** The LoggerFactory used by this class */
    protected static final Logger LOG = LoggerFactory.getLogger( MatchingRuleUseDescriptionSchemaParser.class );


    /**
     * Creates a schema parser instance.
     */
    public MatchingRuleUseDescriptionSchemaParser()
    {
    }


    /**
     * Parses a matching rule use description according to RFC 4512:
     * 
     * <pre>
     * MatchingRuleUseDescription = LPAREN WSP
     *    numericoid                 ; object identifier
     *    [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     *    [ SP "DESC" SP qdstring ]  ; description
     *    [ SP "OBSOLETE" ]          ; not active
     *    SP "APPLIES" SP oids       ; attribute types
     *    extensions WSP RPAREN      ; extensions
     * 
     * extensions = *( SP xstring SP qdstrings )
     * xstring = "X" HYPHEN 1*( ALPHA / HYPHEN / USCORE ) 
     * </pre>
     * 
     * @param matchingRuleUseDescription the matching rule use description to be parsed
     * @return the parsed MatchingRuleUseDescription bean
     * @throws ParseException if there are any recognition errors (bad syntax)
     */
    public synchronized MatchingRuleUseDescription parseMatchingRuleUseDescription( String matchingRuleUseDescription )
        throws ParseException
    {

        if ( matchingRuleUseDescription == null )
        {
            LOG.error( "Cannot parse a null MatchingRuleUse" );
            throw new ParseException( "Null", 0 );
        }

        reset( matchingRuleUseDescription ); // reset and initialize the parser / lexer pair

        try
        {
            MatchingRuleUseDescription mrud = parser.matchingRuleUseDescription();
            return mrud;
        }
        catch ( RecognitionException re )
        {
            String msg = "Parser failure on matching rule description:\n\t" + matchingRuleUseDescription +
                "\nAntlr message: " + re.getMessage() +
                "\nAntlr column: " + re.getColumn();
            LOG.error( msg );
            throw new ParseException( msg, re.getColumn() );
        }
        catch ( TokenStreamException tse )
        {
            String msg = "Parser failure on matching rule description:\n\t" + matchingRuleUseDescription +
                "\nAntlr message: " + tse.getMessage();
            LOG.error( msg );
            throw new ParseException( msg, 0 );
        }

    }


    /**
     * Parses a MatchingRuleUse description
     * 
     * @param The MatchingRuleUse description to parse
     * @return An instance of MatchingRuleUseDescription
     */
    public MatchingRuleUseDescription parse( String schemaDescription ) throws ParseException
    {
        return parseMatchingRuleUseDescription( schemaDescription );
    }
}
