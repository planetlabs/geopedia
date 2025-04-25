package com.sinergise.common.util.collections;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class ListWrapper<E> extends CollectionWrapper<E> implements List<E> {
	private List<E> listTarget;
	
	public ListWrapper(List<E> target) {
		super(target);
		this.listTarget = target;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return listTarget.addAll(index, c);
	}
	@Override
	public E get(int index) {
		return listTarget.get(index);
	}
	@Override
	public E set(int index, E element) {
		return listTarget.set(index, element);
	}
	@Override
	public void add(int index, E element) {
		listTarget.add(index, element);
	}
	@Override
	public E remove(int index) {
		return listTarget.remove(index);
	}
	@Override
	public int indexOf(Object o) {
		return listTarget.indexOf(o);
	}
	@Override
	public int lastIndexOf(Object o) {
		return listTarget.lastIndexOf(o);
	}
	@Override
	public ListIterator<E> listIterator() {
		return listTarget.listIterator();
	}
	@Override
	public ListIterator<E> listIterator(int index) {
		return listTarget.listIterator(index);
	}
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return listTarget.subList(fromIndex, toIndex);
	}
}
