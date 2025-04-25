/*
 * Created on Mar 31, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sinergise.common.util.collections;

/**
 * @author Miha Kadunc (<a href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com</a>
 */
public interface IntIterator {
	public static class Default implements IntIterator {
		private final int[] ints;
		private int         idx = 0;
		
		public Default(final int[] array) {
			this.ints = array;
		}
		
		@Override
		public boolean hasNext() {
			return idx < ints.length;
		}
		
		@Override
		public int next() {
			return ints[idx++];
		}
	}
	
	boolean hasNext();
	
	int next();
}
