package com.sinergise.common.geometry.crs.data;

import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;
import static com.sinergise.common.util.math.MathUtil.SEC_IN_RAD;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.Position2D;


public class MkTransforms {
	/**
	 * epsg:3909 with modified offsets
	 */
	public static final TransverseMercator MKGK7 = (TransverseMercator)new TransverseMercator(
		CRS.MGI_BESSEL_ELLIPSOIDAL, //
		0, 21, 0.9999, new CrsIdentifier("-31275"), new Envelope(450000, 520000, 675000, 700000) //
	).setOffset(500000, -4000000).setNiceName("MKGK7");

	public static final TMToGeographic MKGK7_TO_GEOGRAPHIC = new TMToGeographic(MKGK7);
	public static final GeographicToTM GEOGRAPHIC_TO_MKGK7 = new GeographicToTM(MKGK7);

	public static final TiledCRS TILES_MKGK7 = new WithBounds(MKGK7, "MK Tiles", ScaleLevelsSpec.createWithFactor2(
		0.125, 20, 7), new Envelope(430000, 480000, 430000 + 1024 * 256, 480000 + 1024 * 256), new DimI(256, 256),
		POSITIVE, POSITIVE).setTilePrefixChar('M').setInterlacedName(false);

	private static final ScaleLevelsSpec.ZoomLevelsPix DEM_LEVELS = ScaleLevelsSpec.createWithFactor2(5, 15, 8);
	private static final int DEM_TILE_SIZE = 384;
	private static final double DEM_MBR_SIZE = DEM_TILE_SIZE * DEM_LEVELS.worldPerPix(DEM_LEVELS.getMinLevelId());
	public static final TiledCRS TILES_MK_DEM = new WithBounds(MKGK7, "MK Tiles 5m/px", DEM_LEVELS, new Envelope(
		7430000 - 2.5, 4480000 - 2.5, 7430000 - 2.5 + DEM_MBR_SIZE, 4480000 - 2.5 + DEM_MBR_SIZE), new DimI(
		DEM_TILE_SIZE, DEM_TILE_SIZE), POSITIVE, POSITIVE).setInterlacedName(false).setOverlap(1, 1, 1, 1);

	/**
	 * http://www.katastar.gov.mk/userfiles/file/Specifikacija-JICA%2025000.pdf
	 * dx =-521.7476m, dy =-229.4892m, dz =-590.9207m rx =4.02878”, ry =4.48836”, rz =-15.52067” s =9.7803ppm
	 */
	public static final DatumConversion DC_MGI_TO_WGS = new DatumConversion.PositionVector7Params(
		CRS.MGI_BESSEL_ELLIPSOIDAL, CRS.WGS84_ELLIPSOIDAL,
		new double[]{521.7476, 229.4892, 590.9207}, //
		new double[]{4.02878 * SEC_IN_RAD, 4.48836 * SEC_IN_RAD, -15.52067 * SEC_IN_RAD},
		9.7803);
	public static final DatumConversion DC_WGS_TO_MGI = DC_MGI_TO_WGS.inverse();
	public static final ToCartesian<Ellipsoidal, TransverseMercator> WGS_TO_MKGK7 = Transforms.compose(DC_WGS_TO_MGI,
		GEOGRAPHIC_TO_MKGK7);
	public static final ToLatLon<TransverseMercator, Ellipsoidal> MKGK7_TO_WGS = Transforms.compose(
		MKGK7_TO_GEOGRAPHIC, DC_MGI_TO_WGS);

	/**
	 * epsg:3962
	 */
	public static final DatumConversion DC2_MGI_TO_WGS = new DatumConversion.GeocentricTranslation(
		CRS.MGI_BESSEL_ELLIPSOIDAL, CRS.WGS84_ELLIPSOIDAL,
		new double[] {682, -203, 480});
	public static final DatumConversion DC2_WGS_TO_MGI = DC2_MGI_TO_WGS.inverse();
	
	
	public static final ToCartesian<Ellipsoidal, TransverseMercator> WGS_TO_MKGK7_2 = Transforms.compose(DC2_WGS_TO_MGI,
		GEOGRAPHIC_TO_MKGK7);
	public static final ToLatLon<TransverseMercator, Ellipsoidal> MKGK7_TO_WGS_2 = Transforms.compose(
		MKGK7_TO_GEOGRAPHIC, DC2_MGI_TO_WGS);

	/**
	 * Geodetski Zavod Celje 2012 : UPUTE_ZA_TRANSFORMACIJU_I_EKSPORT_GPS_IZMJERE_U_SHAPE_SA.pdf 
	 */
	public static final DatumConversion DC3_MGI_TO_WGS = new DatumConversion.PositionVector7Params(
		CRS.MGI_BESSEL_ELLIPSOIDAL, CRS.WGS84_ELLIPSOIDAL,
		new double[]{608.912647, 187.096513, 612.359794}, //
		new double[]{4.42044986 * SEC_IN_RAD, 3.66552487 * SEC_IN_RAD, -12.37153856 * SEC_IN_RAD},
		19.95260189);
	public static final DatumConversion DC3_WGS_TO_MGI = DC3_MGI_TO_WGS.inverse();

	public static final ToCartesian<Ellipsoidal, TransverseMercator> WGS_TO_MKGK7_3 = Transforms.compose(DC3_WGS_TO_MGI,
		GEOGRAPHIC_TO_MKGK7);
	public static final ToLatLon<TransverseMercator, Ellipsoidal> MKGK7_TO_WGS_3 = Transforms.compose(
		MKGK7_TO_GEOGRAPHIC, DC3_MGI_TO_WGS);

    // Geodetski Zavod Celje 2012 : UPUTE_ZA_TRANSFORMACIJU_I_EKSPORT_GPS_IZMJERE_U_SHAPE_SA.pdf
    // 7-parameters datum conversion + transverse mercator projection
    // fitted to polynomials of the form a0 + a1 x + a2 x^2 + .. + a5 y + a6 x y + .. + a24 x^4 y^4
	public static final double[] WGS_TO_GK_X = new double[] {-1.2874046463332221e8,1.2189294481083607e7,-438808.4038452528,7032.8349588240735,-42.24195676137058,2.3503606764869444e7,-2.247096889008654e6,80945.21508926345,-1296.5398428483552,7.786303250843563,
	    -1.6159735548273074e6,155249.20984354732,-5593.746440791217,89.57864109004025,-0.5379242906259384,49584.35566729645,-4764.189509525717,171.66296518024552,-2.7490004298754656,0.016507582947782498,-570.189752558432,
	    54.79190969674358,-1.97432737488045,0.031616482113515774,-0.0001898512998973275};
	
	public static final double[] WGS_TO_GK_Y = new double[] {-1.6844495111099314e6,-104215.2280960509,8032.744248861697,-130.66747876398819,0.7848286439718466,-426693.69419342245,
	    40219.08073254994,-1478.6527227535537,23.91323398614877,-0.1437236270166318,29464.432619653617,-2808.5812414096463,102.03569099681468,-1.642155360826644,0.009875177382498676,-903.9812011481565,86.6709148570251,
	    -3.129346839013215,0.050236293879316524,-0.00030219599220943197,10.3974074967444,-0.9967909285629358,0.03599273795439011,-0.00057785094842081,3.476269016691795e-6};
	
	public static final double[] GK_TO_WGS_LAT = new double[] {36.010041658849154,5.122457620506626e-7,-5.04970536998254e-13,-1.3870688259708473e-20,6.9325793985512e-27,8.970718090244213e-6,1.674738097603744e-13,-1.640976691884612e-19,-6.658666442758413e-27,
	    3.331064795162658e-33,-1.159819790822212e-14,1.886349008037948e-20,-1.782046138221042e-26,-2.0611792530631468e-33,1.0239764699395179e-39,-9.739551112464603e-22,2.9882255661011714e-27,-2.880632579857749e-33,
	    -2.195198689616716e-40,1.1655989407180496e-46,-1.7577980271986632e-28,8.983335816176515e-34,-7.463175652919295e-40,-3.0045405541009e-46,1.4745341410015548e-52};
	
	public static final double[] GK_TO_WGS_LON = new double[] {15.45108256330173,0.000011040537751095461,1.4193802114861137e-13,-9.503978264089252e-20,4.495554321800937e-28,-6.297744064625375e-7,1.2374111285650217e-12,6.402545465814531e-20,-4.3007112824159485e-26,
	    3.4170580400070657e-34,-1.41341637220398e-13,2.744941843916801e-19,2.432802975374063e-26,-1.645984010909431e-32,2.4798942873873076e-40,-1.7548002068731872e-20,3.383951967455303e-26,3.7154247447133274e-33,
	    -2.462531995942269e-39,-1.3968743646991117e-47,-6.042476054248728e-27,1.0966911529004001e-32,3.3654156741372246e-39,-2.317034265650075e-45,7.448848633578475e-53};
	
	public static void main(String[] args) {
		
		//google Maps: 42.004457, 21.421360
		//SIZP: 535341,131 651244,734
		printDif(42.004457, 21.421360, 535341.131, 651244.734);
		printDif(41.110953, 20.799238, 483582.525, 551948.618);
		printDif(41.395459, 22.942162, 662857.21, 585349.76);
		printDif(42.202009, 22.333373, 610553.507, 673964.079);
		
		matrixToWgs84();
		testMatrixToWgs84();
		
		/*try {
		    com.sinergise.common.geometry.tiles.TileUtil.saveForBaseDir(TILES_MKGK7, new File("C:\\Temp"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Point bl = GEOGRAPHIC_TO_MKGK7.point(new Point(40.84,20.43));
		Point tr = GEOGRAPHIC_TO_MKGK7.point(new Point(42.37,23.05));
		
		System.out.println(bl);
		System.out.println(tr);
		
		System.out.println("W: "+(tr.x-bl.x));
		System.out.println("H: "+(tr.y-bl.y));
		
		System.out.println("C: "+0.5*(tr.x+bl.x)+","+0.5*(tr.y+bl.y));
		double lx = 430000;
		double by = 480000;
		
		double tSize = 1024*256;
		double rx = lx+tSize;
		double ty = by+tSize;
		System.out.println("---");
		System.out.println(bl.x-lx);
		System.out.println(rx-tr.x);
		System.out.println("---");
		System.out.println(bl.y-by);
		System.out.println(ty-tr.y);
		*/
	}


	private static void printDif(double lat, double lon, double x, double y) {
		Point refGeo = new Point(lat, lon);
		Point refProj= new Point(x, y);

		Point calcProj = WGS_TO_MKGK7.point(refGeo, new Point());
		Point calcGeo = MKGK7_TO_WGS.point(refProj, new Point());
		
		System.out.print(Math.sqrt(calcProj.distanceSq2D(refProj))+ " "+calcGeo.distanceSq2D(refGeo)*60*60*1000);

		Point calcProj2 = WGS_TO_MKGK7_2.point(refGeo, new Point());
		Point calcGeo2 = MKGK7_TO_WGS_2.point(refProj, new Point());
		
		System.out.print(" ... " + Math.sqrt(calcProj2.distanceSq2D(refProj))+ " "+calcGeo2.distanceSq2D(refGeo)*60*60*1000);

		Point calcProj3 = WGS_TO_MKGK7_3.point(refGeo, new Point());
		Point calcGeo3 = MKGK7_TO_WGS_3.point(refProj, new Point());
		
		System.out.println(" ... " + Math.sqrt(calcProj3.distanceSq2D(refProj))+ " "+calcGeo3.distanceSq2D(refGeo)*60*60*1000);

	}
	
	public static void matrixToWgs84() {
		Envelope envUtm = new Envelope(450000, 520000, 670000, 693000);
		final int nPts = 9; 
		Position2D[] coordsUtm = new Position2D[nPts*nPts];
		Position2D[] coordsWgs = new Position2D[nPts*nPts];
		for (int i = 0; i < nPts; i++) {
			for (int j = 0; j < nPts; j++) {
				Point utm = new Point(//
					envUtm.getMinX() + (i * envUtm.getWidth())/(nPts - 1), //
					envUtm.getMinY() + (j * envUtm.getHeight())/(nPts - 1));
				Point wgs = MKGK7_TO_WGS_3.point(utm, new Point());
				
				coordsUtm[nPts * i + j] = utm;
				coordsWgs[nPts * i + j] = wgs;
			}
		}
		System.out.println("{");
		for (int i = 0; i < coordsUtm.length; i++) {
			System.out.print("{" + coordsUtm[i].x +","+coordsUtm[i].y+"},");
		}
		System.out.println("\n}\n>>>>>>>>>>>>>>>\n{");
		for (int i = 0; i < coordsWgs.length; i++) {
			System.out.print("{" + coordsWgs[i].x +","+coordsWgs[i].y+"},");
		}
		System.out.println("\n}");
	}
	
	private static void testMatrixToWgs84() {
		Envelope envUtm = new Envelope(450000, 520000, 670000, 693000);
		final int nPts = 3; 
		for (int i = 0; i < nPts; i++) {
			for (int j = 0; j < nPts; j++) {
				Point utm = new Point(//
					envUtm.getMinX() + (i * envUtm.getWidth())/(nPts - 1), //
					envUtm.getMinY() + (j * envUtm.getHeight())/(nPts - 1));
				Point wgs = MKGK7_TO_WGS_3.point(utm, new Point());
				
				System.out.println(utm + "  ->  "+wgs);
				
				checkVal(wgs.x, Transforms.applyMatrixToXYPowers(utm.x, utm.y, GK_TO_WGS_LAT, 4));
				checkVal(wgs.y, Transforms.applyMatrixToXYPowers(utm.x, utm.y, GK_TO_WGS_LON, 4));
				checkVal(utm.x, Transforms.applyMatrixToXYPowers(wgs.x, wgs.y, WGS_TO_GK_X, 4));
				checkVal(utm.y, Transforms.applyMatrixToXYPowers(wgs.x, wgs.y, WGS_TO_GK_Y, 4));
			}
		}		
	}
	
	private static void checkVal(double expected, double actual) {
		double delta = actual-expected;
		if (Math.abs(delta/actual) > 1e-9) {
			System.out.println("ERROR: "+(expected-actual));
		} else {
			System.out.println("OK: "+Math.abs(delta));
		}
	}
}
