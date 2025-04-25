/*
 *
 */
package com.sinergise.common.util.collections.tree;

import java.util.ArrayList;

public interface TreeVisitor<T extends ITreeNode<?>> {
	public static abstract class SingleNodeFinder<S extends ITreeNode<?>> implements TreeVisitor<S> {
		public S result;
		
		@Override
		public boolean visit(final S node) {
			if (matches(node)) {
				result = node;
				return false;
			}
			return true;
		}
		
		public abstract boolean matches(S node);
	}
	
	public static abstract class MultiNodeFinder<S extends ITreeNode<?>> implements TreeVisitor<S> {
		public ArrayList<S> result = new ArrayList<S>(1);
		
		@Override
		public boolean visit(final S node) {
			if (matches(node)) {
				result.add(node);
			}
			return true;
		}
		
		public abstract boolean matches(S node);
	}
	
	/**
	 * @param node the current tree node
	 * @return true to continue with the next node
	 */
	boolean visit(T node);
}
