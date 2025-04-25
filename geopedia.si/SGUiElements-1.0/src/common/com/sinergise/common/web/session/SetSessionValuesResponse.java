package com.sinergise.common.web.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetSessionValuesResponse implements Serializable {

	private static final long serialVersionUID = -5062097137113928055L;
	
	private List<SessionVariable<?>> variablesSet;
	
	@Deprecated /**Serialization only*/
	protected SetSessionValuesResponse() {}
	
	public SetSessionValuesResponse(List<SessionVariable<?>> variablesSet) {
		this.variablesSet = new ArrayList<SessionVariable<?>>(variablesSet) ;
	}
	
	public List<SessionVariable<?>> getVariablesSet() {
		return Collections.unmodifiableList(variablesSet);
	}
	
	public boolean isEmpty() {
		return variablesSet == null || variablesSet.isEmpty();
	}
	
}
