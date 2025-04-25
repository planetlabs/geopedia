package com.sinergise.common.util.collections;

import java.util.Set;

public class SetWrapper<E> extends CollectionWrapper<E> implements Set<E> {
	public SetWrapper(Set<E> target) {
		super(target);
	}
}
