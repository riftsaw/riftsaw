/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.riftsaw.engine.internal;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.w3c.dom.Document;

/**
 * This class implements the BindingContext interface.
 *
 */
public class RiftsawBindingContext implements BindingContext {

    /**
     * {@inheritDoc}
     */
    public org.apache.ode.bpel.iapi.EndpointReference activateMyRoleEndpoint(QName arg0, Endpoint arg1) {
        return (new EndpointReferenceImpl(arg0, arg1));
    }

    /**
     * {@inheritDoc}
     */
    public long calculateSizeofService(org.apache.ode.bpel.iapi.EndpointReference arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void deactivateMyRoleEndpoint(Endpoint arg0) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * {@inheritDoc}
     */
    public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
              Endpoint initialPartnerEndpoint) {
        return (new PartnerRoleChannelImpl(initialPartnerEndpoint));
    }

    /**
     * Endpoint reference implementation.
     *
     */
    public class EndpointReferenceImpl implements org.apache.ode.bpel.iapi.EndpointReference {

        //private QName _qname=null;
        //private Endpoint _endpoint=null;
        
        /**
         * The constructor.
         * 
         * @param qname The qname
         * @param endpoint The endpoint
         */
        public EndpointReferenceImpl(QName qname, Endpoint endpoint) {
            //_qname = qname;
            //_endpoint = endpoint;
        }
        
        /**
         * {@inheritDoc}
         */
        public Document toXML() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
