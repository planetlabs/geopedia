package com.sinergise.common.geometry.service.util;

import com.sinergise.common.geometry.geom.Geometry;

public class BinaryGeomOpRequest extends GeomOpRequest {
	
	private static final long serialVersionUID = -6800273426347553468L;
	
	private Geometry[] args;

	@Deprecated /** Serialization only */
	protected BinaryGeomOpRequest() { }
	
	public BinaryGeomOpRequest(Geometry ...args) {
		if (args.length < 2) {
			throw new IllegalArgumentException("At least two arguments required");
		}
		this.args = args;
	}
	
	public BinaryGeomOpRequest(double gridSize, Geometry ...args) {
		this(args);
		setGridSize(gridSize);
	}
	
	//constructor for backward compatibility
	public BinaryGeomOpRequest(Geometry a, Geometry b, double gridSize) {
		this(gridSize, a, b);
	}
	
	public Geometry getA() {
		return args[0];
	}
	
	public Geometry getB() {
		return args[1];
	}
	
	public Geometry[] getArguments() {
		return args;
	}

}
