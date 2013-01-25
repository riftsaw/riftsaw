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

import java.util.Collection;
import java.util.Set;

import org.apache.ode.bpel.iapi.Cache;

/**
 * 
 *
 */
public class InfinispanCache<K, V> implements Cache<K, V>{
	
	private final org.infinispan.Cache<K, V> cache;
	
	public InfinispanCache(org.infinispan.Cache<K, V> cache) {
		this.cache = cache;
	}

	public V put(K key, V value) {
		return cache.put(key, value);
	}

	public V get(K key) {
		return cache.get(key);
	}

	public V remove(K key) {
		return cache.remove(key);
	}

	public boolean containsKey(K key) {
		return cache.containsKey(key);
	}

	public Set<K> keySet() {
		return cache.keySet();
	}

	public Collection<V> values() {
		return cache.values();
	}

}
