package com.sinergise.common.geometry.index;

import com.sinergise.common.geometry.index.KdTree.KdNode;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.lang.Function;

public class KdEnvelopeSearcher<E extends HasCoordinate> {
	private Function<? super E, Boolean> collector;
	private KdNode<E> rootNode;
	private Envelope rootEnv;
	
	public KdEnvelopeSearcher(KdTree<E> tree, Function<? super E, Boolean> collector) {
		rootNode = tree.root;
		rootEnv = tree.env.getEnvelope();
		this.collector = collector;
	}
	
	/**
	 * @param envelope
	 * @return true iff any elements were found
	 */
	public boolean findAll(Envelope envelope) {
		return findAll(rootNode, rootEnv, envelope);
	}

	private boolean findAll(KdNode<E> node, Envelope nodeEnv, Envelope envelope) {
		if (!envelope.intersects(nodeEnv)) {
			return true;
		}
		if (envelope.contains(nodeEnv)) {
			return reportAll(node);
		}
		if (node.lowChild != null) {
			if (!findAll(node.lowChild, node.lowHalf(nodeEnv), envelope)) {
				return false;
			}
		}
		if (node.highChild != null) {
			if (!findAll(node.highChild, node.highHalf(nodeEnv), envelope)) {
				return false;
			}
		}
			
		if (envelope.contains(node.x, node.y)) {
			return reportData(node.data);
		}
		return true;
	}
	
	/**
	 * @return TRUE to continue searching
	 */
	private boolean reportAll(KdNode<E> root) {
		for (E e : root) {
			if (!reportData(e)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return TRUE to continue searching
	 */
	public boolean reportData(E data) {
		return collector.execute(data).booleanValue();
	}
}
