package com.sinergise.geopedia.core.crs;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.Identity;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.IdentityLatLon;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;

public class CRSSettingsSVN extends CRSSettings{

	public CRSSettingsSVN () {
		addCRS(CRS.D48_GK);
		addCRS(CRS.WGS84);
		
		addTransform( Transforms.D48_TO_WGS84,  CRS.D48_GK, CRS.WGS84);
		addTransform( Transforms.WGS84_TO_D48, CRS.WGS84,  CRS.D48_GK);
		addTransform( new Identity<CartesianCRS>(CRS.MAP_PIXEL_CRS), CRS.D48_GK, CRS.D48_GK);
		addTransform( new IdentityLatLon<LatLonCRS>(CRS.WGS84), CRS.WGS84, CRS.WGS84);
		
	}
	
	
	@Override
	public Transform<?, ?> getTransform(CrsIdentifier fromCRSsrid, CrsIdentifier toCRSsrid, boolean swapLatLon) {
		Transform<?,?> t = super.getTransform(fromCRSsrid, toCRSsrid, swapLatLon);
		if (swapLatLon) {
			return swapCoordinates(t);
		}
		return t;
	}
	
	public static void main(String[] args) {
		CRSSettingsSVN svnTest = new CRSSettingsSVN();
		for(CRS crs: svnTest.getToCRSTransformCapabilities(CRS.D48_GK.getDefaultIdentifier())) {
			System.out.println(crs.toString());
		}
	}


	@Override
	public WithBounds getMainCRS() {
		return (WithBounds)TiledCRS.GP_SLO;
	}
}
