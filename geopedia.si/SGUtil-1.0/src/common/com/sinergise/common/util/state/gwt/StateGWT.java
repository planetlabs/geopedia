package com.sinergise.common.util.state.gwt;

import static com.sinergise.common.util.Util.ifnull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sinergise.common.util.string.StringUtil;

/**
 * This is the default implementation of the <code>State</code> interface. It is basically extended properties. It uses
 * the <code>LinkedHashMap</code> for the intermediate storage. This implementation should be sufficient for the
 * majority application and it is not likely that a component need to implement its own state object to exchange with
 * the <code>StateKeeper</code>
 * 
 * @author dvitas
 */
@SuppressWarnings("serial")
public class StateGWT implements Serializable, SourcesPropertyChangeEvents<Object> {
	
	public static final String PROP_IS_NULL = "__null_state_";

	private static double parseDbl(String str) {
		return Double.parseDouble(str.replace(',', '.'));
	}

	private static long parseLng(String str) {
		return Math.round(parseDbl(str));
	}

	private static int parseInt(String str) {
		return (int)parseLng(str);
	}

	private static class KeyIterator implements Iterator<String> {
		private transient final HashMap<String, ?>[] maps;
		private Iterator<String> curIter;
		private int nextMap;

		public KeyIterator(final HashMap<String, ?>[] maps) {
			this.maps = maps;
			curIter = maps[0].keySet().iterator();
			nextMap = 1;
		}

		@Override
		public boolean hasNext() {
			while (!curIter.hasNext()) {
				if (nextMap >= maps.length) {
					return false;
				}
				curIter = maps[nextMap].keySet().iterator();
				nextMap++;
			}
			return curIter.hasNext();
		}

		@Override
		public String next() {
			return curIter.next();
		}

		@Override
		public void remove() {
			curIter.remove();
		}
	}

	private static class EntryIterator<T, U> implements Iterator<Map.Entry<T, U>> {
		
		private transient final HashMap<T, U>[] maps;
		private Iterator<Map.Entry<T, U>> curIter;
		private int nextMap;

		public EntryIterator(final HashMap<T, U>[] maps) {
			this.maps = maps;
			curIter = maps[0].entrySet().iterator();
			nextMap = 1;
		}

		@Override
		public boolean hasNext() {
			while (!curIter.hasNext()) {
				if (nextMap >= maps.length) {
					return false;
				}
				curIter = maps[nextMap].entrySet().iterator();
				nextMap++;
			}
			return curIter.hasNext();
		}

		@Override
		public Map.Entry<T, U> next() {
			return curIter.next();
		}

		@Override
		public void remove() {
			curIter.remove();
		}
	}

	private boolean readOnly;

	protected HashMap<String, String> map = new HashMap<String, String>(0, 0.8f);
	protected HashMap<String, StateGWT> children = new HashMap<String, StateGWT>(0, 0.8f);

	protected transient PropertyChangeListenerCollection<Object> listeners;

	/**
	 * Listeners grouped by property. so they don't get multiple events...
	 */
	private transient final PropertyChangeListener<Object> childStatesPcl = new PropertyChangeListener<Object>() {
		@Override
		public void propertyChange(final Object sender, final String propertyName, final Object oldValue,
			final Object newValue) {
			if (propertyName.equals(PROP_IS_NULL)) {
				final StateGWT snd = (StateGWT)sender;
				if (isNull() && !snd.isNull()) {
					setNull(false);
				}
			}
		}
	};

	public transient HashMap<String, PropertyChangeListenerCollection<Object>> specificListners;

	private transient final PropertyChangeListener<Object> eventFilter = new PropertyChangeListener<Object>() {
		@Override
		public void propertyChange(final Object sender, final String propertyName, final Object oldValue,
			final Object newValue) {
			if (specificListners != null && specificListners.containsKey(propertyName)) {
				final PropertyChangeListenerCollection<Object> pl = specificListners.get(propertyName);
				pl.fireChange(sender, propertyName, oldValue, newValue);
			}
		}
	};

	public StateGWT() {
		this(true);
	}

	public StateGWT(final boolean createAsNull) {
		super();
		setNull(createAsNull);
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		for (StateGWT child : children.values()) {
			child.setReadOnly(readOnly);
		}
	}

	public void setNull(final boolean isNull) {
		if (isNull) {
			putString(PROP_IS_NULL, Boolean.toString(true));
		} else {
			putString(PROP_IS_NULL, null);
		}
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener<Object> listener) {
		if (listeners == null) {
			listeners = new PropertyChangeListenerCollection<Object>();
		}
		listeners.add(listener);
	}

	public void addPropertyChangeListener(final String key, final PropertyChangeListener<Object> listener) {
		final PropertyChangeListenerCollection<Object> list;
		if (specificListners == null) {
			specificListners = new HashMap<String, PropertyChangeListenerCollection<Object>>();
		}
		if (specificListners.containsKey(key)) {
			list = specificListners.get(key);
		} else {
			list = new PropertyChangeListenerCollection<Object>();
			specificListners.put(key, list);
		}
		list.add(listener);
	}

	@Override
	public void removePropertyChangeListener(final PropertyChangeListener<Object> listener) {
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);

		if (specificListners == null) {
			return;
		}
		
		//XXX: This probably doesn't do what it intends to
		final Iterator<String> i = specificListners.keySet().iterator();
		while (i.hasNext()) {
			final String key = i.next();
			final PropertyChangeListenerCollection<?> value = specificListners.get(key);
			if (value.equals(listener)) {
				i.remove();
			}
		}
	}

	private static void checkKeyName(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("Key can not be 'null'.");
		}
		if (name.indexOf(' ') >= 0) {
			throw new IllegalArgumentException("Key '" + name + "' can not contain whitespace character(s).");
		}
		if (name.indexOf('*') >= 0) {
			throw new IllegalArgumentException("Key '" + name + "' can not contain '*' character(s).");
		}
	}

	public void putState(final String key, final StateGWT state) {
		checkReadOnly();
		checkKeyName(key);
		final Object oldVal;
		if (state != null) {
			oldVal = children.put(key, state);
			if (!state.isNull()) {
				setNull(false);
			}
		} else {
			oldVal = children.remove(key);
		}
		// ce to odstranimo look upi ne mapirajo pravilno nazaj vrednosti...
		if (oldVal != null) {
			((StateGWT)oldVal).removePropertyChangeListener(childStatesPcl);
		}
		if (state != null) {
			state.addPropertyChangeListener(childStatesPcl);
		}
		fireListenerChange(key, state, oldVal);
	}

	private void fireListenerChange(final String key, final Object newVal, final Object oldVal) {
		if (listeners != null) {
			listeners.fireChange(this, key, oldVal, newVal);
		}
		eventFilter.propertyChange(this, key, oldVal, newVal);
	}

	public boolean isEmpty() {
		if (isNull()) {
			return true;
		}
		return children.isEmpty() && map.isEmpty();
	}

	public boolean isNull() {
		return getBoolean(PROP_IS_NULL, false);
	}

	public void putStates(final String key, final StateGWT[] states) {
		checkKeyName(key);
		if (states != null) {
			putState(key, createListState(states));
		} else {
			putState(key, null);
		}
	}

	public StateGWT[] getStates(final String key) {
		return listToArray(getState(key, false));
	}

	public StateGWT createState(final String key) {
		final StateGWT s = new StateGWT();
		putState(key, s);
		return s;
	}

	public boolean getBoolean(final String key, final boolean defaultValue) {
		final String val = getString(key, null);
		if (val == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(val);
	}

	public StateGWT getState(final String key) {
		return getState(key, false);
	}

	public StateGWT getState(final String key, final boolean createIfNull) {
		StateGWT ret = children.get(key);
		if (createIfNull && ret == null) {
			putState(key, ret = new StateGWT(true));
		}
		return ret;
	}

	public double getDouble(final String key, final double defaultValue) {
		final String obj = map.get(key);
		if (obj == null) {
			return defaultValue;
		}
		return parseDbl(obj);
	}

	public Double getDouble(final String key, final Double defaultValue) {
		final String obj = map.get(key);
		if (obj == null)
			return defaultValue;

		return Double.valueOf(parseDbl(obj));
	}

	public double[] getDoubleSeq(final String key) {
		final int length = getInt(keyForLength(key), -1);

		if (length == -1)
			return null;

		final double[] retVal = new double[length];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getDouble(keyForIndex(key, i), Double.MIN_VALUE);
		}
		return retVal;
	}

	public int getInt(final String key, final int defaultValue) {
		final String obj = map.get(key);
		if (obj == null)
			return defaultValue;

		return parseInt(obj);
	}

	public Integer getInteger(final String key, final Integer defaultValue) {
		final String obj = map.get(key);
		if (obj == null)
			return defaultValue;

		return Integer.valueOf(parseInt(obj));
	}

	public int[] getIntSeq(final String key) {
		final int length = getInt(keyForLength(key), -1);

		if (length == -1)
			return null;

		final int[] retVal = new int[length];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getInt(keyForIndex(key, i), Integer.MIN_VALUE);
		}
		return retVal;
	}

	public long getLong(final String key, final long defaultValue) {
		final String obj = map.get(key);
		if (obj == null)
			return defaultValue;

		return parseLng(obj);
	}

	public String getString(final String key, final String defaultValue) {
		return ifnull(map.get(key), defaultValue);
	}

	/**
	 * @return State or String
	 */
	public Object getObject(final String key) {
		if (children.containsKey(key))
			return children.get(key);
		return map.get(key);
	}

	public String[] getStringSeq(final String key) {
		final int length = getInt(keyForLength(key), -1);

		if (length == -1)
			return null;

		final String[] retVal = new String[length];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getString(keyForIndex(key, i), null);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public Iterator<String> keyIterator() {
		return new KeyIterator(new HashMap[]{map, children});
	}

	public Set<String> keySet() {
		HashSet<String> ret = new HashSet<String>(map.keySet());
		ret.addAll(children.keySet());
		return ret;
	}

	public Map<String, String> getPropertyMap() {
		return Collections.unmodifiableMap(map);
	}

	public void putBoolean(final String key, final boolean value) {
		putString(key, Boolean.toString(value));
	}

	public void putDouble(final String key, final double value) {
		putString(key, String.valueOf(value));

	}

	public void putDoubleSeq(final String key, final double[] value) {
		if (value == null) {
			return;
		}
		putInt(keyForLength(key), value.length);
		for (int i = 0; i < value.length; i++) {
			putDouble(keyForIndex(key, i), value[i]);
		}
	}

	public void putInt(final String key, final int value) {
		putString(key.intern(), Integer.toString(value));
	}

	public void putInteger(final String key, final Integer value) {
		putString(key, StringUtil.toString(value));
	}

	public void putIntSeq(final String key, final int[] value) {
		if (value == null) {
			return;
		}

		putInt(keyForLength(key), value.length);

		for (int i = 0; i < value.length; i++) {
			putInt(keyForIndex(key, i), value[i]);
		}
	}

	public void putLong(final String key, final long value) {
		putString(key, String.valueOf(value));
	}


	public String putString(final String key, final String value) {
		checkReadOnly();
		checkKeyName(key);
		final String oldValue;
		if (value == null) {
			oldValue = map.remove(key);
		} else {
			oldValue = map.put(key.intern(), value.intern());
			if (!PROP_IS_NULL.equals(key)) {
				setNull(false);
			}
		}
		fireListenerChange(key, oldValue, value);
		return oldValue;
	}

	public void putStringSeq(final String key, final String[] value) {
		if (value == null) {
			return;
		}

		putInt(keyForLength(key), value.length);

		for (int i = 0; i < value.length; i++) {
			putString(keyForIndex(key, i), value[i]);
		}

	}

	@Override
	public String toString() {
		return toString("");
	}

	public String toString(final String prefix) {
		if (isNull())
			return prefix + "<null> state";
		final StringBuffer sb = new StringBuffer();
		final Iterator<String> iter = keyIterator();

		while (iter.hasNext()) {
			final Object key = iter.next();
			final Object value = getObject((String)key);
			sb.append(prefix);
			sb.append(key.toString());
			sb.append('=');
			if (value instanceof StateGWT) {
				final StateGWT ch = (StateGWT)value;
				if (!ch.isNull())
					sb.append("\n");
				sb.append(ch.toString("  " + prefix));
			} else {
				sb.append(value.toString());
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	/**
	 * msan iterator vsebuje <string,string> in <string,state>
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Map.Entry<String, Object>> entryIterator() {
		return new EntryIterator<String, Object>(new HashMap[]{map, children});
	}

	/**
	 * @return iterator over keys that are used to store primitive String values
	 */
	public Iterator<String> primitiveKeyIterator() {
		return map.keySet().iterator();
	}

	public boolean contains(final String key) {
		if (map.containsKey(key)) {
			return true;
		}
		return children.containsKey(key);
	}

	public boolean containsPrimitive(final String key) {
		return map.containsKey(key);
	}

	public boolean containsChild(final String key) {
		return children.containsKey(key);
	}

	/**
	 * Copies all values found in <code>otherSt</code> and sets them on this state object. The <code>deep</code>
	 * parameter controls how the child states are copied: <code>true</code> means that current children objects will be
	 * kept and their properties set from the corresponding otherSt's children (
	 * <code>getState(key,true).setFrom(otherSt.getState(key))</code>). <code>false</code> means that current children
	 * objects will be overwritten by the otherSt's children objects <code>putState(key,otherSt.getState(key))</code>
	 * 
	 * @param otherSt
	 * @param deep
	 */
	public void setFrom(final StateGWT otherSt, final boolean deep) {
		if (otherSt == null) {
			clear();
		} else {
			copyAttributes(otherSt);
			copyChildren(otherSt, deep);

			setNull(otherSt.isNull());

			fireListenerChange("", null, this);
		}
	}

	private void copyChildren(final StateGWT otherSt, final boolean deep) {
		// Pre-fetch to avoid concurrent update exception
		final HashSet<String> names = new HashSet<String>(otherSt.children.keySet());
		names.addAll(children.keySet());
		for (final String name : names) {
			if (otherSt.children.containsKey(name)) {
				if (deep) {
					final StateGWT child = getState(name, true);
					child.setFrom(otherSt.getState(name), deep);
					fireListenerChange(name, null, child);
				} else {
					putState(name, otherSt.getState(name));
				}
			} else {
				getState(name).clear();
			}
		}
	}

	private void copyAttributes(final StateGWT otherSt) {
		checkReadOnly();
		final HashSet<String> names = new HashSet<String>(map.keySet());
		names.addAll(otherSt.map.keySet());

		for (final String key : names) {
			if (otherSt.contains(key))
				putString(key, otherSt.getString(key, null));
			else
				putString(key, null);
		}
	}

	private void clear() {
		checkReadOnly();
		ArrayList<String> names = new ArrayList<String>(children.keySet());
		for (final String name : names) {
			final StateGWT curSt = getState(name);
			curSt.clear();
			fireListenerChange(name, null, curSt);
		}

		names = new ArrayList<String>(map.keySet());
		for (final String name : names) {
			putString(name, null);
		}
		setNull(true);
		fireListenerChange("", null, this);
	}

	private void checkReadOnly() {
		if (readOnly) {
			throw new IllegalStateException("Cannot modify values when readOnly is true");
		}
	}

	public static void putStringHierarchical(StateGWT ret, final String name, final String value) {
		final String[] nms = name.split("\\.");
		for (int i = 0; i < nms.length - 1; i++) {
			ret = ret.getState(nms[i], true);
		}
		ret.putString(nms[nms.length - 1], value);
	}

	public static StateGWT getStateHierarchical(final StateGWT parent, final String hierKey) {
		return getStateHierarchical(parent, hierKey.split("\\."));
	}

	public static StateGWT getStateHierarchical(final StateGWT parent, final String[] parts) {
		StateGWT ret = parent;
		for (final String part : parts) {
			ret = ret.getState(part, true);
		}
		return ret;
	}

	public static final String LEN_SUFFIX = "length";

	public static String keyForLength(final String baseKey) {
		return baseKey + '.' + LEN_SUFFIX;
	}

	public static String keyForIndex(final String baseKey, final int index) {
		return baseKey + '_' + index + '_';
	}

	public static int getListLength(final StateGWT listState) {
		return listState.getInt(LEN_SUFFIX, 0);
	}

	public static StateGWT getListElement(final StateGWT listState, final int index) {
		final boolean create = index >= 0 && index < getListLength(listState);
		return getListElement(listState, index, create);
	}

	public static StateGWT getListElement(final StateGWT listState, final int index, final boolean createIfNull) {
		return listState.getState(String.valueOf(index), createIfNull);
	}

	public static void addToList(final StateGWT listState, final StateGWT childState) {
		final int len = getListLength(listState);
		listState.putState(String.valueOf(len), childState);
		listState.putInt(LEN_SUFFIX, len + 1);
	}

	public static int findStateIndex(final StateGWT listState, final StateGWT childState) {
		for (final Map.Entry<String, StateGWT> ent : listState.children.entrySet()) {
			if (ent.getValue().equals(childState))
				return Integer.parseInt(ent.getKey());
		}
		return -1;
	}

	public static void main(final String[] args) {
		final StateGWT test = new StateGWT();
		final StateGWT el1 = new StateGWT();
		el1.putString("bla", "blabla");
		final StateGWT el2 = new StateGWT();
		el2.putString("bla", "2blabla");
		StateGWT.addToList(test, el1);
		StateGWT.addToList(test, el1);
		StateGWT.addToList(test, el2);
		System.out.println(test);
		System.out.println(findStateIndex(test, el2));
	}

	public static void removeFromList(final StateGWT listState, final int index) {
		final int len = getListLength(listState);
		for (int i = index; i < len; i++) {
			setListElement(listState, listState.getState(String.valueOf(i + 1)), i);
		}
		listState.putState(String.valueOf(len - 1), null);
		listState.putInt(LEN_SUFFIX, len - 1);
	}

	public static void setListElement(final StateGWT listState, final StateGWT childState, final int index) {
		final int len = getListLength(listState);
		if (index < 0 || index > len)
			throw new IllegalArgumentException("Invalid index " + index);

		listState.putState(String.valueOf(index), childState);

		if (index == len)
			listState.putInt(LEN_SUFFIX, len + 1);
	}

	/**
	 * Creates a State instance which stores a list of child States. It contains a "length" property for the list
	 * length; Children are stored under keys corresponding to indices in the list ("0" is the first element etc.)
	 */
	public static StateGWT createListState(final StateGWT[] children) {
		if (children == null) {
			return null;
		}
		final StateGWT listState = new StateGWT(false);
		listState.putInt(LEN_SUFFIX, children.length);
		for (int i = 0; i < children.length; i++) {
			listState.putState(String.valueOf(i), children[i]);
		}
		return listState;
	}

	public static List<StateGWT> readElementsWithPrefix(StateGWT st, String namePrefix) {
		//TODO: Use childKeyIterator and do this properly 
		// (filter keys for prefix, numeric-sort those with integer suffix first, append string-sorted others, put suffix only at the end, fetch states)
		//
		ArrayList<StateGWT> ret = null;

		StateGWT child = null;
		int idx = 0;
		int nullCnt = 0;
		do {
			String nm = namePrefix + idx;
			child = st.getState(nm);
			if (child == null) {
				nullCnt++;
			} else {
				nullCnt = 0;
				if (ret == null)
					ret = new ArrayList<StateGWT>();
				ret.add(child);
			}
			idx++;
		} while (nullCnt < 5);

		//Add the last one
		child = st.getState(namePrefix);
		if (child != null) {
			if (ret == null)
				ret = new ArrayList<StateGWT>();
			ret.add(child);
		}
		return ret;
	}

	public static StateGWT[] listToArray(final StateGWT listState) {
		if (listState == null)
			return null;
		final StateGWT[] st = new StateGWT[getListLength(listState)];
		for (int i = 0; i < st.length; i++) {
			st[i] = getListElement(listState, i);
		}
		return st;
	}

	public Iterator<String> childKeyIterator() {
		return children.keySet().iterator();
	}

	public boolean isAlowFireEvents() {
		if (listeners == null) {
			return false;
		}
		return listeners.isAlowFireEvents();
	}

	public void setAlowFireEvents(final boolean fireEvents) {
		if (listeners != null) {
			listeners.setAlowFireEvents(fireEvents);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StateGWT)) {
			return false;
		}
		final StateGWT other = (StateGWT)obj;
		if (children == null) {
			if (other.children != null) {
				return false;
			}
		} else if (!children.equals(other.children)) {
			return false;
		}
		if (map == null) {
			if (other.map != null) {
				return false;
			}
		} else if (!map.equals(other.map)) {
			return false;
		}
		return true;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
}