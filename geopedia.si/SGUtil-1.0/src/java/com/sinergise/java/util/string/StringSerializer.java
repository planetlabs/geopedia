/*
 *
 */
package com.sinergise.java.util.string;

import static com.sinergise.common.util.string.StringTransformUtil.addTransformer;
import static com.sinergise.common.util.string.StringTransformUtil.getTransformer;
import static com.sinergise.common.util.string.StringTransformUtil.forceGetTransformer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.common.util.settings.Settings.SerializeAsString;
import com.sinergise.common.util.string.Escaper;
import com.sinergise.common.util.string.StringTransformer;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.util.reflect.ClassUtil;
import com.sinergise.java.util.settings.ResolvedTypeUtil;

@SuppressWarnings("unchecked")
public class StringSerializer {
	private static final Logger logger = LoggerFactory.getLogger(StringSerializer.class);
	
	public static final char                               ARRAY_SEPARATOR = '|';
	public static final char                               ARRAY_L         = '{';
	public static final char                               ARRAY_R         = '}';
	
	static {
		addTransformer(Byte.TYPE, new StringTransformer.Byt());
		addTransformer(Short.TYPE, new StringTransformer.Sht());
		addTransformer(Integer.TYPE, new StringTransformer.Int());
		addTransformer(Long.TYPE, new StringTransformer.Lng());
		addTransformer(Float.TYPE, new StringTransformer.Flt());
		addTransformer(Double.TYPE, new StringTransformer.Dbl());
		addTransformer(Boolean.TYPE, new StringTransformer.Bool());
		addTransformer(Character.TYPE, new StringTransformer.Chr());
		
		addTransformer(File.class, new StringTransformersJava.TrFile());
		addTransformer(Dimension.class, new StringTransformersJava.Dim());
		addTransformer(Rectangle2D.Double.class, new StringTransformersJava.Rect());
		addTransformer(Rectangle.class, new StringTransformersJava.Rect());
		addTransformer(Color.class, new StringTransformersJava.Clr());
		addTransformer(Class.class, new StringTransformersJava.Cls());
		addTransformer(ResolvedType.class, new StringTransformersJava.ResTyp());
		addTransformer(Font.class, new StringTransformersJava.Fnt());
	}
	
	public static final <T> String store(final T obj, final Class<? super T> cls) {
		if (obj == null) {
			return "";
		}
		if (cls.isArray()) {
			if (cls.getComponentType().isPrimitive()) {
				return storePrimitiveArray(obj);
			}
			return storeArray((Object[])obj, (Class<Object[]>)cls);
		}
		if (cls.isEnum()) {
			return ((Enum<?>)obj).name();
		}
		return forceGetTransformer(cls).store(obj);
	}
	
	public static final <T> boolean canStore(final Class<T> objClass) {
		if (objClass == null) {
			return true;
		}
		if (objClass.isArray()) {
			if (objClass.getComponentType().isPrimitive()) {
				return true;
			}
			return canStore(objClass.getComponentType());
		}
		if (objClass.isEnum()) {
			return true;
		}
		@SuppressWarnings("rawtypes")
		final StringTransformer tr = getTransformer(objClass);
		if (tr == null) {
			return checkAnnotations(objClass);
		}
		return true;
	}

	protected static <T> boolean checkAnnotations(final Class<T> objClass) {
		SerializeAsString ann = objClass.getAnnotation(SerializeAsString.class);
		if (ann == null) return false;
		try {
			Class<? extends StringTransformer<T>> transf = (Class<? extends StringTransformer<T>>)ann.value();
			if (transf == null) return false;
			Constructor<? extends StringTransformer<T>> constructor = transf.getConstructor();
			constructor.setAccessible(true);
			addTransformer(objClass, constructor.newInstance());
			return true;
		} catch(Exception e) {
			logger.error("Retrieving annotation-specified string transformer.", e);
			return false;
		}
	}
	
	private static final Escaper escaper = new Escaper(new char[]{ARRAY_SEPARATOR});
	
	public static final String storePrimitiveArray(final Object arr) {
		final StringBuffer buf = new StringBuffer(100);
		buf.append(ARRAY_L); // Enables distinction between null and empty array
		
		@SuppressWarnings("rawtypes")
		final StringTransformer tr = getTransformer(arr.getClass().getComponentType());
		final int len = Array.getLength(arr);
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				buf.append(ARRAY_SEPARATOR);
			}
			if (tr == null) {
				buf.append(escaper.escapeComponent(Array.get(arr, i).toString()));
			} else {
				buf.append(escaper.escapeComponent(tr.store(Array.get(arr, i))));
			}
		}
		buf.append(ARRAY_R);
		return buf.toString();
	}
	
	public static final <T> String storeArray(final T[] arr, final Class<? super T[]> arrCl) {
		return storeArray(arr, arrCl, ARRAY_SEPARATOR);
	}
	public static final <T> String storeArray(final T[] arr, final Class<? super T[]> arrCl, char separator) {
		final StringBuffer buf = new StringBuffer(100);
		buf.append(ARRAY_L); // Enables distinction between null and empty array
		
		final int len = arr.length;
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				buf.append(separator);
			}
			buf.append(escaper.escapeComponent(store(arr[i], (Class<? super T>)arrCl.getComponentType())));
		}
		buf.append(ARRAY_R);
		return buf.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public static final <T> T valueOf(final String s, final Class<T> target) {
		if (StringUtil.isNullOrEmpty(s)) {
			return null;
		}
		if (target.isArray()) {
			return valueOfArray(s, target);
		}
		if (Enum.class.isAssignableFrom(target)) {
			return (T)Enum.valueOf((Class)target, s);
		}
		final StringTransformer tr = getTransformer(target);
		if (tr == null) {
			if (checkAnnotations(target)) {
				return getTransformer(target).valueOf(s);
			}
			T ret = valueOfWithSuper(s, target);
			if (ret != null) {
				return ret;
			}
			throw new IllegalArgumentException("No transformer for type " + target);
		}
		return (T)tr.valueOf(s);
	}
	
	private static final <T> T valueOfWithSuper(final String s, final Class<T> target) {
		Class<?> superClass = target.getSuperclass();
		if (superClass == Object.class) {
			return null;
		}
		Object superVal = valueOf(s, superClass);
		if (superVal == null) {
			return null;
		}
		try {
			T concrete = ResolvedTypeUtil.constructInstance(target, false);
			ClassUtil.copyFields(superVal, concrete);
			return concrete;
		} catch(Exception e) {
			return null;
		}
	}
	
	public static final <T> T valueOfArray(String s, final Class<T> target) {
		if (!(s.charAt(0) == ARRAY_L && s.charAt(s.length() - 1) == ARRAY_R)) {
			throw new IllegalArgumentException("Illegal array string (should be enclosed in " + ARRAY_L + ARRAY_R + ")");
		}
		s = s.substring(1, s.length() - 1);
		final String[] comps = s.isEmpty() ? new String[0] : escaper.stringToComponents(s);
		final int len = comps.length;
		final Class<?> compType = target.getComponentType();
		final T newArr = (T)Array.newInstance(compType, len);
		for (int i = 0; i < len; i++) {
			Array.set(newArr, i, valueOf(comps[i], compType));
		}
		return newArr;
	}
}
