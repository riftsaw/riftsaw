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
package org.riftsaw.engine;

/**
 * This class represents the BPEL deployment unit.
 *
 */
public class DeploymentUnit {
	
	private String m_name=null;
	private long m_lastModified=0;
	private java.io.File m_deploymentDescriptor=null;
	private String m_version=null;

	/**
	 * The constructor for the deployment unit.
	 * 
	 * @param name The unique name for the deployment unit
	 * @param version The version
	 * @param lastModified When the deployment unit was last modified
	 * @param deploymentDescriptor The deployment descriptor file
	 */
	public DeploymentUnit(String name, String version, long lastModified, java.io.File deploymentDescriptor) {
		m_name = name;
		m_version = version;
		m_lastModified = lastModified;
		m_deploymentDescriptor = deploymentDescriptor;
	}
	
	/**
	 * This method returns the name of the deployment unit.
	 * 
	 * @return The name
	 */
	public String getName() {
		return(m_name);
	}
	
	/**
	 * This method returns the name, modified with the
	 * version number if specified.
	 * 
	 * @return The versioned name
	 */
	public String getVersionedName() {
		if (getVersion() == null) {
			return(getName());
		}
		return(getName()+"-"+getVersion());
	}
	
	/**
	 * This method returns the optional version of the deployment unit.
	 * 
	 * @return The version
	 */
	public String getVersion() {
		return(m_version);
	}
	
	/**
	 * This method returns the last modified time associated with
	 * the deployment unit.
	 * 
	 * @return The last modified time
	 */
	public long getLastModified() {
		return(m_lastModified);
	}
	
	/**
	 * This method returns the deployment descriptor.
	 * 
	 * @return The deployment descriptor
	 */
	public java.io.File getDeploymentDescriptor() {
		return(m_deploymentDescriptor);
	}
	
	public String toString() {
		return("DeploymentUnit[name="+m_name+",version="+m_version+
					",lastModified="+m_lastModified+",descriptor="+
						m_deploymentDescriptor+"]");
	}
}
