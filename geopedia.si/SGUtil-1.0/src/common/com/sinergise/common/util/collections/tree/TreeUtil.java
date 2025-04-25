package com.sinergise.common.util.collections.tree;

public class TreeUtil {
	
	@SuppressWarnings("unchecked")
	public static <T extends ITreeNode<T>> T lastDeep(final ITreeNode<T> parent) {
		final int childCount = parent.getChildCount();
		if (childCount == 0) {
			return (T)parent;
		}
		return lastDeep(parent.getChild(childCount - 1));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ITreeNode<T>> boolean removeFromParent(final T node) {
		T parent = null;
		if (node instanceof ISingleParentTreeNode) {
			parent = ((ISingleParentTreeNode<T>)node).getParent();
		} else if (node instanceof IBoundTreeNode) {
			@SuppressWarnings("cast")
			final AbstractTree<T> model = (AbstractTree<T>)((IBoundTreeNode<T, ? extends AbstractTree<T>>)node).getModel();
			parent = model.getParent(node);
		}
		if (parent instanceof IMutableTreeNode) {
			((IMutableTreeNode<T>)parent).remove(node);
			return true;
		}
		return false;
	}
	
	public static <T extends ITreeNode<T>> boolean traverseChildren(final ITreeNode<T> parent, final TreeVisitor<T> visitor) {
		final int cnt = parent.getChildCount();
		if (cnt == 0) {
			return true;
		}
		for (int i = 0; i < cnt; i++) {
			if (!visitor.visit(parent.getChild(i))) {
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ITreeNode<T>> boolean traverseDescendants(final ITreeNode<T> parent, final TreeVisitor<T> visitor) {
		if (!visitor.visit((T)parent)) {
			return false;
		}
		final int cnt = parent.getChildCount();
		if (cnt == 0) {
			return true;
		}
		for (int i = 0; i < cnt; i++) {
			if (!traverseDescendants(parent.getChild(i), visitor)) {
				return false;
			}
		}
		return true;
	}
}
