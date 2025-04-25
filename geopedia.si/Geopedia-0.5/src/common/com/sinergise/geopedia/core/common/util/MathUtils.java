/*
 *
 */
package com.sinergise.geopedia.core.common.util;


public class MathUtils {
    public static final double LN_10 = 2.302585092994045684017991454684364207601;
    public static final double LN_2 = 0.6931471805599453094172321214581765680755;
    public static final double GOLDEN_RATIO = 1.6180339887498948482046;

    public static final char[] NUMERAL_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    
    public static final int distSq(int x0, int y0, int x1, int y1) {
        return distSq(x1-x0, y1-y0);
    }

    public static final int distSq(int dx, int dy) {
        return dx*dx+dy*dy;
    }

    public static double hypot(double x, double y)
    {
    	return Math.sqrt(x * x + y * y);
    }

    public static int clamp(int min, int val, int max)
    {
    	return val < min ? min : val > max ? max : val;
    }

    public static double atan2(double y, double x)
    {
    	if (x == 0) {
    		if (y > 0) {
    			return Math.PI / 2;
    		} else if (y < 0) {
    			return -Math.PI / 2;
    		} else {
    			return 0;
    		}
    	} else if (x < 0) {
    		if (y < 0) {
    			return Math.atan(y / x) - Math.PI;
    		} else {
    			return Math.atan(y / x) + Math.PI;
    		}
    	} else {
    		return Math.atan(y / x);
    	}
    }

    public static double clamp(double min, double val, double max)
    {
    	return val < min ? min : val > max ? max : val;
    }

    public static final String toHex(long value, int len)
    {
    	return StringUtils.padWith(Long.toHexString(value), '0', len, true);
    }
    
    public static final int floorInt(double d) {
        return (int)Math.floor(d);
    }

    public static int extractExp(double value, double base) {
        return floorInt(log(value*(1+1e-6))/log(base));
    }

    public static void extractPrefixExp(double value, double base, double[] ret) {
        int exp=floorInt(log(value+1e-6*base)/log(base));
        double prefix=value/Math.pow(base, exp);
        ret[0]=prefix;
        ret[1]=exp;
    }
    
    public static double log(double value) {
        if (value==10) return LN_10;
        if (value==1) return 0;
        if (value==2) return LN_2;
        if (value==Math.E) return 1;
        return Math.log(value);
    }

    public static double logRatio(double min, double value, double max) {
        return log(value/min)/log(max/min);
    }
    
    public static double invertIfSmall(double val) {
        return val >= 1 ? val : (1.0/val);
    }
    public static double invertIfLarge(double val) {
        return val <= 1 ? val : (1.0/val);
    }

    public static double roundToList(double value, double[] decPrefixes, double[] retPrefixExp) {
        double exp=extractExp(value, 10);
        double pref=value*Math.pow(10, -exp);
        int i=0;
        
        double minRatio=invertIfSmall(10*decPrefixes[0]/pref);
        retPrefixExp[0]=decPrefixes[0];
        retPrefixExp[1]=exp+1;
        
        while (i<decPrefixes.length) {
            double curRatio=invertIfSmall(decPrefixes[i]/pref);
            if(curRatio<minRatio) {
                minRatio=curRatio;
                retPrefixExp[0]=decPrefixes[i];
                retPrefixExp[1]=exp;
            }
            i++;
        }
        return retPrefixExp[0]*Math.pow(10, retPrefixExp[1]);
    }

	public static double fromLogRatio(double ratio, double min, double max) {
		return min*Math.exp(ratio*log(max/min));//log(value/min)/log(max/min);
	}

    public static long fromHex(String hexString) {
        return Long.parseLong(hexString, 16);
    }

    /**
     * @param min   minimal bounds (inclusive)
     * @param value value to compare
     * @param max   maximal bounds (inclusive)
     * 
     * @return min <= value <= max
     */
    public static boolean between(double min, double value, double max) {
        return min<=value && value <= max;
    }
}
