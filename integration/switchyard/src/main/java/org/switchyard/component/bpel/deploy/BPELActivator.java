/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
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
package org.switchyard.component.bpel.deploy;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.riftsaw.engine.BPELEngine;
import org.riftsaw.engine.BPELEngineFactory;
import org.switchyard.ExchangeHandler;
import org.switchyard.ServiceReference;
//import org.riftsaw.switchyard.component.bpel.config.model.BPELComponentImplementationModel;
//import org.riftsaw.switchyard.component.bpel.exchange.BPELExchangeHandler;
//import org.riftsaw.switchyard.component.bpel.exchange.BPELExchangeHandlerFactory;
import org.switchyard.component.bpel.config.model.BPELComponentImplementationModel;
import org.switchyard.component.bpel.exchange.BPELExchangeHandler;
import org.switchyard.component.bpel.exchange.BPELExchangeHandlerFactory;
import org.switchyard.config.model.Model;
import org.switchyard.config.model.composite.ComponentServiceModel;
import org.switchyard.deploy.BaseActivator;
import org.switchyard.exception.SwitchYardException;

/**
 * Activator for the BPEL component.
 *
 */
public class BPELActivator extends BaseActivator {

    private Map<QName,BPELExchangeHandler> m_handlers = new HashMap<QName,BPELExchangeHandler>();

	private BPELEngine m_engine=null;
	
    /**
     * Constructs a new Activator of type "bpel".
     */
    public BPELActivator() {
        super("bpel");
        
        init();
    }

	public void init() {
		m_engine = BPELEngineFactory.getEngine();
		
		try {
			// TODO: Set an appropriate service locator
			m_engine.init(null);
		} catch(Exception e) {
			throw new SwitchYardException("Failed to initialize the engine: "+e, e);
		}
	}
	
    /**
     * {@inheritDoc}
     */
	public ExchangeHandler init(QName qname, Model model) {
    	if (model instanceof ComponentServiceModel) {
    		BPELExchangeHandler handler = BPELExchangeHandlerFactory.instance().newBPELExchangeHandler(getServiceDomain());
    		BPELComponentImplementationModel bciModel = (BPELComponentImplementationModel)((ComponentServiceModel)model).getComponent().getImplementation();
    		
    		handler.init(qname, bciModel, m_engine);
    		
    		m_handlers.put(qname, handler);
    		return handler;
    	}
    	throw new SwitchYardException("No BPEL component implementations found for service " + qname);
    }

    /**
     * {@inheritDoc}
     */
    public void start(ServiceReference serviceRef) {
    	BPELExchangeHandler handler = m_handlers.get(serviceRef.getName());
    	if (handler != null) {
    		handler.start(serviceRef);
    	}
    }

    /**
     * {@inheritDoc}
     */
    public void stop(ServiceReference serviceRef) {
    	BPELExchangeHandler handler = m_handlers.get(serviceRef.getName());
    	if (handler != null) {
    		handler.stop(serviceRef);
    	}
    }

    /**
     * {@inheritDoc}
     */
    public void destroy(ServiceReference serviceRef) {
    	BPELExchangeHandler handler = m_handlers.get(serviceRef.getName());
    	if (handler != null) {
    		try {
    			handler.destroy(serviceRef);
    		} finally {
    			m_handlers.remove(serviceRef.getName());
    		}
    	}
    }

}
