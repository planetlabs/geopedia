package com.sinergise.common.util.collections;

import java.util.Comparator;

import com.sinergise.common.util.collections.sort.VirtualComparator;

/**
 * <p>
 * This class provides binary search capabilities for arrays of ints, double, Comparables and Objects (with a Comparator). Unlike the
 * java.util.Arrays binary searches, these methods guarantee certain properties on the result:
 * </p>
 * <ul>
 * <li>searchFirst will return the minimum index of matching elements</li>
 * <li>searchLast will return the maximum index of matching elements</li>
 * <li>searchLastSmaller will return the maximum index of elements less than the target</li>
 * <li>searchFirstLarger will return the minimum index of elements greater than the target</li>
 * <li>range combines searchLastSmaller and searchFirstLarger</li>
 * </ul>
 * <p>
 * Additionally, when searching Objects without a comparator, the methods are declared as Comparable[], not Object[], so it is not possible
 * to pass unsupported Object types.
 * </p>
 * <p>
 * The Object functions also have another useful guaranteed property: when calling the comparator, the first argument to compare() will
 * always be an object from the array, while the second argument will be the target object. This allows you to have, for example, an array
 * of Point objects, while the target can be a Double, if that makes sense for your comparator.
 * </p>
 * <p>
 * Temporary note: the documentation on all versions is copied from the int version, so the example doesn't quite match the interface;
 * nonetheless, you should get an idea of how the functions work :) It is in the TODO, though...
 * </p>
 */

public class BinarySearch {
	/**
	 * <p>
	 * Finds the target in the given array and returns the last index that matches the target. For example:
	 * </p>
	 * <tt>
     * searchLast(new int[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt> will return 5. If the array is not sorted, the result is undefined.
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The last index of the target in array, or -1 if it is not found
	 */
	
	static public int searchLast(final int[] array, final int target) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] > target) {
				high = probe;
			} else {
				low = probe;
			}
		}
		if (low == -1 || array[low] != target) {
			return -1;
		}
		return low;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the first index that matches the target. For example:
	 * </p>
	 * <p>
	 * <tt>
     * searchFirst(new int[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt>
	 * </p>
	 * <p>
	 * will return 2.
	 * </p>
	 * <p>
	 * If the array is not sorted, the result is undefined.
	 * </p>
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The first index of the target in array, or -1 if it is not found
	 */
	
	static public int searchFirst(final int[] array, final int target) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] < target) {
				low = probe;
			} else {
				high = probe;
			}
		}
		if (high == array.length || array[high] != target) {
			return -1;
		}
		return high;
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public int[] range(final int[] array, final int floor, final int ceiling) {
		return range(array, floor, ceiling, new int[2]);
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound. If the answer parameter is provided it must be at
	 * least 2 elements long, otherwise an IllegalArgumentException is thrown. If answer is not provided, a new array is allocated. In both
	 * cases, a reference to the answer is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public int[] range(final int[] array, final int floor, final int ceiling, int[] answer) {
		if (answer == null) {
			answer = new int[2];
		}
		
		if (answer.length < 2) {
			throw new IllegalArgumentException("Answer must be at least of length 2");
		}
		
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] < floor) {
				low = probe;
			} else {
				high = probe;
			}
		}
		answer[0] = low;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] > ceiling) {
				high = probe;
			} else {
				low = probe;
			}
		}
		answer[1] = high;
		
		return answer;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the biggest element that is smaller than floor. If all elements of the
	 * array are bigger or equal to floor, -1 is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The floor value
	 * @return The index as described above
	 */
	
	public static int searchLastSmaller(final int[] array, final int floor) {
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] < floor) {
				low = probe;
			} else {
				high = probe;
			}
		}
		return low;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the smallest element that is larger than ceiling. If all elements of the
	 * array are smaller or equal to floor, array.length is returned.
	 * 
	 * @param array The sorted array to search
	 * @param ceiling The ceiling value
	 * @return The index as described above
	 */
	
	public static int searchFirstLarger(final int[] array, final int ceiling) {
		int high, low, probe;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] > ceiling) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return high;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the last index that matches the target. For example:
	 * </p>
	 * <tt>
     * searchLast(new double[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt> will return 5. If the array is not sorted, the result is undefined.
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The last index of the target in array, or -1 if it is not found
	 */
	
	static public int searchLast(final double[] array, final double target) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] > target) {
				high = probe;
			} else {
				low = probe;
			}
		}
		if (low == -1 || array[low] != target) {
			return -1;
		}
		return low;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the first index that matches the target. For example:
	 * </p>
	 * <p>
	 * <tt>
     * searchFirst(new int[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt>
	 * </p>
	 * <p>
	 * will return 2.
	 * </p>
	 * <p>
	 * If the array is not sorted, the result is undefined.
	 * </p>
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The first index of the target in array, or -1 if it is not found
	 */
	
	static public int searchFirst(final double[] array, final double target) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] < target) {
				low = probe;
			} else {
				high = probe;
			}
		}
		if (high == array.length || array[high] != target) {
			return -1;
		}
		return high;
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public int[] range(final double[] array, final double floor, final double ceiling) {
		return range(array, floor, ceiling, new int[2]);
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound. If the answer parameter is provided it must be at
	 * least 2 elements long, otherwise an IllegalArgumentException is thrown. If answer is not provided, a new array is allocated. In both
	 * cases, a reference to the answer is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public int[] range(final double[] array, final double floor, final double ceiling, int[] answer) {
		if (answer == null) {
			answer = new int[2];
		}
		
		if (answer.length < 2) {
			throw new IllegalArgumentException("Answer must be at least of length 2");
		}
		
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] < floor) {
				low = probe;
			} else {
				high = probe;
			}
		}
		answer[0] = low;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] > ceiling) {
				high = probe;
			} else {
				low = probe;
			}
		}
		answer[1] = high;
		
		return answer;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the biggest element that is smaller than floor. If all elements of the
	 * array are bigger or equal to floor, -1 is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The floor value
	 * @return The index as described above
	 */
	
	public static int searchLastSmaller(final double[] array, final double floor) {
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] < floor) {
				low = probe;
			} else {
				high = probe;
			}
		}
		return low;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the smallest element that is larger than ceiling. If all elements of the
	 * array are smaller or equal to floor, array.length is returned.
	 * 
	 * @param array The sorted array to search
	 * @param ceiling The ceiling value
	 * @return The index as described above
	 */
	
	public static int searchFirstLarger(final double[] array, final double ceiling) {
		int high, low, probe;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe] > ceiling) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return high;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the last index that matches the target. For example:
	 * </p>
	 * <tt>
     * searchLast(new double[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt> will return 5. If the array is not sorted, the result is undefined.
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The last index of the target in array, or -1 if it is not found
	 */
	
	static public <T> int searchLast(final Comparable<? super T>[] array, final T target) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe].compareTo(target) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		if (low == -1 || array[low] != target) {
			return -1;
		}
		return low;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the first index that matches the target. For example:
	 * </p>
	 * <p>
	 * <tt>
     * searchFirst(new int[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt>
	 * </p>
	 * <p>
	 * will return 2.
	 * </p>
	 * <p>
	 * If the array is not sorted, the result is undefined.
	 * </p>
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The first index of the target in array, or -1 if it is not found
	 */
	
	static public <T> int searchFirst(final Comparable<? super T>[] array, final T target) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe].compareTo(target) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		if (high == array.length || array[high] != target) {
			return -1;
		}
		return high;
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public <T, U extends T, V extends T> int[] range(final Comparable<T>[] array, final U floor, final V ceiling) {
		return range(array, floor, ceiling, new int[2]);
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound. If the answer parameter is provided it must be at
	 * least 2 elements long, otherwise an IllegalArgumentException is thrown. If answer is not provided, a new array is allocated. In both
	 * cases, a reference to the answer is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public <T, U extends T, V extends T> int[] range(final Comparable<T>[] array, final U floor, final V ceiling, int[] answer) {
		if (answer == null) {
			answer = new int[2];
		}
		
		if (answer.length < 2) {
			throw new IllegalArgumentException("Answer must be at least of length 2");
		}
		
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe].compareTo(floor) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		answer[0] = low;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe].compareTo(ceiling) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		answer[1] = high;
		
		return answer;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the biggest element that is smaller than floor. If all elements of the
	 * array are bigger or equal to floor, -1 is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The floor value
	 * @return The index as described above
	 */
	
	public static <T> int searchLastSmaller(final Comparable<? super T>[] array, final T floor) {
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe].compareTo(floor) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		return low;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the smallest element that is larger than ceiling. If all elements of the
	 * array are smaller or equal to floor, array.length is returned.
	 * 
	 * @param array The sorted array to search
	 * @param ceiling The ceiling value
	 * @return The index as described above
	 */
	
	public static <T> int searchFirstLarger(final Comparable<? super T>[] array, final T ceiling) {
		int high, low, probe;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (array[probe].compareTo(ceiling) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return high;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the last index that matches the target. For example:
	 * </p>
	 * <tt>
     * searchLast(new double[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt> will return 5. If the array is not sorted, the result is undefined.
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The last index of the target in array, or -1 if it is not found
	 */
	
	static public <T, U extends T> int searchLast(final T[] array, final U target, final Comparator<? super T> comparator) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comparator.compare(array[probe], target) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		if (low == -1 || array[low] != target) {
			return -1;
		}
		return low;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the first index that matches the target. For example:
	 * </p>
	 * <p>
	 * <tt>
     * searchFirst(new int[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt>
	 * </p>
	 * <p>
	 * will return 2.
	 * </p>
	 * <p>
	 * If the array is not sorted, the result is undefined.
	 * </p>
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The first index of the target in array, or -1 if it is not found
	 */
	
	static public <T, U extends T> int searchFirst(final U[] array, final T target, final Comparator<? super T> comparator) {
		int high = array.length, low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comparator.compare(array[probe], target) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		if (high == array.length || array[high] != target) {
			return -1;
		}
		return high;
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public <T, U extends T, V extends T> int[] range(final T[] array, final U floor, final V ceiling, final Comparator<? super T> comparator) {
		return range(array, floor, ceiling, new int[2], comparator);
	}
	
	/**
	 * Searches the sorted array provided and returns the indices of the largest element smaller than floor and the smallest element larger
	 * than ceiling. The result is returned in a 2-element array where the indices are in the order listed above. If no such elements exist,
	 * -1 is returned as the lower bound and array.length is returned as the upper bound. If the answer parameter is provided it must be at
	 * least 2 elements long, otherwise an IllegalArgumentException is thrown. If answer is not provided, a new array is allocated. In both
	 * cases, a reference to the answer is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The minimum value to exclude from the range
	 * @param ceiling The maximum value to exclude from the range
	 * @return A 2-element array containing the indices as described above
	 */
	
	static public <T, U extends T, V extends T> int[] range(final T[] array, final U floor, final V ceiling, int[] answer,
	                                                        final Comparator<? super T> comparator) {
		if (answer == null) {
			answer = new int[2];
		}
		
		if (answer.length < 2) {
			throw new IllegalArgumentException("Answer must be at least of length 2");
		}
		
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comparator.compare(array[probe], floor) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		answer[0] = low;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comparator.compare(array[probe], ceiling) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		answer[1] = high;
		
		return answer;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the biggest element that is smaller than floor. If all elements of the
	 * array are bigger or equal to floor, -1 is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The floor value
	 * @return The index as described above
	 */
	
	public static <T, U extends T> int searchLastSmaller(final T[] array, final U floor, final Comparator<? super T> comparator) {
		int high, low, probe;
		
		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comparator.compare(array[probe], floor) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		return low;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the smallest element that is larger than ceiling. If all elements of the
	 * array are smaller or equal to floor, array.length is returned.
	 * 
	 * @param array The sorted array to search
	 * @param ceiling The ceiling value
	 * @return The index as described above
	 */
	
	public static <T, U extends T> int searchFirstLarger(final T[] array, final U ceiling, final Comparator<? super T> comparator) {
		int high, low, probe;
		
		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comparator.compare(array[probe], ceiling) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return high;
	}
	
	/*
	 * public static void main(String[] args) { // 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 int[] test={1, 2, 3, 4, 4, 5, 6, 7, 7, 7, 9, 9, 10,
	 * 10, 10 }; System.out.println(searchFirst(test, 7)); // should be 7 System.out.println(searchLast(test, 7)); // should be 9
	 * System.out.println(searchFirst(test, 8)); // should be -1 System.out.println(searchLast(test, 8)); // should be -1
	 * System.out.println(searchFirst(test, 9)); // should be 10 System.out.println(searchLast(test, 9)); // should be 11
	 * System.out.println(searchLastSmaller(test, 8)); // should be 9 System.out.println(searchFirstLarger(test, 8)); // should be 10
	 * System.out.println(); System.out.println(searchFirst(test, 0)); // should be -1 System.out.println(searchLast(test, 0)); // should be
	 * -1 System.out.println(searchLastSmaller(test, 0)); // should be -1 System.out.println(searchFirstLarger(test, 0)); // should be 0
	 * System.out.println(); System.out.println(searchFirst(test, 15)); // should be -1 System.out.println(searchLast(test, 15)); // should
	 * be -1 System.out.println(searchLastSmaller(test, 15)); // should be 14 System.out.println(searchFirstLarger(test, 15)); // should be
	 * 15 }
	 */

	/**
	 * <p>
	 * Finds the target in the given virtual array and returns the last index that matches the target. For example:
	 * </p>
	 * <tt>
     * searchLast(new int[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt> will return 5. If the array is not sorted, the result is undefined.
	 * 
	 * @param comp the comparator to compare values in the virtual array with the target
	 * @param firstIndex the smallest index to search (inclusive)
	 * @param lastIndex the biggest index to search (exclusive)
	 * @return The last index of the target in array, or -1 if it is not found
	 */
	
	static public int searchLast(final VirtualComparator comp, final int firstIndex, final int lastIndex) {
		int high = lastIndex, low = firstIndex - 1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comp.compare(probe) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		if (low == -1 || comp.compare(low) != 0) {
			return -1;
		}
		return low;
	}
	
	/**
	 * <p>
	 * Finds the target in the given array and returns the first index that matches the target. For example:
	 * </p>
	 * <p>
	 * <tt>
     * searchFirst(new int[] { 0, 1, 2, 2, 2, 2, 3 }, 2);
     * </tt>
	 * </p>
	 * <p>
	 * will return 2.
	 * </p>
	 * <p>
	 * If the array is not sorted, the result is undefined.
	 * </p>
	 * 
	 * @param array The sorted array to search
	 * @param target The value to find
	 * @return The first index of the target in array, or -1 if it is not found
	 */
	
	static public int searchFirst(final VirtualComparator comp, final int firstIndex, final int lastIndex) {
		int high = lastIndex, low = firstIndex - 1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comp.compare(probe) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		if (high == lastIndex || comp.compare(high) != 0) {
			return -1;
		}
		return high;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the biggest element that is smaller than floor. If all elements of the
	 * array are bigger or equal to floor, -1 is returned.
	 * 
	 * @param array The sorted array to search
	 * @param floor The floor value
	 * @return The index as described above
	 */
	
	public static int searchLastSmaller(final VirtualComparator comp, final int firstIndex, final int lastIndex) {
		int high, low, probe;
		
		// work on floor
		high = lastIndex;
		low = firstIndex - 1;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comp.compare(probe) < 0) {
				low = probe;
			} else {
				high = probe;
			}
		}
		return low;
	}
	
	/**
	 * Searches the provided sorted array and returns the index of the smallest element that is larger than ceiling. If all elements of the
	 * array are smaller or equal to floor, array.length is returned.
	 * 
	 * @param array The sorted array to search
	 * @param ceiling The ceiling value
	 * @return The index as described above
	 */
	
	public static int searchFirstLarger(final VirtualComparator comp, final int firstIndex, final int lastIndex) {
		int high, low, probe;
		
		// work on ceiling
		high = lastIndex;
		low = firstIndex;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (comp.compare(probe) > 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return high;
	}
}