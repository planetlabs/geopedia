/**
 * 
 */
package com.sinergise.common.geometry.topo;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinate;

/**
 * @author tcerovski
 */
public class TopoLockException extends TopologyException {

	private static final long serialVersionUID = 1L;
	
	private final TopoElement el;
	
	public TopoLockException(TopoElement el) {
		super(el.getName()+" is locked");
		this.el = el;
	}
	
	public TopoLockException(TopoElement el, HasCoordinate location) {
		super(el.getName()+" is locked", location);
		this.el = el;
	}
	
	public TopoLockException(TopoElement el, Envelope location) {
		super(el.getName()+" is locked", location);
		this.el = el;
	}
	
	public TopoElement getElement() {
		return el;
	}
	
}
