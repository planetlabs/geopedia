package com.sinergise.common.util.collections.sort;

import java.util.Arrays;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.Timing;

/**
 * <p>
 * Implements sorting loops without prescribing the format of the data, the only thing needed is an abstract comparer/swapper which works by
 * indexes only. So, you can sort anything with this.
 * </p>
 * <p>
 * For example, if you have a set of coordinates stored in two arrays (one for Xs and one for Ys), you can still sort them without having to
 * create an array of Coordinate objects.
 * </p>
 */
public class SortLoop {
	
	private static final int QUICKSORT_THRESHOLD = 10;
	private static final int PARTITION_BY_SORT_THRESHOLD = 15;

	/**
	 * Returns the index of the median of the three indexed integers.
	 */
	private static int med3(final VirtualSorter data, final int a, final int b, final int c) {
		return (data.compare(a, b) < 0
		                              ? (data.compare(b, c) < 0 ? b : data.compare(a, c) < 0 ? c : a)
		                              : (data.compare(b, c) > 0 ? b : data.compare(a, c) > 0 ? c : a));
	}

	/**
	 * Partitions the data so that items smaller than the pivot are at the beginning, and 
	 * items larger than the pivot are at the end. Items equal to the pivot are all gathered
	 * in the middle, starting at the returned index.
	 * 
	 * @param s
	 * @param left
	 * @param right
	 * @param pivotIdx
	 * @return Index of the first occurence of the pivot element in the partitioned array
	 */
	public static int partitionByPivot(VirtualSorter s, int left, int right, int pivotIdx) {
		assert left <= pivotIdx && pivotIdx <= right;
		
		s.swap(pivotIdx, right); //move pivot out of the way
		pivotIdx = right;
		for (int i = left; i < pivotIdx; i++) {
			switch (s.compare(i, pivotIdx)) {
				case -1:
					s.swap(i, left++);
					break;
				case 0:
					//same as pivot, move it to the back
					s.swap(i--, --pivotIdx); 
			}
		}
		//move all those equal to the pivot to the middle
		for (int tgt = left; pivotIdx <= right; pivotIdx++, tgt++) {
			s.swap(pivotIdx, tgt);
		}
		return left;
	}

	public static int partitionByKthSmallestUsingSort(VirtualSorter s, int left, int len, int k) {
		quickSort(s, left, len);
		while (k > left && s.compare(k, k-1) == 0) {
			k--;
		}
		return k;
	}

	private static int choosePivot(final VirtualSorter data, int l, int r, int len) {
		int m = l + (len >> 1); // Small arrays, middle element
		if (len > 16) {
			if (len > 64) { // Big arrays, pseudomedian of 9
				final int s = len >>> 3;
				l = med3(data, l, l + s, l + s + s);
				m = med3(data, m - s, m, m + s);
				r = med3(data, r - s - s, r - s, r);
			}
			m = med3(data, l, m, r); // Mid-size, med of 3
		}
		return m;
	}

	/**
	 * Sorts the specified sub-array of integers into ascending order. The algorithm was stolen from the Java runtime source
	 */
	public static void quickSort(final VirtualSorter data, final int off, final int len) {
		// Insertion sort on smallest arrays
		if (len < QUICKSORT_THRESHOLD) {
			insertionSort(data, off, len);
			return;
		}
		
		// Choose a partition element, v
		int pivotIdx = choosePivot(data, off, len);
		
		// Establish Invariant: v* (<v)* (>v)* v*
		int a = off, b = a, c = off + len - 1, d = c;
		while (true) {
			while (b <= c) {
				final int cbm = data.compare(b, pivotIdx);
				if (cbm > 0) {
					break;
				}
				if (cbm == 0) {
					data.swap(a, b);
					if (pivotIdx == a) {
						pivotIdx = b;
					} else if (pivotIdx == b) {
						pivotIdx = a;
					}
					a++;
				}
				b++;
			}
			while (c >= b) {
				final int ccm = data.compare(c, pivotIdx);
				if (ccm < 0) {
					break;
				}
				if (ccm == 0) {
					data.swap(c, d);
					if (pivotIdx == c) {
						pivotIdx = d;
					} /*
					   * else if (m==d) { // this seems to never happen m=c; }
					   */
					d--;
				}
				c--;
			}
			if (b > c) {
				break;
			}
			data.swap(b++, c--);
		}
		
		// Swap partition elements back to middle
		int s;
		final int n = off + len;
		s = Math.min(a - off, b - a);
		vecswap(data, off, b - s, s);
		s = Math.min(d - c, n - d - 1);
		vecswap(data, b, n - s, s);
		
		// Recursively sort non-partition-elements
		if ((s = b - a) > 1) {
			quickSort(data, off, s);
		}
		if ((s = d - c) > 1) {
			quickSort(data, n - s, s);
		}
	}

	public static int choosePivot(final VirtualSorter data, final int off, final int len) {
		int m = off + (len >> 1); // Small arrays, middle element
		if (len > 7) {
			int l = off;
			int n = off + len - 1;
			if (len > 40) { // Big arrays, pseudomedian of 9
				final int s = len >>> 3;
				l = med3(data, l, l + s, l + s + s);
				m = med3(data, m - s, m, m + s);
				n = med3(data, n - s - s, n - s, n);
			}
			m = med3(data, l, m, n); // Mid-size, med of 3
		}
		return m;
	}

	public static void insertionSort(final VirtualSorter data, final int off, final int len) {
		for (int i = off; i < len + off; i++) {
			for (int j = i; j > off && data.compare(j - 1, j) > 0; j--) {
				data.swap(j - 1, j);
			}
		}
		return;
	}
	
	/**
	 * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
	 */
	private static void vecswap(final VirtualSorter data, int a, int b, final int n) {
		for (int i = 0; i < n; i++, a++, b++) {
			data.swap(a, b);
		}
	}
	
	static void heapSort(final int numbers[], final int array_size) {
		int i, temp;
		
		for (i = (array_size / 2) - 1; i >= 0; i--) {
			siftDown(numbers, i, array_size);
		}
		
		for (i = array_size - 1; i >= 1; i--) {
			temp = numbers[0];
			numbers[0] = numbers[i];
			numbers[i] = temp;
			siftDown(numbers, 0, i);
		}
	}
	
	static void siftDown(final int numbers[], int root, final int bottom) {
		int maxChild, temp;
		
		boolean done = false;
		while ((root * 2 < bottom) && (!done)) {
			if (root * 2 + 1 == bottom) {
				break;
			}
			if (root * 2 == bottom - 1) {
				maxChild = root * 2;
			} else if (numbers[root * 2] > numbers[root * 2 + 1]) {
				maxChild = root * 2;
			} else {
				maxChild = root * 2 + 1;
			}
			
			if (numbers[root] < numbers[maxChild]) {
				temp = numbers[root];
				numbers[root] = numbers[maxChild];
				numbers[maxChild] = temp;
				root = maxChild;
			} else {
				done = true;
			}
		}
	}
	
	public static void main(final String[] args) {
		final int[] test = new int[255];
		for (int a = 0; a < test.length; a++) {
			test[a] = (int)(100000 * Math.random());
		}
		
		final int[] test2 = new int[test.length];
		ArrayUtil.arraycopy(test, 0, test2, 0, test.length);
		
		final Timing timer = new Timing();
		timer.start();
		heapSort(test, test.length);
		timer.step("heapSort()");
		heapSort(test, test.length);
		timer.step("heapSort()");
		Arrays.sort(test2);
		timer.step("Arrays.sort()");
		Arrays.sort(test2);
		timer.step("Arrays.sort()");
		
		for (int a = 0; a < test.length; a++) {
			if (test[a] != test2[a]) {
				System.err.println("" + a + ": " + test[a] + " vs " + test2[a]);
			}
		}
	}

	public static int partitionByKthSmallest(VirtualSorter s, int left, int right, int k) {
		while (true) {
			assert left <= k && k <= right;
			
			int len = right - left + 1;
			if (len == 1) {
				return left;
			} else if (len < PARTITION_BY_SORT_THRESHOLD) {
				return SortLoop.partitionByKthSmallestUsingSort(s, left, len, k);
			}
			int pivotIdx = choosePivot(s, left, right, len);
			pivotIdx = partitionByPivot(s, left, right, pivotIdx);
			
			if (pivotIdx <= k && k <= firstGreaterThan(s, pivotIdx, pivotIdx+1, right)-1) {
				return pivotIdx;
			}
			if (k < pivotIdx) {
				right = pivotIdx - 1;
			} else {
				left = pivotIdx + 1;
			}
		}
	}
	

	private static int firstGreaterThan(VirtualSorter s, int compValIdx, int start, int end) {
		while (start <= end && s.compare(start, compValIdx) <= 0) {
			start++;
		}
		return start;
	}
}