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

import org.switchyard.exception.SwitchYardException;

/**
 * WSDL Helper.
 *
 */
public class WSDLHelper {

	private static final String WSDL_PORTTYPE_PREFIX = "#wsdl.porttype(";

	public static javax.wsdl.Definition getWSDLDefinition(String location) throws SwitchYardException {
		javax.wsdl.Definition ret=null;
		
		if (location == null) {
			throw new SwitchYardException("WSDL location has not been specified");
		} else {
			try {
				int index=location.indexOf('#');
				
				if (index != -1) {
					location = location.substring(0, index);
				}
				
				java.net.URL url=ClassLoader.getSystemResource(location);
				
		        ret = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader().readWSDL(url.getFile());
				
			} catch(Exception e) {
				throw new SwitchYardException("Failed to load WSDL '"+location+"'", e);
			}
		}

		return(ret);
	}

	public static javax.wsdl.PortType getPortType(String location, javax.wsdl.Definition wsdl)
									throws SwitchYardException {
		javax.wsdl.PortType ret=null;
		
		if (location == null) {
			throw new SwitchYardException("WSDL location has not been specified");
		} else {
			int index=location.indexOf(WSDL_PORTTYPE_PREFIX);
			
			if (index != -1) {
				String portTypeName = location.substring(index+WSDL_PORTTYPE_PREFIX.length(), location.length()-1);
				
				ret = wsdl.getPortType(new QName(wsdl.getTargetNamespace(), portTypeName));
			}
		}

		return(ret);
	}
	
	public static javax.wsdl.Service getServiceForPortType(javax.wsdl.PortType portType,
								javax.wsdl.Definition wsdl) {
		javax.wsdl.Service ret=null;
		
		java.util.Iterator<?> iter=wsdl.getServices().values().iterator();
		while (ret == null && iter.hasNext()) {
			ret = (javax.wsdl.Service)iter.next();
			
			java.util.Iterator<?> ports=ret.getPorts().values().iterator();
			boolean f_found=false;
			
			while (!f_found && ports.hasNext()) {
				javax.wsdl.Port port=(javax.wsdl.Port)ports.next(); 
				
				if (port.getBinding().getPortType() == portType) {
					f_found = true;
				}
			}
			
			if (!f_found) {
				ret = null;
			}
		}
		
		return(ret);
	}

}
