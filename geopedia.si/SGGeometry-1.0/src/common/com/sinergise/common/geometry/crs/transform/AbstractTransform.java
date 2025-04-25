/*
 *
 */
package com.sinergise.common.geometry.crs.transform;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;

public abstract class AbstractTransform<S extends CRS, T extends CRS> implements Transform<S,T> {
	public static interface ToCartesian<A extends CRS, B extends CartesianCRS> extends Transform<A,B> {
		double x(double src0, double src1);
		double y(double src0, double src1);
	}
	
	public static interface ToLatLon<A extends CRS, B extends LatLonCRS> extends Transform<A,B> {
		double lat(double src0, double src1);
		double lon(double src0, double src1);
	}
	
	public static class Identity<A extends CRS> extends AbstractTransform<A,A> {
		public Identity(A src) {
			super(src, src);
		}
		@Override
		public Point point(Point src, Point ret) {
			ret.setFrom(src);
			return ret;
		}
	}
	
	public static class IdentityLatLon<A extends LatLonCRS> extends Identity<A> implements ToLatLon<A, A> {
		public IdentityLatLon(A src) {
			super(src);
		}
		@Override
		public double lat(double arg0, double arg1) {
			return arg0;
		}
		@Override
		public double lon(double arg0, double arg1) {
			return arg1;
		}
	}
	
	public static class IdentityCartesian<A extends CartesianCRS> extends Identity<A> implements ToCartesian<A, A> {
		public IdentityCartesian(A src) {
			super(src);
		}
		@Override
		public double x(double arg0, double arg1) {
			return arg0;
		}
		@Override
		public double y(double arg0, double arg1) {
			return arg1;
		}
	}
	
	public static class Swap<A extends CRS> extends AbstractTransform<A, A> implements InvertibleTransform<A, A>, EnvelopeTransform {
		public Swap(A src) {
			super(src, src);
		}
		
		@Override
		public Point point(Point src, Point ret) {
			final double temp = src.x;
			ret.x = src.y;
			ret.y = temp;
			ret.z = src.z;
			return ret;
		}
		
		@Override
		public Envelope envelope(Envelope src) {
			EnvelopeBuilder builder = new EnvelopeBuilder(target.getDefaultIdentifier());
			builder.setMBR(src.getMinY(), src.getMinX(), src.getMaxY(), src.getMaxX());
			return builder.getEnvelope();
		}
		
		@Override
		public InvertibleTransform<A, A> inverse() {
			return this;
		}
	}
	
	public static class SwapLatLon<A extends LatLonCRS> extends Swap<A> implements ToLatLon<A, A> {
		public SwapLatLon(A src) {
			super(src);
		}
		@Override
		public double lat(double lat, double lon) {
			return lon;
		}
		@Override
		public double lon(double lat, double lon) {
			return lat;
		}
	}
	
	public static class SwapXY<A extends CartesianCRS> extends Swap<A> implements ToCartesian<A, A> {
		public SwapXY(A src) {
			super(src);
		}
		@Override
		public double x(double x, double y) {
			return y;
		}
		@Override
		public double y(double x, double y) {
			return x;
		}
	}
	
	public static class Reverse<A extends CRS> extends AbstractTransform<A, A> {
		protected final boolean first;
		protected final boolean second;
		public Reverse(A src, boolean first, boolean second) {
			super(src, src);
			this.first = first;
			this.second = second;
		}
		
		@Override
		public Point point(Point src, Point ret) {
			ret.x = first ? -src.x : src.x;
			ret.y = second ? -src.y : src.y;
			ret.z = src.z;
			return ret;
		}
	}
	
	public static class ReverseLatLon<A extends LatLonCRS> extends Reverse<A> implements ToLatLon<A, A> {
		public ReverseLatLon(A src, boolean first, boolean second) {
			super(src, first, second);
		}
		@Override
		public double lat(double lat, double lon) {
			return first ? -lat : lat;
		}
		@Override
		public double lon(double lat, double lon) {
			return second ? -lon : lon;
		}
	}
	
	public static class ReverseXY<A extends CartesianCRS> extends Reverse<A> implements ToCartesian<A, A> {
		public ReverseXY(A src, boolean first, boolean second) {
			super(src, first, second);
		}
		@Override
		public double x(double x, double y) {
			return first ? -x : x;
		}
		@Override
		public double y(double x, double y) {
			return second ? -y : y;
		}
	}
	
    protected transient S source;
    protected transient T target;
	protected String name;
	
    public AbstractTransform(S source, T target) {
        this.source=source;
        this.target=target;
        Transforms.register(this);
    }
    
    public AbstractTransform<S, T> setName(String name) {
    	this.name = name;
    	return this;
    }
    
    @Override
	public String getName() {
    	return name;
    }

    @Override
	public S getSource() {
    	return source;
    }
    @Override
	public T getTarget() {
    	return target;
    }
    
    public Point point(Point src) {
    	return point(src, new Point());
    }
    
    protected void updateCrsReference(Point tgtPt) {
    	tgtPt.setCrsId(target.getDefaultIdentifier());
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AbstractTransform)) {
			return false;
		}
		AbstractTransform<?,?> other = (AbstractTransform<?,?>)obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}
}
