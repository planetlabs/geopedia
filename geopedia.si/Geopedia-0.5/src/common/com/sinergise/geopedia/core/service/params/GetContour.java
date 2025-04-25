package com.sinergise.geopedia.core.service.params;

import java.io.Serializable;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;

@SuppressWarnings("serial")
public class GetContour implements Serializable {

	public HasCoordinate 	point;
	public Envelope 		envelope;
	
	public int simplificationTolerance 	= 150; //m2 - pixel size of dmv is 12.5x12.5 m2, so 150m2 is ~ 1 dmv pixel 
	public int itForSimplification 		= 10;
	
	@Deprecated //serialization only
	protected GetContour() {	}

	public GetContour(HasCoordinate point, Envelope envelope) {
		this.point = point;
		this.envelope = envelope;
	}

	public GetContour(HasCoordinate point, Envelope envelope, int simplificationTolerance, int itForSimplification) {
		this.point = point;
		this.envelope = envelope;
		this.simplificationTolerance = simplificationTolerance;
		this.itForSimplification = itForSimplification;
	}

	
}
