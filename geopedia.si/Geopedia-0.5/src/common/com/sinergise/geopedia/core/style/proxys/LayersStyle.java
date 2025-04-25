package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StyleSpec;

public final class LayersStyle extends StyleSpec
{
	public TablePath tablePath;
	
	public LayersStyle()
	{
		// ...
	}
	
	public LayersStyle(TablePath tablePath)
    {
		this.tablePath = tablePath;
    }
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(LayersStyle.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		tablePath.toString(sb);
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return false;
	}
	
	public Object clone()
	{
		return new LayersStyle(tablePath);
	}
}
