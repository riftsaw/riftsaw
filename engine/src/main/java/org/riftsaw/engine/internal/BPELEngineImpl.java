/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009-11, Red Hat Middleware LLC, and others contributors as indicated
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

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.evt.DebugBpelEventListener;
import org.apache.ode.bpel.engine.BpelManagementFacadeImpl;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.CountLRUDehydrationPolicy;
import org.apache.ode.bpel.engine.cron.CronScheduler;
import org.apache.ode.bpel.extvar.jdbc.JdbcExternalVariableModule;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.CacheProvider;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;
import org.apache.ode.dao.scheduler.SchedulerDAOConnectionFactory;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import org.apache.ode.il.cache.CacheProviderFactory;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.scheduler.simple.SimpleScheduler;
import org.apache.ode.store.RiftSawProcessStore;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.Properties;
import org.infinispan.manager.EmbeddedCacheManager;
import org.riftsaw.engine.BPELEngine;
import org.riftsaw.engine.DeploymentRef;
import org.riftsaw.engine.DeploymentUnit;
import org.riftsaw.engine.Fault;
import org.riftsaw.engine.ServiceLocator;
import org.riftsaw.engine.jboss.JndiRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This class provides an ODE based implementation of the BPEL engine interface.
 *
 */
public class BPELEngineImpl implements BPELEngine {
    
    private static final Log LOG=LogFactory.getLog(BPELEngineImpl.class);

    private static final String _jndiName = "java:jboss/BPELEngine";
    private static final String _emfJndiName = "java:jboss/BPELEMFactory";
    
    private static final String DEPLOYMENT_FOLDER_ENV_VAR = "jboss.server.temp.dir";

    private BpelServerImpl _bpelServer;
    private RiftSawProcessStore _store;
    private OdeConfigProperties _odeConfig;
    private TransactionManager _txMgr;
    private BpelDAOConnectionFactory _daoCF;
    private ConfStoreDAOConnectionFactory _storeCF;
    private SchedulerDAOConnectionFactory _schedulerDaoCF;
    private Scheduler _scheduler;
    private Database _db;
    private ExecutorService _executorService;
    private CronScheduler _cronScheduler;
    private CacheProvider _cacheProvider;
    private org.infinispan.Cache<String, String> _cache;
    private ServiceLocator _serviceLocator;
    private DeploymentManager _deploymentManager;

    /**
     * {@inheritDoc}
     */
    public void init(ServiceLocator locator, java.util.Properties props) throws Exception {

        if (props == null) {
            props = new java.util.Properties();
        }

        LOG.info("ODE PROPS="+props);

        _odeConfig = new OdeConfigProperties(props, "bpel.");
        _serviceLocator = locator;

        LOG.info("Initializing transaction manager");
        initTxMgr();
        
        LOG.info("Creating data source.");
        initDataSource();
        
        populateDatabaseSchema();
        
        LOG.info("Starting DAO.");
        initDAO();
        
        LOG.info("Initializing deployment manager");
        initDeploymentManager();
        
        EndpointReferenceContextImpl eprContext = new EndpointReferenceContextImpl(this);
        
        initCacheProvider();
        
        LOG.info("Initializing BPEL process store.");
        initProcessStore(eprContext);
        
        LOG.info("Initializing UDDI registration");
        //initUDDIRegistration();
        
        LOG.info("Initializing BPEL server.");
        initBpelServer(eprContext, locator);

        _store.loadAll();

        // Register BPEL event listeners configured in axis2.properties file.
        registerEventListeners();
        registerMexInterceptors();

        //registerExtensionActivityBundles();

        registerExternalVariableModules();

        try {
            _bpelServer.start();
        } catch (Exception ex) {
            String errmsg = "SERVER START FAILED";
            LOG.error(errmsg, ex);
        }

        LOG.info("Starting scheduler");
        _scheduler.start();

        RegisterServicesIntoJNDI();
    }
    
    
	private void populateDatabaseSchema() throws Exception {
		//populate the schema only if the db.emb.create=false in the bpel.properties
		if (!Boolean.valueOf(_odeConfig.getProperty(OdeConfigProperties.PROP_DB_EMBEDDED_CREATE, "false"))) {
			String dialect = _odeConfig.getProperties().getProperty("hibernate.dialect");
			DatabaseInitialiser dbInitialiser = new DatabaseInitialiser(_db.getDataSource(), _txMgr, dialect);
	        dbInitialiser.initDatabase();			
		}
	}

    private void RegisterServicesIntoJNDI() {
        LOG.info("Register BPEL engine, EntityManagerFactory into JNDI.");
        JndiRegistry.bindToJndi(_jndiName, this);
        // hack to expose the EntityManagerFactory.
        // See org.apache.ode.dao.jpa.hibernate.BpelDAOConnectionFactoryImpl as well.
        Object emf = _odeConfig.getProperties().get("ode.emf");
        if (emf != null) {
           JndiRegistry.bindToJndi(_emfJndiName, emf);
        }
    }

    private void unregisterServicesFromJNDI() {
        LOG.info("Unbind the services from JNDI.");
        
        try {
            JndiRegistry.unbindFromJndi(_jndiName);
            JndiRegistry.unbindFromJndi(_emfJndiName);
        } catch (Throwable t) {
            LOG.debug("Failed to unbind services on engine close", t);
        }
    }

    /**
     * This method returns the service locator
     * associated with the BPEL engine.
     * 
     * @return The service locator
     */
    public ServiceLocator getServiceLocator() {
        return (_serviceLocator);
    }
    
    /**
     * Initialise the process store.
     * 
     * @param eprContext The endpoint reference context
     */
    protected void initProcessStore(EndpointReferenceContext eprContext) {
        _store = createProcessStore(eprContext, _txMgr, _storeCF);
        _store.registerListener(new ProcessStoreListenerImpl());
    }
    
    /**
     * Create the process store.
     * 
     * @param eprContext The endpoint reference context
     * @param txm The transaction manager
     * @param cf The connection factory
     * @return The process store
     */
    protected RiftSawProcessStore createProcessStore(EndpointReferenceContext eprContext, TransactionManager txm, ConfStoreDAOConnectionFactory cf) {
        return new RiftSawProcessStore(eprContext, txm, cf, _cacheProvider);
    }

    /**
     * This method initializes the cache provider.
     */
    private void initCacheProvider() {
        _cacheProvider = CacheProviderFactory.getCacheProvider(_odeConfig);
        try {
            _cacheProvider.start(_odeConfig.getProperties());
        } catch (Exception e) {
            LOG.error("Error in starting cache provider", e);
            throw new RuntimeException("Error in initCacheProvider.", e);
        }
    }
    
    private void initDeploymentManager() {
        _deploymentManager = new DeploymentManager();
        
        String folder=_odeConfig.getProperty("deployment.folder");
        
        if (folder == null) {
            folder = System.getProperty(DEPLOYMENT_FOLDER_ENV_VAR);
        }

        if (folder != null) {
            _deploymentManager.setDeploymentFolder(folder);
        }
    }
    
    /**
     * This method initializes the data store.
     * 
     * @throws Exception Failed to initialize
     */
    private void initDataSource() throws Exception {
        _db = new Database(_odeConfig);
        _db.setTransactionManager(_txMgr);

        //_db.setWorkRoot(new java.io.File("/tmp/h2"));
        
        try {
            _db.start();
        } catch (Exception ex) {
            String errmsg = "FAILED TO INITIALISE DATA SOURCE";
            LOG.error(errmsg, ex);
            throw new Exception(errmsg, ex);
        }
    }

    /**
     * This method initializes the transaction manager.
     * 
     * @throws Exception Failed to initialize
     */
    private void initTxMgr() throws Exception {
        String txFactoryName = _odeConfig.getTxFactoryClass();
        LOG.info("Initializing transaction manager using " + txFactoryName);
        try {
            Class<?> txFactClass = this.getClass().getClassLoader().loadClass(txFactoryName);
            Object txFact = txFactClass.newInstance();
            _txMgr = (TransactionManager) txFactClass.getMethod("getTransactionManager", (Class[]) null).invoke(txFact);
            
            //if (__logTx.isDebugEnabled() && System.getProperty("ode.debug.tx") != null)
            //    _txMgr = new DebugTxMgr(_txMgr);
            //_axisConfig.addParameter("ode.transaction.manager", _txMgr);
        } catch (Exception e) {
            LOG.error("Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
            throw new Exception("Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
        }
    }

    /**
     * This method initializes DAO.
     * 
     * @throws Exception Failed to initialize
     */
    protected void initDAO() throws Exception {
        LOG.info("USING DAO: "+_odeConfig.getDAOConnectionFactory() + ", " + _odeConfig.getDAOConfStoreConnectionFactory()
                        + ", " + _odeConfig.getDAOConfScheduleConnectionFactory());
        try {
            _daoCF = _db.createDaoCF();
            _storeCF = _db.createDaoStoreCF();
            _schedulerDaoCF = _db.createDaoSchedulerCF();
        } catch (Exception ex) {
            String errmsg = "DAO INSTANTIATION FAILED: "+_odeConfig.getDAOConnectionFactory() + " , " + _odeConfig.getDAOConfStoreConnectionFactory()
                            + " and " + _odeConfig.getDAOConfScheduleConnectionFactory();
            LOG.error(errmsg, ex);
            throw new Exception(errmsg, ex);
        }
    }
    
    /**
     * This method creates the scheduler.
     * 
     * @return The scheduler
     */
    protected Scheduler createScheduler() {
    	
    	String clusterNodeName = _odeConfig.getProperty(OdeConfigProperties.RIFTSAW_NODE_NAME, "non-cluster-node");
    	String cacheName = _odeConfig.getProperty(OdeConfigProperties.CACHE_NAME_PROPERTY, "cluster");
    	try {
			EmbeddedCacheManager ecm = (EmbeddedCacheManager)
			        new InitialContext().lookup(OdeConfigProperties.CACHE_CONTAINER_ROOT + cacheName);
			clusterNodeName = ecm.getAddress().toString();
			_cache = ecm.getCache();
			_cache.start();
			ecm.addListener(new MemberDropListener(_schedulerDaoCF, _txMgr));
			
		} catch (NamingException e) {
			LOG.error(e);
			//Ignore for now, it means that it is not in the clustering environment.
		}
    	LOG.info("The schduler node name is: "  + clusterNodeName);
    	
        SimpleScheduler scheduler = new SimpleScheduler(clusterNodeName,
                        _schedulerDaoCF, _txMgr, _odeConfig.getProperties());
        scheduler.setExecutorService(_executorService);
        scheduler.setTransactionManager(_txMgr);

        return scheduler;
    }

    /**
     * Initialize the BPEL server.
     * 
     * @param eprContext The endpoint reference context
     * @param locator The service locator
     */
    private void initBpelServer(EndpointReferenceContextImpl eprContext, ServiceLocator locator) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ODE initializing");
        }
        ThreadFactory threadFactory = new ThreadFactory() {
            private int _threadNumber = 0;
            public Thread newThread(Runnable r) {
                _threadNumber += 1;
                Thread t = new Thread(r, "ODEServer-"+_threadNumber);
                t.setDaemon(true);
                return t;
            }
        };

        if (_odeConfig.getThreadPoolMaxSize() == 0) {
            _executorService = Executors.newCachedThreadPool(threadFactory);
        } else {
            _executorService = Executors.newFixedThreadPool(_odeConfig.getThreadPoolMaxSize(), threadFactory);
        }

        _bpelServer = new BpelServerImpl();
        _scheduler = createScheduler();
        _scheduler.setJobProcessor(_bpelServer);

        BpelServerImpl.PolledRunnableProcessor polledRunnableProcessor = new BpelServerImpl.PolledRunnableProcessor();
        polledRunnableProcessor.setPolledRunnableExecutorService(_executorService);
        polledRunnableProcessor.setContexts(_bpelServer.getContexts());
        _scheduler.setPolledRunnableProcesser(polledRunnableProcessor);

        _cronScheduler = new CronScheduler();
        _cronScheduler.setScheduledTaskExec(_executorService);
        _cronScheduler.setContexts(_bpelServer.getContexts());
        _bpelServer.setCronScheduler(_cronScheduler);

        _bpelServer.setDaoConnectionFactory(_daoCF);
        _bpelServer.setInMemDaoConnectionFactory(new BpelDAOConnectionFactoryImpl(_scheduler, _odeConfig.getInMemMexTtl()));
        _bpelServer.setEndpointReferenceContext(eprContext);
        _bpelServer.setMessageExchangeContext(new MessageExchangeContextImpl(locator));
            
        _bpelServer.setBindingContext(new RiftsawBindingContext());
            
        _bpelServer.setScheduler(_scheduler);
        if (_odeConfig.isDehydrationEnabled()) {
            CountLRUDehydrationPolicy dehy = new CountLRUDehydrationPolicy();
            dehy.setProcessMaxAge(_odeConfig.getDehydrationMaximumAge());
            dehy.setProcessMaxCount(_odeConfig.getDehydrationMaximumCount());
            _bpelServer.setDehydrationPolicy(dehy);
        }
        _bpelServer.setConfigProperties(_odeConfig.getProperties());
        _bpelServer.init();
        _bpelServer.setInstanceThrottledMaximumCount(_odeConfig.getInstanceThrottledMaximumCount());
        _bpelServer.setProcessThrottledMaximumCount(_odeConfig.getProcessThrottledMaximumCount());
        _bpelServer.setProcessThrottledMaximumSize(_odeConfig.getProcessThrottledMaximumSize());
        _bpelServer.setHydrationLazy(_odeConfig.isHydrationLazy());
        _bpelServer.setHydrationLazyMinimumSize(_odeConfig.getHydrationLazyMinimumSize());
        _bpelServer.setXTSEnable(_odeConfig.isXTSEnable());
    }
    
    /**
     * {@inheritDoc}
     */
    public void deploy(DeploymentUnit bdu) throws Exception {
        _store.deploy(bdu);
    }

    /**
     * {@inheritDoc}
     */
    public void undeploy(DeploymentUnit bdu) throws Exception {
        _store.undeploy(bdu);
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentRef deploy(File deployment) throws Exception {
        return(deploy(deployment.getName(), deployment));
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentRef deploy(String name, File deployment) throws Exception {
        DeploymentRef ret=null;
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deploying: "+name+" at: "+deployment);
        }
        
        java.util.List<DeploymentUnit> dus=null;
        
        try {
            dus=_deploymentManager.getDeploymentUnits(name, deployment);
        } catch(Exception e) {
            LOG.error("Failed to get deployment units from '"+deployment+"'", e);
            throw e;
        }
            
            for (DeploymentUnit du : dus) {
                try {
                    _store.deploy(du);
                } catch(Exception e) {
                    LOG.error("Failed to deploy '"+du.getName()+"'", e);
                    
                    // Need to undeploy any successful deployments
                    for (DeploymentUnit du2 : dus) {
                        if (du == du2) {
                            break;
                        } else {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Undeploying '"+du2.getName()
                                    +"' after failure when deploying '"+du.getName()+"'");
                            }
                            _store.undeploy(du2);
                        }
                    }
                    
                    throw e;
                }
            }
            
            ret = new DeploymentRefImpl(dus);
            
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deployed: "+deployment+" ref="+ret);
        }
        
        return(ret);
    }

    /**
     * {@inheritDoc}
     */
    public void undeploy(DeploymentRef ref) throws Exception {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Undeploying ref: "+ref);
        }
        
        if (ref instanceof DeploymentRefImpl) {
            for (int i=0; i < ((DeploymentRefImpl)ref).getDeploymentUnits().size(); i++) {
                DeploymentUnit du=((DeploymentRefImpl)ref).getDeploymentUnits().get(i);
                
                if (_store != null) {
                    _store.undeploy(du);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void close() throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            if (_bpelServer != null) {
                try {
                    LOG.debug("shutting down BPEL server.");
                    _bpelServer.shutdown();
                    _bpelServer = null;
                } catch (Throwable ex) {
                    LOG.debug("Error stopping services.", ex);
                }

                if (_cronScheduler != null) {
                    try {
                        LOG.debug("shutting down cron scheduler.");
                        _cronScheduler.shutdown();
                        _cronScheduler = null;
                    } catch (Exception ex) {
                        LOG.debug("Cron scheduler couldn't be shutdown.", ex);
                    }
                }
              
                if (_scheduler != null) {
                    try {
                        LOG.debug("shutting down scheduler.");
                        _scheduler.shutdown();
                        _scheduler = null;
                    } catch (Exception ex) {
                        LOG.debug("Scheduler couldn't be shutdown.", ex);
                    }
                }

                if (_store != null) {
                    try {
                        _store.shutdown();
                        _store = null;
                    } catch (Throwable t) {
                        LOG.debug("Store could not be shutdown.", t);
                    }
                }
                
                //Stop the cache
                _cacheProvider.stop();

                if (_daoCF != null) {
                    try {
                        _daoCF.shutdown();
                    } catch (Throwable ex) {
                        LOG.debug("Bpel DAO shutdown failed.", ex);
                    } finally {
                        _daoCF = null;
                    }
                }
                  
                if (_storeCF != null) {
                    try {
                        _storeCF.shutdown();
                    } catch (Throwable ex) {
                        LOG.debug("Store DAO shutdown failed.", ex);
                    } finally {
                        _storeCF = null;
                    }
                }
                  
                if (_schedulerDaoCF != null) {
                    try {
                        _schedulerDaoCF.shutdown();
                    } catch (Throwable ex) {
                        LOG.debug("Scheduler DAO shutdown failed.", ex);
                    } finally {
                        _schedulerDaoCF = null;
                    }    
                }

                if (_db != null) {
                    try {
                        _db.shutdown();
                    } catch (Throwable ex) {
                        LOG.debug("DB shutdown failed.", ex);
                    } finally {
                        _db = null;
                    }
                }

                if (_txMgr != null) {
                    LOG.debug("shutting down transaction manager.");
                    _txMgr = null;
                }

                unregisterServicesFromJNDI();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }  
    }
    
    /**
     * Register the event listeners.
     */
    private void registerEventListeners() {
        
        // let's always register the debugging listener....
        _bpelServer.registerBpelEventListener(new DebugBpelEventListener());

        // then, whatever else they want.
        String listenersStr = _odeConfig.getEventListeners();
        if (listenersStr != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(listenersStr, ",;"); tokenizer.hasMoreTokens();) {
                String listenerCN = tokenizer.nextToken();
                try {
                    _bpelServer.registerBpelEventListener((BpelEventListener) Class.forName(listenerCN).newInstance());
                    LOG.debug("REGISTERED EVENT LISTENER: "+listenerCN);
                } catch (Exception e) {
                    LOG.warn("Couldn't register the event listener " + listenerCN + ", the class couldn't be "
                                + "loaded properly: " + e);
                }
            }

        }
    }

    /**
     * Register the message exchange interceptors.
     */
    private void registerMexInterceptors() {
        String listenersStr = _odeConfig.getMessageExchangeInterceptors();
        if (listenersStr != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(listenersStr, ",;"); tokenizer.hasMoreTokens();) {
                String interceptorCN = tokenizer.nextToken();
                try {
                    _bpelServer.registerMessageExchangeInterceptor((MessageExchangeInterceptor) Class.forName(interceptorCN).newInstance());
                    LOG.debug("MESSAGE EXCHANGE INTERCEPTOR REGISTERED: "+interceptorCN);
                } catch (Exception e) {
                    LOG.warn("Couldn't register the event listener " + interceptorCN + ", the class couldn't be "
                            + "loaded properly: " + e);
                }
            }
        }
    }

    /**
     * Register the external variable modules.
     */
    private void registerExternalVariableModules() {
        JdbcExternalVariableModule jdbcext;
        jdbcext = new JdbcExternalVariableModule();
        jdbcext.registerDataSource("ode", _db.getDataSource());
        _bpelServer.registerExternalVariableEngine(jdbcext);
    }

    /**
     * Handle the process store event.
     * 
     * @param pse The process store event
     */
    private void handleEvent(ProcessStoreEvent pse) {

        LOG.debug("Process store event: " + pse);
        
        ProcessConf pconf = _store.getProcessConfiguration(pse.pid);
        switch (pse.type) {
        case DEPLOYED:
            if (pconf != null) {
                /*
                 * If and only if an old process exists with the same pid, the old process is cleaned up.
                 * The following line is IMPORTANT and used for the case when the deployment and store
                 * do not have the process while the process itself exists in the BPEL_PROCESS table.
                 * Notice that the new process is actually created on the 'ACTIVATED' event.
                 */
                _bpelServer.cleanupProcess(pconf);
            }
            break;
        case ACTVIATED:
            // bounce the process
            _bpelServer.unregister(pse.pid);
            if (pconf != null) {
                _bpelServer.register(pconf);
            } else {
                LOG.debug("slighly odd: recevied event "
                            + pse + " for process not in store!");
            }
            break;
        case RETIRED:
            // are there are instances of this process running?
            boolean instantiated = _bpelServer.hasActiveInstances(pse.pid);
            // remove the process
            _bpelServer.unregister(pse.pid);
            // bounce the process if necessary
            if (instantiated) {
                if (pconf != null) {
                    _bpelServer.register(pconf);
                } else {
                    LOG.debug("slighly odd: recevied event "
                            + pse + " for process not in store!");
                }
            } else {
                // we may have potentially created a lot of garbage, so,
                // let's hope the garbage collector is configured properly.
                if (pconf != null) {
                    _bpelServer.cleanupProcess(pconf);
                }
            }
            break;
        case DISABLED:
        case UNDEPLOYED:
            _bpelServer.unregister(pse.pid);
            if (pconf != null) {
                _bpelServer.cleanupProcess(pconf);
            }
                
            String retiredProcess = _store.getLatestPackageVersion(pse.deploymentUnit);
            if (retiredProcess != null) {
                _store.setRetiredPackage(retiredProcess, false);
                _store.setRetiredPackage(retiredProcess, true);
            }
                
            break;
        default:
            LOG.debug("Ignoring store event: " + pse);
        }

        if  (pconf != null) {
            if (pse.type == ProcessStoreEvent.Type.UNDEPLOYED) {
                LOG.debug("Cancelling all cron scheduled jobs on store event: " + pse);
                _bpelServer.getContexts().cronScheduler.cancelProcessCronJobs(pse.pid, true);
            }

            // Except for undeploy event, we need to re-schedule process dependent jobs
            LOG.debug("(Re)scheduling cron scheduled jobs on store event: " + pse);
            if (pse.type != ProcessStoreEvent.Type.UNDEPLOYED) {
                _bpelServer.getContexts().cronScheduler.scheduleProcessCronJobs(pse.pid, pconf);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Element invoke(QName serviceName, String portName, String operationName, Element mesg,
                            java.util.Map<String, Object> headers) throws Exception {
        Element ret=null;
        boolean success = true;
        MyRoleMessageExchange odeMex = null;
        Future<?> responseFuture = null;
        Transaction current=null;
        boolean immediate=_odeConfig.getProperty("invoke.immediate", Boolean.FALSE.toString()).equalsIgnoreCase(Boolean.TRUE.toString());
        try {
            current =_txMgr.getTransaction();
            if (current == null) {
                _txMgr.begin();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Starting transaction.");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using existing transaction.");    
                }
                immediate = true;
            }      
            if (LOG.isDebugEnabled()) {
                LOG.debug("Immediate invocation mode: "+immediate);    
            }
            odeMex = createMessageExchange(serviceName, portName, operationName);
            odeMex.setProperty("isTwoWay", Boolean.toString(odeMex.getOperation().getOutput() != null));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Is two way operation? "+odeMex.getProperty("isTwoWay"));
            }     
            if (odeMex.getOperation() != null) {
                // Preparing message to send to ODE
                Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());        
                odeRequest.setMessage(mesg);
                
                if (headers != null) {
                    Set<String> keys = headers.keySet();
                    for (String key : keys) {
                        Object headerPart = headers.get(key);
                        if (headerPart instanceof Element) {
                            Element element = (Element)headerPart;
                            QName name=QName.valueOf(key);
                            odeRequest.setHeaderPart(name.getLocalPart(), element); // put the element to header
                        }
                    }
                }
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invoking ODE using MEX " + odeMex);
                    LOG.debug("Message content:  " + DOMUtils.domToString(odeRequest.getMessage()));
                }

                // Invoke ODE
                // NOTE: 'immediate' parameter could be either true, so that requests invoked immediately?
                // or 'current != null', so only true if request being performed in an outer transaction
                responseFuture = odeMex.invoke(odeRequest, immediate);

                LOG.debug("Commiting ODE MEX " + odeMex);
                if (current == null) {
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Commiting transaction.");
                        }
                        _txMgr.commit();
                    } catch (Exception e) {
                        LOG.error("Commit failed", e);
                        success = false;
                    }
                }
            } else {
                success = false;
            }
        } catch (Exception e) {
            LOG.error("Exception occured while invoking ODE", e);
            success = false;
            String errmesg = e.getMessage();
            if (errmesg == null) {
                errmesg = "An exception occured while invoking ODE.";
            }
            throw new Exception(errmesg, e);
        } finally {
            if (!success) {
                if (odeMex != null) {
                    odeMex.release(success);
                }                
                if (current == null) {
                    try {
                        _txMgr.rollback();
                    } catch (Exception e) {
                        throw new Exception("Rollback failed", e);
                    }
                }
            }
        }

        if (odeMex.getOperation().getOutput() != null) {
            if (!immediate) {
                // Waits for the response to arrive
                try {
                    responseFuture.get(resolveTimeout(serviceName, portName, odeMex), TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    String errorMsg = "Timeout or execution error when waiting for response to MEX "
                                        + odeMex + " " + e.toString();
                    LOG.error(errorMsg, e);
                    throw new Exception(errorMsg);
                }
            }
       
            // Hopefully we have a response
            LOG.debug("Handling response for MEX " + odeMex);
            boolean commit = false;
            if (current == null) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Starting transaction.");
                    }
                    _txMgr.begin();
                } catch (Exception ex) {
                    throw new Exception("Error starting transaction!", ex);
                }
            }
            try {
                // Refreshing the message exchange
                odeMex = (MyRoleMessageExchange) _bpelServer.getEngine().getMessageExchange(odeMex.getMessageExchangeId());
                Message msg = onResponse(odeMex);
                
                if (msg != null) {

                    // Set header parts to the headers
                    Map<String, Node> headerParts = msg.getHeaderParts();
                    if (headers != null) {
                        headers.clear();
                        Set<String> keys = headerParts.keySet();
                        for (String key : keys) {
                            Element e = (Element)msg.getHeaderPart(key).getFirstChild();
                            String k;
                            if (e.getNamespaceURI() == null || e.getNamespaceURI().isEmpty()) {
                                k = e.getLocalName();
                            } else {
                                k = "{"+e.getNamespaceURI()+"}" + e.getLocalName();
                            }
                            headers.put(k, e);
                        }
                    }
                    
                    ret = msg.getMessage();
                    
                }

                LOG.debug("Returning: "+ret);
                commit = true;
            } catch (Fault f) {
                commit = true;
                throw f;                
            } catch (Exception e) {
                LOG.error("Error processing response for MEX " + odeMex, e);
                throw new Exception("An exception occured when invoking ODE.", e);
            } finally {
                odeMex.release(commit);
                if (current == null) {
                    if (commit) {
                        try {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Comitting transaction.");
                            }
                            _txMgr.commit();
                        } catch (Exception e) {
                            throw new Exception("Commit failed!", e);
                        }
                    } else {
                        try {
                            _txMgr.rollback();
                        } catch (Exception ex) {
                            throw new Exception("Rollback failed!", ex);
                        }
                    }
                }
            }
            if (!success) {
                throw new Exception("Message was either unroutable or timed out!");
            }
        } else {
            // One ways cleanup
            odeMex.release(true);
        }
        
        return ret;
    }

    /**
     * Create a message exchange.
     * 
     * @param serviceName The service name
     * @param portName The port name
     * @param operationName The operation name
     * @return The message exchange
     */
    private MyRoleMessageExchange createMessageExchange(QName serviceName, String portName, String operationName) {
        // Creating message exchange
        String messageId = new GUID().toString();
        MyRoleMessageExchange odeMex = _bpelServer.getEngine()
                .createMessageExchange(messageId, serviceName, operationName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("ODE routed to operation "
                    + odeMex.getOperation() + " from service " + serviceName);
        }
        return odeMex;
    }

    /**
     * This method processes the supplied message exchange to return the response.
     * 
     * @param mex The message exchange
     * @return The contents
     * @throws Exception Failed
     */
    private Message onResponse(MyRoleMessageExchange mex) throws Exception {
        Message ret=null;

        switch (mex.getStatus()) {
        case FAULT:
            if (LOG.isDebugEnabled()) {
                LOG.debug("Fault response message: " + mex.getFault());
            }

            throw new Fault(mex.getFault(), mex.getFaultResponse().getMessage());

            //break;
        case ASYNC:
        case RESPONSE:
            ret = mex.getResponse();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Response message " + ret);
            }
            break;
        case FAILURE:
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failure response message: " + mex.getFault());
            }
            LOG.error("Failure details: "+mex.getFaultResponse());

            throw new Exception("Failure response message: "+mex.getFault()+" : "+mex.getFaultExplanation());
        default:
            throw new Exception("Received ODE message exchange in unexpected state: " + mex.getStatus());
        } 
        
        return (ret);
    }

    /**
     * This method returns the timeout period.
     * 
     * @param serviceName The service name
     * @param portName The port name
     * @param odeMex The message exchange
     * @return The timeout period
     */
    private long resolveTimeout(QName serviceName, String portName, MyRoleMessageExchange odeMex) {
        ProcessConf conf=odeMex.getProcessConf();
          
        String timeout = conf.getEndpointProperties(serviceName, portName).get(Properties.PROP_MEX_TIMEOUT);
          
        if (timeout != null) {
            try {
                return Long.parseLong(timeout);
            } catch (NumberFormatException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Mal-formatted Property: ["+ Properties.PROP_MEX_TIMEOUT+"="
                            +timeout+"] Default value ("+Properties.DEFAULT_MEX_TIMEOUT+") will be used");
                }
            }
        }
        return Properties.DEFAULT_MEX_TIMEOUT;
    }
    
    /**
     * This class provides the process store listener implementation.
     *
     */
    private class ProcessStoreListenerImpl implements ProcessStoreListener {

        /**
         * {@inheritDoc}
         */
        public void onProcessStoreEvent(ProcessStoreEvent event) {
            handleEvent(event);
        }
    }
    
    
	public Object getManagementInterface() {
		return new BpelManagementFacadeImpl(_bpelServer, _store);
	}
	
	public RiftSawProcessStore getStore() {
		return _store;
	}
    
    
}
