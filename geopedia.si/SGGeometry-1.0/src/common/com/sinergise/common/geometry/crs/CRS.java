package com.sinergise.common.geometry.crs;

import static com.sinergise.common.util.collections.CollectionUtil.firstOrNullIfEmpty;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.CRS_AUTH;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;
import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sinergise.common.geometry.crs.CartesianCRS.PseudoPlateCarree;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.settings.Settings.TypeMap;

@TypeMap(
	names = {"TM","UTM"},
	types = {TransverseMercator.class, UTM.class}
	)
@SuppressWarnings("unchecked")
public class CRS {

	public static final Ellipsoidal WGS84_ELLIPSOIDAL = (Ellipsoidal)(new Ellipsoidal(new CrsIdentifier(EPSG, 4326), Ellipsoid.WGS84)).setNiceName("WGS 84").registerIdentifier(new CrsIdentifier(CRS_AUTH, 84)).registerIdentifier(new CrsIdentifier(CrsAuthority.POSTGIS, 4326));
	public static final Ellipsoidal OSGB1936_ELLIPSOIDAL = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 4277), Ellipsoid.AIRY_1830, new Envelope(-7.56,49.96,1.78,60.84)).setNiceName("OSGB 1936");
	public static final Ellipsoidal MGI_BESSEL_ELLIPSOIDAL = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 3906), Ellipsoid.BESSEL_1841, new Envelope(9.53,40.84,23,49.02)).setNiceName("MGI 1901");
	public static final Ellipsoidal ETRS89_ELLIPSOIDAL = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 4258), Ellipsoid.GRS80, new Envelope(-10.67, 34.5, 31.55, 71.05)).setNiceName("ETRS 89");

	public static final CartesianCRS NONAME_WORLD_CRS = new CartesianCRS(new CrsIdentifier("Default"), null);

	// ---------------------
	// GLOBAL
	// ---------------------

	public static final Ellipsoidal WGS84 = CRS.WGS84_ELLIPSOIDAL;
	/**
	 *  http://lists.eogeo.org/pipermail/wms-dev/2001-July/000015.html
	 */
	public static final PseudoPlateCarree<Ellipsoidal> WGS84_GLOBAL_PSEUDO_PLATTE_CARRE = (PseudoPlateCarree<Ellipsoidal>)(new PseudoPlateCarree<Ellipsoidal>(WGS84, new CrsIdentifier(EPSG, 32662)).setNiceName("Pseudo Plate Carrée"));
	public static final PseudoPlateCarree<Ellipsoidal> WGS84_GLOBAL_PSEUDO_PLATTE_CARRE_SEC = (PseudoPlateCarree<Ellipsoidal>)(new PseudoPlateCarree<Ellipsoidal>(WGS84, new CrsIdentifier(EPSG, 326620), 1).setNiceName("Pseudo Plate Carrée Seconds"));
	public static final EquidistantCylindrical WGS84_GLOBAL_PLATTE_CARRE = (EquidistantCylindrical)new EquidistantCylindrical(WGS84, new CrsIdentifier(EPSG, 32663)).setNiceName("Plate Carrée");

	/**
	 * NOTE: EPSG code is for the ellipsoidal CRS
	 */
	public static final PseudoPlateCarree<Ellipsoidal> ETRS89_GLOBAL_PSEUDO_PLATTE_CARRE = (PseudoPlateCarree<Ellipsoidal>)(new PseudoPlateCarree<Ellipsoidal>(ETRS89_ELLIPSOIDAL, new CrsIdentifier(EPSG, 4258)).setNiceName("Pseudo Plate Carrée").registerIdentifier(new CrsIdentifier(CrsAuthority.POSTGIS, 4258)));


	// ----------------------
	// UNITED KINGDOM
	// ----------------------

	public static final TransverseMercator BNG = (TransverseMercator)new TransverseMercator(
		CRS.OSGB1936_ELLIPSOIDAL,
		49,-2,0.9996012717, new CrsIdentifier(EPSG, 27700), 
		new Envelope(0,0e3,700e3,1300e3)).setOffset(400000, -100000).setNiceName("British National Grid");

	// ----------------------
	// SLOVENIA
	// ----------------------


	// EPSG:3911 // Was 2170, but changed by change request EPSG::2009.015
	public static final TransverseMercator SI_D48=(TransverseMercator)new TransverseMercator(CRS.MGI_BESSEL_ELLIPSOIDAL,0,15,0.9999, new CrsIdentifier(EPSG, 3911), new Envelope(350e3,5000e3,650e3,5200e3)).setOffset(500000, 0).setCoordNames(new String[]{"Y","X"}).setNiceName("SI D48")
		.registerIdentifier(new CrsIdentifier(CrsAuthority.POSTGIS, 3911));
	// EPSG:3912 //Was 3787, but changed by change request EPSG::2009.015
	public static final TransverseMercator D48_GK = (TransverseMercator)new TransverseMercator(CRS.MGI_BESSEL_ELLIPSOIDAL,0,15,0.9999, new CrsIdentifier(EPSG, 3912), new Envelope(350e3,0,650e3,200e3)).setOffset(500000, -5000000).setCoordNames(new String[]{"Y","X"}).setNiceName("D48/GK")
		.registerIdentifier(new CrsIdentifier(EPSG, 3787))
		.registerIdentifier(new CrsIdentifier(EPSG, 217000))
		.registerIdentifier(new CrsIdentifier(CrsAuthority.POSTGIS, 3912));
	// GURS old Slo (217000)
	public static final TransverseMercator D48_GK_GURS = D48_GK;
	// EPSG 3787
	public static final TransverseMercator D48_GK_3787 = D48_GK;
	// EPSG:3794
	public static final TransverseMercator D96_TM=(TransverseMercator)new TransverseMercator(CRS.ETRS89_ELLIPSOIDAL,0,15,0.9999, new CrsIdentifier(EPSG, 3794), new Envelope(350e3,0,650e3,200e3)).setOffset(500000, -5000000).setNiceName("D96/TM").registerIdentifier(new CrsIdentifier(CrsAuthority.POSTGIS, 3794));

	// -----------------------
	// SPECIAL
	// -----------------------

	public static final CartesianCRS MAP_CRS=(CartesianCRS)new CartesianCRS(new CrsIdentifier(CrsAuthority.CRS_AUTH, 0), null).setNiceName("Map");
	public static final CartesianCRS MAP_PIXEL_CRS=(CartesianCRS)new CartesianCRS(new CrsIdentifier(CrsAuthority.CRS_AUTH, -2), null).setNiceName("Map Pixels");


	public Envelope bounds2D; 
	private LinkedHashSet<CrsIdentifier> ids = new LinkedHashSet<CrsIdentifier>(1);

	@Deprecated // Serialization only
	protected CRS() { }

	public CRS(CrsAuthority auth, int id, Envelope bounds2D) {
		this(new CrsIdentifier(auth, id), bounds2D);
	}

	public CRS(CrsIdentifier defaultId, Envelope bounds2D) {
		ids.add(defaultId);
		this.bounds2D=bounds2D==null?new Envelope():bounds2D;
	}

	public CrsIdentifier getDefaultIdentifier() {
		return firstOrNullIfEmpty(ids);
	}

	public Set<CrsIdentifier> getIdentifiers() {
		return unmodifiableSet(ids);
	}

	public CrsIdentifier getIdentifier(CrsAuthority authority) {
		for (CrsIdentifier crsId : ids) {
			if (Util.safeEquals(authority, crsId.getAuthority())) {
				return crsId;
			}
		}
		return null;
	}

	public final CRS registerIdentifier(CrsIdentifier id) {
		ids.add(id);
		return this;
	}

	public String getCode() {
		return getDefaultIdentifier().getCode();
	}

	String[] coordNames;
	public String getCoordName(int coord) {
		return coordNames[coord];
	}

	public CRS setCoordNames(String[] names) {
		this.coordNames=names;
		return this;
	}

	public String niceName;
	public String shortName;

	public CRS setNiceName(String niceName) {
		this.niceName=niceName;
		return this;
	}

	public CRS setShortName(String shortName) {
		this.shortName=shortName;
		return this;
	}

	public String getNiceName() {
		if (niceName==null) return getDefaultIdentifier().getCode();
		return niceName;
	}

	public String getShortName() {
		if (shortName==null) return getNiceName();
		return shortName;
	}

	public String getNiceName(boolean useShortName) {
		if (useShortName) return getShortName();
		return getNiceName();
	}

	@Override
	public String toString() {
		return getDefaultIdentifier().getCode()+" - "+getNiceName();
	}

	@Override
	public int hashCode() {
		CrsIdentifier id = getDefaultIdentifier();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CRS))
			return false;
		CRS other = (CRS)obj;

		if (getDefaultIdentifier() == null) {
			if (other.getDefaultIdentifier() != null)
				return false;
		} else if (!getDefaultIdentifier().equals(other.getDefaultIdentifier()))
			return false;
		return true;
	}


}
