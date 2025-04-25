/*
 *
 */
package com.sinergise.common.util.collections.tree;


public interface ISingleParentTreeNode<T extends ITreeNode<T>> extends ITreeNode<T> {
	public static abstract class AbstractSingleParentNode<S extends ITreeNode<S>> implements ISingleParentTreeNode<S>{
		protected S parent;
		@Override
		public void onAttach(S newParent) {
			this.parent = newParent;
		}
		
		@Override
		public void onDetach(S oldParent) {
			if (this.parent != oldParent) throw new IllegalArgumentException("Cannot detach from a node that's not the current parent");
			this.parent = null;
		}
		
		@Override
		public S getParent() {
			return parent;
		}
	}
	
	public T getParent();
}
