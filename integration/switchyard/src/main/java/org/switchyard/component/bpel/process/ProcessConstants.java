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
package org.switchyard.component.bpel.process;

import javax.xml.namespace.QName;

/**
 * Various constants and context variables.
 *
 */
public final class ProcessConstants {

    /**
     * The default process namespace.
     */
    public static final String PROCESS_NAMESPACE = "urn:switchyard-component-bpel:process:1.0";

    /** processDescriptor . */
    public static final String PROCESS_DESCRIPTOR = "processDescriptor";
    /** {urn:switchyard-component-bpel:process:1.0}processDescriptor . */
    public static final String PROCESS_DESCRIPTOR_VAR = new QName(PROCESS_NAMESPACE, PROCESS_DESCRIPTOR).toString();

    /** version . */
    public static final String VERSION = "version";
    /** {urn:switchyard-component-bpel:process:1.0}version . */
    public static final String VERSION_VAR = new QName(PROCESS_NAMESPACE, VERSION).toString();

    /** serviceName . */
    public static final String SERVICE_NAME = "serviceName";
    /** {urn:switchyard-component-bpel:process:1.0}serviceName . */
    public static final String SERVICE_NAME_VAR = new QName(PROCESS_NAMESPACE, SERVICE_NAME).toString();

    /** processDescriptor . */
    public static final String PORT_NAME = "portName";
    /** {urn:switchyard-component-bpel:process:1.0}processDescriptor . */
    public static final String PORT_NAME_VAR = new QName(PROCESS_NAMESPACE, PROCESS_DESCRIPTOR).toString();

    private ProcessConstants() {}

}
