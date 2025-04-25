package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StringSpec;

public class LayersTextRep extends StringSpec
{
	public TablePath tablePath;
	
	public LayersTextRep()
	{
		//
	}
	
	public LayersTextRep(TablePath tablePath)
	{
		this.tablePath = tablePath;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(LayersTextRep.class+" not supported");	
	}

	public void accept(StyleVisitor v)
    {
		if (v.visit(this, true))
			v.visit(this, false);
    }

	public boolean isConst()
    {
	    return false;
    }

	public void toString(StringBuffer sb)
    {
		tablePath.toString(sb);
		sb.append(".textRep");
    }
	
	public Object clone()
	{
	    return new LayersTextRep(tablePath);
	}
}
