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
package org.riftsaw.engine;

/**
 * This class provides a factory for BPELEngine instances.
 *
 */
public final class BPELEngineFactory {
    
    private static BPELEngine _engine=null;
    private static ServiceLocator _serviceLocator=null;
    private static java.util.Properties _config=null; 
    
    /**
     * The private constructor.
     */
    private BPELEngineFactory() {
    }
    
    /**
     * This method sets the service locator. If the locator and
     * config are both provided, then they will be used to
     * initialized the engine before it is returned. Otherwise
     * the caller must initialize the engine directly.
     * 
     * NOTE: Using this method will cause any current singleton
     * engine to be cleared.
     * 
     * @param locator The service locator
     */
    public static void setServiceLocator(ServiceLocator locator) {
        _serviceLocator = locator;
        _engine = null;
    }

    /**
     * This method sets the config. If the locator and
     * config are both provided, then they will be used to
     * initialized the engine before it is returned. Otherwise
     * the caller must initialize the engine directly.
     * 
     * NOTE: Using this method will cause any current singleton
     * engine to be cleared.
     * 
     * @param config The config
     */
    public static void setConfig(java.util.Properties config) {
        _config = config;
        _engine = null;
    }

    /**
     * This method returns a new potentially uninitialized instance
     * of a BPEL engine. If the service locator and config have
     * previously been supplied, then the engine will be initialized
     * before being returned. The caller is expected to manage the
     * lifecycle of the engine.
     * 
     * @return The BPEL engine instance
     * @throws Exception Failed to create engine
     */
    public static BPELEngine createEngine() throws Exception {
        BPELEngine ret=new org.riftsaw.engine.internal.BPELEngineImpl();
        
        if (_serviceLocator != null && _config != null) {
            ret.init(_serviceLocator, _config);
        }
        
        return (ret);
    }
    
    /**
     * This method returns an initially uninitialized singleton instance
     * of a BPEL engine. The caller is expected to manage the lifecycle
     * of the engine.
     * 
     * @return The BPEL engine singleton instance
     * @throws Exception Failed to get the engine
     */
    public static synchronized BPELEngine getEngine() throws Exception {
        if (_engine == null) {
            _engine = createEngine();
        }
        return (_engine);
    }
}
