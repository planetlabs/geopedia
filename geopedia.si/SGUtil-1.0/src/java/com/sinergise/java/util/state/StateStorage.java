/*
 * Copyright (c) 2004 by Cosylab d.o.o.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * This interface should be implemented by the objects which will be used for storing and restoring an array of the <code>State</code>
 * objects.
 * 
 * @author dvitas
 */
public interface StateStorage {
	/**
	 * Adds new state to storage.
	 * 
	 * @param state the state to be added
	 */
	public void add(State state);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param states DOCUMENT ME!
	 */
	public void addAll(List<State> states);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List<State> getStates();
	
	/**
	 * DOCUMENT ME! TODO: Add load(InputStream, String) to support loading from non-file sources!
	 * 
	 * @param filePath DOCUMENT ME!
	 * @param applicationName DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public void load(String filePath, String applicationName) throws IOException;
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param stream DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public void load(InputStream stream) throws IOException;
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param filePath DOCUMENT ME!
	 * @param applicationName DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public void store(String filePath, String applicationName) throws IOException;
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param stream
	 * @throws IOException DOCUMENT ME!
	 */
	public void store(OutputStream stream) throws IOException;
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param st DOCUMENT ME!
	 */
	public void remove(State st);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param st DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public boolean contains(State st);
	
	/**
	 * Rerurns iterator over states.
	 * 
	 * @return iterator over states
	 */
	public Iterator<State> iterator();
}

/* __oOo__ */
