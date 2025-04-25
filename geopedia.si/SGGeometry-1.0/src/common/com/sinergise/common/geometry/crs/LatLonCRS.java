/*
 *
 */
package com.sinergise.common.geometry.crs;

import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;


public class LatLonCRS extends CRS {
    public static class Ellipsoidal extends LatLonCRS {
        public final Ellipsoid ellipsoid;
        
        public Ellipsoidal() {
        	ellipsoid = null;
		}
        
        public Ellipsoidal(CrsIdentifier id, Ellipsoid ellipsoid) {
        	this(id, ellipsoid, new Envelope(-90,-180,90,180));
        }
        
        public Ellipsoidal(CrsIdentifier id, Ellipsoid ellipsoid, Envelope bounds) {
            super(id, bounds);
            this.ellipsoid=ellipsoid;
        }
    }
    @Deprecated
    protected LatLonCRS() {
    	super();
		}
    
    public LatLonCRS(CrsIdentifier id) {
        this(id, new Envelope(-90,-180,90,180));
    }
    public LatLonCRS(CrsIdentifier id, Envelope bounds) {
        super(id, bounds);
        setCoordNames(new String[]{"Lat","Lon"});
    }
}
