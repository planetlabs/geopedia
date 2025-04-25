/*
 *
 */
package com.sinergise.common.util.collections.tree;

public interface IBoundTreeNode<T extends ITreeNode<T>, M extends AbstractTree<T>> extends ITreeNode<T> {
	M getModel();
	void attachedToModel(M newModel);
	void detachedFromModel(M oldModel);
}
