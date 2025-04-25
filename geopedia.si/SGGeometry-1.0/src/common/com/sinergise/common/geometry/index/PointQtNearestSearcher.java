package com.sinergise.common.geometry.index;

import java.util.Set;

import com.sinergise.common.geometry.index.PointQuadtree.Bulk;
import com.sinergise.common.geometry.index.PointQuadtree.Empty;
import com.sinergise.common.geometry.index.PointQuadtree.Leaf;
import com.sinergise.common.geometry.index.PointQuadtree.Node;
import com.sinergise.common.geometry.index.PointQuadtree.NodeBase;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.math.MathUtil;

public class PointQtNearestSearcher<E extends HasCoordinate> {
	private static final int[][] NEIGBOURS = new int[][]{//
	{1, 2, 3},
	{0, 3, 2},
	{3, 0, 1},
	{2, 1, 0}};

	private final Set<?> excluded;
	private final double x;
	private final double y;
	private Leaf<E> best;
	
	public PointQtNearestSearcher(Set<?> excluded) {
		x = Double.NEGATIVE_INFINITY;
		y = 0;
		this.excluded = excluded;
	}
	
	public PointQtNearestSearcher(HasCoordinate pos, Set<?> excluded) {
		this.excluded = excluded;
		this.x = pos.x();
		this.y = pos.y();
	}

	public E findNearest(Node<E> root, double withinDistSq) {
		getNearest(root, withinDistSq);
		return best == null ? null : best.data;
	}
	private double getNearest(Node<E> root, double maxDistSq) {
		if (root == Empty.EMPTY) {
			return maxDistSq;
		}
		NodeBase<E> n = (NodeBase<E>)root;
		if (n.isLeaf()) {
			return getNearestInLeaf((Leaf<E>)n, maxDistSq);
		}
		return getNearestInBulk((Bulk<E>)n, maxDistSq);
	}

	private double getNearestInBulk(final Bulk<E> n, double maxDistSq) {
		final double dx = x - n.posX;
		final double dy = y - n.posY;
		final int closest = dy < 0 ? dx < 0 ? 0 : 1 : dx < 0 ? 2 : 3;
		final int[] order = NEIGBOURS[closest];
		maxDistSq = getNearest(n.children[closest], maxDistSq);
		
		final double dxSq = dx * dx;
		if (dxSq < maxDistSq) {
			maxDistSq = getNearest(n.children[order[0]], maxDistSq);
		}
		final double dySq = dy * dy;
		if (dySq < maxDistSq) {
			maxDistSq = getNearest(n.children[order[1]], maxDistSq);
			if (dxSq + dySq < maxDistSq) {
				return getNearest(n.children[order[2]], maxDistSq);
			}
		}
		return maxDistSq;
	}

	private double getNearestInLeaf(Leaf<E> n, double maxDistSq) {
		double distSq = MathUtil.sqr(x - n.posX);
		if (distSq > maxDistSq) {
			return maxDistSq;
		}
		distSq += MathUtil.sqr(y - n.posY);
		if (distSq > maxDistSq) {
			return maxDistSq;
		}
		return reportLeaf(n, maxDistSq, distSq);
	}

	public double reportLeaf(Leaf<E> n, double prevBestVal, double leafRetVal) {
		while (excluded.contains(n.data)) {
			n = n.next;
			if (n == null) {
				return prevBestVal;
			}
		}
		best = n;
		return leafRetVal;
	}
	
	public E findLeftmost(Node<E> root) {
		getLeftmost(root, Double.POSITIVE_INFINITY);
		return best == null ? null : best.data;
		
	}

	private double getLeftmost(Node<E> root, double curMinX) {
		if (root == Empty.EMPTY) {
			return curMinX;
		}
		NodeBase<E> n = (NodeBase<E>)root;
		if (n.isLeaf()) {
			return getLeftmostInLeaf((Leaf<E>)n, curMinX);
		}
		return getLeftmostInBulk((Bulk<E>)n, curMinX);

	}

	private double getLeftmostInLeaf(Leaf<E> n, double curMinX) {
		if (n.posX >= curMinX) {
			return curMinX;
		} 
		return reportLeaf(n, curMinX, n.posX);
	}

	private double getLeftmostInBulk(Bulk<E> n, double curMinX) {
		if (n.posX - n.childSize > curMinX) {
			return curMinX;
		}
		curMinX = getLeftmost(n.children[0], curMinX);
		curMinX = getLeftmost(n.children[2], curMinX);
		if (curMinX > n.posX) {
			curMinX = getLeftmost(n.children[1], curMinX);
			curMinX = getLeftmost(n.children[3], curMinX);
		}
		return curMinX;
	}

}
