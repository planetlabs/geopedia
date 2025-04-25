/*
 *
 */
package com.sinergise.common.geometry.display;

import java.util.Arrays;

import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsDisp;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsPix;
import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.settings.Settings;
import com.sinergise.common.util.settings.Settings.TypeMap;


@TypeMap(
	names = {"PIX", 				"DISP"},
	types = {ZoomLevelsPix.class,	ZoomLevelsDisp.class}
	)
public class ScaleLevelsSpec implements Settings {
    public static class ZoomLevelsPix extends ScaleLevelsSpec {
      	@Deprecated // Serialization only
      	public ZoomLevelsPix() {
      		super();
      	}
        public ZoomLevelsPix(int offset, double[] scalesPerPix) {
            super(offset, scalesPerPix, false);
        }
        public double worldPerPix(int levelId) {
            return scales[levelId-offset];
        }
        public double minWorldPerPix() {
            return worldPerPix(getMaxLevelId());
        }
        public double maxWorldPerPix() {
            return worldPerPix(getMinLevelId());
        }
        public int nearestZoomLevelPix(double worldPerPix) {
            return optimalLevel(worldPerPix, 0.5);
        }
        
        /**
         * @param worldPerPix Required exact scale
         * @param ratio Log ratio used to decide which value to round to (0 -> smaller scales, 1 -> larger scales)
         */
        public int optimalZoomLevelPix(double worldPerPix, double ratio) {
            return optimalLevel(worldPerPix, ratio);
        }
        public int lastGreaterOrEqualPix(double worldPerPix) {
            return lastGtEqVal(worldPerPix);
        }
        public int firstSmallerOrEqualPix(double worldPerPix) {
            return firstSmEqVal(worldPerPix);
        }
        
		public Envelope getWorldEnvelope(double minx, double miny, int pixW, int pixH, int levelId) {
			double pxSize = worldPerPix(levelId);
			return new Envelope(minx, miny, minx + pixW * pxSize, miny + pixH * pxSize);
		}

		public boolean isWholeFactors() {
			double prevSc = scales[0];
			for (int i = 1; i < scales.length; i++) {
				double curSc = scales[i];
				double fact = prevSc/curSc;
				prevSc = curSc;
				if (Math.rint(fact) != fact) return false;
			}
			return true;
		}
		
		public boolean isFactorsOf2() {
			double prevSc = scales[0];
			for (int i = 1; i < scales.length; i++) {
				double curSc = scales[i];
				if (prevSc/curSc != 2) return false;
				prevSc = curSc;
			}
			return true;
		}
    }
    public static class ZoomLevelsDisp extends ScaleLevelsSpec {
      	@Deprecated // Serialization only
      	public ZoomLevelsDisp() {
      		super();
				}
    	
        public ZoomLevelsDisp(int offset, double[] scalesPerDisp) {
            super(offset, scalesPerDisp, true);
        }
        public double worldPerDisp(int levelId) {
            return scales[levelId-offset];
        }
        public double minWorldPerDisp() {
            return worldPerDisp(getMaxLevelId());
        }
        public double maxWorldPerDisp() {
            return worldPerDisp(getMinLevelId());
        }
        public int nearestZoomLevelDisp(double worldPerDisp) {
            return optimalLevel(worldPerDisp, 0.5);
        }
        public int optimalZoomLevelDisp(double worldPerDisp, double ratio) {
            return optimalLevel(worldPerDisp, ratio);
        }
        public int lastGreaterOrEqualDisp(double worldPerDisp) {
            return lastGtEqVal(worldPerDisp);
        }
        public int firstSmallerOrEqualDisp(double worldPerDisp) {
            return firstSmEqVal(worldPerDisp);
        }
        public ZoomLevelsPix createInPix(double pixSizeInMicrons) {
            double[] worldPerPix=new double[scales.length];
            for (int i = 0; i < worldPerPix.length; i++) {
                worldPerPix[i]=CoordUtil.worldPerPix(scales[i], pixSizeInMicrons);
            }
            return new ZoomLevelsPix(offset, worldPerPix);
        }
    }
    /**
     * 1:2500, 1:5000, 1:1500, 1:3500, 1:7500, 1:2000, 1:6000, 1:1250, 1:4000 ...
     */
    private static final double[] STD_PREFIXES = new double[] {1, 2.5, 5, 1.5, 3.5, 7.5, 2, 6, 1.25, 4, 3, 8.5, 1.75, 4.5, 6.5, 2.25, 5.5, 2.75, 1.3, 3.25};
    
    
    public static ScaleLevelsSpec clone(ScaleLevelsSpec specs, int maxLevelId) {
		if (maxLevelId>specs.getMaxLevelId())
			maxLevelId=specs.getMaxLevelId();
		double scales[] = new double[maxLevelId-specs.getMinLevelId()+1];
		for (int i = 0; i < scales.length; i++) {
			scales[i]=specs.scales[i];
		}

    	if (specs instanceof ZoomLevelsDisp) {
    		return new ZoomLevelsDisp(specs.getMinLevelId(),scales);
    	} 
		return new ZoomLevelsPix(specs.getMinLevelId(), scales);
    }
    /**
     * Constructs levels using powers of 2; for Geopedia, parameters used would
     * be 0.25, 19, 7
     * 
     * @param worldMinPerPix -
     *            value corresponding to maxLevelId
     * @param maxLevelId
     * @param minLevelId
     */
   public static ZoomLevelsPix createWithPixSize(double factor, double base, double worldMinPerPix, int maxLevelId, int minLevelId) {
        int minExp=MathUtil.extractExp(worldMinPerPix/factor, base);
        int maxExp=minExp+(maxLevelId-minLevelId);
        return (ZoomLevelsPix)createWithBase(factor, base, new double[] {1}, 
                minExp, 0, 
                maxExp, 0, 
                false, 
                maxExp+minLevelId, 0);
    }

    /**
     * Constructs industry standard levels (using the provided min and max scale
     * values). The pattern here is (1.0, 2.5, 5.0) and the scales are geographic
     * (display/world) inverse values rather than pixel-based
     * 
     * @param worldMinPerDisplay
     *            e.g. <code>5e3</code> which means scale of 1:5,000
     * @param worldMaxPerDisplay
     *            e.g. <code>2.5e6</code> which means scale of 1:2,500,000
     */
    public static ScaleLevelsSpec.ZoomLevelsDisp createStandard(double worldMinPerDisplay, double worldMaxPerDisplay) {
        return createStandard(worldMinPerDisplay, worldMaxPerDisplay, 3);
    }
    
    /**
     * 
     * @param worldMinPerDisplay
     * @param worldMaxPerDisplay
     * @param howMany - how many of the standard prefixes every decade will be split into
     * @return
     */
    public static ScaleLevelsSpec.ZoomLevelsDisp createStandard(double worldMinPerDisplay, double worldMaxPerDisplay, int howMany) {
        double[] prefxs=new double[howMany];
        for (int i = 0; i < howMany; i++) {
            prefxs[i]=STD_PREFIXES[i];
        }
        return createStandard(worldMinPerDisplay, worldMaxPerDisplay, prefxs);
    }
    
    /**
     * prefxs is the array of factors between [1, 10), which will be used to 
     * calculate the scales.
     * 
     * Example: if prefsx = {1.0, 1.25, 2.5, 5.0, 7.5} and min=500, max = 10000, 
     * the resulting scales will be:
     * 500
     * 750
     * 1000
     * 1250
     * 2500
     * 5000
     * 7500
     * 10000
     * 
     * @param worldMinPerDisplay
     * @param worldMaxPerDisplay
     * @param prefxs
     * @return
     */
    public static ScaleLevelsSpec.ZoomLevelsDisp createStandard(double worldMinPerDisplay, double worldMaxPerDisplay, double[] prefxs) {
        for (int i = 0; i < prefxs.length; i++) {
            while (prefxs[i]<1) {
                prefxs[i] = 10*prefxs[i];
            }
            while (prefxs[i]>=10) {
                prefxs[i] = prefxs[i]/10;
            }
        }
        Arrays.sort(prefxs);
        
        int minExp=MathUtil.extractExp(worldMinPerDisplay, 10);
        double minPrefix=worldMinPerDisplay/Math.pow(10, minExp);
		int minPrefixIdx = ArrayUtil.indexOfClosest(prefxs, minPrefix);

        int maxExp=MathUtil.extractExp(worldMaxPerDisplay, 10);
        double maxPrefix=worldMaxPerDisplay/Math.pow(10, maxExp);
		int maxPrefixIdx = ArrayUtil.indexOfClosest(prefxs, maxPrefix);
        
        // level 0 is 1:500,000,000 (1px=100km in CSL DPI; 1px=140km in OGC DPI)
        int offsetMaxExp=8;
		int offsetMaxPrefixIdx = ArrayUtil.indexOfClosest(prefxs, 5);
        
        return (ZoomLevelsDisp)createWithBase(1, 10, prefxs, 
                minExp, minPrefixIdx, 
                maxExp, maxPrefixIdx, 
                true, 
                offsetMaxExp, offsetMaxPrefixIdx);
    }
    /**
     * Creates zoom levels using a base and its powers together with a list of prefixes to append.
     * One can specify also factors for recalculating values to the target units (worldPixelSize):
     * <ul>
     * <li>both "pix" factors can be set to 1 if this calculation is not needed;</li>
     * <li>if base and prefixes are specified in user scale (OGC-style), 
     * pixFactor should be set to pixSize and pixDivider should be 1 (or, for better accuracy, use pixSizeInMicrons and 1e6).</li>
     * </ul>
     * The offset is normally set so that level 0 covers the whole world in a small tile (for standard tile size of 256px, pix size has to be 40000/256 ~ 156 km)
     * 
     * @param base
     * @param prefixes
     * @param minExp
     * @param minPrefixIdx
     * @param maxExp
     * @param maxPrefixIdx
     * @param toPixFactor
     * @param toPixDivider
     * @param offsetMaxExp
     * @param offsetMaxPrefixIdx
     * @return
     */
    public static ScaleLevelsSpec createWithBase(
    				double factor, double base, double[] prefixes, 
            int minExp, int minPrefixIdx, 
            int maxExp, int maxPrefixIdx,
            boolean isWorldPerDisp, 
            int offsetMaxExp, int offsetMaxPrefixIdx) {
        int len=(maxPrefixIdx-minPrefixIdx)+(maxExp-minExp)*prefixes.length+1;
        double[] ret=new double[len];
   
        double wCurValPerD = 0;
        int curExp=minExp;
        int cnt=minPrefixIdx;
        for (int i = len - 1; i >= 0; i--) {
            int pref = cnt % prefixes.length;
            wCurValPerD = prefixes[pref] * Math.pow(base, curExp);
            ret[i] = factor*wCurValPerD;
            if (pref == prefixes.length - 1) {
                curExp++;
            }
            cnt++;
        }
        
        int off=(offsetMaxPrefixIdx-maxPrefixIdx)+(offsetMaxExp-maxExp)*prefixes.length;
        
        if (isWorldPerDisp) {
            return new ZoomLevelsDisp(off, ret);
        }
		return new ZoomLevelsPix(off, ret);
    }

    protected int offset;
    /**
     * A descending array of scales - smaller scales (i.e. larger values) are at the beginning.
     */
    protected double[] scales; // values (worldLenPerDisplay or worldLenPerPix)

    private boolean inPix=false;
    
    @Deprecated // Serialization only
    protected ScaleLevelsSpec() {
    	super();
    }
    
    /**
     * @param offset
     * @param levelsPerPx
     */
    public ScaleLevelsSpec(int offset, double[] scales, boolean isWorldPerDisp) {
        super();
        this.offset = offset;
        this.scales = scales;
        inPix=!isWorldPerDisp;
    }

    public int firstSmallerThanOrEqual(double scale, double pixSizeInMicrons) {
        if (inPix) scale = CoordUtil.worldPerPix(scale, pixSizeInMicrons);
        return firstSmEqVal(scale);
    }

    public int lastGreaterThanOrEqual(double scale, double pixSizeInMicrons) {
        if (inPix) scale = CoordUtil.worldPerPix(scale, pixSizeInMicrons);
        return lastGtEqVal(scale);
    }
    protected int lastGtEqVal(double val) {
        for (int i = scales.length-1; i >=0; i--) {
            if (scales[i] >= val) {
                return offset + i;
            }
        }
        return -1;
    }
    protected int firstSmEqVal(double val) {
        for (int i = 0; i < scales.length; i++) {
            if (scales[i] <= val) {
                return offset + i;
            }
        }
        return -1;
    }

    /**
     * @param scale the value for which the nearest value should be found
     * @return level id of the nearest scale
     */
    public int nearestZoomLevel(double scale, double pixSizeInMicrons) {
        return optimalZoomLevel(scale, 0.5, pixSizeInMicrons);
    }
    /**
     * @param scale  
     * @param ratio a value between 0 and 1, which controls whether smaller (0) or larger (1) scales are preffered
     * @return
     */
    public int optimalZoomLevel(double scale, double ratio, double pixSizeInMicrons) {
        if (inPix) scale = CoordUtil.worldPerPix(scale, pixSizeInMicrons);
        return optimalLevel(scale, ratio);
    }

    /**
     * @param value Required exact scale
     * @param ratio Log ratio used to decide which value to round to (0 -> floor, 1 -> ceil)
     * 
     * @return
     */
    protected int optimalLevel(double value, double ratio) {
        double prevScale=scales[0];
        // scale smaller than the smallest possible
        if (value>=prevScale) return offset;
        
        for (int i = 1; i < scales.length; i++) {
            double curScale=scales[i];
            if (value==curScale) return offset+i;
            if (value > curScale) { // we hit the right interval
                double curRatio=MathUtil.logRatio(curScale, value, prevScale);
				if (curRatio >= ratio) {
					return offset + i - 1; // return previous (smaller scale, larger value)
				}
				return offset+i; // return current (larger scale, smaller value)
            }
            prevScale=curScale;
        }
        // scale larger than the largest possible
        return offset+scales.length-1;
    }
    
    /**
     * @return maximal id (i.e. id of the level with minimal worldLenPerPix)
     */
    public int getMaxLevelId() {
        return offset + scales.length - 1;
    }

    /**
     * @return minimal id (i.e. id of the level with maximal worldLenPerPix)
     */
    public int getMinLevelId() {
        return offset;
    }

    public double worldPerPix(int levelId, double pixSizeInMicrons) {
        if (inPix) return scales[levelId-offset];
        return CoordUtil.worldPerPix(scales[levelId - offset], pixSizeInMicrons);
    }

    public double scale(int levelId, double pixSizeInMicrons) {
        if (inPix) return CoordUtil.worldPerDisp(scales[levelId - offset], pixSizeInMicrons);
        return scales[levelId - offset];
    }

    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < scales.length; i++) {
            if (i > 0)
                buf.append(", ");
            buf.append('(');
            buf.append(offset + i);
            buf.append('=');
            buf.append(scale(offset+i, CoordUtil.getDefaultPixSizeInMicrons()));
            buf.append(" / ");
            buf.append(worldPerPix(offset + i, CoordUtil.getDefaultPixSizeInMicrons()));
            buf.append(')');
        }
        return buf.toString();
    }

    public double minScale(double pixSizeInMicrons) {
        return scale(getMaxLevelId(), pixSizeInMicrons);
    }
    
    public double maxScale(double pixSizeInMicrons) {
        return scale(getMinLevelId(), pixSizeInMicrons);
    }
    
    public ZoomLevelsDisp toDisplay(double pixSizeInMicrons) {
    	if (this instanceof ZoomLevelsDisp) return (ZoomLevelsDisp)this;
    	double[] lvls=scales;
    	if (inPix) {
    		lvls=new double[getMaxLevelId()-getMinLevelId()+1];
    		for (int i = 0; i < lvls.length; i++) {
    			lvls[i]=scale(getMinLevelId()+i, pixSizeInMicrons);
    		}
    	}
    	return new ZoomLevelsDisp(0, lvls);
    }
    
    public ZoomLevelsPix toPix(double pixSizeInMicrons) {
    	if (this instanceof ZoomLevelsPix) return (ZoomLevelsPix)this;
    	double[] lvls=scales;
    	if (!inPix) {
    		lvls=new double[getMaxLevelId()-getMinLevelId()+1];
    		for (int i = 0; i < lvls.length; i++) {
    			lvls[i]=worldPerPix(getMinLevelId()+i, pixSizeInMicrons);
    		}
    	}
    	return new ZoomLevelsPix(0, lvls);
    }

		public static ZoomLevelsPix createWithFactor2(double maxLevelPixSize, int maxLevelId, int minLevelId) {
			return createWithPixSize(maxLevelPixSize, 2, maxLevelPixSize, maxLevelId, minLevelId);
		}
		public static ZoomLevelsPix createWithFactor2(double nomLevelPixSize, int nomLevelId, int maxLevelId, int minLevelId) {
			double maxLevelPixSize = nomLevelPixSize*Math.pow(2, nomLevelId-maxLevelId);
			return createWithPixSize(nomLevelPixSize, 2, maxLevelPixSize, maxLevelId, minLevelId);
		}

		public double roundScale(double scale, double pixSizeInMicrons) {
			return scale(optimalZoomLevel(scale, 0.138647, pixSizeInMicrons),pixSizeInMicrons);
		}
		
		/**
		 * 
		 * @param scale
		 * @param pixSizeInMicrons
		 * @return the larger value of the two candidates
		 */
		public double roundScaleUp(double scale, double pixSizeInMicrons) {
			return scale(optimalZoomLevel(scale, 0, pixSizeInMicrons),pixSizeInMicrons);
		}
		
		public double roundScaleDown(double scale, double pixSizeInMicrons) {
			return scale(optimalZoomLevel(scale, 1, pixSizeInMicrons),pixSizeInMicrons);
		}

	public ScaleBounds getScaleBounds() {
		if (inPix) {
			return new ScaleBounds.InPix(this.scales[this.scales.length - 1], this.scales[0]);
		}
		return new ScaleBounds.InDisp(this.scales[this.scales.length - 1], this.scales[0]);
	}
}
