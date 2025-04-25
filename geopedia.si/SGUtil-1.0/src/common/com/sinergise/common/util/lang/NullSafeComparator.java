package com.sinergise.common.util.lang;

import java.util.Comparator;

import com.sinergise.common.util.Util;

public final class NullSafeComparator<T> implements Comparator<T> {
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static final Comparator COMPARABLE_NULL_LAST = new Comparator<Comparable>() {
		@Override
		public int compare(Comparable o1, Comparable o2) {
			return Util.safeCompare(o1, o2, true);
		}
	}; 
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static final Comparator COMPARABLE_NULL_FIRST = new Comparator<Comparable>() {
		@Override
		public int compare(Comparable o1, Comparable o2) {
			return Util.safeCompare(o1, o2, false);
		}
	}; 

	@SuppressWarnings("unchecked")
	public static final <T extends Comparable<? super T>> Comparator<T> get(boolean nullFirst) {
		return nullFirst ? COMPARABLE_NULL_FIRST : COMPARABLE_NULL_LAST;
	}
	
	public static final <T extends Comparable<? super T>> Comparator<T> get(Comparator<T> comp, boolean nullFirst) {
		return new NullSafeComparator<T>(comp, !nullFirst);
	}
	
	private final Comparator<T> comp;
	private final boolean nullGreater;
	private NullSafeComparator(Comparator<T> comp, boolean nullGreater) {
		this.comp = comp;
		this.nullGreater = nullGreater;
	}
	
	@Override
	public int compare(T o1, T o2) {
		return Util.safeCompare(o1, o2, comp, nullGreater);
	}
}
