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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * This interface represents the BPEL engine, used to deploy
 * process definitions and invoke BPEL process instances.
 *
 */
public interface BPELEngine {
    
    /**
     * This method initializes the BPEL engine.
     * 
     * @param locator The service locator
     * @param props The properties
     * @throws Exception Failed to initialize
     */
    public void init(ServiceLocator locator, java.util.Properties props) throws Exception;
    
    /**
     * This method returns the service locator
     * associated with the BPEL engine.
     * 
     * @return The service locator
     */
    public ServiceLocator getServiceLocator();
    
    /**
     * This method deploys a BPEL process definition.
     * 
     * @param bdu The BPEL process definition
     * @deprecated Use deploy(String,java.io.File) instead
     */
    public void deploy(DeploymentUnit bdu);

    /**
     * This method deploys a BPEL process definition. The
     * deployment can be provided as an archive or folder.
     * The name of the deployment will be taken from the
     * archive or folder name. The archive or folder must
     * contain the deployment descriptor at its root.
     * 
     * @param deployment The BPEL process archive or folder
     * @return The reference for the deployment, to be used
     *              during undeployment
     * @deprecated Use deploy(String,java.io.File) instead
     */
    public DeploymentRef deploy(java.io.File deployment);

    /**
     * This method deploys a BPEL process definition. The
     * deployment can be provided as an archive or folder.
     * The name of the deployment will be taken from the
     * archive or folder name. The archive or folder must
     * contain the deployment descriptor at its root.
     * 
     * @param name The deployment name
     * @param deployment The BPEL process archive or folder
     * @return The reference for the deployment, to be used
     *              during undeployment
     */
    public DeploymentRef deploy(String name, java.io.File deployment);

    /**
     * This method invokes a BPEL process instance. If a process
     * does not exist, one will be created. If the message is
     * for an existing process, it will be routed to the appropriate
     * instance based on correlaton information within the
     * message contents or header values.
     * 
     * @param serviceName The service name
     * @param portName The port name
     * @param operationName The operation name
     * @param mesg The multipart message
     * @param headers The optional header values
     * @return The response, or null if a one-way request
     * @throws Exception Failed to invoke operation
     */
    public Element invoke(QName serviceName, String portName, String operationName, Element mesg,
                        java.util.Map<String, Object> headers) throws Exception;
    
    /**
     * This method undeploys a BPEL process definition.
     * 
     * @param bdu The BPEL process definition
     * @deprecated
     */
    public void undeploy(DeploymentUnit bdu);
    
    /**
     * This method undeploys the BPEL process definitions associated
     * with the supplied deployment reference.
     * 
     * @param ref The deployment reference
     */
    public void undeploy(DeploymentRef ref);
    
    /**
     * This method closes the BPEL engine.
     * 
     * @throws Exception Failed to close
     */
    public void close() throws Exception;
    
}
