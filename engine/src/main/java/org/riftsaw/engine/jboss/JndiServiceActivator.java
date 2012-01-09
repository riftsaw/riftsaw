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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.NamingStore;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;

import java.util.logging.Logger;

/**
 *
 * //TODO: This is just a hack for the AS7.0.2, as they are a better way of doing so in AS7.1.0
 *
 * @author: Jeff Yu
 * @date: 9/01/12
 */
public class JndiServiceActivator {

    private static final Log logger = LogFactory.getLog(JndiServiceActivator.class);

    private static ServiceTarget serviceTarget;

    /**
     * Managed references are what is actually used to retrieve a value binding.
     *
     * Release will be called when the AS is done with the binding, however in the case of programatic
     * lookups this will not be called.
     *
     */
    private static class ObjectManagedReferenceFactory implements ManagedReferenceFactory {

        private final Object value;

        public ObjectManagedReferenceFactory(final Object value) {
            this.value = value;
        }

        public ManagedReference getReference() {

            return new ManagedReference() {
                public void release() {

                }

                public Object getInstance() {
                    return value;
                }
            };
        }
    }

    public static void registerToJndi(String name, Object bpelEngine) {
        serviceTarget = org.jboss.as.server.CurrentServiceContainer.getServiceContainer();
        if (serviceTarget != null) {
            final ServiceName bindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME.append(name);
            final BinderService binderService = new BinderService(name);
            ServiceBuilder<ManagedReferenceFactory> builder = serviceTarget.addService(bindingServiceName, binderService);
            builder.addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector());
            binderService.getManagedObjectInjector().inject(new ObjectManagedReferenceFactory(bpelEngine));
            builder.install();
        }
    }
}
