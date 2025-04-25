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

package com.sinergise.java.util.state.impl;

import java.io.IOException;

import com.sinergise.java.util.state.StateStorage;

/**
 * This class is an implementation of the <code>StateStorage</code> that uses the CDB server to store the array of the <code>State</code>
 * objects.
 * 
 * @author dvitas
 */
public class CDBStateStorage extends XMLStateStorage {
	/**
			 *
			 */
	public CDBStateStorage() {
		super();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param other
	 */
	public CDBStateStorage(final StateStorage other) {
		super(other);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#load(java.io.InputStream)
	 */
	@Override
	public void load(final String filePath, final String applicationName) throws IOException {
		System.err.println("CDB is not activated. Using the base class!");
		super.load(filePath, applicationName);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#store(java.io.OutputStream)
	 */
	@Override
	public void store(final String filePath, final String applicationName) throws IOException {
		System.err.println("CDB is not activated. Using the base class!");
		super.store(filePath, applicationName);
	}
}

/* __oOo__ */
