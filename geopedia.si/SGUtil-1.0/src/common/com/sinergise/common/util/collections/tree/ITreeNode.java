/*
 *
 */
package com.sinergise.common.util.collections.tree;

public interface ITreeNode<T extends ITreeNode<T>> {
	T getChild(int index);
	
	int getChildCount();
	
	int indexOf(T child);
	
	/**
	 * Called when this node is added to the tree or has its parent changed 
	 * 
	 * @param parent the node that is the new immediate parent of this node
	 */
	void onAttach(T parent);
	
	/**
	 * Called when this node is removed from its immediate parent
	 * 
	 * @param parent the node that is no longer the immediate parent of this node
	 */
	void onDetach(T parent);
}