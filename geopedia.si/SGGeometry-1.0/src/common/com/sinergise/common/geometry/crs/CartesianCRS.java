/*
 *
 */
package com.sinergise.common.geometry.crs;

import static com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority.CRS_AUTH;

import com.sinergise.common.geometry.crs.LatLonCRS.Ellipsoidal;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;

public class CartesianCRS extends CRS {
    public static CartesianCRS createImageCRS(int w, int h) {
    	return (CartesianCRS)(new CartesianCRS(new CrsIdentifier(CRS_AUTH, 1), new Envelope(0,0,w,h)).setNiceName("Image pixels CRS"));
    }
    
    public static class ProjectedCRS<S extends LatLonCRS> extends CartesianCRS {
        public final S sourceCRS;
        
		@Deprecated
		//Serialization only
		protected ProjectedCRS() {
			super();
			sourceCRS = null;
		}

		public ProjectedCRS(S sourceCRS, CrsIdentifier ref, Envelope bounds) {
			super(ref, bounds);
			setCoordNames(new String[]{"E", "N"});
			this.sourceCRS = sourceCRS;
		}
    }
    
    public static class ProjectedEllipsoidalCRS extends ProjectedCRS<Ellipsoidal> {
        public ProjectedEllipsoidalCRS(Ellipsoidal sourceCRS, CrsIdentifier ref, Envelope bounds) {
            super(sourceCRS, ref, bounds);
        }

        @Deprecated // Serialization only
		protected ProjectedEllipsoidalCRS() {
			super();
		}
    }
    
    public static class PseudoPlateCarree<S extends LatLonCRS> extends ProjectedCRS<S> {
    	public final double unitInSeconds;
    	public PseudoPlateCarree(S sourceCRS, CrsIdentifier ref) {
    		this(sourceCRS, ref, 3600);
    	}
    	public PseudoPlateCarree(S sourceCRS, CrsIdentifier ref, double unitInSeconds) {
    		super(sourceCRS, ref, sourceCRS.bounds2D);
    		this.unitInSeconds=unitInSeconds;
    	}
    }
    
    public static class PseudoPlatteCarreeToLatLon<S extends LatLonCRS> extends CartesianToLatLon<PseudoPlateCarree<S>, S> {
    	
    	public PseudoPlatteCarreeToLatLon(PseudoPlateCarree<S> ppc) {
            super(ppc, ppc.sourceCRS);
        }
    	
    	@Override
		public Point point(Point src, Point ret) {
    		PseudoPlateCarree<S> ppc=source;
    		ret.x = src.y * ppc.unitInSeconds/3600.0;
    		ret.y = src.x*ppc.unitInSeconds/3600.0;
    		ret.x = src.z;
    		updateCrsReference(ret);
    		return ret;
    	}
    }
    
    public static class LatLonToPseudoPlatteCarree<T extends LatLonCRS> extends LatLonToCartesian<T, PseudoPlateCarree<T>> {
    	
    	public LatLonToPseudoPlatteCarree(PseudoPlateCarree<T> ppc) {
           super(ppc.sourceCRS, ppc);
      }
    	
    	@Override
		public Point point(Point src, Point ret) {
    		PseudoPlateCarree<T> ppc=target;
    		ret.x = src.y*3600/ppc.unitInSeconds;
    		ret.y = src.x*3600/ppc.unitInSeconds;
    		ret.z = src.z;
    		updateCrsReference(ret);
    		return ret;
    	}
    }
    
    @Deprecated //Serialization only
    protected CartesianCRS() {
    	super();
        setCoordNames(new String[]{"x","y","z"});
	}
    
    public CartesianCRS(CrsIdentifier ref, Envelope bounds) {
        super(ref, bounds);
        setCoordNames(new String[]{"x","y","z"});
    }
    
}
