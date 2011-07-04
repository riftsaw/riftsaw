package org.riftsaw.engine.internal;

import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.evt.DebugBpelEventListener;
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
import org.riftsaw.engine.BPELDeploymentUnit;
import org.w3c.dom.Element;

public class BPELEngineImpl {
	
	public static void main(String[] args) {
		
		java.net.URL url=BPELEngineImpl.class.getResource("/hello_world/deploy.xml");
		
		java.io.File deployFile=new java.io.File(url.getFile());
		
		BPELDeploymentUnit bdu=new BPELDeploymentUnit("hello_world", deployFile.lastModified());
		bdu.setDeploymentDescriptor(deployFile);

		// Create the engine
		BPELEngineImpl engine=new BPELEngineImpl();
		
		try {
			engine.init();
			
			Thread.sleep(2000);
			
			logger.info("GPB: DEPLOY");
			
			// Deploy the process
			engine.deploy(bdu);
			
			Thread.sleep(5000);
			
			org.w3c.dom.Element mesgElem=DOMUtils.stringToDOM("<message><TestPart>Hello</TestPart></message>");
	
			// Invoke the service
			String serviceName="{http://www.jboss.org/bpel/examples/wsdl}HelloService";
			String portName="HelloPort";
			String operation="hello";
			
			javax.xml.namespace.QName qname=javax.xml.namespace.QName.valueOf(serviceName);
			
			// Create invocationContext
			//EmbeddedInvocationAdapter invocationContext =
			//	new EmbeddedInvocationAdapter(operation, qname, portName);
			//invocationContext.setRequestXML(mesgElem);
			
			// invoke ODE
			Element resp=null;
			
			try {
				resp  = engine.invoke(qname, portName, operation, mesgElem, null);
			} catch(Throwable t) {
				// RIFTSAW-177 - prevent ODE specific exceptions being returned to ESB client where
				// a ClassNotFoundException would be thrown
				throw new Exception("BPEL invoke failed: "+t);
			}
			
			logger.info("RESPONSE="+DOMUtils.domToString(resp));
			
			logger.info("GPB: UNDEPLOY");
			
			// Undeploy the process
			engine.undeploy(bdu);
			
			Thread.sleep(2000);

			System.out.println("GPB: CLOSE");
			
			engine.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static final Logger logger=Logger.getLogger(BPELEngineImpl.class.getName());

	protected BpelServerImpl _bpelServer;
	protected RiftSawProcessStore _store;
	protected OdeConfigProperties _odeConfig;
	protected TransactionManager _txMgr;
	protected BpelDAOConnectionFactory _daoCF;
	protected ConfStoreDAOConnectionFactory _storeCF;
	protected SchedulerDAOConnectionFactory _schedulerDaoCF;
	protected Scheduler _scheduler;
	protected Database _db;
	protected ExecutorService _executorService;
	protected CronScheduler _cronScheduler;
	protected CacheProvider _cacheProvider;
	//protected UDDIRegistration _uddiRegistration;

	public void init() throws Exception {

		java.util.Properties props=new java.util.Properties();

		try {
			java.io.InputStream is=BPELEngineImpl.class.getClassLoader().getResourceAsStream("bpel.properties");
	
			props.load(is);
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Failed to load properties", e);
		}

		logger.info("ODE PROPS="+props);

		_odeConfig = new OdeConfigProperties(props, "bpel.");

		logger.info("Initializing transaction manager");
		initTxMgr();
		
		logger.info("Creating data source.");
		initDataSource();
		
		logger.info("Starting DAO.");
		initDAO();
		
		EndpointReferenceContextImpl eprContext = new EndpointReferenceContextImpl(this);
		
		initCacheProvider();
		
		logger.info("Initializing BPEL process store.");
		initProcessStore(eprContext);
		
		logger.info("Initializing UDDI registration");
		//initUDDIRegistration();
		
		logger.info("Initializing BPEL server.");
		initBpelServer(eprContext);

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
			logger.log(Level.SEVERE, errmsg, ex);
		}

		logger.info("Starting scheduler");
		_scheduler.start();
	}
	
	protected void initProcessStore(EndpointReferenceContext eprContext) {
		// GPB: IS TX MGR NECESSARY?
		_store = createProcessStore(eprContext, _txMgr, _storeCF);
		
		// GPB: TO INVESTIGATE
		_store.registerListener(new ProcessStoreListenerImpl());
	}
	
	protected RiftSawProcessStore createProcessStore(EndpointReferenceContext eprContext, TransactionManager txm, ConfStoreDAOConnectionFactory cf) {
		return new RiftSawProcessStore(eprContext, txm, cf, _cacheProvider);
	}

	private void initCacheProvider() {
		_cacheProvider = CacheProviderFactory.getCacheProvider(_odeConfig);
		try {
			_cacheProvider.start();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in starting cache provider", e);
			throw new RuntimeException("Error in initCacheProvider.", e);
		}
	}
	
	private void initDataSource() throws Exception {
		_db = new Database(_odeConfig);
		_db.setTransactionManager(_txMgr);

		//_db.setWorkRoot(new java.io.File("/tmp/h2"));
		
		try {
			_db.start();
		} catch (Exception ex) {
			String errmsg = "FAILED TO INITIALISE DATA SOURCE";
			logger.log(Level.SEVERE, errmsg, ex);
			throw new Exception(errmsg, ex);
		}
	}

	private void initTxMgr() throws Exception {
		/*
		String txFactoryName = _odeConfig.getTxFactoryClass();
		logger.info("Initializing transaction manager using " + txFactoryName);
		try {
			Class<?> txFactClass = this.getClass().getClassLoader().loadClass(txFactoryName);
			Object txFact = txFactClass.newInstance();
			_txMgr = (TransactionManager) txFactClass.getMethod("getTransactionManager", (Class[]) null).invoke(txFact);
			
			// GPB: INVESTIGATE
			//if (__logTx.isDebugEnabled() && System.getProperty("ode.debug.tx") != null)
			//	_txMgr = new DebugTxMgr(_txMgr);
			//_axisConfig.addParameter("ode.transaction.manager", _txMgr);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
			throw new Exception("Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
		}
		*/
		_txMgr = new org.apache.geronimo.transaction.manager.GeronimoTransactionManager();
	}

	protected void initDAO() throws Exception {
		logger.info("USING DAO: "+_odeConfig.getDAOConnectionFactory() + ", " + _odeConfig.getDAOConfStoreConnectionFactory()
						+ ", " + _odeConfig.getDAOConfScheduleConnectionFactory());
		try {
			_daoCF = _db.createDaoCF();
			_storeCF = _db.createDaoStoreCF();
			_schedulerDaoCF = _db.createDaoSchedulerCF();
		} catch (Exception ex) {
			String errmsg = "DAO INSTANTIATION FAILED: "+_odeConfig.getDAOConnectionFactory() + " , " + _odeConfig.getDAOConfStoreConnectionFactory()
							+ " and " + _odeConfig.getDAOConfScheduleConnectionFactory();
			logger.log(Level.SEVERE, errmsg, ex);
			throw new Exception(errmsg, ex);
		}
	}
	
	protected Scheduler createScheduler() {
		  
		// GPB: TO INVESTIGATE
		//String clusterNodeName=JBossDSPFactory.getServerConfig().getClusterNodeName();
		String clusterNodeName="node1";
		
		//logger.info("Scheduler node name: "+clusterNodeName);
		  
		SimpleScheduler scheduler = new SimpleScheduler(clusterNodeName,
						_schedulerDaoCF, _txMgr, _odeConfig.getProperties());
		scheduler.setExecutorService(_executorService);
		scheduler.setTransactionManager(_txMgr);

		return scheduler;
	}

	private void initBpelServer(EndpointReferenceContextImpl eprContext) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("ODE initializing");
		}
		ThreadFactory threadFactory = new ThreadFactory() {
			int threadNumber = 0;
			public Thread newThread(Runnable r) {
				threadNumber += 1;
				Thread t = new Thread(r, "ODEServer-"+threadNumber);
				t.setDaemon(true);
				return t;
			}
		};

		if (_odeConfig.getThreadPoolMaxSize() == 0)
			_executorService = Executors.newCachedThreadPool(threadFactory);
		else
			_executorService = Executors.newFixedThreadPool(_odeConfig.getThreadPoolMaxSize(), threadFactory);

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
		_bpelServer.setMessageExchangeContext(new MessageExchangeContextImpl(null));
		    
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
	}
	
	public void deploy(BPELDeploymentUnit bdu) {
		_store.deploy(bdu);
	}

	public void undeploy(BPELDeploymentUnit bdu) {
		_store.undeploy(bdu);
	}
	
	public void close() throws Exception {
		ClassLoader old = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {
			if (_bpelServer != null)
				try {
					logger.fine("shutting down BPEL server.");
					_bpelServer.shutdown();
					_bpelServer = null;
				} catch (Throwable ex) {
					logger.log(Level.FINE, "Error stopping services.", ex);
				}
	              
				_cacheProvider.stop();

				/*
				if (_uddiRegistration != null) 
	        	  try {
	        		  __log.debug("shutting down UDDI Registration client.");
	        		  _uddiRegistration.shutdown();
	        		  _uddiRegistration = null;
	        	  } catch (Throwable ex) {
	                  __log.debug("Error stopping UDDI Registration client.", ex);
	              }
	        	*/  

				if( _cronScheduler != null ) {
					try {
						logger.fine("shutting down cron scheduler.");
						_cronScheduler.shutdown();
						_cronScheduler = null;
					} catch (Exception ex) {
						logger.log(Level.FINE, "Cron scheduler couldn't be shutdown.", ex);
					}
				}
	          
				if (_scheduler != null)
					try {
						logger.fine("shutting down scheduler.");
						_scheduler.shutdown();
						_scheduler = null;
					} catch (Exception ex) {
						logger.log(Level.FINE, "Scheduler couldn't be shutdown.", ex);
					}

				if (_store != null)
					try {
						_store.shutdown();
						_store = null;
					} catch (Throwable t) {
						logger.log(Level.FINE, "Store could not be shutdown.", t);
					}

				if (_daoCF != null)
					try {
						_daoCF.shutdown();
					} catch (Throwable ex) {
						logger.log(Level.FINE, "Bpel DAO shutdown failed.", ex);
					} finally {
						_daoCF = null;
					}
	              
				if (_storeCF != null)
					try {
						_storeCF.shutdown();
					} catch (Throwable ex) {
						logger.log(Level.FINE, "Store DAO shutdown failed.", ex);
					} finally {
						_storeCF = null;
					}
		          
				if (_schedulerDaoCF != null)
					try {
						_schedulerDaoCF.shutdown();
					} catch (Throwable ex) {
						logger.log(Level.FINE, "Scheduler DAO shutdown failed.", ex);
					} finally {
						_schedulerDaoCF = null;
					}    

				if (_db != null)
					try {
						_db.shutdown();
					} catch (Throwable ex) {
						logger.log(Level.FINE, "DB shutdown failed.", ex);
					} finally {
						_db = null;
					}

				if (_txMgr != null) {
					logger.fine("shutting down transaction manager.");
					_txMgr = null;
				}

		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}  
	}
	
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
					logger.fine("REGISTERED EVENT LISTENER: "+listenerCN);
				} catch (Exception e) {
					logger.warning("Couldn't register the event listener " + listenerCN + ", the class couldn't be "
								+ "loaded properly: " + e);
				}
			}

		}
	}

	private void registerMexInterceptors() {
		String listenersStr = _odeConfig.getMessageExchangeInterceptors();
		if (listenersStr != null) {
			for (StringTokenizer tokenizer = new StringTokenizer(listenersStr, ",;"); tokenizer.hasMoreTokens();) {
				String interceptorCN = tokenizer.nextToken();
				try {
					_bpelServer.registerMessageExchangeInterceptor((MessageExchangeInterceptor) Class.forName(interceptorCN).newInstance());
					logger.fine("MESSAGE EXCHANGE INTERCEPTOR REGISTERED: "+interceptorCN);
				} catch (Exception e) {
					logger.warning("Couldn't register the event listener " + interceptorCN + ", the class couldn't be "
							+ "loaded properly: " + e);
				}
			}
		}
	}

	private void registerExternalVariableModules() {
		JdbcExternalVariableModule jdbcext;
		jdbcext = new JdbcExternalVariableModule();
		jdbcext.registerDataSource("ode", _db.getDataSource());
		_bpelServer.registerExternalVariableEngine(jdbcext);
	}

	private void handleEvent(ProcessStoreEvent pse) {

		logger.fine("Process store event: " + pse);
		
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
				logger.fine("slighly odd: recevied event " +
							pse + " for process not in store!");
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
					logger.fine("slighly odd: recevied event " +
							pse + " for process not in store!");
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
			logger.fine("Ignoring store event: " + pse);
		}

		if( pconf != null ) {
			if( pse.type == ProcessStoreEvent.Type.UNDEPLOYED) {
				logger.fine("Cancelling all cron scheduled jobs on store event: " + pse);
				_bpelServer.getContexts().cronScheduler.cancelProcessCronJobs(pse.pid, true);
			}

			// Except for undeploy event, we need to re-schedule process dependent jobs
			logger.fine("(Re)scheduling cron scheduled jobs on store event: " + pse);
			if( pse.type != ProcessStoreEvent.Type.UNDEPLOYED) {
				_bpelServer.getContexts().cronScheduler.scheduleProcessCronJobs(pse.pid, pconf);
			}
		}
	}

	public Element invoke(QName serviceName, String portName, String operationName, Element mesg,
							java.util.Map<String, Object> headers) throws Exception {
		Element ret=null;
		boolean success = true;
		MyRoleMessageExchange odeMex = null;
		Future<?> responseFuture = null;
   
		try {
			_txMgr.begin();
			if (logger.isLoggable(Level.FINE)) logger.fine("Starting transaction.");
      
			odeMex = createMessageExchange(serviceName, portName, operationName);
			odeMex.setProperty("isTwoWay", Boolean.toString(odeMex.getOperation().getOutput() != null));
			if (logger.isLoggable(Level.FINE)) logger.fine("Is two way operation? "+odeMex.getProperty("isTwoWay"));
      
			if (odeMex.getOperation() != null) {
				// Preparing message to send to ODE
				Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
        
				odeRequest.setMessage(mesg);
				
				// TODO: Need to apply headers - should they be elements or strings, or both catered for?
				
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Invoking ODE using MEX " + odeMex);
					logger.fine("Message content:  " + DOMUtils.domToString(odeRequest.getMessage()));
				}

				// Invoke ODE
				responseFuture = odeMex.invoke(odeRequest);

				logger.fine("Commiting ODE MEX " + odeMex);
				try {
					if (logger.isLoggable(Level.FINE)) logger.fine("Commiting transaction.");
					_txMgr.commit();
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Commit failed", e);
					success = false;
				}
			} else {
				success = false;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception occured while invoking ODE", e);
			success = false;
			String errmesg = e.getMessage();
			if (errmesg == null) {
				errmesg = "An exception occured while invoking ODE.";
			}
			throw new Exception(errmesg, e);
		} finally {
			if (!success) {
				if (odeMex != null) odeMex.release(success);
				try {
					_txMgr.rollback();
				} catch (Exception e) {
					throw new Exception("Rollback failed", e);
				}
			}
		}

		if (odeMex.getOperation().getOutput() != null) {
			// Waits for the response to arrive
			try {
				responseFuture.get(resolveTimeout(serviceName, portName, odeMex), TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				String errorMsg = "Timeout or execution error when waiting for response to MEX "
									+ odeMex + " " + e.toString();
				logger.log(Level.SEVERE, errorMsg, e);
				throw new Exception(errorMsg);
			}
       
			// Hopefully we have a response
			logger.fine("Handling response for MEX " + odeMex);
			boolean commit = false;
			try {
				if (logger.isLoggable(Level.FINE)) logger.fine("Starting transaction.");
				_txMgr.begin();
			} catch (Exception ex) {
				throw new Exception("Error starting transaction!", ex);
			}
			try {
				// Refreshing the message exchange
				odeMex = (MyRoleMessageExchange) _bpelServer.getEngine().getMessageExchange(odeMex.getMessageExchangeId());
				ret = onResponse(odeMex);

				logger.fine("Returning: "+ret);

				commit = true;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error processing response for MEX " + odeMex, e);
				throw new Exception("An exception occured when invoking ODE.", e);
			} finally {
				odeMex.release(commit);
				if (commit) {
					try {
						if (logger.isLoggable(Level.FINE)) logger.fine("Comitting transaction.");
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
        //}
			}
			if (!success) {
				throw new Exception("Message was either unroutable or timed out!");
			}
		} else {
			// One ways cleanup
			odeMex.release(true);
		}
		
		return(ret);
	}

	private MyRoleMessageExchange createMessageExchange(QName serviceName, String portName, String operationName) {
		// Creating message exchange
		String messageId = new GUID().toString();
		MyRoleMessageExchange odeMex = _bpelServer.getEngine()
				.createMessageExchange(messageId, serviceName, operationName);
		if (logger.isLoggable(Level.FINE)) logger.fine("ODE routed to operation " +
					odeMex.getOperation() + " from service " + serviceName);
		return odeMex;
	}

	private Element onResponse(MyRoleMessageExchange mex) throws Exception {
		Element ret=null;

		switch (mex.getStatus()) {
		case FAULT:
			if (logger.isLoggable(Level.FINE))
				logger.fine("Fault response message: " + mex.getFault());

			
			// TODO: Throw 'fault' exception?
		    //this.responseXML = fault.getMessage();
		    //this.faultName = faultName;
			//invocationAdapter.createFault(mex.getOperation(), mex.getFault(), new ODEMessageAdapter(mex.getFaultResponse()));
			break;
		case ASYNC:
		case RESPONSE:
			ret = mex.getResponse().getMessage();        
			if (logger.isLoggable(Level.FINE))
				logger.fine("Response message " + ret);
			break;
		case FAILURE:
			if (logger.isLoggable(Level.FINE))
				logger.fine("Failure response message: " + mex.getFault());
			logger.severe("Failure details: "+mex.getFaultResponse());

			throw new Exception("Failure response message: "+mex.getFault()+" : "+mex.getFaultExplanation());
		default:
			throw new Exception("Received ODE message exchange in unexpected state: " + mex.getStatus());
		} 
		
		return(ret);
	}

	private long resolveTimeout(QName serviceName, String portName, MyRoleMessageExchange odeMex) {
		ProcessConf conf=odeMex.getProcessConf();
		  
		String timeout = conf.getEndpointProperties(serviceName, portName).get(Properties.PROP_MEX_TIMEOUT);
	      
		if (timeout != null) {
			try {
				return Long.parseLong(timeout);
			} catch (NumberFormatException e) {
				if (logger.isLoggable(Level.WARNING)) logger.warning("Mal-formatted Property: ["+ Properties.PROP_MEX_TIMEOUT+"="+timeout+"] Default value ("+Properties.DEFAULT_MEX_TIMEOUT+") will be used");
			}
		}
		return Properties.DEFAULT_MEX_TIMEOUT;
	}
	
	private class ProcessStoreListenerImpl implements ProcessStoreListener {

		public void onProcessStoreEvent(ProcessStoreEvent event) {
			handleEvent(event);
		}
	}
}
