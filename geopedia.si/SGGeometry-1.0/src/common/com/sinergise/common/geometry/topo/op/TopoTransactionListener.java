/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import com.sinergise.common.geometry.topo.TopologyException;


/**
 * @author tcerovski
 */
public interface TopoTransactionListener {

	public void transactionPeformed(TopoTransaction tx);
	
	public void transactionFailed(TopoTransaction tx, TopologyException error);
	
}
