/*
 *
 */
package com.sinergise.common.util.collections.tree;

@SuppressWarnings("rawtypes")
public interface SourcesTreeEvents<T extends ITreeNode> {
	boolean addTreeListener(TreeListener<T> listener);
	
	boolean removeTreeListener(TreeListener<T> listener);
}
