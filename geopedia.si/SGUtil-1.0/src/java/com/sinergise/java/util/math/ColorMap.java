package com.sinergise.java.util.math;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.lang.SGCloneable;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.math.interpolation.Interpolation;
import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.StateOriginator;
import com.sinergise.java.util.state.impl.DefaultState;

public class ColorMap implements StateOriginator, Cloneable {
	public static class ColorMapElement implements StateOriginator, Comparable<ColorMapElement>, SGCloneable {
		Color  color;
		double value;
		
		public ColorMapElement() {
			super();
		}
		
		public ColorMapElement(final double value, final Color color) {
			this.value = value;
			this.color = color;
		}
		
		public ColorMapElement(final double value, final int colorInt) {
			this.value = value;
			this.color = new Color(colorInt, true);
		}
		
		@Override
		public ColorMapElement clone() {
			try {
				return (ColorMapElement)super.clone();
			} catch(CloneNotSupportedException e) {
				throw new Error(e);
			}
		}
		
		@Override
		public int compareTo(final ColorMapElement cme) {
			return MathUtil.fastCompare(this.value, cme.value);
		}
		
		@Override
		public State getState() {
			final State ret = new DefaultState();
			ret.putDouble("value", value);
			ret.putColor("color", color);
			return ret;
		}
		
		@Override
		public void setState(final State state) {
			value = state.getDouble("value", Double.MIN_VALUE);
			color = state.getColor("color");
		}
	}
	
	ColorMapElement[]               colors;
	private boolean                 smooth = true;
	
	transient double[][]            colValsRGBA;
	transient double[]              xVals;
	transient ThreadLocal<double[]> tmp    = new ThreadLocal<double[]>() {
		                                       @Override
		                                       protected double[] initialValue() {
			                                       return new double[4];
		                                       }
	                                       };
	
	
	public void appendIdentifier(final StringBuffer out) {
		if (smooth) {
			out.append('S');
		}
		if (colors == null) {
			return;
		}
		for (int i = 0; i < colors.length; i++) {
			if (i > 0) {
				out.append(',');
			}
			out.append(String.valueOf(colors[i].value));
			out.append('>');
			out.append(Integer.toHexString(colors[i].color.getRGB()));
		}
	}
	
	@Override
	public ColorMap clone() {
		try {
			ColorMap ret = (ColorMap)super.clone();
			ret.setColors(ArrayUtil.cloneDeep(colors, new ColorMapElement[colors.length]));
			return ret;
		} catch(CloneNotSupportedException e) {
			throw new Error(e);
		}
	}
	
	public Color color(final double val) {
		return new Color(colorARGB(val), hasAlpha());
	}
	
	public void color(final double val, final int[] outRGBA) {
		final double[] ret = smooth ? Interpolation.linear(val, xVals, colValsRGBA, tmp.get()) : Interpolation.nearestNeighbour(val, xVals, colValsRGBA);
		for (int i = 0; i < outRGBA.length; i++) {
			outRGBA[i] = (int)ret[i];
		}
	}
	
	public int colorARGB(final double val) {
		final double[] ret = smooth ? Interpolation.linear(val, xVals, colValsRGBA, tmp.get()) : Interpolation.nearestNeighbour(val, xVals, colValsRGBA);
		return ((int)ret[3]) << 24 | ((int)ret[0]) << 16 | ((int)ret[1]) << 8 | (int)ret[2];
	}
	
	@Override
	public State getState() {
		final State st = new DefaultState();
		st.putBoolean("smooth", smooth);
		for (int i = 0; i < colors.length; i++) {
			st.putState("color" + i, colors[i].getState());
		}
		return st;
	}
	
	public boolean hasAlpha() {
		for (final ColorMapElement color : colors) {
			if (color.color.getAlpha() < 255) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSmooth() {
		return smooth;
	}
	
	public void setColors(final ColorMapElement[] array) {
		Arrays.sort(array);
		colors = array;
		xVals = new double[colors.length];
		colValsRGBA = new double[colors.length][4];
		for (int i = 0; i < colors.length; i++) {
			xVals[i] = colors[i].value;
			colValsRGBA[i][0] = colors[i].color.getRed();
			colValsRGBA[i][1] = colors[i].color.getGreen();
			colValsRGBA[i][2] = colors[i].color.getBlue();
			colValsRGBA[i][3] = colors[i].color.getAlpha();
		}
	}

	public void setSmooth(boolean smooth) {
		this.smooth = smooth;
	}

	@Override
	public void setState(final State state) {
		smooth = state.getBoolean("smooth", true);
		final ArrayList<ColorMapElement> lst = new ArrayList<ColorMapElement>();
		for (final String string : state.keySet()) {
			final Object st = state.getObject(string);
			if (st instanceof State) {
				final ColorMapElement cme = new ColorMapElement();
				cme.setState((State)st);
				lst.add(cme);
			}
		}
		setColors(lst.toArray(new ColorMapElement[lst.size()]));
	}
}
