/**
 * 
 */
package com.sinergise.common.geometry.geom;

/**
 * @author tcerovski
 */
public interface GeometryTypes {

	/* Geometry operand masks */
	
	public static final int GEOM_TYPE_NONE = 0;
	
	public static final int GEOM_TYPE_ENVELOPE = 0x01;
	
	public static final int GEOM_TYPE_POINT = 0x01<<1;
	
	public static final int GEOM_TYPE_LINESTRING = 0x01<<2;
	
	public static final int GEOM_TYPE_POLYGON = 0x01<<3;
	
	public static final int GEOM_TYPE_ARC_BY_CENTERPOINT = 0x01<<4;
	
	public static final int GEOM_TYPE_CIRCLE_BY_CENTERPOINT = 0x01<<5;
	
	public static final int GEOM_TYPE_ARC = 0x01<<6;
	
	public static final int GEOM_TYPE_CIRCLE = 0x01<<7;
	
	public static final int GEOM_TYPE_ARC_BY_BULGE = 0x01<<8;
	
	public static final int GEOM_TYPE_BEZIER = 0x01<<9;
	
	public static final int GEOM_TYPE_CLOTHOID = 0x01<<10;
	
	public static final int GEOM_TYPE_CUBIC_SPLINE = 0x01<<11;
	
	public static final int GEOM_TYPE_GEODESIC = 0x01<<12;
	
	public static final int GEOM_TYPE_OFFSET_CURVE = 0x01<<13;
	
	public static final int GEOM_TYPE_TRIANGLE = 0x01<<14;
	
	public static final int GEOM_TYPE_POLYHEDRIAL_SURFACE = 0x01<<15;
	
	public static final int GEOM_TYPE_TRIANGULATED_SURFACE = 0x01<<16;
	
	public static final int GEOM_TYPE_TIN = 0x01<<17;
	
	public static final int GEOM_TYPE_SOLID = 0x01<<18;
	
}
