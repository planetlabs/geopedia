/**
 * 
 */
package com.sinergise.common.geometry.topo;

import java.util.Collection;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;


/**
 * @author tcerovski
 */
public class TopoValidationException extends TopologyException {

	private static final long serialVersionUID = 1L;
	
	private Collection<TopoValidationException> exceptions;
	
	private int errorFlag;
	
	@Deprecated /** Serialization only */
	protected TopoValidationException() { }
	
	public TopoValidationException(String msg, int errorFlag) {
		super(msg);
		this.errorFlag = errorFlag;
	}
	
	public TopoValidationException(String msg, int errorFlag, Envelope env) {
		super(msg, env);
		this.errorFlag = errorFlag;
	}

	public TopoValidationException(String msg, Collection<TopoValidationException> exceptions) {
		super(msg);
		this.exceptions = exceptions;
		
		//get envelope for all exceptions
		EnvelopeBuilder env = new EnvelopeBuilder();
		for(TopoValidationException ex : exceptions) {
			if(ex.hasLocation())
				env.expandToInclude(ex.getLocation());
		}
		if(!env.isEmpty()) {
			this.location = env.getEnvelope();
		}
	}
	
	public boolean hasMultipleErrors() {
		return exceptions != null && exceptions.size() > 0;
	}
	
	public Collection<TopoValidationException> getErrors() {
		return exceptions;
	}
	
	public int getErrorFlag() {
		if (hasMultipleErrors()) {
			int flag = 0;
			for (TopoValidationException e : exceptions) {
				flag |= e.getErrorFlag();
			}
			return flag;
		}
		return errorFlag;
	}
	
}
