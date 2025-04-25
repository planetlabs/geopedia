/**
 * 
 */
package com.sinergise.common.gis.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.util.ArrayUtil;


/**
 * Represents Filter capabilities.
 * 
 * @author tcerovski
 */
@SuppressWarnings("boxing")
public class FilterCapabilities implements Serializable, GeometryTypes {
	
	private static final long serialVersionUID = 1L;

	/* Operations masks */
	
	public static final int NO_OP = 0;
	
	public static final int SPATIAL_OP_BBOX = 0x01;
	
	public static final int SPATIAL_OP_EQUALS = 0x01<<1;
	
	public static final int SPATIAL_OP_DISJOINT = 0x01<<2;
	
	public static final int SPATIAL_OP_INTERSECT = 0x01<<3;
	
	public static final int SPATIAL_OP_TOUCHES = 0x01<<4;
	
	public static final int SPATIAL_OP_CROSSES = 0x01<<5;
	
	public static final int SPATIAL_OP_WITHIN = 0x01<<6;
	
	public static final int SPATIAL_OP_CONTAINS = 0x01<<7;
	
	public static final int SPATIAL_OP_OVERLAPS = 0x01<<8;
	
	public static final int SPATIAL_OP_BEYOND = 0x01<<9;
	
	public static final int SCALAR_OP_LOGICAL_AND = 0x01<<10;
	
	public static final int SCALAR_OP_LOGICAL_OR = 0x01<<11;
	
	public static final int SCALAR_OP_LOGICAL_NOT = 0x01<<12;
	
	public static final int SCALAR_OP_COMP_LESSTHAN = 0x01<<15;
	
	public static final int SCALAR_OP_COMP_GREATERTHAN = 0x01<<16;
	
	public static final int SCALAR_OP_COMP_LESSTHAN_EQUALTO = 0x01<<17;
	
	public static final int SCALAR_OP_COMP_GREATERTHAN_EQUALTO = 0x01<<18;
	
	public static final int SCALAR_OP_COMP_EQUALTO = 0x01<<19;
	
	public static final int SCALAR_OP_COMP_NOTEQUALTO = 0x01<<20;
	
	public static final int SCALAR_OP_COMP_LIKE = 0x01<<21;
	
	public static final int SCALAR_OP_COMP_BETWEEN = 0x01<<22;
	
	public static final int SCALAR_OP_COMP_NULLCHECK = 0x01<<23;
	
	public static final int SCALAR_OP_COMP_CONTAINS = 0x01<<24;
	
	public static final int FID_OP = 0x01<<30;
	
	/* Convenience operations masks */
	
	public static final int SPATIAL_OPS = 
		SPATIAL_OP_BBOX | SPATIAL_OP_BEYOND | SPATIAL_OP_CONTAINS | SPATIAL_OP_CROSSES | 
		SPATIAL_OP_DISJOINT | SPATIAL_OP_EQUALS | SPATIAL_OP_INTERSECT | SPATIAL_OP_OVERLAPS | 
		SPATIAL_OP_TOUCHES | SPATIAL_OP_WITHIN;
										
	public static final int LOGICAL_UNARY_OPS = SCALAR_OP_LOGICAL_NOT;
	
	public static final int LOGICAL_BINARY_OPS = SCALAR_OP_LOGICAL_AND | SCALAR_OP_LOGICAL_OR;
	
	public static final int LOGICAL_OPS = LOGICAL_BINARY_OPS | LOGICAL_UNARY_OPS;
	
	public static final int COMPARISON_UNARY_OPS = SCALAR_OP_COMP_NULLCHECK;
	
	public static final int COMPARISON_BINARY_OPS = 
		SCALAR_OP_COMP_EQUALTO | SCALAR_OP_COMP_GREATERTHAN | 
		SCALAR_OP_COMP_GREATERTHAN_EQUALTO | SCALAR_OP_COMP_LESSTHAN | 
		SCALAR_OP_COMP_LESSTHAN_EQUALTO | SCALAR_OP_COMP_LIKE | SCALAR_OP_COMP_NOTEQUALTO | SCALAR_OP_COMP_CONTAINS;
	
	public static final int COMPARISON_TERNARY_OPS = SCALAR_OP_COMP_BETWEEN;
	
	public static final int COMPARISON_OPS = 
		COMPARISON_UNARY_OPS | COMPARISON_BINARY_OPS | COMPARISON_TERNARY_OPS;
	
	public static final int BASIC_GEOM_TYPES = 
		GEOM_TYPE_ENVELOPE | GEOM_TYPE_POINT | GEOM_TYPE_LINESTRING | GEOM_TYPE_POLYGON;
	
	
	/* Functions masks */
	
	public static final int FUNCT_IDENTITY = 0x0;
	
	public static final int SPATIAL_FUNCT_BUFFER = 0x01<<1;
	public static final int SPATIAL_FUNCT_UNION = 0x0<<2;
	public static final int SPATIAL_FUNCT_LENGTH = 0x01<<6;
	public static final int SPATIAL_FUNCT_AREA = 0x01<<7;
	public static final int SPATIAL_FUNCT_DISTANCE = 0x01<<8;
	
	public static final int SCALAR_FUNCT_ADD = 0x01<<15;
	public static final int SCALAR_FUNCT_SUB = 0x01<<16;
	public static final int SCALAR_FUNCT_MUL = 0x01<<17;
	public static final int SCALAR_FUNCT_DIV = 0x01<<18;
	public static final int SCALAR_FUNCT_REMAIN = 0x01<<19;
	
	public static final int SCALAR_FUNCT_MIN = 0x01<<20;
	public static final int SCALAR_FUNCT_MAX = 0x01<<21;
	public static final int SCALAR_FUNCT_SIN = 0x01<<22;
	public static final int SCALAR_FUNCT_COS = 0x01<<23;
	public static final int SCALAR_FUNCT_TAN = 0x01<<24;
	
	public static final int SCALAR_FUNCT_STRING_UPPERCASE = 0x01<<27;
	public static final int SCALAR_FUNCT_STRING_LOWERCASE = 0x01<<28;
	public static final int SCALAR_FUNCT_STRING_SUBSTRING = 0x01<<29;
	public static final int SCALAR_FUNCT_STRING_LENGTH = 0x01<<30;
	
	/* Convenience functions masks */
	
	public static final int SCALAR_FUNCT_SIMPLE_ARITHMETIC = 
		SCALAR_FUNCT_ADD | SCALAR_FUNCT_SUB | SCALAR_FUNCT_MUL | SCALAR_FUNCT_DIV | SCALAR_FUNCT_REMAIN;
	
	public static final int SCALAR_FUNCT_BASIC_STRING_MANIPULATION = 
		SCALAR_FUNCT_STRING_UPPERCASE | SCALAR_FUNCT_STRING_LOWERCASE | SCALAR_FUNCT_STRING_SUBSTRING;
	
	public static final int STRING_FUNCTIONS = SCALAR_FUNCT_BASIC_STRING_MANIPULATION;
	
	public static final int SPATIAL_FUNCTIONS = 
		SPATIAL_FUNCT_BUFFER | SPATIAL_FUNCT_UNION | SPATIAL_FUNCT_LENGTH | SPATIAL_FUNCT_AREA | SPATIAL_FUNCT_DISTANCE;
	
	/** Supported operations mask */
	private int operations;
	
	/** Supported functions mask */
	private int functions;
	
	/** Supported geometry types mask */
	private int geometryTypes;
	
	public FilterCapabilities() {
		this(NO_OP, GEOM_TYPE_NONE);
	}
	
	public FilterCapabilities(int opMask) {
		this(opMask, GEOM_TYPE_NONE);
	}
	
	public FilterCapabilities(int opMask, int geomTypeMask) {
		this(opMask, geomTypeMask, FUNCT_IDENTITY);
	}
	
	public FilterCapabilities(int opMask, int geomTypeMask, int funMask) {
		this.operations = opMask;
		this.geometryTypes = geomTypeMask;
		this.functions = funMask;
	}
	
	public int operations() {
		return operations;
	}
	
	public int functions() {
		return functions;
	}
	
	public boolean supports(FilterDescriptor filter) {
		if(filter instanceof SpatialOperation)
			return supportsOperation(filter.getOperationsMask())
				&& supportsGeometryType(((SpatialOperation)filter).getGeometriesTypeMask());
		return supportsOperation(filter.getOperationsMask());
	}
	
	public boolean supports(FilterCapabilities filterCaps) {
		return supportsOperation(filterCaps.operations)
				&& supportsGeometryType(filterCaps.geometryTypes)
				&& supportsFunction(filterCaps.functions);
	}
	
	public boolean supportsOperation(int opMask) {
		return (opMask & operations) == opMask;
	}
	
	public boolean supportsGeometryType(int geomTypeMask) {
		return (geomTypeMask & geometryTypes) == geomTypeMask;
	}
	
	public boolean supportsFunction(int functMask) {
		return (functMask & functions) == functMask;
	}
	
	public void addCapability(FilterCapabilities caps) {
		addOperationCapability(caps.operations);
		addFunctionCapability(caps.functions);
		addGeomTypeCapability(caps.geometryTypes);
	}
	
	public void addOperationCapability(int opMask) {
		operations |= opMask;
	}
	
	public void addFunctionCapability(int functMask) {
		functions |= functMask;
	}
	
	public void addGeomTypeCapability(int geomTypeMask) {
		geometryTypes |= geomTypeMask;
	}
	
	public static String getOperationSymbol(int operation) {
		switch (operation) {
		case SCALAR_OP_COMP_EQUALTO: return "=";
		case SCALAR_OP_COMP_NOTEQUALTO: return "!=";
		case SCALAR_OP_COMP_GREATERTHAN: return ">";
		case SCALAR_OP_COMP_GREATERTHAN_EQUALTO: return ">=";
		case SCALAR_OP_COMP_LESSTHAN: return "<";
		case SCALAR_OP_COMP_LESSTHAN_EQUALTO: return "<=";
		case SCALAR_OP_COMP_LIKE: return "LIKE";
		default: return "Unknown operation";
		}
	}
	
	public static int[] splitMaskToOps(int opMask) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i=0; i<32; i++) {
			int m = 0x01<<i;
			if((m&opMask)==m)
				list.add(Integer.valueOf(m));
		}
		return ArrayUtil.toIntArray(list);
	}
	
	public static FilterCapabilities resolveCapabilities(String caps) {
		if (caps.equalsIgnoreCase("FULL")) {
			return new FilterCapabilities(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		}
		
		int operations = NO_OP;
		int geomTypes = Integer.MAX_VALUE;
		int functions = Integer.MAX_VALUE;
		
		String[] parts = caps.split(",");

		if (parts.length > 0) {
			operations = parseFilterMask(parts[0], operations);
		}
		if (parts.length > 1) {
			geomTypes = parseFilterMask(parts[1], geomTypes);
		}
		if (parts.length > 2) {
			functions = parseFilterMask(parts[2], functions);
		}
		
		return new FilterCapabilities(operations, geomTypes, functions);
	}
	
	private static int parseFilterMask(String maskStr, int mask) {
		//try if integer mask already passed
		try {
			return mask | Integer.parseInt(maskStr);
		} catch (NumberFormatException e) {
			//ignore, parse string
		}
		
		StringBuilder part = new StringBuilder();
		boolean remove = false;
		
		for (int i=0; i<maskStr.length(); i++) {
			char c = maskStr.charAt(i);
			if ('+' == c || '-' == c || i == maskStr.length()-1) {
				if (i == maskStr.length()-1) {
					part.append(c); //add last character
				}
				if (part.length() > 0) {
					Integer partBits = NAMED_FILTER_CAPABILITIES.get(part.toString().trim());
					if (partBits != null) {
						if (remove) {
							mask ^= partBits.intValue();
						} else {
							mask |= partBits.intValue();
						}
					}
					part.setLength(0);
				}
				
				remove = '-' == c;
			} else {
				part.append(c);
			}
		}
		
		return mask;
	}
	
	private static Map<String, Integer> NAMED_FILTER_CAPABILITIES;
	
	{
		Map<String, Integer> namedCaps = new HashMap<String, Integer>();
		namedCaps.put("FULL", Integer.MAX_VALUE);
		namedCaps.put("NO_OP", NO_OP);
		
		namedCaps.put("SPATIAL_OP_BBOX", SPATIAL_OP_BBOX);
		namedCaps.put("SPATIAL_OP_EQUALS", SPATIAL_OP_EQUALS);
		namedCaps.put("SPATIAL_OP_DISJOINT", SPATIAL_OP_DISJOINT);
		namedCaps.put("SPATIAL_OP_INTERSECT", SPATIAL_OP_INTERSECT);
		namedCaps.put("SPATIAL_OP_TOUCHES", SPATIAL_OP_TOUCHES);
		namedCaps.put("SPATIAL_OP_CROSSES", SPATIAL_OP_CROSSES);
		namedCaps.put("SPATIAL_OP_WITHIN", SPATIAL_OP_WITHIN);
		namedCaps.put("SPATIAL_OP_CONTAINS", SPATIAL_OP_CONTAINS);
		namedCaps.put("SPATIAL_OP_OVERLAPS", SPATIAL_OP_OVERLAPS);
		namedCaps.put("SPATIAL_OP_BEYOND", SPATIAL_OP_BEYOND);
		namedCaps.put("SPATIAL_OPS", SPATIAL_OPS);
		
		namedCaps.put("LOGICAL_UNARY_OPS", LOGICAL_UNARY_OPS);
		namedCaps.put("LOGICAL_BINARY_OPS", LOGICAL_BINARY_OPS);
		namedCaps.put("LOGICAL_OPS", LOGICAL_OPS);
		
		namedCaps.put("COMPARISON_UNARY_OPS", COMPARISON_UNARY_OPS);
		namedCaps.put("COMPARISON_BINARY_OPS", COMPARISON_BINARY_OPS);
		namedCaps.put("COMPARISON_TERNARY_OPS", COMPARISON_TERNARY_OPS);
		namedCaps.put("COMPARISON_OPS", COMPARISON_OPS);
		
		namedCaps.put("GEOM_TYPE_NONE", GEOM_TYPE_NONE);
		namedCaps.put("GEOM_TYPE_ENVELOPE", GEOM_TYPE_ENVELOPE);
		namedCaps.put("GEOM_TYPE_POINT", GEOM_TYPE_POINT);
		namedCaps.put("GEOM_TYPE_LINESTRING", GEOM_TYPE_LINESTRING);
		namedCaps.put("GEOM_TYPE_POLYGON", GEOM_TYPE_POLYGON);
		namedCaps.put("BASIC_GEOM_TYPES", BASIC_GEOM_TYPES);
		
		namedCaps.put("FUNCT_IDENTITY", FUNCT_IDENTITY);
		namedCaps.put("SPATIAL_FUNCT_BUFFER", SPATIAL_FUNCT_BUFFER);
		namedCaps.put("SPATIAL_FUNCT_UNION", SPATIAL_FUNCT_UNION);
		namedCaps.put("SPATIAL_FUNCT_LENGTH", SPATIAL_FUNCT_LENGTH);
		namedCaps.put("SPATIAL_FUNCT_AREA", SPATIAL_FUNCT_AREA);
		namedCaps.put("SPATIAL_FUNCT_DISTANCE", SPATIAL_FUNCT_DISTANCE);
		namedCaps.put("SPATIAL_FUNCTIONS", SPATIAL_FUNCTIONS);
		
		namedCaps.put("SCALAR_FUNCT_STRING_UPPERCASE", SCALAR_FUNCT_STRING_UPPERCASE);
		namedCaps.put("SCALAR_FUNCT_STRING_LOWERCASE", SCALAR_FUNCT_STRING_LOWERCASE);
		namedCaps.put("SCALAR_FUNCT_STRING_SUBSTRING", SCALAR_FUNCT_STRING_SUBSTRING);
		namedCaps.put("SCALAR_FUNCT_STRING_LENGTH", SCALAR_FUNCT_STRING_LENGTH);
		namedCaps.put("SCALAR_FUNCT_BASIC_STRING_MANIPULATION", SCALAR_FUNCT_BASIC_STRING_MANIPULATION);
		namedCaps.put("STRING_FUNCTIONS", STRING_FUNCTIONS);
		
		NAMED_FILTER_CAPABILITIES = Collections.unmodifiableMap(namedCaps);
	}
	
	
}
