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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.pmapi.InstanceInfoListDocument;
import org.apache.ode.bpel.pmapi.ProcessInfoListDocument;
import org.apache.ode.bpel.pmapi.TInstanceInfo;
import org.apache.ode.bpel.pmapi.TProcessInfo;
import org.apache.ode.bpel.pmapi.TProcessStatus;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.soa.bpel.console.util.Base64;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class ModelAdaptor
{
  public static List<ProcessDefinitionRef> adoptDefinitions(ProcessInfoListDocument processInfoListDocument)
  {
    List<ProcessDefinitionRef> results = new ArrayList<ProcessDefinitionRef>();

    for(TProcessInfo pi : processInfoListDocument.getProcessInfoList().getProcessInfoList())
    {
      QName processQName = pi.getDefinitionInfo().getProcessName();
      
      ProcessDefinitionRef def = new ProcessDefinitionRef(
          encodeId(pi.getPid()),
          processQName.toString(),
          pi.getVersion()
      );

      def.setPackageName(processQName.getNamespaceURI());
      def.setDeploymentId(pi.getDeploymentInfo().getPackage());
      def.setSuspended( pi.getStatus() == TProcessStatus.RETIRED );// Used in ProcessEnginePluginImpl

      results.add(def);
    }
    
    return results;
  }

  public static List<ProcessInstanceRef> adoptInstances(
      QName procesQName, InstanceInfoListDocument instanceListDocument)
  {
    List<ProcessInstanceRef> results = new ArrayList<ProcessInstanceRef>();

    for(TInstanceInfo i0 : instanceListDocument.getInstanceInfoList().getInstanceInfoList())
    {
      if(i0.getPid().equals(procesQName.toString()))
      {
        ProcessInstanceRef ref = new ProcessInstanceRef(
            i0.getIid(),
            encodeId(i0.getPid()),
            i0.getDtStarted().getTime(),
            null,
            false // see filter criteria when selecting instances
        );

        results.add(ref);
      }
    }
    
    return results;
  }

  public static String encodeId(String actualId)
  {
    return Base64.encodeBytes(actualId.getBytes());
  }

  public static String decodeId(String encodedId)
  {
    try
    {
      return new String(Base64.decode(encodedId));
    }
    catch (IOException e)
    {
      throw new RuntimeException("Failed to decode id", e);
    }
  }
  

}
