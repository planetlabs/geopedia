package com.sinergise.common.geometry.index;

import java.util.Set;

import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;

public interface PointSpatialIndex<E extends HasCoordinate> extends Set<E> {

	Envelope getEnvelope();

	/**
	 * @param queryEnvelope
	 * @param sink
	 * @return true if all have been reported to the sink (i.e. sink didn't return false on any result) 
	 */
	boolean findInEnvelope(Envelope queryEnvelope, SearchItemReceiver<? super E> sink);

	//TODO: redo to work with SearchItemReceiver ?
	E findNearest(HasCoordinate position, double withinDistSq, Set<?> excluded);

}
