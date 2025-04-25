package com.sinergise.geopedia.core.service.result;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GetHeightResult implements Serializable {

	public Double height;

	@Deprecated //serialization only
	protected GetHeightResult() {	}

	public GetHeightResult(Double height) {
		this.height = height;
	}
	
	
}
