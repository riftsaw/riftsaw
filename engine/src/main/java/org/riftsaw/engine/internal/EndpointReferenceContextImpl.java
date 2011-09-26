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

/**
 * The EndpointReference implementation.
 *
 */
public class EndpointReferenceContextImpl implements EndpointReferenceContext {

    private static final Log LOG = LogFactory.getLog(EndpointReferenceContextImpl.class);

    /**
     * The constructor.
     * 
     * @param server The server
     */
    public EndpointReferenceContextImpl(BPELEngineImpl server) {
    }

    /**
     * {@inheritDoc}
     */
    public EndpointReference resolveEndpointReference(Element element) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolving endpoint reference " + DOMUtils.domToString(element));
        }
    
        return EndpointFactory.createEndpoint(element);
    }

    /**
     * {@inheritDoc}
     */
    public EndpointReference convertEndpoint(QName qName, Element element) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Convert endpoint reference: qname="+qName+" element="+DOMUtils.domToString(element));
        }
        EndpointReference endpoint = EndpointFactory.convert(qName, element);
        return endpoint;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public Map<?,?> getConfigLookup(EndpointReference epr) {
        Map<?,?> result = null;

        if (epr instanceof MutableEndpoint) {
            result = ((MutableEndpoint)epr).toMap();
        } else {
            result = new HashMap();
            // todo map access to xml tree
            LOG.debug("Map access not implemented");
        }
        return result;
    }
}
