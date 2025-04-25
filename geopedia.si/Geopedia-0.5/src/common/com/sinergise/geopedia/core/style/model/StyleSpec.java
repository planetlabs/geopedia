package com.sinergise.geopedia.core.style.model;

public abstract class StyleSpec extends CompositeSpec
{
	private static final long serialVersionUID = 1L;

	public final int getType()
	{
		return T_STYLE;
	}
}
