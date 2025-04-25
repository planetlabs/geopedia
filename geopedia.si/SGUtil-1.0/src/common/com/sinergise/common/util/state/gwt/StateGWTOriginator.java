/*
 *
 */
package com.sinergise.common.util.state.gwt;

import java.io.Serializable;

public interface StateGWTOriginator extends Serializable {
	/**
	 * @param target state which can optionally be used to return values (to prevent having to create a new state each time)
	 * @return
	 */
	public StateGWT storeInternalState(StateGWT target);
	
	public void loadInternalState(StateGWT st);
}
