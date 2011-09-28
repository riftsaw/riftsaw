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
package org.jboss.soa.bpel.console.bpaf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.soa.bpel.console.util.DefaultEMLocator;
import org.jboss.soa.bpel.console.util.EntityManagerLocator;

import javax.persistence.EntityManager;

/**
 * Plain JDBC batch persistence.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 21, 2010
 */
public final class JDBCPersistenceStrategy implements PersistenceStrategy {

    protected final Log log = LogFactory.getLog(JDBCPersistenceStrategy.class);

    private EntityManagerLocator defaultLocator = new DefaultEMLocator();

    public void start() {

    }

    public void stop() {

    }

    public void persist(Event event) {

        try {

            // get entity manager
            EntityManager entityManager = defaultLocator.locate();

            // It's managed by JACORB
            // Hence we don't deal with the TX here            
            entityManager.persist(event);

        } catch (Exception e) {
            log.error("Error persisting event", e);
        }

    }
}
