package com.sinergise.common.util.collections.safe;

import java.util.Map;

import com.sinergise.common.util.collections.MapWrapper;

/**
 * Abstract typesafe map which allows the type of the value to be defined by the type parameter of the TypeSafeKey interface implementation.
 * 
 * Extensions of this class will typically require their own class for the key (e.g. <code>MyKeyClass&lt;V&gt; implements TypeSafeKey&lt;V&gt;</code>);
 * they should define and implement the three type safe methods as shown in this example:
 * <code><pre>
 *  public &lt;T extends V&gt; T getSafe(MyKeyClass&lt;T&gt; key) {
 *      return internalGet(key);
 *  }
 *  public &lt;T extends V&gt; T putSafe(MyKeyClass&lt;T&gt; key, T value) {
 *      return internalPut(key, value);
 *  }
 *  public &lt;T extends V&gt; T removeSafe(MyKeyClass&lt;T&gt; key) {
 *      return internalRemove(key);
 *  }</pre></code>
 * Check out {@link DefaultTypeSafeMap} for an example
 * 
 * @see DefaultTypeSafeMap
 * @author Miha
 *
 * @param <K> Type of keys, which should implement the TypeSafeKey interface
 * @param <V> type of values
 */
public abstract class TypeSafeMap<K extends TypeSafeKey<? extends V>, V> extends MapWrapper<K, V> {
	
	private static final long serialVersionUID = 681172340244436251L;

	public TypeSafeMap() {
		super();
	}
	
	public TypeSafeMap(Map<K, V> m) {
		super(m);
	}

	/**
	 * This method is internal because Java for some reason doesn't allow the following declaration:
	 * <p><code>
	 * public &lt;T extends V, S extends <b>K & TypeSafeKey&lt;T&gt;</b>&gt; T getSafe(S key);
	 * </code></p>
	 * 
	 * In subclasses that want to deal with specific subtypes of TypeSafeKey, implement getSafe as:
	 * <p><code>
	 * public &lt;T extends V&gt; T getSafe(SpecificKeyType&lt;T&gt; key);
	 * </code></p>
	 * 
	 * @param key should be an instance of {@link K}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final <T extends V> T internalGet(TypeSafeKey<T> key) {
		T ret = (T)super.get(key);
		if (ret == null) {
			return internalGetDefault(key);
		}
		return ret;
	}

	/**
	 * This method is internal because Java for some reasone doesn't allow the following declaration:
	 * <p><code>
	 * public &lt;T extends V, S extends <b>K & TypeSafeKey&lt;T&gt;</b>&gt; T putSafe(S key, T value);
	 * </code></p>
	 * 
	 * In subclasses that want to deal with specific subtypes of TypeSafeKey, implement putSafe as:
	 * <p><code>
	 * public &lt;T extends V&gt; T putSafe(SpecificKeyType&lt;T&gt; key, T value);
	 * </code></p>
	 * 
	 * @param key should be an instance of {@link K}
	 * @param value
	 * @return previous value contained in the map for the specified key
	 */
	@SuppressWarnings("unchecked")
	protected final <T extends V> T internalPut(TypeSafeKey<T> key, T value) {
		T ret = (T)map.put((K)key, value);
		if (ret == null) {
			return internalGetDefault(key);
		}
		return ret;
	}
	
	/**
	 * Determines what to return as a result of put, get and remove when no value is present in the map.
	 * Defaults to null (same behaviour as Map). Override to provide key-specific defaults.
	 * 
	 * @param key
	 * @return
	 */
	protected <T> T internalGetDefault(TypeSafeKey<T> key) {
		return null;
	}

	/**
	 * This method is internal because Java for some reasone doesn't allow the following declaration:
	 * <p><code>
	 * public &lt;T extends V, S extends <b>K & TypeSafeKey&lt;T&gt;</b>&gt; T removeSafe(S key, T value);
	 * </code></p>
	 * 
	 * In subclasses that want to deal with specific subtypes of TypeSafeKey, implement removeSafe as:
	 * <p><code>
	 * public &lt;T extends V&gt; T removeSafe(SpecificKeyType&lt;T&gt; key);
	 * </code></p>
	 *  
	 * @param key should be an instance of {@link K}
	 * @return the value contained in the map for the specified key
	 */
	@SuppressWarnings("unchecked")
	protected final <T extends V> T internalRemove(TypeSafeKey<T> key) {
		T ret = (T)map.remove(key);
		if (ret == null) {
			return internalGetDefault(key);
		}
		return ret;
	}
	
	@Override
	@Deprecated
	@SuppressWarnings("unchecked")
	public V put(K key, V value) {
		return internalPut((TypeSafeKey<V>)key, value);
	}

	@Override
	@Deprecated
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		return internalGet((TypeSafeKey<V>)key);
	}

	@Override
	@Deprecated
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		return internalRemove((TypeSafeKey<V>)key);
	}

	
// TODO: When Java allows combining type parameters with interface implementation, implement the typesafe methods here instead of having subclasses 
//	public <T extends V, S extends K & TypeSafeKey<T>> T putSafe(S key) {
//		return (T)map.put(key);
//	}
}
