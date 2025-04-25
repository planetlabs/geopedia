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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * This interface should be implemented by the object which will be exchanged with a component in the state saving/restoring process. This
 * is basically <code>java.util.Properties</code> with extension for other useful types of objects that are not strings. A
 * <code>State</code> can be used to create and store other <code>State</code> object as well and in such way the hierarchy can be builded
 * easily. For example a component can use this code snippet to produce a <code>State</code> object which has two child object 'Window' and
 * 'Splitter'. This allows the component to easy distinguish between parts of the state later in process of restoring.
 * 
 * <pre>
 * <code>
 *         public State getState() {
 *          State state = StateFactory.createState();
 *          State s = state.createChild("Window");
 *          getWindow().setProperties(s);
 *          s = state.createChild("Splitter");
 *          getSplitter().setProperties(s);
 *         }
 * </code>
 * </pre>
 * 
 * Beside that the enumeration of stored objects is added to allow different <code>StateStorage</code> objects to iterate over.
 * 
 * @author dvitas
 */
public interface State {
	/**
	 * Adds child State with given key.
	 * 
	 * @param key the key under state is stored
	 * @param state the state to be added
	 */
	public void putState(String key, State state);
	
	/**
	 * Creates new child and adds it to contaiment with provided key.
	 * 
	 * @param key the key of new state inside this containment
	 * @return new state contained with provided key
	 */
	public State createState(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param defaultValue DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public boolean getBoolean(String key, boolean defaultValue);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public State getState(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public Class<?> getClass(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public Color getColor(String key);
	
	public Color[] getColorSeq(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public Dimension getDimension(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param defaultValue DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public double getDouble(String key, double defaultValue);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public double[] getDoubleSeq(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public Font getFont(String key);
	
	public Font[] getFontSeq(String key);
	
	/**
	 * Returns ID, which is used to distinguish different states.
	 * 
	 * @return state identification
	 */
	public String getID();
	
	/**
	 * Sets ID to state.
	 * 
	 * @param id the new ID string
	 */
	public void setID(String id);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param defaultValue DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public int getInt(String key, int defaultValue);
	
	
	public long getLong(String key, long defaultValue);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public int[] getIntSeq(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public Object getObject(String key);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public Class<?> getType(String key);
	
	// convinient types
	public Rectangle getRectangle(String key);
	
	// basic types
	public String getString(String key, String defaultValue);
	
	// sequences
	public String[] getStringSeq(String key);
	
	// listing
	public Set<String> keySet();
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putBoolean(String key, boolean value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putClass(String key, Class<?> value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putColor(String key, Color value);
	
	public void putColorSeq(String key, Color[] value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putDimension(String key, Dimension value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putDouble(String key, double value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putDoubleSeq(String key, double[] value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putFont(String key, Font value);
	
	public void putFontSeq(String key, Font[] value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putInt(String key, int value);

	
	public void putLong(String key, long value);

	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putIntSeq(String key, int[] value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putRectangle(String key, Rectangle value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putString(String key, String value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void putStringSeq(String key, String[] value);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param writer DOCUMENT ME!
	 * @throws IOException DOCUMENT ME!
	 */
	public void writeXML(Writer writer) throws IOException;

}

/* __oOo__ */
