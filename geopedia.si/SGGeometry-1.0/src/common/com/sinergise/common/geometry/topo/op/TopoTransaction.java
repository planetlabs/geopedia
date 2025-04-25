/**
 * 
 */
package com.sinergise.common.geometry.topo.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.sinergise.common.geometry.topo.TopoMap;
import com.sinergise.common.geometry.topo.TopoValidationException;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.geometry.topo.action.TopoAction;


/**
 * @author tcerovski
 */
public final class TopoTransaction {
	private List<TopoAction> actions = new ArrayList<TopoAction>();
	private Stack<TopoOperation> undoStack = new Stack<TopoOperation>();
	private List<TopoTransactionListener> listeners = new ArrayList<TopoTransactionListener>();
	
	private final TopoMap map;
	private final int validationFlags;
	
	public TopoTransaction(TopoMap map, int validationFlags) {
		this.map = map;
		this.validationFlags = validationFlags;
	}
	
	public void addAction(TopoAction action) {
		if (!undoStack.isEmpty()) {
			throw new IllegalStateException("Transaction already executing.");
		}
		actions.add(action);
	}
	
	public void execute() throws TopologyException, TopoValidationException {
		if (actions.isEmpty()) {
			throw new IllegalStateException("Nothing to execute");
		}
		
		TopologyException error = null;
		
		try {
			for (TopoAction action : actions) {
				action.prepareAction(map);
				for (TopoOperation op : action.getOps()) {
					executeOp(op, map);
				}
			}
			map.validate(validationFlags);
			
		} catch (TopologyException e) {
			error = e;
			throw e;
		} catch (Throwable t) {
			error = new TopologyException(t);
			throw error;
		} finally {
			if(error != null)
				fireTransactionFailed(error);
		}
		fireTransactionPerformed();
	}
	
	private void executeOp(TopoOperation op, TopoUpdater topo) throws Exception {
		op.execute(topo);
		undoStack.push(op.createUndoOperation());
	}
	
	public void rollback() throws TopologyException, TopoValidationException {
		if(undoStack == null || undoStack.isEmpty())
			return;
		
		try {
			while(!undoStack.isEmpty()) {
				undoStack.pop().execute(map);
			}
			map.validate(validationFlags);
		} catch (TopologyException e) {
			throw e;
		} catch (Throwable t) {
			throw new TopologyException(t);
		}
	}
	
	public void addTransactionListener(TopoTransactionListener listener) {
		listeners.add(listener);
	}
	
	public void removeTransactionListener(TopoTransactionListener listener) {
		listeners.remove(listener);
	}
	
	private void fireTransactionPerformed() {
		for(TopoTransactionListener listener : listeners) {
			listener.transactionPeformed(this);
		}
	}
	
	private void fireTransactionFailed(TopologyException error) {
		for(TopoTransactionListener listener : listeners) {
			listener.transactionFailed(this, error);
		}
	}
	
	
}
