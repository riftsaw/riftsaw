/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.soa.bpel.console.util;

import org.apache.ode.dao.jpa.bpel.BpelDAOConnectionImpl;

import javax.persistence.EntityManager;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 22, 2010
 */
public class DefaultEMLocator implements EntityManagerLocator {
    public EntityManager locate() {

        final ThreadLocal<BpelDAOConnectionImpl> currentConnection = BpelDAOConnectionImpl.getThreadLocal();
        BpelDAOConnectionImpl bpelDAOConnection = currentConnection.get();
        if(null==bpelDAOConnection)
            throw new IllegalStateException("Unabled to locate BpelDAOConnectionImpl from ThreadLocal");

        return bpelDAOConnection.getEntityManager();
        
    }
}
