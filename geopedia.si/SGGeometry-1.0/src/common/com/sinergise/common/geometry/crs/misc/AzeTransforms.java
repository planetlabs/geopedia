package com.sinergise.common.geometry.crs.misc;

import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * Documentation found here: https://internal.sinergise.com/wiki/bin/view/GIS/TiledCRS
 */
public class AzeTransforms {
	
	// actual max mbr is (-35184.5807999996,4252020.3657,492048.974,4650347.2703)
	public static final Envelope AZE_MBR = new Envelope(-35000, 4250000, 473000, 4651000);

	public static TransverseMercator.UTM WGS84_UTM39N = (UTM)TransverseMercator.createWGS84N(39).setNiceName(
		"UTM 39N (WGS84)").registerIdentifier(new CrsIdentifier(CrsAuthority.POSTGIS, 32639));
	
	public static final TMToGeographic UTM39N_TO_WGS84 =  new TMToGeographic(WGS84_UTM39N);
	public static final GeographicToTM WGS84_TO_UTM39N = UTM39N_TO_WGS84.inverse();
	
	
	public static final TiledCRS AZE_TILES = TiledCRS.createDefault(//
			WGS84_UTM39N, "Azerbaijani tiles", // 
			-192000, 3920000, //
			512, 6);
	
	public static void init() {
		CrsRepository.INSTANCE.add(WGS84_UTM39N);
	}
	
	public static void main(String[] args) {
		tiledCrsDerivation();
	}

	private static void tiledCrsDerivation() {
		UTM utm = TransverseMercator.createWGS84N(39);
		GeographicToTM toTM = new TransverseMercator.GeographicToTM(utm);
		Point utmLL = toTM.point(new Point(38.41, 44.82));
		Point utmUR = toTM.point(new Point(41.94, 50.41));

		Envelope env = Envelope.create(utmLL, utmUR);
		HasCoordinate cent = env.getCenter();
		System.out.println(cent);
		Envelope tileEnv = Envelope.withSize(-192000, 3920000, 512*2048, 512*2048); 
		System.out.println(tileEnv);
		System.out.println(tileEnv.contains(env));
		System.out.println("Left offset  : " + Math.round(env.getMinX() - tileEnv.getMinX()));
		System.out.println("Bottom offset: " + Math.round(env.getMinY() - tileEnv.getMinY()));
		System.out.println("Right offset : " + Math.round(-env.getMaxX() + tileEnv.getMaxX()));
		System.out.println("Top offset   : " + Math.round(-env.getMaxY() + tileEnv.getMaxY()));

		Point sumgaytLatLong = new Point(40.5897, 49.6686);
		ptinrTransformed(toTM, sumgaytLatLong);

	}

	private static void ptinrTransformed(GeographicToTM toTM, Point sumgaytLatLong) {
		Point sumgayt = toTM.point(sumgaytLatLong);
		System.out.println("Sumgait point   : " + sumgaytLatLong.x + ", " + sumgaytLatLong.y + ", " + sumgayt.x + ", "
			+ sumgayt.y);
	}
}
