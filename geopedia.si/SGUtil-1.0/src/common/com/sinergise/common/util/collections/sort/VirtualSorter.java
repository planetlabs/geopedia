package com.sinergise.common.util.collections.sort;

import java.util.Comparator;
import java.util.List;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.math.MathUtil;

/**
 * By adding a swap method to VirtualComparator, VirtualSorter allows in place sorting to be performed, again without having the data in
 * some prespecified form (like an array of objects).
 */
public interface VirtualSorter {
	public static final class DoubleArraySorter implements VirtualSorter {
		final double[] data;
		
		public DoubleArraySorter(final double[] data) {
			this.data = data;
		}
		
		@Override
		public int compare(final int i1, final int i2) {
			return MathUtil.fastCompare(data[i1], data[i2]);
		}
		
		@Override
		public void swap(final int i1, final int i2) {
			final double tmp = data[i1];
			data[i1] = data[i2];
			data[i2] = tmp;
		}
	}
	
	public static final class ObjectArraySorter<T> implements VirtualSorter {
		private final T[] data;
		private final Comparator<? super T> comp;
		
		public ObjectArraySorter(T[] data, Comparator<? super T> comp) {
			this.data = data;
			this.comp = comp;
		}
		
		@Override
		public int compare(final int i1, final int i2) {
			return comp.compare(data[i1], data[i2]);
		}
		
		@Override
		public void swap(final int i1, final int i2) {
			ArrayUtil.swap(data, i1, i2);
		}

		public static <E extends Comparable<? super E>> VirtualSorter forComparable(E[] arr, boolean nullFirst) {
			return new ObjectArraySorter<E>(arr, CollectionUtil.<E>comparableComparator(nullFirst));
		}
	}

	public static class ListSorter<T> implements VirtualSorter {
		private final List<T> data;
		private final Comparator<? super T> comp;
		
		public ListSorter(List<T> data, Comparator<? super T> comp) {
			this.data = data;
			this.comp = comp;
		}
		
		@Override
		public int compare(final int i1, final int i2) {
			return comp.compare(data.get(i1), data.get(i2));
		}
		
		@Override
		public void swap(final int i1, final int i2) {
			data.set(i1, data.set(i2, data.get(i1)));
		}
	}
	
	/**
	 * Swaps the elements at the specified indexes.
	 * 
	 * @param i1 index of the first element
	 * @param i2 index of the second element
	 */
	public void swap(int i1, int i2);
	
	/**
	 * Compares elements at indexes index1 and index2 and returns -1 if the first is smaller, 1 if the first is larger, or 0 if they are
	 * equal (much like Comparator).
	 * 
	 * @param i1 index of the first element
	 * @param i2 index of the second element
	 * @return -1, 0, or 1 as described above
	 */
	int compare(int i1, int i2);
}