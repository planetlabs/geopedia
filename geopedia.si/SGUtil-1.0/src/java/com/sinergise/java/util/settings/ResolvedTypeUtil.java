package com.sinergise.java.util.settings;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.common.util.settings.Settings;
import com.sinergise.java.util.reflect.ClassUtil;

@SuppressWarnings("unchecked")
public class ResolvedTypeUtil {
	
	public static final char              LEFT_GEN_BRACKET  = '(';
	public static final char              RIGHT_GEN_BRACKET = ')';

	private static final Logger logger = LoggerFactory.getLogger(ResolvedTypeUtil.class);
	
	private static HashMap<Class<?>, String> defCSMap;
	private static HashMap<String, Class<?>> defSCMap;
	
	private static HashMap<Class<?>, String> getDefCSMap() {
		if (defCSMap == null) {
			defCSMap = new HashMap<Class<?>, String>();
		}
		return defCSMap;
	}
	
	private static HashMap<String, Class<?>> getDefSCMap() {
		if (defSCMap == null) {
			defSCMap = new HashMap<String, Class<?>>();
		}
		return defSCMap;
	}
	
	// private static final String TYPE_MAP_FIELDNAME = "TYPE_MAP";
	static {
		addToMapping("boolean", Boolean.class);
		addToMapping("byte", Byte.class);
		addToMapping("short", Short.class);
		addToMapping("int", Integer.class);
		addToMapping("long", Long.class);
		addToMapping("float", Float.class);
		addToMapping("double", Double.class);
		addToMapping("char", Character.class);
		addToMapping("string", String.class);
		addToMapping("bigint", BigInteger.class);
		addToMapping("bigdec", BigDecimal.class);
	}
	
	public static final void addToMapping(final String name, final Class<?> cls) {
		getDefCSMap().put(cls, name);
		getDefSCMap().put(name, cls);
	}
	
	public static <K, V> Map<K, V> buildMap(final K[] keys, final V[] values) {
		final HashMap<K, V> ret = new HashMap<K, V>();
		for (int i = 0; i < values.length; i++) {
			ret.put(keys[i], values[i]);
		}
		return ret;
	}
	
	public static final <T, C extends T> T constructInstance(final Class<C> klass, final boolean trySuper) throws InvocationTargetException {
		return (T)constructInstanceOrSuper(klass, Object.class, trySuper);
	}
	
	public static final <T> T constructInstance(final ResolvedType<T> type, final boolean trySuper) throws InvocationTargetException {
		return (T)constructInstanceOrSuper(type.rawType, Object.class, trySuper);
	}
	
	public static final <T, C extends T> T constructInstanceOrSuper(final Class<C> klass, final Class<T> endClass, final boolean trySuper) throws InvocationTargetException {
		Exception ex = null;
		try {
			final Constructor<C> c = klass.getDeclaredConstructor();
			c.setAccessible(true);
			return c.newInstance();
		} catch(final NoSuchMethodException e) {
			ex = e;
		} catch(final InstantiationException e) {
			ex = e;
		} catch(final IllegalAccessException e) {
			ex = e;
		}
		if (!trySuper || endClass.equals(klass)) {
			throw new IllegalArgumentException("Class '" + klass + "' could not be instantiated.", ex);
		}
		return (T)constructInstance(klass.getSuperclass(), trySuper);
	}
	
	public static final <T> T constructInstanceOrSuper(final ResolvedType<? extends T> actualType, final ResolvedType<T> declaredType) throws InvocationTargetException {
		return constructInstanceOrSuper(actualType.rawType, declaredType.rawType, true);
	}
	
	public static final <T> ResolvedType<T[]> createArray(final ResolvedType<T> componentType) {
		final Class<T> ct = componentType.rawType;
		return new ResolvedType<T[]>(toArrayClass(ct), componentType.parameterTypes);
	}
	
	public static final ResolvedType<?> getForField(final ResolvedType<?> parent, final Field f) {
		return resolve(parent, f.getGenericType());
	}
	
	public static final <C> ResolvedType<C> getForField(final ResolvedType<?> parent, final String fieldName) {
		try {
			Field fld = ClassUtil.getFieldAll(parent.rawType, fieldName);
			if (fld == null) {
				logger.warn("Field '{}' not found for type {}", fieldName, parent);
				return null;
			}
			return (ResolvedType<C>)getForField(parent, fld);
		} catch(final Exception e) {
			logger.warn("Exception getting field '{}' for type {}", fieldName, parent);
			return null;
		}
	}
	
	private static final Map<Class<?>, String> getMapCS(final Class<?> declared) {
		try {
			final Settings.TypeMap annot = declared.getAnnotation(Settings.TypeMap.class);
			if (annot != null) {
				final HashMap<Class<?>, String> ret = new HashMap<Class<?>, String>();
				for (int i = 0; i < annot.names().length; i++) {
					ret.put(annot.types()[i], annot.names()[i]);
				}
				return ret;
			}
			
		} catch(final Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static final Map<String, Class<?>> getMapSC(final Class<?> declared) {
		try {
			// try {
			// Field fMap = declared.getField(TYPE_MAP_FIELDNAME);
			// if (fMap != null) {
			// return (Map<String, Class>) fMap.get(null);
			// }
			// } catch (NoSuchFieldException nsfe) {
			// }
			
			final Settings.TypeMap annot = declared.getAnnotation(Settings.TypeMap.class);
			if (annot != null) {
				final HashMap<String, Class<?>> ret = new HashMap<String, Class<?>>();
				for (int i = 0; i < annot.names().length; i++) {
					ret.put(annot.names()[i], annot.types()[i]);
				}
				return ret;
			}
		} catch(final Exception e) {}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static final <T> ResolvedType<? extends T> readFromString(String str, final ResolvedType<T> declared, final Map<String, Class<?>> mapping) {
		try {
			if (str == null) {
				str = "";
			}
			if (str.endsWith("[]")) {
				ResolvedType<?> declComp = null;
				if (declared.rawType.isArray()) {
					declComp = new ResolvedType<T>((Class<T>)declared.rawType.getComponentType(), declared.parameterTypes);
				}
				final ResolvedType<?> compType = readFromString(str.substring(0, str.length() - 2), declComp, mapping);
				return new ResolvedType(toArrayClass(compType.rawType), compType.parameterTypes);
			}
			final int idx = str.indexOf(LEFT_GEN_BRACKET);
			String rawStr = null;
			if (idx < 0) {
				rawStr = str.trim();
			} else {
				rawStr = str.substring(0, idx);
			}
			Class<T> rawT = null;
			final Map<String, Class<?>> dMap = (declared == null) ? null : getMapSC(declared.rawType);
			if (dMap != null && dMap.containsKey(rawStr)) {
				rawT = (Class<T>)dMap.get(rawStr);
			} else if (mapping != null && mapping.containsKey(rawStr)) {
				rawT = (Class<T>)mapping.get(rawStr);
			} else if (getDefSCMap().containsKey(rawStr)) {
				rawT = (Class<T>)getDefSCMap().get(rawStr);
			} else if (rawStr.length() == 0) {
				rawT = declared == null ? null : declared.rawType;
			} else {
				rawT = (Class<T>)Class.forName(rawStr);
			}
			
			if (idx < 0) {
				return resolveSubClass(declared, rawT);
			}
			
			final String[] comps = splitString(str.substring(idx + 1, str.length() - 1));
			final ResolvedType<?>[] arr = new ResolvedType<?>[comps.length];
			for (int i = 0; i < arr.length; i++) {
				ResolvedType<?> pt = null;
				if (declared != null && declared.parameterTypes != null && declared.parameterTypes.length > i) {
					pt = declared.parameterTypes[i];
				}
				arr[i] = readFromString(comps[i], pt, mapping);
			}
			return new ResolvedType(rawT, arr);
		} catch(final ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static final void removeMapping(final String name, final Class<?> cls) {
		if (name.equals(getDefCSMap().get(cls)) && cls.equals(getDefSCMap().get(name))) {
			getDefCSMap().remove(cls);
			getDefSCMap().remove(name);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static final ResolvedType<?> resolve(final ResolvedType<?> parent, final Type childType) {
		if (childType instanceof Class) {
			return new ResolvedType((Class<?>)childType);
		} else if (childType instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType)childType;
			final ResolvedType rt = resolve(parent, pt.getRawType());
			final Type[] typs = pt.getActualTypeArguments();
			return new ResolvedType(rt.rawType, resolveArguments(parent, typs));
			
		} else if (childType instanceof TypeVariable) {
			final TypeVariable tv = (TypeVariable)childType;
			final TypeVariable[] parentVars = parent.rawType.getTypeParameters();
			for (int i = 0; i < parentVars.length; i++) {
				if (parentVars[i].getName().equals(tv.getName())) {
					return parent.parameterTypes[i];
				}
			}
			return resolve(resolve(parent, parent.rawType.getGenericSuperclass()), childType);
			
		} else if (childType instanceof GenericArrayType) {
			final Type ct = ((GenericArrayType)childType).getGenericComponentType();
			final ResolvedType rt = resolve(parent, ct);
			final Class cl = toArrayClass(rt.rawType);
			return new ResolvedType(cl, rt.parameterTypes);
			
		} else if (childType instanceof WildcardType) {
			final WildcardType wt = (WildcardType)childType;
			final Type[] upB = wt.getUpperBounds();
			if (upB != null && upB.length > 0) {
				return resolve(parent, upB[0]);
			}
			return new ResolvedType(Object.class);
			
		}
		return null;
	}
	
	public static final ResolvedType<?>[] resolveArguments(final ResolvedType<?> parent, final Type[] typs) {
		final ResolvedType<?>[] rt = new ResolvedType<?>[typs.length];
		for (int i = 0; i < rt.length; i++) {
			rt[i] = resolve(parent, typs[i]);
		}
		return rt;
	}
	
	public static final <T> ResolvedType<T> resolveSubClass(final ResolvedType<? super T> superClass, final Class<T> subClass) {
		if (subClass.isAssignableFrom(superClass.rawType)) {
			return (ResolvedType<T>)superClass;
		}
		final Class<? super T> parClass = superClass.rawType;
		final TypeVariable<?>[] parTyps = parClass.getTypeParameters();
		final TypeVariable<?>[] subTyps = subClass.getTypeParameters();
		if (variablesEquals(parTyps, subTyps)) {
			final ResolvedType<?>[] parTypes = resolveArguments(superClass, subTyps);
			return new ResolvedType<T>(subClass, parTypes);
		}
		return new ResolvedType<T>(subClass);
	}
	
	private static final String[] splitString(final String str) {
		final ArrayList<String> ret = new ArrayList<String>();
		int brCnt = 0;
		int curStart = 0;
		for (int i = 0; i < str.length(); i++) {
			final char ch = str.charAt(i);
			if (brCnt == 0 && ch == ',') {
				ret.add(str.substring(curStart, i).trim());
				curStart = i + 1;
			} else if (ch == LEFT_GEN_BRACKET) {
				brCnt++;
			} else if (ch == RIGHT_GEN_BRACKET) {
				brCnt--;
			}
		}
		ret.add(str.substring(curStart).trim());
		return ret.toArray(new String[ret.size()]);
	}
	
	public static final <T> Class<T[]> toArrayClass(final Class<T> compType) {
		return (Class<T[]>)Array.newInstance(compType, 0).getClass();
	}
	
	private static boolean variableEquals(final TypeVariable<?> a, final TypeVariable<?> b) {
		return Arrays.equals(a.getBounds(), b.getBounds());
	}
	
	private static boolean variablesEquals(final TypeVariable<?>[] a, final TypeVariable<?>[] b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		if (a == b) {
			return true;
		}
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < b.length; i++) {
			if (!variableEquals(a[i], b[i])) {
				return false;
			}
		}
		return true;
	}
	
	public static final String writeToString(final ResolvedType<?> type, final ResolvedType<?> declared, final Map<Class<?>, String> mapping) {
		final StringBuffer ret = new StringBuffer();
		final boolean arr = type.rawType.isArray();
		Class<?> cls = null;
		if (arr) {
			cls = type.rawType.getComponentType();
		} else {
			cls = type.rawType;
		}
		String rawSt = null;
		final Map<Class<?>, String> dMap = declared == null ? null : getMapCS(declared.rawType);
		if (dMap != null && dMap.containsKey(cls)) {
			rawSt = dMap.get(cls);
		} else if (mapping != null && mapping.containsKey(cls)) {
			rawSt = mapping.get(cls);
		} else if (getDefCSMap().containsKey(cls)) {
			rawSt = getDefCSMap().get(cls);
		} else {
			rawSt = cls.getName();
		}
		ret.append(rawSt);
		if (type.parameterTypes != null && type.parameterTypes.length > 0) {
			if ((rawSt == null || rawSt.length() == 0) && (declared == null || ArrayUtil.equals(type.parameterTypes, declared.parameterTypes))) {
				return null;
			}
			if (!arr && declared != null && variablesEquals(type.rawType.getTypeParameters(), declared.rawType.getTypeParameters())
			    && ArrayUtil.equals(type.parameterTypes, declared.parameterTypes)) {
				return rawSt;
			}
			ret.append(LEFT_GEN_BRACKET);
			for (int i = 0; i < type.parameterTypes.length; i++) {
				if (i > 0) {
					ret.append(", ");
				}
				ResolvedType<?> pt = null;
				if (declared != null && declared.parameterTypes != null && declared.parameterTypes.length > i) {
					pt = declared.parameterTypes[i];
				}
				final String typeStr = writeToString(type.parameterTypes[i], pt, mapping);
				if (typeStr != null) {
					ret.append(typeStr);
				}
			}
			ret.append(RIGHT_GEN_BRACKET);
		}
		if (arr) {
			ret.append("[]");
		}
		return ret.toString();
	}
	
}
