package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.geometry.crs.CRS.ETRS89_ELLIPSOIDAL;
import static com.sinergise.common.geometry.crs.CRS.WGS84_ELLIPSOIDAL;
import static com.sinergise.common.geometry.crs.transform.Transforms.compose;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.ObliqueStereographic;
import com.sinergise.common.geometry.crs.ObliqueStereographic.GeographicToOS;
import com.sinergise.common.geometry.crs.ObliqueStereographic.OSToGeographic;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;
import com.sinergise.common.util.geom.Envelope;

public class RomanianTransforms {
	
	//"Dealul Piscului 1970/ Stereo 70"
	//http://spatialreference.org/ref/epsg/3844/
	
	public static final Envelope RO_MBR = new Envelope(134140, 235566, 871026, 753167);
	
	public static final Ellipsoidal PULKOVO_1942_58 = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 4179), Ellipsoid.KRASSOWSKY_1940, new Envelope(9.92, 39.64, 29.73, 54.89)).setNiceName("Pulkovo 1942(58)");
		
	public static final ObliqueStereographic STEREO70 = (ObliqueStereographic)new ObliqueStereographic(
		PULKOVO_1942_58, 		//baseCRS
		46, 					//Longitude of natural origin
		25, 					//Latitude of natural origin
		0.99975, 				//scale factor at natural origin
		new CrsIdentifier(CrsAuthority.EPSG, 3844), 
		RO_MBR)
	.setOffset(500000, 500000)	//false easting and northing
	.setNiceName("Dealul Piscului 1970 / Stereo 70");
	
	public static final OSToGeographic STEREO70_TO_PULKOVO_1942_58 = new OSToGeographic(STEREO70);
	public static final GeographicToOS PULKOVO_1942_58_TO_STEREO70 = STEREO70_TO_PULKOVO_1942_58.inverse();
	
	public static final TiledCRS RO_TILES = TiledCRS
		.createDefault(STEREO70, "Romanian tiles", 50000, 50000, 512, 5)
		.setTilePrefixChar('X')
		.setInterlacedName(false);

	/**
	 * EPSG::15994
	 * Accuracy of 1.5 to 3 metres horizontal, 3 to 5m vertical.
	 */
	public static final DatumConversion DC_PULKOVO_1942_58_TO_ETRS89 = DatumConversion.cfrFromMetreArcSecondPpm(PULKOVO_1942_58, ETRS89_ELLIPSOIDAL, //
		new double[] {2.3287, -147.0425, -92.0802, //
		0.3092483, -0.32482185, -0.49729934, //
		5.68906266});
	
	public static final DatumConversion DC_ETRS89_TO_PULKOVO_1942_58 = DC_PULKOVO_1942_58_TO_ETRS89.inverse();
	
	/**
	 * EPSG::15995
	 * Accuracy of 1.5 to 3 metres horizontal, 3 to 5m vertical.
	 */
	public static final DatumConversion DC_PULKOVO_1942_58_TO_WGS84 = DatumConversion.cfrFromMetreArcSecondPpm(PULKOVO_1942_58, WGS84_ELLIPSOIDAL, //
		new double[] {2.329, -147.042, -92.08, //
		0.309, -0.325, -0.497, //
		5.69});

	public static final DatumConversion DC_WGS84_TO_PULKOVO_1942_58 = DC_PULKOVO_1942_58_TO_WGS84.inverse();

	public static final ToLatLon<ObliqueStereographic, Ellipsoidal> STEREO70_TO_WGS84 = compose(STEREO70_TO_PULKOVO_1942_58, DC_PULKOVO_1942_58_TO_WGS84);
	public static final ToCartesian<Ellipsoidal, ObliqueStereographic> WGS84_TO_STEREO70 = compose(DC_WGS84_TO_PULKOVO_1942_58, PULKOVO_1942_58_TO_STEREO70);
	
	
	public static void init() {
		CrsRepository.INSTANCE.add(PULKOVO_1942_58);
		CrsRepository.INSTANCE.add(STEREO70);
	}
}
