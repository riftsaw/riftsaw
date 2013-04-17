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

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.ValueManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
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
	             final BindListener listener = new BindListener();
	             binderService.getManagedObjectInjector().inject(new ValueManagedReferenceFactory(new ImmediateValue<Object>(object)));
	             // creates the service builder with dep to the parent jndi context
	             ServiceBuilder<?> builder = serviceContainer.addService(bindInfo.getBinderServiceName(), binderService)
	               .addDependency(bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
	               .setInitialMode(ServiceController.Mode.ACTIVE)
	               .addListener(listener);
	             
	             builder.install();
	             listener.await();
	             binderService.acquire();
    
	         }catch (Throwable e) {
	        	 final NamingException ne = new NamingException("Failed to bind "+ object + " at location " + name);
	             ne.setRootCause(e);
	             LOG.error(ne);
	         } 
    	 }
     }

     public static void unbindFromJndi(String name){
    	 ServiceContainer serviceContainer = CurrentServiceContainer.getServiceContainer();
         if (serviceContainer != null) {
        	 
        	 final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(name);
        	 final ServiceController<?> controller = serviceContainer.getService(bindInfo.getBinderServiceName());
        	 final UnbindListener listener = new UnbindListener();
             controller.addListener(listener);
             
             try {
                 // when added, the listener stops the binding service
                 listener.await();
             } catch (Exception e) {
                 LOG.error("Failed to unbind [" + name + "]", e);
             }
         }
     }
     
     private static class BindListener extends AbstractServiceListener<Object> {
         private Exception exception;
         private boolean complete;

         public synchronized void transition(ServiceController<? extends Object> serviceController, ServiceController.Transition transition) {
             switch (transition) {
                 case STARTING_to_UP: {
                     complete = true;
                     notifyAll();
                     break;
                 }
                 case STARTING_to_START_FAILED: {
                     complete = true;
                     exception = serviceController.getStartException();
                     notifyAll();
                     break;
                 }
                 default:
                     break;
             }
         }

         public synchronized void await() throws Exception {
             while(!complete) {
                 wait();
             }
             if (exception != null) {
                 throw exception;
             }
         }
     }

     private static class UnbindListener extends AbstractServiceListener<Object> {
         private boolean complete;

         public void listenerAdded(ServiceController<?> controller) {
             controller.setMode(ServiceController.Mode.REMOVE);
         }

         public synchronized void transition(ServiceController<? extends Object> serviceController, ServiceController.Transition transition) {
             switch (transition) {
                 case REMOVING_to_REMOVED: {
                     complete = true;
                     notifyAll();
                     break;
                 }
                 default:
                     break;
             }
         }

         public synchronized void await() throws Exception {
             while(!complete) {
                 wait();
             }
         }
     }

}
