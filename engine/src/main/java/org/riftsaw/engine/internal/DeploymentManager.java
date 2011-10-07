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

import java.io.File;
import java.util.Collections;
import java.util.jar.JarEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.riftsaw.engine.DeploymentUnit;

/**
 * This class is responsible for managing the deployment of
 * a (set of) BPEL process(es).
 *
 */
public class DeploymentManager {
    
    private static final Log LOG=LogFactory.getLog(DeploymentManager.class);
    
    private String _tmpFolder=System.getProperty("java.io.tmpdir");

    /**
     * The default constructor.
     */
    public DeploymentManager() {
    }
    
    /**
     * This method sets the location of the temporary folder used
     * to expand the BPEL deployments.
     * 
     * @param tmp The temporary folder location
     */
    public void setTemporaryFolder(String tmp) {
        if (tmp == null) {
            _tmpFolder = System.getProperty("java.io.tmpdir");
        } else {
            _tmpFolder = tmp;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Set temporary folder to: "+_tmpFolder);
        }
    }
    
    /**
     * This method returns the location of the temporary folder used
     * to expand the BPEL deployments.
     * 
     * @return The temporary folder location
     */
    public String getTemporaryFolder() {
        return (_tmpFolder);
    }
    
    /**
     * This method creates a list of deployment units representing the distinct
     * deployments that are associated with the source BPEL processes provided
     * in the deployment root. Distinct deployment units may be required if
     * (for example) multiple versions of the same BPEL process are defined.
     * 
     * @param deploymentName The overall deployment name
     * @param deploymentRoot The root location containing the deployment descriptor
     *                  and associated artifacts (bpel, wsdl, xsd, etc).
     * @return The list of distinct BPEL deployment units
     * @throws Exception Failed to retrieve deployment units
     */
    public java.util.List<DeploymentUnit> getDeploymentUnits(String deploymentName,
                        java.io.File deploymentRoot) throws Exception {
        java.util.List<DeploymentUnit> ret=new java.util.Vector<DeploymentUnit>();
        
        if (deploymentRoot == null || !deploymentRoot.exists()) {
            throw new java.lang.IllegalArgumentException(
                        "Deployment root was not supplied, or does not exist");
        }
        
        // Check if deployment root is an archive
        boolean exploded=false;
        
        if (deploymentRoot.isFile()) {
            deploymentRoot = explodeJar(deploymentName, deploymentRoot);
            exploded = true;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deploy is exploded? "+exploded);
        }
        
        // Scan deployment for BPEL processes
        java.util.List<java.io.File> processes=getBPELProcesses(deploymentRoot);
        
        if (processes.size() == 1 && exploded) {
            java.io.File newDir=new java.io.File(deploymentRoot.getParentFile(),
                    getDeploymentUnitName(deploymentName, processes.get(0)));
            
            // If exists already, then delete
            if (newDir.exists()) {
                delete(newDir);
            }
            
            // Rename the deployment
            if (deploymentRoot.renameTo(newDir)) {
                ret.add(createDeploymentUnit(newDir));
            } else {
                // Log error and try to copy
                LOG.error("Unable to rename deployment at '"+deploymentRoot+"' to '"+newDir+"'");
                
                copy(deploymentRoot, newDir, processes.get(0).getName(),
                                getProcessLocalName(processes.get(0)));
                
                ret.add(createDeploymentUnit(newDir));
            }

        } else if (processes.size() > 0) {
            
            for (java.io.File bpel : processes) {
                java.io.File newDir=getTemporaryFolder(getDeploymentUnitName(deploymentName, bpel));
                
                // If exists already, then delete
                if (newDir.exists()) {
                    delete(newDir);
                }
                
                copy(deploymentRoot, newDir, bpel.getName(), 
                                getProcessLocalName(bpel));
                
                ret.add(createDeploymentUnit(newDir));
            }
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get deployment units for name '"+deploymentName+
                            "' and root '"+deploymentRoot+" = "+ret);
        }
        
        return (ret);
    }
    
    /**
     * This method returns the local name of the BPEL process contained
     * in the supplied file.
     * 
     * @param bpel The BPEL process file
     * @return The process's local name
     * @throws Exception Failed to get local name
     */
    protected String getProcessLocalName(java.io.File bpel) throws Exception {
        String ret=null;
        java.io.InputStream is=new java.io.FileInputStream(bpel);

        byte[] b=new byte[is.available()];
        is.read(b);
        
        is.close();

        org.w3c.dom.Element proc=DOMUtils.stringToDOM(b);
        
        ret = proc.getAttribute("name");
        
        return (ret);
    }
    
    /**
     * This method copies the contents of the 'from' folder
     * to the 'to' folder, filtering out all BPEL processes except
     * the named one.
     * 
     * @param fromDir The source folder 
     * @param toDir The destination folder
     * @param bpelFileName The BPEL process filename to retain
     * @throws Exception Failed to copy
     */
    protected void copy(java.io.File fromDir, java.io.File toDir, String bpelFileName, String processName)
                            throws Exception {
        
        // Check that destination folder exists
        if (!toDir.exists()) {
            toDir.mkdirs();
        }
        
        for (java.io.File f : fromDir.listFiles()) {
            if (f.isFile()) {
                if (!f.getName().endsWith(".bpel") || f.getName().equals(bpelFileName)) {
                    // Copy the file
                    java.io.InputStream is=new java.io.FileInputStream(f);

                    java.io.FileOutputStream fos =
                            new java.io.FileOutputStream(new java.io.File(toDir, f.getName()));

                    byte[] b=new byte[is.available()];
                    is.read(b);
                    
                    if (f.getName().equals("deploy.xml")) {
                        b = filterDeploymentDescriptor(b, processName);
                    }

                    fos.write(b);

                    fos.flush();
                    fos.close();
                    is.close();
                }
            } else if (f.isDirectory()) {
                copy(f, new java.io.File(toDir, f.getName()), bpelFileName, processName);
            }
        }
    }
    
    protected byte[] filterDeploymentDescriptor(byte[] b, String processName)
                                    throws Exception {
        byte[] ret=b;
        org.w3c.dom.Element deploy=DOMUtils.stringToDOM(b);
        boolean changed=false;
        
        org.w3c.dom.NodeList nl=deploy.getElementsByTagName("process");
        
        for (int i=nl.getLength()-1; i >= 0; i--) {
            if (nl.item(i) instanceof org.w3c.dom.Element) {
                org.w3c.dom.Element proc=(org.w3c.dom.Element)nl.item(i);
                
                String name=proc.getAttribute("name");
                
                // Check if has prefix and remove
                int index=name.indexOf(':');
                
                if (index != -1) {
                    name = name.substring(index+1);
                }
                
                if (!name.equals(processName)) {
                    // Remove element
                    proc.getParentNode().removeChild(proc);
                    changed = true;
                }
            }
        }
        
        if (changed) {
            ret = DOMUtils.domToBytes(deploy);
        }
        
        return(ret);
    }
    
    /**
     * This method creates a deployment unit from the supplied root and its
     * contained deployment descriptor.
     * 
     * @param deploymentRoot The deployment root
     * @return The deployment unit
     */
    protected DeploymentUnit createDeploymentUnit(java.io.File deploymentRoot) {
        DeploymentUnit ret=null;
        
        // Locate the deployment descriptor
        java.io.File deployFile=new java.io.File(deploymentRoot, "deploy.xml");
        
        if (!deployFile.exists()) {
            throw new IllegalArgumentException("Supplied deployment root does not contain a deploy.xml file");
        }
        
        // Get version from root name
        String version="1";
        String name=deploymentRoot.getName();
        
        int index=name.lastIndexOf('-');
        
        if (index != -1) {
            version = name.substring(index+1);
            name = name.substring(0, index);
        }
        
        ret = new DeploymentUnit(name, version, deployFile.lastModified(),
                                deployFile);
        
        return (ret);
    }
    
    /**
     * This method returns the BPEL process's deployment folder name.
     * 
     * @param deploymentName The deployment name
     * @param process The BPEL Process file
     * @return The process's deployment folder name
     */
    protected String getDeploymentUnitName(String deploymentName, java.io.File process) {
        String processName=process.getName().substring(0, process.getName().length()-5);
        
        return (deploymentName+"_"+processName);
    }
    
    /**
     * This method provides a temporary directory associated with the
     * supplied deployment name.
     * 
     * @param deploymentName The deployment name
     * @return The temporary folder
     */
    protected java.io.File getTemporaryFolder(String deploymentName) {
        String destPath = _tmpFolder
                + java.io.File.separatorChar
                + "riftsaw"
                + java.io.File.separatorChar
                + deploymentName;
        return (new java.io.File(destPath));
    }

    /**
     * This method explodes the supplied jar into a temporary location
     * associated with the deployment name.
     * 
     * @param deploymentName The deployment name
     * @param deploymentRoot The deployment archive
     * @return The new deployment root
     * @throws java.io.IOException Failed to explode the jar
     */
    private java.io.File explodeJar(String deploymentName, java.io.File deploymentRoot)
                                        throws java.io.IOException {
        java.io.File ret = getTemporaryFolder(deploymentName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Exploring deployment name '"+deploymentName+"' at '"+deploymentRoot+"' to: "+ret);
        }
        
        // Recursive delete in case already exists
        delete(ret);

        ret.mkdirs();

        java.util.jar.JarFile jarFile =
                new java.util.jar.JarFile(deploymentRoot);
        java.util.Enumeration<JarEntry> iter =
                jarFile.entries();

        while (iter.hasMoreElements()) {
            JarEntry entry = iter.nextElement();

            String entryPath = ret.getAbsolutePath()
                    + java.io.File.separatorChar;
            entryPath += entry.getName();
            java.io.File entryFile =
                    new java.io.File(entryPath);

            if (entry.isDirectory()) {
                entryFile.mkdirs();
            } else {
                java.io.InputStream is =
                  jarFile.getInputStream(entry);

                java.io.FileOutputStream fos =
                    new java.io.FileOutputStream(entryFile);

                byte[] b =
                    new byte[is.available()];
                is.read(b);

                fos.write(b);

                fos.flush();
                fos.close();
                is.close();
            }
        }

        jarFile.close();
        
        return (ret);
    }

    /**
     * This method deletes the supplied file. If it
     * represents a directory, then the operation will
     * be performed recursively.
     *
     * @param file The file or directory to be deleted
     */
    private void delete(final java.io.File file) {
        if (file.isDirectory()) {
            for (java.io.File f : file.listFiles()) {
                delete(f);
            }
        }
        file.delete();
    }

    /**
     * This method returns the list of BPEL process files.
     * 
     * @param deploymentRoot The deployment root
     * @return The list of BPEL process files
     */
    protected java.util.List<java.io.File> getBPELProcesses(java.io.File deploymentRoot) {
        java.util.List<java.io.File> ret=new java.util.Vector<java.io.File>();
        
        if (!deploymentRoot.isDirectory()) {
            throw new IllegalArgumentException("Deployment root must be a directory");
        }
        
        findBPELProcesses(deploymentRoot, ret);
        
        // Sort files by name (and therefore version)
        Collections.sort(ret, new java.util.Comparator<java.io.File>() {

            public int compare(File o1, File o2) {
                return(o1.getName().compareTo(o2.getName()));
            }
            
        });
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found BPEL processes under '"+deploymentRoot+"' are: "+ret);
        }
        
        return (ret);
    }
    
    /**
     * This method recursively scans a directory structure to locate all of the
     * BPEL processes.
     * 
     * @param file The file/directory to check
     * @param processes The list of BPEL processes
     */
    protected void findBPELProcesses(java.io.File file, java.util.List<java.io.File> processes) {
        
        if (file.isDirectory()) {
            for (java.io.File f : file.listFiles()) {
                findBPELProcesses(f, processes);
            }
        } else if (file.getName().endsWith(".bpel")) {
            processes.add(file);
        }
    }
}
