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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.StateStorage;

/**
 * This class is an abstract class that can be extended by the <code>StateStorage</code> implementators.
 * 
 * @author dvitas
 */
abstract public class DefaultStateStorage implements StateStorage {
	protected ArrayList<State> states   = new ArrayList<State>();
	protected StateStorage     delegate = null;
	
	/**
			 *
			 */
	public DefaultStateStorage() {
		super();
	}
	
	/**
	 * Creates a new DefaultStateStorage object.
	 * 
	 * @param other DOCUMENT ME!
	 */
	public DefaultStateStorage(final StateStorage other) {
		super();
		delegate = other;
		
		// states.addAll(other.getStates());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#add(com.cosylab.application.state.State)
	 */
	@Override
	public void add(final State state) {
		states.add(state);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#add(java.util.ArrayList)
	 */
	@Override
	public void addAll(final List<State> statesToAdd) {
		this.states.addAll(statesToAdd);
	}
	
	protected InputStream getInputStream(final String filePath, final String fileName) {
		// if file exists
		final File file = new File(filePath + File.separatorChar + fileName);
		
		if (!file.exists() || !file.canRead()) {
			return null;
		}
		
		BufferedInputStream bis;
		
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
		} catch(final FileNotFoundException e) {
			return null;
		}
		
		return bis;
	}
	
	protected OutputStream getOutputStream(final String filePath, final String fileName) throws IOException {
		// prepare the file object
		final File file = new File(filePath + File.separatorChar + fileName);
		
		// make path to the file if it doesn't exists jet
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		// create output stream
		return new BufferedOutputStream(new FileOutputStream(file));
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#load(java.io.InputStream)
	 */
	@Override
	abstract public void load(String filePath, String applicationName) throws IOException;
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param filePath DOCUMENT ME!
	 * @param applicationName DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	@Override
	abstract public void store(String filePath, String applicationName) throws IOException;
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#getStates()
	 */
	@Override
	public List<State> getStates() {
		return states;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#contains(com.cosylab.application.state.State)
	 */
	@Override
	public boolean contains(final State st) {
		return states.contains(st);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#iterator()
	 */
	@Override
	public Iterator<State> iterator() {
		return states.iterator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#remove(com.cosylab.application.state.State)
	 */
	@Override
	public void remove(final State st) {
		states.remove(st);
	}
	
}

/* __oOo__ */
