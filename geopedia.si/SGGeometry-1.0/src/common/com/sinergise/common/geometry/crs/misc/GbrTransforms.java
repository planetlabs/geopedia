package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;

public class GbrTransforms {
	public static final Ellipsoidal OSGB_1936 = new Ellipsoidal(new CrsIdentifier(EPSG, 4277), Ellipsoid.AIRY_1830);
	public static final TransverseMercator BRITISH_NATIONAL_GRID = new TransverseMercator(OSGB_1936, 49, -2, 0.9996012717, new CrsIdentifier(EPSG, 27700), new Envelope(0,0,700000,1300000)).setOffset(400000, -100000);
	public static final TiledCRS GB_TILES = TiledCRS.createDefault(BRITISH_NATIONAL_GRID, "UK Tiles", -100000, -100000, 512, 0.125, 20, 5);

	public static final TMToGeographic BNG_TO_LATLON = new TMToGeographic(BRITISH_NATIONAL_GRID);
	public static final GeographicToTM LATLON_TO_BNG = BNG_TO_LATLON.inverse();

}
