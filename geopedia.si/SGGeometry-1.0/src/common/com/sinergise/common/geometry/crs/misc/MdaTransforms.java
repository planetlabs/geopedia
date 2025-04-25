package com.sinergise.common.geometry.crs.misc;

import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;

public class MdaTransforms {

	public static final Ellipsoidal MOLDREF99 = (Ellipsoidal) new Ellipsoidal(
		new CrsIdentifier(CrsAuthority.EPSG, 4023),
		Ellipsoid.GRS80)
		.setNiceName("MOLDREF99");
	
	public static final TransverseMercator MOLDOVA_TM = (TransverseMercator) new TransverseMercator(
		MOLDREF99,
		0.0,
		28.4,
		0.99994,
		new CrsIdentifier(CrsAuthority.EPSG, 4026),
		null)
		.setOffset(200000, -5000000)
		.setNiceName("MOLDREF99 / Moldova TM");
	
	public static final TiledCRS MOLDOVA_TILES = new WithBounds(
		MOLDOVA_TM, 
		"Moldova Tiles", 
		ScaleLevelsSpec.createWithFactor2(0.125, 20, 7), 
		Envelope.withSize(-76800, -51200, 512*1024, 512*1024),
		new DimI(512, 512));
	
	public static void main(String[] args) {
		
	}
}
