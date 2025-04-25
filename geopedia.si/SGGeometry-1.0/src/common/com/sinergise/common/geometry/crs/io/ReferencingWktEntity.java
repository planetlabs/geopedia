package com.sinergise.common.geometry.crs.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ReferencingWktEntity {
	
	public static ReferencingWktEntity parseEntity(String wkt) {
		int sbidx = wkt.indexOf("[");
		String keyword = wkt.substring(0, sbidx);
		
		String rest = wkt.substring(sbidx+1, wkt.lastIndexOf(']'));
		List<String> params = new ArrayList<String>();
		List<ReferencingWktEntity> childs = new ArrayList<ReferencingWktEntity>();
		
		StringBuffer param = new StringBuffer();
		boolean quoted = false;
		for(int i=0; i<rest.length(); i++) {
			char c = rest.charAt(i);
			if(c == '"') {
				quoted = !quoted;
			} 
			if(c == '[' && !quoted) {
				StringBuffer entityWkt = param;
				int sb = 0; //start brackets count
				int cb = 0; //close brackets count
				for(;i<rest.length();i++) {
					c = rest.charAt(i);
					entityWkt.append(c);
					if(c == '[') sb++;
					else if (c == ']') cb++;
					
					if(sb > 0 && sb == cb) {
						childs.add(parseEntity(entityWkt.toString()));
						param.setLength(0);
						break;
					}
				}
			} else if(c != ',' || quoted) {
				param.append(c);
			} else if (c == ',' && param.length() > 0){
				quoted = false;
				params.add(param.toString());
				param.setLength(0);
			}
		}
		if(param.length() > 0)
			params.add(param.toString());
		
		return new ReferencingWktEntity(keyword.trim(), params, childs);
	}
	
	String keyword;
	List<String> params;
	List<ReferencingWktEntity> childs;
	
	public ReferencingWktEntity(String keyword, List<String> params, List<ReferencingWktEntity> childs) {
		this.keyword = keyword;
		this.params = params;
		this.childs = childs;
	}
	
	public ReferencingWktEntity(String keyword) {
		this.keyword = keyword;
	}

	public ReferencingWktEntity(String keyword, String param) {
		this(keyword);
		addParam(param);
	}

	public ReferencingWktEntity(String keyword, int intParam) {
		this(keyword, String.valueOf(intParam));
	}
	

	public ReferencingWktEntity(String keyword, double param) {
		this(keyword, String.valueOf(param));
	}

	public ReferencingWktEntity(String keyword, int[] intParams) {
		this(keyword);
		params = new ArrayList<String>(intParams.length);
		for (int i = 0; i < intParams.length; i++) {
			addParam(String.valueOf(intParams[i]));
		}
	}

	public ReferencingWktEntity(String keyword, double[] dblParams) {
		this(keyword);
		params = new ArrayList<String>(dblParams.length);
		for (int i = 0; i < dblParams.length; i++) {
			addParam(String.valueOf(dblParams[i]));
		}
	}

	public ReferencingWktEntity(String keyword, long[] inParams) {
		this(keyword);
		params = new ArrayList<String>(inParams.length);
		for (int i = 0; i < inParams.length; i++) {
			addParam(String.valueOf(inParams[i]));
		}
	}


	public void addParam(String param) {
		if (params == null) params = new ArrayList<String>(1);
		params.add(param);
	}

	public ReferencingWktEntity getChild(String childKeyword) {
		for(ReferencingWktEntity c : childs)
			if(c.keyword.equalsIgnoreCase(childKeyword))
				return c;
		return null;
	}
	
	public List<ReferencingWktEntity> getAllChildren(String childKeyword) {
		ArrayList<ReferencingWktEntity> ret=null;
		for(ReferencingWktEntity c : childs) {
			if(c.keyword.equalsIgnoreCase(childKeyword)) {
				if (ret==null) ret=new ArrayList<ReferencingWktEntity>();
				ret.add(c);
			}
		}
		return ret;
	}
	
	public ReferencingWktEntity getFirstChild() {
		return getChildAt(0);
	}
	
	public ReferencingWktEntity getChildAt(int index) {
		if(index >= 0 && index < childs.size())
			return childs.get(index);
		return null;
	}
	
	public String getFirstParam() {
		return getParamAt(0);
	}
	
	public String getParamAt(int index) {
		if(index >= 0 && index < params.size()) {
			String paramStr = params.get(index);
			if (paramStr.length()>1 && paramStr.charAt(0)=='"') {
				paramStr=paramStr.substring(1, paramStr.length()-1);
			}				
			return paramStr;
		}
		return null;
	}
	
	public double getDoubleParamAt(int index) {
		return Double.parseDouble(getParamAt(index));
	}
	

	public long getLongParamAt(int index) {
		return Long.parseLong(getParamAt(index));
	}
	
	public double getDoubleParamAt(int index, double defValue) {
		try {
			return Double.parseDouble(getParamAt(index));
		} catch (NumberFormatException nfe) {
			return defValue;
		}
	}
	
	@Override
	public String toString() {
		return toWKTString();
	}
	public String toWKTString() {
		StringBuilder sb = new StringBuilder();
		try {
			appendWKTString(sb);
		} catch(IOException e) {
			// Won't happen with StringBuilder
		}
		return sb.toString();
	}
	public void appendWKTString(Appendable sb) throws IOException {
		sb.append(keyword).append('[');
		int cnt = 0;
		if(params != null)
			for(String p : params) {
				if(cnt++ > 0) sb.append(',');
				boolean quote = (p.indexOf(',') >=0 || p.indexOf(']') >= 0);
				if (quote) sb.append('"');
				sb.append(p);
				if (quote) sb.append('"');
			}
		if(childs != null)
			for(ReferencingWktEntity c : childs){
				if(cnt++ > 0)
					sb.append(',');
				sb.append(c.toWKTString());
			}
		sb.append(']');
	}
	public String getKeyword() {
		return keyword;
	}

	public void addChild(ReferencingWktEntity child) {
		if (childs == null) childs = new ArrayList<ReferencingWktEntity>(1);
		childs.add(child);
	}

	public int getChildCount() {
		return childs==null ? 0 : childs.size();
	}

	/**
	 * Coordinate System WKT keywords
	 * 
	 * @author Teo Cerovski, Cosylab Ltd.
	 */
	public static interface ProjWKTConsts {

		public static final String GEOCCS = "GEOCCS";
		public static final String GEOGCS = "GEOGCS";
		public static final String PROJCS = "PROJCS";
		public static final String COMPD_CS = "COMPD_CS";
		public static final String FITTED_CS = "FITTED_CS";
		public static final String LOCAL_CS = "LOCAL_CS";
		public static final String PROJECTION = "PROJECTION";
		public static final String DATUM = "DATUM";
		public static final String SPHEROID = "SPHEROID";
		public static final String PRIMEM = "PRIMEM";
		public static final String UNIT = "UNIT";
		public static final String AUTHORITHY = "AUTHORITHY";
		public static final String VERT_CS = "VERT_CS";
		public static final String VERT_DATUM = "VERT_DATUM";
		public static final String AXIS = "AXIS";
		public static final String TOWGS84 = "TOWGS84";
		public static final String LOCAL_DATUM = "LOCAL_DATUM";
		public static final String PARAMETER = "PARAMETER";
		
		public static final String NORTH = "NORTH";
		public static final String EAST = "EAST";
		public static final String WEST = "WEST";
		public static final String SOUTH = "SOUTH";
		
		
		public static final String PPARAM_LAT0  = "Latitude_Of_Origin";
		public static final String PPARAM_LON0  = "Central_Meridian";
		public static final String PPARAM_SCALE = "Scale_Factor";
		public static final String PPARAM_OFFX  = "False_Easting";
		public static final String PPARAM_OFFY  = "False_Northing";
		
		public static final String[] KEYWORDS = new String[]{
			GEOCCS,
			GEOGCS,
			PROJCS,
			COMPD_CS,
			FITTED_CS,
			LOCAL_CS,
			PROJECTION,
			DATUM,
			SPHEROID,
			PRIMEM,
			UNIT,
			AUTHORITHY,
			VERT_CS,
			VERT_DATUM,
			AXIS,
			TOWGS84,
			LOCAL_DATUM,
			PARAMETER
		};
	}

	public static ReferencingWktEntity parseEntityASCII(InputStream is) throws IOException {
		boolean inQuote = false;
		int bracketCnt = 0;
		StringBuilder ret = new StringBuilder();
		while (true) {
			int read = is.read();
			if (read<0) {
				return parseEntity(ret.toString());
			}
			char ch = (char)read;
			ret.append(ch);
			if (inQuote) {
				if (ch=='"') inQuote = false;
			}  else {
				if (ch == '"') inQuote = true;
				else if (ch == '[') bracketCnt++;
				else if (ch == ']') {
					bracketCnt--;
					if (bracketCnt == 0) {
						return parseEntity(ret.toString());
					}
				}
			}
		}
	}
}