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

import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.w3c.dom.Document;

/**
 * This class provides an implementation of the ODE PartnerRolechannel,
 * that can be used as a proxy to a BPEL engine 'partner channel'.
 * 
 * @author gbrown
 *
 */
public class PartnerRoleChannelImpl implements PartnerRoleChannel {

	public PartnerRoleChannelImpl(PartnerChannel channel) {
		m_channel = channel;
		m_epr = new EndpointReferenceProxy(m_channel.getEndpointReference());
	}
	
	public PartnerChannel getPartnerChannel() {
		return(m_channel);
	}

	public org.apache.ode.bpel.iapi.EndpointReference getInitialEndpointReference() {
		return(m_epr);
	}
	
	public void close() {
	}

	private PartnerChannel m_channel=null;
	private org.apache.ode.bpel.iapi.EndpointReference m_epr=null;
	
	public static class EndpointReferenceProxy implements org.apache.ode.bpel.iapi.EndpointReference {
		
		public EndpointReferenceProxy(EndpointReference ref) {
			m_endpointRef = ref;
		}

		public Document toXML() {
			return(m_endpointRef.toXML());
		}
		
		private EndpointReference m_endpointRef=null;
	}
}
