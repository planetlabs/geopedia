/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package com.sinergise.java.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.collections.ArrayIterator;
import com.sinergise.java.util.ArrayUtilJava;

/**
 * A simple resizable threadsafe array. Offers minimal capability and overhead for all operations except <code>toArray(Class)</code>. The
 * main cost of the array maintainance is in the addition and removal of the elements. The potential traversals are very fast, because they
 * can occur outside the synchronized blocks of code through normal arrays. The arrays returned by the <code>toArray</code> methods are of
 * the correct runtime type. If, for example, the type of the list was specified to be <code>X</code>, the following cast is permitted:
 * <p>
 * <code>X[] array = list.toArray()</code>
 * </p>
 * Only one type cast is thus needed to obtain the final array for traversals.
 * 
 * @author <a href="mailto:gasper.tkacik@cosylab.com">Gasper Tkacik</a>
 * @author <a href="mailto:gasper.pajor@cosylab.com">Gasper Pajor</a>
 * @version $id$
 */
public class ObjectList<E> implements List<E> {
	private E[]           array = null;
	
	/**
	 * Creates a new ObjectList object.
	 * 
	 * @param type the type of objects this list will hold
	 * @throws NullPointerException if the type is <code>null</code>
	 */
	public ObjectList(final Class<? extends E> type) {
		this(ArrayUtilJava.newInstance(type, 0));
	}
	
	public ObjectList(E[] content) {
		this.array = content;
	}
	
	/**
	 * Adds an object to this list. This list can contain multiple entries of the same object.
	 * 
	 * @param o object to add to the collection
	 * @return true
	 * @see java.util.List#add(Object)
	 */
	@Override
	public boolean add(final E o) {
		add(array.length, o);
		return true;
	}
	
	/**
	 * Removes an element from the list. If there is more than one reference to the same object in the array, this method will remove only
	 * one reference.
	 * 
	 * @param o element to be removed from this list, if present.
	 * @return <tt>true</tt> if this list contained the specified element.
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object o) {
		if (o == null) {
			return false;
		}
		synchronized(this) {
			int index = -1;
			
			for (int i = 0; i < array.length; i++) {
				if (array[i] == o) {
					index = i;
					
					break;
				}
			}
			
			if (index == -1) {
				return false;
			}
			
			final int length = array.length;
			final E[] els = ArrayUtilJava.newInstance(array, length - 1);
			System.arraycopy(array, 0, els, 0, index);
			System.arraycopy(array, index + 1, els, index, length - index - 1);
			array = els;
		}
		
		return true;
	}
	
	/**
	 * Returns the size of the list.
	 * 
	 * @return int list size
	 */
	@Override
	public int size() {
		return array.length;
	}
	
	/**
	 * Returns all objects in the list which are subtypes of the <code>class</code> parameter. This necessitates the construction of a new
	 * array which must occur in a synchronized block of code. This method therefore takes some time to execute.
	 * 
	 * @param type the type of the objects to look for
	 * @return Object[] an array containing only those elements of the list, which are a subtype of <code>type</code>
	 * @throws NullPointerException when type is <code>null</code>
	 * @exception IllegalArgumentException if <code>type</code> is not a subtype of the type specified as the constructor parameter
	 */
	@SuppressWarnings("unchecked")
	public <T extends E> T[] toArray(final Class<T> arrType) {
		if (arrType == null) {
			throw new NullPointerException("type");
		}
		
		synchronized(this) {
			int count = 0;
			for (final Object element : array) {
				if (arrType.isInstance(element)) {
					count++;
				}
			}
			
			final T[] retVal = ArrayUtilJava.newInstance(arrType, count);
			int idx = 0;
			for (final Object element : array) {
				if (arrType.isInstance(element)) {
					retVal[idx++] = (T)element;
				}
			}
			
			return retVal;
		}
	}
	
	/**
	 * Returns the elements of this list. <b>Do not modify this array in any way.</b>. List membership must only be affected through
	 * <code>add</code> and <code>remove()</code> methods of this class.
	 * 
	 * @return an array of stored objects, of the run time type specified by the constructor argument
	 */
	@Override
	public E[] toArray() {
		return array;
	}
	
	/**
	 * Checks if the list contains a given element. Comparison with <code>equals</code>. Uses linear search.
	 * 
	 * @param o the object to look for
	 * @return <code>true</code> iff the list contains object <code>o</code>.
	 */
	@Override
	public boolean contains(final Object o) {
		return ArrayUtil.contains(array, o);
	}
	
	/**
	 * Returns the object at the specified index
	 * 
	 * @param index the index of the requested object
	 * @return the object at the specified index.
	 */
	@Override
	public E get(final int index) {
		return array[index];
	}
	
	/**
	 * Searches for the first occurence of the given argument, testing for equality using the <tt>equals</tt> method.
	 * 
	 * @param o an object.
	 * @return the index of the first occurrence of the argument in this list; returns <tt>-1</tt> if the object is not found.
	 * @see Object#equals(Object)
	 */
	@Override
	public int indexOf(final Object o) {
		return ArrayUtil.firstIndexOf(array, o);
	}
	
	/**
	 * Produces a string rendering of this instance by enumerating all members. Contract of <code>Object.toString</code>.
	 * 
	 * @return String all members
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(500);
		sb.append("ObjectList (size ");
		sb.append(toArray().length);
		sb.append(") = { ");
		
		final Object[] arr = toArray();
		
		for (final Object element : arr) {
			if (element == null) {
				continue;
			}
			
			sb.append(element.toString());
			sb.append(' ');
		}
		
		sb.append('}');
		
		return new String(sb);
	}
	
	/**
	 * Empties this list. References to contained elementsw are released.
	 */
	@Override
	public synchronized void clear() {
		array = ArrayUtilJava.newInstance(array, 0);
	}
	
	/**
	 * Returns iterator which iterates over elements, which was contained in list at the moment iterator was called. Iteration does not
	 * fail if elements are added or removed during the iteration.
	 * 
	 * @return non-failing iterator with copy of elements
	 */
	@Override
	public Iterator<E> iterator() {
		return new ArrayIterator<E>(array, 0);
	}
	
	/**
	 * Tests if this list has no elements.
	 * 
	 * @return <tt>true</tt> if this list has no elements; <tt>false</tt> otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return array.length == 0;
	}
	
	/**
	 * Removes the element at the specified position in this list (optional operation). Shifts any subsequent elements to the left
	 * (subtracts one from their indices). Returns the element that was removed from the list.
	 * 
	 * @param index the index of the element to removed.
	 * @return the element previously at the specified position.
	 * @throws IndexOutOfBoundsException DOCUMENT ME!
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E remove(final int index) {
		if (index < 0 || index > array.length) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		
		final Object retObj = array[index];
		
		synchronized(this) {
			final int length = array.length;
			final E[] els = ArrayUtilJava.newInstance(array, length - 1);
			System.arraycopy(array, 0, els, 0, index);
			System.arraycopy(array, index + 1, els, index, length - index - 1);
			array = els;
		}
		
		return (E)retObj;
	}
	
	/**
	 * Adds an object to this list. This list can contain multiple entries of the same object.
	 * 
	 * @param index where the object should be inserted
	 * @param o object to add to the collection
	 * @throws NullPointerException
	 * @exception ClassCastException if the RTT of o is not assignable to the type specified in the constructor
	 * @see java.util.List#add(int, Object)
	 */
	@Override
	public void add(final int index, final E o) {
		if (o == null) {
			throw new NullPointerException("o");
		}
		
		synchronized(this) {
			final int length = array.length;
			final E[] els = ArrayUtilJava.newInstance(array, length + 1);
			
			System.arraycopy(array, 0, els, 0, index);
			if (index < length) {
				System.arraycopy(array, index, els, index + 1, length - index);
			}
			els[index] = o;
			array = els;
		}
	}
	
	/**
	 * Returns the index of the last occurrence of the specified object in this list.
	 * 
	 * @param elem the desired element.
	 * @return the index of the last occurrence of the specified object in this list; returns -1 if the object is not found.
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(final Object elem) {
		if (elem == null) {
			for (int i = array.length - 1; i >= 0; i--) {
				if (array[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = array.length - 1; i >= 0; i--) {
				if (elem.equals(array[i])) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Inserts all of the elements in the specified Collection into this list, starting at the specified position. Shifts the element
	 * currently at that position (if any) and any subsequent elements to the right (increases their indices). The new elements will appear
	 * in the list in the order that they are returned by the specified Collection's iterator.
	 * 
	 * @param index index at which to insert first element from the specified collection.
	 * @param c elements to be inserted into this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		final Object[] a = c.toArray();
		final int numNew = a.length;
		
		synchronized(this) {
			final int length = array.length;
			final E[] nArray = ArrayUtilJava.newInstance(array, length + a.length);
			
			// if (index == length) {
			// System.arraycopy(array, 0, nArray, 0, length);
			// Array.set(nArray, length, o);
			// } else {
			System.arraycopy(array, 0, nArray, 0, index);
			System.arraycopy(a, 0, nArray, index, a.length);
			
			// Array.set(els, index, o);
			System.arraycopy(array, index, nArray, index + a.length, length - index);
			
			// }
			array = nArray;
		}
		
		return numNew != 0;
	}
	
	/**
	 * Appends all of the elements in the specified Collection to the end of this list, in the order that they are returned by the specified
	 * Collection's Iterator.
	 * 
	 * @param c the elements to be inserted into this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		return addAll(array.length, c);
	}
	
	/**
	 * Returns <tt>true</tt> if this collection contains all of the elements in the specified collection.
	 * <p>
	 * This implementation iterates over the specified collection, checking each element returned by the iterator in turn to see if it's
	 * contained in this collection. If all elements are so contained <tt>true</tt> is returned, otherwise <tt>false</tt>.
	 * </p>
	 * 
	 * @param c collection to be checked for containment in this collection.
	 * @return <tt>true</tt> if this collection contains all of the elements in the specified collection.
	 * @see #contains(Object)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		final Iterator<?> e = c.iterator();
		
		while (e.hasNext()) {
			if (!contains(e.next())) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Removes the elements that are contained in the collection.
	 * 
	 * @param c the collection of elements to be removed
	 * @return <code>true</code> if at least one element is removed
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean modified = false;
		
		for (int i = 0; i < array.length; i++) {
			if (c.contains(array[i])) {
				remove(i);
				i = i - 1;
				modified = true;
			}
		}
		
		return modified;
	}
	
	/**
	 * Removes all elements but those contained in the collection.
	 * 
	 * @param c the collection of elements to keep
	 * @return <code>true</code> if at lest one element is removed
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final java.util.Collection<?> c) {
		boolean modified = false;
		
		for (int i = 0; i < array.length; i++) {
			if (!c.contains(array[i])) {
				remove(i);
				i = i - 1;
				modified = true;
			}
		}
		
		return modified;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param fromIndex DOCUMENT ME!
	 * @param toIndex DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws UnsupportedOperationException when invoked
	 * @see java.util.List#subList(int, int)
	 * @deprecated This method is not implemented.
	 */
	@Override
	@Deprecated
	public List<E> subList(final int fromIndex, final int toIndex) {
		/*
		 * to implement this method, a new extended class should be made from object list, where all methods that change the elements would
		 * need to reflect the change onto the referenced (this) object list.
		 */
		/*
		 * if (fromIndex < 0 || toIndex > array.length || fromIndex>toIndex) throw new IllegalArgumentException(); ObjectList ol = new
		 * ObjectList(type); for (int i = fromIndex; i < toIndex; i++) { ol.add(i, array[i]); } return ol;
		 */
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns iterator which iterates over elements, which was contained in list at the moment iterator was called. Iteration does not
	 * fail, if elements are added or removed during the iteration.
	 * 
	 * @param index index of the first element to be returned from the list iterator (by a call to the <tt>next</tt> method).
	 * @return a list iterator of the elements in this list, starting at the specified position in the list.
	 * @throws IndexOutOfBoundsException if the specified index is out of range (<tt>index &lt; 0 || index &gt; size()</tt>).
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator(final int index) {
		if (index < 0 || index > array.length) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		
		return new ArrayIterator<E>(array, index);
	}
	
	/**
	 * Sets the element at specified index to specified object.
	 * 
	 * @param index the index of object to set
	 * @param element the object to be set to the index
	 * @return the element previously at the specified position
	 * @throws NullPointerException DOCUMENT ME!
	 * @see java.util.List#set(int, Object)
	 */
	@Override
	public E set(final int index, final E element) {
		if (element == null) {
			throw new NullPointerException("element");
		}
		
		E retObj = null;
		
		synchronized(this) {
			retObj = array[index];
			array[index] = element;
		}
		
		return retObj;
	}
	
	/**
	 * Returns an array containing all of the elements in this list.
	 * 
	 * @param a the array into which the elements of this list are to be stored, if it is big enough; otherwise, a new array is allocated
	 *            for this purpose.
	 * @return an array containing the elements of this list.
	 * @throws NullPointerException if the specified array is <tt>null</tt>.
	 * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every element in
	 *             this list.
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		if (a == null) {
			throw new NullPointerException("a");
		}
		if (a.length < array.length) {
			a = ArrayUtilJava.newInstance(a, array.length);
		}
		System.arraycopy(array, 0, a, 0, a.length);
		return a;
	}
	
	/**
	 * Returns a list iterator of the elements in this list, iterator will iterate over the elements, which were in list at the moment
	 * iterator was created thus iteration will not fail if list is modified during iteration.
	 * 
	 * @return a list iterator of the elements in this list
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator() {
		return new ArrayIterator<E>(array, 0);
	}
}

/* __oOo__ */
