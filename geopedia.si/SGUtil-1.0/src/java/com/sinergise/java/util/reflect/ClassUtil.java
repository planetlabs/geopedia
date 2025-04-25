package com.sinergise.java.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides helper methods to easily manipulate an analyze classes and their members.
 * 
 * @author Miha Kadunc
 */
public class ClassUtil {
	public static Field[] getAllFields(final Class<?> clazz) {
		final List<Field> ret = new ArrayList<Field>();
		addAllFields(clazz, ret);
		return ret.toArray(new Field[ret.size()]);
	}
	
	public static void addAllFields(final Class<?> clazz, final List<Field> retList) {
		retList.addAll(Arrays.asList(clazz.getDeclaredFields()));
		final Class<?> sup = clazz.getSuperclass();
		if (sup != null) {
			addAllFields(sup, retList);
		}
	}
	
	public static Field getFieldAll(final Class<?> type, final String fieldName) {
		try {
			return type.getDeclaredField(fieldName);
		} catch(final Exception e) {
			// go to superclass
		}
		final Class<?> superType = type.getSuperclass();
		if (superType == null) {
			return null;
		}
		return getFieldAll(superType, fieldName);
	}
	
	public static <S, T extends S> void copyFields(final S source, final T target) throws IllegalArgumentException, IllegalAccessException {
		Field[] srcFlds = getAllFields(source.getClass());
		for (Field f : srcFlds) {
			if (isStatic(f)) {
				continue;
			}
			f.setAccessible(true);
			f.set(target, f.get(source));
		}
	}

	private static boolean isStatic(Field f) {
		return (f.getModifiers() & Modifier.STATIC) != 0;
	}
	
}
