package com.sinergise.common.util.geom;

import java.io.Serializable;

/**
 * Describes basic map display specifications.
 * 
 * @author tcerovski
 */
public class MapDisplaySpec implements Serializable {

	private static final long serialVersionUID = -7524394182765022713L;
	
	private Envelope mbr;
	private double scale;
	private double dpi;
	
	public MapDisplaySpec(Envelope mbr, double scale, double dpi) {
		this.mbr = mbr;
		this.scale = scale;
		this.dpi = dpi;
	}

	/**
	 * @return Map world rectangle
	 */
	public Envelope getMbr() {
		return mbr;
	}

	/**
	 * @return Map scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @return Map DPI (Dots Per Inch)
	 */
	public double getDpi() {
		return dpi;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MapSpec [mbr=" + mbr + ", scale=" + scale + ", dpi=" + dpi + "]";
	}
	
}
