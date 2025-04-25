package com.sinergise.common.util.math;

@SuppressWarnings("unchecked")
public class IntervalBuilder<T extends Comparable<? super T>> {
	@SuppressWarnings("rawtypes")
	private Interval data = Interval.EMPTY;
	
	public IntervalBuilder() {
	}
	
	public IntervalBuilder(Interval<T> interval) {
		this.data = interval;
	}
	public void expandToInclude(T val) {
		data = data.expandToInclude(val);
	} 
	
	public void intersectWith(Interval<T> val) {
		data = data.intersection(val);
	}
	
	public Interval<T> getInterval() {
		return data;
	}
	
	@Override
	public String toString() {
		return data.toString();
	}
}
