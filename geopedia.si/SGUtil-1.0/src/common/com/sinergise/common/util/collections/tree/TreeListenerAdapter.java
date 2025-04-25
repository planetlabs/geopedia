/*
 *
 */
package com.sinergise.common.util.collections.tree;

@SuppressWarnings("rawtypes")
public abstract class TreeListenerAdapter<T extends ITreeNode> implements TreeListener<T> {
	@Override
	public void nodeAdded(final T parent, final T added, final int newIndex) {}
	
	@Override
	public void nodeRemoved(final T parent, final T removed, final int oldIndex) {}
	
	@Override
	public void treeStructureChanged(final T changeRoot) {}
	
	@Override
	public void nodeChanged(final T node, final String propertyName) {}
}
