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
package org.apache.directory.shared.ldap.message.control.replication;

import org.apache.directory.shared.asn1.codec.EncoderException;
import org.apache.directory.shared.ldap.codec.controls.replication.syncInfoValue.SyncInfoValueControlCodec;
import org.apache.directory.shared.ldap.message.control.AbstractMutableControlImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implement the SyncInfoValue interface, and represent the third
 * choice : refreshPresent.
 * The structure for this control is :
 * 
 * syncInfoValue ::= CHOICE {
 *     ...
 *     refreshPresent  [2] SEQUENCE {
 *         cookie         syncCookie OPTIONAL,
 *         refreshDone    BOOLEAN DEFAULT TRUE
 *     },
 * ...
 * }
 *
 * @see <a href="http://www.faqs.org/rfcs/rfc4533.html">RFC 4533</a>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev: $
 *
 */
public class SyncInfoValueRefreshPresentControl extends AbstractMutableControlImpl implements SyncInfoValueControl 
{
    /** As this class is serializable, defined its serialVersionUID */ 
    private static final long serialVersionUID = 1L;

    /** The Logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( SyncInfoValueRefreshPresentControl.class );

    /** The cookie */
    private byte[] cookie;
    
    
    /** The refreshDone flag, default to true */
    private boolean refreshDone = true;
    
    
    /**
     * {@inheritDoc}
     */
    public byte[] getCookie()
    {
        return cookie;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setCookie( byte[] cookie )
    {
        this.cookie = cookie;
    }


    /**
     * @return the refreshDone
     */
    public boolean isRefreshDone()
    {
        return refreshDone;
    }


    /**
     * @param refreshDone the refreshDone to set
     */
    public void setRefreshDone( boolean refreshDone )
    {
        this.refreshDone = refreshDone;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getID()
    {
        return CONTROL_OID;
    }

    
    /**
     * {@inheritDoc}
     */
    public byte[] getEncodedValue()
    {
        SyncInfoValueControlCodec syncInfoValueCtlCodec = 
            new SyncInfoValueControlCodec( SynchronizationInfoEnum.REFRESH_PRESENT );
        syncInfoValueCtlCodec.setCookie( cookie );

        try
        {
            return syncInfoValueCtlCodec.encode( null ).array();
        }
        catch ( EncoderException e )
        {
            LOG.error( "Failed to encode syncInfoValue control", e );
            throw new IllegalStateException( "Failed to encode control with encoder.", e );
        }
    }
}