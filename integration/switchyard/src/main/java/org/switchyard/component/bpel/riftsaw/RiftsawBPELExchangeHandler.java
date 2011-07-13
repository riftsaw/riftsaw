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
package org.switchyard.component.bpel.riftsaw;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.riftsaw.engine.BPELEngine;
import org.riftsaw.engine.DeploymentUnit;
import org.riftsaw.engine.Fault;
import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.ServiceReference;
import org.switchyard.component.bpel.config.model.BPELComponentImplementationModel;
import org.switchyard.component.bpel.exchange.BaseBPELExchangeHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Riftsaw implementation of a BPEL ExchangeHandler.
 *
 */
public class RiftsawBPELExchangeHandler extends BaseBPELExchangeHandler {

    private static final Logger logger = Logger.getLogger(RiftsawBPELExchangeHandler.class);

    private final ServiceDomain m_serviceDomain;
    private BPELEngine m_engine=null;
    private QName m_serviceName=null;
    private String m_portName=null;
    private String m_version=null;

    /**
     * Constructs a new DroolsBpmExchangeHandler within the specified ServiceDomain.
     * @param serviceDomain the specified ServiceDomain
     */
    public RiftsawBPELExchangeHandler(ServiceDomain serviceDomain) {
        m_serviceDomain = serviceDomain;
    }

    /**
     * {@inheritDoc}
     */
    public void init(QName qname, BPELComponentImplementationModel model, BPELEngine engine) {
    	
    	m_engine = engine;
    	m_serviceName = QName.valueOf(model.getServiceName());
    	m_portName = model.getPortName();
    	m_version = model.getVersion();
    	
    	String descriptor=model.getProcessDescriptor();
    	
		java.net.URL url=ClassLoader.getSystemResource(descriptor);
		
		java.io.File deployFile=new java.io.File(url.getFile());
		
		DeploymentUnit bdu=new DeploymentUnit(qname.getLocalPart(), m_version,
							deployFile.lastModified(), deployFile);

		// Deploy the process
		engine.deploy(bdu);
    }

    /**
     * {@inheritDoc}
     */
    public void start(ServiceReference serviceRef) {
    	System.out.println("START: "+serviceRef);
    }

    /**
     * {@inheritDoc}
     */
    public void handleMessage(Exchange exchange) throws HandlerException {
        
    	if (exchange.getContract().getServiceOperation().getExchangePattern().equals(ExchangePattern.IN_OUT)) {
            Node request = exchange.getMessage().getContent(Node.class);
            
            java.util.Map<String,Object> headers=new java.util.HashMap<String, Object>();
            
            try {
            	Element response=m_engine.invoke(m_serviceName, m_portName,
            			exchange.getContract().getServiceOperation().getName(),
            					(Element)request, headers);
            	
                Message message = exchange.createMessage();
                message.setContent(response);
                
                exchange.send(message);

            } catch(Fault f) {
            	Message faultMessage=exchange.createMessage();
            	faultMessage.setContent(f.getFaultMessage());
            	
            	// TODO: What about the fault code?
            	
            	exchange.sendFault(faultMessage);
            	
            } catch(Exception e) {
            	e.printStackTrace();
            }
        }
    }

     /**
     * {@inheritDoc}
     */
    public void stop(ServiceReference serviceRef) {
    	System.out.println("STOP: "+serviceRef);
    }

    /**
     * {@inheritDoc}
     */
    public void destroy(ServiceReference serviceRef) {
    	System.out.println("DESTROY: "+serviceRef);
    }

}
