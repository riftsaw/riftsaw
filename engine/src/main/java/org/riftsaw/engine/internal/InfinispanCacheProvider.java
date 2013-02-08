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

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Cache;
import org.apache.ode.bpel.iapi.CacheProvider;
import org.apache.ode.il.cache.HashMapCache;
import org.apache.ode.il.config.OdeConfigProperties;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * 
 *
 */
public class InfinispanCacheProvider implements CacheProvider{
		
	private static Log logger = LogFactory.getLog(InfinispanCacheProvider.class);
	
	private org.infinispan.Cache cache;

	public void start(Properties properties) throws Exception {
		try {
			EmbeddedCacheManager ecm = (EmbeddedCacheManager)
			        new InitialContext().lookup(OdeConfigProperties.CACHE_CONTAINER_ROOT + properties.getProperty(OdeConfigProperties.CACHE_NAME_PROPERTY, "cluster"));
			cache = ecm.getCache();
			cache.start();
		} catch (NamingException e) {
			logger.debug("Error on starting the Infinispan cache Manager. detaild is: " + e);	 
		}
		
	}

	public <K, V> Cache<K, V> createCache() {
		if (cache == null) {
			return new HashMapCache<K, V>(); 
		} else {
			return new InfinispanCache(cache);
		}
		
	}

	public void stop() throws Exception {
		cache.stop();
	}

}
