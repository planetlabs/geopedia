package com.sinergise.geopedia.core.crs;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.misc.SenegalTransforms;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.Identity;
import com.sinergise.common.geometry.tiles.WithBounds;

public class CRSSettingsSEN extends CRSSettings{

	public CRSSettingsSEN () {
		addCRS(SenegalTransforms.WGS84_UTM28N);
		addCRS(CRS.WGS84);
		addTransform( new Identity<CartesianCRS>(CRS.MAP_PIXEL_CRS), SenegalTransforms.WGS84_UTM28N, SenegalTransforms.WGS84_UTM28N);
		addTransform(SenegalTransforms.FROM_WGS_UTM28N_TO_WGS84, SenegalTransforms.WGS84_UTM28N,CRS.WGS84);
		addTransform(SenegalTransforms.FROM_WGS84_TO_WGS_UTM28N, CRS.WGS84, SenegalTransforms.WGS84_UTM28N);
	}
	

	@Override
	public WithBounds getMainCRS() {
		return (WithBounds)SenegalTransforms.SEN_TILES;
	}
}
