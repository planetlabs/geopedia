package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.POSTGIS;
import static com.sinergise.common.util.math.MathUtil.SEC_IN_RAD;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.Krovak;
import com.sinergise.common.geometry.crs.KrovakNorthOrientated;
import com.sinergise.common.geometry.crs.KrovakNorthOrientated.GeographicToKrovakNorthOrientated;
import com.sinergise.common.geometry.crs.KrovakNorthOrientated.KrovakNorthOrientatedToGeographic;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;

public class CzeTransforms {
	private static final int MAX_WORLD_TILE_SIZE_125 = 1024*512;
	
	public static final Envelope CZ_MBR_EPSG5514 = new Envelope(-910000, -1230000.0, -420000, -930000);
	
	// otherwise WMS doesn't work
	private static Envelope CZE_BOUNDS = new Envelope(-925000.0, -1250000.0, -86139.2, -411139.2);
	
	//http://epsg.io/5514-1623
	public static final Ellipsoidal S_JTSK = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 6422), Ellipsoid.BESSEL_1841, new Envelope(12.09, 48.58, 18.85, 51.05 )).setNiceName("S_JTSK");
	public static final KrovakNorthOrientated S_JTSK_KROVAKNO = (KrovakNorthOrientated)new KrovakNorthOrientated(
		S_JTSK, 
		49.5, //latitude of center 
		24.83333333333333, //longitude of center 
		30.28813972222222, //azimuth, co-latitude of cone axis 
	    78.5, //pseudo standard parallel
	    0.9999, //scale factor - double kp
	    new CrsIdentifier(EPSG, 5514), 
		new Envelope(CZE_BOUNDS)).setOffset(0, 0)
	.setNiceName("S-JTSK / Krovak East North").registerIdentifier(new CrsIdentifier(POSTGIS, 5514));
	
	public static final CartesianCRS EPSG5514CZE = S_JTSK_KROVAKNO;
	
	public static final KrovakNorthOrientatedToGeographic KROVAKNO_TO_GEOGRAPHIC = new KrovakNorthOrientatedToGeographic(S_JTSK_KROVAKNO);
	public static final GeographicToKrovakNorthOrientated GEOGRAPHIC_TO_KROVAKNO = KROVAKNO_TO_GEOGRAPHIC.inverse();
	
	// EPSG::1622  
	// http://grass.fsv.cvut.cz/gwiki/S-JTSK
	// http://crs.bkg.bund.de/crseu/crs/eu-description.php?crs_id=dENaX1MtSlRTSyslMkYrS1JPVkFL
	/*
	 * 7 Parameter Helmert Transformation,
 		|X|   |X|    |Tx|   |  0  -Rz   Ry |   |X|       |X|
 		|Y| = |Y| +  |Ty| + | Rz   0   -Rx | * |Y| + D * |Y|
 		|Z|   |Z|    |Tz|   |-Ry   Rx    0 |   |Z|       |Z|
    	   T     S                                S         S
		
		T          ... Target Datum 
		S          ... Source Datum 
		Tx, Ty, Tz ... geocentric X/Y/Z translations  [m]
		Rx Ry, Rz  ... rotations around X/Y/Z axis    [radian]
		D          ... correction of scale            [ppm]
		
		Tx = 570.8 		Ty = 85.7 		Tz = 462.8
		Rx = +4.998"	Ry = +1.587"	Rz = +5.261"
		D = +3.56 ppm 
	 */
	public static final DatumConversion S_JTSK_TO_ETRS89 = new DatumConversion.PositionVector7Params(
		S_JTSK, //src 
		CRS.ETRS89_ELLIPSOIDAL, //target 
		new double[]{570.8, 85.7, 462.8}, //translation
		new double[]{4.998 * SEC_IN_RAD, 1.587 * SEC_IN_RAD, 5.261 * SEC_IN_RAD}, //rotation
		3.56 //scale
		);
	
	public static final DatumConversion ETRS89_TO_S_JTSK = S_JTSK_TO_ETRS89.inverse();
	
	public static final ToLatLon<Ellipsoidal, Ellipsoidal> S_JTSK_TO_WGS84 = Transforms.compose(S_JTSK_TO_ETRS89, new DatumConversion.Identity(CRS.ETRS89_ELLIPSOIDAL, CRS.WGS84_ELLIPSOIDAL));
	public static final ToLatLon<Ellipsoidal, Ellipsoidal> WGS84_TO_S_JTSK = Transforms.compose(new DatumConversion.Identity(CRS.WGS84_ELLIPSOIDAL, CRS.ETRS89_ELLIPSOIDAL), ETRS89_TO_S_JTSK);

	public static final ToLatLon<Krovak, Ellipsoidal> S_JTSK_KROVAKNO_TO_WGS84 = Transforms.compose(KROVAKNO_TO_GEOGRAPHIC, S_JTSK_TO_WGS84);
	public static final ToCartesian<Ellipsoidal, Krovak> WGS84_TO_S_JTSK_KROVAKNO = Transforms.compose(WGS84_TO_S_JTSK, GEOGRAPHIC_TO_KROVAKNO);
	
	public static final TiledCRS CZ_TILES125 = new WithBounds(
		EPSG5514CZE, 
		"CZ Tiles 0.125", 
		ScaleLevelsSpec.createWithFactor2(0.125, 20, 7),
		Envelope.withSize(-925000, -1250000, MAX_WORLD_TILE_SIZE_125, MAX_WORLD_TILE_SIZE_125),
		new DimI(512, 512)
	);
	
	public static final TiledCRS CZ_TILES = new WithBounds(
		EPSG5514CZE, 
		"CZ Tiles 0.2", 
		ScaleLevelsSpec.createWithFactor2(0.2, 20, 7),
		CZE_BOUNDS,
		new DimI(512, 512)
	);
	
	public static final TiledCRS CZ_TILES_GEOPEDIA = new WithBounds(
		EPSG5514CZE, 
		"CZ Tiles 0.05", 
		ScaleLevelsSpec.createWithFactor2(0.05, 22, 7),
		CZE_BOUNDS,
		new DimI(512, 512)
	);
	
	public static void init() {
		CrsRepository.INSTANCE.add(EPSG5514CZE);
		CrsRepository.INSTANCE.add(S_JTSK_KROVAKNO);
		CrsRepository.INSTANCE.add(S_JTSK);
	}

}
