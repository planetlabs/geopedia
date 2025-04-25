package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.geometry.crs.CRS.WGS84;
import static com.sinergise.common.geometry.crs.transform.Transforms.compose;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsPix;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;

@SuppressWarnings("unused")
public class NgaTransforms {
	public static final TransverseMercator.UTM WGS84_UTM31N = (UTM)TransverseMercator.createWGS84N(31).setNiceName("UTM 31N (WGS84)");
	public static final TMToGeographic WUTM31N_TO_WGS84 = new TMToGeographic(WGS84_UTM31N);
	public static final GeographicToTM WGS84_TO_WUTM31N = WUTM31N_TO_WGS84.inverse();

	private static final DimI TILE_SIZE = new DimI(512, 512);
	private static final ZoomLevelsPix ZOOMS = ScaleLevelsSpec.createWithFactor2(0.125, 20, 5);
	private static final Envelope ENV = Envelope.withSize(450000, 450000, TILE_SIZE.w() * ZOOMS.maxWorldPerPix(), TILE_SIZE.h() * ZOOMS.maxWorldPerPix());
	public static final TiledCRS cs = new WithBounds(WGS84_UTM31N, "Nigeria WGS84/UTM31N Tiles", ZOOMS, ENV, TILE_SIZE);
	
	public static final Ellipsoidal MINNA = new Ellipsoidal(new CrsIdentifier(EPSG, 4263), Ellipsoid.CLARKE_1880_RGS);
	
	//epsg:1168
	public static final DatumConversion DC_MINNA_TO_WGS84_TRANSL = new DatumConversion.GeocentricTranslation(MINNA, WGS84, -92, -93, 122);
	public static final DatumConversion DC_WGS84_TO_MINNA_TRANSL = DC_MINNA_TO_WGS84_TRANSL.inverse();

	public static final TransverseMercator NNO_WEST = (TransverseMercator)new TransverseMercator(MINNA, 4, 4.5, 0.99975, new CrsIdentifier(EPSG, 26391), Envelope.getEmpty()).setOffset(230738.266, 0).setNiceName("NNO (Minna West Belt)");
	public static final TransverseMercator NNO_MID = new TransverseMercator(MINNA, 4, 8.5, 0.99975, new CrsIdentifier(EPSG, 26392), Envelope.getEmpty()).setOffset(670553.984, 0);
	public static final TransverseMercator NNO_EAST = new TransverseMercator(MINNA, 4, 12.5, 0.99975, new CrsIdentifier(EPSG, 26393), Envelope.getEmpty()).setOffset(1110369.702, 0);
	
	private static final TMToGeographic NNO_WEST_TO_MINNA = new TMToGeographic(NNO_WEST);
	private static final GeographicToTM MINNA_TO_NNO_WEST = NNO_WEST_TO_MINNA.inverse();
	private static final TMToGeographic NNO_MID_TO_MINNA = new TMToGeographic(NNO_MID);
	private static final TMToGeographic NNO_EAST_TO_MINNA = new TMToGeographic(NNO_EAST);

	private static final double MINNA_SWITCH_WEST_M = 0.5*(NNO_WEST.offX + NNO_MID.offX);
	private static final double MINNA_SWITCH_EAST_M = 0.5*(NNO_MID.offX + NNO_EAST.offX);
	private static final double MINNA_LONSWITCH_WEST_DEG = 0.5*(NNO_WEST.lam0_deg + NNO_MID.lam0_deg);
	private static final double MINNA_LONSWITCH_EAST_DEG = 0.5*(NNO_MID.lam0_deg + NNO_EAST.lam0_deg);
		
	public static final UTM MINNA_UTM31N = (UTM)TransverseMercator.createNorth(MINNA, 26300, 31).setNiceName("UTM 31N (Minna)");
	private static final GeographicToTM MINNA_TO_MUTM31N = new GeographicToTM(MINNA_UTM31N);
	private static final TMToGeographic MUTM31N_TO_MINNA = MINNA_TO_MUTM31N.inverse();
	
	public static final UTM MINNA_UTM32N = (UTM)TransverseMercator.createNorth(MINNA, 26300, 32).setNiceName("UTM 32N (Minna)");
	public static final UTM MINNA_UTM33N = (UTM)TransverseMercator.createNorth(MINNA, 26300, 33).setNiceName("UTM 33N (Minna)");
	
	private static final Transform<Ellipsoidal, TransverseMercator> WGS84_TO_NNO_WEST = compose(DC_WGS84_TO_MINNA_TRANSL, MINNA_TO_NNO_WEST);
	private static final Transform<Ellipsoidal, TransverseMercator> WGS84_TO_MUTM31N = compose(DC_WGS84_TO_MINNA_TRANSL, MINNA_TO_MUTM31N);
	
	private static final Transform<TransverseMercator, TransverseMercator> WUTM31N_TO_NNO_WEST = compose(WUTM31N_TO_WGS84, WGS84_TO_NNO_WEST);
	private static final Transform<TransverseMercator, TransverseMercator> WUTM31N_TO_MUTM31N = compose(WUTM31N_TO_WGS84, WGS84_TO_MUTM31N);

	private static final Transform<TransverseMercator, Ellipsoidal> NNO_WEST_TO_WGS84 = compose(NNO_WEST_TO_MINNA, DC_MINNA_TO_WGS84_TRANSL);
	public static final Transform<TransverseMercator, TransverseMercator> NNO_WEST_TO_WUTM31N = compose(NNO_WEST_TO_WGS84, WGS84_TO_WUTM31N);
	private static final Transform<TransverseMercator, TransverseMercator> NNO_WEST_TO_MUTM31N = compose(NNO_WEST_TO_MINNA, MINNA_TO_MUTM31N);
	
	private static final Transform<TransverseMercator, Ellipsoidal> MUTM31N_TO_WGS84 = compose(MUTM31N_TO_MINNA, DC_MINNA_TO_WGS84_TRANSL);
	private static final Transform<TransverseMercator, TransverseMercator> MUTM31N_TO_WUTM31N = compose(MUTM31N_TO_WGS84, WGS84_TO_WUTM31N);
	private static final Transform<TransverseMercator, TransverseMercator> MUTM31N_TO_NNO_WEST = compose(MUTM31N_TO_MINNA, MINNA_TO_NNO_WEST);
	
	public static final TMToGeographic getNnoToMinnaBand(double nnoEasting) {
		if (nnoEasting < MINNA_SWITCH_WEST_M) {
			return NNO_WEST_TO_MINNA;
		}
		if (nnoEasting < MINNA_SWITCH_EAST_M) {
			return NNO_MID_TO_MINNA;
		}
		return NNO_EAST_TO_MINNA;
	}
	
	public static final GeographicToTM getMinnaToNnoBand(double minnaLon) {
		if (minnaLon < MINNA_LONSWITCH_WEST_DEG) {
			return NNO_WEST_TO_MINNA.inverse();
		}
		if (minnaLon < MINNA_LONSWITCH_EAST_DEG) {
			return NNO_MID_TO_MINNA.inverse();
		}
		return NNO_EAST_TO_MINNA.inverse();
	}
	
	public static void init() {
		CrsRepository.INSTANCE.add(WGS84_UTM31N);
		CrsRepository.INSTANCE.add(MINNA_UTM31N);
		CrsRepository.INSTANCE.add(MINNA_UTM32N);
		CrsRepository.INSTANCE.add(MINNA_UTM33N);
		CrsRepository.INSTANCE.add(NNO_WEST);
		CrsRepository.INSTANCE.add(NNO_MID);
		CrsRepository.INSTANCE.add(NNO_EAST);
	}
}
