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

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;

import com.sinergise.java.util.state.impl.CDBStateStorage;
import com.sinergise.java.util.state.impl.DefaultState;
import com.sinergise.java.util.state.impl.SerialStateStorage;
import com.sinergise.java.util.state.impl.TextStateStorage;
import com.sinergise.java.util.state.impl.XMLStateStorage;

/**
 * This class is used as the factory for the state exchange objects mainly the <code>State</code> implementation and the
 * <code>StateStorage</code> implementation.
 * 
 * @author dvitas
 */
public final class StateFactory {
	/** DOCUMENT ME! */
	public static final String            STORAGE_BIN         = "BIN";
	
	/** DOCUMENT ME! */
	public static final String            STORAGE_TXT         = "TXT";
	
	/** DOCUMENT ME! */
	public static final String            STORAGE_XML         = "XML";
	
	/** DOCUMENT ME! */
	public static final String            STORAGE_CDB         = "CDB";
	
	/** DOCUMENT ME! */
	public static final String            STORAGE_DAT         = "DAT";
	
	/** DOCUMENT ME! */
	public static HashMap<String, String> stateStorages       = null;
	
	/** DOCUMENT ME! */
	public static String                  defaultstateStorage = null;
	
	static {
		// prepare storage
		stateStorages = new HashMap<String, String>(5);
		
		// register our defaults
		registerStateStorage(STORAGE_BIN, SerialStateStorage.class.getName());
		registerStateStorage(STORAGE_TXT, TextStateStorage.class.getName());
		registerStateStorage(STORAGE_XML, XMLStateStorage.class.getName());
		registerStateStorage(STORAGE_CDB, CDBStateStorage.class.getName());
		setDefaultStateStorage(STORAGE_XML);
		
		// maybe something else
		final String userTypeClass = System.getProperty("StateStorage");
		
		if (userTypeClass != null) {
			registerStateStorage(STORAGE_DAT, userTypeClass);
			
			// if so set it as default
			setDefaultStateStorage(STORAGE_DAT);
		}
		
		// default ?
		final String defaultSS = System.getProperty("DefaultStateStorage");
		
		if (defaultSS != null) {
			// if we new it
			if (stateStorages.get(defaultSS) != null) {
				setDefaultStateStorage(defaultSS);
			}
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param type DOCUMENT ME!
	 * @param className DOCUMENT ME!
	 */
	public static void registerStateStorage(final String type, final String className) {
		stateStorages.put(type, className);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param type DOCUMENT ME!
	 */
	public static void setDefaultStateStorage(final String type) {
		defaultstateStorage = type;
	}
	
	protected static boolean typeExists(final File[] files, final String type) {
		if (files == null) {
			return false;
		}
		
		for (final File file : files) {
			if (file.getName().endsWith(type) || file.getName().endsWith(type.toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param filePath DOCUMENT ME!
	 * @param appName DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static StateStorage createStateStorage(final String filePath, final String appName) {
		// first try to gues state storage type from file extension
		try {
			// list all files that starts with app name
			final File file = new File(filePath);
			final File[] files = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					if (name.startsWith(appName)) {
						return true;
					}
					
					return false;
				}
			});
			
			// if there is files and none of them is default then find out which storage to use
			if (!typeExists(files, defaultstateStorage)) {
				final Iterator<String> iter = stateStorages.keySet().iterator();
				
				while (iter.hasNext()) {
					final String type = iter.next();
					
					if (typeExists(files, type)) {
						// try to create it with the delegator
						final StateStorage ss = createStateStorage(type);
						final Class<?>[] paramTypes = {StateStorage.class};
						final Object[] params = {ss};
						
						return (StateStorage)Class.forName(stateStorages.get(defaultstateStorage)).getConstructor(paramTypes).newInstance(params);
					}
				}
			}
		} catch(final Exception e) {
			// no op - will use default
		}
		
		return createStateStorage();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static StateStorage createStateStorage() {
		return createStateStorage(defaultstateStorage);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param type DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public static StateStorage createStateStorage(final String type) {
		final String className = stateStorages.get(type);
		
		if (className == null) {
			return null;
		}
		
		try {
			final Object obj = Class.forName(className).newInstance();
			
			return (StateStorage)obj;
		} catch(final Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	/**
	 * Creates new storage with default ID.
	 * 
	 * @return no-name storage
	 */
	public static State createState() {
		return new DefaultState();
	}
}

/* __oOo__ */
