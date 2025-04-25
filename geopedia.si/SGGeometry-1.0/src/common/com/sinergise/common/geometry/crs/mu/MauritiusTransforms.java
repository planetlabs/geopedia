package com.sinergise.common.geometry.crs.mu;

import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;
import static com.sinergise.common.geometry.util.CoordUtil.degFromDms;
import static com.sinergise.common.geometry.util.CoordUtil.GeogDirection.E;
import static com.sinergise.common.geometry.util.CoordUtil.GeogDirection.S;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;
import static com.sinergise.common.util.math.MathUtil.SEC_IN_RAD;

import com.sinergise.common.geometry.crs.CartesianCRS.PseudoPlateCarree;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.LambertConicConformal;
import com.sinergise.common.geometry.crs.LambertConicConformal.GeographicToLCC;
import com.sinergise.common.geometry.crs.LambertConicConformal.LCCToGeographic;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledCRSMapping;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;


public class MauritiusTransforms {
	public static final Ellipsoidal GEO_LE_POUCE_1934 = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 4699), Ellipsoid.CLARKE_1880_RGS).setNiceName("Le Pouce 1934");
	
	public static final Ellipsoidal GEO_MAURITIUS_1994 =
		(Ellipsoidal)new Ellipsoidal(new CrsIdentifier("CRS:MUSWGS84"),Ellipsoid.WGS84)
		.setNiceName("Geodetic Datum of Mauritius 1994").setShortName("GDM 1994");
	
	public static final Ellipsoidal GEO_GDM_2008 = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier("CRS:GDM2008"), Ellipsoid.WGS84)
		.setNiceName("Geodetic Datum of Mauritius 2008").setShortName("GDM 2008");

	public static final LambertConicConformal MUSGRID = 
		new LambertConicConformal(GEO_LE_POUCE_1934, degFromDms(20, 11, 42.25, S), degFromDms(57, 31, 18.58, E), 1, new CrsIdentifier(EPSG, 3337), new Envelope(0, 0, 2e6, 2e6));
	static {
		MUSGRID.setFalseOrigin(1000000, 1000000).setNiceName("Mauritius Grid");
	}

	public static final UTM MGM2008 = new UTM(GEO_GDM_2008, new CrsIdentifier(EPSG, 32740), UTM.centralLongForZone(40), new Envelope(530000, 7729000, 588000, 7797000), false);
	static {
		MGM2008.setNiceName("Map Grid of Mauritius 2008").setShortName("MGM 2008");
	}

	public static final LambertConicConformal LGM2011 = 
		new LambertConicConformal(GEO_GDM_2008, 
			degFromDms(20, 16, 31.85868, S), degFromDms(57, 33, 52.40384, E),
			degFromDms(20, 3, 40, S), degFromDms(20, 26, 40, S), 
			new CrsIdentifier("3337011"), new Envelope(220000, 310000, 280000, 390000));
	static {
		LGM2011.setFalseOrigin(250000, 350000).setNiceName("Local Grid Mauritius 2011").setShortName("LGM 2011");
	}

	/**
	 * 12-parameter affine transform (non-conformal)
	 */

	//		public static final DatumConversion AF_DC_GDM2008_TO_LEPOUCE1934 = new DatumConversion.GeneralMatrix(GEO_GDM_2008, GEO_LE_POUCE_1934,
	//			new double[][]{
	//				{0.999296863241401, -0.000975550074858865, 0.0003693724360850781, 8754.4815887311}, 
	//				{-0.000845523460261062, 0.998853807746347, 0.0004764280142132059, 9382.02633269157},
	//				{0.0003579510427534156, 0.0003804950201792608, 0.99981522462028, -2974.92662582486}
	//			},
	//			// Put inverse here, so that the constant definition is standard
	//			new DatumConversion.GeneralMatrix(GEO_LE_POUCE_1934, GEO_GDM_2008,
	//						new double[][]{
	//				{1.0007044446861817, 0.0009772599936931322, -0.00037006564682918807, -8769.02372510197},
	//				{0.0008469512834776222, 1.00114801192354, -0.00047716237588991197, -9397.623478423504},
	//				{-0.0003584866412022082, -0.0003811808296703655, 1.0001850509745103, 2980.8301314089863}})
	//		);

	/**
	 * 7-parameter Position-Vector rotation, translation & scale
	 */
	// Landgate - inverse is specified
	public static final DatumConversion PV_DC_GDM2008_TO_LEPOUCE1934 = new DatumConversion.PositionVector7Params(
		GEO_LE_POUCE_1934, GEO_GDM_2008, new double[]{-146.610, -228.913, -139.243}, new double[]{
			-1.0828 * SEC_IN_RAD, 18.6823 * SEC_IN_RAD, 19.5543 * SEC_IN_RAD}, 21.28428).inverse();


	// MY 2008->1934 with Z
	//		public static final DatumConversion PV_DC_GDM2008_TO_LEPOUCE1934 = new DatumConversion.PositionVector7Params(GEO_GDM_2008, GEO_LE_POUCE_1934,
	//	  	new double[]{-195.8950,432.4090,107.2960},
	//	  	new double[]{-0.3994*SEC_IN_RAD,-23.0682*SEC_IN_RAD,-31.6275*SEC_IN_RAD}, -20.8073);
	//  
	//	dS -> -20.8073, Rx -> -0.3994, Ry -> -23.0682, Rz -> -31.6275, dX -> -195.8950, dY -> 432.4090, dZ -> 107.2960
	//
	// MY 2008->1934 with Z
	//	dS -> -20.8458, Rx -> 1.0013, Ry -> -18.7812, Rz -> -19.5068, dX -> 145.2750, dY -> 226.7510, dZ -> 140.6290
	//
	// MY 1934->2008 with Z: 
	//   dS -> 20.7979, Rx -> 0.3982, Ry -> 23.0682, Rz -> 31.6264, dX -> 195.8430, dY -> -432.4640, dZ -> -107.1970
	//
	// MY 1934->2008 no Z:
	//	 dS -> 20.8409, Rx -> -1.0022, Ry -> 18.7810, Rz -> 19.5059, dX -> -145.3330, dY -> -226.7860, dZ -> -140.5510

	/**
	 * 3-parameter translation
	 */
	// ICZM - WGS84
	//		public static final DatumConversion TR_DC_GDM2008_TO_LEPOUCE1934 = new DatumConversion.GeocentricTranslation(GEO_GDM_2008, GEO_LE_POUCE_1934,
	//			new double[]{770.126, -158.383, 498.232});

	// MY
	//		public static final DatumConversion TR_DC_GDM2008_TO_LEPOUCE1934 = new DatumConversion.GeocentricTranslation(GEO_GDM_2008, GEO_LE_POUCE_1934,
	//			new double[]{757.61, -169.05, 502.07});

	// Landgate translation
	//		public static final DatumConversion TR_DC_GDM2008_TO_LEPOUCE1934 = new DatumConversion.GeocentricTranslation(GEO_GDM_2008, GEO_LE_POUCE_1934,
	//			new double[]{756.089, -171.409, 503.098});

	/**
	 * Identity datum conversion (i.e. no conversion)
	 */
	//		public static final DatumConversion ID_DC_GDM2008_TO_LEPOUCE1934 = new DatumConversion.Identity(GEO_GDM_2008, GEO_LE_POUCE_1934);

	public static final DatumConversion DC_GDM2008_TO_LEPOUCE1934 = PV_DC_GDM2008_TO_LEPOUCE1934;

	public static final DatumConversion DC_LEPOUCE1934_TO_GDM2008 = DC_GDM2008_TO_LEPOUCE1934.inverse();

	public static final LCCToGeographic MUSGRID_TO_LEPOUCE1934 = new LCCToGeographic(MUSGRID);
	public static final GeographicToLCC LEPOUCE1934_TO_MUSGRID = new GeographicToLCC(MUSGRID);

	public static final TMToGeographic MGM2008_TO_GDM2008 = new TMToGeographic(MGM2008);
	public static final GeographicToTM GDM2008_TO_MGM2008 = new GeographicToTM(MGM2008);

	public static final ToLatLon<LambertConicConformal, Ellipsoidal> MUSGRID_TO_GDM2008 = Transforms.compose(
		MUSGRID_TO_LEPOUCE1934, DC_LEPOUCE1934_TO_GDM2008);
	public static final ToCartesian<Ellipsoidal, LambertConicConformal> GDM2008_TO_MUSGRID = Transforms.compose(
		DC_GDM2008_TO_LEPOUCE1934, LEPOUCE1934_TO_MUSGRID);
	public static final ToLatLon<TransverseMercator, Ellipsoidal> MGM2008_TO_LEPOUCE1934 = Transforms.compose(
		MGM2008_TO_GDM2008, DC_GDM2008_TO_LEPOUCE1934);
	public static final ToCartesian<Ellipsoidal, TransverseMercator> LEPOUCE1934_TO_MGM2008 = Transforms.compose(
		DC_LEPOUCE1934_TO_GDM2008, GDM2008_TO_MGM2008);

	public static final ToCartesian<LambertConicConformal, TransverseMercator> MUSGRID_TO_MGM2008 = Transforms.compose(
		MUSGRID_TO_GDM2008, GDM2008_TO_MGM2008);
	public static final ToCartesian<TransverseMercator, LambertConicConformal> MGM2008_TO_MUSGRID = Transforms.compose(
		MGM2008_TO_GDM2008, GDM2008_TO_MUSGRID);

	//		public static final ToCartesian<LambertConicConformal, TransverseMercator> _PV_MUSGRID_TO_MGM2008 = Transforms.compose(MUSGRID_TO_LEPOUCE1934, Transforms.compose(PV_DC_GDM2008_TO_LEPOUCE1934.inverse(), GDM2008_TO_MGM2008));
	//		public static final ToCartesian<TransverseMercator, LambertConicConformal> _PV_MGM2008_TO_MUSGRID = Transforms.compose(MGM2008_TO_GDM2008, Transforms.compose(PV_DC_GDM2008_TO_LEPOUCE1934, LEPOUCE1934_TO_MUSGRID));
	//		public static final ToCartesian<LambertConicConformal, TransverseMercator> _TR_MUSGRID_TO_MGM2008 = Transforms.compose(MUSGRID_TO_LEPOUCE1934, Transforms.compose(TR_DC_GDM2008_TO_LEPOUCE1934.inverse(), GDM2008_TO_MGM2008));
	//		public static final ToCartesian<TransverseMercator, LambertConicConformal> _TR_MGM2008_TO_MUSGRID = Transforms.compose(MGM2008_TO_GDM2008, Transforms.compose(TR_DC_GDM2008_TO_LEPOUCE1934, LEPOUCE1934_TO_MUSGRID));
	//		public static final ToCartesian<LambertConicConformal, TransverseMercator> _AF_MUSGRID_TO_MGM2008 = Transforms.compose(MUSGRID_TO_LEPOUCE1934, Transforms.compose(AF_DC_GDM2008_TO_LEPOUCE1934.inverse(), GDM2008_TO_MGM2008));
	//		public static final ToCartesian<TransverseMercator, LambertConicConformal> _AF_MGM2008_TO_MUSGRID = Transforms.compose(MGM2008_TO_GDM2008, Transforms.compose(AF_DC_GDM2008_TO_LEPOUCE1934, LEPOUCE1934_TO_MUSGRID));


	public static final ToCartesian<PseudoPlateCarree<Ellipsoidal>, TransverseMercator> PPC_TO_MUSUTM = Transforms.compose(
		Transforms.PSEUDO_PLATE_CARREE_TO_WGS84, GDM2008_TO_MGM2008);

	public static TiledCRS TILES_MUS_SAMPLE = new WithBounds(MGM2008, "MUS UTM Tiles",
		ScaleLevelsSpec.createWithFactor2(0.15, 20, 8),
		// NEW DELIVERY 2009-05-04
		// Centered on {550000.5, 7768999.5}, sizeof 0.15 * 2^12 * 256 = 157286.4
		new Envelope(471357.3, 7690356.3, 628643.7, 7847642.7),

		// Centered on {550000, 7769000}, sizeof 0.15 * 2^12 * 256 = 157286.4
		// new Envelope(471356.8, 7690356.8, 628643.2, 7847643.2),
		new DimI(256, 256), POSITIVE, POSITIVE).setTilePrefixChar('M').setInterlacedName(true);
	static {
		TiledCRSMapping.INSTANCE.put(TILES_MUS_SAMPLE);
	}

	public static TiledCRS TILES_SAT_2 = new WithBounds(MGM2008, "MUS SAT Tiles", ScaleLevelsSpec.createWithFactor2(2,
		16, 8),
	// Top left on {530606.224, 7796389.536}, sizeof 512 * 256 = 131072
		new Envelope(530606.224, 7665317.536, 661678.224, 7796389.536), new DimI(256, 256), POSITIVE, POSITIVE).setTilePrefixChar(
		'X')
		.setInterlacedName(true);

	public static TiledCRS TILES_DMR_10 = new WithBounds(MGM2008, "MUS DMR Tiles", ScaleLevelsSpec.createWithFactor2(
		10, 14, 10),
	// Bottom left on {517495, 7721995}, sizeof 16 * 512 = 81920
		new Envelope(517495, 7721995, 599415, 7803915), new DimI(512, 512), POSITIVE, POSITIVE).setTilePrefixChar('X')
		.setInterlacedName(true)
		.setOverlap(1, 1, 1, 1);

	public static void initialize() {
		CrsRepository.INSTANCE.add(MGM2008);
		CrsRepository.INSTANCE.add(GEO_GDM_2008);
		CrsRepository.INSTANCE.add(MUSGRID);
		CrsRepository.INSTANCE.add(GEO_LE_POUCE_1934);
	}


}
