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
 * This class represents a fault that may be returned from
 * an operation performed on a BPEL process or an external
 * service.
 *
 */
public class Fault extends Exception {

    private static final long serialVersionUID = 1L;
    
    private QName _faultName=null;
    private Element _faultMessage=null;

    /**
     * This is the constructor for the fault.
     * 
     * @param faultName The fault name
     * @param faultMessage The fault message
     */
    public Fault(QName faultName, Element faultMessage) {
        _faultName = faultName;
        _faultMessage = faultMessage;
    }
    
    /**
     * This method returns the fault name.
     * 
     * @return The fault name
     */
    public QName getFaultName() {
        return (_faultName);
    }
    
    /**
     * This method returns the fault message.
     * 
     * @return The fault message
     */
    public Element getFaultMessage() {
        return (_faultMessage);
    }
}
