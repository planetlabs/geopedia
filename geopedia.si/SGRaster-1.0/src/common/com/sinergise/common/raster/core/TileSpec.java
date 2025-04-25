package com.sinergise.common.raster.core;

import java.io.Serializable;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.util.geom.PointI;


public class TileSpec implements Serializable {
	private static final long serialVersionUID = 523342422637927577L;
	
	public PointI location;
	public int zoomLevel;
	public TileSpec() {
	}
	public TileSpec(int zoomLevel, int col, int row) {
		this.location=new PointI(col, row);
		this.zoomLevel=zoomLevel;
	}
	
	@Override
	public String toString() {
		return TileUtilGWT.tileLevelCharFromZoomLevel(zoomLevel) + " " + location.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + zoomLevel;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		TileSpec other = (TileSpec) obj;
		if (location == null) {
			if (other.location != null) return false;
		} else if (!location.equals(other.location)) return false;
		if (zoomLevel != other.zoomLevel) return false;
		return true;
	}
	
	
}
