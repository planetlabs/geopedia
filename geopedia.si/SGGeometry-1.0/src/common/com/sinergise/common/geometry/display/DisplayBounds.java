/*
 *
 */
package com.sinergise.common.geometry.display;

import com.sinergise.common.geometry.display.ScaleBounds.InDisp;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;


public class DisplayBounds {
    public static class Pix extends DisplayBounds {
        public Pix() {
            super(new ScaleBounds.InPix());
        }
        public void setScaleBoundsPix(double minWorldPerPix, double maxWorldPerPix) {
           ((ScaleBounds.InPix)scaleBounds).set(minWorldPerPix, maxWorldPerPix);
        }
        public boolean scaleIntersectsPix(double worldPerPix) {
            return ((ScaleBounds.InPix)scaleBounds).intersectsPix(worldPerPix);
        }
    }

    public static class Disp extends DisplayBounds {
        public Disp() {
            this(Envelope.getEmpty());
        }
        public Disp(Envelope mbr) {
            super(mbr == null ? Envelope.getEmpty() : mbr, new ScaleBounds.InDisp());
        }
        public double clampScale(double scale) {
            return ((ScaleBounds.InDisp)scaleBounds).clampScale(scale);
        }
        public boolean scaleIntersects(double scale) {
            return ((ScaleBounds.InDisp)scaleBounds).intersects(scale);
        }
        public double minScale() {
            return ((ScaleBounds.InDisp)scaleBounds).minScale(); 
        }
        public double maxScale() {
            return ((ScaleBounds.InDisp)scaleBounds).maxScale(); 
        }
        public double scaleRatio(double scale) {
            return MathUtil.logRatio(minScale(), scale, maxScale());
        }
        public boolean intersects(Disp other) {
        	if (!((InDisp)other.scaleBounds).intersects((InDisp)scaleBounds)) return false;
        	if (!other.mbr.intersects(mbr)) return false;
        	return true;
        }
    }
    public Envelope mbr = Envelope.getEmpty();
    public final ScaleBounds scaleBounds;
    
    public DisplayBounds() {
        this(null);
    }
    
    public DisplayBounds(ScaleBounds sb) {
        this(null, sb);
    }
    
    public DisplayBounds(Envelope mbr2, ScaleBounds sb) {
    	this.mbr = mbr2 == null ? Envelope.getEmpty() : mbr2;
        this.scaleBounds = sb == null ? new ScaleBounds() : sb;
    }

    public void copyFrom(DisplayBounds bounds) {
        this.mbr = bounds.mbr;
        this.scaleBounds.setFrom(bounds.scaleBounds);
    }
    
    public DisplayBounds cloneDisplayBounds() {
    	DisplayBounds ret=new DisplayBounds();
    	ret.copyFrom(this);
    	return ret;
    }

    public void clear() {
        mbr = Envelope.getEmpty();
        scaleBounds.clear();
    }
    
    public boolean isEmpty() {
		return mbr.isEmpty() && scaleBounds.isEmpty();
	}
    
    public double clampX(double input) {
        return mbr.clampX(input);
    }
    public double clampY(double input) {
        return mbr.clampY(input);
    }
    public double clampScale(double scale, double pixSizeInMicrons) {
        return scaleBounds.clamp(scale, pixSizeInMicrons);
    }
    public double scaleRatio(double scale, double pixSizeInMicrons) {
        return scaleBounds.ratio(scale, pixSizeInMicrons);
    }

    public boolean intersects(double scale, double pixSizeInMicrons, Envelope mbr2) {
        if (!scaleIntersects(scale, pixSizeInMicrons)) return false;
        if (mbr.isEmpty()) return true;
        return mbr.intersects(mbr2);
    }
    
    public boolean intersects(DisplayBounds other, double pixSizeInMicrons) {
    	if (!scaleBounds.intersects(other.scaleBounds, pixSizeInMicrons)) return false;
    	if (!mbr.intersects(other.mbr)) return false;
    	return true;
    }

    public boolean scaleIntersects(double scale, double pixSizeInMicrons) {
        return scaleBounds.intersects(scale, pixSizeInMicrons);
    }

    public void setScaleBounds(double minScale, double maxScale) {
        scaleBounds.set(minScale, maxScale, false);
    }

    public void setMBR(double minX, double minY, double maxX, double maxY) {
        mbr = new Envelope(minX,  minY,  maxX,  maxY);
    }

    public void setMBR(Envelope newMbr) {
    	if (newMbr == null) {
    		mbr = Envelope.getEmpty();
    	} else {
    		mbr = newMbr;
    	}
    }

    public void expandToInclude(DisplayBounds bounds) {
        this.mbr = this.mbr.union(bounds.mbr);
        this.scaleBounds.expandToInclude(bounds.scaleBounds);
    }
}
