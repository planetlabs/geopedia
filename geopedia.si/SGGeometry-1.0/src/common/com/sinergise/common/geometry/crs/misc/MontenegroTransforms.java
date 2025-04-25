package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.geometry.crs.misc.BalkansGKTransforms.GK6_TO_MGI;
import static com.sinergise.common.geometry.crs.misc.BalkansGKTransforms.GK7_TO_MGI;
import static com.sinergise.common.geometry.crs.misc.BalkansGKTransforms.MGI_TO_GK6;
import static com.sinergise.common.geometry.crs.misc.BalkansGKTransforms.MGI_TO_GK7;
import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;

public class MontenegroTransforms {
	public static final DatumConversion DC_ME_MGI_1901_TO_ETRS89 = (DatumConversion)new DatumConversion.PositionVector7Params(
			CRS.MGI_BESSEL_ELLIPSOIDAL, CRS.ETRS89_ELLIPSOIDAL, 
			261.89858, 221.21591, 743.87680, 
			4.99487 * MathUtil.SEC_IN_RAD, 14.45241 * MathUtil.SEC_IN_RAD, -15.13857 * MathUtil.SEC_IN_RAD, 
			2.03665).setName("MGI 1901 to ETRS89 (valid for Montenegro)");
	public static final DatumConversion DC_ETRS89_TO_ME_MGI_1901 = DC_ME_MGI_1901_TO_ETRS89.inverse();
	
	public static final TransverseMercator.UTM ME_UTM34N = new TransverseMercator.UTM(CRS.ETRS89_ELLIPSOIDAL
			, new CrsIdentifier(EPSG, 25834), UTM.centralLongForZone(34), new Envelope(0,0,1000000,10000000), true);
	public static final TMToGeographic ME_UTM34N_TO_ETRS89 = new TMToGeographic(ME_UTM34N);
	public static final GeographicToTM ETRS89_TO_ME_UTM34N = new GeographicToTM(ME_UTM34N);
	
	public static final ToCartesian<Ellipsoidal, TransverseMercator> MGI1901_TO_ME_UTM34N = Transforms.compose(DC_ME_MGI_1901_TO_ETRS89, ETRS89_TO_ME_UTM34N);
	public static final ToLatLon<TransverseMercator, Ellipsoidal> ME_UTM34N_TO_MGI1901 = Transforms.compose(ME_UTM34N_TO_ETRS89, DC_ETRS89_TO_ME_MGI_1901);
	
	public static final ToCartesian<TransverseMercator, TransverseMercator> GK6_TO_ME_UTM34N = Transforms.compose(GK6_TO_MGI, MGI1901_TO_ME_UTM34N); 
	public static final ToCartesian<TransverseMercator, TransverseMercator> GK7_TO_ME_UTM34N = Transforms.compose(GK7_TO_MGI, MGI1901_TO_ME_UTM34N);
	
	public static final ToCartesian<TransverseMercator, TransverseMercator> ME_UTM34N_TO_GK6 = Transforms.compose(ME_UTM34N_TO_MGI1901, MGI_TO_GK6);
	public static final ToCartesian<TransverseMercator, TransverseMercator> ME_UTM34N_TO_GK7 = Transforms.compose(ME_UTM34N_TO_MGI1901, MGI_TO_GK7);
	
	public static final ToCartesian<Ellipsoidal, TransverseMercator> ETRS89_TO_GK6 = Transforms.compose(DC_ETRS89_TO_ME_MGI_1901, MGI_TO_GK6);
	
	public static final Envelope MNE_BOUNDS_UTM34N = new Envelope(220000, 4600000, 220000 + 262144, 4600000 + 262144); 
	
	public static WithBounds MNE_DEFAULT_TILES =
        new WithBounds(ME_UTM34N,
                       "MNE Tiles",
                       ScaleLevelsSpec.createWithFactor2(0.125, 20, 7),
                       MNE_BOUNDS_UTM34N,
                       new DimI(256, 256),
                       POSITIVE,
                       POSITIVE);
	
	public static void main(String[] args) {
		System.out.println(ME_UTM34N_TO_ETRS89.point(new Point(MNE_BOUNDS_UTM34N.topLeft())));
		System.out.println(ME_UTM34N_TO_ETRS89.point(new Point(MNE_BOUNDS_UTM34N.bottomRight())));
		System.out.println(">>>>>>>>>>>>>>");
		
		//GK6 4500m * 6000m, 0.8m/px @ k5-19: 6603500, 4707000
		System.out.println(ME_UTM34N_TO_GK6.point(new Point(356559.5, 4706803), new Point()));
		System.out.println(ME_UTM34N_TO_GK6.point(new Point(361061.75, 4706643.5), new Point()));
		System.out.println(ME_UTM34N_TO_GK6.point(new Point(360847.41, 4700644.1), new Point()));
		System.out.println(ME_UTM34N_TO_GK6.point(new Point(356345.8, 4700804.5), new Point()));
//		System.out.println(">>>>>>>>>>>>>>");
//		System.out.println(ME_UTM34N_TO_GK7.point(new Point(356559.5, 4706803), new Point()));
//		System.out.println(ME_UTM34N_TO_GK7.point(new Point(361061.75, 4706643.5), new Point()));
//		System.out.println(ME_UTM34N_TO_GK7.point(new Point(360847.41, 4700644.1), new Point()));
//		System.out.println(ME_UTM34N_TO_GK7.point(new Point(356345.8, 4700804.5), new Point()));
	}
}
