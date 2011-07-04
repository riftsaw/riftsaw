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
 * @author gbrown
 *
 */
public class BPELDeploymentUnit {

	public static final String BPEL_DEPLOY_XML = "bpel-deploy.xml";
	public static final String DEPLOY_XML = "deploy.xml";

	/**
	 * The constructor for the deployment unit.
	 * 
	 * @param name The unique name for the deployment unit
	 * @param lastModified When the deployment unit was last modified
	 */
	public BPELDeploymentUnit(String name, long lastModified) {
		m_name = name;
		m_lastModified = lastModified;
		
		// Remove any .jar suffix
		if (m_name != null && m_name.endsWith(".jar")) {
			m_name = m_name.substring(0, m_name.length()-4);
		}
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
	
	/**
	 * This method sets the deployment descriptor for the BPEL module
	 * being deployed.
	 * 
	 * @param file The BPEL deployment descriptor
	 */
	public void setDeploymentDescriptor(java.io.File file) {
		m_deploymentDescriptor = file;
	}
	
	public String toString() {
		return("BPELDeploymentUnit[name="+m_name+",lastModified="+m_lastModified+",descriptor="+
						m_deploymentDescriptor+"]");
	}
	
	private String m_name=null;
	private long m_lastModified=0;
	private java.io.File m_deploymentDescriptor=null;
}
