package com.sinergise.common.web.session;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GetSessionValuesRequest implements Serializable {
	
	private static final long serialVersionUID = 647888237260043727L;
	
	private Set<SessionVariable<?>> variables;

	@Deprecated /** Serialization only */
	protected GetSessionValuesRequest() {}
	
	public GetSessionValuesRequest(SessionVariable<?> ...variables) {
		this(Arrays.asList(variables));
	}
	
	public GetSessionValuesRequest(Collection<SessionVariable<?>> variables) {
		this.variables = new HashSet<SessionVariable<?>>(variables);
	}
	
	public Collection<SessionVariable<?>> getVariables() {
		return Collections.unmodifiableSet(variables);
	}
	
	public boolean isEmpty() {
		return variables == null || variables.isEmpty();
	}
	
}
