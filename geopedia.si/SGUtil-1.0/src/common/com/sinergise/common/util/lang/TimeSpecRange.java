package com.sinergise.common.util.lang;

import java.io.Serializable;

public class TimeSpecRange implements Serializable {
	private static final long serialVersionUID = -7219153441705684771L;
	
	TimeSpec minimum;
	TimeSpec maximum;
	
	public TimeSpec getMinimum() {
		return minimum;
	}
	public void setMinimum(TimeSpec minimum) {
		this.minimum = minimum;
	}
	public TimeSpec getMaximum() {
		return maximum;
	}
	
	public void setMaximum(TimeSpec maximum) {
		this.maximum = maximum;
	}
	
	public boolean hasMaximum() {
		return maximum != null;
	}
	
	public boolean hasMinimum() {
		return minimum != null;
	}
	
	public TimeSpecRange() {}
	
	public TimeSpecRange(TimeSpec a, TimeSpec b) {
		if (a == null) {
			if (b == null) {
				return;
			}
			minimum =null;
			maximum = b;
			return;
		}
		if (b == null) {
			maximum = null;
			minimum = a;
			return;
		}
		
		if (a.compareTo(b) <= 0)  {
			minimum = a;
			maximum = b;
		} else {
			minimum = b;
			maximum = a;
		}
	}
	public static boolean isEmpty(TimeSpecRange ts) {
		return ts == null || (ts.getMinimum() == null && ts.getMaximum() == null);
	}
}
