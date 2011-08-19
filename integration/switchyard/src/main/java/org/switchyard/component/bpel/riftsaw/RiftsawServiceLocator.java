/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.switchyard.component.bpel.riftsaw;

import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.riftsaw.engine.Service;
import org.riftsaw.engine.ServiceLocator;
import org.switchyard.BaseHandler;
import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.ServiceReference;
import org.switchyard.component.bpel.config.model.BPELComponentImplementationModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.exception.SwitchYardException;
import org.switchyard.metadata.ExchangeContract;
import org.w3c.dom.Element;

/**
 * This class implements the service locator interface to retrieve a
 * reference to an external service (provided by switchyard) for use
 * by a BPEL process instance.
 *
 */
public class RiftsawServiceLocator implements ServiceLocator {

    private static final Logger logger = Logger.getLogger(RiftsawServiceLocator.class);

	private ServiceDomain m_serviceDomain=null;
	
	public RiftsawServiceLocator(ServiceDomain serviceDomain) {
		m_serviceDomain = serviceDomain;
	}
	
	public void setServiceDomain(ServiceDomain serviceDomain) {
		m_serviceDomain = serviceDomain;
	}
	
	public ServiceDomain getServiceDomain() {
		return(m_serviceDomain);
	}
	
	public Service getService(QName processName, QName serviceName, String portName) {	
		ServiceReference sref=m_serviceDomain.getService(serviceName);
		
		if (sref == null) {
			logger.error("No service found for '"+serviceName+"' (port "+portName+")");
			return(null);
		}
		
		return(new ServiceProxy(sref));
	}
	
	public void initialiseReference(ComponentReferenceModel crm) {
		
		// Find the BPEL implementation associated with the reference
		if (crm.getComponent() != null &&
					crm.getComponent().getImplementation() instanceof BPELComponentImplementationModel) {
			BPELComponentImplementationModel impl=
					(BPELComponentImplementationModel)crm.getComponent().getImplementation();
			
			try {
				java.net.URL top=ClassLoader.getSystemResource("loan_approval");
				
				System.out.println("Scan resources under: "+top);
				
				java.util.Enumeration<java.net.URL> urls=ClassLoader.getSystemResources("deploy.xml");
				
				while (urls.hasMoreElements()) {
					java.net.URL url=urls.nextElement();
					
					System.out.println("LOCATED AT: "+url);
				}
			} catch(Exception e) {
				throw new SwitchYardException("Unable to find BPEL deployment descriptor(s)", e);
			}


			
		} else {
			throw new SwitchYardException("Could not find BPEL implementation associated with reference");
		}
		
	}

	public static class ServiceProxy implements Service {
		
		private ServiceReference m_serviceReference=null;
		
		public ServiceProxy(ServiceReference sref) {
			m_serviceReference = sref;
		}

		public Element invoke(String operationName, Element mesg,
				Map<String, Object> headers) throws Exception {
			Semaphore sem=new Semaphore(0);
			
			// Need to create an exchange
			ResponseHandler rh=new ResponseHandler();
			rh.init(sem);
						
			Exchange exchange=m_serviceReference.createExchange(ExchangeContract.IN_OUT, rh);
			
			Message req=exchange.createMessage();			
			req.setContent(mesg);
			exchange.send(req);
			
			// Wait for response
			sem.acquire();
			
			Message resp=rh.getMessage();
			
			if (resp == null) {
				throw new Exception("Response not returned from operation '"+operationName+
								"' on service: "+m_serviceReference.getName());
			} else if ((resp.getContent() instanceof Element) == false) {
				throw new Exception("Response is not an Element for operation '"+operationName+
						"' on service: "+m_serviceReference.getName());
			}
			
			// TODO Handle one-way requests, faults and headers
			
			return((Element)resp.getContent());
		}
		
		public class ResponseHandler extends BaseHandler {
			
			private Semaphore m_semaphore=null;
			private Message m_message=null;
			
			public ResponseHandler() {
			}
			
			public void init(Semaphore sem) {
				m_semaphore = sem;
			}
			
			public Message getMessage() {
				return(m_message);
			}
			
			public void handleFault(final Exchange exchange) {
				m_message = exchange.getMessage();
				
				// TODO: How to distinguish fault?
				
				m_semaphore.release();
			}

			public void handleMessage(final Exchange exchange) throws HandlerException {
				m_message = exchange.getMessage();
				
				m_semaphore.release();
			}

		}
	}
	
}
