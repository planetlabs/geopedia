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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.StateFactory;
import com.sinergise.java.util.state.StateStorage;

/**
 * This class is an implementation of the <code>StateStorage</code> that uses simple text file to store the array of the <code>State</code>
 * objects. Stored file is the plain <code>java.util.Properties</code> file format with the few 'weird' properties added which are used for
 * fast hierarchy resolving.
 * 
 * @author dvitas
 */
public class TextStateStorage extends DefaultStateStorage {
	protected final static String stateStart     = "<State>";
	protected final static String stateEnd       = "<EndState>";
	protected final static String childDelimiter = "<Child>";
	
	/**
                     *
                     */
	public TextStateStorage() {
		super();
	}
	
	/**
	 * Creates a new TextStateStorage object.
	 * 
	 * @param ss DOCUMENT ME!
	 */
	public TextStateStorage(final StateStorage ss) {
		super(ss);
	}
	
	private void readState(final BufferedReader br, final State state) throws IOException {
		String line;
		
		while (true) {
			line = br.readLine();
			
			if (line == null) {
				break;
			}
			
			line = line.trim();
			
			if (line.startsWith(stateEnd)) {
				break;
			}
			
			if (line.startsWith(childDelimiter)) {
				final String childKey = line.substring(childDelimiter.length());
				final State s = state.createState(childKey);
				br.readLine(); // eat state header
				readState(br, s);
				
				continue;
			}
			
			final int index = line.indexOf('=');
			final String key = line.substring(0, index);
			final String value = line.substring(index + 1);
			state.putString(key, value);
		}
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
		final InputStream is = getInputStream(filePath, applicationName + ".txt");
		load(is);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#load(java.io.InputStream)
	 */
	@Override
	public void load(final InputStream is) throws IOException {
		if (is == null) {
			return;
		}
		
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		
		while (true) {
			line = br.readLine();
			
			if (line == null) {
				break;
			}
			
			line = line.trim();
			
			if (line.startsWith(stateStart)) {
				final State state = StateFactory.createState();
				readState(br, state);
				states.add(state);
			}
		}
	}
	
	private static void printIndent(final PrintWriter pw, final int indent) {
		for (int i = 0; i < indent; i++) {
			pw.print("  ");
		}
	}
	
	private void print(final PrintWriter pw, final State s, final int indent) {
		final Iterator<String> iter = s.keySet().iterator();
		printIndent(pw, indent);
		pw.println(stateStart);
		
		while (iter.hasNext()) {
			final String key = iter.next();
			final Object value = s.getObject(key);
			
			if (value instanceof State) {
				printIndent(pw, indent + 1);
				pw.println(childDelimiter + key);
				print(pw, (State)value, indent + 1);
				
				continue;
			}
			
			printIndent(pw, indent);
			pw.println(key + "=" + value.toString());
		}
		
		printIndent(pw, indent);
		pw.println(stateEnd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#store(java.io.OutputStream)
	 */
	@Override
	public void store(final String filePath, final String applicationName) throws IOException {
		final OutputStream os = getOutputStream(filePath, applicationName + ".txt");
		store(os);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateStorage#store(java.io.OutputStream, java.lang.String)
	 */
	@Override
	public void store(final OutputStream os) throws IOException {
		if (os == null) {
			return;
		}
		
		final PrintWriter pw = new PrintWriter(os);
		
		for (int i = 0; i < states.size(); i++) {
			final State s = states.get(i);
			print(pw, s, 0);
		}
		
		pw.flush();
		pw.close();
	}
}

/* __oOo__ */
