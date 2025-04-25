/*
 *
 */
package com.sinergise.common.util.collections.tree;

import java.util.Vector;

@SuppressWarnings("rawtypes")
public class TreeListenersCollection<T extends ITreeNode> extends Vector<TreeListener<T>> {
	private static final long serialVersionUID = 1L;
	
	public void fireNodeAdded(final T parent, final T child, final int newIndex) {
		for (final TreeListener<T> treeListener : this) {
			treeListener.nodeAdded(parent, child, newIndex);
		}
	}
	
	public void fireNodeRemoved(final T parent, final T child, final int oldIndex) {
		for (final TreeListener<T> treeListener : this) {
			treeListener.nodeRemoved(parent, child, oldIndex);
		}
	}
	
	public void fireNodeChanged(final T node, final String propertyName) {
		for (final TreeListener<T> treeListener : this) {
//			long t = System.nanoTime();
			treeListener.nodeChanged(node, propertyName);
//			long tDur = System.nanoTime()-t;
//			System.out.println(">> "+propertyName+"@"+node+" -> "+treeListener+" ["+tDur+"]");
		}
	}
	
	public void fireStructureChanged(final T changeRoot) {
		for (final TreeListener<T> treeListener : this) {
			treeListener.treeStructureChanged(changeRoot);
		}
	}
}
