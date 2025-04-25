/**
 * 
 */
package com.sinergise.common.geometry.topo;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;

public class TopologyException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	protected Envelope location = null;
	
	@Deprecated /** Serialization only */
	protected TopologyException() { }
	
	public TopologyException(Throwable cause) {
		this(cause, null);
	}
	
	public TopologyException(Throwable cause, Envelope location) {
		super(cause);
		this.location = location;
	}
	
	public TopologyException(String msg) {
		this(msg, (Envelope)null);
	}
	
	public TopologyException(String msg, HasCoordinate location) {
		this(msg, location != null 
			? new Envelope(location.x(), location.y(), location.x(), location.y()) 
			: null);
	}
	
	public TopologyException(String msg, Envelope location) {
		super(msg);
		this.location = location;
	}
	
	public boolean hasLocation() {
		return location != null;
	}
	
	public Envelope getLocation() {
		return location;
	}
	
}
