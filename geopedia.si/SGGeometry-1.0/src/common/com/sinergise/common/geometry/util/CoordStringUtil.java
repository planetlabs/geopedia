package com.sinergise.common.geometry.util;

import java.util.ArrayList;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianToLatLon;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.format.Format;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.string.StringUtil;


/**
 * Utility class for interpreting coordinate data as entered by the user. 
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 */
public class CoordStringUtil
{
	private static final NumberFormatter[] formats=new NumberFormatter[10];
    private CoordStringUtil()
	{
	}
    
    public static final NumberFormatter getFormat(int minDec, int maxDec) {
    	boolean noStore=true;
    	if (minDec==maxDec) noStore=false;
    	if (noStore || formats[maxDec]==null) {
    		StringBuffer buf=new StringBuffer("0");
    		for (int i = 0; i < maxDec; i++) {
				if (i==0) buf.append('.');
				if (i<minDec) buf.append("0");
				else buf.append("#");
			}
    		NumberFormatter ret=NumberFormatUtil.create(buf.toString());
    		if (!noStore) formats[maxDec]=ret;
    		return ret;
    	}
    	return formats[maxDec];
    }

    public static final String formatCoordsWithDec(double wx, double wy, int numDec) {
    	NumberFormatter nf=getFormat(numDec, numDec);
        return nf.format(wx)+", "+nf.format(wy);
    }
    
    public static final String formatCoordsWithScale(double wx, double wy, double worldPerPix) {
        int numDec= worldPerPix>1? 0: worldPerPix>0.1?1: worldPerPix>0.01?2:3;
        return formatCoordsWithDec(wx, wy, numDec);
    }
    
    public static final Point parseCoords(CRS cs, String cordsText) {
    	if (cs instanceof LatLonCRS) {
    		return parseGpsCoords(cordsText);
    	}
    	return parseCoords(cordsText);
    }
    
	public static final Point parseCoords(String cords)
	{
		String[] cordStrings = findCoordTokens(cords.trim());
		if (cordStrings == null || cordStrings.length < 1) return null;
		
		if (cordStrings.length == 1) {
			cordStrings = cordStrings[0].split(",");
		}
		if (cordStrings.length>=2) {
			return new Point(Format.readDecimal(cordStrings[0]), Format.readDecimal(cordStrings[1]));
		}
		return null;
	}

	private static String[] findCoordTokens(String allText) {
		if (StringUtil.isNullOrEmpty(allText)) return null;
		ArrayList<String> ret=new ArrayList<String>();
		int curIdx=0;
		int startIdx=-1;
		do {
			char ch=allText.charAt(curIdx);
			if (startIdx<0 && isNumberStart(ch)) {
				startIdx=curIdx;
			} else if (startIdx>=0) {
				if (!isNumberPart(ch)) {
					ret.add(trimToNumber(allText.substring(startIdx, curIdx+1)));
					startIdx=-1;
				}
			} 
			curIdx++;
		} while (curIdx<allText.length());
		if (startIdx>=0) ret.add(allText.substring(startIdx, curIdx));
		return ret.toArray(new String[ret.size()]);
	}

	private static String trimToNumber(String numStr) {
		int startIdx=0;
		int endIdx=numStr.length();
		for (int i = 0; i < numStr.length(); i++) {
			if (isNumberStart(numStr.charAt(i))) {
				startIdx=i;
				break;
			}
		}
		for (int i = numStr.length()-1; i >= 0; i--) {
			if (Character.isDigit(numStr.charAt(i))) {
				endIdx=i;
				break;
			}
		}
		if (startIdx==0 && endIdx==numStr.length()) return numStr;
		return numStr.substring(startIdx, endIdx+1);
	}

	/**
	 * @param scale
	 * @param minScale minimal value of worldPerDisplayLen (used for better handling of inverse values)
	 * @param maxScale maximal value of worldPerDisplayLen (if the value read is greater than this parameter, inverse is returned)
	 * @return the scale, as read from the string
	 */
	public static final double parseScale(String scale, double minScale, double maxScale)
	{
		int idx = scale.indexOf(':');
		if (idx >= 0) {
			double first = Format.readDecimal(scale.substring(0, idx));
			double second = Format.readDecimal(scale.substring(idx + 1, scale.length()));
			return second / first;
		}
		double val = Format.readDecimal(scale);
		if (val > maxScale) {
		    double inv = 1.0 / val;
		    if (inv <= minScale) {
		        return inv;
		    }
		}
		return val;
	}

    public static String getLatLonString(double lat, double lon) {
    	String latSuf="N";
    	if (lat<0) {
    		latSuf="S";
    		lat=-lat;
    	}
    	String lonSuf="E";
    	if (lon<0) {
    		lonSuf="W";
    		lon=-lon;
    	}
    	
        return toDegMinSecString(lat)+""+latSuf+"  "+toDegMinSecString(lon)+""+lonSuf;
    }

    public static final String toDegMinSecString(double dblDeg) {
    	return toDegMinSecString(dblDeg, 2);
    }
    public static final String toDegMinSecString(double dblDeg, int numDec) {
    	final boolean neg = dblDeg<0;
    	dblDeg = Math.abs(dblDeg);
    	
    	final long decFact = (long)Math.pow(10, numDec);
    	final long secDiv = 60 * decFact;
    	long centSec = Math.round(dblDeg*60*secDiv);
    	int min=(int)(centSec/secDiv);
    	final int deg=min/60;
        min = min%60;
        centSec = centSec%secDiv;
        String secStr = String.valueOf(centSec);
        if (numDec>0) {
        	secStr = StringUtil.padWith(String.valueOf(centSec/decFact),'0',2,true)
        		+ NumberFormatUtil.getDefaultConstants().decimalSeparator
        		+ StringUtil.padWith(String.valueOf(centSec%decFact),'0',2,true);
        }
        return (neg?"-":"")+StringUtil.padWith(String.valueOf(deg),' ',3,true)+"°"+StringUtil.padWith(String.valueOf(min),'0',2,true)+"'"+secStr+"\"";
    }
    
    public static final String getLatLonStringFromXY(double x, double y, CartesianToLatLon<?, ?> transform) {
        return getLatLonString(transform.lat(x, y), transform.lon(x, y));
    }

    /**
     * 
     * @param text
     * @return Point(lat, lon)
     */
    public static final Point parseGpsCoords(String text) {
       return parseGpsCoords(text, false);
    }
    
    /**
     * 
     * @param text
     * @return Point(lat, lon)
     */
    public static final Point parseGpsCoords(String text, boolean onlyWithDegree) {
        text=text.trim().replaceAll("\\+","");
        int degIdx=text.indexOf('\u00B0');
        int minIdx=text.indexOf('\'');
        int secIdx=text.indexOf('\"');
        if ( degIdx<0 && minIdx<0 && secIdx<0) {
        	if(!onlyWithDegree) {
        		return parseCoords(text);
        	}
        	return null;
        }
        
        int[] start=new int[1];
        
        double lat = readToken(text,start,'\u00B0',true);
        lat += readToken(text,start,'\'',false)/60;
        lat += readToken(text,start,'\"',false)/3600;
        
        boolean latN = readNS(text, start);
        if (!latN) lat=-lat;
        
        double lon = readToken(text,start,'\u00B0',true);
        lon += readToken(text,start,'\'',false)/60;
        lon += readToken(text,start,'\"',false)/3600;
        
        boolean lonW = readWE(text, start);
        if (lonW) lon=-lon;
        
        return new Point(lat, lon);
    }

    /**
     * @param text
     * @param start
     * @return true if text is north
     */
    private static boolean readNS(String text, int[] start) {
    	int st=start[0]+1;
    	if (st>=text.length()) return true;
    	while (text.charAt(st)==' ') {
    		st++;
    		if (st>=text.length()) return true;
    	}
    	boolean ret=true;
    	if (text.charAt(st)=='N') {
    		st++;
    		start[0]=st;
    	}
    	if (text.charAt(st)=='S') {
    		st++;
    		start[0]=st;
    		ret = false;
    	}
		return ret;
	}

    private static boolean readWE(String text, int[] start) {
    	int st=start[0]+1;
		if (st>=text.length()) return false;
    	while (text.charAt(st)==' ') {
    		st++;
    		if (st>=text.length()) return false;
    	}
    	boolean ret=false;
    	if (text.charAt(st)=='E') {
    		st++;
    		start[0]=st;
    	}
		if (st>=text.length()) return ret;
    	if (text.charAt(st)=='W') {
    		st++;
    		start[0]=st;
    		ret=true;
    	}
		return ret;
	}
	private static double readToken(String text, int[] start, char endSign, boolean returnAnyway) {
        int st=start[0];
        if (st>=text.length()) return 0;
        while (!isNumberStart(text.charAt(st))) {
            st++;
            if (st>=text.length()) return 0;
        }
        int en=st+1;
        while (en<text.length() && isNumberPart(text.charAt(en))) {
            en++;
        }
        double ret=Format.readDecimal(text.substring(st,en));
        if (returnAnyway || (en<text.length() && text.charAt(en)==endSign)) {
            start[0]=en;
            return ret;
        }
        return 0;
    }
    
    private static boolean isNumberStart(char ch) {
        return Character.isDigit(ch) || ch=='-';
    }
    
    private static boolean isNumberPart(char ch) {
        return Character.isDigit(ch) || ch=='.' || ch==',' || ch=='e' || ch=='E' || ch=='-';
    }
//
//    public static int parseScaleToLevel(String scale, ScaleLevelsSpec zoomLevels, double pixSizeInMicrons) {
//        double sc = parseScale(scale,zoomLevels.minScale(pixSizeInMicrons),zoomLevels.maxScale(pixSizeInMicrons));
//        if (isLevel(sc, zoomLevels)) {
//            return (int) sc;
//        } else {
//            return zoomLevels.lastGreaterThanOrEqual(sc, pixSizeInMicrons);
//        }
//    }
//    private static boolean isLevel(double scale, ScaleLevelsSpec zoomLevels) {
//        return (scale == (int) scale) && (scale >= zoomLevels.getMinLevelId()) && (scale <= zoomLevels.getMaxLevelId());
//    }

    public static String getScaleString(double scale) {
        return getScaleString(scale, 0);
    }
    public static String getScaleString(double scale, int numDec) {
        if (scale<1) return Format.format((1.0/scale),numDec,true)+":1";
        return "1:"+Format.format(scale,numDec,true);
    }
    public static String getScaleDenominatorString(double scale, int numDec) {
        return Format.format(scale,numDec,true);
    }
    
    public static void main(String[] args) {
//    	ServerSideInitializer.initialize();
    	String[] tkns=findCoordTokens("142134.421 1234123.4213");
    	for (int i = 0; i < tkns.length; i++) {
			System.out.println(tkns[i]);
		}
		System.out.println(parseGpsCoords("48°49'53.09\" N 4°20'41.43\" W"));
	}

	public static int parseScaleToLevel(String sText, ScaleLevelsSpec zoomLevels, double pixSizeInMicrons) {
		return zoomLevels.optimalZoomLevel(parseScale(sText, zoomLevels.minScale(pixSizeInMicrons), zoomLevels.maxScale(pixSizeInMicrons)),0.5,pixSizeInMicrons);
	}

//	public static String formatCoords(CRS system, double x, double y) {
//		if (system instanceof LatLonCRS) {
//			return toDegMinSecString(x)+" "+system.getCoordName(0)+" "+toDegMinSecString(y)+" "+system.getCoordName(1);
//		}
//		NumberFormat nf=getFormat(0, 3);
//		return nf.format(x)+" "+nf.format(y);
//	}
}
