package com.sinergise.geopedia.core.service.result;

import java.io.Serializable;

public class GetDMVResult implements Serializable {
	private static final long serialVersionUID = 4722109060983471283L;
	
	public double step;
	public double txPxSize;//size of one px in meters (related to step)
	
	//the actual dmv
	public short[][] data; // real height = data * 0.1
	
	public double wTxMinX;
	public double wTxMinY;
	public double wTxMaxX;
	public double wTxMaxY;
	
	public double wDmrMinX;
	public double wDmrMinY;
	public double wDmrMaxX;
	public double wDmrMaxY;
	
	public short maxZ;
	public short minZ;
	
	public GetDMVResult() {
	}

	@Override
	public String toString() {
		return "DEMResult [step=" + step 
				+ ", data.length=" + data.length + ", txPxSize=" + txPxSize
				+ ", wTxMinX=" + wTxMinX + ", wTxMinY=" + wTxMinY
				+ ", wTxMaxX=" + wTxMaxX + ", wTxMaxY=" + wTxMaxY
				+ ", wDmrMinX=" + wDmrMinX + ", wDmrMinY=" + wDmrMinY
				+ ", wDmrMaxX=" + wDmrMaxX + ", wDmrMaxY=" + wDmrMaxY  
				+ ", minZ= "+ minZ+", maxZ="+maxZ+"]";
	}
	
	
}
