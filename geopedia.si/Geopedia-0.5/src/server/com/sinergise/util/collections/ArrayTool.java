package com.sinergise.util.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

public class ArrayTool
{
	/**
	 * Adds all elements in data to coll
	 * 
	 * @param <T> type of elements
	 * @param coll receiving collection
	 * @param data data to add
	 */
	public static <T> void addTo(Collection<? super T> coll, T[] data)
	{
		if (data == null)
			return;
		
		if (coll == null)
			throw new IllegalArgumentException();
		
		for (T t : data)
			coll.add(t);
	}
	
	/**
	 * Adds elements from data to coll.
	 * 
	 * @param <T> type of elements
	 * @param coll receiving collection
	 * @param data data
	 * @param offset starting offset
	 * @param length number of elements to add
	 */
	public static <T> void addTo(Collection<? super T> coll, T[] data, int offset, int length)
	{
		if (data == null)
			return;
		
		if (coll == null)
			throw new IllegalArgumentException();
		
		if (offset < 0 || length < 0 || offset > data.length - length)
			throw new IllegalArgumentException();
		
		int last = offset + length;
		for (int i = offset; i < last; i++)
			coll.add(data[i]);
	}
	
	/**
	 * Filters data with predicate and returns an array
	 * without elements for which predicate returned false.
	 * <br><br>
	 * Note that if all elements satisfied the predicate, the
	 * same array will be returned, not a copy.
	 * 
	 * @param <T> type of elements
	 * @param data data to filter
	 * @param filter filtering predicate
	 * @return new array containing only elements that satisfy predicate
	 */
	@SuppressWarnings("unchecked")
    public static <T> T[] filter(T[] data, Predicate<T> filter)
	{
		if (data == null)
			return null;
		
		if (filter == null)
			return data;
		
		int l = data.length;
		for (int a=0; a<l; a++) {
			if (!filter.eval(data[a])) {
				ArrayList<T> tmp = new ArrayList<T>();
				addTo(tmp, data, 0, a);
				for (a++; a < l; a++) {
					if (filter.eval(data[a]))
						tmp.add(data[a]);
				}
				return toArray(tmp, (Class<T>)data.getClass().getComponentType());
			}
		}
		
		return data;
	}
	
	/**
	 * Filters data with predicate and removes all elements
	 * for which predicate returns false.
	 * 
	 * @param <T> type
	 * @param data data
	 * @param filter filter predicate
	 */
	public static <T> void filterInPlace(Collection<T> data, Predicate<T> filter)
	{
		if (data == null || filter == null)
			return;
		
		if (data instanceof List && data instanceof RandomAccess) {
			List<T> list = (List<T>) data;
			int size = list.size();
			int inpos = 0;
			while (inpos < size && filter.eval(list.get(inpos)))
				inpos++;
			
			if (inpos >= size)
				return;
			
			int outpos = inpos++;
			while (inpos < size) {
				T t = list.get(inpos++);
				if (filter.eval(t))
					list.set(outpos++, t);
			}
			
			while (outpos < size)
				list.remove(--size);
		} else {
			Iterator<T> i = data.iterator();
			while (i.hasNext())
				if (!filter.eval(i.next()))
					i.remove();
		}
	}
	
	/**
	 * Creates an array of the specified type and copies data from
	 * the collection to the array.
	 * 
	 * @param <T>
	 * @param coll
	 * @param klass
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<? extends T> coll, Class<T> klass)
	{
		T[] arr = (T[]) Array.newInstance(klass, coll.size());
		return coll.toArray(arr);
	}
	
	/**
	 * Merges arrays and returns a linearized array containing all
	 * the data combined. Null arrays are skipped but null elements
	 * are not. The class of the resulting array is the same as
	 * the first non-null array found.
	 * 
	 * @param <T>
	 * @param elements
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static <T> T[] splice(T[] ... elements)
	{
		int totalSize = 0;
		Class<T> elClass = null;
		
		for (T[] arr : elements) {
			if (arr != null) {
				if (totalSize > Integer.MAX_VALUE - arr.length)
					throw new IllegalArgumentException("Total size would be too large");
				
				totalSize += arr.length;
				
				if (elClass == null)
					elClass = (Class<T>) arr.getClass().getComponentType();
			}
		}
		
		if (elClass == null)
			return (T[]) empty;
		
		T[] out = (T[])Array.newInstance(elClass, totalSize);
		int pos = 0;
		for (T[] arr : elements) {
			int len = arr.length;
			if (len > 8) {
				System.arraycopy(arr, 0, out, pos, len);
				pos += len;
			} else {
				for (int a=0; a<len; a++)
					out[pos++] = arr[a];
			}
		}
		return out;
	}
	
	public static int[] unbox(Integer[] data)
	{
		return unbox(data, Integer.MIN_VALUE);
	}
	
	public static int[] unbox(Integer[] data, int nullValue)
	{
		if (data == null || data.length == 0)
			return emptyIntArray;
		
		int[] out = new int[data.length];
		int pos = 0;
		for (Integer i : data) {
			if (i == null) {
				out[pos++] = nullValue;
			} else {
				out[pos++] = i.intValue();
			}
		}
		return out;
	}
	
	public static int[] unbox(Collection<Integer> data)
	{
		return unbox(data, Integer.MIN_VALUE);
	}
	
	public static int[] unbox(Collection<Integer> data, int nullValue)
	{
		if (data == null || data.size() == 0)
			return emptyIntArray;
		
		int size = data.size();
		int[] out = new int[size];
		int pos = 0;
		
		if (data instanceof List && data instanceof RandomAccess) {
			List<Integer> list = (List<Integer>) data;
			for (; pos < size; pos++) {
				Integer i;
				if ((i = list.get(pos)) == null) {
					out[pos] = nullValue;
				} else {
					out[pos] = i.intValue();
				}
			}
		} else {
			for (Integer i : data) {
				if (i == null) {
					out[pos++] = nullValue;
				} else {
					out[pos++] = i.intValue();
				}
			}
		}
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
    public static <T> T[] pick(T[] data, int[] indexes)
	{
		T[] out = (T[]) Array.newInstance(data.getClass().getComponentType(), indexes.length);
		int pos = 0;
		for (int i : indexes)
			out[pos++] = data[i];
		return out;
	}
	
	public static final int[] emptyIntArray = new int[0];
	public static final Object[] empty = new Object[0]; 
}
