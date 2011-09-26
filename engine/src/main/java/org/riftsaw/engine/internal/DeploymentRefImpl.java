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
package org.riftsaw.engine.internal;

import org.riftsaw.engine.DeploymentRef;
import org.riftsaw.engine.DeploymentUnit;

/**
 * The deployment reference implementation.
 *
 */
public class DeploymentRefImpl implements DeploymentRef {

    private java.util.List<DeploymentUnit> _deploymentUnits=null;
    
    /**
     * This is the constructor.
     * 
     * @param deployments The list of deployments
     */
    public DeploymentRefImpl(java.util.List<DeploymentUnit> deployments) {
        _deploymentUnits = deployments;
    }
    
    /**
     * This method returns the list of deployment units
     * associated with the deployment.
     * 
     * @return The list of deployment units
     */
    public java.util.List<DeploymentUnit> getDeploymentUnits() {
        return(_deploymentUnits);
    }
}
