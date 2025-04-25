package com.sinergise.common.util.collections;

import java.util.Collection;
import java.util.Iterator;

public class CollectionWrapper<E> implements Collection<E> { 
//TODO extends SGWrapperImpl<Collection<E>> 

	private Collection<E> wrappedObj;

	public CollectionWrapper(Collection<E> target) {
		this.wrappedObj = target;
	}
	
	@Override
	public boolean contains(Object o) {
		return wrappedObj.contains(o);
	}

	@Override
	public Object[] toArray() {
		return wrappedObj.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return wrappedObj.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return wrappedObj.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wrappedObj.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return wrappedObj.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return wrappedObj.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return wrappedObj.retainAll(c);
	}

	@Override
	public void clear() {
		wrappedObj.clear();
	}
	
	@Override
	public boolean isEmpty() {
		return wrappedObj.isEmpty();
	}
	
	@Override
	public boolean add(E e) {
		return wrappedObj.add(e);
	}
	
	@Override
	public Iterator<E> iterator() {
		return wrappedObj.iterator();
	}
	
	@Override
	public int size() {
		return wrappedObj.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		return wrappedObj.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return wrappedObj.hashCode();
	}
}
