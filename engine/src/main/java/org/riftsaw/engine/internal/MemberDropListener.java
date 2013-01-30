/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and others contributors as indicated
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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.scheduler.SchedulerDAOConnection;
import org.apache.ode.dao.scheduler.SchedulerDAOConnectionFactory;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;

/**
 * 
 * This listener will be invoked once a node drops from the cluster.
 *
 */
@Listener
public class MemberDropListener {
	
	private static Log logger = LogFactory.getLog(MemberDropListener.class);
	
	private SchedulerDAOConnectionFactory schedulerCF;
		
	private TransactionManager txm;
	
	public MemberDropListener(SchedulerDAOConnectionFactory factory, TransactionManager tm) {
		this.schedulerCF = factory;
		this.txm = tm;
	}
	
	@ViewChanged
    public void viewChanged(ViewChangedEvent event) {
        List<Address> old = new ArrayList<Address>(event.getOldMembers());
        List<Address> actives = new ArrayList<Address>(event.getCacheManager().getMembers());
        
        List<Address> dropped = new ArrayList<Address>();
        
        for (Address oldEntry : old) {
        	if (!actives.contains(oldEntry)) {
        		dropped.add(oldEntry);
        	}
        }
        
        logger.debug("dropped nodes =>  " + dropped + "; active node =>  " + event.getCacheManager().getMembers());
        if (dropped.size() > 0) {
    		try {
    			txm.begin();
    			SchedulerDAOConnection conn = schedulerCF.getConnection();
    			String activeNodeId = actives.iterator().next().toString();
    			for (Address addr : dropped) {
    				String deadNodeId = addr.toString();
    				int jobNum = conn.updateReassign(deadNodeId, activeNodeId);
    				logger.info("Moved " + jobNum + " jobs associated with [" + deadNodeId + "] to the node [" + activeNodeId + "]");
    			}
    			txm.commit();
    		} catch (Exception e) {
    			logger.error(e);
    			try {
    				txm.rollback();
    			} catch (Exception e1) {
    				logger.error(e1);
    			}
    		}
        }
        
    }
}
