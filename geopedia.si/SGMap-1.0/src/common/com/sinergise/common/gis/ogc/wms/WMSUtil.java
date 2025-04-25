/*
 *
 */
package com.sinergise.common.gis.ogc.wms;

import java.awt.Color;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.style.NamedStyle;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.base.OGCImageRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.string.Escaper;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.common.util.web.MimeType;


public class WMSUtil {
	private static Escaper arrayEscaper = new Escaper('\\', new char[] { ',' });
	private static Escaper parenthesesEscaper = new Escaper('\\', new char[] { '(', ')' });
	private static Escaper array2DEscaper = new Escaper('\\', new char[] { ',', '(', ')' });
	public static final String WMS_TRUE = "TRUE";
	public static final String WMS_FALSE = "FALSE";

	public static final String toWMSColor(Color c) {
		return "0x" + MathUtil.toHex(c.getRGB(), 6);
	}
	
	public static Color fromWMSColor(String wmsColorString) {
		if (wmsColorString == null || wmsColorString.length() < 2) return null;
		return new Color((int) MathUtil.fromHex(wmsColorString.substring(1)));
	}
	
	public static String toWMSBoolean(boolean b) {
		return b ? WMS_TRUE : WMS_FALSE;
	}
	
	public static boolean fromWMSBoolean(String stringVal) {
		return WMS_TRUE.equals(stringVal);
	}
	
	public static String toWMSBBox(Envelope bbox) {
		return bbox.getMinX() + "," + bbox.getMinY() + "," + bbox.getMaxX() + "," + bbox.getMaxY();
	}
	
	public static void appendWMSBBox(StringBuffer buf, Envelope bbox) {
		buf.append(URLUtil.encodePart(toWMSBBox(bbox)));
	}
	
	public static Envelope fromWMSBBox(String wmsBBox) {
		if (wmsBBox == null) return null;
		String[] vals = wmsBBox.split(",");
		if (vals.length < 4) return null;
		return new Envelope(Double.parseDouble(vals[0]), Double.parseDouble(vals[1]), Double
			.parseDouble(vals[2]), Double.parseDouble(vals[3]));
		
	}
	
	public static String encodeArray(String[] strings) {
		return encodeArray(strings, arrayEscaper);
	}
	
	public static String encodeArray(String[] strings, Escaper whichEscaper) {
		if (strings == null) return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			if (i > 0) buf.append(',');
			if (strings[i]!=null && strings[i].length()>0) {
				buf.append(whichEscaper.escapeComponent(strings[i]));
			}
		}
		return buf.toString();
	}
	
	public static String encodeIntArray(int[] ints) {
		String strings[] = new String[ints.length];
		for (int i = 0; i < ints.length; i++) {
			strings[i] = String.valueOf(ints[i]);
		}
		return encodeArray(strings);
	}
	
	/**
	 * Encodes a list of lists according to WMS specification (as specified in WFS 1.1.0):
	 * lists are enclosed in parentheses '(' and  ')', and there is no comma to separate
	 * the lists.
	 * <p>
	 * Example: <br />
	 * <code>encode2DArray({{"dog","cat"},{"mouse","rabbit"}})</code> <br />
	 * gives: <code>"(dog,cat)(mouse,rabbit)"</code>
	 * </p>
	 * 
	 * @param stringArr
	 * @return
	 */
	public static String encode2DArray(String[][] stringArr) {
		if (stringArr == null) return "";
		if (stringArr.length == 0) return "";
		if (stringArr.length == 1) { 
			return encodeArray(stringArr[0], array2DEscaper);
		}
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < stringArr.length; i++) {
			buf.append('(');
			buf.append(encodeArray(stringArr[i], array2DEscaper));
			buf.append(')');
		}
		return buf.toString();
	}
	
	public static String encodeArrayParentheses(String[] array) {
		if (array == null) return null;
		if (array.length == 0) return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			buf.append('(');
			buf.append(parenthesesEscaper.escapeComponent(array[i]));
			buf.append(')');
		}
		return buf.toString();
	}
	
	public static String[] decodeArray(String nms) {
		return decodeArray(nms, arrayEscaper);
	}
	
	public static int[] decodeIntArray(String nms) {
		if (nms == null) return null;
		String[] strings = decodeArray(nms);
		int[] ints = new int[strings.length];
		for (int i = 0; i < strings.length; i++) {
			try {
				ints[i] = Integer.parseInt(strings[i]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return ints;
	}
	
	public static String[] decodeArray(String nms, Escaper whichEscaper) {
		if (nms == null) return new String[0];
		nms = nms.trim();
		if (nms.length() == 0) return new String[0];
		return trimArray(whichEscaper.stringToComponents(nms));
	}
	
	public static String[] decodeArrayParentheses(String str) {
		if (str == null || str.length() < 1) return null;
		if (str.charAt(str.length() - 1) == ')') str = str.substring(0, str.length() - 1);
		String[] split = parenthesesEscaper.splitEscaped(str, ')');
		for (int i = 0; i < split.length; i++) {
			if (split[i].charAt(0) == '(') split[i] = split[i].substring(1);
			split[i] = parenthesesEscaper.unescapeComponent(split[i]);
		}
		return trimArray(split);
	}
	
	public static String[] trimArray(String[] arr) {
		if (arr.length==0) return null;
		for (int i = 0; i < arr.length; i++) {
			arr[i] = arr[i].trim();
			if (arr[i].length()==0) arr[i] = null;
		}
		return arr;
	}
	
	public static void set(OGCRequest req, String param, boolean value, boolean defaultVal) {
		if (value == defaultVal) req.set(param, null);
		else req.set(param, toWMSBoolean(value));
	}
	
	public static void set(OGCRequest req, String param, String value, String defaultVal) {
		if (value == defaultVal || value.equals(defaultVal)) req.set(param, null);
		else req.set(param, value);
	}
	
	public static boolean getBoolean(OGCRequest req, String param, boolean defaultVal) {
		String tr = req.get(param);
		if (tr == null) return defaultVal;
		return fromWMSBoolean(tr);
	}
	
	public static boolean setBoolean(OGCRequest req, String param, boolean value) {
		String prev = req.set(param, toWMSBoolean(value));
		return fromWMSBoolean(prev);
	}
	
	public static String getString(OGCRequest req, String param, String defaultVal) {
		String tr = req.get(param);
		if (tr == null) return defaultVal;
		return tr;
	}
	
	public static int getInt(OGCRequest req, String param, int defaultVal) {
		String tr = req.get(param);
		if (tr == null) return defaultVal;
		return Integer.parseInt(tr);
	}
	
	public static String[][] decode2DArray(String string) {
		if (string == null || string.length() == 0) return null;
		if (string.charAt(string.length() - 1) == ')') string = string
			.substring(0, string.length() - 1);
		String[] level1 = array2DEscaper.splitEscaped(string, ')');
		if (level1 == null || level1.length==0) return null; 
		String[][] ret = new String[level1.length][];
		for (int i = 0; i < level1.length; i++) {
			String cur = level1[i];
			if (cur.charAt(0) == '(') cur = cur.substring(1);
			ret[i] = decodeArray(cur, array2DEscaper);
		}
		return ret;
	}
	
	public static void main(String[] args) {
		// THIS FAILS
		String[][] split0 = decode2DArray(encode2DArray(new String[][]{{"aaa","bbb"}}));
		System.out.println(split0[0][0]);
		
		
		String[] split = decodeArray("item1,i\\,tem2,");
		for (int i = 0; i < split.length; i++) {
			System.out.println((i + 1) + ":" + split[i]);
		}
		System.out.println("--------");
		split = decodeArray(",,,");
		for (int i = 0; i < split.length; i++) {
			System.out.println((i + 1) + ":" + split[i]);
		}
		System.out.println("--------");
		String[][] split2 = decode2DArray(encode2DArray(new String[][]{{},null,{}}));
		for (int i = 0; i < split2.length; i++) {
			if (split2[i] == null) System.out.println((i + 1) + " is null");
			else if (split2[i].length == 0) System.out.println((i + 1) + " empty");
			else for (int j = 0; j < split2[i].length; j++) {
				System.out.println((i + 1) + "." + (j + 1) + ":" + split2[i][j]);
			}
		}
		System.out.println("--------");
		split2 = decode2DArray(encode2DArray(new String[][]{{"item11","item12"},{"item21","item(22)",null,"item,23"},{},{null,null}}));
		for (int i = 0; i < split2.length; i++) {
			if (split2[i] == null) System.out.println((i + 1) + " is null");
			else if (split2[i].length == 0) System.out.println((i + 1) + " empty");
			else for (int j = 0; j < split2[i].length; j++) {
				System.out.println((i + 1) + "." + (j + 1) + ":" + split2[i][j]);
			}
		}
		System.out.println("--------");
		split2 = decode2DArray("(<filter></filter>)(<filter></filter>)");
		for (int i = 0; i < split2.length; i++) {
			if (split2[i] == null) System.out.println((i + 1) + " is null");
			else if (split2[i].length == 0) System.out.println((i + 1) + " empty");
			else for (int j = 0; j < split2[i].length; j++) {
				System.out.println((i + 1) + "." + (j + 1) + ":" + split2[i][j]);
			}
		}
	}
	
	public static String[] getLayerNames(OGCRequest mapRequest, String paramName) {
		return decodeArray(mapRequest.get(paramName));
	}
	
	public static String[] getStyleNames(OGCRequest mapRequest, String paramName) {
		return decodeArray(mapRequest.get(paramName));
	}
	
	public static boolean hasArray(OGCRequest mapRequest, String paramName) {
		return mapRequest.get(paramName, "").length() > 0;
	}
	
	public static boolean isTransparent(OGCRequest mapRequest) {
		return getBoolean(mapRequest, OGCImageRequest.PARAM_TRANSPARENT, false);
	}
	
	public static boolean setTransparent(OGCRequest req, boolean trans) {
		return setBoolean(req, OGCImageRequest.PARAM_TRANSPARENT, trans);
	}
	
	public static void setCRS(OGCRequest req, CRS crs) {
		if ("1.1.1".equals(req.get(OGCRequest.PARAM_VERSION))) {
			req.set(WMSMapRequest.PARAM_SRS, crs.getCode());
		}
			
		req.set(WMSMapRequest.PARAM_CRS, crs.getCode());
	}
	
	public static CRS getCRS(OGCRequest req) {
		return CrsRepository.INSTANCE.get(getCrsCode(req));
	}
	
	public static String getCrsCode(OGCRequest req) {
		String crsCode = req.get(WMSMapRequest.PARAM_CRS);
		if("1.1.1".equals(req.get(OGCRequest.PARAM_VERSION))) {
			crsCode = req.get(WMSMapRequest.PARAM_SRS, crsCode);
		}
		return crsCode;
	}
	
	public static void setTransparentAdjustFormat(OGCRequest req, boolean trans) {
		setTransparent(req, trans);
		MimeType frm = getMimeType(req, OGCRequest.PARAM_FORMAT);
		if (trans && (frm == null || !MimeType.isTransparentImage(frm))) {
			setMimeType(req, OGCRequest.PARAM_FORMAT, MimeType.MIME_IMAGE_PNG);
		} else if (frm == null) {
			setMimeType(req, OGCRequest.PARAM_FORMAT, MimeType.MIME_IMAGE_JPG);
		}
	}
	
	public static MimeType getMimeType(OGCRequest req, String paramName) {
    	String frmStr = req.get(paramName, null);
    	if (frmStr==null) return null;
    	return MimeType.constructMimeType(frmStr);		
	}
	
	public static void setMimeType(OGCRequest req, String paramName, MimeType mime) {
		req.set(paramName, mime.createContentTypeString());
	}
	
	public static void setLayers(OGCRequest req, String paramName, String styleParamName, WMSLayerElement[] layers) {
		String[] names = new String[layers.length];
		String[] styles = null;
		for (int i = 0; i < layers.length; i++) {
			names[i] = layers[i].getWMSName();
			String stStr = layers[i].getWMSStyleName();
			if (stStr != null) {
				if (styles == null) styles = new String[layers.length];
				styles[i] = stStr;
			}
		}
		setLayerNames(req, paramName, names);
		setStyleNames(req, styleParamName, styles);
	}
	
	public static void setStyleNames(OGCRequest req, String styleParamName, String[] styleNames) {
		req.set(styleParamName, styleNames == null ? "" : WMSUtil.encodeArray(styleNames));
	}
	
	public static void setLayerNames(OGCRequest req, String paramName, String[] layerNames) {
		req.set(paramName, WMSUtil.encodeArray(layerNames));
	}
	
	public static void setDouble(OGCRequest req, String paramName, double value) {
		req.set(paramName, String.valueOf(value));
	}
	
	public static double getDouble(OGCRequest req, String paramName) {
		return Double.parseDouble(req.get(paramName));
	}

	public static String getWMSStyleFor(Layer lyr) {
		if (lyr instanceof WMSLayerElement) {
			return ((WMSLayerElement)lyr).getWMSStyleName();
		} else if (lyr.getStyle() instanceof NamedStyle) {
			return ((NamedStyle)lyr.getStyle()).getName();
		}
		return "";
	}

	public static String[] toWMSLayerNames(WMSLayerElement[] layers) {
		String[] names = new String[layers.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = layers[i].getWMSName();
		}
		return names;
	}
	
}
