package com.sinergise.common.geometry.crs.mu;

import static com.sinergise.common.geometry.crs.mu.MauritiusTransforms.GEO_GDM_2008;
import static com.sinergise.common.geometry.crs.mu.MauritiusTransforms.GEO_LE_POUCE_1934;
import static com.sinergise.common.geometry.crs.mu.MauritiusTransforms.GEO_MAURITIUS_1994;
import static com.sinergise.common.geometry.crs.mu.MauritiusTransforms.LEPOUCE1934_TO_MUSGRID;
import static com.sinergise.common.geometry.crs.mu.MauritiusTransforms.MUSGRID_TO_LEPOUCE1934;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;
import static com.sinergise.common.util.math.MathUtil.SEC_IN_RAD;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS.PseudoPlateCarree;
import com.sinergise.common.geometry.crs.DatumConversion;
import com.sinergise.common.geometry.crs.EquidistantCylindrical;
import com.sinergise.common.geometry.crs.EquidistantCylindrical.ECToGeographic;
import com.sinergise.common.geometry.crs.EquidistantCylindrical.GeographicToEC;
import com.sinergise.common.geometry.crs.LambertConicConformal;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToCartesian;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.ToLatLon;
import com.sinergise.common.geometry.crs.transform.SimpleTransform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;

public class MusObsolete {
	/**
	 * EPSG:15784 Accuracy: 2m
	 */
	public static final DatumConversion DC_MUS1994_TO_LEPOUCE1934 = new DatumConversion.GeocentricTranslation(GEO_MAURITIUS_1994, GEO_LE_POUCE_1934,
		770.126,-158.383,498.232);

	public static final DatumConversion DC_MUS1994_TO_LEPOUCE1934_7 = new DatumConversion.PositionVector7Params(GEO_MAURITIUS_1994, GEO_LE_POUCE_1934,
  	new double[]{-91.824,-292.222,-115.604},
  	new double[]{1.01*SEC_IN_RAD,-19.74*SEC_IN_RAD,-22.14*SEC_IN_RAD},23.15822);

	public static final DatumConversion DC_LEPOUCE1934_TO_MUS1994 = DC_MUS1994_TO_LEPOUCE1934.inverse();

  public static final EquidistantCylindrical MUS_PLATE_CARREE = new EquidistantCylindrical(CRS.WGS84, new CrsIdentifier(EPSG, -1243), new Envelope(500000,7700000,600000,7850000), 0, 57).setFalseCenter(500000, 10000000);

	public static final DatumConversion DC_GDM2008_TO_LEPOUCE1934_NOZ = new DatumConversion.GeneralMatrix(GEO_GDM_2008, MauritiusTransforms.GEO_LE_POUCE_1934,
	new double[][]{{0.9980811528414311, -0.002919225339577357, 0.0012294545943565016, 24362.964824119474}, 
	   {-0.0011676613538114493, 0.9982879687315748, 0.0007320960035390214, 13833.349009180582}, 
	   {-0.00514035504972221, -0.008272576702352097, 1.0035966533456167, 66693.89560837236}});
	public static final DatumConversion DC_LEPOUCE1934_TO_GDM2008_NOZ = new DatumConversion.GeneralMatrix(MauritiusTransforms.GEO_LE_POUCE_1934, MauritiusTransforms.GEO_GDM_2008,
	new double[][]{{1.001920114707811, 0.0029204380078006226, -0.0012298717883919732, -24374.40813301447}, 
	   {0.0011680337319534425, 1.001712150317513, -0.0007320799781498005, -13835.33171310654}, 
	   {0.005141890373593014, 0.008272781681800876, 0.9964035610760146, -66700.04798477177}}, DC_GDM2008_TO_LEPOUCE1934_NOZ);
	//		public static final DatumConversion DC_MUS1994_TO_LEPOUCE1934 = new DatumConversion.Identity(GEO_MAURITIUS_1994, GEO_LE_POUCE_1934);
	public static final GeographicToEC WGS84_TO_MUS_PLATE_CARREE = new GeographicToEC(MUS_PLATE_CARREE);
	public static final ToCartesian<PseudoPlateCarree<Ellipsoidal>, EquidistantCylindrical> PPC_TO_MUS_PLATE_CARREE = Transforms.compose(Transforms.PSEUDO_PLATE_CARREE_TO_WGS84, WGS84_TO_MUS_PLATE_CARREE);
	public static final ToLatLon<LambertConicConformal, Ellipsoidal> MUSGRID_TO_MUS_1994 = Transforms.compose(MUSGRID_TO_LEPOUCE1934, DC_LEPOUCE1934_TO_MUS1994);
	public static final ToCartesian<LambertConicConformal, EquidistantCylindrical> MUSGRID_TO_MUS_PLATE_CARREE = 
	Transforms.compose(
			Transforms.compose(MUSGRID_TO_MUS_1994, WGS84_TO_MUS_PLATE_CARREE),
			new SimpleTransform.TranslationScale<EquidistantCylindrical, EquidistantCylindrical>(MUS_PLATE_CARREE, MUS_PLATE_CARREE, -139, -204, 1)
		);
	
	
	public static final ToCartesian<Ellipsoidal, LambertConicConformal> MUS_1994_TO_MUSGRID = Transforms.compose(DC_MUS1994_TO_LEPOUCE1934, LEPOUCE1934_TO_MUSGRID);
	public static final ECToGeographic MUS_PLATE_CARREE_TO_WGS84 = new ECToGeographic(MUS_PLATE_CARREE);
	public static final ToCartesian<EquidistantCylindrical, LambertConicConformal> MUS_PLATTE_CARREE_TO_MUSGRID = Transforms.compose(MUS_PLATE_CARREE_TO_WGS84, MUS_1994_TO_MUSGRID);
	
	
}
