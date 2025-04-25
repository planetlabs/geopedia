/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 * 
 * The full license specifying the redistribution, modification, usage and
 * other rights and obligations is included with the distribution of this
 * project in the file license.html. If the license is not included you may
 * find a copy at http://www.cosylab.com/legal/abeans_license.htm or may write
 * to Cosylab, d.o.o.
 * 
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE,
 * MODIFICATION, OR REDISTRIBUTION OF THIS SOFTWARE.
 */
package com.sinergise.java.util.state.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.StateStorage;

/**
 * This class implements the state storage and saves states into a binary file using simple JAVA serialization.
 * 
 * @author dvitas
 */
public class SerialStateStorage extends DefaultStateStorage {
	/**
	 *  
	 */
	public SerialStateStorage() {
		super();
	}
	
	/**
	 * Creates a new SerialStateStorage object.
	 * 
	 * @param ss DOCUMENT ME!
	 */
	public SerialStateStorage(final StateStorage ss) {
		super(ss);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#load(java.io.InputStream)
	 */
	@Override
	public void load(final String filePath, final String applicationName) throws IOException {
		if (delegate != null) {
			delegate.load(filePath, applicationName);
			addAll(delegate.getStates());
			
			return;
		}
		
		// get the InputStream if file exists
		final InputStream is = getInputStream(filePath, applicationName + ".bin");
		load(is);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#load(java.io.InputStream)
	 */
	@Override
	public void load(final InputStream is) throws IOException {
		try {
			if (is == null) {
				return;
			}
			
			final ObjectInputStream ois = new ObjectInputStream(is);
			
			while (true) {
				states.add((State)ois.readObject());
			}
		} catch(final ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		} catch(final EOFException e) {
			// end of stream
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#store(java.io.OutputStream, com.cosylab.application.state.State[])
	 */
	@Override
	public void store(final String filePath, final String applicationName) throws IOException {
		final OutputStream os = getOutputStream(filePath, applicationName + ".bin");
		store(os);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#store(java.io.OutputStream)
	 */
	@Override
	public void store(final OutputStream os) throws IOException {
		if (os == null) {
			return;
		}
		
		final ObjectOutputStream oos = new ObjectOutputStream(os);
		
		for (int i = 0; i < states.size(); i++) {
			oos.writeObject(states.get(i));
		}
		
		os.flush();
		os.close();
	}
}

/* __oOo__ */
