package com.sinergise.java.util.collections;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Implementation of Map<K,V> with expiring entries. Entries expire (are removed from the map) 
 * after not being accessed for TTL (Time To Live) that was set when the entries were created.
 * Last access time is updated when entries are accessed by their key.
 * 
 * @author tcerovski
 */
public class ExpirableMap<K,V> implements Map<K, V> {

	final Map<K, ExpirableEntry<K, V>> delegate;
	private long ttl;
	
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); 
	
	/**
	 * @param ttl Time to live in milliseconds.
	 */
	public ExpirableMap(long ttl) {
		this.delegate = new HashMap<K, ExpirableEntry<K, V>>();
		this.ttl = ttl;
	}
	
	/**
	 * @param ttl Time to live in milliseconds.
	 */
	public ExpirableMap(long ttl, int initialCapacity) {
		this.delegate = new HashMap<K, ExpirableEntry<K, V>>(initialCapacity);
		this.ttl = ttl;
	}
	
	/**
	 * @param ttl Time to live in milliseconds.
	 */
	public ExpirableMap(long ttl, int initialCapacity, float loadFactor) {
		this.delegate = new HashMap<K, ExpirableEntry<K, V>>(initialCapacity, loadFactor);
		this.ttl = ttl;
	}
	
	/**
	 * Sets maps' time to live. New value will be applied only to new entries,
	 * old entries will expire after TTL set at the time of entry. 
	 * @param ttl Time to live in milliseconds.
	 */
	public void setTTL(long ttl) {
		this.ttl = ttl;
	}
	
	/**
	 * @param Maps' time to live in milliseconds.
	 */
	public long getTTL() {
		return ttl;
	}
	
	private ExpirableEntry<K, V> touch(ExpirableEntry<K, V> e) {
		return touch(e, System.currentTimeMillis());
	}
	
	private ExpirableEntry<K, V> touch(ExpirableEntry<K, V> e, long lastAccess) {
		e.setLastAccess(lastAccess);
		scheduler.schedule(new CheckExpiredCommand(e), e.getTTL()+1, MILLISECONDS);
		return e;
	}
	
	@Override
	public V put(K key, V value) {
		return putInternally(key, value, System.currentTimeMillis());
	}
	
	private V putInternally(K key, V value, long time) {
		ExpirableEntry<K,V> prevValue = delegate.put(key, touch(new ExpirableEntry<K, V>(key, value, ttl), time));
		if (prevValue != null) {
			prevValue.setRemoved();
			return prevValue.value;
		}
		return null;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		long time = System.currentTimeMillis(); //use same time for all
		
		for (K key : m.keySet()) {
			putInternally(key, m.get(key), time);
		}
	}
	
	@Override
	public V remove(Object key) {
		ExpirableEntry<K,V> e = delegate.remove(key);
		if (e != null) {
			e.setRemoved();
			return e.value;
		}
		return null;
	}
	
	@Override
	public V get(Object key) {
		ExpirableEntry<K,V> e = delegate.get(key);
		if (e != null) {
			touch(e);
			return e.value;
		}
		return null;
	}
	
	@Override
	public void clear() {
		for (ExpirableEntry<K, V> e : delegate.values()) {
			e.setRemoved();
		}
		delegate.clear();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}
	
	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int size() {
		return delegate.size();
	}
	
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}
	
	private static class ExpirableEntry<K, V> {
		final V value;
		final K key;
		final long ttl;
		long lastAccess = 0;
		boolean removed = false;
		
		ExpirableEntry(K key, V value, long ttl) {
			this.key = key;
			this.value = value;
			this.ttl = ttl;
			lastAccess = System.currentTimeMillis();
		}
		
		void setLastAccess(long timeMillis) {
			this.lastAccess = timeMillis;
		}
		
		boolean isExpired() {
			return lastAccess + ttl < System.currentTimeMillis();
		}
		
		long getTTL() {
			return ttl;
		}
		
		void setRemoved() {
			this.removed = true;
		}
		
	}
	
	private class CheckExpiredCommand implements Runnable {
		
		final ExpirableEntry<K, V> e;
		
		CheckExpiredCommand(ExpirableEntry<K, V> e) {
			this.e = e;
		}
		
		@Override
		public void run() {
			if (!e.removed && e.isExpired()) {
				delegate.remove(e.key);
			}
		}
	}

}
