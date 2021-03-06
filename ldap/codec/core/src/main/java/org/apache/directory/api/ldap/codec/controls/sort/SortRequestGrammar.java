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
package org.apache.directory.api.ldap.codec.controls.sort;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.AbstractGrammar;
import org.apache.directory.api.asn1.ber.grammar.Grammar;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.grammar.GrammarTransition;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.BooleanDecoder;
import org.apache.directory.api.asn1.ber.tlv.BooleanDecoderException;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.api.ldap.model.message.controls.SortKey;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Grammar used for decoding a SortRequestControl.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SortRequestGrammar extends AbstractGrammar<SortRequestContainer>
{
    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger( SortRequestGrammar.class );

    /** Speedup for logs */
    static final boolean IS_DEBUG = LOG.isDebugEnabled();

    /** The instance of grammar. SortRequestGrammar is a singleton */
    private static Grammar<SortRequestContainer> instance = new SortRequestGrammar();


    @SuppressWarnings("unchecked")
    private SortRequestGrammar()
    {
        setName( SortRequestGrammar.class.getName() );

        GrammarAction<SortRequestContainer> addSortKey = new GrammarAction<SortRequestContainer>()
        {

            @Override
            public void action( SortRequestContainer container ) throws DecoderException
            {
                BerValue value = container.getCurrentTLV().getValue();

                String atDesc = Strings.utf8ToString( value.getData() );
                if ( IS_DEBUG )
                {
                    LOG.debug( "AttributeTypeDesc = " + atDesc );
                }
                
                SortKey sk = new SortKey( atDesc );
                container.setCurrentKey( sk );
                container.getControl().addSortKey( sk );
            }

        };

        GrammarAction<SortRequestContainer> storeReverseOrder = new GrammarAction<SortRequestContainer>()
        {

            @Override
            public void action( SortRequestContainer container ) throws DecoderException
            {
                BerValue value = container.getCurrentTLV().getValue();

                try
                {
                    boolean reverseOrder = BooleanDecoder.parse( value );
                    if ( IS_DEBUG )
                    {
                        LOG.debug( "ReverseOrder = " + reverseOrder );
                    }
                    
                    container.getCurrentKey().setReverseOrder( reverseOrder );
                    
                    container.setGrammarEndAllowed( true );
                }
                catch ( BooleanDecoderException e )
                {
                    //String msg = I18n.err( I18n.ERR_04050 );
                    //LOG.error( msg, e );
                    throw new DecoderException( e.getMessage() );
                }
            }

        };
        
        // Create the transitions table
        super.transitions = new GrammarTransition[SortRequestStates.END_STATE.ordinal()][256];

        super.transitions[SortRequestStates.START_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<SortRequestContainer>( SortRequestStates.START_STATE,
                SortRequestStates.SEQUENCE_OF_SEQUENCE_STATE,
                UniversalTag.SEQUENCE.getValue(), null );

        super.transitions[SortRequestStates.SEQUENCE_OF_SEQUENCE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<SortRequestContainer>( SortRequestStates.SEQUENCE_OF_SEQUENCE_STATE,
                SortRequestStates.SORT_KEY_SEQUENCE_STATE,
                UniversalTag.SEQUENCE.getValue(), null );

        super.transitions[SortRequestStates.SORT_KEY_SEQUENCE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] =
            new GrammarTransition<SortRequestContainer>( SortRequestStates.SORT_KEY_SEQUENCE_STATE,
                SortRequestStates.AT_DESC_STATE,
                UniversalTag.OCTET_STRING.getValue(), addSortKey );
        
        super.transitions[SortRequestStates.AT_DESC_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] =
            new GrammarTransition<SortRequestContainer>( SortRequestStates.AT_DESC_STATE,
                SortRequestStates.ORDER_RULE_STATE,
                UniversalTag.OCTET_STRING.getValue(), new GrammarAction<SortRequestContainer>()
                {

                    @Override
                    public void action( SortRequestContainer container ) throws DecoderException
                    {
                        BerValue value = container.getCurrentTLV().getValue();

                        String matchingRuleOid = Strings.utf8ToString( value.getData() );
                        if ( IS_DEBUG )
                        {
                            LOG.debug( "MatchingRuleOid = " + matchingRuleOid );
                        }
                        
                        container.getCurrentKey().setMatchingRuleId( matchingRuleOid );
                    }

                } );
        
        super.transitions[SortRequestStates.ORDER_RULE_STATE.ordinal()][UniversalTag.BOOLEAN.getValue()] =
            new GrammarTransition<SortRequestContainer>( SortRequestStates.ORDER_RULE_STATE,
                SortRequestStates.REVERSE_ORDER_STATE,
                UniversalTag.BOOLEAN.getValue(), storeReverseOrder );
        
        super.transitions[SortRequestStates.AT_DESC_STATE.ordinal()][UniversalTag.BOOLEAN.getValue()] =
            new GrammarTransition<SortRequestContainer>( SortRequestStates.AT_DESC_STATE,
                SortRequestStates.REVERSE_ORDER_STATE,
                UniversalTag.BOOLEAN.getValue(), storeReverseOrder );

        super.transitions[SortRequestStates.REVERSE_ORDER_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<SortRequestContainer>( SortRequestStates.REVERSE_ORDER_STATE,
                SortRequestStates.SORT_KEY_SEQUENCE_STATE,
                UniversalTag.SEQUENCE.getValue(), null );

    }


    /**
     * This class is a singleton.
     * 
     * @return An instance on this grammar
     */
    public static Grammar<?> getInstance()
    {
        return instance;
    }
}
