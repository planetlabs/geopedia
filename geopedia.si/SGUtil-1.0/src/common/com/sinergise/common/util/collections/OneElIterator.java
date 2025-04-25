package com.sinergise.common.util.collections;

import java.util.Iterator;

public class OneElIterator<T> implements Iterator<T> {
	
	public static final <T> OneElIterator<T> createFor(T item) {
		return new OneElIterator<T>(item);
	}
	
	private T item;
	
	public OneElIterator(T item) {
		this.item = item;
	}
	@Override
	public boolean hasNext() {
		return item != null;
	}
	@Override
	public T next() {
		try {
			return item;
		} finally {
			item = null;
		}
	}
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
