/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package com.sinergise.java.util.state;

/**
 * This interface has to be implemented by the components which will exchange its state with the <code>StateKeeper</code>
 * 
 * @author dvitas
 */
public interface StateOriginator {
	/**
	 * Will be called by the <code>StateKeeper</code> in the process of the application state saving. The component which implements this
	 * interface will tipycally use <code>StateFactory</code> to create a <code>State</code> object and fill it with needed values. For
	 * example a component can use this snippet.
	 * 
	 * <pre>
	 * <code>
	 * public State getState() {
	 * 	State s = StateFactory.createState();
	 * 	s.putDouble("Min", getMinimum());
	 * 	s.putDouble("Max", getMaximum());
	 * 	s.putString("Format", getFormat());
	 * 	s.putString("Units", getUnits());
	 * 	return s;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @return state object
	 */
	public State getState();
	
	/**
	 * Will be called by the <code>StateKeepr</code> when it reads <code>State</code> object which belongs to this component.
	 * 
	 * @param state object previously created by this component
	 */
	public void setState(State state);
}

/* __oOo__ */
