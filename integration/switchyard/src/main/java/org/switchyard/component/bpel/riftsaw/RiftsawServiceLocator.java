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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;

import org.apache.log4j.Logger;
import org.riftsaw.engine.Fault;
import org.riftsaw.engine.Service;
import org.riftsaw.engine.ServiceLocator;
import org.switchyard.Exchange;
import org.switchyard.ExchangeState;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.ServiceReference;
import org.switchyard.SynchronousInOutHandler;
import org.switchyard.component.bpel.config.model.BPELComponentImplementationModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.exception.DeliveryException;
import org.switchyard.exception.SwitchYardException;
import org.switchyard.metadata.BaseExchangeContract;
import org.switchyard.metadata.ServiceOperation;
import org.w3c.dom.Element;

/**
 * This class implements the service locator interface to retrieve a
 * reference to an external service (provided by switchyard) for use
 * by a BPEL process instance.
 *
 */
public class RiftsawServiceLocator implements ServiceLocator {

    private static final Logger logger = Logger.getLogger(RiftsawServiceLocator.class);
    
    private static final long DEFAULT_TIMEOUT = 120000;

	private ServiceDomain m_serviceDomain=null;
	private java.util.Map<QName, RegistryEntry> m_registry=new java.util.HashMap<QName, RegistryEntry>();
    private long m_waitTimeout = DEFAULT_TIMEOUT;
	
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
		// Currently need to just use the local part, without the version number, to
		// lookup the registry entry
		int index=processName.getLocalPart().indexOf('-');
		QName localProcessName=new QName(null, processName.getLocalPart().substring(0,index));
		
		RegistryEntry re=m_registry.get(localProcessName);
		
		if (re == null) {
			logger.error("No service references found for process '"+processName+"'");
			return(null);
		}
		
		Service ret=re.getService(serviceName, portName, m_serviceDomain);
		
		if (ret == null) {
			logger.error("No service found for '"+serviceName+"' (port "+portName+")");
		}
		
		return(ret);
	}
	
	public void initialiseReference(ComponentReferenceModel crm) {
		
		// Find the BPEL implementation associated with the reference
		if (crm.getComponent() != null &&
					crm.getComponent().getImplementation() instanceof BPELComponentImplementationModel) {
			BPELComponentImplementationModel impl=
					(BPELComponentImplementationModel)crm.getComponent().getImplementation();
			
			String local=impl.getProcess();
			String ns=null;
			int index=0;
			
			if ((index=local.indexOf(':')) != -1) {
				// TODO: For now ignore the namespace
				//String prefix = local.substring(0, index);
				local = local.substring(index+1);
			}
			
			QName processName=new QName(ns, local);
			
			if (logger.isDebugEnabled()) {
				logger.debug("Register reference "+crm.getName()+" ("+crm.getQName()+") for process "+processName);
			}
			
			RegistryEntry re=m_registry.get(processName);
			
			if (re == null) {
				re = new RegistryEntry();
				m_registry.put(processName, re);
			}
			
			javax.wsdl.Definition wsdl=WSDLHelper.getWSDLDefinition(crm.getInterface().getInterface());
			javax.wsdl.PortType portType=WSDLHelper.getPortType(crm.getInterface().getInterface(), wsdl);
			
			re.register(wsdl, portType.getQName(), crm.getQName());

		} else {
			throw new SwitchYardException("Could not find BPEL implementation associated with reference");
		}
		
	}
	
	public class RegistryEntry {
		
		private java.util.List<javax.wsdl.Definition> m_wsdls=new java.util.Vector<javax.wsdl.Definition>();
		private java.util.List<QName> m_portTypes=new java.util.Vector<QName>();
		private java.util.List<QName> m_services=new java.util.Vector<QName>();
		
		public void register(javax.wsdl.Definition wsdl, QName portType, QName service) {
			m_wsdls.add(wsdl);
			m_portTypes.add(portType);
			m_services.add(service);
		}
		
		public Service getService(QName serviceName, String portName, ServiceDomain serviceDomain) {
			Service ret=null;
			
			for (int i=0; ret == null && i < m_wsdls.size(); i++) {
				javax.wsdl.Service service=m_wsdls.get(i).getService(serviceName);
				
				if (service != null) {
					javax.wsdl.Port port=service.getPort(portName);
					
					if (port != null &&
							port.getBinding().getPortType().getQName().equals(m_portTypes.get(i))) {
						QName switchYardService=m_services.get(i);
						
						ServiceReference sref=serviceDomain.getService(switchYardService);
						
						if (sref == null) {
							logger.error("No service found for '"+serviceName+"' (port "+portName+")");
							return(null);
						}
						
						ret = new ServiceProxy(sref, port.getBinding().getPortType());
					}
				}
			}

			return(ret);
		}
	}

	public class ServiceProxy implements Service {
		
		private ServiceReference m_serviceReference=null;
		private javax.wsdl.PortType m_portType=null;
		
		public ServiceProxy(ServiceReference sref, javax.wsdl.PortType portType) {
			m_serviceReference = sref;
			m_portType = portType;
		}

		public Element invoke(String operationName, Element mesg,
				Map<String, Object> headers) throws Exception {
			
			// Unwrap the first two levels, to remove the part wrapper
			mesg = WSDLHelper.unwrapMessagePart(mesg);
			
			// Need to create an exchange
            SynchronousInOutHandler rh = new SynchronousInOutHandler();

			ServiceOperation op=m_serviceReference.getInterface().getOperation(operationName);
			
			BaseExchangeContract exchangeContract = new BaseExchangeContract(op);
						
			Exchange exchange=m_serviceReference.createExchange(exchangeContract, rh);
			
			Message req=exchange.createMessage();			
			req.setContent(mesg);
			exchange.send(req);
			
            try {
                exchange = rh.waitForOut(m_waitTimeout);
            } catch (DeliveryException e) {
                throw new HandlerException("Timed out after " + m_waitTimeout +
                		" ms waiting on synchronous response from target service '" +
                		m_serviceReference.getName() + "'.");
            }
            
			Message resp=exchange.getMessage();
			
			if (resp == null) {
				throw new Exception("Response not returned from operation '"+operationName+
								"' on service: "+m_serviceReference.getName());
				
			} else if ((resp.getContent() instanceof Element) == false) {
				throw new Exception("Response is not an Element for operation '"+operationName+
						"' on service: "+m_serviceReference.getName());
			}

        	Element respelem=(Element)resp.getContent();
        	
        	javax.wsdl.Operation operation=m_portType.getOperation(operationName, null, null);
        	
			if (exchange.getState() == ExchangeState.FAULT) {
				QName faultCode=null;
				
				if (respelem instanceof SOAPFault) {
					SOAPFault fault=(SOAPFault)resp.getContent();
					
					respelem = (Element)fault.getDetail().getFirstChild();
					
					faultCode = fault.getFaultCodeAsQName();
				}
				
				Element newfault=WSDLHelper.wrapFaultMessagePart(respelem, operation, null);

				throw new Fault(faultCode, newfault);
			}
			
			Element newresp=WSDLHelper.wrapResponseMessagePart(respelem, operation);
			
			return((Element)newresp);
		}
	}
	
}
