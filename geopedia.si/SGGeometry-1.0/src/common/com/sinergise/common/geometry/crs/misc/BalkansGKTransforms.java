package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.geometry.crs.CRS.MGI_BESSEL_ELLIPSOIDAL;
import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.TMToGeographic;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;

public class BalkansGKTransforms {
	public static final TransverseMercator createBalkansGK(int zone) {
		double minX = zone*1000000.0;
		Envelope env = new Envelope(minX, 0, minX+1000000, 10000000);
		TransverseMercator ret = new TransverseMercator(MGI_BESSEL_ELLIPSOIDAL, 0, 3*zone, 0.9999, new CrsIdentifier(EPSG, 3902+zone), env);
		ret.setOffset(minX + 500000.0, 0);
		ret.setNiceName("MGI 1901 / Balkans zone "+zone);
		return ret;
	}
	
	public static final TransverseMercator GK5 = createBalkansGK(5);
	public static final TMToGeographic GK5_TO_MGI = GK5.createTransformToGeographic();
	public static final GeographicToTM MGI_TO_GK5 = GK5.createTransformFromGeographic();

	public static final TransverseMercator GK6 = createBalkansGK(6);
	public static final TMToGeographic GK6_TO_MGI = GK6.createTransformToGeographic();  
	public static final GeographicToTM MGI_TO_GK6 = GK6.createTransformFromGeographic();

	public static final TransverseMercator GK7 = createBalkansGK(7);
	public static final TMToGeographic GK7_TO_MGI = GK7.createTransformToGeographic();  
	public static final GeographicToTM MGI_TO_GK7 = GK7.createTransformFromGeographic();

	public static final TransverseMercator GK8 = createBalkansGK(8);
	public static final TMToGeographic GK8_TO_MGI = GK8.createTransformToGeographic();  
	public static final GeographicToTM MGI_TO_GK8 = GK8.createTransformFromGeographic();
}
