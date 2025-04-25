package com.sinergise.geopedia.core.crs;

import com.sinergise.common.geometry.crs.misc.TzaTransforms;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;

public class CRSSettingsTZA extends CRSSettings {

	public CRSSettingsTZA() {
		addCRS(TzaTransforms.ARC_1960);
		addTransform(TzaTransforms.UTM35S_TO_UTM36S, TzaTransforms.ARC1960_UTM35S, TzaTransforms.ARC1960_UTM36S);
		addTransform(TzaTransforms.UTM37S_TO_UTM36S, TzaTransforms.ARC1960_UTM37S, TzaTransforms.ARC1960_UTM36S);
	}
	
	
	@Override
	public Transform<?, ?> getTransform(CrsIdentifier fromCRSsrid, CrsIdentifier toCRSsrid, boolean swapLatLon) {
		Transform<?,?> t = super.getTransform(fromCRSsrid, toCRSsrid, swapLatLon);
		if (swapLatLon) {
			return swapCoordinates(t);
		}
		return t;
	}

	@Override
	public WithBounds getMainCRS() {
		return (WithBounds) TzaTransforms.TILES_TZA;
	}
}
