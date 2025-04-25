package com.sinergise.geopedia.core.crs;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.misc.GbrTransforms;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.Identity;
import com.sinergise.common.geometry.tiles.WithBounds;

public class CRSSettingsGBR extends CRSSettings{

	public CRSSettingsGBR () {
		addCRS(GbrTransforms.BRITISH_NATIONAL_GRID);
		addCRS(CRS.WGS84);
		addTransform( new Identity<CartesianCRS>(CRS.MAP_PIXEL_CRS), GbrTransforms.BRITISH_NATIONAL_GRID, GbrTransforms.BRITISH_NATIONAL_GRID);
	}
	

	@Override
	public WithBounds getMainCRS() {
		return (WithBounds)GbrTransforms.GB_TILES;
	}
}
