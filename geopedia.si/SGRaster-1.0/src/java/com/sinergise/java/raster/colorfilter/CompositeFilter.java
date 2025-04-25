/*
 *
 */
package com.sinergise.java.raster.colorfilter;

import java.util.ArrayList;
import java.util.Collections;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.java.util.state.State;


public class CompositeFilter extends ColorFilter {
	protected static final String	TYPE	= "Composite";

	protected ColorFilter[]			filters;

	public CompositeFilter(final ColorFilter[] filters) {
		this(TYPE, filters);
	}

	protected CompositeFilter(final String type, final ColorFilter[] filters) {
		super(type);
		this.filters = filters;
	}
	
	@Override
	protected void copyInto(ColorFilter copy) {
		super.copyInto(copy);
		((CompositeFilter)copy).filters = ArrayUtil.cloneDeep(filters, new ColorFilter[filters.length]);
	}
	
	@Override
	public boolean filter(final int[] rgba) {
		boolean ret = false;
		for (final ColorFilter filter : filters) {
			if (filter.filter(rgba)) ret = true;
		}
		return ret;
	}

	@Override
	public boolean filterBytes(final byte[] data, final int off, final byte[] outData, final int outOff, final int[] byteOrder) {
		boolean ret = false;
		byte[] indata = data;
		int inOff = off;
		for (final ColorFilter filter : filters) {
			if (filter.filterBytes(indata, inOff, outData, outOff, byteOrder)) {
				indata = outData;
				inOff = outOff;
				ret = true;
			}
		}
		return ret;
	}

	@Override
	public int filterInt(int src) {
		for (final ColorFilter filter : filters) {
			src = filter.filterInt(src);
		}
		return src;
	}

	@Override
	public int getNumComponents(int srcNumComponents) {
		for (final ColorFilter filter : filters) {
			srcNumComponents = filter.getNumComponents(srcNumComponents);
		}
		return srcNumComponents;
	}

	@Override
	public void setInputSampleSize(int inputSampleSize) {
		super.setInputSampleSize(inputSampleSize);
		for (final ColorFilter filter : filters) {
			filter.setInputSampleSize(inputSampleSize);
			inputSampleSize = filter.getNumComponents(inputSampleSize);
		}
	}

	@Override
	public void setState(final State state) {
		final ArrayList<String> names = new ArrayList<String>();
		for (final String key : state.keySet()) {
			if (state.getState(key) != null) names.add(key);
		}

		Collections.sort(names);
		filters = new ColorFilter[names.size()];
		for (int i = 0; i < names.size(); i++) {
			filters[i] = ColorFilter.createFilter(state.getState(names.get(i)));
		}
	}

	@Override
	public void appendIdentifier(final StringBuffer out) {
		out.append(type);
		for (final ColorFilter filter : filters) {
			out.append('|');
			filter.appendIdentifier(out);
		}
	}
}
