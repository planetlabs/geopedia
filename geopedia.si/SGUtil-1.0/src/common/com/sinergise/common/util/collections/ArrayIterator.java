package com.sinergise.common.util.collections;

import java.util.ListIterator;

public class ArrayIterator<E> implements ListIterator<E> {
	private final E[] array;
	private final int len;

	private int index = 0;
	private int lastIdx = -1;

	public ArrayIterator(final E[] o) {
		this(o, 0);
	}
	
	/**
	 * Creates a new ArrayIterator object.
	 * 
	 * @param o the array for which the iterator is created
	 * @param index start index of the iteration
	 * @throws NullPointerException when object is null
	 */
	public ArrayIterator(final E[] o, final int index) {
		array = o;
		this.len = o != null ? array.length : 0;
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return index < len;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next() {
		lastIdx = index++;
		return array[lastIdx];
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws UnsupportedOperationException when invoked
	 * @see java.util.Iterator#remove()
	 * @deprecated uniplemented
	 */
	@Override
	@Deprecated
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.ListIterator#nextIndex()
	 */
	@Override
	public int nextIndex() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.ListIterator#previousIndex()
	 */
	@Override
	public int previousIndex() {
		return index - 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.ListIterator#hasPrevious()
	 */
	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.ListIterator#previous()
	 */
	@Override
	public E previous() {
		lastIdx = --index;
		return array[lastIdx]; // exception here will not change the index
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.ListIterator#add(java.lang.Object)
	 */
	@Override
	@Deprecated
	public void add(final E o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(final E o) {
		array[lastIdx] = o;
	}
}