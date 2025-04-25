package com.sinergise.common.raster.dataraster.service.average;

import java.io.Serializable;

public class GetDemAverageResponse implements Serializable {
	
	private static final long serialVersionUID = -1316239478474708987L;
	
	private DemDataAverage dataAvg;
	
	@Deprecated /** Serialization only */
	protected GetDemAverageResponse() {}
	
	public GetDemAverageResponse(DemDataAverage dataAvg) {
		this.dataAvg = dataAvg;
	}
	
	public DemDataAverage getAverage() {
		return dataAvg;
	}

	public static GetDemAverageResponse createEmpty() {
		return new GetDemAverageResponse(DemDataAverage.createEmpty());
	}
	
}
