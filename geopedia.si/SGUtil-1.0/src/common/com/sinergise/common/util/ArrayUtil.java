package com.sinergise.common.util;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.Predicate;
import com.sinergise.common.util.lang.SGCloneable;
import com.sinergise.common.util.string.StringUtil;

public class ArrayUtil {
	public static final Object[] empty = new Object[0];

	public static final byte[] emptyByteArray = new byte[0];
	
	public static final int[] emptyIntArray = new int[0];

	public static final long[] emptyLongs = new long[0];

	public static final double[] emptyDoubleArray = new double[0];

	/**
	 * @see CollectionUtil#addTo(Collection, Object)
	 * @deprecated use {@link CollectionUtil#addTo(Collection, Object)} instead
	 */
	@Deprecated
	public static <T> boolean addTo(final Collection<? super T> coll, final T element) {
		return CollectionUtil.addTo(coll, element);
	}
	
	/**
	 * @see CollectionUtil#addTo(Collection, Object[])
	 */
	public static <T> boolean addTo(final Collection<? super T> coll, final T[] data) {
		return CollectionUtil.addTo(coll, data);
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
	/**
	 * @see CollectionUtil#addTo(Collection, Object[], int, int)
	 */
	public static <T> boolean addTo(final Collection<? super T> coll, final T[] data, final int offset, final int length) {
		return CollectionUtil.addTo(coll, data, offset, length);
	}

	public static Object[] append(final Object[] base, final Object value) {
		final int baseLen = base.length;
		final Object[] out = new Object[baseLen + 1];
		arraycopy(base, 0, out, 0, baseLen);
		out[baseLen] = value;

		return out;
	}

	public static double[] arraycopy(double[] src) {
		return arraycopy(src, 0, new double[src.length], 0, src.length);
	}
	
	public static double[] arraycopy(double[] src, int srcOff, double[] target, int tOff, int length) {
		System.arraycopy(src, srcOff, target, tOff, length);
		return target;
	}
	
	public static float[] arraycopy(float src[], float target[]) {
		System.arraycopy(src, 0, target, 0, src.length);
		return target;
	}

	public static int[] arraycopy(final int[] src) {
		return arraycopy(src, 0, new int[src.length], 0, src.length);
	}

	public static int[] arraycopy(int[] src, int srcOff, int[] target, int tOff, int length) {
		System.arraycopy(src, srcOff, target, tOff, length);
		return target;
	}
	
	public static long[] arraycopy(final long[] src) {
		return arraycopy(src, 0, new long[src.length], 0, src.length);
	}

	public static long[] arraycopy(long[] src, int srcOff, long[] target, int tOff, int length) {
		System.arraycopy(src, srcOff, target, tOff, length);
		return target;
	}

	public static <T> T[] arraycopy(T[] src, int sOff, T[] target, int tOff, int len) {
		System.arraycopy(src, sOff, target, tOff, len);
		return target;
	}

	public static <T> T[] arraycopy(final T[] src, T[] target) {
		System.arraycopy(src, 0, target, 0, src.length);
		return target;
	}

	public static void checkOffsetLength(final int offset, final int length, final int arrLen) {
		if (length != 0 && (length < 0 || offset < 0 || offset >= arrLen || offset > arrLen - length)) {
			throw new IndexOutOfBoundsException("index: " + (offset + length) + " length: " + arrLen);
		}
	}


	@SuppressWarnings("unchecked")
	public static <T, S extends SGCloneable> T[] cloneDeep(S[] inArr, T[] outArr) {
		for (int i = 0; i < inArr.length; i++) {
			outArr[i] = (T)inArr[i].clone();
		}
		return outArr;
	}

	public static <T> T[] concat(final T[] a, final T[] b, final T[] ret) {
		arraycopy(a, 0, ret, 0, a.length);
		arraycopy(b, 0, ret, a.length, b.length);
		return ret;
	}

	public static boolean contains(final int[] array, final int val) {
		for (final int element : array) {
			if (element == val) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(final Object[] array, final Object key) {
		return firstIndexOf(array, key) != -1;
	}

	public static int count(long[] values, long query) {
		int cnt = 0;
		for (long l : values) {
			if (l == query) {
				cnt++;
			}
		}
		return cnt;
	}

	public static int countEquals(Object[] arr, Object value) {
		int cnt = 0;
		for (Object o : arr) {
			if (Util.safeEquals(o, value)) {
				cnt++;
			}
		}
		return cnt;
	}

	public static int countSame(Object[] arr, Object value) {
		int cnt = 0;
		for (Object o : arr) {
			if (o == value) {
				cnt++;
			}
		}
		return cnt;
	}

	/**
	 * @param inarr
	 * @param n
	 * @return array with first n elements dropped (or last -n for negative n)
	 */
	public static String[] drop(String[] inarr, int n) {
		if (n == 0) {
			return inarr;
		}
		String[] ret = new String[inarr.length - abs(n)];
		System.arraycopy(inarr, max(0, n), ret, 0, ret.length);
		return ret;
	}

	public static boolean equals(final Object[] arr1, final Object[] arr2) {
		if (arr1 == null) {
			return arr2 == null;
		}
		if (arr2 == null) {
			return false;
		}
		if (arr1.length != arr2.length) {
			return false;
		}
		for (int i = 0; i < arr1.length; i++) {
			if (!Util.safeEquals(arr1[i], arr2[i])) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("null")
	public static float[][] expand(final float[][] data, final int rowsBefore, final int rowsAfter,
		final int colsBefore, final int colsAfter, final float valToFill) {
		final int oldH = data == null ? 0 : data.length;
		final int oldW = (oldH == 0) ? 0 : data[0].length;
		final int newH = oldH + rowsBefore + rowsAfter;
		final int newW = oldW + colsBefore + colsAfter;

		if (colsBefore == 0 && colsAfter == 0) {
			if (rowsBefore == 0 && rowsAfter == 0)
				return data;
			float[][] ret = new float[newH][];
			for (int i = 0; i < rowsBefore; i++) {
				ret[i] = new float[newW];
				if (valToFill != 0)
					Arrays.fill(ret[i], valToFill);
			}
			for (int i = 0; i < oldH; i++) {
				ret[rowsBefore + i] = data[i];
			}
			for (int i = (newH - rowsAfter); i < newH; i++) {
				ret[i] = new float[newW];
				if (valToFill != 0)
					Arrays.fill(ret[i], valToFill);
			}
			return ret;
		}
		float[][] ret = new float[newH][newW];
		for (int y = 0; y < newH; y++) {
			if (y >= rowsBefore && y < rowsBefore + oldH) {
				// fill start of row
				if (colsBefore > 0 && valToFill != 0)
					Arrays.fill(ret[y], 0, colsBefore, valToFill);
				// copy middle of row
				System.arraycopy(data[y - rowsBefore], 0, ret[y], colsBefore, oldW);
				// fill end of row
				if (colsAfter > 0 && valToFill != 0)
					Arrays.fill(ret[y], colsBefore + oldW, newW, valToFill);
			} else if (valToFill != 0) {
				// fill top and bottom rows
				Arrays.fill(ret[y], valToFill);
			}
		}
		return ret;
	}

	@SuppressWarnings("null")
	public static short[][] expand(final short[][] data, final int rowsBefore, final int rowsAfter,
		final int colsBefore, final int colsAfter, final short valToFill) {
		final int oldH = data == null ? 0 : data.length;
		final int oldW = (oldH == 0) ? 0 : data[0].length;
		final int newH = oldH + rowsBefore + rowsAfter;
		final int newW = oldW + colsBefore + colsAfter;

		if (colsBefore == 0 && colsAfter == 0) {
			if (rowsBefore == 0 && rowsAfter == 0)
				return data;
			short[][] ret = new short[newH][];
			for (int i = 0; i < rowsBefore; i++) {
				ret[i] = new short[newW];
				if (valToFill != 0)
					Arrays.fill(ret[i], valToFill);
			}
			for (int i = 0; i < oldH; i++) {
				ret[rowsBefore + i] = data[i];
			}
			for (int i = (newH - rowsAfter); i < newH; i++) {
				ret[i] = new short[newW];
				if (valToFill != 0)
					Arrays.fill(ret[i], valToFill);
			}
			return ret;
		}
		short[][] ret = new short[newH][newW];
		for (int y = 0; y < newH; y++) {
			if (y >= rowsBefore && y < rowsBefore + oldH) {
				// fill start of row
				if (colsBefore > 0 && valToFill != 0)
					Arrays.fill(ret[y], 0, colsBefore, valToFill);
				// copy middle of row
				System.arraycopy(data[y - rowsBefore], 0, ret[y], colsBefore, oldW);
				// fill end of row
				if (colsAfter > 0 && valToFill != 0)
					Arrays.fill(ret[y], colsBefore + oldW, newW, valToFill);
			} else if (valToFill != 0) {
				// fill top and bottom rows
				Arrays.fill(ret[y], valToFill);
			}
		}
		return ret;
	}

	public static boolean[] fill(final boolean[] array, final boolean value) {
		Arrays.fill(array, value);
		return array;
	}

	public static short[] fill(final short[] array, final short value) {
		Arrays.fill(array, value);
		return array;
	}

	public static int[] fill(final int[] array, final int value) {
		Arrays.fill(array, value);
		return array;
	}

	public static double[] fill(final double[] array, final double value) {
		Arrays.fill(array, value);
		return array;
	}
	
	public static float[] fill(float[] array, float value) {
		Arrays.fill(array, value);
		return array;
	}

	public static long[] fill(long[] array, long value) {
		Arrays.fill(array, value);
		return array;
	}
	
	public static String[] fill(String[] array, String value) {
		Arrays.fill(array, value);
		return array;
	}
	
	public static <T> T[] fill(T[] array, T value) {
		Arrays.fill(array, value);
		return array;
	}
	
	public static Long[] fill(Long[] array, Long value) {
		Arrays.fill(array, value);
		return array;
	}

	public static short[][] fill2D(short[][] data, short val) {
		for (short[] s : data) {
			fill(s, val);
		}
		return data;
	}

	/**
	 * Filters data with predicate and returns an array without elements for which predicate returned false. <br>
	 * <br>
	 * Note that if all elements satisfied the predicate, the same array will be returned, not a copy.
	 * 
	 * @param <T> type of elements
	 * @param data data to filter
	 * @param filter filtering predicate
	 * @return new array containing only elements that satisfy predicate
	 */
	public static <T> List<T> filter(final T[] data, final Predicate<T> filter) {
		if (data == null) {
			return null;
		}

		if (filter == null) {
			return Arrays.asList(data);
		}

		final int l = data.length;
		for (int a = 0; a < l; a++) {
			if (!filter.eval(data[a])) {
				final ArrayList<T> tmp = new ArrayList<T>();
				addTo(tmp, data, 0, a);
				for (a++; a < l; a++) {
					if (filter.eval(data[a])) {
						tmp.add(data[a]);
					}
				}
				return tmp;
			}
		}

		return Arrays.asList(data);
	}

	/**
	 * Filters data with predicate and removes all elements for which predicate returns false.
	 * 
	 * @param <T> type
	 * @param data data
	 * @param filter filter predicate
	 */
	public static <T> void filterInPlace(final Collection<T> data, final Predicate<T> filter) {
		if (data == null || filter == null) {
			return;
		}

		if (data instanceof List<?> && data instanceof RandomAccess) {
			final List<T> list = (List<T>)data;
			int size = list.size();
			int inpos = 0;
			while (inpos < size && filter.eval(list.get(inpos))) {
				inpos++;
			}

			if (inpos >= size) {
				return;
			}

			int outpos = inpos++;
			while (inpos < size) {
				final T t = list.get(inpos++);
				if (filter.eval(t)) {
					list.set(outpos++, t);
				}
			}

			while (outpos < size) {
				list.remove(--size);
			}
		} else {
			final Iterator<T> i = data.iterator();
			while (i.hasNext()) {
				if (!filter.eval(i.next())) {
					i.remove();
				}
			}
		}
	}

	/**
	 * Works on non sorted arrays
	 */
	public static int firstGreaterThan(final double[] array, final double key) {
		return firstGreaterThan(array, key, 0, array.length - 1);
	}

	/**
	 * Works on non sorted arrays
	 */
	public static int firstGreaterThan(final double[] array, final double key, final int rangeStart, final int rangeEnd) {
		for (int i = rangeStart; i < rangeEnd + 1; i++) {
			if (array[i] > key) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Works fast on sorted arrays
	 */
	public static int firstGreaterThanInSorted(final double[] array, final double key) {
		return firstGreaterThanInSorted(array, key, 0, array.length - 1);
	}

	/**
	 * Works fast on sorted arrays
	 */
	public static int firstGreaterThanInSorted(final double[] array, final double key, final int startIndex,
		final int endIndex) {
		if (array.length < 1) {
			return -1;
		}
		if (array[startIndex] > key) {
			return startIndex;
		}
		if (endIndex - startIndex > 3) {
			final int mid = (startIndex + endIndex) / 2;
			if (array[mid] <= key) {
				return firstGreaterThanInSorted(array, key, mid + 1, endIndex);
			}
			return firstGreaterThanInSorted(array, key, startIndex, mid);
		}
		return firstGreaterThan(array, key, startIndex, endIndex);
	}

	public static int firstIndexOf(final Object[] array, final Object key) {
		final int len = array.length;
		for (int i = 0; i < len; i++) {
			if (Util.safeEquals(array[i], key)) {
				return i;
			}
		}
		return -1;
	}

	public static void flip(final Object[] array) {
		if (array == null) {
			return;
		}
		Object v = null;
		final int size = array.length;
		if (size == 1) {
			return;
		}
		for (int i = 0; i < size / 2; i++) {
			v = array[i];
			array[i] = array[size - 1 - i];
			array[size - 1 - i] = v;
		}
	}

	public static int hashCode(final Object[] array) {
		final int prime = 31;
		if (array == null) {
			return 0;
		}
		int result = 1;
		for (final Object element : array) {
			result = prime * result + (element == null ? 0 : element.hashCode());
		}
		return result;
	}

	public static int firstIndexOf(final double[] array, final double value) {
		int len = array.length;
		for (int i = 0; i < len; i++) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}
	
	public static int firstIndexOf(final double[] array, final double value, final double epsilon) {
		int len = array.length;
		for (int i = 0; i < len; i++) {
			if (Math.abs(array[i] - value) < epsilon) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the value of the closest element in array.
	 * 
	 * @param array
	 * @param value
	 * @param epsilon
	 * @return
	 */
	public static int indexOfClosest(final double[] array, final double value) {
		if (array == null || array.length == 0) {
			return -1;
		}

		int idx = -1;
		double diff = Double.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			double tmpDiff = Math.abs(array[i] - value);
			if (tmpDiff < diff) {
				idx = 1;
				diff = tmpDiff;
			}
		}
		return idx;
	}


	public static int[][] intArray2DFromUShort(short[][] sArr) {
		int[][] ret = new int[sArr.length][sArr[0].length];
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[0].length; j++) {
				ret[i][j] = sArr[i][j] & 0xFFFF;
			}
		}
		return ret;
	}

	public static boolean isNullOrEmpty(byte[] array) {
		return array == null || array.length == 0;
	}
	
	public static boolean isNullOrEmpty(double[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullOrEmpty(int[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullOrEmpty(long[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullOrEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	public static <T> Iterator<T> iterator(T[] results) {
		return Arrays.asList(results).iterator();
	}

	public static short[][] pad2D(short[][] inArr, short value, int p0_start, int p0_end, int p1_start, int p1_end) {
		short[][] outArr = new short[inArr.length + p0_start + p0_end][inArr[0].length + p1_start + p1_end];

		for (int i = 0; i < inArr.length; i++) {
			Arrays.fill(outArr[i], 0, p1_start, value);
			Arrays.fill(outArr[i], outArr[i].length - 1 - p1_end, outArr[i].length, value);
			System.arraycopy(inArr[i], 0, outArr[i + p0_start], p1_start, inArr[i].length);
		}

		for (int top = 0; top < p0_start; top++)
			Arrays.fill(outArr[top], value);
		for (int bot = p0_start + inArr.length; bot < outArr.length; bot++)
			Arrays.fill(outArr[bot], value);
		return outArr;
	}

	public static int[] parseInt(final String[] input) {
		final int[] ret = new int[input.length];
		for (int i = input.length - 1; i >= 0; i--) {
			ret[i] = Integer.parseInt(input[i].trim());
		}
		return ret;
	}

	public static <T> List<T> pick(final T[] data, final int[] indexes) {
		final List<T> out = new ArrayList<T>(indexes.length);
		int pos = 0;
		for (final int i : indexes) {
			out.set(pos++, data[i]);
		}
		return out;
	}

	/**
	 * Reads a string of double values, separated by <code>|</code>. Example: "1234.1234|134.42" returns
	 * {1234.1234,134.42}
	 * 
	 * @param sVal
	 * @return the parsed double[], or <code>null</code> if string is empty
	 */
	public static double[] readDoubleSeq(final String sVal) {
		if (sVal.length() < 1) {
			return null;
		}
		final String[] st = sVal.split("\\|");
		final double[] ret = new double[st.length];
		try {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = Double.parseDouble(st[i]);
			}
		} catch(final NumberFormatException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public static int[] readIntSeq(final String sVal) {
		if (sVal.length() < 1) {
			return null;
		}
		final String[] st = sVal.split("\\|");
		final int[] ret = new int[st.length];
		try {
			for (int i = 0; i < ret.length; i++) {
				ret[i] = Integer.parseInt(st[i]);
			}
		} catch (final NumberFormatException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public static double[] reverse(final double[] arr) {
		final int len = arr.length;
		for (int i = 0; i < arr.length / 2; i++) {
			final double left = arr[i];
			final int rightIdx = len - 1 - i;
			arr[i] = arr[rightIdx];
			arr[rightIdx] = left;
		}
		return arr;
	}

	public static <T> T[] reverse(final T[] arr) {
		final int len = arr.length;
		for (int i = 0; i < arr.length / 2; i++) {
			final T left = arr[i];
			final int rightIdx = len - 1 - i;
			arr[i] = arr[rightIdx];
			arr[rightIdx] = left;
		}
		return arr;
	}

	/**
	 * Merges arrays and returns list containing all the data combined.
	 * 
	 * @param <T>
	 * @param elements
	 * @return
	 */
	public static <T> List<T> splice(final T[]... elements) {
		int totalSize = 0;

		for (final T[] arr : elements) {
			if (arr != null) {
				if (totalSize > Integer.MAX_VALUE - arr.length) {
					throw new IllegalArgumentException("Total size would be too large");
				}
				totalSize += arr.length;
			}
		}

		if (totalSize == 0) {
			return new ArrayList<T>(0);
		}

		final List<T> out = new ArrayList<T>(totalSize);
		int pos = 0;
		for (final T[] arr : elements) {
			final int len = arr.length;
			if (len > 8) {
				System.arraycopy(arr, 0, out, pos, len);
				pos += len;
			} else {
				for (int a = 0; a < len; a++) {
					out.set(pos++, arr[a]);
				}
			}
		}
		return out;
	}

	public static <T, S extends T> T[] subArray(S[] source, T[] dest, int srcPos) {
		System.arraycopy(source, srcPos, dest, 0, dest.length);
		return dest;
	}
	
	/**
	 * @param data
	 * @param pivot
	 * @return index after the last element that is smaller than value  
	 */
	//TODO: Implement a more generic version of this (with VirtualSorter, maybe); this is just so that the algorithm can be tested
	public static final int partition(double[] data, int left, int right, double value) {
		assert left <= right;
		//find first swap
		while (data[left] < value) {
			if (left++ == right) {
				return left;
			}
		}
		while (data[right] >= value) {
			if (left == right--) {
				return left;
			}
		}
		//all the rest don't need to check for boundary 
		while (left < right) {
			swap(data, left, right);
			while(data[++left] < value) {}
			while(data[--right] >= value) {}
		}
		return left;
	}

	public static final void swap(double[] data, int i1, int i2) {
		final double tmp = data[i1];
		data[i1] = data[i2];
		data[i2] = tmp;
	}
	
	public static final <T> void swap(T[] data, int i1, int i2) {
		final T tmp = data[i1];
		data[i1] = data[i2];
		data[i2] = tmp;
	}
	
	public static final void swap(long[] data, int i1, int i2) {
		final long tmp = data[i1];
		data[i1] = data[i2];
		data[i2] = tmp;
	}

	public static final void swap(double[] a, int idxA, double[] b, int idxB) {
		double dum = a[idxA];
		a[idxA] = b[idxB];
		b[idxB] = dum;
	}

	public static final void swap(double[][] mtrxA, int a1, int a2, double[][] mtrxB, int b1, int b2) {
		swap(mtrxA[a1], a2, mtrxB[b1], b2);
	}

	public static <T> T[] toArray(final Collection<? extends T> src, final T[] tgt) {
		return src.toArray(tgt);
		// for (int i = 0; i < tgt.length; i++) {
		// tgt[i]=src.get(i);
		// }
		// return tgt;
	}

	public static Object[] toArray(final Iterator<?> i) {
		final ArrayList<Object> lst = new ArrayList<Object>();
		while (i.hasNext()) {
			lst.add(i.next());
		}
		return toArray(lst, new Object[lst.size()]);
	}

	/**
	 * Creates a new mutable {@link ArrayList} with elements from provided array. Updating the resulting list will not
	 * affect the provided array.
	 */
	public static <T> ArrayList<T> toArrayList(T... a) {
		if (a == null) {
			return new ArrayList<T>();
		}

		ArrayList<T> list = new ArrayList<T>(a.length);
		for (T el : a) {
			list.add(el);
		}
		return list;
	}

	public static double[] toDoubleArray(final Object array) {
		if (array instanceof double[]) {
			return (double[])array;
		} else if (array instanceof long[]) {
			final long[] lAr = (long[])array;
			final double[] ret = new double[lAr.length];
			for (int i = 0; i < lAr.length; i++) {
				ret[i] = lAr[i];
			}
			return ret;
		} else if (array instanceof float[]) {
			final float[] lAr = (float[])array;
			final double[] ret = new double[lAr.length];
			for (int i = 0; i < lAr.length; i++) {
				ret[i] = lAr[i];
			}
			return ret;
		} else if (array instanceof int[]) {
			final int[] lAr = (int[])array;
			final double[] ret = new double[lAr.length];
			for (int i = 0; i < lAr.length; i++) {
				ret[i] = lAr[i];
			}
			return ret;
		} else if (array instanceof short[]) {
			final short[] lAr = (short[])array;
			final double[] ret = new double[lAr.length];
			for (int i = 0; i < lAr.length; i++) {
				ret[i] = lAr[i];
			}
			return ret;
		} else if (array instanceof byte[]) {
			final byte[] lAr = (byte[])array;
			final double[] ret = new double[lAr.length];
			for (int i = 0; i < lAr.length; i++) {
				ret[i] = lAr[i];
			}
			return ret;
		} else if (array instanceof Number[]) {
			final Number[] lAr = (Number[])array;
			final double[] ret = new double[lAr.length];
			for (int i = 0; i < lAr.length; i++) {
				ret[i] = lAr[i].doubleValue();
			}
			return ret;
		}
		return null;
	}

	/**
	 * Creates a new mutable {@link HashSet} with elements from provided array. Updating the resulting set will not
	 * affect the provided array.
	 */
	public static <T> HashSet<T> toHashSet(T... e) {
		HashSet<T> set = new HashSet<T>(e.length);
		for (T o : e) {
			set.add(o);
		}
		return set;
	}

	public static int[] toIntArray(final Collection<? extends Number> intCol) {
		if (intCol == null)
			return null;

		final int[] ret = new int[intCol.size()];
		int off = 0;
		for (final Number i : intCol) {
			ret[off++] = i.intValue();
		}
		return ret;
	}

	public static <T extends Number> int[] toIntArray(final T[] input) {
		final int[] ret = new int[input.length];
		for (int i = input.length - 1; i >= 0; i--) {
			ret[i] = input[i].intValue();
		}
		return ret;
	}

	public static Integer[] toIntegerArray(final int[] input) {
		return box(input);
	}

	public static long[] toLongArray(final Collection<? extends Number> longCol) {
		if (longCol == null)
			return null;

		final long[] ret = new long[longCol.size()];
		int off = 0;
		for (final Number i : longCol) {
			ret[off++] = i.longValue();
		}
		return ret;
	}

	public static String[] toStringArray(final int[] is) {
		final String[] ret = new String[is.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = String.valueOf(is[i]);
		}
		return ret;
	}

	public static String[] toStringArray(final Object[] objArr) {
		if (objArr == null) {
			return null;
		}
		final String[] ret = new String[objArr.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = StringUtil.toString(objArr[i], null);
		}
		return ret;
	}

	public static Object[] trimAtStart(final Object[] array) {
		final int end = firstIndexOf(array, null);
		if (end == -1) {
			return array;
		}
		final Object[] ret = new Object[end];
		System.arraycopy(array, 0, ret, 0, end);
		return ret;
	}

	public static int[] unbox(final Collection<Integer> data) {
		return unbox(data, Integer.MIN_VALUE);
	}

	public static int[] unbox(final Collection<Integer> data, final int nullValue) {
		if (data == null || data.size() == 0) {
			return emptyIntArray;
		}

		final int size = data.size();
		final int[] out = new int[size];
		int pos = 0;

		if (data instanceof List<?> && data instanceof RandomAccess) {
			final List<Integer> list = (List<Integer>)data;
			for (; pos < size; pos++) {
				Integer i;
				if ((i = list.get(pos)) == null) {
					out[pos] = nullValue;
				} else {
					out[pos] = i.intValue();
				}
			}
		} else {
			for (final Integer i : data) {
				if (i == null) {
					out[pos++] = nullValue;
				} else {
					out[pos++] = i.intValue();
				}
			}
		}

		return out;
	}

	public static int[] unbox(final Integer[] data) {
		return unbox(data, Integer.MIN_VALUE);
	}

	public static int[] unbox(final Integer[] data, final int nullValue) {
		if (data == null || data.length == 0) {
			return emptyIntArray;
		}

		final int[] out = new int[data.length];
		int pos = 0;
		for (final Integer i : data) {
			if (i == null) {
				out[pos++] = nullValue;
			} else {
				out[pos++] = i.intValue();
			}
		}
		return out;
	}
	
	public static Integer[] box(final int[] input) {
		final Integer[] ret = new Integer[input.length];
		for (int i = input.length - 1; i >= 0; i--) {
			ret[i] = Integer.valueOf(input[i]);
		}
		return ret;
	}
	
	public static Long[] box(final long[] input) {
		final Long[] ret = new Long[input.length];
		for (int i = input.length - 1; i >= 0; i--) {
			ret[i] = Long.valueOf(input[i]);
		}
		return ret;
	}
	
	public static Double[] box(final double[] input) {
		final Double[] ret = new Double[input.length];
		for (int i = input.length - 1; i >= 0; i--) {
			ret[i] = Double.valueOf(input[i]);
		}
		return ret;
	}
	
	public static Float[] box(final float[] input) {
		final Float[] ret = new Float[input.length];
		for (int i = input.length - 1; i >= 0; i--) {
			ret[i] = Float.valueOf(input[i]);
		}
		return ret;
	}

	public static String writeDoubleSeq(final double[] value) {
		final StringBuffer sb = new StringBuffer(3 * value.length);
		for (int i = 0; i < value.length; i++) {
			if (i > 0) {
				sb.append("|");
			}
			sb.append(value[i]);
		}
		return sb.toString();
	}

	public static String writeIntSeq(final int[] value) {
		final StringBuffer sb = new StringBuffer(3 * value.length);
		for (int i = 0; i < value.length; i++) {
			if (i > 0) {
				sb.append("|");
			}
			sb.append(value[i]);
		}
		return sb.toString();
	}

	public static double[][] arraycopy(double[][] src) {
		double[][] ret = new double[src.length][src[0].length];
		arraycopy(src, 0, 0, ret, 0, 0, src.length, src[0].length);
		return ret;
	}

	public static void arraycopy(double[][] src, int sI, int sJ, double[][] tgt, int tI, int tJ, int nI, int nJ) {
		for (int i = 0; i < nI; i++) {
			arraycopy(src[sI + i], sJ, tgt[tI + i], tJ, nJ);
		}
	}

	public static double[] parseDouble(String[] input) {
		final double[] ret = new double[input.length];
		for (int i = input.length - 1; i >= 0; i--) {
			ret[i] = Double.parseDouble(input[i].trim());
		}
		return ret;
	}
	
	public static <T> T firstElement(T[] array) {
		return array[0];
	}
	
	public static <T> T lastElement(T[] array) {
		return array[array.length-1];
	}
}
