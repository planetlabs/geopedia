package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.geometry.crs.CRS.ETRS89_ELLIPSOIDAL;
import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import java.util.Date;

import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.DatumConversion.PositionVector7Params;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.Position2D;

@SuppressWarnings("unused")
public class NorTransforms {
	private static UTM createUTM(int zone) {
		return new UTM(ETRS89_ELLIPSOIDAL, new CrsIdentifier(EPSG, 25800+zone), UTM.centralLongForZone(zone), UTM.getNorthEnvelope(), true);
	}

	// Official projections are ETRS89 / North UTM 32, 33 and 35
	public static final UTM NOR_UTM_32N = createUTM(32);
	public static final UTM NOR_UTM_33N = createUTM(33);
	
	public static final TMToGeographic UTM32_TO_ETRS89 = new TMToGeographic(NOR_UTM_32N);
	public static final TMToGeographic UTM33_TO_ETRS89 = new TMToGeographic(NOR_UTM_33N);

	public static final GeographicToTM ETRS89_TO_UTM32 = new GeographicToTM(NOR_UTM_32N);
	public static final GeographicToTM ETRS89_TO_UTM33 = new GeographicToTM(NOR_UTM_33N);
	
	public static final ToCartesian<TransverseMercator, TransverseMercator> UTM32_TO_UTM33 = Transforms.compose(UTM32_TO_ETRS89, ETRS89_TO_UTM33);  
	public static final ToCartesian<TransverseMercator, TransverseMercator> UTM33_TO_UTM32 = Transforms.compose(UTM33_TO_ETRS89, ETRS89_TO_UTM32);

	@SuppressWarnings("deprecation")
	public static final PositionVector7Params WGS_TO_ETRS89 = DatumConversion.ITRF2005_TO_ETRS89(new Date().getYear()+1900);
	
	public static final Envelope NOR_TILES_ENV = Envelope.withSize(0, 6000000, 2097152, 2097152);
	public static final TiledCRS TILES_NOR = new WithBounds(NOR_UTM_32N, "Norway Tiles", //
		ScaleLevelsSpec.createWithFactor2(0.125, 20, 4), NOR_TILES_ENV, //
		DimI.create(256, 256), POSITIVE, POSITIVE //
	);
	
	public static final double[] UTM_TO_WGS_LAT = new double[] { //
		 2.5202719812189285,     -5.78865740507348e-6,     3.3481427540921767e-12,  9.484417160484777e-18,  7.236543745619538e-24, -2.759432542037395e-29, //	
		 7.714912742207818e-6,    2.1185381354001945e-12, -1.1963178337157001e-18, -3.835674393106768e-24, -3.110951919259064e-30,  1.168571828265938e-35, //
		 2.677481337520992e-13,  -1.0241366894356716e-19,  3.901361228541261e-26,   3.1964335866966784e-31, 3.054220424096401e-37, -1.096494355107996e-42, //
		-3.0821156662110597e-20, -2.752221478875837e-26,   1.5001442725191864e-32,  5.18955917864523e-38,   4.145867237163839e-44, -1.5671633826794158e-49,//
		 2.160650028062334e-27,   2.8206122477517408e-33, -1.065717365154507e-39,  -9.35296885623413e-45,  -9.369670094918242e-51,  3.309786078041233e-56, //
		-8.772774300103908e-35,   3.962303098871673e-43,  -4.67758204212116e-47,    3.7264232131205615e-52, 4.700224094819279e-58, -1.558427542484349e-63  //
	};
	
	public static final double[] UTM_TO_WGS_LON = new double[] { //
		-32.69995203507964,      -7.937717349645302e-6,   8.489037049011167e-11,   2.246196579671021e-16,  1.8210786053147202e-22, -4.808525265737963e-28, //
		  1.8684940209036683e-5,  3.100671460256736e-12, -4.0556140722178245e-17, -9.367378240319899e-23, -7.590409681193047e-29,   2.0395803125651054e-34,//
		 -2.3531687268343152e-12, 5.844219014820882e-19,  4.62989034962534e-24,    8.522669355531188e-30,  6.885899092475753e-36,  -1.9166210182810072e-41,//
		 -2.515979221289246e-19, -3.658779715842668e-26,  5.4426884341821485e-31,  1.2553912348871715e-36, 1.0179614430768709e-42, -2.7355378938360228e-48,//
		  7.310296093970885e-26, -2.1112668509147637e-32,-1.4350443461556597e-37, -2.55579669444422e-43,  -2.061578798152352e-49,   5.777316760947843e-55, //
		 -4.333857742980667e-33,  2.3888332138695056e-39, 7.795282147619549e-45,   1.165829088304407e-50,  9.372151387559084e-57,  -2.7161678289623312e-62 //
	};
	
	public static final double[] WGS_TO_UTM_X = new double[] { //
		-532875.5490530161,      2106.6278025203424,     95.11294303776064,       0.7542714946163205,     -0.008467139850788523,    0.000012764042883730941,//
		 114524.09892618858,     -240.053199250861,     -10.380815949096373,     -0.07241146640963773,     0.0005435118156409744,   1.6400209213518411e-6,  //
		    365.4727659151438,     -7.338451076227314,   -0.10360675058279398,    0.0012612766382238637,   0.00005081362812401998, -5.70667148884437e-7,    //
		    -65.27086346533494,     1.6642928052923156,   0.006956048966497727,  -0.00025466392920661684, -3.5276885527171537e-6,   5.0864836287694926e-8,  //
		      2.861111133445057,   -0.05188464653366072, -0.000858641594249348,   6.777060839791227e-6,    3.586399960500772e-7,   -3.5516179458625815e-9,  //
	         -0.01046546216512541, -0.001166409353804038, 0.00004032015908168661, 2.2322547223063747e-7,  -1.5645967905721798e-8,   1.153580823518292e-10   //
	};
	
	public static final double[] WGS_TO_UTM_Y = new double[] { //
		292316.4012739544,    93951.65917117988,     414.3013043431909,        -4.444351572616065,       0.017242718488993825,    1.5588564946865672e-6,//
		-30789.958789196884,  1030.2213996567043,    -11.719854787575594,      -0.16929464454784032,     0.00454443024416174,    -2.4133444585405807e-5,//
		  1026.9361210691438,  -11.09308452291124,    -0.05986637621528778,     0.001510614883304995,    1.5856731115029163e-5,  -3.2539382694418047e-7,//
		    32.853697754606884, -3.432947461343678,    0.06689132269524473,     0.000611826870261684,   -0.000025656642459379794, 1.7031146620521503e-7,//
		    10.664892794298867, -0.41192585619445454,  0.002783636743720112,    0.00006641032138665435, -1.003167800533846e-6,    3.46412214067619e-9,  //
	        -0.8455983521444428, 0.03819855623803174, -0.00037278968067105776, -6.2031814019876505e-6,   1.3602968495488663e-7,  -6.782255390154335e-10 //
	};
	
	public static void main(String[] args) {
//		matrixToWgs84();
		testMatrixToWgs84();
//		printTilesEnv();
//		saveTiledCRS();
	}

	private static void testMatrixToWgs84() {
		checkVal(64.01239249834329, Transforms.applyMatrixToXYPowers(600000, 7100000, UTM_TO_WGS_LAT, 5));
		checkVal(11.045654581634253, Transforms.applyMatrixToXYPowers(600000, 7100000, UTM_TO_WGS_LON, 5));
		checkVal(600000, Transforms.applyMatrixToXYPowers(64.01239249834329, 11.045654581634253, WGS_TO_UTM_X, 5));
		checkVal(7100000, Transforms.applyMatrixToXYPowers(64.01239249834329, 11.045654581634253, WGS_TO_UTM_Y, 5));
	}

	private static void checkVal(double expected, double actual) {
		double delta = actual-expected;
		if (Math.abs(delta/actual) > 1e-10) {
			System.out.println("ERROR: "+(expected-actual));
		}
	}

	private static void printTilesEnv() {
		Point southWestMain = ETRS89_TO_UTM32.point(new Point(58, 5));
		Point northEastMain = ETRS89_TO_UTM32.point(new Point(71, 31));
		Envelope mainEnv = Envelope.create(southWestMain, northEastMain);
		System.out.println(mainEnv + " " + mainEnv.getWidth() + " " + mainEnv.getHeight());
		System.out.println(TILES_NOR.worldTileWidth(TILES_NOR.getMinLevelId()));
		System.out.println(NOR_TILES_ENV.getMinX() + TILES_NOR.worldTileWidth(TILES_NOR.getMinLevelId()+1));
		System.out.println(NOR_TILES_ENV);
		System.out.println(NOR_TILES_ENV.contains(mainEnv));
		
		Point southWestAll = ETRS89_TO_UTM32.point(new Point(58, -9));
		Point northEastAll = ETRS89_TO_UTM32.point(new Point(81, 33));
		Envelope svalAll = Envelope.create(southWestAll, northEastAll);
		System.out.println(svalAll +" "+svalAll.getWidth() + " "+svalAll.getHeight());
	}
	
	public static void matrixToWgs84() {
		Envelope envUtm = new Envelope(500000, 7000000, 600000, 7100000);
		final int nPts = 9; 
		Position2D[] coordsUtm = new Position2D[nPts*nPts];
		Position2D[] coordsWgs = new Position2D[nPts*nPts];
		for (int i = 0; i < nPts; i++) {
			for (int j = 0; j < nPts; j++) {
				Point utm = new Point(//
					envUtm.getMinX() + (i * envUtm.getWidth())/(nPts - 1), //
					envUtm.getMinY() + (j * envUtm.getHeight())/(nPts - 1));
				Point wgs = UTM32_TO_ETRS89.point(utm);
				
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
}
