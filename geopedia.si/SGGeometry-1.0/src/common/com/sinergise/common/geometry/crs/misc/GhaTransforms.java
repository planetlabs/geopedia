package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.geometry.crs.CRS.WGS84;
import static com.sinergise.common.geometry.util.CoordUtil.degFromDms;
import static com.sinergise.common.geometry.util.CoordUtil.GeogDirection.N;
import static com.sinergise.common.geometry.util.CoordUtil.GeogDirection.W;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.SINERGISE;
import static com.sinergise.common.util.math.MathUtil.AS_IN_RAD;

import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.crs.transform.MolodenskyBadekas;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.Units;
import com.sinergise.common.util.math.Units.Unit;

public class GhaTransforms {
	public static final Unit GOLD_COAST_FOOT = new Unit("GOLD COAST FOOT", "gcft", 0.3047997101815088175823217984, Units.METRE);

	private static final Ellipsoid WAR_OFFICE_ELLIPSOID = new Ellipsoid("EPSG::7029", 6378300, 296);
	public static final Ellipsoidal ACCRA_GEOGCS = new Ellipsoidal(new CrsIdentifier(EPSG, 4168), WAR_OFFICE_ELLIPSOID);
	static {
		ACCRA_GEOGCS.setNiceName("Accra");
	}
	
	// from "transformation parameters.docx" sent by Giles on 2014-01-22 
	public static final Ellipsoidal GGD_GEOGCS = new Ellipsoidal(new CrsIdentifier(SINERGISE,"GhanaGD"), Ellipsoid.GRS80);
	public static final TransverseMercator GTM_M = new TransverseMercator(GGD_GEOGCS, 0, -1, 0.99975, new CrsIdentifier(SINERGISE, "GhanaTM-M"), Envelope.getEmpty());
	static {
		GTM_M.setOffset(400000, 0);
		GGD_GEOGCS.setNiceName("GhanaGD");
		GTM_M.setNiceName("GhanaTM-M");
	}
	
	public static final TMToGeographic GTM_M_TO_GGD_GEOCS = GTM_M.createTransformToGeographic();
	public static final GeographicToTM GGD_GEOCS_TO_GTM_M = GTM_M.createTransformFromGeographic();
	
	public static final DatumConversion DC_ACCRA_TO_GGD = new MolodenskyBadekas(ACCRA_GEOGCS, GGD_GEOGCS, //
		-200.4650, 32.7959, 321.2570,//m
		-1.45396 * AS_IN_RAD, 9.83886 * AS_IN_RAD, -1.11425 * AS_IN_RAD, // rad
		-8.7570, // ppm
		6327518.6407, -136486.6156, 771316.7740);//m
	
	public static final DatumConversion GGD_TO_DC_ACCRA = DC_ACCRA_TO_GGD.inverse();
	
	
	//http://resources.arcgis.com/en/help/main/10.1/003r/pdf/geographic_transformations.pdf
	//Geographic (datum) Transformation Name WKID Method dx dy dz
	//Accra_To_WGS_1984 1569 Geocentric_Translation -199 32 322
	public static final DatumConversion DC_ACCRA_TO_WGS84_GEOCENTRIC_TRANS = new DatumConversion.GeocentricTranslation(ACCRA_GEOGCS, WGS84, -199, 32, 322);
	
	/**
	 * From "GTM Final Report" document
	 */
	public static final DatumConversion DC_ACCRA_TO_WGS84 //
		= DatumConversion.fromMetreArcSecondPpm(ACCRA_GEOGCS, WGS84, new double[] {//
			-109.0074, 2.8559, 27.1466, //
			-1.45396, 9.83886, -1.11425, //
			-8.7570});
	public static final DatumConversion WGS84_TO_DC_ACCRA = DC_ACCRA_TO_WGS84.inverse();
	
	
	public static final TransverseMercator GHANA_NATIONAL_GRID = (TransverseMercator)new TransverseMercator(ACCRA_GEOGCS, //
		degFromDms(4, 40, N), degFromDms(1, W), //
		GOLD_COAST_FOOT, 
		0.99975, //
		new CrsIdentifier(EPSG, 2136), //
		null).setOffset(900000,0).setNiceName("Ghana National Grid");
	
	public static final TMToGeographic GRID_TO_ACCRA = GHANA_NATIONAL_GRID.createTransformToGeographic();
	public static final GeographicToTM ACCRA_TO_GRID = GHANA_NATIONAL_GRID.createTransformFromGeographic();
	
	
	public static final TransverseMercator.UTM WGS84_UTM30N = (UTM)TransverseMercator.createWGS84N(30).setNiceName("UTM 30N");
	public static final TMToGeographic UTM30N_TO_WGS84 = new TMToGeographic(WGS84_UTM30N);
	public static final GeographicToTM WGS84_TO_UTM30N = UTM30N_TO_WGS84.inverse();

	public static final Transform<Ellipsoidal,TransverseMercator> ACCRA_GEOGCS_TO_UTM30N = Transforms.compose(DC_ACCRA_TO_WGS84, WGS84_TO_UTM30N);
	public static final Transform<TransverseMercator,Ellipsoidal> UTM30N_TO_ACCRA_GEOGCS = Transforms.compose(UTM30N_TO_WGS84, WGS84_TO_DC_ACCRA);
	
	public static final Transform<Ellipsoidal,Ellipsoidal> GGD_TO_WGS84 = Transforms.compose(GGD_TO_DC_ACCRA, DC_ACCRA_TO_WGS84);
	public static final Transform<Ellipsoidal,Ellipsoidal> WGS84_TO_GGD = Transforms.compose(WGS84_TO_DC_ACCRA, DC_ACCRA_TO_GGD);
	
	public static final Transform<Ellipsoidal,TransverseMercator> GGD_TO_UTM30N = Transforms.compose(GGD_TO_WGS84, WGS84_TO_UTM30N);
	public static final Transform<TransverseMercator,Ellipsoidal> UTM30N_TO_GGD = Transforms.compose(UTM30N_TO_WGS84, WGS84_TO_GGD);
	
	public static final Transform<TransverseMercator,TransverseMercator> GNG_TO_UTM30N = Transforms.compose(GRID_TO_ACCRA, ACCRA_GEOGCS_TO_UTM30N);
	public static final Transform<TransverseMercator,TransverseMercator> UTM30N_TO_GNG = Transforms.compose(UTM30N_TO_ACCRA_GEOGCS, ACCRA_TO_GRID);
	
	public static final Transform<TransverseMercator,TransverseMercator> GTM_TO_UTM30N = Transforms.compose(GTM_M_TO_GGD_GEOCS, GGD_TO_UTM30N);
	public static final Transform<TransverseMercator,TransverseMercator> UTM30N_TO_GTM = Transforms.compose(UTM30N_TO_GGD, GGD_GEOCS_TO_GTM_M);
	
	public static final Envelope GHA_MBR = new Envelope(376464, 399840, 1000000, 1300000);
	
	public static final TiledCRS GHA_TILES = TiledCRS.createDefault(//
		WGS84_UTM30N, "Ghana tiles", // 
		192000, 256000, //
		512, 6);

	
	public static void init() {
		
		CrsRepository.INSTANCE.add(WGS84_UTM30N);
		CrsRepository.INSTANCE.add(WGS84);
		CrsRepository.INSTANCE.add(GTM_M);
		CrsRepository.INSTANCE.add(GGD_GEOGCS);
		CrsRepository.INSTANCE.add(GHANA_NATIONAL_GRID);
		CrsRepository.INSTANCE.add(ACCRA_GEOGCS);
		
		
		Transforms.register(UTM30N_TO_WGS84);
		Transforms.register(WGS84_TO_UTM30N);
		
		Transforms.register(GRID_TO_ACCRA);
		Transforms.register(ACCRA_TO_GRID);
		
		Transforms.register(DC_ACCRA_TO_WGS84);
		Transforms.register(WGS84_TO_DC_ACCRA);
		
		Transforms.register(DC_ACCRA_TO_GGD);
		Transforms.register(GGD_TO_DC_ACCRA);
		
		Transforms.register(GGD_TO_WGS84);
		Transforms.register(WGS84_TO_GGD);
		
		Transforms.register(GGD_TO_UTM30N);
		Transforms.register(UTM30N_TO_GGD);
		
		Transforms.register(GTM_TO_UTM30N);
		Transforms.register(UTM30N_TO_GTM);
		
		Transforms.register(ACCRA_GEOGCS_TO_UTM30N);
		Transforms.register(UTM30N_TO_ACCRA_GEOGCS);
		
		Transforms.register(GNG_TO_UTM30N);
		Transforms.register(UTM30N_TO_GNG);
		
		
	}
}
