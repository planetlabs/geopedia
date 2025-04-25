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

import java.io.IOException;

/**
 * This interface is implemented by the object which will take care for the application state saving and restoring.
 * 
 * @author dvitas
 */
public interface StateKeeper {
	/**
	 * DOCUMENT ME!
	 * 
	 * @param filePath DOCUMENT ME!
	 * @param applicationName DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public void save(String filePath, String applicationName) throws IOException;
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param filePath DOCUMENT ME!
	 * @param applicationName DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public void restore(String filePath, String applicationName) throws IOException;
}

/* __oOo__ */
