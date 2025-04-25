package com.sinergise.common.geometry.index;

import static com.sinergise.common.util.collections.CollectionUtil.partitionByKthSmallest;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.collections.SearchItemReceiver.SetCollector;
import com.sinergise.common.util.collections.sort.VirtualSorter;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.math.MathUtil;

public class KdTree<E extends HasCoordinate> extends AbstractSet<E> implements PointSpatialIndex<E> {
	private static final Envelope ENV_EMPTY = Envelope.getEmpty();
	
	private static final class KdTreeBuilder<E extends HasCoordinate> implements VirtualSorter {
		private final KdNode<E>[] arr;
		private Dimension dim;
		
		public KdTreeBuilder(KdNode<E>[] arr) {
			this.arr = arr;
		}
		
		@Override
		public void swap(int i1, int i2) {
			ArrayUtil.swap(arr, i1, i2);
		}

		@Override
		public int compare(int i1, int i2) {
			return dim.compare(arr[i1], arr[i2]);
		}
		
		public final KdNode<E> initParent(int idx, KdNode<E> parent) {
			KdNode<E> ret = arr[idx];
			ret.initParent(parent, dim);
			return ret;
		}

		public KdNode<E> buildTree() {
			dim = Dimension.X;
			return buildTree(null, 0, arr.length - 1);
		}
		private KdNode<E> buildTree(KdNode<E> parent, int leftIdx, int rightIdx) {
			assert leftIdx <= rightIdx;
			if (leftIdx == rightIdx) {
				return initParent(leftIdx, parent);
			}
			int pivotIdx = partitionByKthSmallest(this, leftIdx, rightIdx, (leftIdx + rightIdx) / 2);
			KdNode<E> root = initParent(pivotIdx, parent);

			dim = dim.next();
			if (pivotIdx > leftIdx) {
				root.setLowChild(buildTree(root, leftIdx, pivotIdx - 1));
			}
			root.setHighChild(buildTree(root, pivotIdx + 1, rightIdx));
			return root;
		}


	}
	
	protected static enum Dimension {
		X() {
			@Override
			public Envelope lowHalf(Envelope e, double x, double y) {
				double minX = e.getMinX();
				return x < minX ? ENV_EMPTY : new Envelope(minX, e.getMinY(), x, e.getMaxY());
			}

			@Override
			public Envelope highHalf(Envelope e, double x, double y) {
				double maxX = e.getMaxX();
				return x > maxX ? ENV_EMPTY : new Envelope(x, e.getMinY(), maxX, e.getMaxY());
			}

			@Override
			public <F extends HasCoordinate> int compare(KdNode<F> a, KdNode<F> b) {
				return MathUtil.fastCompare(a.x, b.x);
			}
			
			@Override
			public int compare(double xa, double ya, double xb, double yb) {
				return MathUtil.fastCompare(xa, xb);
			}

			@Override
			public int compare(HasCoordinate a, double xb, double yb) {
				return MathUtil.fastCompare(a.x(), xb);
			}
			
			@Override
			public double delta(double xa, double ya, double xb, double yb) {
				return xb - xa;
			}

			@Override
			public Dimension next() {
				return Y;
			}	
		},
		Y() {
			@Override
			public Envelope lowHalf(Envelope e, double x, double y) {
				return new Envelope(e.getMinX(), e.getMinY(), e.getMaxX(), y);
			}
			
			@Override
			public Envelope highHalf(Envelope e, double x, double y) {
				return new Envelope(e.getMinX(), y, e.getMaxX(), e.getMaxY());
			}
			
			@Override
			public <F extends HasCoordinate> int compare(KdTree.KdNode<F> a, KdTree.KdNode<F> b) {
				return MathUtil.fastCompare(a.y, b.y);
			}
			
			@Override
			public int compare(double xa, double ya, double xb, double yb) {
				return MathUtil.fastCompare(ya, yb);
			}
			
			@Override
			public int compare(HasCoordinate a, double xb, double yb) {
				return MathUtil.fastCompare(a.y(), yb);
			}
			
			@Override
			public double delta(double xa, double ya, double xb, double yb) {
				return yb - ya;
			}
			
			@Override
			public Dimension next() {
				return X;
			}
		};
		
		public abstract Dimension next();
		public abstract int compare(double xa, double ya, double xb, double yb);
		public abstract int compare(HasCoordinate a, double xa, double xb);
		public abstract double delta(double xa, double ya, double xb, double yb);

		public final <F extends HasCoordinate> KdNode<F> min(KdNode<F> a, KdNode<F> b) {
			if (compare(a, b) < 0) {
				return a;
			}
			return b;
		}
		
		public abstract <F extends HasCoordinate> int compare(KdNode<F> a, KdNode<F> b);

		public abstract Envelope lowHalf(Envelope nodeEnv, double x, double y);
		public abstract Envelope highHalf(Envelope e, double x, double y);
	}

	protected static final class KdNode<E extends HasCoordinate> implements HasCoordinate, Iterable<E> {
		
		private static final Function<KdNode<?>, HasCoordinate> DATA_GETTER = new Function<KdNode<?>, HasCoordinate>() {
			@Override
			public HasCoordinate execute(KdNode<?> param) {
				return param.getData();
			}
		};
		
		protected Dimension d;
		protected KdNode<E> parent;
		E data;
		protected KdNode<E> lowChild;
		protected KdNode<E> highChild;
		private int size;

		double x;
		double y;
		
		private KdNode(E data) {
			assert (data != null);
			this.setData(data);
			this.size = 1;
		}
		
		private void initParent(KdNode<E> p, Dimension dim) {
			assert this.parent == null && this.d == null && this.data != null; 
			this.d = dim;
			this.parent = p;
		}

		public KdNode(KdNode<E> parent, E data) {
			assert (parent == null || data != null);

			this.parent = parent;
			this.d = parent == null ? Dimension.X : parent.d.next();
			this.setData(data);
			this.size = data == null ? 0 : 1;
		}
		
		@Override
		public double x() {
			return x;
		}
		
		@Override
		public double y() {
			return y;
		}

		public int size() {
			return size;
		}

		public KdNode<E> next() {
			if (hasLow()) {
				return lowChild;
			}
			return nextHigh();
		}

		private KdNode<E> nextHigh() {
			if (hasHigh()) {
				return highChild;
			}
			return nextUp();
		}

		private KdNode<E> nextUp() {
			if (parent == null) {
				return null;
			}
			if (isLow()) {
				return parent.nextHigh();
			}
			return parent.nextUp();
		}

		private boolean hasLow() {
			return lowChild != null;
		}

		private boolean hasHigh() {
			return highChild != null;
		}

		private boolean isLeaf() {
			return lowChild == null && highChild == null;
		}
		
		private boolean isLow() {
			return this == parent.lowChild;
		}

		public E getData() {
			return data;
		}
		
		private void setData(E data) {
			this.data = data;
			this.x = data == null ? Double.NaN : data.x();
			this.y = data == null ? Double.NaN : data.y();
		}

		public boolean contains(E value) {
			if (data == null) {
				return false;
			}
			int cmp = d.compare(value, x, y);
			if (cmp == 0 && data.equals(value)) {
				return true;
			}
			if (cmp < 0) {
				return hasLow() ? lowChild.contains(value) : false;
			}
			return hasHigh() ? highChild.contains(value) : false;
		}

		public boolean add(E value) {
			if (data == null) {
				setData(value);
				return true;
			}
			return add(value, value.x(), value.y());
		}
		
		private boolean add(E value, double valX, double valY) {
			int cmp = d.compare(valX, valY, this.x, this.y);
			if (cmp == 0 && data.equals(value)) {
				return false;
			}
			return addToChild(cmp < 0, value, valX, valY);
		}
		
		private boolean addToChild(boolean low, E value, double valX, double valY) {
			KdNode<E> child = getChild(low);
			if (child == null) {
				setChild(low, new KdNode<E>(this, value));
				return true;
			}
			return child.add(value, valX, valY);
		}

		private KdNode<E> getChild(boolean low) {
			return low ? lowChild : highChild;
		}

		private boolean hasChild(boolean low) {
			return getChild(low) != null;
		}
		
		private void setChild(boolean left, KdNode<E> node) {
			if (getChild(left) != node) {
				if (left) {
					lowChild = node;
				} else {
					highChild = node;
				}
				updateSize();
			}
		}

		private void updateSize() {
			int oldSize = size;
			size = 1 + lowSize() + highSize();
			if (oldSize != size && parent != null) {
				parent.updateSize();
			}
		}

		private int lowSize() {
			return hasLow() ? lowChild.size() : 0;
		}

		private int highSize() {
			return hasHigh() ? highChild.size() : 0;
		}
		
		private KdNode<E> findMin(Dimension whichDimension) {
			KdNode<E> cur = this;
			if (hasLow()) {
				cur = whichDimension.min(cur, lowChild.findMin(whichDimension));
			}
			if (hasHigh() && whichDimension != this.d) {
				cur = whichDimension.min(cur, highChild.findMin(whichDimension));
			}
			return cur;
		}

		private <L extends Collection<KdNode<E>>> L findMax(Dimension whichD, L out) {
			KdNode<E> cur = CollectionUtil.firstOrNullIfEmpty(out);
			int cmp = cur == null ? 1 : whichD.compare(this, cur);
			if (cmp >= 0) {
				if (cmp > 0) {
					out.clear();
				}
				out.add(this);
			}
			if (hasHigh()) {
				highChild.findMax(whichD, out);
			}
			if (hasLow() && whichD != this.d) {
				lowChild.findMax(whichD, out);
			}
			return out;
		}

		public boolean remove(E value) {
			if (data == null) {
				return false;
			}
			int cmp = d.compare(value, x, y);
			if (cmp == 0 && data.equals(value)) {
				remove();
				return true;
			}
			return removeFromChild(cmp < 0, value);
		}

		private boolean removeFromChild(boolean low, E value) {
			return hasChild(low) ? getChild(low).remove(value) : false;
		}

		/**
		 * @return true iff this node was leaf and was removed from the tree
		 */
		public boolean remove() {
			if (isLeaf()) {
				removeLeaf();
				return true;
			}
			replaceWithClosestChild();
			return false;
		}

		private void replaceWithClosestChild() {
			if (lowSize() > highSize()) {
				LinkedList<KdNode<E>> children = lowChild.findMax(d, new LinkedList<KdNode<E>>());
				@SuppressWarnings("unchecked")
				List<E> childData = (List<E>)CollectionUtil.mapToList(children, DATA_GETTER);
				for (E child : childData) {
					remove(child);
				}
				setData(childData.get(0));
				for (int i = 1; i < childData.size(); i++) {
					add(childData.get(i));
				}
			} else {
				replaceWithChild(highChild.findMin(d));
			}
		}

		public void replaceWithChild(final KdNode<E> replacement) {
			setData(replacement.data);
			replacement.remove();
		}

		public void removeLeaf() {
			if (parent == null) {
				size = 0;
				setData(null);
				return;
			}
			parent.removeChild(this);
		}

		private void removeChild(KdNode<E> node) {
			if (lowChild == node) {
				lowChild = null;
				
			} else if (highChild == node) {
				highChild = null;
				
			} else {
				throw new IllegalArgumentException("Can't remove node that is not a child "+node+" ("+lowChild+", "+highChild+")");
			}
			updateSize();
		}

		private void setLowChild(KdNode<E> node) {
			setChild(true, node);
		}

		private void setHighChild(KdNode<E> node) {
			setChild(false, node);
		}
		
		
		public KdNode<E> findNearest(double px, double py, double curDistSq, Set<?> excluded) {
			if (data == null) {
				return null;
			}
			int cmp = d.compare(px, py, x, y);
			KdNode<E> nearSideChild = getChild(cmp < 0);
			KdNode<E> curNearest = nearSideChild == null ? null : nearSideChild.findNearest(px, py, curDistSq, excluded);
			if (curNearest != null) {
				//TODO: optimize this: distSq is calculated for each level of the tree
				curDistSq = curNearest.distanceSq(px, py);
			}
			double perpDistSq = perpDistSq(px, py);
			if (perpDistSq >= curDistSq) {
				return curNearest;
			}
			double thisDistSq = perpDistSq + parDistSq(px, py);
			if (thisDistSq < curDistSq && !excluded.contains(data)) {
				curDistSq = thisDistSq;
				curNearest = this;
			}
			KdNode<E> farSideChild = getChild(cmp > 0);
			KdNode<E> farSideNearest = farSideChild == null ? null : farSideChild.findNearest(px, py, curDistSq, excluded);
			return (farSideNearest != null) ? farSideNearest : curNearest;
		}

		private double perpDistSq(double px, double py) {
			return MathUtil.sqr(d.delta(px, py, this.x, this.y));
		}

		private double parDistSq(double px, double py) {
			return MathUtil.sqr(d.delta(py, px, this.y, this.x));
		}
		
		private double distanceSq(double x2, double y2) {
			return GeomUtil.distanceSq(x, y, x2, y2);
		}

		public Envelope lowHalf(Envelope parentEnv) {
			return d.lowHalf(parentEnv, x, y);
		}

		public Envelope highHalf(Envelope parentEnv) {
			return d.highHalf(parentEnv, x, y);
		}

		@Override
		public String toString() {
			return data.x() + " " + data.y() + " ("+d +";"+size+")";
		}

		@Override
		public Iterator<E> iterator() {
			return new KdIterator<E>(this);
		}
	}

	protected static class KdIterator<E extends HasCoordinate> implements Iterator<E> {
		KdNode<E> cur = null;
		KdNode<E> next;
		KdNode<E> stop;

		public KdIterator(KdNode<E> root) {
			next = root;
			stop = root.nextUp();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public E next() {
			cur = next;
			next = cur.next();
			if (next == stop) {
				next = null;
			}
			return cur.data;
		}

		@Override
		public void remove() {
			if (cur == null) {
				throw new IllegalStateException("Cannot remove from iterator before calling next()");
			}
			if (!cur.remove()) {
				next = cur;
			}
			cur = null;
		}
	}

	KdNode<E> root;
	EnvelopeBuilder env = new EnvelopeBuilder();

	public KdTree() {
		clear();
	}

	public KdTree(Collection<? extends E> elements) {
		this();
		setAll(elements);
	}

	@Override
	public boolean isEmpty() {
		return root.data == null;
	}

	@Override
	public int size() {
		return root.size();
	}

	@Override
	public Iterator<E> iterator() {
		if (isEmpty()) {
			return CollectionUtil.emptyIterator();
		}
		return new KdIterator<E>(root);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return root.contains((E)o);
	}

	@Override
	public boolean add(E e) {
		env.expandToInclude(e);
		return root.add(e);
	}
	
	@Override
	public void clear() {
		root = new KdNode<E>(null, null);
		env.clear();
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (c.isEmpty()) {
			return false;
		}
		if (isEmpty()) {
			setAll(c);
			return true;
		}
		if (c.size() > 1) {
			HashSet<E> union = new HashSet<E>(this);
			if (!union.addAll(c)) {
				return false;
			}
			setAll(union);
			return true;
		}
		return super.addAll(c);
	}

	public void setAll(Collection<? extends E> c) {
		root = buildTree(c);
		computeEnvelope();
	}
	
	private void computeEnvelope() {
		env.clear();
		KdNode<E> maxX = CollectionUtil.firstOrNullIfEmpty(root.findMax(Dimension.X, new LinkedList<KdNode<E>>()));
		if (maxX == null) {
			return;
		}
		KdNode<E> maxY = CollectionUtil.firstOrNullIfEmpty(root.findMax(Dimension.Y, new LinkedList<KdNode<E>>()));
		env.setMBR(root.findMin(Dimension.X).x(), root.findMin(Dimension.Y).y(), maxX.x(), maxY.y());
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (root.remove((E)o)) {
			if (env.isPointOnEdge((E)o)) {
				computeEnvelope();
			}
			return true;
		}
		return false;
	}
	
	public E findNearest(HasCoordinate pos) {
		return findNearest(pos, Double.POSITIVE_INFINITY, Collections.<E>emptySet());
	}
	

	@Override
	public E findNearest(HasCoordinate pos, double minDistSq, Set<?> excluded) {
		double x = pos.x();
		double y = pos.y();
		assert (!Double.isNaN(x) && !Double.isNaN(y));
		KdNode<E> ret = root.findNearest(x, y, minDistSq, excluded);
		if (ret != null) {
			return ret.data;
		}
		if (isEmpty() || minDistSq < Double.POSITIVE_INFINITY || excluded.size() == size()) {
			return null;
		}
		throw new RuntimeException("Error in KdTree - there should be a nearest node, but null was returned! Node positions were likely changed after being added to the tree.");
	}
	

	public Set<E> findInEnvelope(Envelope envelope, Set<? super E> excluded) {
		SetCollector<E> ret = new SetCollector<E>();
		SearchItemReceiver<E> sink = ret;
		if (!CollectionUtil.isNullOrEmpty(excluded)) {
			sink = new SearchItemReceiver.Filter<E>(ret, excluded);
		}
		findInEnvelope(envelope, sink);
		return ret;
	}

	@Override
	public boolean findInEnvelope(Envelope envelope, SearchItemReceiver<? super E> collector) {
		if (envelope.isEmpty()) {
			return true;
		}
		KdEnvelopeSearcher<E> searcher = new KdEnvelopeSearcher<E>(this, collector);
		return searcher.findAll(envelope);
	}
	
	private static <E extends HasCoordinate> KdNode<E> buildTree(Collection<? extends E> elements) {
		@SuppressWarnings("unchecked")
		KdNode<E>[] arr = new KdNode[elements.size()];
		int i = 0;
		for (E el : elements) {
			arr[i++] = new KdNode<E>(el);
		}
		return buildTree(arr); 
	}
	
	private static <F extends HasCoordinate> KdNode<F> buildTree(KdNode<F>[] arr) {
		return new KdTreeBuilder<F>(arr).buildTree();
	}

	@Override
	public Envelope getEnvelope() {
		return env.getEnvelope();
	}
}
