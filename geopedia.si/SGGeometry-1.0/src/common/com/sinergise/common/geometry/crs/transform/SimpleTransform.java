/*
 *
 */
package com.sinergise.common.geometry.crs.transform;

import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.CartesianToCartesian;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;


public abstract class SimpleTransform<S extends CartesianCRS, T extends CartesianCRS> extends CartesianToCartesian<S,T> {
	public static class TranslationScale<S extends CartesianCRS, T extends CartesianCRS> extends SimpleTransform<S,T> {
		protected double offX;
		protected double offY;
		protected double scale;
		public TranslationScale(S src, T tgt, double offX, double offY) {
			this(src, tgt, offX, offY, 1);
		}
		public TranslationScale(S src, T tgt, double offX, double offY, double scale) {
			super(src, tgt);
			this.offX=offX;
			this.offY=offY;
			this.scale=scale;
		}
		@Override
		public double length(double arg0) {
			return scale*arg0;
		}
		@Override
		public double x(double arg0) {
			return arg0*scale+offX;
		}
		@Override
		public double y(double arg0) {
			return arg0*scale+offY;
		}
	}
	
    public abstract static class ToInt<S extends CartesianCRS, T extends CartesianCRS> extends SimpleTransform<S,T> {
        
        public ToInt(S source, T target) {
            super(source, target);
        }
        
        public int lengthInt(double sourceLength) {
            return (int)Math.round(length(sourceLength));
        }
        
        public EnvelopeI rectInt(Envelope sourceRect) {
            return rectInt(sourceRect.getMinX(),sourceRect.getMinY(),sourceRect.getMaxX(),sourceRect.getMaxY());
        }

        public EnvelopeI rectInt(double sourceMinX, double sourceMinY, double sourceMaxX, double sourceMaxY) {
			return EnvelopeI.withPoints(//
				xInt(sourceMinX),//
				yInt(sourceMinY),//
				xInt(sourceMaxX),//
				yInt(sourceMaxY));
        }
        
        public PointI pointInt(double sourceX, double sourceY) {
            return new PointI(xInt(sourceX),yInt(sourceY));
        }

        public PointI pointInt(Point sourcePoint) {
            return pointInt(sourcePoint.x, sourcePoint.y);
        }

        public int xInt(double sourceX) {
            return (int)Math.round(x(sourceX));
        }

        public int yInt(double sourceY) {
            return (int)Math.round(y(sourceY));
        }
    }
    
    public abstract static class FromInt<S extends CartesianCRS, T extends CartesianCRS> extends SimpleTransform<S,T> {
        
        public FromInt(S source, T target) {
            super(source, target);
        }
        
        public Point point(PointI sourcePoint) {
        		Point ret=new Point();
            return super.point(new Point(sourcePoint.x, sourcePoint.y), ret);
        }
        public Envelope rect(EnvelopeI sourceRect) {
            return super.rect(sourceRect.minX(), sourceRect.minY(), sourceRect.maxX(), sourceRect.maxY());
        }
        public double x(int sourceX) {
            return x((double)sourceX);
        }
        public double y(int sourceY) {
            return y((double)sourceY);
        }
        public Point point(int x, int y) {
        	return point((double)x, (double)y);
        }
    }
    
    public SimpleTransform(S source, T target) {
        super(source, target);
    }
    
    /**
     * @param worldArea 
     * @return area in (square) pixels
     */
    public double area(double sourceArea) {
        return sourceArea * getScaleSquared();
    }
    @Override
	public Point point(Point src, Point ret) {
  		ret.x=x(src.x);
  		ret.y=y(src.y);
  		ret.z=src.z;
  		updateCrsReference(ret);
    	return ret;
    }
    
    public Point point(double x, double y) {
    	return point(new Point(x,y), new Point());
    }

    public Envelope rect(Envelope sourceRect) {
        return rect(sourceRect.getMinX(),sourceRect.getMinY(),sourceRect.getMaxX(),sourceRect.getMaxY());
    }
    
    public Envelope rect(double sourceMinX, double sourceMinY, double sourceMaxX, double sourceMaxY) {
        return new Envelope(x(sourceMinX), y(sourceMinY), x(sourceMaxX), y(sourceMaxY));
    }
    
    public double getInverseScale() {
        return 1.0 / length(1);
    }
    public double getInverseScaleSquared() {
        double invSc=getInverseScale();
        return invSc*invSc;
    }
    /**
     * @return the scale ratio of the transform (length of source unit in target space)
     */
    public double getScale() {
        return length(1);
    }

    public double getScaleSquared() {
        double sc=getScale();
        return sc*sc;
    }
    
    @Override
	public double x(double x, double y) {
        return this.x(x);
    }
		@Override
		public double y(double x, double y) {
        return this.y(y);
    }

    public abstract double length(double sourceLength);

    public abstract double x(double sourceX);
    
    public abstract double y(double sourceY);
}
