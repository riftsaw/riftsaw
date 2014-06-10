/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and others contributors as indicated
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
package org.riftsaw.engine.osgi;

import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.ode.dao.jpa.hibernate.HibernateUtil;
import org.apache.ode.dao.jpa.hibernate.JpaOperatorImpl;
import org.apache.ode.dao.jpa.store.ConfStoreDAOConnectionImpl;
import org.apache.ode.dao.store.ConfStoreDAOConnection;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import org.apache.ode.il.config.OdeConfigProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * ConfStoreDAOConnectionFactory which delegates to OSGi PersistenceProvider to
 * acquire EntityManagerFactory.
 */
public class ConfStoreDAOConnectionFactoryImpl implements ConfStoreDAOConnectionFactory {

    private EntityManagerFactory _emf;
    private TransactionManager _txm;
    private DataSource _ds;
    private JpaOperator _operator = new JpaOperatorImpl();

    @Override
    public void init(Properties odeConfig, TransactionManager txm, Object env) {
        _txm = txm;
        _ds = (DataSource) env;

        Bundle thisBundle = FrameworkUtil.getBundle(getClass());
        BundleContext context = thisBundle.getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(PersistenceProvider.class.getName());
        PersistenceProvider persistenceProvider = (PersistenceProvider) context.getService(serviceReference);

        Map<?, ?> emfProperties = HibernateUtil.buildConfig(OdeConfigProperties.PROP_DAOCF_STORE + ".", odeConfig, _txm, _ds);
        _emf = persistenceProvider.createEntityManagerFactory("ode-store", emfProperties);
    }

    @Override
    public ConfStoreDAOConnection getConnection() {
        final ThreadLocal<ConfStoreDAOConnectionImpl> currentConnection = ConfStoreDAOConnectionImpl.getThreadLocal();

        ConfStoreDAOConnectionImpl conn = (ConfStoreDAOConnectionImpl) currentConnection.get();
        if (conn != null && HibernateUtil.isOpen(conn)) {
            return conn;
        } else {
            EntityManager em = _emf.createEntityManager();
            conn = new ConfStoreDAOConnectionImpl(em, _txm, _operator);
            currentConnection.set(conn);
            return conn;
        }
    }

    @Override
    public void shutdown() {
        _emf.close();
    }

}
