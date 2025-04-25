/**
 * 
 */
package com.sinergise.common.geometry.crs;

import com.sinergise.common.geometry.crs.CartesianCRS.ProjectedEllipsoidalCRS;
import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;


public class EquidistantCylindrical extends ProjectedEllipsoidalCRS {
	public static class GeographicToEC extends LatLonToCartesian<Ellipsoidal, EquidistantCylindrical> {
	    public GeographicToEC(EquidistantCylindrical proj) {
	        super(proj.sourceCRS, proj);
	    }
	    
	    @Override
		public Point point(Point src, Point ret) {
	    	EquidistantCylindrical proj=target;
	    	
	        double lam=src.y*MathUtil.DEGREE_IN_RAD;
	        double lam0=proj.lam0_deg*MathUtil.DEGREE_IN_RAD;
	        ret.x = proj.falseX+proj.R*(lam-lam0)*proj.cosFi0;
	        
	        double fi=src.x*MathUtil.DEGREE_IN_RAD;
	        ret.y = proj.falseY+proj.R*fi;
	        
	        ret.z = src.z;
	        updateCrsReference(ret);
	    	return ret;
	    }
	    
	    @Override
		public double x(double lat, double lon) {
    		EquidistantCylindrical proj=target;
	        double lam=lon*MathUtil.DEGREE_IN_RAD;
	        double lam0=proj.lam0_deg*MathUtil.DEGREE_IN_RAD;
	        return proj.falseX+proj.R*(lam-lam0)*proj.cosFi0;
		    }
		    @Override
			public double y(double lat, double lon) {
	    		EquidistantCylindrical proj=target;
	        double fi=lat*MathUtil.DEGREE_IN_RAD;
	        return proj.falseY+proj.R*fi;
	    }
	}
	public static class ECToGeographic extends CartesianToLatLon<EquidistantCylindrical, Ellipsoidal> {
	    
	    public ECToGeographic(EquidistantCylindrical projected) {
	        super(projected, projected.sourceCRS);
	    }
	
	    @Override
		public Point point(Point src, Point ret) {
	    	EquidistantCylindrical proj=source;
	    	double fi = (src.y-proj.falseY)/proj.R;
	    	ret.x = MathUtil.RAD_IN_DEGREES*fi;

	    	double lam0 = Math.toRadians(proj.lam0_deg);
	    	double lam = lam0+(src.x-proj.falseX)/(proj.R*proj.cosFi0);

	    	ret.y = MathUtil.RAD_IN_DEGREES*lam;
	    	ret.z = src.z;
	    	updateCrsReference(ret);
	    	return ret;
	    }
	    
	    @Override
		public double lat(double E, double N) {
	    	EquidistantCylindrical proj=source;
	    	double fi = (N-proj.falseY)/proj.R;
	    	return MathUtil.RAD_IN_DEGREES*fi;
	    }
	
	    @Override
		public double lon(double E, double N) {
	    	EquidistantCylindrical proj=source;
	    	double lam0 = Math.toRadians(proj.lam0_deg);
	    	double lam = lam0+(E-proj.falseX)/(proj.R*proj.cosFi0);
	    	return MathUtil.RAD_IN_DEGREES*lam;
	    }
	}
	public double fi0_deg;
	public double lam0_deg;
	
	/**
	 * As defined in http://www.epsg.org/guides/docs/G7-2.pdf
	 */
	public transient double cosFi0;
	public transient double R;
	double falseX=0;
	double falseY=0;
	
	public EquidistantCylindrical(Ellipsoidal sourceCRS, CrsIdentifier id) {
		this(sourceCRS, id, null,0,0);
	}
	public EquidistantCylindrical(Ellipsoidal sourceCRS, CrsIdentifier id, Envelope bounds, double lat0_deg, double lon0_deg) {
		super(sourceCRS, id, bounds);
		setOrigin(lat0_deg, lon0_deg);
	}
	public EquidistantCylindrical setOrigin(double lat0_deg, double lon0_deg) {
		this.fi0_deg=lat0_deg;
		this.lam0_deg=lon0_deg;
		updateTransient();
		return this;
	}
	
	protected void updateTransient() {
		cosFi0=Math.cos(fi0_deg*MathUtil.DEGREE_IN_RAD);
		
		Ellipsoid el=(sourceCRS).ellipsoid;
		double sinFi0=Math.sin(fi0_deg*MathUtil.DEGREE_IN_RAD);
		double sinSqFi0=sinFi0*sinFi0;
		double div=1-el.eSq[1]*sinSqFi0;
		R=Math.sqrt(el.a*el.a*(1-el.eSq[1])/(div*div));
	}
	/**
	 * @param i
	 * @param j
	 */
	public EquidistantCylindrical setFalseCenter(int falseX, int falseY) {
		this.falseX=falseX;
		this.falseY=falseY;
		return this;
	}
}