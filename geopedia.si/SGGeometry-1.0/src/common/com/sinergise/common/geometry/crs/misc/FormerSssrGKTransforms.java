package com.sinergise.common.geometry.crs.misc;

import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.EPSG;

import com.sinergise.common.geometry.crs.Ellipsoid;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;

public class FormerSssrGKTransforms {
	public static final Ellipsoidal PULKOVO_1942 = (Ellipsoidal)new Ellipsoidal(new CrsIdentifier(EPSG, 4284), Ellipsoid.KRASSOWSKY_1940, new Envelope(35.15, 19.58, 81.9, 191.02)).setNiceName("Pulkovo 1942");

	public static final TransverseMercator create3degGK(int zone) {
		assert MathUtil.between(7, zone, 64);
		
		double minX = zone*1000000.0;
		Envelope env = new Envelope(minX, 0, minX+1000000, 10000000);
		
		TransverseMercator ret = new TransverseMercator(PULKOVO_1942, 0, 3*zone, 1, new CrsIdentifier(EPSG, 2516+zone), env);
		ret.setOffset(minX + 500000, 0);
		ret.setNiceName("Pulkovo 1942 / 3-deg GK zone "+zone);
		return ret;
	}

	public static final TransverseMercator create3degGK_CM(int lon0) {
		assert lon0 % 3 == 0;
		assert MathUtil.between(21, lon0, 180) || MathUtil.between(-177, lon0, -168);
		
		int crsId;
		if (lon0 >= 0) {
			crsId = 2575 + lon0/3;
			if (crsId > 2600) { //for some reason EPSG would rather skip an id than use on that's too round :)
				crsId++;
			}
		} else {
			crsId = 2696 + lon0/3;
		}
		TransverseMercator ret = new TransverseMercator(PULKOVO_1942, 0, lon0, 1, new CrsIdentifier(EPSG, crsId), new Envelope(0, 0, 1000000, 10000000));
		ret.setOffset(500000, 0);
		ret.setNiceName("Pulkovo 1942 / 3-deg GK CM "+ (lon0<0 ? (-lon0)+"W" : lon0+"E"));
		return ret;
	}
	
	public static final TransverseMercator create6degGK(int zone) {
		assert MathUtil.between(4, zone, 32);
		
		double minX = zone*1000000.0;
		Envelope env = new Envelope(minX, 0, minX+1000000, 10000000);
		
		TransverseMercator ret = new TransverseMercator(PULKOVO_1942, 0, 6*zone-3, 1, new CrsIdentifier(EPSG, 28400+zone), env);
		ret.setOffset(minX + 500000, 0);
		ret.setNiceName("Pulkovo 1942 / 6-deg GK zone "+zone);
		return ret;
	}
	
	public static final TransverseMercator create6degGK_CM(int lon0) {
		assert (lon0 + 3) % 6 == 0;
		assert MathUtil.between(21, lon0, 177) || MathUtil.between(-177, lon0, -171);
		
		int crsId;
		if (lon0 >= 0) {
			crsId = 2490 + (lon0+3)/6;
		} else {
			crsId = 2550 + (lon0+3)/6;
		}
		TransverseMercator ret = new TransverseMercator(PULKOVO_1942, 0, lon0, 1, new CrsIdentifier(EPSG, crsId), new Envelope(0, 0, 1000000, 10000000));
		ret.setOffset(500000, 0);
		ret.setNiceName("Pulkovo 1942 / 6-deg GK CM " + (lon0 < 0 ? (-lon0) + "W" : lon0 + "E"));
		return ret;
	}
	
	private FormerSssrGKTransforms() {
	}
}
