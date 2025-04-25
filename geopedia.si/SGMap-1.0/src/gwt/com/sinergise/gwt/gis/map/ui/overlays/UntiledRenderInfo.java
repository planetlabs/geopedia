package com.sinergise.gwt.gis.map.ui.overlays;

import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;

public class UntiledRenderInfo extends RenderInfo {
	public Envelope worldBounds;
	public Object imageParam;
	public DimI imageNaturalSize;
	public transient int nTries = 0;

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof UntiledRenderInfo))
			return false;

		UntiledRenderInfo ri = (UntiledRenderInfo) obj;

		if (worldBounds != null) {
			if (!worldBounds.equals(ri.worldBounds))
				return false;
		} else if (ri.worldBounds != null)
			return false;

		if (imageNaturalSize != null) {
			if (!imageNaturalSize.equals(ri.imageNaturalSize))
				return false;
		} else if (ri.imageNaturalSize != null)
			return false;

		if (!themeEquals(ri))
			return false;
		return true;
	}

	public boolean themeEquals(UntiledRenderInfo otherInfo) {
		return Util.safeEquals(imageParam, otherInfo.imageParam);
	}

	public UntiledRenderInfo createCopy() {
		UntiledRenderInfo ret = new UntiledRenderInfo();
		ret.dca = dca;
		ret.hasAnything = hasAnything;
		ret.imageNaturalSize = imageNaturalSize;
		ret.imageParam = imageParam;
		ret.isTransparent = isTransparent;
		ret.quick = quick;
		ret.worldBounds = worldBounds;
		return ret;
	}
}