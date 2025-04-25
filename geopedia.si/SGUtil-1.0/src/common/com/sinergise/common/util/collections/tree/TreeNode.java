/*
 *
 */
package com.sinergise.common.util.collections.tree;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public abstract class TreeNode<T extends ITreeNode<T>, M extends AbstractTree<T>> implements IBoundTreeNode<T, M>, IMutableTreeNode<T>, ISingleParentTreeNode<T> {
	protected T parent;
	protected M model;
	
	protected ArrayList<T> children;
	
	@Override
	public T getParent() {
		return parent;
	}
	
	@Override
	public M getModel() {
		return model;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onAttach(final T newParent) {
		this.parent = newParent;
		if (newParent instanceof IBoundTreeNode) attachedToModel(((IBoundTreeNode<T,M>)newParent).getModel());
	}
	
	@Override
	public void onDetach(final T oldParent) {
		if (oldParent != parent) throw new IllegalArgumentException("TreeNode can only be removed from its immediate parent.");
		this.parent = null;
		if (oldParent instanceof IBoundTreeNode) detachedFromModel(this.model);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void attachedToModel(final M newModel) {
		if (model == newModel) return;
		model = newModel;
		TreeUtil.traverseChildren(this, new TreeVisitor<T>() {
			@Override
			public boolean visit(T node) {
				if (node instanceof IBoundTreeNode) {
					((IBoundTreeNode<?, M>)node).attachedToModel(model);
				}
				return true;
			}
		});
	}
	
	@Override
	public void detachedFromModel(final M oldModel) {
		if (model != oldModel) throw new IllegalArgumentException("Cannot detach from the model if it's not attached.");
		model = null;
		TreeUtil.traverseChildren(this, new TreeVisitor<T>() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean visit(final T node) {
				if (node instanceof IBoundTreeNode) {
					((IBoundTreeNode<T,M>)node).detachedFromModel(oldModel);
				}
				return true;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	protected void removeFromParent() {
		if (isRoot()) {
			model.setRoot(null);
			return;
		}
		if (parent == null && model == null) {
			throw new IllegalStateException("Need a parent or a model to successfully remove myself");
		}
		if (parent == null) {
			parent = model.getParent(this);
		}
		if (parent instanceof IMutableTreeNode) {
			((IMutableTreeNode)parent).remove(this);
		}
	}
	
	public void add(final T child) {
		add(child, getChildCount());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(final T child, final int index) {
		TreeUtil.removeFromParent(child);
		if (children == null) children = new ArrayList<T>();
		children.add(index, child);
		
		if (model != null) {
			model.childAdded(this, child, index);
		} else {
			child.onAttach((T)this);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void remove(final T child) {
		final int oldIdx = indexOf(child);
		if (oldIdx < 0) return;
		
		children.remove(oldIdx);
		if (model != null) {
			model.childRemoved(this, child, oldIdx);
		} else {
			child.onDetach((T)this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public int indexInParent() {
		if (parent == null) {
			return -1;// ROOT or not initialized
		}
		return parent.indexOf((T)this);
	}
	
	@Override
	public int indexOf(final T child) {
		if (children == null) {
			throw new IllegalStateException("Can't get child index from empty node");
		}
		// if (this != child.parent) throw new IllegalStateException("Can't get index - node is not a child of this node");
		return children.indexOf(child);
	}
	
	@Override
	public T getChild(final int index) {
		if (children == null) {
			throw new IllegalStateException("Can't get children from empty node");
		}
		return children.get(index);
	}
	
	@Override
	public int getChildCount() {
		if (children == null) {
			return 0;
		}
		return children.size();
	}
	
	public boolean hasNoChildren() {
		if (children == null) {
			return true;
		}
		return children.isEmpty();
	}
	
	public ITreeNode nextSibling(final boolean down) {
		if (parent == null) return null; // root
		final int idx = indexInParent();
		
		if (down && idx + 1 < parent.getChildCount()) return parent.getChild(idx + 1);
		if (!down && idx - 1 >= 0) return parent.getChild(idx - 1);
		return null;
	}
	
	public ITreeNode lastDescendant() {
		if (hasNoChildren()) return null;
		return TreeUtil.lastDeep(this);
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	
	public void notifyChange(final String prop_name) {
		if (model != null) model.nodeChanged(this, prop_name);
	}
	
	public void removeAll() {
		if (children != null) {
			for (int i = children.size() - 1; i >= 0; i--) {
				remove(children.get(i));
			}
		}
	}
}
