/*
 *
 */
package com.sinergise.common.geometry.crs;

import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.settings.Settings;

public class Ellipsoid implements Settings {
    public static final Ellipsoid WGS84=new Ellipsoid("WGS84", 6378137, 6356752.3142, 298.257223563);
    public static final Ellipsoid GRS80=new Ellipsoid("GRS 80", 6378137, 6356752.3141, 298.257222101);
    public static final Ellipsoid BESSEL_1841=new Ellipsoid("Bessel 1841",6377397.155,6356078.963,299.1528128153513233);
    public static final Ellipsoid AIRY_1830=new Ellipsoid("Airy 1830",6377563.396, 299.3249646);
    public static final Ellipsoid INTERNATIONAL_1924=new Ellipsoid("International 1924",6378388.0, 297.0);
    public static final Ellipsoid CLARKE_1880_RGS=new Ellipsoid("EPSG:7012 Clarke 1880 (RGS)",6378249.145, 293.465);
    public static final Ellipsoid CLARKE_1866=new Ellipsoid("EPSG:7008 Clarke 1866",6378206.400, 294.97870);
    public static final Ellipsoid KRASSOWSKY_1940 = new Ellipsoid("Krassowsky 1940", 6378245, 298.3);
    
    public final String id;
    public final double a;
    public final double b;
    public final double fInv;

    public transient double e;
    public transient double[] eSq;
    public transient double[] ePrimeSq;
    // epsilon as defined by OGC guidance note 7#2
    public transient double[] eps;
    
    // Used for new TM projection
    public transient double[] MFacts;
    public transient double[] FFacts;
    private transient double e1;
    public transient double muFact;
    
    protected Ellipsoid() {
    	id=null;
    	a = Double.NaN;
    	b = Double.NaN;
    	fInv = Double.NaN;
    }
    
    public Ellipsoid(String id, double a, double invFlat) {
        this(id, a,a*(1-1/invFlat),invFlat);
    }

    public Ellipsoid(String id, double a, double b, double invFlat) {
        this.id=id;
        this.a=a;
        this.b=b;
        this.fInv=invFlat;
        updateTransient();
    }
    
    protected void updateTransient() {
        double fInvMin1=fInv-1.0;
        ePrimeSq=MathUtil.powers((2*fInv-1)/(fInvMin1*fInvMin1),4);
        
        eSq=MathUtil.powers((2*fInv-1)/(fInv*fInv),6);
        e = Math.sqrt(eSq[1]);
        
        MFacts = new double[] {
        	 (1 - 0.25*eSq[1] - 3*eSq[2]/64 - 5*eSq[3]/256),
        	-(3*eSq[1]/8 + 3*eSq[2]/32 + 45*eSq[3]/1024),
        	 (15*eSq[2]/256 + 45*eSq[3]/1024),
        	-(35*eSq[3]/3072)
        };
        
        muFact=(a*(1-0.25*eSq[1]-3*eSq[2]/64-5*eSq[3]/256));
        
        e1=(1-Math.sqrt(1-eSq[1]))/(1+Math.sqrt(1-eSq[1]));
        double[] e1s = MathUtil.powers(e1, 4);
        FFacts = new double[] {
        	1.5*e1s[1] - 27*e1s[3]/32,
        	21*e1s[2]/16 - 55*e1s[4]/32,
        	151*e1s[3]/96,
        	1097*e1s[4]/512
        };
    }
    
    /**
     * http://kom.aau.dk/~borre/masters/frames/notes.pdf
     * 
     * @param radLat northing latitude expressed in radians
     * @return M - the radius of curvature in the direction of the meridian (south-north axis) 
     */
    public double M(double radLat) {
    	double sinLat=Math.sin(radLat);
    	return a*(1-eSq[1])/Math.pow(1-eSq[1]*sinLat*sinLat, 1.5);
    }
    
    /**
     * http://kom.aau.dk/~borre/masters/frames/notes.pdf
     * 
     * @param radLat northing latitude expressed in radians
     * @return p - the radius of the parallel at the provided latitude
     */
    public double p(double radLat) {
    	double cosLat=Math.cos(radLat);
    	return a*Math.sqrt(1+ePrimeSq[1])*cosLat/Math.sqrt(1+ePrimeSq[1]*cosLat*cosLat);
    }
}
