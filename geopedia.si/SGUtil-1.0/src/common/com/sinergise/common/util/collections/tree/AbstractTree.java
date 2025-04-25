package com.sinergise.common.util.collections.tree;

import com.sinergise.common.util.collections.tree.TreeVisitor.SingleNodeFinder;

@SuppressWarnings("rawtypes")
public class AbstractTree<T extends ITreeNode<T>> implements SourcesTreeEvents<T> {
	protected T							root;
	private TreeListenersCollection<T>	treeListeners;

	public AbstractTree(final T root) {
		this(root, false);
	}

	protected AbstractTree(final T root, final boolean allowNull) {
		if (root == null && !allowNull) {
			throw new NullPointerException("Cannot construct a tree with null root");
		}
		
		if (root != null) {
			setRoot(root);
		}
	}

	protected void setRoot(final T root) {
		this.root = root;
		childAdded(null, root, 0);
	}

	public T getRoot() {
		return root;
	}

	@Override
	public boolean addTreeListener(final TreeListener<T> listener) {
		if (treeListeners == null) {
			treeListeners = new TreeListenersCollection<T>();
		}
		if (treeListeners.contains(listener)) {
			return false;
		}
		return treeListeners.add(listener);
	}

	@Override
	public boolean removeTreeListener(final TreeListener<T> listener) {
		if (treeListeners == null) {
			return false;
		}
		return treeListeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public void childAdded(final ITreeNode<T> parent, final T child, final int index) {
		child.onAttach((T)parent);
		if (child instanceof IBoundTreeNode) ((IBoundTreeNode)child).attachedToModel(this);
		if (treeListeners == null) return;
		treeListeners.fireNodeAdded((T)parent, child, index);
	}

	@SuppressWarnings("unchecked")
	public void childRemoved(final ITreeNode<T> parent, final T child, final int oldIdx) {
		if (child instanceof IBoundTreeNode) ((IBoundTreeNode)child).detachedFromModel(this);
		child.onDetach((T)parent);
		if (treeListeners == null) return;
		treeListeners.fireNodeRemoved((T)parent, child, oldIdx);
	}

	@SuppressWarnings("unchecked")
	public void nodeChanged(final ITreeNode<T> node, final String propertyName) {
		if (treeListeners == null) {
			return;
		}
		treeListeners.fireNodeChanged((T)node, propertyName);
	}

	public boolean traverseDepthFirst(final TreeVisitor<T> visitor) {
		return traverseSubTree(root, true, visitor);
	}

	public boolean traverseSubTree(final T start, final boolean depthFirst, final TreeVisitor<T> visitor) {
		final boolean goOn = visitor.visit(start);
		if (!goOn) {
			return false;
		}
		return traverseDescendants(start, depthFirst, visitor);
	}

	public boolean traverseDescendants(final T parent, final boolean depthFirst, final TreeVisitor<T> visitor) {
		if (depthFirst) {
			boolean goOn = true;
			final int numCh = parent.getChildCount();
			for (int i = 0; i < numCh; i++) {
				goOn = traverseSubTree(parent.getChild(i), true, visitor);
				if (!goOn) {
					return false;
				}
			}
			return goOn;
		}
		throw new UnsupportedOperationException("Breadth first traversal not supported");
	}

	@SuppressWarnings("unchecked")
	public T getParent(final ITreeNode<T> child) {
		if (child instanceof ISingleParentTreeNode) {
			return ((ISingleParentTreeNode<T>)child).getParent();
		}

		final SingleNodeFinder<ITreeNode<T>> finder = new SingleNodeFinder<ITreeNode<T>>() {
			@Override
			public boolean matches(final ITreeNode<T> node) {
				return node.indexOf((T)child) >= 0;
			}
		};

		traverseDepthFirst((TreeVisitor<T>)finder);
		return (T)finder.result;
	}

	public boolean isDescendant(final T descendant, final T node) {
		if (descendant instanceof ISingleParentTreeNode) {
			return isAncestor(node, (ISingleParentTreeNode)descendant);
		}
		final SingleNodeFinder<T> finder = new SingleNodeFinder<T>() {
			@Override
			public boolean matches(final ITreeNode candidate) {
				return node == candidate;
			}
		};
		traverseDescendants(node, true, finder);
		return finder.result == descendant;
	}

	@SuppressWarnings("unchecked")
	public boolean isAncestor(final T ancestor, final ISingleParentTreeNode node) {
		final T parent = (T)node.getParent();
		if (parent == ancestor) return true;
		if (parent == root) return false;
		if (parent instanceof ISingleParentTreeNode) return isAncestor(ancestor, (ISingleParentTreeNode)parent);
		return isDescendant(parent, ancestor);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AbstractTree other = (AbstractTree)obj;
		if (root == null) {
			if (other.root != null) return false;
		} else if (!root.equals(other.root)) return false;
		return true;
	}
}
