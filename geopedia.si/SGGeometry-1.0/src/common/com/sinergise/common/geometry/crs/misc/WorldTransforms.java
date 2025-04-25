package com.sinergise.common.geometry.crs.misc;


import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.SphericalMercator;
import com.sinergise.common.geometry.crs.SphericalMercator.GeographicToSM;
import com.sinergise.common.geometry.crs.SphericalMercator.SMToGeographic;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledCRS.AxisSign;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;

public class WorldTransforms {

	public static final Envelope WORLD_MBR_LATLON = new Envelope(-85.05112878, -180, 85.05112878, 180);
	public static final Envelope WORLD_MBR_SM = new Envelope(-20037508.342789244, -20037508.342789244, 20037508.342789244, 20037508.342789244);

	public static final SphericalMercator POPULAR_WEB_WGS84 = (SphericalMercator)new SphericalMercator(
		CRS.WGS84, 		//baseCRS
		0, 				//Longitude of natural origin
		0, 				//Latitude of natural origin
		6378137, 		//earth radius
		new CrsIdentifier(CrsAuthority.EPSG, 3857), 
		WORLD_MBR_SM)
	.setOffset(0., 0.)	//false easting and northing
	.setNiceName("Popular Visualisation Pseudo Mercator").registerIdentifier(new CrsIdentifier(CrsAuthority.POSTGIS, 3857));
	
	public static final TiledCRS POP_WEB_WORLD_TILES = new WithBounds(POPULAR_WEB_WGS84, "Global Google Maps Like Tiles 512", //
		ScaleLevelsSpec.createWithFactor2(WORLD_MBR_SM.getWidth()/512, 1, 20, 1), WORLD_MBR_SM, new DimI(512, 512), //
		AxisSign.POSITIVE, AxisSign.NEGATIVE);

	public static final TiledCRS GMAPS_WORLD_TILES = new WithBounds(POPULAR_WEB_WGS84, "Google Maps Tiles", //
		ScaleLevelsSpec.createWithFactor2(WORLD_MBR_SM.getWidth()/256, 0, 20, 0), WORLD_MBR_SM, new DimI(256, 256), //
		AxisSign.POSITIVE, AxisSign.NEGATIVE);

	public static DatumConversion ETRS89_TO_WGS84 = new DatumConversion.Identity(CRS.ETRS89_ELLIPSOIDAL, CRS.WGS84); //0.5m accuracy
	public static Transform<?,?> ETRS89_TO_WEBMERCATOR = Transforms.compose(ETRS89_TO_WGS84, new GeographicToSM(POPULAR_WEB_WGS84));
	public static Transform<?,?> WEBMERCATOR_TO_ETRS89 = Transforms.compose(new SMToGeographic(POPULAR_WEB_WGS84), ETRS89_TO_WGS84.inverse());
	
}
