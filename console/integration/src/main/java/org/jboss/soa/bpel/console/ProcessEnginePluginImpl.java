/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.soa.bpel.console;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.jboss.bpm.console.client.model.DeploymentRef;
import org.jboss.bpm.console.client.model.JobRef;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.server.plugin.ProcessEnginePlugin;
import org.riftsaw.engine.BPELEngine;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Adopts the notion of deployments to the ODE process management API
 */
public class ProcessEnginePluginImpl implements ProcessEnginePlugin
{
  private Log log = LogFactory.getLog(ProcessEnginePluginImpl.class);

  private BPELEngine engine;

  private ProcessMgmtImpl processManagement;

  public ProcessEnginePluginImpl()
  {
    try
    {
      InitialContext ctx = new InitialContext();
      engine = (BPELEngine)ctx.lookup(JNDINamingUtils.BPEL_ENGINE);
    }
    catch (NamingException e)
    {
      throw new RuntimeException("Failed to initialize BPEL engine");
    }

    this.processManagement = new ProcessMgmtImpl();
  }

  private BpelManagementFacade getMgmtFacade()
  {
    return (BpelManagementFacade)engine.getManagementInterface();
  }
  
  public List<DeploymentRef> getDeployments()
  {

    // we are abusing the process definitions here and turn them
    // into the concept of "Deployment"

    List<DeploymentRef> deployments = new ArrayList<DeploymentRef>();
    List<ProcessDefinitionRef> definitions = processManagement.getProcessDefinitions();
    for(ProcessDefinitionRef def : definitions)
    {
      DeploymentRef dref = new DeploymentRef(
          def.getId(),def.isSuspended()
      );
      dref.setName(ModelAdaptor.decodeId(def.getId()));
      dref.getDefinitions().add(def.getName());
      deployments.add(dref);
    }
    return deployments;
  }

  public void deleteDeployment(String s)
  {
    throw new IllegalArgumentException("Not implemented");
  }

  public void suspendDeployment(String definitionId, boolean b)
  {
    String actualId = ModelAdaptor.decodeId(definitionId);

    try
    {
      getMgmtFacade().setRetired(QName.valueOf(actualId), b);
    }
    catch (Exception e)
    {
      log.warn("Error on suspending process "+ actualId, e);
    }
  }

  public List<JobRef> getJobs()
  {
    throw new IllegalArgumentException("Not implemented");
  }

  public void executeJob(String s)
  {
    throw new IllegalArgumentException("Not implemented"); 
  }
}
