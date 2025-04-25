/*
 *
 */
package com.sinergise.common.gis.map.render;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.DimI;


public class RenderInfo {
    public boolean isTransparent=true;
    public boolean quick=false;
    public boolean hasAnything=true;
    public DisplayCoordinateAdapter dca;
    
    public RenderInfo() {
	}
    
    public RenderInfo(DisplayCoordinateAdapter dca, boolean trans, boolean quick) {
    	this.dca=dca;
    	this.quick=quick;
    	this.isTransparent=trans;
	}
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dca == null) ? 0 : dca.hashCode());
		result = prime * result + (hasAnything ? 1231 : 1237);
		result = prime * result + (isTransparent ? 1231 : 1237);
		result = prime * result + (quick ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RenderInfo other = (RenderInfo) obj;
		if (dca == null) {
			if (other.dca != null)
				return false;
		} else if (!dca.equals(other.dca))
			return false;
		if (hasAnything != other.hasAnything)
			return false;
		if (isTransparent != other.isTransparent)
			return false;
		if (quick != other.quick)
			return false;
		return true;
	}

	public int getDisplayWidth() {
		return dca.pixDisplaySize.w();
	}

	public int getDisplayHeight() {
		return dca.pixDisplaySize.h();
	}

	public DimI getDisplaySize() {
		return dca.getDisplaySize();
	}
    
}
