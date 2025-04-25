package com.sinergise.common.geometry.index;

import static com.sinergise.common.geometry.index.PointQuadtree.Empty.EMPTY;
import static com.sinergise.common.util.math.MathUtil.ceilPow2;
import static com.sinergise.common.util.math.MathUtil.floorToMultiple;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.lang.Function;

public class PointQuadtree<E extends HasCoordinate> extends AbstractSet<E> implements PointSpatialIndex<E> {

	interface Node<N extends  com.sinergise.common.util.geom.HasCoordinate> { // full name with package or it won't build 
		int size();

		NodeBase<N> insert(Leaf<N> subtree);

		Node<N> remove(double x, double y, HasCoordinate value);

		boolean contains(double x, double y, HasCoordinate value);

		boolean queryEnvelope(double nodeMinX, double nodeMinY, double nodeMaxX, double nodeMaxY, Envelope env,
			Function<? super N, Boolean> sink);

		boolean reportAll(Function<? super N, Boolean> sink);
	}

	static abstract class NodeBase<N extends HasCoordinate> implements Node<N> {
		final double posX;
		final double posY;
		protected int size = 0;

		public NodeBase(double x, double y) {
			this.posX = x;
			this.posY = y;
		}

		@Override
		public final int size() {
			return size;
		}

		@Override
		public String toString() {
			return posX + ", " + posY;
		}
		
		public boolean equalsPos(NodeBase<?> other) {
			return posX == other.posX && posY == other.posY; 
		}

		public abstract boolean isLeaf();
	}

	/**
	 * <pre>
	 *  2 | 3
	 *  --+--
	 *  0 | 1
	 * </pre>
	 */
	static final class Bulk<N extends HasCoordinate> extends NodeBase<N> {
		public final double childSize;
		
		@SuppressWarnings("unchecked")
		public final Node<N>[] children = new Node[]{EMPTY, EMPTY, EMPTY, EMPTY};

		public Bulk(double x, double y, double childSize) {
			super(x, y);
			this.childSize = childSize;
		}

		@Override
		public NodeBase<N> insert(Leaf<N> value) {
			final int idx = childIdxIfContained(value.posX, value.posY);
			if (idx < 0) {
				return createLarger(this, value);
			}
			return insertContained(value, idx);
		}

		public NodeBase<N> insertContained(Leaf<N> value) {
			return insertContained(value, childIdx(value.posX, value.posY));
		}
		
		public void insertDirectChild(NodeBase<N> value) {
			children[childIdx(value.posX, value.posY)] = value;
			size = value.size;
		}
		
		public NodeBase<N> insertContained(Leaf<N> value, final int idx) {
			children[idx] = children[idx].insert(value);
			size += value.size;
			return this;
		}

		private int childIdxIfContained(double x, double y) {
			switch (childIdxIfContainedOneDim(y, posY)) {
				case 0: return childIdxIfContainedOneDim(x, posX);
				case 1: {
					switch (childIdxIfContainedOneDim(x, posX)) {
						case 0: return 2;
						case 1: return 3;
					}
				}
			}
			return -1;
		}

		private int childIdxIfContainedOneDim(double a, double pos) {
			return a >= pos //
				? (a >= (pos + childSize) ? -1 : 1) //
				: (a < (pos - childSize) ? -1 : 0);
		}

		private int childIdx(double x, double y) {
			return (x >= posX) // 
				? (y >= posY ? 3 : 1) //
				: (y >= posY ? 2 : 0);
		}

		@Override
		public Node<N> remove(double x, double y, HasCoordinate value) {
			final int idx = childIdx(x, y);

			Node<N> oldChild = children[idx];
			int oldSize = oldChild.size();
			Node<N> newChild = oldChild.remove(x, y, value);
			children[idx] = newChild;
			size += newChild.size() - oldSize;
			if (newChild == EMPTY) {
				return prune();
			}
			return this;
		}

		@SuppressWarnings("unchecked")
		private Node<N> prune() {
			int singleChildIdx = -1;
			for (int i = 0; i < 4; i++) {
				if (children[i] != EMPTY) {
					if (singleChildIdx >= 0) {
						return this;
					}
					singleChildIdx = i;
				}
			}
			if (singleChildIdx < 0) { // Shouldn't happen as the tree must have been pruned to leaf before
				return EMPTY; 
			}
			return children[singleChildIdx];
		}

		@Override
		public boolean contains(double x, double y, HasCoordinate value) {
			return children[childIdx(x, y)].contains(x, y, value);
		}

		@Override
		public boolean queryEnvelope(double nodeMinX, double nodeMinY, double nodeMaxX, double nodeMaxY, Envelope env,
			Function<? super N, Boolean> sink) {
			if (!env.intersects(nodeMinX, nodeMinY, nodeMaxX, nodeMaxY)) {
				return true;
			}
			if (env.contains(nodeMinX, nodeMinY, nodeMaxX, nodeMaxY)) {
				return reportAll(sink);
			}
			if (!children[0].queryEnvelope(nodeMinX, nodeMinY, posX, posY, env, sink)) {
				return false;
			}
			if (!children[1].queryEnvelope(posX, nodeMinY, nodeMaxX, posY, env, sink)) {
				return false;
			}
			if (!children[2].queryEnvelope(nodeMinX, posY, posX, nodeMaxY, env, sink)) {
				return false;
			}
			if (!children[3].queryEnvelope(posX, posY, nodeMaxX, nodeMaxY, env, sink)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean reportAll(Function<? super N, Boolean> sink) {
			for (Node<N> n : children) {
				if (!n.reportAll(sink)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

		@Override
		public String toString() {
			return super.toString() + " |" + childSize + "|";
		}

		static <T extends HasCoordinate> Bulk<T> create(Leaf<T> l1, Leaf<T> l2) {
			Bulk<T> ret = create(l1.posX, l1.posY, l2.posX, l2.posY);
			if (ret.childSize < 0.00001) {
				System.out.println("FISHY");
			}
			ret.insertContained(l1);
			ret.insertContained(l2);
			return ret;
		}
		
		private static <T extends HasCoordinate> Bulk<T> createLarger(Bulk<T> l1, Leaf<T> l2) {
			double halfChild = 0.5*l1.childSize;
			double minX = Math.min(l1.posX - halfChild, l2.posX);
			double minY = Math.min(l1.posY - halfChild, l2.posY);
			double maxX = Math.max(l1.posX + halfChild, l2.posX);
			double maxY = Math.max(l1.posY + halfChild, l2.posY);
			Bulk<T> ret = create(minX, minY, maxX, maxY);
			if (l1.posX == ret.posX && l1.posY == ret.posY) {
				// root expanded, keep the same children;
				System.arraycopy(l1.children, 0, ret.children, 0, 4);
				ret.size = l1.size;
			} else {
				ret.insertDirectChild(l1);
			}
			ret.insertContained(l2);
			return ret;
		}

		private static <T extends HasCoordinate> Bulk<T> create(double x1, double y1, double x2, double y2) {
			if (x1 < 0 != x2 < 0 || y1 < 0 != y2 < 0) {
				double ext = max(max(abs(x1), abs(y1)), max(abs(x2), abs(y2)));
				Bulk<T> temp = createForSameSign(0, 0, ext, ext);
				return new Bulk<T>(0, 0, 2*temp.childSize);
			}
			return createForSameSign(x1, y1, x2, y2);
		}

		private static <T extends HasCoordinate> Bulk<T> createForSameSign(double x1, double y1, double x2, double y2) {
			// 1 take larger extent;
			// 2 ceil to next pow 2 - this will be the lower bound for square size
			// 3 floor min and max to size
			// 4 if both floor to same return;
			// 5 size <<<= 1 and goto 3 
			double ext = ceilPow2(max(abs(x2 - x1), abs(y2 - y1)));
			while ((x1 = floorToMultiple(x1, ext)) != floorToMultiple(x2, ext) // 
				|| (y1 = floorToMultiple(y1, ext)) != floorToMultiple(y2, ext)) {
				ext *= 2;
			}
			double childSize = ext / 2;
			return new Bulk<T>(x1 + childSize, y1 + childSize, childSize);
		}
	}

	static final class Leaf<N extends HasCoordinate> extends NodeBase<N> {
		N data;
		Leaf<N> next = null;

		public Leaf(double x, double y, N data) {
			super(x, y);
			this.data = data;
			size = 1;
		}
		
		@Override
		public boolean contains(double x, double y, HasCoordinate value) {
			return this.data.equals(value) || (next != null && next.contains(x, y, value));
		}

		@Override
		public NodeBase<N> insert(Leaf<N> leaf) {
			if (this.posX == leaf.posX && this.posY == leaf.posY) {
				addData(leaf);
				return this;
			}
			return Bulk.create(this, leaf);
		}

		private void addData(Leaf<N> leaf) {
			if (data.equals(leaf.data)) {
				leaf.size = 0; //signal no change upwards
			} else if (next == null) {
				next = leaf;
			} else {
				next.addData(leaf);
			}
			size+=leaf.size;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Node<N> remove(double x, double y, HasCoordinate value) {
			if (x != posX || y != posY) {
				return this;
			}
			Leaf<N> replacement = removeData(value);
			if (replacement == null) {
				return EMPTY;
			}
			return replacement;
		}

		private Leaf<N> removeData(HasCoordinate value) {
			if (data.equals(value)) {
				return next;
			}
			if (next != null) {
				next = next.removeData(value);
				size = 1 + (next == null ? 0 : next.size);
			}
			return this;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}

		@Override
		public boolean queryEnvelope(double nodeMinX, double nodeMinY, double nodeMaxX, double nodeMaxY, Envelope env,
			Function<? super N, Boolean> sink) {
			if (env.contains(posX, posY)) {
				return reportAll(sink);
			}
			return true;
		}

		@Override
		public boolean reportAll(Function<? super N, Boolean> sink) {
			if (!sink.execute(data).booleanValue()) {
				return false;
			}
			return next == null ? true : next.reportAll(sink);
		}

		public boolean wasInserted() {
			return size == 1;
		}
	}

	@SuppressWarnings("rawtypes")
	static enum Empty implements Node {
		EMPTY {
			@Override
			@Deprecated
			public int size() {
				return 0;
			}
			
			@Override
			@Deprecated
			public NodeBase insert(Leaf leaf) {
				return leaf;
			}

			@Override
			@Deprecated
			public boolean contains(double x, double y, HasCoordinate value) {
				return false;
			}

			@Override
			@Deprecated
			public boolean queryEnvelope(double nodeMinX, double nodeMinY, double nodeMaxX, double nodeMaxY,
				Envelope env, Function sink) {
				return true;
			}
			
			@Override
			@Deprecated
			public Node remove(double x, double y, HasCoordinate value) {
				return this;
			}

			@Override
			@Deprecated
			public boolean reportAll(Function sink) {
				return true;
			}
		};


		@SuppressWarnings("unchecked")
		static <N extends HasCoordinate> Node<N> getInstance() {
			return EMPTY;
		}
	}

	Node<E> root = Empty.getInstance();
	private EnvelopeBuilder env = new EnvelopeBuilder();

	public PointQuadtree() {}

	public PointQuadtree(Collection<E> data) {
		setAll(data);
	}

	@Override
	public boolean isEmpty() {
		return root == EMPTY;
	}

	@Override
	public Envelope getEnvelope() {
		return env.getEnvelope();
	}

	@Override
	public int size() {
		return root.size();
	}

	@Override
	public boolean add(E val) {
		double x = val.x();
		double y = val.y();
		Leaf<E> leaf = new Leaf<E>(x, y, val);
		root = root.insert(leaf);
		env.expandToInclude(x, y);
		return leaf.wasInserted();
	}

	@Override
	public boolean contains(Object val) {
		return contains((HasCoordinate)val);
	}

	public boolean contains(HasCoordinate val) {
		return root.contains(val.x(), val.y(), val);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = false;
		for (Object o : c) {
			ret |= remove(o);
		}
		
		return ret;
	}
	
	@Override
	public boolean remove(Object val) {
		return remove((HasCoordinate)val);
	}

	public boolean remove(HasCoordinate val) {
		int oldSize = size();
		root = root.remove(val.x(), val.y(), val);
		return oldSize != size();
	}

	@Override
	public Iterator<E> iterator() {
		if (isEmpty()) {
			return CollectionUtil.emptyIterator();
		}
		return new PointQtIterator<E>((NodeBase<E>)root);
	}

	@Override
	public boolean findInEnvelope(Envelope queryEnvelope, SearchItemReceiver<? super E> sink) {
		return root.queryEnvelope(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY(), queryEnvelope, sink);
	}

	@Override
	public E findNearest(HasCoordinate pos, double withinDistSq, Set<?> excluded) {
		return new PointQtNearestSearcher<E>(pos, excluded).findNearest(root, withinDistSq);
	}

	@Override
	public void clear() {
		root = Empty.getInstance();
	}

	public void setAll(Collection<? extends E> values) {
		Set<E> s = CollectionUtil.asSet(values);
		root = buildTree(s, env);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends HasCoordinate> Node<T> buildTree(Set<? extends T> data, EnvelopeBuilder env) {
		env.clear();
		Leaf<T>[] nodes = new Leaf[data.size()];
		int idx = 0;
		for (T t : data) {
			double x = t.x();
			double y = t.y();
			nodes[idx++] = new Leaf<T>(x, y, t);
			env.expandToInclude(x, y);
		}
		return new Builder<T>(nodes, env).buildTree();
	}
	
	@SuppressWarnings("unchecked")
	private static class Builder<T extends HasCoordinate> {
		Leaf<T>[] nodes;
		EnvelopeBuilder env;
		public Builder(Leaf<T>[] nodes, EnvelopeBuilder env) {
			this.nodes = nodes;
			this.env = env;
		}
		
		public Node<T> buildTree() {
			Bulk<HasCoordinate> temp = Bulk.create(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
			return buildTree(0, nodes.length-1, temp.posX, temp.posY, temp.childSize);
		}

		private Node<T> buildTree(int left, int right, double x, double y, double childSize) {
			int len = right - left + 1;
			switch (len) {
				case 1: return nodes[left];
				case 0: return EMPTY;
			}
			return fillChildren(new Bulk<T>(x, y, childSize), left, right, x, y);
		}

		private Node<T> fillChildren(Bulk<T> node, int left, int right, double x, double y) {
			int mid = partitionY(left, right, y);
			int lmid = left >= mid ? mid : partitionX(left, mid-1, x);
			int rmid = mid > right ? mid : partitionX(mid, right, x);
			if (checkSame(left, right)) {
				return buildLeaves(left, right);
			}
			double halfSize = 0.5 * node.childSize;
			int countNonEmpty = 0;
			countNonEmpty += fillChild(node, 0, left, lmid - 1, halfSize, -1, -1);
			countNonEmpty += fillChild(node, 1, lmid, mid - 1, halfSize, +1, -1);
			countNonEmpty += fillChild(node, 2, mid, rmid - 1, halfSize, -1, +1);
			countNonEmpty += fillChild(node, 3, rmid, right, halfSize, +1, +1);
			
			if (countNonEmpty <= 1) {
				for (Node<T> child : node.children) {
					if (child != EMPTY) {
						return child;
					}
				}
				return EMPTY;
			}
			return node;
		}

		private boolean checkSame(int left, int right) {
			left--;
			while (++left < right) {
				if (!nodes[left].equalsPos(nodes[right])) {
					return false;
				}
			}
			return true;
		}

		private Node<T> buildLeaves(int left, int right) {
			Leaf<T> prevChild = nodes[left];
			int size = 1;
			while (++left <= right) {
				Leaf<T> curChild = nodes[left];
				curChild.next = prevChild;
				curChild.size = ++size;
				prevChild = curChild;
			}
			return prevChild;
		}

		/**
		 * @return 1 if child is not EMPTY
		 */
		private int fillChild(Bulk<T> parent, int childIdx, int left, int right, double halfSize, int signX, int signY) {
			Node<T> child = buildTree(left, right, parent.posX + signX*halfSize, parent.posY + signY*halfSize, halfSize);
			if (child == EMPTY) {
				return 0;
			}
			parent.children[childIdx] = child;
			parent.size += child.size();
			return 1;
		}

		public int partitionX(int left, int right, double value) {
			//find first swap
			while (nodes[left].posX < value) {
				if (left++ == right) {
					return left;
				}
			}
			while (nodes[right].posX >= value) {
				if (left == right--) {
					return left;
				}
			}
			//all the rest don't need to check for boundary 
			while (left < right) {
				swap(left, right);
				while(nodes[++left].posX < value) {} 
				while(nodes[--right].posX >= value) {}
			}
			return left;
		}
		
		public int partitionY(int left, int right, double value) {
			//find first swap
			while (nodes[left].posY < value) {
				if (left++ == right) {
					return left;
				}
			}
			while (nodes[right].posY >= value) {
				if (left == right--) {
					return left;
				}
			}
			//all the rest don't need to check for boundary 
			while (left < right) {
				swap(left, right);
				while(nodes[++left].posY < value) {} 
				while(nodes[--right].posY >= value) {}
			}
			return left;
		}

		private void swap(int i, int j) {
			Leaf<T> tmp = nodes[i];
			nodes[i] = nodes[j];
			nodes[j] = tmp;
		}
	}

	public E findLeftmost(Set<? super E> excluded) {
		return new PointQtNearestSearcher<E>(excluded).findLeftmost(root);
	}
}
