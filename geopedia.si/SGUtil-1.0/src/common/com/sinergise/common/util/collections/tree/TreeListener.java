/*
 *
 */
package com.sinergise.common.util.collections.tree;

@SuppressWarnings("rawtypes")
public interface TreeListener<T extends ITreeNode> {
	void nodeAdded(T parent, T added, int newIndex);
	
	void nodeRemoved(T parent, T removed, int oldIndex);
	
	void treeStructureChanged(T changeRoot);
	
	void nodeChanged(T node, String propertyName);
}
