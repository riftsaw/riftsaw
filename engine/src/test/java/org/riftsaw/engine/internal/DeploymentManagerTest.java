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

import static org.junit.Assert.*;

import org.junit.Test;
import org.riftsaw.engine.DeploymentUnit;
import org.riftsaw.engine.internal.DeploymentManager;

public class DeploymentManagerTest {

    @Test
    public void testGetDeploymentUnitsExplodedSingle() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/exploded/single/deploy.xml");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile()).getParentFile();
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 1) {
                fail("Expecting 1 deployment unit, but got: "+dus.size());
            }
            
            DeploymentUnit du=dus.get(0);
            
            if (!du.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du.getDeploymentDescriptor().getParentFile().listFiles().length != 3) {
                fail("Deployment should contains 3 files");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess.bpel").exists()) {
                fail("MyProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testGetDeploymentUnitsExplodedSingleVersioned() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/exploded/single_versioned/deploy.xml");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile()).getParentFile();
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 1) {
                fail("Expecting 1 deployment unit, but got: "+dus.size());
            }
            
            DeploymentUnit du=dus.get(0);
            
            if (!du.getVersion().equals("2")) {
                fail("Version was not 2: "+du.getVersion());
            }
            
            if (!du.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du.getDeploymentDescriptor().getParentFile().listFiles().length != 3) {
                fail("Deployment should contains 3 files");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess-2.bpel").exists()) {
                fail("MyProcess-2.bpel does not exist");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testGetDeploymentUnitsArchivedSingle() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/archived/single.jar");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile());
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 1) {
                fail("Expecting 1 deployment unit, but got: "+dus.size());
            }
            
            DeploymentUnit du=dus.get(0);
            
            if (!du.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du.getDeploymentDescriptor().getParentFile().listFiles().length != 3) {
                fail("Deployment should contains 3 files");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess.bpel").exists()) {
                fail("MyProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testGetDeploymentUnitsArchivedSingleVersioned() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/archived/single_versioned.jar");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile());
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 1) {
                fail("Expecting 1 deployment unit, but got: "+dus.size());
            }
            
            DeploymentUnit du=dus.get(0);
            
            if (!du.getVersion().equals("2")) {
                fail("Version was not 2: "+du.getVersion());
            }
            
            if (!du.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du.getDeploymentDescriptor().getParentFile().listFiles().length != 3) {
                fail("Deployment should contains 3 files");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess-2.bpel").exists()) {
                fail("MyProcess-2.bpel does not exist");
            }
            
            if (!new java.io.File(du.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }
    
    @Test
    public void testGetDeploymentUnitsExplodedMultiple() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/exploded/multiple/deploy.xml");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile()).getParentFile();
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 2) {
                fail("Expecting 2 deployment units, but got: "+dus.size());
            }
            
            DeploymentUnit du1=dus.get(0);
            DeploymentUnit du2=dus.get(1);
            
            if (!du1.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du1.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.bpel").exists()) {
                fail("MyOtherProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

            if (!du2.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du2.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess.bpel").exists()) {
                fail("MyProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testGetDeploymentUnitsExplodedMultipleVersioned() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/exploded/multiple_versioned/deploy.xml");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile()).getParentFile();
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 2) {
                fail("Expecting 2 deployment units, but got: "+dus.size());
            }
            
            DeploymentUnit du1=dus.get(0);
            DeploymentUnit du2=dus.get(1);
            
            if (!du1.getVersion().equals("3")) {
                fail("Version of MyOtherProcess was not 3: "+du1.getVersion());
            }
            
            if (!du1.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du1.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess-3.bpel").exists()) {
                fail("MyOtherProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

            if (!du2.getVersion().equals("4")) {
                fail("Version of MyProcess was not 4: "+du2.getVersion());
            }
            
            if (!du2.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du2.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess-4.bpel").exists()) {
                fail("MyProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testGetDeploymentUnitsArchivedMultiple() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/archived/multiple.jar");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile());
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 2) {
                fail("Expecting 2 deployment units, but got: "+dus.size());
            }
            
            DeploymentUnit du1=dus.get(0);
            DeploymentUnit du2=dus.get(1);
            
            if (!du1.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du1.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.bpel").exists()) {
                fail("MyOtherProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

            if (!du2.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du2.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess.bpel").exists()) {
                fail("MyProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }

    @Test
    public void testGetDeploymentUnitsArchivedMultipleVersioned() {
        
        java.net.URL root=ClassLoader.getSystemResource("deployments/archived/multiple_versioned.jar");
        
        java.io.File deploymentRoot=new java.io.File(root.getFile());
        
        DeploymentManager dm=new DeploymentManager();
        dm.setTemporaryFolder(deploymentRoot.getParentFile().getParent()+
                java.io.File.separatorChar+"tmp");
        
        try {
            java.util.List<DeploymentUnit> dus=dm.getDeploymentUnits("test", deploymentRoot);
            
            if (dus.size() != 2) {
                fail("Expecting 2 deployment units, but got: "+dus.size());
            }
            
            DeploymentUnit du1=dus.get(0);
            DeploymentUnit du2=dus.get(1);
            
            if (!du1.getVersion().equals("3")) {
                fail("Version of MyOtherProcess was not 3: "+du1.getVersion());
            }
            
            if (!du1.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du1.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess-3.bpel").exists()) {
                fail("MyOtherProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du1.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

            if (!du2.getVersion().equals("4")) {
                fail("Version of MyProcess was not 4: "+du2.getVersion());
            }
            
            if (!du2.getDeploymentDescriptor().exists()) {
                fail("Deployment descriptor does not exist");
            }
            
            if (du2.getDeploymentDescriptor().getParentFile().listFiles().length != 4) {
                fail("Deployment should contains 4 files");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess-4.bpel").exists()) {
                fail("MyProcess.bpel does not exist");
            }
            
            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyProcess.wsdl").exists()) {
                fail("MyProcess.wsdl does not exist");
            }

            if (!new java.io.File(du2.getDeploymentDescriptor().getParentFile(), "MyOtherProcess.wsdl").exists()) {
                fail("MyOtherProcess.wsdl does not exist");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }

}
