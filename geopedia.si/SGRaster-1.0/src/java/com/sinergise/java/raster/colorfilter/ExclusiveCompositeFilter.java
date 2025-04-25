/*
 *
 */
package com.sinergise.java.raster.colorfilter;

/**
 * Loops through several filters, until one of them returns true, meaning that it performed an operation on the pixel
 * 
 * @author Miha
 */
public class ExclusiveCompositeFilter extends CompositeFilter {
	protected static final String	TYPE_EXCLUSIVE	= "Exclusive";

	public ExclusiveCompositeFilter(final ColorFilter[] filters) {
		this(TYPE_EXCLUSIVE, filters);
	}

	protected ExclusiveCompositeFilter(final String type, final ColorFilter[] filters) {
		super(type, filters);
	}

	@Override
	public boolean filter(final int[] rgba) {
		for (final ColorFilter filter : filters) {
			if (filter.filter(rgba)) {
				return true;
			}
		}
		return false;
	}
}
