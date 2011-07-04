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

import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * This interface represents a channel established to
 * an external service, used by a BPEL engine to invoke
 * the service.
 * 
 * @author gbrown
 *
 */
public interface PartnerChannel {
	
	/**
	 * This method returns the endpoint reference of the
	 * external service associated with this channel.
	 * 
	 * @return The endpoint reference
	 */
	public EndpointReference getEndpointReference();

	/**
	 * This method invokes the partner service with the 
	 * supplied request, receiving an optional response
	 * or fault.
	 * 
	 * @param operation The operation name
	 * @param mesg The message
	 * @return The optional response
	 * @throws Exception The optional fault
   * @deprecated use {@link #invoke(org.apache.ode.bpel.iapi.PartnerRoleMessageExchange)} instead
	 */
	public org.w3c.dom.Element invoke(String operation, org.w3c.dom.Element mesg)
							throws Exception;

  
  void invoke(PartnerRoleMessageExchange mex);
	
}
