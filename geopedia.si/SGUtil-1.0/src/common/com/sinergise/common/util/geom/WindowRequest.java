package com.sinergise.common.util.geom;

public class WindowRequest {
	protected Envelope env;
	protected double scale; 
	
	public WindowRequest(double cX, double cY, double scale) {
		this.env = new Envelope(cX, cY, cX, cY);
		this.scale = scale;
	}
	
	public WindowRequest(Envelope env) {
		this.env = env;
		this.scale = Double.NaN;
	}
	
	/**
	 * @return Envelope to display. If scale is set, only center of the envelope should be used.
	 */
	public Envelope getEnvelope() {
		return env;
	}
	
	/**
	 * @return requested scale (in worldLenPerDisp), or NaN if only Envelope should be used
	 */
	public double getScale() {
		return scale;
	}
}
