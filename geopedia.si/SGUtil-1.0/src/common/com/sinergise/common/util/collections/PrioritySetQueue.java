/**
 * 
 */
package com.sinergise.common.util.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author tcerovski
 */
public class PrioritySetQueue<E> extends PriorityQueue<E> {

	private static final long serialVersionUID = 1L;
	
	private final Set<E> elementsSet; 
	
	public PrioritySetQueue() {
		super();
		elementsSet = new HashSet<E>();
	}
	
	public PrioritySetQueue(int initialCapacity, Comparator<? super E> comparator) {
		super(initialCapacity, comparator);
		elementsSet = new HashSet<E>(initialCapacity);
	}
	
	public PrioritySetQueue(Collection<? extends E> c) { 
		super(c);
		elementsSet = new HashSet<E>(c.size());
	}
	
	public PrioritySetQueue(PriorityQueue<? extends E> c) {
		super(c);
		elementsSet = new HashSet<E>(c.size());
	}
	
	public PrioritySetQueue(SortedSet<? extends E> c) {
		super(c);
		elementsSet = new HashSet<E>(c.size());
	}
	
	@Override
	public boolean offer(E o) {
		if(!elementsSet.contains(o) && super.offer(o)) {
			elementsSet.add(o);
			return true;
		}
		return false;
	}
	
	@Override
	public E poll() {
		E ret = super.poll();
		if (ret != null) {
			elementsSet.remove(ret);
		}
		return ret;
	}
	
	@Override
	public boolean remove(Object o) {
		if (super.remove(o)) {
			elementsSet.remove(o);
			return true;
		}
		return false;
	}
	
	@Override
	public void clear() {
		super.clear();
		elementsSet.clear();
	}
	
	@Override
	public boolean contains(Object o) {
		return elementsSet.contains(o);
	}
	
}
