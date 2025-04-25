package com.sinergise.geopedia.core.style;

import com.sinergise.geopedia.core.style.model.StyleSpecPart;

public interface StyleVisitor
{
	/**
	 * if entering, return true to continue visitation, false to stop; when
	 * leaving, the value is ignored
	 */

	public boolean visit(StyleSpecPart ssp, boolean entering);
}
