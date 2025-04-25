/**
 * 
 */
package com.sinergise.common.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author tcerovski
 */
public class CyclicList<E> extends ArrayList<E> {
	
	private static final long serialVersionUID = -386913781516339042L;
	
	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public CyclicList() {
		super();
	}
	
	/**
	 * Constructs an empty list with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity of the list.
	 * @exception IllegalArgumentException if the specified initial capacity is negative
	 */
	public CyclicList(final int initialCapacity) {
		super(initialCapacity);
	}
	
	/**
	 * Constructs a list containing the elements of the specified collection, in the order they are returned by the collection's iterator.
	 * The <tt>ArrayList</tt> instance has an initial capacity of 110% the size of the specified collection.
	 * 
	 * @param c the collection whose elements are to be placed into this list.
	 * @throws NullPointerException if the specified collection is null.
	 */
	public CyclicList(final Collection<? extends E> c) {
		super(c);
	}
	
	private int pos = 0;
	
	/**
	 * Appends the specified element to the current position of this list and increments the current position.
	 * 
	 * @param o element to be appended to this list.
	 * @return <tt>true</tt> (as per the general contract of Collection.add).
	 */
	@Override
	public boolean add(final E o) {
		pos = checkIndex(pos);
		super.add(pos++, o);
		return true;
	}
	
	@Override
	public void add(int index, E element) {
		super.add(index, element);
		if (index <= pos) {
			pos++;
		}
	}
	
	/**
	 * Appends all of the elements in the specified Collection to the current position of this list, in the order that they are returned by
	 * the specified Collection's Iterator.
	 * 
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 * @param c the elements to be inserted into this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * @throws NullPointerException if the specified collection is null.
	 */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		pos = checkIndex(pos);
		return super.addAll(pos++, c);
	}
	
	/**
	 * Appends all of the elements in the specified Collection to the end of this list, in the order that they are returned by the specified
	 * Collection's Iterator.
	 * 
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 * @param c the elements to be inserted into this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * @throws NullPointerException if the specified collection is null.
	 */
	public boolean addAllToTail(final Collection<? extends E> c) {
		return super.addAll(c);
	}
	
	/**
	 * Appends the specified element to the end of this list.
	 * 
	 * @see java.util.ArrayList#add(E)
	 * @param o element to be appended to this list.
	 * @return <tt>true</tt> (as per the general contract of Collection.add).
	 */
	public boolean addToTail(final E o) {
		return super.add(o);
	}
	
	/**
	 * Returns the first element in this list.
	 * 
	 * @return the first element in this list
	 * @throws NoSuchElementException if this list is empty
	 */
	public E getFirst() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		
		return get(0);
	}
	
	/**
	 * Returns the last element in this list.
	 * 
	 * @return the last element in this list
	 * @throws NoSuchElementException if this list is empty
	 */
	public E getLast() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		
		return get(size() - 1);
	}
	
	/**
	 * Returns the next element in the list and sets the position to that element.
	 * 
	 * @return the next element in the list.
	 */
	public E next() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		
		return get(pos++);
	}
	
	/**
	 * Returns the next element in the list.
	 * 
	 * @return the next element in the list.
	 */
	public E peekNext() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		
		return get(pos);
	}
	
	/**
	 * Returns the element after next element in the list and sets the position at that element.
	 * 
	 * @return the element after next element in the list.
	 */
	public E afterNext() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		
		pos++;
		return next();
	}
	
	/**
	 * Returns the element after next element in the list.
	 * 
	 * @return the element after next element in the list.
	 */
	public E peekAfterNext() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		return get(pos + 1);
	}
	
	/**
	 * Returns the previous element in the list and sets the position to that element.
	 * 
	 * @return the previous element in the list.
	 */
	public E previous() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		
		return get(--pos);
	}
	
	/**
	 * Returns the previous element in the list.
	 * 
	 * @return the previous element in the list.
	 */
	public E peekPrevious() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		return get(pos - 1);
	}
	
	private int checkIndex(int index) {
		final int s = size();
		if (s == 0) return 0;
		if (index < 0) index = s + index;
		if (index >= s) index = index % s;
		return index;
	}
	
	/**
	 * Sets the <tt>CyclicList</tt> position at the specified element. Call to <tt>next</tt> will return the specified element and call to
	 * <tt>previous</tt> will return an element before.
	 * 
	 * @param elem the first element to be returned by a call to <tt>next</tt>.
	 */
	public void positionAt(final Object elem) {
		final int index = indexOf(elem);
		if (index < 0) {
			throw new NoSuchElementException();
		}
		pos = index;
	}
	
	/**
	 * Sets the <tt>CyclicList</tt> position after the specified element. Call to <tt>next</tt> will return an element after the specified
	 * element and call to <tt>previous</tt> will return the specified element.
	 * 
	 * @param elem the first element to be returned by a call to <tt>next</tt>.
	 */
	public void positionAfter(final Object elem) {
		positionAt(elem);
		next();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.ArrayList#get(int)
	 */
	@Override
	public E get(final int index) {
		return super.get(checkIndex(index));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.ArrayList#remove(int)
	 */
	@Override
	public E remove(final int index) {
		return super.remove(checkIndex(index));
	}
	
	@Override
	public boolean remove(Object o) {
		return super.remove(o);
	}
	
	public static void main(final String[] args) {
		
		final String s1 = "1";
		final String s2 = "2";
		final String s3 = "3";
		final String s4 = "4";
		final String s5 = "5";
		
		final CyclicList<String> list = new CyclicList<String>();
		list.add(s1);
		list.add(s2);
		list.add(s3);
		list.add(s4);
		list.add(s5);
		
		System.out.println(list.next());
		System.out.println("first: " + list.getFirst());
		System.out.println("last: " + list.getLast());
		
		System.out.println();
		list.positionAt(s2);
		System.out.println(list.previous());
		list.positionAt(s2);
		System.out.println(list.next());
		list.positionAt(s2);
		System.out.println(list.afterNext());
		
		System.out.println();
		list.positionAt(s5);
		System.out.println(list.previous());
		list.positionAt(s5);
		System.out.println(list.next());
		list.positionAt(s5);
		System.out.println(list.afterNext());
		
		System.out.println();
		list.positionAt(s1);
		System.out.println(list.previous());
		list.positionAt(s1);
		System.out.println(list.next());
		list.positionAt(s1);
		System.out.println(list.afterNext());
		
	}
	
}
