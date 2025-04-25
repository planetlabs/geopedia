/*
 * (c) Infoterra Limited 2004.  The contents of this file are under the
 * copyright of Infoterra.  All rights reserved.  This file must not be
 * copied, reproduced or distributed in wholly or in part or used for
 * purposes other than for that for which it has been supplied without
 * the prior written permission of Infoterra.
 */

package com.sinergise.java.util;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Extension of <code>java.util.Properties</code> which facilitates retrieving typed property values.
 * 
 * @version $id$
 */
public class ExtendedProperties extends Properties {
	/**
	 * 
	 */
	private static final long	serialVersionUID		= 1L;
	/**
	 * The delimiter that is used to separate each part in the property name that makes the hierarchy. For example the
	 * property name 'windowX' under the property 'Panel1' becomes 'Panel1.windowX' and the dot between those names is
	 * the ExtendedProperties.DELIMITER
	 */
	public static final String	DELIMITER				= ".";
	private final Set<String>	usedKeys				= new HashSet<String>();
	private static final String	SYSTEM_OVERRIDE_PREFIX	= "SGProperties";

	/**
	 * Creates a new ExtendedProperties object.
	 */
	public ExtendedProperties() {
		super();
	}

	/**
	 * Creates a new ExtendedProperties object.
	 * 
	 * @param defaults Default values which are read when this object doesn't hold a specific key.
	 */
	public ExtendedProperties(final Properties defaults) {
		super(defaults);
	}

	/**
	 * Convenience method that tries to return <code>Class</code> instace specified by fully qualified string.
	 * 
	 * @param key Name of key associated with value.
	 * @return Instance of <code>Class</code> or null if not found.
	 * @see #getProperty(String)
	 */
	public Class<?> getClassProperty(final String key) {
		final String value = getProperty(key);

		if (value == null) { return null; }

		Class<?> c;

		try {
			c = Class.forName(value);
		} catch(final ClassNotFoundException e) {
			return null;
		}

		return c;
	}

	/**
	 * Convenience method that tries to return double value of property.
	 * 
	 * @param key Name of key to query value for
	 * @return double type value of property
	 * @throws NumberFormatException
	 * @see #getProperty(String)
	 */
	public double getDoubleProperty(final String key) throws NumberFormatException {
		final String value = getProperty(key);

		if (value == null) { throw new NumberFormatException("Cannot create double from null."); }

		return Double.parseDouble(value);
	}

	/**
	 * Convenience method that tries to return int value of property.
	 * 
	 * @param key Name of key to query value for
	 * @return int type value of property
	 * @throws NumberFormatException
	 * @see #getProperty(String)
	 */
	public int getIntProperty(final String key) throws NumberFormatException {
		final String value = getProperty(key);

		if (value == null) { throw new NumberFormatException("Cannot create int from null."); }

		return Integer.parseInt(value);
	}

	public int getIntProperty(final String key, final int defaultVal) throws NumberFormatException {
		final String value = getProperty(key);

		if (value == null) { return defaultVal; }

		return Integer.parseInt(value);
	}

	public boolean getBooleanProperty(final String key) {
		final String value = getProperty(key);
		return Boolean.valueOf(value).booleanValue();
	}

	/**
	 * Inserts the content of the given ExtendedProperties to this properties by prefixing each key with given
	 * <code>mementoName</code>.
	 * 
	 * @param mementoName
	 * @param ep
	 */
	public void setProperties(final String mementoName, final ExtendedProperties ep) {
		assert (mementoName != null);
		assert (ep != null);

		for (final Enumeration<Object> e = ep.keys(); e.hasMoreElements();) {
			final String key = (String)e.nextElement();
			final String newKey = mementoName + DELIMITER + key;
			put(newKey, ep.get(key));
		}
	}

	/**
	 * Returns an instance of the ExtendedProperties object which have only properties under the hierarchy given by
	 * <code>mementoName</code> . For example if this object has 'window.x', 'window.y' and 'something else' properties
	 * then invoking getProperties("window") will return ExtendedProperties with anly 'x' and 'y' properties.
	 * 
	 * @param mementoName name to extract from this properties
	 * @return an ExtendedProperties object with properties matching mementoName
	 */
	public ExtendedProperties getProperties(final String mementoName) {
		assert (mementoName != null);

		// calculate susbstring length that we will skip on ech key
		final int stripLen = mementoName.length() + DELIMITER.length();
		final ExtendedProperties ep = new ExtendedProperties();

		for (final Enumeration<Object> e = keys(); e.hasMoreElements();) {
			final String key = (String)e.nextElement();

			if (!key.startsWith(mementoName)) {
				continue;
			}

			ep.put(key.substring(stripLen), get(key));
		}

		return ep;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Properties#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(final String key) {
		usedKeys.add(key);
		final String sysTest = System.getProperty(SYSTEM_OVERRIDE_PREFIX + DELIMITER + key);
		if (sysTest != null && sysTest.length() > 0) { return sysTest; }
		return super.getProperty(key);
	}

	/**
	 * Method used to get information on the usage of a specific property map.
	 * 
	 * @return a set of properties which have already been retrieved from this map
	 */
	public Set<String> getUsedKeySet() {
		return new HashSet<String>(usedKeys);
	}
}

/* __oOo__ */
