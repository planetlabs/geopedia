package com.sinergise.geopedia.core.crs;

import com.sinergise.common.geometry.crs.misc.CzeTransforms;
import com.sinergise.common.geometry.tiles.WithBounds;

public class CRSSettingsCZE extends CRSSettings{

	public CRSSettingsCZE () {
	}
	

	@Override
	public WithBounds getMainCRS() {
		return (WithBounds)CzeTransforms.CZ_TILES_GEOPEDIA;
	}
}
