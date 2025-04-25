/*
 *
 */
package com.sinergise.common.util.collections.tree;

public interface IMutableTreeNode<T extends ITreeNode<T>> extends ITreeNode<T> {

	public void add(T child, int index);

	public void remove(T child);

}
