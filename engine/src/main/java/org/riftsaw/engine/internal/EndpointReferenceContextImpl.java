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

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.utils.DOMUtils;
//import org.jboss.soa.bpel.runtime.ws.ClientEndpointReference;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.HashMap;

public class EndpointReferenceContextImpl implements EndpointReferenceContext {

  private static final Log log = LogFactory.getLog(EndpointReferenceContextImpl.class);

  public EndpointReferenceContextImpl(BPELEngineImpl server) {
  }

  public EndpointReference resolveEndpointReference(Element element) {
    if (log.isDebugEnabled())
      log.debug("Resolving endpoint reference " + DOMUtils.domToString(element));
    
    // GPB: TO INVESTIGATE
    //if (element != null && element.getNodeName().equals(ClientEndpointReference.CLIENT_EPR_ELEMENT)) {
    	// Returning null as ODE endpoint factory does not have an EndpointReference type for
    	// this internal riftsaw EPR representation - so null indicates use the default EPR associated
    	// with the WSDL
    	//return(null);
    //}
    return EndpointFactory.createEndpoint(element);
  }

  public EndpointReference convertEndpoint(QName qName, Element element) {
	if (log.isDebugEnabled())
	   log.debug("Convert endpoint reference: qname="+qName+" element="+DOMUtils.domToString(element));
    EndpointReference endpoint = EndpointFactory.convert(qName, element);
    return endpoint;
  }

  public Map<?,?> getConfigLookup(EndpointReference epr) {
    Map result = null;

    if(epr instanceof MutableEndpoint)
    {
      result = ((MutableEndpoint)epr).toMap();
    }
    else
    {
      result = new HashMap();
      // todo map access to xml tree
      log.debug("Map access not implemented");
    }
    return result;
  }
}
