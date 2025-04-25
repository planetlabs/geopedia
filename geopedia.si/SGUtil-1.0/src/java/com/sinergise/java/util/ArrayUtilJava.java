package com.sinergise.java.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.lang.Function;

public class ArrayUtilJava extends ArrayUtil {

	/**
	 * Creates an array of the specified type and copies data from the collection to the array.
	 * 
	 * @param <T>
	 * @param coll
	 * @param klass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(final Collection<? extends T> coll, final Class<T> klass) {
		final T[] arr = (T[])Array.newInstance(klass, coll.size());
		return coll.toArray(arr);
	}

	public static int firstIndexOf(final Object array, final Object key) {
		for (int i = 0; i < Array.getLength(array); i++) {
			if (Array.get(array, i) != null && Array.get(array, i).equals(key)) { return i; }
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] newInstance(Class<E> componentType, int length) {
		return (E[])Array.newInstance(componentType, length);
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] newInstance(E[] prototype, int length) {
		return newInstance((Class<E>)prototype.getClass().getComponentType(), length);
	}
	
	public static void flip(final Object array) {
		if (array == null) { return; }
		Object v = null;
		final int size = Array.getLength(array);
		if (size == 1) { return; }
		for (int i = 0; i < size / 2; i++) {
			v = Array.get(array, i);
			Array.set(array, i, Array.get(array, size - i - 1));
			Array.set(array, size - i - 1, v);
		}
	}
	
	/**
	 * Iff count > 0, returns count elements from the start of array; iff count<0 return count elements from the end of array;
	 * @param source
	 * @param count
	 * @return
	 */
	public static <T> T[] take(T[] source, int count) {
		if (count == 0) {
			return newInstance(source, 0);
		} else if (count > 0) {
			return subArray(source, newInstance(source, count), 0);
		}
		return subArray(source, newInstance(source, -count), source.length + count - 1);
	}

	public static <T> T[] toArray(final Enumeration<? extends T> e, final Class<T> klass) {
		assert (e != null);
		final ArrayList<T> temp = new ArrayList<T>();
		while (e.hasMoreElements()) {
			temp.add(e.nextElement());
		}
		return toArray(temp, klass);
	}

	public static <T> T[] toArray(final Iterator<? extends T> i, final Class<T> klass) {
		assert (i != null);
		final ArrayList<T> temp = new ArrayList<T>();
		while (i.hasNext()) {
			temp.add(i.next());
		}
		return toArray(temp, klass);
	}

	/**
	 * Creates a {@link List} from provided varargs. In contrast to {@link Arrays}.asList in creates a modifiable list.
	 */
	public static <E> List<E> asList(final E... elems) {
		final List<E> list = new ArrayList<E>();
		for (final E e : elems) {
			list.add(e);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <T, S, E extends Throwable> T[] toArray(Object array, Function<S, T> func) {
		if (array == null) return null;
		int len = Array.getLength(array);
		T[] ret = (T[])new Object[len];
		for (int i = 0; i < len; i++) {
			ret[i] = func.execute((S)Array.get(array, i));
		}
		return ret;
	}
}
