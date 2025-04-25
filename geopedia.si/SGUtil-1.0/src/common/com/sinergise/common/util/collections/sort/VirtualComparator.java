package com.sinergise.common.util.collections.sort;

/**
 * This interface is used for binary searching without having to write the binary search loop if the data is not in an array-of-objects
 * form. Each instance should know what is being searched (the target), and the result of the compare() method should compare the element
 * with the target.
 */

public interface VirtualComparator {
	public int compare(int index);
}
