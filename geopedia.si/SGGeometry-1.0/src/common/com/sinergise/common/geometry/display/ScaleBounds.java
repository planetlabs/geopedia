/*
 *
 */
package com.sinergise.common.geometry.display;

import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.common.util.math.MathUtil;


public class ScaleBounds {
    public static class InPix extends ScaleBounds {
        public InPix(double min, double max) {
            super(min,max,true);
        }
        public InPix() {
            super();
            inPix=true;
        }
        @Override
		public void setFrom(ScaleBounds other) {
            if (!other.inPix) throw new IllegalArgumentException("Can't set from non-pix-specified bounds");
            super.setFrom(other);
        }
        
        @Override
		public void clear() {
            super.clear();
            inPix=true;
        }
        public double clampScalePix(double scale) {
            return MathUtil.clamp(min, scale, max);
        }
        @Override
		public void set(double minScale, double maxScale, boolean inPix) {
            if (!inPix) throw new IllegalArgumentException("Can't set inPix to false on InPix scale bounds");
            super.set(minScale, maxScale, inPix);
        }
        public void set(double minPerPix, double maxPerPix) {
            super.set(minPerPix, maxPerPix, true);
        }
        public boolean intersectsPix(double worldPerPix) {
            return min<=worldPerPix && worldPerPix <= max;
        }
    }

    public static class InDisp extends ScaleBounds {
        public InDisp(double min, double max) {
            super(min,max,false);
        }
        public InDisp() {
            super();
            inPix=false;
        }
        @Override
		public void setFrom(ScaleBounds other) {
            if (other.inPix) throw new IllegalArgumentException("Can't set from pix-specified bounds");
            super.setFrom(other);
        }
        
        @Override
		public void clear() {
            super.clear();
            inPix=false;
        }
        public double clampScale(double scale) {
            return MathUtil.clamp(min, scale, max);
        }
        @Override
		public void set(double minScale, double maxScale, boolean inPix) {
            if (inPix) throw new IllegalArgumentException("Can't set inPix to InDisp scale bounds");
            super.set(minScale, maxScale, inPix);
        }
        
        public boolean intersects(double scale) {
            return min<=scale && scale<=max;
        }
        
        public boolean intersects(InDisp other) {
            return min < other.max && max > other.min;
        }
        
        public double minScale() {
            return min;
        }
        public double maxScale() {
            return max;
        }
    }

    protected double min;
    protected double max;
    protected boolean inPix;
    
    public ScaleBounds() {
        clear();
    }
    
    public ScaleBounds(double min, double max, boolean inPix) {
        this.min=min;
        this.max=max;
        this.inPix=inPix;
    }

    public void setFrom(ScaleBounds other) {
        this.min=other.min;
        this.max=other.max;
        this.inPix=other.inPix;
    }

    public void clear() {
        min=0;
        max=Double.POSITIVE_INFINITY;
        inPix=false;
    }
    
    public boolean isEmpty() {
		return min==0 && max == Double.POSITIVE_INFINITY;
	}
    
    public double minScale(double pixSizeInMicrons) {
        if (inPix && min!=0) return CoordUtil.worldPerDisp(min, pixSizeInMicrons);
        return min;
    }
    
    public double maxScale(double pixSizeInMicrons) {
        if (inPix && max!=Double.POSITIVE_INFINITY) {
        	return CoordUtil.worldPerDisp(max, pixSizeInMicrons);
        }
        return max;
    }

    public boolean intersects(double scale, double pixSizeInMicrons) {
        return (minScale(pixSizeInMicrons) <= scale && scale <= maxScale(pixSizeInMicrons));
    }
    
    public double ratio(double scale, double pixSizeInMicrons) {
        return MathUtil.logRatio(minScale(pixSizeInMicrons), scale, maxScale(pixSizeInMicrons));
    }

    public double clamp(double scale, double pixSizeInMicrons) {
        return MathUtil.clamp(minScale(pixSizeInMicrons), scale, maxScale(pixSizeInMicrons));
    }

    public void set(double minScale, double maxScale, boolean inPix) {
        this.min=minScale;
        this.max=maxScale;
        this.inPix=inPix;
    }
    
    public void setFromPixDiagonal(double min, double max) {
    	set(min/MathUtil.SQRT2,max/MathUtil.SQRT2, true);
    }

    public void expandToInclude(ScaleBounds scaleBounds) {
    	if (scaleBounds.isEmpty()) return;
        if (this.inPix!=scaleBounds.inPix) throw new IllegalArgumentException("Cannot merge bounds with pixel scales and those with nominal scales");
        this.min=Math.min(this.min, scaleBounds.min);
        this.max=Math.max(this.max, scaleBounds.max);
    }
    
    public void expandToInclude(ScaleBounds scaleBounds, double pixSizeInMicrons) {
        if (this.inPix==scaleBounds.inPix) {
            expandToInclude(scaleBounds);
        } else if (this.inPix) {
            this.min=Math.min(this.min, scaleBounds.minWorldPerPix(pixSizeInMicrons));
            this.max=Math.min(this.max, scaleBounds.maxWorldPerPix(pixSizeInMicrons));
        } else {
            this.min=Math.min(this.min, scaleBounds.minScale(pixSizeInMicrons));
            this.max=Math.min(this.max, scaleBounds.maxScale(pixSizeInMicrons));
        }
    }

    private double maxWorldPerPix(double pixSizeInMicrons) {
        return inPix||max==Double.POSITIVE_INFINITY?max:CoordUtil.worldPerPix(max, pixSizeInMicrons);
    }

    private double minWorldPerPix(double pixSizeInMicrons) {
        return inPix||min==0?min:CoordUtil.worldPerPix(min, pixSizeInMicrons);
    }

	public boolean intersects(ScaleBounds other, double pixSizeInMicrons) {
		return minScale(pixSizeInMicrons)<other.maxScale(pixSizeInMicrons) && maxScale(pixSizeInMicrons)>other.minScale(pixSizeInMicrons);
	}
}
