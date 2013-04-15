/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
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
package org.riftsaw.engine.jboss;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.ValueManagedReferenceFactory;
import org.jboss.as.naming.WritableServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author: Jeff Yu
 * @date: 1/02/12
 */
public class JndiRegistry {

     private static final Log LOG= LogFactory.getLog(JndiRegistry.class);
     
     public static void bindToJndi(String name, Object object) {
    	 ServiceContainer serviceContainer = CurrentServiceContainer.getServiceContainer();
    	 //Only register it in AS7 container.
    	 if (serviceContainer != null) { 
	    	 try {
	    		 // creates binder service
	             final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(name);
	             final BinderService binderService = new BinderService(bindInfo.getBindName());
	             binderService.getManagedObjectInjector().inject(new ValueManagedReferenceFactory(new ImmediateValue<Object>(object)));
	             // creates the service builder with dep to the parent jndi context
	             ServiceController<?> serviceController = serviceContainer.addService(bindInfo.getBinderServiceName(), binderService)
	               .addDependency(bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
	               .setInitialMode(ServiceController.Mode.ACTIVE)
	               .install();
    
	         }catch (Throwable e) {
	        	 final NamingException ne = new NamingException("Failed to bind "+ object + " at location " + name);
	             ne.setRootCause(e);
	         } 
    	 }
     }

     public static void unbindFromJndi(String name){
         ServiceTarget serviceTarget = CurrentServiceContainer.getServiceContainer();
         if (serviceTarget != null) { 
             try {
                 InitialContext context = new InitialContext();
                 context.unbind(name);
             } catch (NamingException e) {
                 LOG.error("Error in unbinding the object from JNDI.", e);
             } finally {
            	 WritableServiceBasedNamingStore.popOwner();
             }
         }
     }

}
