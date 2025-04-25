/*
 *
 */
package com.sinergise.java.util.settings;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.sinergise.common.util.Util;
import com.sinergise.common.util.collections.DEQueue;
import com.sinergise.common.util.settings.NamedTypedObject;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.java.util.reflect.ClassUtil;
import com.sinergise.java.util.string.StringSerializer;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class MapTransformer<T> {
	public static class TrGeneric<T> extends MapTransformer<T> {
		@Override
		public <C> ResolvedType<C> childType(final ResolvedType<T> parentType, final String name, final boolean complex) {
			if (StringSerializer.canStore(parentType.rawType)) {
				if (complex) return (ResolvedType<C>)parentType;
//				else System.err.println("Expected plain string-encoded instance of "+parentType+", but got attribute: "+name);
			}
			try {
				return ResolvedTypeUtil.getForField(parentType, name);
			} catch(final Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public T setChildren(final T parent, final ResolvedType<T> parentType, final List<? extends NamedTypedObject> myVals) {
			for (final NamedTypedObject cur : myVals) {
				setFieldValue(parent, parentType, cur);
			}
			return parent;
		}

		protected void setFieldValue(final T parent, final ResolvedType<T> parentType, final NamedTypedObject<?> value) {
			try {
				final Field fld = ClassUtil.getFieldAll(parent.getClass(), value.name);
				fld.setAccessible(true);
				fld.set(parent, value.value);
			} catch(final Exception e) {
				System.err.println("Field " + value.name + " could not be set on " + parent.getClass() + " (expected " + parentType + ")");
				e.printStackTrace();
			}
		}

		@Override
		public List<NamedTypedObject> getChildren(final T parent, final ResolvedType<? extends T> parentType, final ResolvedType<T> declaredType) {
			if (StringSerializer.canStore(parentType.rawType)) {
				final ArrayList<NamedTypedObject> rarr = new ArrayList<NamedTypedObject>();
				rarr.add(new NamedTypedObject("value", parentType, parent));
				return rarr;
			}
			final ArrayList<NamedTypedObject> ret = new ArrayList<NamedTypedObject>();
			final Class cls = parentType.rawType;
			final Field[] allFlds = ClassUtil.getAllFields(cls);
			Object defaultParent = null;
			try {
				defaultParent = constructDefault(parentType, declaredType);
			} catch(final Exception e) {
				System.err.println("Failed to construct "+parentType+" to determine default values.");
			}
			for (final Field f : allFlds) {
				f.setAccessible(true);
				if (Modifier.isTransient(f.getModifiers())) continue;
				if (Modifier.isStatic(f.getModifiers())) continue;
				try {
					if (isDefault(f, parent, defaultParent)) continue;
					ret.add(new NamedTypedObject(f.getName(), ResolvedTypeUtil.getForField(parentType, f), f.get(parent)));
				} catch(final Exception e) {}
			}
			return ret;
		}

		private static <T> T constructDefault(final ResolvedType<? extends T> parentType, final ResolvedType<T> declaredType) throws InvocationTargetException {
			return ResolvedTypeUtil.constructInstanceOrSuper(parentType, declaredType);
		}

		@Override
		public boolean canProcess(final Class<?> type) {
			return true;
		}

		@Override
		public T createParentOrSuper(final ResolvedType<T> actType, final ResolvedType<T> decType) {
			return super.createParentOrSuper(actType, decType);
		}
	}

	public static class TrArray extends MapTransformer<Object[]> {
		{
			addTypeMapping(Object[].class, "", Object[].class);
		}

		@Override
		public <C> ResolvedType<C> childType(final ResolvedType<Object[]> parentType, final String name, final boolean complex) {
			return new ResolvedType(parentType.rawType.getComponentType());
		}

		@Override
		public List<NamedTypedObject> getChildren(final Object[] parent, final ResolvedType<? extends Object[]> actualType, final ResolvedType<Object[]> declaredType) {
			final ArrayList<NamedTypedObject> ret = new ArrayList<NamedTypedObject>(parent.length);
			final ResolvedType compType = new ResolvedType(actualType.rawType.getComponentType(), actualType.parameterTypes);
			for (final Object element : parent) {
				ret.add(new NamedTypedObject("item", compType, element));
			}
			return ret;
		}

		@Override
		public Object[] setChildren(Object[] parent, final ResolvedType<Object[]> parentType, final List<? extends NamedTypedObject> myVals) {
			if (parent == null || parent.length != myVals.size()) {
				parent = (Object[])Array.newInstance(parentType.rawType.getComponentType(), myVals.size());
			}
			for (int i = 0; i < parent.length; i++) {
				parent[i] = myVals.get(i).value;
			}
			return parent;
		}

		@Override
		public Object[] createParentOrSuper(final ResolvedType<Object[]> actType, final ResolvedType<Object[]> decType) {
			return (Object[])Array.newInstance(actType.rawType.getComponentType(), 0);
		}

		@Override
		public boolean canProcess(final Class type) {
			return Object[].class.isAssignableFrom(type);
		}
	}

	public static class TrSet extends MapTransformer<Set> {
		{
			addTypeMapping(Set.class, "", HashSet.class);
			addTypeMapping(Set.class, "LINKED", LinkedHashSet.class);
			addTypeMapping(Set.class, "TREE", TreeSet.class);
		}
		
		@Override
		public Set createParentOrSuper(final ResolvedType<Set> actType, final ResolvedType<Set> decType) {
			try {
				return super.createParentOrSuper(actType, decType);
			} catch(final Exception e) {
				return new HashSet();
			}
		}

		private static ResolvedType getCompType(final ResolvedType<? extends Set> parentType) {
			ResolvedType compT = null;
			if (parentType.parameterTypes == null || parentType.parameterTypes.length == 0) {
				try {
					final Type typ = parentType.rawType.getMethod("add", Object.class).getGenericParameterTypes()[0];
					compT = ResolvedTypeUtil.resolve(parentType, typ);
				} catch(final Exception e) {}
				if (compT == null) {
					compT = new ResolvedType(Object.class);
				}
			} else {
				compT = parentType.parameterTypes[0];
			}
			return compT;
		}

		@Override
		public <C> ResolvedType<C> childType(final ResolvedType<Set> parentType, final String name, final boolean complex) {
			final ResolvedType compT = getCompType(parentType);
			if (StringSerializer.canStore(compT.rawType)) {
				return ResolvedTypeUtil.createArray(compT);
			}
			return compT;
		}

		@Override
		public List<NamedTypedObject> getChildren(final Set parent, final ResolvedType<? extends Set> parentType, final ResolvedType<Set> declaredType) {
			final ArrayList<NamedTypedObject> ret = new ArrayList<NamedTypedObject>();
			// int idx = 0;
			final ResolvedType compType = getCompType(parentType);

			if (StringSerializer.canStore(compType.rawType)) {
				final Object[] ar = (Object[])Array.newInstance(compType.rawType, parent.size());
				parent.toArray(ar);
				ret.add(new NamedTypedObject("values", ResolvedTypeUtil.createArray(compType), ar));
				return ret;
			}
			for (final Object o : parent) {
				ret.add(new NamedTypedObject("item", compType, o));
			}
			return ret;
		}

		@Override
		public Set setChildren(final Set parent, final ResolvedType<Set> parentType, final List<? extends NamedTypedObject> myVals) {
			final ResolvedType compType = getCompType(parentType);
			parent.clear();
			if (StringSerializer.canStore(compType.rawType)) {
				final Object[] arr = (Object[])myVals.get(0).value;
				for (final Object element : arr) {
					parent.add(element);
				}
				return parent;
			}
			for (final NamedTypedObject object : myVals) {
				parent.add(object.value);
			}
			return parent;
		}

		@Override
		public boolean canProcess(final Class type) {
			return Set.class.isAssignableFrom(type);
		}
	}

	public static class TrMap extends MapTransformer<Map> {
		{
			addTypeMapping(Map.class, "", HashMap.class);
			addTypeMapping(Map.class, "TABLE", Hashtable.class);
			addTypeMapping(Map.class, "LINKED", LinkedHashMap.class);
			addTypeMapping(Map.class, "TREE", TreeMap.class);
		}

		public static class Item<K, V> {
			public K	key;
			public V	value;

			public Item() {}

			public Item(final Entry<K, V> entry) {
				this.key = entry.getKey();
				this.value = entry.getValue();
			}
		}

		private static ResolvedType createEntryType(final ResolvedType<? extends Map> parentType) {
			ResolvedType keyT = null;
			if (parentType.parameterTypes == null || parentType.parameterTypes.length == 0) {
				try {
					final Method m = parentType.rawType.getMethod("keySet", new Class[0]);
					final Type t = ((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0];
					keyT = ResolvedTypeUtil.resolve(parentType, t);
				} catch(final Exception e) {
					e.printStackTrace();
				}
				if (keyT == null) {
					keyT = new ResolvedType(Object.class);
				}
			} else {
				keyT = parentType.parameterTypes[0];
			}
			ResolvedType valT = null;
			if (parentType.parameterTypes == null || parentType.parameterTypes.length < 2) {
				try {
					valT =
							ResolvedTypeUtil.resolve(parentType, parentType.rawType.getMethod("get", new Class[]{Object.class})
									.getGenericReturnType());
				} catch(final Exception e) {
					e.printStackTrace();
				}
				if (valT == null) {
					valT = new ResolvedType(Object.class);
				}
			} else {
				valT = parentType.parameterTypes[1];
			}
			return new ResolvedType<Item>(Item.class, new ResolvedType[]{keyT, valT});
		}

		@Override
		public <C> ResolvedType<C> childType(final ResolvedType<Map> parentType, final String name, final boolean complex) {
			final ResolvedType et = createEntryType(parentType);
			// if (String.class.equals(et.parameterTypes[0].rawType)) {
			// return et.parameterTypes[0];
			// }
			return et;
		}

		@Override
		public List<NamedTypedObject> getChildren(final Map parent, final ResolvedType<? extends Map> parentType, final ResolvedType<Map> declaredType) {
			final ArrayList<NamedTypedObject> ret = new ArrayList<NamedTypedObject>();
			// int idx = 0;
			final ResolvedType entryType = createEntryType(parentType);
			// if (String.class.equals(entryType.parameterTypes[0].rawType)) {
			// for (Object o : parent.entrySet()) {
			// Map.Entry e=(Map.Entry)o;
			// ret.add(new NamedTypedObject((String)e.getKey(),entryType.parameterTypes[1],e.getValue()));
			// }
			// return ret;
			// }
			for (final Object o : parent.entrySet()) {
				final Map.Entry e = (Map.Entry)o;
				ret.add(new NamedTypedObject("item", entryType, new Item(e)));
			}
			return ret;
		}

		@Override
		public Map setChildren(final Map parent, final ResolvedType<Map> parentType, final List<? extends NamedTypedObject> myVals) {
			// ResolvedType entryType=
			createEntryType(parentType);
			parent.clear();

			// if (String.class.equals(entryType.parameterTypes[0].rawType)) {
			// for (NamedTypedObject it : myVals) {
			// parent.put(it.name, it.value);
			// }
			// } else {
			for (final NamedTypedObject<Item> it : myVals) {
				parent.put(it.value.key, it.value.value);
			}
			// }
			return parent;
		}

		@Override
		public boolean canProcess(final Class type) {
			return Map.class.isAssignableFrom(type);
		}
	}

	public static class TrList extends MapTransformer<List> {
		static Class<?>	arrayAsList;

		{
			addTypeMapping(List.class, "", ArrayList.class);
			addTypeMapping(List.class, "LINKED_LIST", LinkedList.class);
			addTypeMapping(List.class, "VECTOR", Vector.class);
			addTypeMapping(List.class, "STACK", Stack.class);
			addTypeMapping(List.class, "DEQUEUE", DEQueue.class);
			try {
				addTypeMapping(List.class, "IMMUTABLE_LIST", (Class<? extends List>)(arrayAsList = Class.forName("java.util.Arrays$ArrayList")));
			} catch(final ClassNotFoundException e) {
				throw new RuntimeException("Should not happen");
			}
		}
		
		@Override
		public List createParentOrSuper(final ResolvedType<List> actType, final ResolvedType<List> decType) {
			try {
				if (arrayAsList != null && arrayAsList.equals(actType.rawType)) {
					return Arrays.asList((Object[])Array.newInstance(actType.rawType.getComponentType(), 0));
				}
				return super.createParentOrSuper(actType, decType);
			} catch(final Exception e) {
				return new ArrayList();
			}
		}
	
		private static ResolvedType getCompType(final ResolvedType<? extends List> parentType) {
			ResolvedType compT = null;
			if (parentType.parameterTypes == null || parentType.parameterTypes.length == 0) {
				try {
					final Type typ = parentType.rawType.getMethod("get", new Class[]{Integer.TYPE}).getGenericReturnType();
					compT = ResolvedTypeUtil.resolve(parentType, typ);
				} catch(final Exception e) {}
				if (compT == null) {
					compT = new ResolvedType(Object.class);
				}
			} else {
				compT = parentType.parameterTypes[0];
			}
			return compT;
		}
	
		@Override
		public <C> ResolvedType<C> childType(final ResolvedType<List> parentType, final String name, final boolean complex) {
			final ResolvedType compT = getCompType(parentType);
			if (StringSerializer.canStore(compT.rawType)) {
				return ResolvedTypeUtil.createArray(compT);
			}
			return compT;
		}
	
		@Override
		public List<NamedTypedObject> getChildren(final List parent, final ResolvedType<? extends List> parentType, final ResolvedType<List> declaredType) {
			final ArrayList<NamedTypedObject> ret = new ArrayList<NamedTypedObject>();
			// int idx = 0;
			final ResolvedType compType = getCompType(parentType);
	
			if (StringSerializer.canStore(compType.rawType)) {
				final Object[] ar = (Object[])Array.newInstance(compType.rawType, parent.size());
				parent.toArray(ar);
				ret.add(new NamedTypedObject("values", ResolvedTypeUtil.createArray(compType), ar));
				return ret;
			}
			for (final Object o : parent) {
				ret.add(new NamedTypedObject("item", compType, o));
			}
			return ret;
		}
	
		@Override
		public List setChildren(final List parent, final ResolvedType<List> parentType, final List<? extends NamedTypedObject> myVals) {
			final ResolvedType compType = getCompType(parentType);
			boolean asList = false;
			if (arrayAsList != null && arrayAsList.equals(parentType.rawType)) {
				asList = true;
			} else {
				parent.clear();
			}
			if (StringSerializer.canStore(compType.rawType)) {
				final Object[] arr = (Object[])myVals.get(0).value;
				if (asList) {
					return Arrays.asList(arr);
				}
				for (final Object element : arr) {
					parent.add(element);
				}
				return parent;
			}
			if (asList) {
				final Object[] arr = new Object[myVals.size()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = myVals.get(i).value;
				}
				return Arrays.asList(arr);
			}
			for (final NamedTypedObject object : myVals) {
				parent.add(object.value);
			}
			return parent;
		}
	
		@Override
		public boolean canProcess(final Class type) {
			return List.class.isAssignableFrom(type);
		}
	}

	private static final List<MapTransformer>		trs		= new ArrayList<MapTransformer>();

	private static final HashMap<Class<?>, HashMap<String, Class<?>>> classForName = new HashMap<Class<?>, HashMap<String,Class<?>>>();
	private static final HashMap<Class<?>, HashMap<Class<?>, String>> nameForClass = new HashMap<Class<?>, HashMap<Class<?>, String>>();
	
	static {
		// trs.add(new MapTransformer.TrState());
		// trs.add(new MapTransformer.TrStateOriginator());
		trs.add(new MapTransformer.TrSet());
		trs.add(new MapTransformer.TrArray());
		trs.add(new MapTransformer.TrList());
		trs.add(new MapTransformer.TrMap());
		trs.add(new MapTransformer.TrGeneric());
	}
	
	public static void registerMapTransformer(MapTransformer<?> transformer) {
		trs.add(0, transformer);
	}

	public static boolean isDefault(final Field f, final Object parent, final Object defaultParent) {
		try {
			if (defaultParent == null) {
				return false;
			}
			return Util.safeEquals(f.get(parent), f.get(defaultParent));
		} catch(final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String encodeParentType(final T parent, final ResolvedType<T> expectedType) {
		if (parent.getClass().equals(expectedType.rawType)) {
			return null;
		}
		final ResolvedType<T> actual = new ResolvedType(parent.getClass(), expectedType.parameterTypes);
		return ResolvedTypeUtil.writeToString(actual, expectedType, getClassToNameMapping(expectedType.rawType));
	}

	public T createParentOrSuper(final ResolvedType<T> actType, final ResolvedType<T> decType) {
		try {
			return ResolvedTypeUtil.constructInstanceOrSuper(actType, decType);
		} catch(final Exception e) {
			throw new IllegalArgumentException("Object of type " + actType.rawType + " could not be instantiated.", e);
		}
	}
	
	public ResolvedType<? extends T> resolveActualType(String typeString, ResolvedType<T> declaredType) {
		return ResolvedTypeUtil.readFromString(typeString, declaredType, getNameToClassMapping(declaredType.rawType));
	}

	private static Map<String, Class<?>> getNameToClassMapping(Class<?> declaredType) {
		return classForName.get(declaredType);
	}
	

	private static Map<Class<?>, String> getClassToNameMapping(Class<?> declaredType) {
		return nameForClass.get(declaredType);
	}

	abstract public T setChildren(T parent, ResolvedType<T> parentType, List<? extends NamedTypedObject> myVals);

	abstract public List<NamedTypedObject> getChildren(T parent, ResolvedType<? extends T> actualType, final ResolvedType<T> declaredType);

	abstract public <C> ResolvedType<C> childType(ResolvedType<T> parentType, String name, boolean complex);

	abstract public boolean canProcess(Class<?> type);

	public static <T> MapTransformer<T> getFor(final Class<? extends T> type) {
		if (type == null) {
			throw new NullPointerException("Class in getForType");
		}
		for (final MapTransformer e : trs) {
			if (e.canProcess(type)) {
				return e;
			}
		}
		// System.out.println(type);
		return null;
	}

	/**
	 * @param cur
	 * @return true if the child needs a <code><null/></code> element to be stored; 
	 * 				false if null can be inferred from other information stored 
	 */
	public boolean storeNullChild(final NamedTypedObject cur) {
		return true;
	}

	protected static <T> void addTypeMapping(final Class<? super T> declaredClass, final String targetName, final Class<T> targetClass) {
		addTypeMapping(declaredClass, targetName, targetClass, true);
	}
	private static <T> void addTypeMapping(final Class<? super T> declaredClass, final String targetName, final Class<T> targetClass, boolean overwrite) {
		if (declaredClass == null || targetClass == null || !declaredClass.isAssignableFrom(targetClass)) {
			return;
		}
		HashMap<Class<?>, String> nForC = nameForClass.get(declaredClass);
		if (nForC == null) {
			nForC = new HashMap<Class<?>, String>();
			nameForClass.put(declaredClass, nForC);
		}
		
		HashMap<String, Class<?>> cForN = classForName.get(declaredClass);
		if (cForN == null) {
			cForN = new HashMap<String, Class<?>>();
			classForName.put(declaredClass, cForN);
		}
		if (overwrite || !(nForC.containsKey(targetClass) || cForN.containsKey(targetName))) {
			nForC.put(targetClass, targetName);
			cForN.put(targetName, targetClass);
			
			addTypeMapping(targetClass.getSuperclass(), targetName, targetClass, false);
			for (Class<?> cls : targetClass.getInterfaces()) {
				addTypeMapping((Class<? super T>)cls, targetName, targetClass, false);
			}
		
			addTypeMapping(declaredClass.getSuperclass(), targetName, targetClass, false);
			for (Class<?> cls : declaredClass.getInterfaces()) {
				addTypeMapping((Class<? super T>)cls, targetName, targetClass, false);
			}
		}
	}
}
