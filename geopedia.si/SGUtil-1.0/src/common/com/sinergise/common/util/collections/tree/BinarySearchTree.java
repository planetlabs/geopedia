/**
 * 
 */
package com.sinergise.common.util.collections.tree;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.sinergise.common.util.collections.tree.BinarySearchTree.BSTNode;

/**
 * Implements an unbalanced binary search tree (BST).<br/>
 * Elements with compareTo == 0 are added to the left (resulting in reverse insertion order), 
 * duplicate elements (equals == true) are added only once.
 * 
 * @author tcerovski
 */
public class BinarySearchTree<T> extends AbstractTree<BSTNode<T>> {
	
	public static interface InsertionComparator<S> {
		int compareForInsert(S elementInCollection, S insertedElement);
	}
	
	private static class ICFWithComparator<S> implements InsertionComparator<S> {
		private final Comparator<? super S> comp;
		public ICFWithComparator(Comparator<? super S> comp) {
			this.comp = comp;
		}
		@Override
		public int compareForInsert(S elementInCollection, S insertedElement) {
			return comp.compare(elementInCollection, insertedElement);
		}
	}
	
	private static class ICFWithComparable<S extends Comparable<? super S>> implements InsertionComparator<S> {
		public ICFWithComparable() {
		}

		@Override
		public int compareForInsert(S elementInCollection, S insertedElement) {
			return elementInCollection.compareTo(insertedElement);
		}
	}

	/** Map of nodes mapped by their elements for faster searching of nodes */
	protected final Map<T, BSTNode<T>> nodes = new HashMap<T, BSTNode<T>>();
	protected final InsertionComparator<? super T> compareFunction;
	
	/** 
	 * Constructs new empty tree that uses the natural order (compareTo function) 
	 * of the Comparable elements for insertion. 
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public BinarySearchTree() {
		this(new ICFWithComparable());
	}
	
	/** 
	 * Constructs new empty tree that uses the provided comparator for insertion. 
	 */
	public BinarySearchTree(Comparator<? super T> insertionComparator) {
		this(new ICFWithComparator<T>(insertionComparator));
	}
	
	/** 
	 * Constructs new empty tree that uses the provided insertCompareFunction. 
	 */
	public BinarySearchTree(InsertionComparator<? super T> insertionCompareFunction) {
		super(null, true);
		this.compareFunction = insertionCompareFunction;
		if (compareFunction == null) throw new IllegalArgumentException("Canno construct a tree without the comparison function.");
	}

	/**
	 * @return Number of nodes in the tree.
	 */
	public int size() {
		return nodes.size();
	}

	/**
	 * @return Top tree element.
	 */
	public T getTopElement() {
		return getElementOrNull(root);
	}

	private T getElementOrNull(BSTNode<T> node) {
		return node == null ? null : node.getElement();
	}

	/**
	 * @param el
	 * @return <code>true</code> if the tree contains provided element.
	 */
	public boolean contains(T el) {
		return nodes.containsKey(el);
	}

	public T getParent(T el) {
		return getElementOrNull(getNode(el).getParent());
	}

	private BSTNode<T> getNode(T el) {
		BSTNode<T> node = getNodeIfExists(el);
		if (node == null) {
			throw new NoSuchElementException("Element not in the tree: " + el);
		}
		return node;
	}

	private BSTNode<T> getNodeIfExists(T el) {
		if (el == null) {
			throw new NullPointerException("Can't search for null element in the tree");
		}
		return nodes.get(el);
	}

	/**
	 * @param el
	 * @return left child of the provided element 
	 * or <code>null</code> if element is root */
	
	public T getLeftChild(T el) {
		return getElementOrNull(getNode(el).getLeft());
	}

	/**
	 * @param el
	 * @return right child of the provided element 
	 * or <code>null</code> if element is root */
	
	public T getRightChild(T el) {
		return getElementOrNull(getNode(el).getRight());
	}

	/**
	 * @param el
	 * @return largest element not greater than provided element 
	 * or <code>null</code> if element not in the tree or
	 * the element is the smallest element in the tree.
	 */
	
	public T predecessor(T el) {
		return getElementOrNull(predecessorNode(getNode(el)));
	}

	protected BSTNode<T> predecessorNode(BSTNode<T> node) {
		if (node.hasLeft()) {
			return findMax(node.getLeft());
		}
		
		while (node.isLeftChild()) {
			node = node.getParent();
		}
		return node.getParent();
	}

	/**
	 * @param el
	 * @return smallest element not smaller than provided element 
	 * or <code>null</code> if element not in the tree or 
	 * the element is the largest element in the tree.	 */
	public  T successor(T el){
		return getElementOrNull(successorNode(getNode(el)));
	}

	protected BSTNode<T> successorNode(BSTNode<T> node) {
		if (node.hasRight()) {
			return findMin(node.getRight());
		}
		while (node.isRightChild()) {
			node = node.getParent();
		}
		return node.getParent();
	}

	/**
	 * Finds the smallest (left most) element in the tree.
	 * @return smallest (left most) element in the tree or <code>null</code> if the tree is empty.
	 */
	public T findMin() {
		return getElementOrNull(findMin(root));
	}

	/**
	 * Finds the smallest (left most) element in the subtree under <code>parent</code>.
	 * @param parent - subtree root
	 * @return node containing the smallest (left most) element
	 */
	protected BSTNode<T> findMin(BSTNode<T> parent) {
		if (parent != null) {
			while (parent.hasLeft()) {
				parent = parent.getLeft();
			}
		}
		return parent;
	}

	/**
	 * Finds the largest (right most) element in the tree.
	 * @return largest (right most) element in the tree or <code>null</code> if the tree is empty.
	 */
	public T findMax() {
		return getElementOrNull(findMax(root));
	}

	/**
	 * Finds the largest (right most) element in the subtree under <code>parent</code>.
	 * @param parent - subtree root
	 * @return node containing the largest (right most) element
	 */
	protected BSTNode<T> findMax(BSTNode<T> parent) {
		if (parent != null) {
			while (parent.hasRight()) {
				parent = parent.getRight();
			}
		}
		return parent;
	}

	/**
	 * Insert new element into the tree.
	 * @param el - element to insert
	 */
	public void insert(T el) {
		BSTNode<T> existingNode = getNodeIfExists(el);
		if (existingNode != null) {
			setNodeEl(existingNode, el);
			return;
		}
		root = insert(el, root);
	}

	private void setNodeEl(BSTNode<T> node1, T el2) {
		node1.setElement(el2);
		nodes.put(el2, node1);
	}

	public void remove(final T el) {
		removeNode(getNode(el));
		if (nodes.remove(el) == null) {
			throw new IllegalStateException("Node removed from tree, but couldn't be removed from the index map");
		}
	}

	protected void removeNode(BSTNode<T> n) {
		final BSTNode<T> oldParent = n.getParent();
		final BSTNode<T> replacement;
		
		if (n.hasLeft() && !n.hasRight()) { //remove node with one child on the left
			replacement = n.getLeft();
		
		} else if (n.hasRight() && !n.hasLeft())  { //remove node with one child on the right
			replacement = n.getRight();
	
		} else if (n.hasLeft() && n.hasRight()) { //remove node with two children
			replacement = findMin(n.getRight());
			removeNode(replacement);
			// attach sub-trees to new node
			replacement.setLeft(n.getLeft());
			replacement.setRight(n.getRight());
			
		} else {
			replacement = null;
		}
		
		if (oldParent == null) {
			root = replacement;
			if (replacement != null) {
				replacement.onDetach(replacement.getParent());
			}
		} else {
			oldParent.setLeftOrRight(replacement, n.isLeftChild());
		}
	}

	/**
	 * Removes all elements from the tree. 
	 */
	public void clear() {
		root = null;
		nodes.clear();
	}

	public void swap(T el1, T el2){
		//First get both, then set both
		BSTNode<T> node1 = getNode(el1);
		BSTNode<T> node2 = getNode(el2);
		
		setNodeEl(node1, el2);
		setNodeEl(node2, el1);
	}

	/**
	 * Insert into the subtree under <code>parent</code>.
	 * @param el - element to insert
	 * @param parent - subtree root
	 * @return new subtree root
	 */
	protected BSTNode<T> insert(final T el, final BSTNode<T> parent) {
		if (parent == null) {
			BSTNode<T> insNode = new BSTNode<T>(el);
			nodes.put(el, insNode);
			return insNode;
		}
		final boolean insLeft = (compareFunction.compareForInsert(parent.getElement(), el) >= 0);
		final BSTNode<T> subRoot = parent.getLeftOrRight(insLeft); 
		final BSTNode<T> newRoot = insert(el, subRoot);
		if (subRoot != newRoot) {
			parent.setLeftOrRight(newRoot, insLeft);
		}
		return parent;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		T cur = findMin();
		while (cur != null) {
			sb.append(cur);
			cur = successor(cur);
			if (cur != null) sb.append(", ");
		}
		return sb.toString(); 
	}

	public String toString(boolean treeLike) {
		if (!treeLike) return toString();
		
		StringBuilder sb = new StringBuilder();
		appendNode(sb, root, "");
		return sb.toString(); 
	}

	private static <T> void appendNode(StringBuilder sb, BSTNode<T> node, String prefix) {
		sb.append(node.getElement());
		sb.append('\n');
		if (node.hasLeft()) {
			sb.append(prefix).append("L ");
			appendNode(sb, node.getLeft(), prefix+"  ");
		}
		if (node.hasRight()) {
			sb.append(prefix).append("R ");
			appendNode(sb, node.getRight(), prefix+"  ");
		}
	}

	/**
	 * Internal {@link BinarySearchTree} node, containing references to 
	 * its left and right nodes and the element stored in the node.
	 */
	public static class BSTNode<T> implements ISingleParentTreeNode<BSTNode<T>>, IMutableTreeNode<BSTNode<T>> {
		private BSTNode<T> leftChild = null;
		private BSTNode<T> rightChild = null;
		private BSTNode<T> parent = null;
		private T element;
		
		protected BSTNode(T element) {
			this.element = element;
		}
		
		public boolean hasLeft() {
			return leftChild != null;
		}
		
		public boolean hasRight() {
			return rightChild != null;
		}

		public void setLeft(BSTNode<T> left) {
			if (left == this.leftChild) return;
			if (hasLeft()) {
				remove(this.leftChild);
			}
			this.leftChild = left;
			if (hasLeft()) {
				left.onAttach(this);
			}
		}
		
		public void setLeftOrRight(BSTNode<T> child, boolean left) {
			if (left) {
				setLeft(child);
			} else {
				setRight(child);
			}
		}
		
		public BSTNode<T> getLeftOrRight(boolean left) {
			return left ? leftChild : rightChild;
		}
		
		public void setRight(BSTNode<T> right) {
			if (right == this.rightChild) return;
			if (hasRight()) {
				remove(this.rightChild);
			}
			this.rightChild = right;
			if (hasRight()) {
				right.onAttach(this);
			}
		}
		
		@Override
		public BSTNode<T> getChild(int index) {
			if (index == 0) {
				return leftChild;
			}
			if (index == 1) {
				return rightChild;
			}
			throw new IndexOutOfBoundsException();
		}
		
		@Override
		public int getChildCount() {
			if (!hasLeft() && !hasRight()) {
				return 0;
			}
			return 2;
		}
		
		@Override
		public int indexOf(BSTNode<T> child) {
			if (hasLeft() && leftChild.equals(child)) {
				return 0;
			}
			if (hasRight() && rightChild.equals(child)) {
				return 1;
			}
			return -1;
		}
		
		public boolean isLeftChild() {
			if (parent==null) {
				return false;
			}
			return this == parent.leftChild;
		}
		
		public boolean isRightChild() {
			if (parent==null || parent.rightChild == null) {
				return false;
			}
			return parent.rightChild.equals(this);
		}
		
		@Override
		public void onAttach(BSTNode<T> newParent) {
			if (this.parent == newParent) {
				return;
			}
			if (this.parent != null) {
				this.parent.remove(this);
			}
			this.parent = newParent;
		}
		
		@Override
		public void onDetach(BSTNode<T> oldParent) {
			if (oldParent == this.parent) {
				this.parent = null;
			} else {
				throw new IllegalArgumentException("Not this node's parent - this: "+this+" detaching: "+oldParent+" parent: "+parent);
			}
		}
		
		public BSTNode<T> getLeft() {
			return leftChild;
		}
		
		public BSTNode<T> getRight() {
			return rightChild;
		}
		
		public T getElement() {
			return element;
		}
		
		public void setElement(T element) {
			this.element = element;
		}
		
		@Override
		public BSTNode<T> getParent() {
			return parent;
		}
		
		@Override
		public void remove(BSTNode<T> child) {
			child.onDetach(this);
			if (child == leftChild) {
				leftChild = null;
			} else if (child == rightChild) {
				rightChild = null;
			} else {
				throw new IllegalArgumentException("Not this node's child: "+child);
			}
		}
		
		@Override
		public void add(BSTNode<T> child, int index) {
			if (index == 0 && leftChild == null) {
				setLeft(child);
			} else if (index == 1 && leftChild != null && rightChild == null) {
				setRight(child);
			} else {
				throw new IllegalArgumentException("Index not appropriate for this node's state");
			}
		}
		
		@Override
		public String toString() {
			return "BSTNode["+element+"]";
		}
	}
}
