package com.sinergise.common.geometry.index;

import static com.sinergise.common.geometry.index.PointQuadtree.Empty.EMPTY;

import java.util.Iterator;

import com.sinergise.common.geometry.index.PointQuadtree.Bulk;
import com.sinergise.common.geometry.index.PointQuadtree.Leaf;
import com.sinergise.common.geometry.index.PointQuadtree.Node;
import com.sinergise.common.geometry.index.PointQuadtree.NodeBase;
import com.sinergise.common.util.geom.HasCoordinate;

public class PointQtIterator<N extends HasCoordinate> implements Iterator<N> {
	public static class QuadtreePath<N extends HasCoordinate> {
		private final QuadtreePath<N> parent;
		private final NodeBase<N> node;
		private final int nodeIdx;
	
		public QuadtreePath(QuadtreePath<N> parent, NodeBase<N> node, int nodeIdx) {
			this.parent = parent;
			this.node = node;
			this.nodeIdx = nodeIdx;
		}
	
		public boolean isLeaf() {
			return node.isLeaf();
		}
	
		public QuadtreePath<N> nextLeaf() {
			assert isLeaf();
			Leaf<N> nextLinked = ((Leaf<N>)node).next;
			if (nextLinked != null) {
				return new QuadtreePath<N>(parent, nextLinked, nodeIdx);
			}
			return nextLeafUp();
		}
	
		QuadtreePath<N> firstLeaf(int fromIdx) {
			if (isLeaf()) {
				return this;
			}
			Bulk<N> qnode = (Bulk<N>)node;
			for (int i = fromIdx; i < 4; i++) {
				Node<N> child = qnode.children[i];
				if (child != EMPTY) {
					return new QuadtreePath<N>(this, (NodeBase<N>)child, i).firstLeaf(0);
				}
			}
			return nextLeafUp();
		}
	
		public QuadtreePath<N> nextLeafUp() {
			if (parent == null) {
				return null;
			}
			return parent.firstLeaf(nodeIdx + 1);
		}
	
		public N getValue() {
			assert isLeaf();
			return ((Leaf<N>)node).data;
		}
	}

	PointQtIterator.QuadtreePath<N> next;

	public PointQtIterator(PointQuadtree.NodeBase<N> root) {
		next = new PointQtIterator.QuadtreePath<N>(null, root, -1).firstLeaf(0);
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public N next() {
		N ret = next.getValue();
		next = next.nextLeaf();
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}