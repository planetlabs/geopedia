package com.sinergise.geopedia.core.service.params;

import java.io.Serializable;

import com.sinergise.common.util.geom.HasCoordinate;

@SuppressWarnings("serial")
public class GetHeight implements Serializable {

	public HasCoordinate point;

	@Deprecated //serialization only
	protected GetHeight() { }
	
	
	public GetHeight(HasCoordinate point){
		this.point = point;
	}
}
