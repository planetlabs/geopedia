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

package com.sinergise.java.util.state.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import com.sinergise.java.util.state.State;
import com.sinergise.java.util.string.StringUtilJava;

/**
 * This is the default implementation of the <code>State</code> interface. It is basically extended properties. It uses the
 * <code>LinkedHashMap</code> for the intermidiate storage. This implementation should be sufficient for the majority application and it is
 * not likely that a component need to implement its own state object to exchange with the <code>StateKeeper</code>
 * 
 * @author dvitas
 */
public class DefaultState implements State, Serializable {
	
	/**
	 * 
	 */
	private static final long               serialVersionUID = 1L;
	private static final String             keyID            = "ID";
	private static boolean                  typed            = false;
	private static final String             typeMark         = ".type";
	protected LinkedHashMap<String, Object> map              = new LinkedHashMap<String, Object>(0, 0.8f);
	
	/**
	 * Turn on/off storing type information about stored objects. Default is off - objects are stored without information about its type. If
	 * it is turned on, all objects are written with its type information so the function <code>getType()</code> can be used.
	 * 
	 * @param typed on/off
	 */
	public static void setTyped(final boolean typed) {
		DefaultState.typed = typed;
	}
	
	/**
					 *
					 */
	public DefaultState() {
		super();
	}
	
	private static void checkKeyName(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("Key name can not be 'null'.");
		}
		int i = name.indexOf(' ');
		if (i >= 0) {
			throw new IllegalArgumentException("Key name '" + name + "' can not contain whitespace character(s).");
		}
		i = name.indexOf('*');
		if (i >= 0) {
			throw new IllegalArgumentException("Key name '" + name + "' can not contain '*' character(s).");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putState(java.lang.String, com.cosylab.application.state.State)
	 */
	@Override
	public void putState(final String key, final State state) {
		if (state != null) {
			checkKeyName(key);
			map.put(key, state);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#createChild(java.lang.String)
	 */
	@Override
	public State createState(final String key) {
		checkKeyName(key);
		final State s = new DefaultState();
		map.put(key, s);
		
		return s;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(final String key, final boolean defaultValue) {
		final String val = getString(key, null);
		
		if (val == null) {
			return defaultValue;
		}
		
		return Boolean.valueOf(val).booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getChild(java.lang.String)
	 */
	@Override
	public State getState(final String key) {
		final Object obj = map.get(key);
		
		if (obj == null) {
			return null;
		}
		
		if (!(obj instanceof State)) {
			return null;
		}
		
		return (State)obj;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getClas(java.lang.String)
	 */
	@Override
	public Class<?> getClass(final String key) {
		final String value = getString(key, null);
		
		if (value == null) {
			return null;
		}
		
		try {
			return Class.forName(value);
		} catch(final ClassNotFoundException e) {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getColor(java.lang.String)
	 */
	@Override
	public Color getColor(final String key) {
		String value = getString(key, null);
		
		if (value == null) {
			return null;
		}
		if (value.charAt(0) == '#') {
			value = value.substring(1);
		}
		
		try {
			final int rgb = (int)Long.parseLong(value, 16);
			return new Color(rgb, true);
		} catch(final NumberFormatException e) {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getDimension(java.lang.String)
	 */
	@Override
	public Dimension getDimension(final String key) {
		// test if exists
		final Object obj = map.get(key + ".width");
		
		if (obj == null) {
			return null;
		}
		
		final Dimension dim = new Dimension();
		dim.width = getInt(key + ".width", 0);
		dim.height = getInt(key + ".height", 0);
		
		return dim;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(final String key, final double defaultValue) {
		final Object obj = map.get(key);
		
		if (obj == null) {
			return defaultValue;
		}
		
		if (obj instanceof Number) {
			return ((Number)obj).doubleValue();
		}
		
		return Double.parseDouble(obj.toString());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getDoubleSeq(java.lang.String)
	 */
	@Override
	public double[] getDoubleSeq(final String key) {
		final int length = getInt(key + ".length", -1);
		
		if (length == -1) {
			return null;
		}
		
		final double[] retVal = new double[length];
		
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getDouble(key + '_' + i + '_', Double.NaN);
		}
		
		return retVal;
	}
	
	@Override
	public Color[] getColorSeq(final String key) {
		final int length = getInt(key + ".length", -1);
		
		if (length < 0) {
			return null;
		}
		
		final Color[] retVal = new Color[length];
		
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getColor(key + '_' + i + '_');
		}
		
		return retVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getFont(java.lang.String)
	 */
	@Override
	public Font getFont(final String key) {
		final String value = getString(key, null);
		
		if (value == null) {
			return null;
		}
		
		Font f;
		
		try {
			f = Font.decode(value);
		} catch(final NumberFormatException e) {
			return null;
		}
		
		return f;
	}
	
	@Override
	public Font[] getFontSeq(final String key) {
		final int length = getInt(key + ".length", -1);
		
		if (length < 0) {
			return null;
		}
		
		final Font[] retVal = new Font[length];
		
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getFont(key + '_' + i + '_');
		}
		
		return retVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getID()
	 */
	@Override
	public String getID() {
		return getString(keyID, null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getInt(java.lang.String)
	 */
	@Override
	public int getInt(final String key, final int defaultValue) {
		final Object obj = map.get(key);
		
		if (obj == null) {
			return defaultValue;
		}
		
		if (obj instanceof Number) {
			return ((Number)obj).intValue();
		}
		
		return Integer.parseInt(obj.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getLong(java.lang.String)
	 */
	@Override
	public long getLong(final String key, final long defaultValue) {
		final Object obj = map.get(key);
		
		if (obj == null) {
			return defaultValue;
		}
		
		if (obj instanceof Number) {
			return ((Number)obj).longValue();
		}
		
		return Long.parseLong(obj.toString());
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getIntSeq(java.lang.String)
	 */
	@Override
	public int[] getIntSeq(final String key) {
		final int length = getInt(key + ".length", -1);
		
		if (length == -1) {
			return null;
		}
		
		final int[] retVal = new int[length];
		
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getInt(key + '_' + i + '_', Integer.MAX_VALUE);
		}
		
		return retVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getObject()
	 */
	@Override
	public Object getObject(final String key) {
		return map.get(key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getType(java.lang.String)
	 */
	@Override
	public Class<?> getType(final String key) {
		final String className = getString(key + typeMark, null);
		
		if (className == null) {
			return null;
		}
		
		try {
			return StringUtilJava.classFromString(className);
		} catch(final Exception e) {
			// no op
		}
		
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getRectangle(java.lang.String)
	 */
	@Override
	public Rectangle getRectangle(final String key) {
		// test if exists
		final Object obj = map.get(key + ".x");
		
		if (obj == null) {
			return null;
		}
		
		final Rectangle r = new Rectangle();
		r.x = getInt(key + ".x", 0);
		r.y = getInt(key + ".y", 0);
		r.width = getInt(key + ".width", 0);
		r.height = getInt(key + ".height", 0);
		
		return r;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getString(java.lang.String)
	 */
	@Override
	public String getString(final String key, final String defaultValue) {
		final Object obj = map.get(key);
		
		if (obj == null) {
			return defaultValue;
		}
		
		return obj.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#getStringSeq(java.lang.String)
	 */
	@Override
	public String[] getStringSeq(final String key) {
		final int length = getInt(key + ".length", -1);
		
		if (length == -1) {
			return null;
		}
		
		final String[] retVal = new String[length];
		
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = getString(key + '_' + i + '_', null);
		}
		
		return retVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#keySet()
	 */
	@Override
	public Set<String> keySet() {
		return map.keySet();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putBoolean(java.lang.String, boolean)
	 */
	@Override
	public void putBoolean(final String key, final boolean value) {
		putString(key, Boolean.toString(value));
		
		if (DefaultState.typed) {
			map.put(key + typeMark, Boolean.TYPE);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putClas(java.lang.String, java.lang.Class)
	 */
	@Override
	public void putClass(final String key, final Class<?> value) {
		if (value == null) {
			return;
		}
		
		putString(key, value.getName());
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putColor(java.lang.String, java.awt.Color)
	 */
	@Override
	public void putColor(final String key, final Color value) {
		if (value == null) {
			return;
		}
		
		putString(key, Integer.toHexString(value.getRGB()));
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putDimension(java.lang.String, java.awt.Dimension)
	 */
	@Override
	public void putDimension(final String key, final Dimension value) {
		if (value == null) {
			return;
		}
		
		putInt(key + ".width", value.width);
		putInt(key + ".height", value.height);
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putDouble(java.lang.String, double)
	 */
	@Override
	public void putDouble(final String key, final double value) {
		checkKeyName(key);
		map.put(key, new Double(value));
		
		if (DefaultState.typed) {
			map.put(key + typeMark, Double.TYPE.getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putDoubleSeq(java.lang.String, double[])
	 */
	@Override
	public void putDoubleSeq(final String key, final double[] value) {
		if (value == null) {
			return;
		}
		
		putInt(key + ".length", value.length);
		
		for (int i = 0; i < value.length; i++) {
			putDouble(key + '_' + i + '_', value[i]);
		}
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putDoubleSeq(java.lang.String, double[])
	 */
	@Override
	public void putColorSeq(final String key, final Color[] value) {
		if (value == null) {
			return;
		}
		
		putInt(key + ".length", value.length);
		
		for (int i = 0; i < value.length; i++) {
			putColor(key + '_' + i + '_', value[i]);
		}
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putFont(java.lang.String, java.awt.Font)
	 */
	@Override
	public void putFont(final String key, final Font value) {
		if (value == null) {
			return;
		}
		
		String strStyle;
		
		if (value.isBold()) {
			strStyle = value.isItalic() ? "bolditalic" : "bold";
		} else {
			strStyle = value.isItalic() ? "italic" : "plain";
		}
		
		putString(key, value.getName() + "-" + strStyle + "-" + value.getSize());
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	@Override
	public void putFontSeq(final String key, final Font[] value) {
		if (value == null) {
			return;
		}
		
		putInt(key + ".length", value.length);
		
		for (int i = 0; i < value.length; i++) {
			putFont(key + '_' + i + '_', value[i]);
		}
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putInt(java.lang.String, int)
	 */
	@Override
	public void putInt(final String key, final int value) {
		checkKeyName(key);
		map.put(key, new Integer(value));
		
		if (DefaultState.typed) {
			map.put(key + typeMark, Integer.TYPE.getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putLong(java.lang.String, long)
	 */
	@Override
	public void putLong(final String key, final long value) {
		checkKeyName(key);
		map.put(key, new Long(value));
		
		if (DefaultState.typed) {
			map.put(key + typeMark, Long.TYPE.getName());
		}
	}
	
		
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putIntSeq(java.lang.String, int[])
	 */
	@Override
	public void putIntSeq(final String key, final int[] value) {
		if (value == null) {
			return;
		}
		
		putInt(key + ".length", value.length);
		
		for (int i = 0; i < value.length; i++) {
			putInt(key + '_' + i + '_', value[i]);
		}
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putRectangle(java.lang.String, java.awt.Rectangle)
	 */
	@Override
	public void putRectangle(final String key, final Rectangle value) {
		if (value == null) {
			return;
		}
		
		putInt(key + ".x", value.x);
		putInt(key + ".y", value.y);
		putInt(key + ".width", value.width);
		putInt(key + ".height", value.height);
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putString(java.lang.String, java.lang.String)
	 */
	@Override
	public void putString(final String key, final String value) {
		if (value == null) {
			return;
		}
		
		checkKeyName(key);
		map.put(key, value);
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#putStringSeq(java.lang.String, java.lang.String[])
	 */
	@Override
	public void putStringSeq(final String key, final String[] value) {
		if (value == null) {
			return;
		}
		
		putInt(key + ".length", value.length);
		
		for (int i = 0; i < value.length; i++) {
			putString(key + '_' + i + '_', value[i]);
		}
		
		if (DefaultState.typed) {
			map.put(key + typeMark, value.getClass().getName());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#setID()
	 */
	@Override
	public void setID(final String ID) {
		putString(keyID, ID);
	}
	
	private static String processValueString(final String inString) {
		if (inString == null) {
			return "null";
		}
		final int len = inString.length();
		final StringBuffer retBuf = new StringBuffer();
		for (int i = 0; i < len; i++) {
			final char c = inString.charAt(i);
			if (c > 127 || c == '&' || c == '"' || c == '<' || c == '>') {
				retBuf.append("&#" + ((int)c) + ";");
			} else {
				retBuf.append(c);
			}
		}
		return retBuf.toString();
	}
	
	private void writeState(final Writer writer, final String name, final State state, final String linePrefix) throws IOException {
		boolean hasChilds = false;
		writer.write(linePrefix + "<" + name);
		
		Iterator<String> iter = state.keySet().iterator();
		
		while (iter.hasNext()) {
			final String key = iter.next();
			final Object value = state.getObject(key);
			
			if (value instanceof State) {
				hasChilds = true;
				
				continue;
			}
			
			final String aa = processValueString(value.toString());
			writer.write(" " + key + "=\"" + aa + "\" ");
		}
		
		if (hasChilds) {
			writer.write(">\n");
		} else {
			writer.write("/>\n");
		}
		
		iter = state.keySet().iterator();
		
		while (iter.hasNext()) {
			final String key = iter.next();
			final Object value = state.getObject(key);
			
			if (value instanceof State) {
				writeState(writer, key, (State)value, linePrefix + "  ");
				
				continue;
			}
		}
		
		if (hasChilds) {
			writer.write(linePrefix + "</" + name + ">\n");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.State#write(java.io.Writer)
	 */
	@Override
	public void writeXML(final Writer writer) throws IOException {
		writer.write("<?xml version=\"1.0\" standalone=\"yes\"?>\n<states>\n");
		writeState(writer, "state", this, "");
		writer.write("</states>\n");
		writer.close();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		final Iterator<String> iter = map.keySet().iterator();
		sb.append("ID=" + getID());
		sb.append('\n');
		
		while (iter.hasNext()) {
			final Object key = iter.next();
			final Object value = map.get(key);
			sb.append(key.toString());
			sb.append('=');
			sb.append(value.toString());
			sb.append('\n');
		}
		
		sb.append('\n');
		
		return new String(sb);
	}
}

/* __oOo__ */
