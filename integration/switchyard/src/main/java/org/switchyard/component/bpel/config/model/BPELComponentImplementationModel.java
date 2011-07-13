/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
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
package org.switchyard.component.bpel.config.model;

import org.switchyard.component.bpel.config.model.BPELComponentImplementationModel;
import org.switchyard.config.model.composite.ComponentImplementationModel;

/**
 * A "bpel" ComponentImplementationModel.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
public interface BPELComponentImplementationModel extends ComponentImplementationModel {

    /**
     * The "bpel" namespace.
     */
    public static final String DEFAULT_NAMESPACE = "urn:switchyard-component-bpel:config:1.0";

    /**
     * The "bpel" implementation type.
     */
    public static final String BPEL = "bpel";
    
    /**
     * Gets the "processDescriptor" attribute.
     *
     * @return the "processDescriptor" attribute
     */
    public String getProcessDescriptor();

    /**
     * Sets the "processDescriptor" attribute.
     *
     * @param processDescriptor the "processDescriptor" attribute
     * @return this instance (useful for chaining)
     */
    public BPELComponentImplementationModel setProcessDescriptor(String processDescriptor);

    /**
     * Gets the "version" attribute.
     *
     * @return the "version" attribute
     */
    public String getVersion();

    /**
     * Sets the "version" attribute.
     *
     * @param portName the "version" attribute
     * @return this instance (useful for chaining)
     */
    public BPELComponentImplementationModel setVersion(String version);

    /**
     * Gets the "serviceName" attribute.
     *
     * @return the "serviceName" attribute
     */
    public String getServiceName();

    /**
     * Sets the "serviceName" attribute.
     *
     * @param serviceName the "serviceName" attribute
     * @return this instance (useful for chaining)
     */
    public BPELComponentImplementationModel setServiceName(String serviceName);

    /**
     * Gets the "portName" attribute.
     *
     * @return the "portName" attribute
     */
    public String getPortName();

    /**
     * Sets the "portName" attribute.
     *
     * @param portName the "portName" attribute
     * @return this instance (useful for chaining)
     */
    public BPELComponentImplementationModel setPortName(String portName);

    
}
