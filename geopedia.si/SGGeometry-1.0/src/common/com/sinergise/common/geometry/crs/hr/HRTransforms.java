package com.sinergise.common.geometry.crs.hr;

import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;
import static com.sinergise.common.util.math.MathUtil.SEC_IN_RAD;

import java.util.Date;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.misc.BalkansGKTransforms;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledCRSMapping;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;


public class HRTransforms {
	public static final TransverseMercator HTRS96TM = (TransverseMercator)new TransverseMercator(
		CRS.ETRS89_ELLIPSOIDAL, // Should be HTRS96_ELLIPSOIDAL (epsg:4761), but there's really no difference
		0, 16.5, 0.9999, new CrsIdentifier(EPSG, 3765), new Envelope(200000, 4650000, 800000, 5200000)).setOffset(
		500000, 0).setNiceName("HTRS 96/TM");
	public static final TMToGeographic HTRS96TM_TO_GEOGRAPHIC = new TMToGeographic(HTRS96TM);
	public static final GeographicToTM GEOGRAPHIC_TO_HTRS96TM = new GeographicToTM(HTRS96TM);

	public static final TransverseMercator HRGK5 = BalkansGKTransforms.GK5;
	public static final TMToGeographic HRGK5_TO_MGI = BalkansGKTransforms.GK5_TO_MGI;
	public static final GeographicToTM MGI_TO_HRGK5 = BalkansGKTransforms.MGI_TO_GK5;

	public static final TransverseMercator HRGK6 = BalkansGKTransforms.GK6;
	public static final TMToGeographic HRGK6_TO_MGI = BalkansGKTransforms.GK6_TO_MGI;
	public static final GeographicToTM MGI_TO_HRGK6 = BalkansGKTransforms.MGI_TO_GK6;

	@SuppressWarnings("deprecation")
	public static final DatumConversion GPS_TO_HTRS96 = DatumConversion.ITRF2005_TO_ETRS89(new Date().getYear() + 1900);
	public static final DatumConversion HTRS96_TO_GPS = GPS_TO_HTRS96.inverse();

	public static final ToCartesian<Ellipsoidal, TransverseMercator> GPS_TO_HTRS96TM = Transforms.compose(
		GPS_TO_HTRS96, HRTransforms.GEOGRAPHIC_TO_HTRS96TM);
	public static final ToLatLon<TransverseMercator, Ellipsoidal> HTRS96TM_TO_GPS = Transforms.compose(
		HRTransforms.HTRS96TM_TO_GEOGRAPHIC, HTRS96_TO_GPS);

	/**
	 * Provided by EPSG::3963
	 */
	public static final DatumConversion MGI_TO_HTRS96 = (DatumConversion)new DatumConversion.PositionVector7Params(
		CRS.MGI_BESSEL_ELLIPSOIDAL, CRS.ETRS89_ELLIPSOIDAL, new double[]{551.7, 162.9, 467.9}, new double[]{
			6.04 * SEC_IN_RAD, 1.96 * SEC_IN_RAD, -11.38 * SEC_IN_RAD}, -4.82).setName("MGI 1901 to ETRS96 (valid for Croatia)");
	public static final DatumConversion HTRS96_TO_MGI = MGI_TO_HTRS96.inverse();

	public static final ToCartesian<Ellipsoidal, TransverseMercator> MGI_TO_HTRS96TM = Transforms.compose(
		MGI_TO_HTRS96, GEOGRAPHIC_TO_HTRS96TM);
	public static final ToLatLon<TransverseMercator, Ellipsoidal> HTRS96TM_TO_MGI = Transforms.compose(
		HTRS96TM_TO_GEOGRAPHIC, HTRS96_TO_MGI);

	public static final ToCartesian<TransverseMercator, TransverseMercator> HRGK5_TO_HTRS96TM = Transforms.compose(
		HRGK5_TO_MGI, MGI_TO_HTRS96TM);
	public static final ToCartesian<TransverseMercator, TransverseMercator> HRGK6_TO_HTRS96TM = Transforms.compose(
		HRGK6_TO_MGI, MGI_TO_HTRS96TM);

	public static final ToCartesian<TransverseMercator, TransverseMercator> HTRS96TM_TO_HRGK5 = Transforms.compose(
		HTRS96TM_TO_MGI, MGI_TO_HRGK5);
	public static final ToCartesian<TransverseMercator, TransverseMercator> HTRS96TM_TO_HRGK6 = Transforms.compose(
		HTRS96TM_TO_MGI, MGI_TO_HRGK6);

	public static final TiledCRS TILES_HR = new WithBounds(HTRS96TM, "HRV Tiles", ScaleLevelsSpec.createWithFactor2(
		0.125, 20, 4), new Envelope(200000, 4570000, 200000 + 8192 * 256, 4570000 + 8192 * 256), new DimI(256, 256),
		POSITIVE, POSITIVE).setTilePrefixChar('H').setInterlacedName(false);

	public static final TiledCRS TILES_HR_512 = new WithBounds(HTRS96TM, "HRV Tiles",
		ScaleLevelsSpec.createWithFactor2(0.125, 20, 4), new Envelope(200000, 4570000, 200000 + 8192 * 256,
			4570000 + 8192 * 256), new DimI(512, 512), POSITIVE, POSITIVE).setTilePrefixChar('H').setInterlacedName(
		false);

	/**
	 * 
	 */
	public static final double[] M_HTRS96TM_TO_WGS84_LAT = {
		-8.775674234098270e-02, +3.246795709784323e-07, -1.775490767392439e-13, -2.973313972227400e-19, +1.516008118311515e-25,
		+9.096753602176130e-06, -1.904315373891446e-13, +6.745121148573961e-20, +2.484697080047817e-25, -1.266308626049163e-31, 
		-2.779533020935630e-14, +1.075313788228834e-19, -6.781784075598722e-26, -8.019587868333960e-32, +4.083125224162611e-38,
		+3.388404925869571e-21, -1.679332022511593e-26, +1.107403600979838e-32, +1.154323933805585e-38, -5.871344847491935e-45,
		-2.574832923300115e-28, +1.184598933898077e-33, -8.623735962945390e-40, -6.497935802719555e-46, +3.299814039236600e-52};

	public static final double[] M_HTRS96TM_TO_WGS84_LON = {
		+9.745287658982520e+00, +1.228637061798697e-05, +4.628923674622278e-12, -5.539872894002261e-18, +2.360749688129251e-24,
		+2.084907323924331e-06, -3.166616784258868e-12, -3.767756933343794e-18, +4.450622903076592e-24, -1.865323424215759e-30, 
		-7.885684537160666e-13, +1.258884827388593e-18, +1.179114015631492e-24, -1.359880725198465e-30, +5.521093351359307e-37, 
		+1.181269308022717e-19, -1.909029834766515e-25, -1.655271528949358e-31, +1.857438906048716e-37, -7.254803534376395e-44,
		-8.095097520657850e-27, +1.365620964221388e-32, +9.052070417942330e-39, -9.744429674079780e-45, +3.570046744391369e-51};

	public static final double[] M_WGS84_TO_HTRS96TM_X = {
		-1.337693812002819e+06, -2.385916448614858e+03, +3.875604063127658e+02, -1.610525898002887e+00, +1.502232082316177e-03, 
		+1.106315937774744e+05, +5.094441192314258e+02, -3.916982851376629e+01, +3.244171323420419e-01, -1.209860984661700e-03,
		+1.658677672484410e+02, -4.203028837601119e+01, +1.725244303692790e+00, -2.511670152376882e-02, +1.269951097864489e-04,
		-1.125544232398142e+01, +1.562111244707468e+00, -5.896218628385723e-02, +8.692506504745510e-04, -4.599732811219159e-06,
		+2.384883277108112e-01, -2.151236304111564e-02, +7.273875885513847e-04, -1.091719830184086e-05, +6.137339691993756e-08};

	public static final double[] M_WGS84_TO_HTRS96TM_Y = {
		-9.358551438425450e+01, +1.152813349389794e+05, +7.348198975142810e+00, -1.249273264254780e+00, +7.347740609417680e-03,
		+2.817078939221200e+02, -6.179186090871208e+02, +1.400147337438156e-02, +1.669795664780378e-01, -1.029375242332241e-03,
		-4.645072126858890e+01, +2.496627878577896e+01, -1.453091078817037e-01, -4.310702837874818e-03, +3.345233287632816e-05,
		+2.297008901820731e+00, -3.781969025341439e-01, +8.778381691019770e-03, -4.537349120463566e-05, -1.371282485232423e-07,
		-3.477866243894585e-02, +5.728034007181636e-03, -1.329307633603919e-04, +6.863563706736722e-07, +2.083966453668657e-09}


	;

	static {
		TiledCRSMapping.INSTANCE.put(TILES_HR);
	}

	public static final void hrgk5_to_htrs96tm(Point src, Point tgt) {
		HRGK5_TO_HTRS96TM.point(src, tgt);
	}

	public static final void hrgk6_to_htrs96tm(Point src, Point tgt) {
		HRGK6_TO_HTRS96TM.point(src, tgt);
	}

	public static final void htrs96tm_to_hrgk5(Point src, Point tgt) {
		HTRS96TM_TO_HRGK5.point(src, tgt);
	}

	public static final void htrs96tm_to_hrgk6(Point src, Point tgt) {
		HTRS96TM_TO_HRGK6.point(src, tgt);
	}
}
