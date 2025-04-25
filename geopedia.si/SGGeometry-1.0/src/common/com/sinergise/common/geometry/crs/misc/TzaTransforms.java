package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.geometry.tiles.TiledCRS.AxisSign.POSITIVE;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.Ellipsoid;
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
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;

public class TzaTransforms {
    public static final Ellipsoidal ARC_1960 = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 4210),Ellipsoid.CLARKE_1880_RGS, new Envelope(-11.74, 29.34, 4.62, 41.91)).setNiceName("ARC 1960");
    
    public static DatumConversion ARC_1960_TO_WGS_84 = new DatumConversion.GeocentricTranslation(ARC_1960, CRS.WGS84, -175, -23, -303);
    public static DatumConversion WGS_84_TO_ARC_1960 = ARC_1960_TO_WGS_84.inverse();
    
    public static final UTM ARC1960_UTM35S = new UTM(ARC_1960, new CrsIdentifier(EPSG, 21035), UTM.centralLongForZone(35), new Envelope(166015.5539, 8700569.9433, 833984.4461, 10000000.0000), false);
	public static final UTM ARC1960_UTM36S = new UTM(ARC_1960, new CrsIdentifier(EPSG, 21036), UTM.centralLongForZone(36), new Envelope(92000, 8690000, 1315000, 9892000), false);
    public static final UTM ARC1960_UTM37S = new UTM(ARC_1960, new CrsIdentifier(EPSG, 21037), UTM.centralLongForZone(37), new Envelope(166015.5539, 8700569.9433, 833984.4461, 10000000.0000), false);
    
    public static final GeographicToTM ARC1960_TO_UTM36S = new GeographicToTM(ARC1960_UTM36S); 
    public static final GeographicToTM ARC1960_TO_UTM35S = new GeographicToTM(ARC1960_UTM35S);
	public static final GeographicToTM ARC1960_TO_UTM37S = new GeographicToTM(ARC1960_UTM37S);
    
	public static final TMToGeographic UTM35S_TO_ARC1960 = new TMToGeographic(ARC1960_UTM35S);
    public static final TMToGeographic UTM36S_TO_ARC1960 = new TMToGeographic(ARC1960_UTM36S);
    public static final TMToGeographic UTM37S_TO_ARC1960 = new TMToGeographic(ARC1960_UTM37S);    
    
    public static final Envelope TILESENV_TZA = Envelope.withSize(0, 8600000, 2097152, 2097152); // level 4 is the smallest
    
    public static final ToCartesian<TransverseMercator, TransverseMercator> UTM35S_TO_UTM36S = Transforms.compose(UTM35S_TO_ARC1960, ARC1960_TO_UTM36S); 
    public static final ToCartesian<TransverseMercator, TransverseMercator> UTM35S_TO_UTM37S = Transforms.compose(UTM35S_TO_ARC1960, ARC1960_TO_UTM37S); 
	public static final ToLatLon<TransverseMercator, Ellipsoidal> UTM35S_TO_UTMWGS84 = Transforms.compose(UTM35S_TO_ARC1960, ARC_1960_TO_WGS_84);
    
    public static final ToCartesian<TransverseMercator, TransverseMercator> UTM36S_TO_UTM35S = Transforms.compose(UTM36S_TO_ARC1960, ARC1960_TO_UTM35S); 
    public static final ToCartesian<TransverseMercator, TransverseMercator> UTM36S_TO_UTM37S = Transforms.compose(UTM36S_TO_ARC1960, ARC1960_TO_UTM37S); 
    public static final ToLatLon<TransverseMercator, Ellipsoidal> UTM36S_TO_UTMWGS84 = Transforms.compose(UTM36S_TO_ARC1960, ARC_1960_TO_WGS_84);

    public static final ToCartesian<TransverseMercator, TransverseMercator> UTM37S_TO_UTM35S = Transforms.compose(UTM37S_TO_ARC1960, ARC1960_TO_UTM35S); 
    public static final ToCartesian<TransverseMercator, TransverseMercator> UTM37S_TO_UTM36S = Transforms.compose(UTM37S_TO_ARC1960, ARC1960_TO_UTM36S); 
    public static final ToLatLon<TransverseMercator, Ellipsoidal> UTM37S_TO_UTMWGS84 = Transforms.compose(UTM37S_TO_ARC1960, ARC_1960_TO_WGS_84);
	
	public static final ToCartesian<Ellipsoidal, TransverseMercator> WGS84_TO_UTM35S = Transforms.compose(
		WGS_84_TO_ARC_1960, ARC1960_TO_UTM35S);
	public static final ToCartesian<Ellipsoidal, TransverseMercator> WGS84_TO_UTM36S = Transforms.compose(
		WGS_84_TO_ARC_1960, ARC1960_TO_UTM36S);
	public static final ToCartesian<Ellipsoidal, TransverseMercator> WGS84_TO_UTM37S = Transforms.compose(
		WGS_84_TO_ARC_1960, ARC1960_TO_UTM37S);
    
    public static final TiledCRS TILES_TZA = new WithBounds(ARC1960_UTM36S , "Tanzania Tiles", //
		ScaleLevelsSpec.createWithFactor2(1, 17, 24, 4), TILESENV_TZA, //
		DimI.create(256, 256), POSITIVE, POSITIVE //
	);
    
    public static void main(String[] args) {
//		Envelope tanzaniaTight = ARC1960_TO_UTM36S.envelope(new Envelope(-11.74, 29.34, -1, 40.48));
//		
//		Envelope magomeni =  UTM37S_TO_ARC1960.envelope(new Envelope( 527999.850, 9249600.150, 527999.850, 9249600.150));
//		
		Envelope mwanza = UTM36S_TO_ARC1960.envelope(new Envelope(9726865.592, 487635.875, 9726865.592, 487635.875));
		
		System.out.println(mwanza);
		System.out.println(mwanza.getWidth() +" "+mwanza.getHeight());
		System.out.println(TILESENV_TZA.contains(mwanza));
		

		UTM utm = ARC1960_UTM36S;
		GeographicToTM toTM = new TransverseMercator.GeographicToTM(utm);

		Point sumgaytLatLong = new Point(-2.5166667, 32.90);
		ptinrTransformed(toTM, sumgaytLatLong);
	}


	private static void ptinrTransformed(GeographicToTM toTM, Point sumgaytLatLong) {
		Point sumgayt = toTM.point(sumgaytLatLong);
		System.out.println("Sumgait point   : " + sumgaytLatLong.x + ", " + sumgaytLatLong.y + ", " + sumgayt.x + ", "
			+ sumgayt.y);
	}


}
