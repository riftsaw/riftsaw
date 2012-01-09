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

import org.apache.ode.bpel.pmapi.*;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.server.integration.ProcessManagement;
import org.jboss.soa.bpel.console.json.XmlToJson;
import org.riftsaw.engine.BPELEngine;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class ProcessMgmtImpl implements ProcessManagement
{

  private BPELEngine engine;
  
  public ProcessMgmtImpl()
  {
    try
    {
      InitialContext ctx = new InitialContext();
      engine = (BPELEngine)ctx.lookup("java:global/BPELEngine");
    }
    catch (NamingException e)
    {
      throw new RuntimeException("Failed to initialize BPEL engine");
    }
  }

  private BpelManagementFacade getMgmtFacade()
  {
    return (BpelManagementFacade)engine.getManagementInterface();
  }

  public List<ProcessDefinitionRef> getProcessDefinitions()
  {
    return ModelAdaptor.adoptDefinitions(getMgmtFacade().listAllProcesses());
  }

  public ProcessDefinitionRef getProcessDefinition(String definitionId)
  {
    throw new RuntimeException("Not implemented");
  }

  public List<ProcessDefinitionRef> removeProcessDefinition(String definitionId)
  {
    throw new RuntimeException("Not implemented");
  }

  public List<ProcessInstanceRef> getProcessInstances(String definitionId)
  {
    String actualId = ModelAdaptor.decodeId(definitionId);
    QName processQName = QName.valueOf(actualId);
    
    //TODO: processQName.getLocalPart() includes the version number.
    String instanceName = processQName.getLocalPart().substring(0, processQName.getLocalPart().indexOf("-"));
    InstanceInfoListDocument result =
        getMgmtFacade().listInstances(                         // filter, order, limit
            "name="+ instanceName +" namespace=" + processQName.getNamespaceURI()+" status=active", "name", 1000);


    return ModelAdaptor.adoptInstances(processQName, result);    
  }

  public ProcessInstanceRef getProcessInstance(String instanceId)
  {
    throw new RuntimeException("Not implemented");
  }

  public ProcessInstanceRef newInstance(String defintionId)
  {
   throw new RuntimeException("Not implemented");
  }

  public ProcessInstanceRef newInstance(String definitionId, Map<String, Object> processVars)
  {
    throw new RuntimeException("Not implemented");
  }

  public Map<String, Object> getInstanceData(String instanceId)
  {
    Map<String,Object> variables = new HashMap<String,Object>();

    String iid = instanceId;//ModelAdaptor.decodeId(instanceId);  // what a crappy API...

    // get root scope id
    BpelManagementFacade mgmtFacade = getMgmtFacade();
    TInstanceInfo instanceInfo = mgmtFacade.getInstanceInfo(Long.valueOf(iid)).getInstanceInfo();
    String scopeId = instanceInfo.getRootScope().getSiid();

    // get scope info
    TScopeInfo scopeInfo = mgmtFacade.getScopeInfo(scopeId).getScopeInfo();
    List<TVariableRef> varRefs = scopeInfo.getVariables().getVariableRefList();

    for(TVariableRef varRef : varRefs)
    {
      String varName = varRef.getName();
      TVariableInfo varInfo = mgmtFacade.getVariableInfo(scopeId, varName).getVariableInfo();
      Object obj = varInfo.getValue();  // actually an xml representation
      variables.put(varName,           // cheap tricks
          XmlToJson.parse(
              new ByteArrayInputStream(obj.toString().getBytes()
              )
          )
      ); 
    }

    return variables;
  }

  public void setInstanceData(String instanceId, Map<String, Object> data)
  {
    throw new RuntimeException("Not implemented");
  }

  public void endInstance(String instanceId, ProcessInstanceRef.RESULT result)
  {    
	try {
		String iid = instanceId;//ModelAdaptor.decodeId(instanceId);
		getMgmtFacade().terminate(Long.valueOf(iid));
	} catch(org.apache.ode.bpel.pmapi.ProcessNotFoundException ex) {
		// Silently ignore
	}
  }

  public void deleteInstance(String instanceId)
  {
    throw new RuntimeException("Not implemented");
  }

  public void setProcessState(String instanceId, ProcessInstanceRef.STATE nextState)
  {
    throw new RuntimeException("Not implemented");
  }

  public void signalExecution(String executionId, String signal)
  {
    throw new RuntimeException("Not implemented");  
  } 
}
