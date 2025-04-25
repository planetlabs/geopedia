package com.sinergise.common.geometry.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.CartesianCRS.ProjectedCRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.format.NumberFormatProvider.NumberFormatConstants;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart shows pattern letters, date/time component, presentation, and examples.">
 * 	<tr bgcolor="#ccccff">
 * 		<th align=left>Letter
 * 		<th align=left>Coordinate Component
 * 	<tr>
 * 		<td><code>X</code>
 * 		<td>Marker to start X (lat) coordinate
 * 	<tr bgcolor="#eeeeff">
 * 		<td><code>Y</code>
 * 		<td>Marker to start Y (lon) coordinate
 * 	<tr>
 * 		<td><code>A</code>
 * 		<td>Name of the ordinate (from the CRS, e.g. "Y" for x ordinate and "X" for y ordinate in Gauss Grueger)
 * 	<tr bgcolor="#eeeeff">
 * 		<td><code>C</code>
 * 		<td>Signed raw ordinate value, or first ordinate of projected coordinate in case the CRS is Geographic (lat, lon)
 * 	<tr>
 * 		<td><code>c</code>
 * 		<td>unsigned (abs) ordinate value, or first ordinate of projected coordinate in case the CRS is Geographic (lat, lon)
 * 	<tr bgcolor="#eeeeff">
 * 		<td><code>+</code>
 * 		<td>Sign of the latitude/longitude value '+' or '-'
 * 	<tr>
 * 		<td><code>-</code>
 * 		<td>Sign, plus is not shown ('' or '-')
 * 	<tr bgcolor="#eeeeff">
 * 		<td><code>D</code>
 * 		<td>Absolute value of the latitude/longitude in degrees (or raw ordinate in case CRS is not specified)
 * 	<tr>
 * 		<td><code>d</code>
 * 		<td>Whole part (floor) of the absolute value of the latitude/longitude in degrees (or raw ordinate in case CRS is not specified)
 * 	<tr bgcolor="#eeeeff">
 * 		<td><code>M</code>
 * 		<td>Absolute value of residual latitude/longitude minutes (x%60)
 * 	<tr>
 * 		<td><code>m</code>
 * 		<td>Whole part (floor) of the absolute value of residual latitude/longitude minutes floor(60*(x-x/60))
 * 	<tr bgcolor="#eeeeff">
 * 		<td><code>S</code>
 * 		<td>Absolute value of residual latitude/longitude seconds (x%3600)
 * 	<tr>
 * 		<td><code>w</code>
 * 		<td>Direction, according to the current ordinate (x, y, lat or lon) and sign ('N','S','E','W')
 * 	</table>
 * </blockquote>
 * 
 * @author Miha
 */
public class CoordinateFormat {
	private static final char[] specialChars = new char[]{'X','Y','A','C','c','+','-','D','d','M','m','S','w'};
	static {
		Arrays.sort(specialChars);
	}
	public static final CoordinateFormat LAT_LON=new CoordinateFormat("Xd°m''S{0.##}\" w Yd°m''S{0.##}\" w");
	public static final CoordinateFormat XY=new CoordinateFormat("XC{0.###} YC{0.###}");

	public static final CoordinateFormat LAT_LON_D=new CoordinateFormat("XD{00.0000}° w YD{000.0000}° w");
	public static final CoordinateFormat LAT_LON_DM=new CoordinateFormat("Xd{00}°M{00.00}'' w Yd{000}°M{00.00}'' w");
	public static final CoordinateFormat LAT_LON_DMS=new CoordinateFormat("Xd{00}°m{00}''S{00.0}\" w Yd{000}°m{00}''S{00.0}\" w");
	
	/**
	 * ISO 6709 compliant formatting http://en.wikipedia.org/wiki/ISO_6709
	 */
	public static final CoordinateFormat ISO_DEG=new CoordinateFormat("X+D{00.######}Y+D{000.######}",new NumberFormatConstants(".",","));
	public static final CoordinateFormat ISO_DM=new CoordinateFormat("X+d{00}M{00.######}Y+d{000}M{00.######}",new NumberFormatConstants(".",","));
	public static final CoordinateFormat ISO_DMS=new CoordinateFormat("X+d{00}m{00}S{00.######}Y+d{000}m{00}S{00.######}",new NumberFormatConstants(".",","));
	/**
	 * "IETF rfc 5870" http://www.rfc-editor.org/rfc/rfc5870.txt geo:X,Y
	 */
	public static final CoordinateFormat RFC5870=new CoordinateFormat("'geo:'XC{0.##########################},YC{0.##########################}",new NumberFormatConstants(".",","));

	private static class CoordinateFormatToken {
		private String constString;
		private NumberFormatter formatter;
		private boolean number = false;
		private int index = -1;
		
		public CoordinateFormatToken(String constString) {
			this.constString = constString;
		}
		
		public CoordinateFormatToken(int index) {
			this.index = index;
		}

		public CoordinateFormatToken(int index, String pattern, NumberFormatConstants consts) {
			this.formatter = pattern == null ? NumberFormatUtil.createDefaultDecimal(consts) : NumberFormatUtil.create(pattern, consts);
			this.index = index;
			this.number = true;
		}

		public void appendFormatted(Object[] data, StringBuffer outBuf) {
			if (index < 0) {
				outBuf.append(constString);
			} else if (number) {
				Number val = (Number)data[index];
				outBuf.append(formatter.format(val.doubleValue()));
			} else {
				outBuf.append(String.valueOf(data[index]));
			}
		}
	}
	
	List<CoordinateFormatToken> tokens;
	
	
	public CoordinateFormat(String pattern) {
		this(pattern, NumberFormatUtil.getDefaultConstants());
	}
	
	public CoordinateFormat(String pattern, NumberFormatConstants consts) {
		tokens = convertPattern(pattern, consts);
	}
	
	private static final CoordinateFormatToken convertToken(String token, boolean isX, boolean isLatLon, NumberFormatConstants consts) {
//		System.out.println((isX?"X":"Y")+"--|"+token+"|--");
		if (token.length()<1) return null;
		
		String fmt = null;
		if (token.length()>=3 && token.charAt(1) == '{') {
			fmt = token.substring(2,token.length()-1);
			if (fmt.length()==0) fmt = null;
		}
		
		final int off = isX ? 0:12;
		char firstCh = token.charAt(0);
		// 'A','C','c','+','-','D','d','M','m','S','w'
		switch (firstCh) {
			case 'A': return new CoordinateFormatToken(off+0);
			case 'C': return new CoordinateFormatToken(off+1, fmt==null?"0.##":fmt, consts);
			case 'c': return new CoordinateFormatToken(off+2, fmt==null?"0.##":fmt, consts);
			case '+': return new CoordinateFormatToken(off+3);
			case '-': return new CoordinateFormatToken(off+4);
			case 'D': return new CoordinateFormatToken(off+5, fmt==null?"0.##":fmt, consts);
			case 'd': return new CoordinateFormatToken(off+6, fmt==null?"0":fmt, consts);
			case 'M': return new CoordinateFormatToken(off+7, fmt==null?"0.##":fmt, consts);
			case 'm': return new CoordinateFormatToken(off+8, fmt==null?"0":fmt, consts);
			case 'S': return new CoordinateFormatToken(off+9, fmt==null?"0.##":fmt, consts);
			case 'w': return new CoordinateFormatToken(isLatLon ? off+11 : off+10);
			default:
				break;
		}
		return new CoordinateFormatToken(token);
	}
	
	private static final List<CoordinateFormatToken> convertPattern(String pattern, NumberFormatConstants consts) {
		ArrayList<CoordinateFormatToken> out = new ArrayList<CoordinateFormatToken>();
		StringBuffer curToken = new StringBuffer();
		char coordCh = 0;
		boolean lastLatLon = false;
		boolean inquote = false;
		int inFmt = 0;
		for (int i = 0; i < pattern.length(); i++) {
			char curCh = pattern.charAt(i);
			char nextCh = (i+1)<pattern.length() ? pattern.charAt(i+1) : 0;
			if (inFmt > 0) {
				if (curCh == '{') inFmt++;
				else if (curCh == '}') inFmt--;
				curToken.append(curCh);
				if (inFmt == 0) {
					out.add(convertToken(curToken.toString(), coordCh != 'Y', lastLatLon, consts));
					curToken = new StringBuffer();
				}
			} else if (curCh == '\'') {
				if (nextCh=='\'') {
					curToken.append("'");
					i++;
					continue;
				}
				if (inquote) {
					inquote = false;
					out.add(new CoordinateFormatToken(curToken.toString()));
					curToken = new StringBuffer();
				} else {
					inquote = true;
				}
			} else if (curCh == 'X' || curCh == 'Y') {
				if (curCh != coordCh && curToken.length()>0) {
					out.add(convertToken(curToken.toString(), coordCh != 'Y', lastLatLon, consts));
					curToken = new StringBuffer(); 
				}
				coordCh = curCh;
			} else if (Arrays.binarySearch(specialChars, curCh)>=0) {
					if (curToken.length()>0) {
						out.add(convertToken(curToken.toString(), coordCh != 'Y', lastLatLon, consts));
						curToken = new StringBuffer();
					}
					curToken.append(curCh);
					
					if ("cC".indexOf(curCh) > 0) lastLatLon = false;
					else if ("dDmMS".indexOf(curCh) > 0) lastLatLon = true;
					
					if (nextCh == '{') {
						inFmt = 1;
						curToken.append(nextCh);
						i++;
					} else {
						out.add(convertToken(curToken.toString(), coordCh != 'Y', lastLatLon, consts));
						curToken = new StringBuffer();
					}
			} else {
				curToken.append(curCh);
			}
		}
		if (curToken.length()>0) {
			out.add(convertToken(curToken.toString(), coordCh != 'Y', lastLatLon, consts));
		}
		return out;
	}
	
	public String format(HasCoordinate p) {
		return format(CRS.NONAME_WORLD_CRS, p);
	}
	
	public static void breakCoord(String coordName, double xVal, double degVal, String coordDir, String degDir, Object[] out, int off) {
		boolean sign = degVal>=0;
		degVal = Math.abs(degVal);
		long wholeDeg = (long)degVal;
		double minVal = 60 * (degVal - wholeDeg);
		long wholeMin = (long)minVal;
		double sec = 60 * (minVal-wholeMin);
		
		out[off++] = coordName;
		out[off++] = Double.valueOf(xVal);
		out[off++] = Double.valueOf(Math.abs(xVal));
		out[off++] = sign?"+":"-";
		out[off++] = sign?"":"-";
		out[off++] = Double.valueOf(degVal);
		out[off++] = Long.valueOf(wholeDeg);
		out[off++] = Double.valueOf(minVal);
		out[off++] = Long.valueOf(wholeMin);
		out[off++] = Double.valueOf(sec);
		out[off++] = coordDir;
		out[off++] = degDir;
	}
	
	public String format(CRS sourceCRS, HasCoordinate p) {
		Point xy = new Point(p);
		Point ll = new Point(p);
		String dirsXLatYLon = convertPoints(sourceCRS, p, xy, ll);
		String nameX = sourceCRS==null?"X":sourceCRS.getCoordName(0);
		String nameY = sourceCRS==null?"Y":sourceCRS.getCoordName(1);
		
		// 'A','C','c','+','-','D','d','M','m','S','w'
		Object[] data = new Object[24];
		breakCoord(nameX, xy.x, ll.x, dirsXLatYLon.substring(0, 1), dirsXLatYLon.substring(1,2),data, 0);
		breakCoord(nameY, xy.y, ll.y, dirsXLatYLon.substring(2, 3), dirsXLatYLon.substring(3,4),data, 12);
		
		StringBuffer ret=new StringBuffer();
		for (CoordinateFormatToken token : tokens) {
			token.appendFormatted(data, ret);
		}
		return ret.toString();
	}

	@SuppressWarnings("rawtypes")
	private static String convertPoints(CRS sourceCRS, HasCoordinate c, Point xy, Point ll) {
		Point p = new Point(c);
		StringBuffer ret = new StringBuffer();
		if (sourceCRS == null) return ret.append(p.x>0?"EN":"WS").append(p.y>0?"NE":"SW").toString();
		xy.x = Double.NaN;
		xy.y = Double.NaN;
		xy.z = Double.NaN;
		ll.setFrom(xy);
		if (sourceCRS instanceof ProjectedCRS<?>) {
			xy.setFrom(p);
			ProjectedCRS<?> projCS = (ProjectedCRS<?>)sourceCRS;
			Transform tr = Transforms.find(projCS, projCS.sourceCRS);
			if (tr != null) {
				tr.point(p, ll);
			} else {
				return ret.append(xy.x>=0?"EE":"WW").append(xy.y>=0?"NN":"SS").toString();
			}
		} else if (sourceCRS instanceof LatLonCRS) {
			ll.setFrom(p);
			Transform myTr = null;
			for (Transform tr : Transforms.getDefaultTransforms(sourceCRS)) {
				if (tr.getTarget() instanceof CartesianCRS) {
					myTr = tr;
					break;
				}
			}
			if (myTr == null) {
				xy.setFrom(p);
				return ret.append(ll.x>=0?"NN":"SS").append(ll.y>=0?"EE":"WW").toString();
			}
			myTr.point(p, xy);
		} else {
			xy.setFrom(p);
			ll.setFrom(p);
		}
		return ret.append(xy.x>=0?'E':'W').append(ll.x>=0?'N':'S').append(xy.y>=0?'N':'S').append(ll.y>=0?'E':'W').toString();
	}
	

}
