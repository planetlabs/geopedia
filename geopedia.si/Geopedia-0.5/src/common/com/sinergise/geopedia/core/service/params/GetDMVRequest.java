package com.sinergise.geopedia.core.service.params;

import java.io.Serializable;

public class GetDMVRequest implements Serializable {
	private static final long serialVersionUID = 7179613578828865672L;

	public double wCenterX, wCenterY;
	public short scale;
	//size of texture to be used on the given dmv
	//from the size in pixels and the scale we can calculate the minx, miny, maxx and maxy
	public int txSize;

	public GetDMVRequest() {
	}

	@Override
	public String toString() {
		return "DEMRequest [wCenterX=" + wCenterX + ", wCenterY=" + wCenterY
				+ ", scale=" + scale + ", txSize="+txSize+"]";
	}
	
}
