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
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.jboss.bpm.monitor.model.bpaf.Event;

import java.util.Properties;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 20, 2010
 */
public class BPAFLogAdapter implements BpelEventListener {

    protected final Log log = LogFactory.getLog(BPAFLogAdapter.class);    
    private PersistenceStrategy persistenceStrategy = null;

    public void onEvent(BpelEvent bpelEvent) {

        Event event = EventAdapter.createBPAFModel(bpelEvent);
        if(event!=null) //no mapping or not of interest
            persistenceStrategy.persist(event);
       
    }

    public void startup(Properties properties) {
        this.persistenceStrategy = new JDBCPersistenceStrategy();
        this.persistenceStrategy.start();
        log.info("Using: " + persistenceStrategy.getClass());
    }

    public void shutdown() {
        this.persistenceStrategy.stop();
        System.out.println("BPAFLogAdapter shutdown");        
    }   
}
