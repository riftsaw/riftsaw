/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and others contributors as indicated
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
package org.apache.ode.store;

import java.util.ArrayList;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.iapi.CacheProvider;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.dao.store.ConfStoreDAOConnection;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import org.apache.ode.dao.store.DeploymentUnitDAO;
import org.riftsaw.engine.DeploymentUnit;

public class RiftSawProcessStore extends ProcessStoreImpl {

	private static final Log __log = LogFactory.getLog(RiftSawProcessStore.class);

	public RiftSawProcessStore(EndpointReferenceContext eprContext, TransactionManager txm,
							   ConfStoreDAOConnectionFactory cf, CacheProvider cacheProvider) {
		super(eprContext, txm, cf, cacheProvider);
	}

	public void loadAll() {
	}

	public void deploy(final DeploymentUnit bdu) {
		__log.debug("Deploy "+bdu);
		
		doDeploy(bdu);
	}

	public void undeploy(DeploymentUnit bdu) {
		__log.debug("Undeploy "+bdu);
		
		undeploy(bdu.getVersionedName());
	}

	protected void doDeploy(final DeploymentUnit bdu) {
		__log.debug("Deploy scheduled: "+bdu.getDeploymentDescriptor().getParentFile());

		/* 
		 * NOTE: When using this approach to deployment, where the
		 * BPEL deployment is redeployed each time the server is started,
		 * it is necessary to 'disable' checking of the GUID associated
		 * with the process definitions - otherwise ODE will detect the
		 * different GUID, and remove the process definition and instances.
		 * This can be achieved using the 'ode.process.checkguid' boolean
		 * property.
		 */
		final ArrayList<ProcessConfImpl> loaded = new ArrayList<ProcessConfImpl>();

		// Check for the deployment unit associated with the name
		boolean deploy=exec(new ProcessStoreImpl.Callable<Boolean>() {
			public Boolean call(ConfStoreDAOConnection conn) {
				boolean ret=false;
				DeploymentUnitDAO dudao = conn.getDeploymentUnit(bdu.getVersionedName());
				if (dudao == null)
					return true;

				try {
					String dir=bdu.getDeploymentDescriptor().getParentFile().getCanonicalPath();

					if (dudao.getDeploymentUnitDir() != null &&
							dudao.getDeploymentUnitDir().equals(dir) == false) {
						__log.debug("Updating deployunit directory from: "+dudao.getDeploymentUnitDir()+" to: "+dir);
						dudao.setDeploymentUnitDir(dir);
					}

					// Check if process has changed, and needs to be redeployed
					if (bdu.getLastModified() > dudao.getDeployDate().getTime()) {
						ret = true;
					} else {
						__log.debug("Re-compiling: "+bdu.getDeploymentDescriptor().getParentFile());

						DeploymentUnitDir du=new DeploymentUnitDir(bdu.getDeploymentDescriptor().getParentFile());
						
						du.setName(bdu.getVersionedName());

						// Create the DU and compile it before acquiring lock.
						//du.setExtensionValidators(getExtensionValidators());
						try {
							du.compile();
						} catch (CompilationException ce) {
							String errmsg = "Failed to compile deployment unit '"+
									bdu.getDeploymentDescriptor().getParentFile()+"'";
							__log.error(errmsg, ce);
							throw new ContextException(errmsg, ce);
						}

						loaded.addAll(load(dudao));
					}

				} catch(Throwable e) {
					__log.error("Failed to update deployment unit dir", e);
				}

				return ret;
			}
		});

		if (deploy) {
			__log.debug("Deploy new version: "+bdu.getDeploymentDescriptor().getParentFile());

			deploy(bdu.getDeploymentDescriptor().getParentFile(), true, bdu.getVersionedName(), false);

		} else {
			__log.debug("Trigger Integration Layer to use existing version: "+bdu.getDeploymentDescriptor().getParentFile());

			// Just load and notify IL
			for (ProcessConfImpl p : loaded) {
				try {
					fireStateChange(p.getProcessId(), p.getState(), p.getDeploymentUnit().getName());
				} catch (Exception except) {
					__log.error("Error while activating process: pid=" + p.getProcessId() + " package="+p.getDeploymentUnit().getName(), except);
				}
			}
		}
	}

	/**
	 * This method is copied from the ODE ProcessStoreImpl, as its implementation is private.
	 * 
	 * @param processId
	 * @param state
	 * @param duname
	 */
	private void fireStateChange(QName processId, ProcessState state, String duname) {
		switch (state) {
		case ACTIVE:
			fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTVIATED, processId, duname));
			break;
		case DISABLED:
			fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DISABLED, processId, duname));
			break;
		case RETIRED:
			fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.RETIRED, processId, duname));
			break;
		}
	}
}
