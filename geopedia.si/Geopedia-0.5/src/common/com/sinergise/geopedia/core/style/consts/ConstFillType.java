package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FillType;
import com.sinergise.geopedia.core.style.model.FillTypeSpec;

public final class ConstFillType extends FillTypeSpec
{
	public int fillTypeId;
	
	public ConstFillType()
	{
		
	}
	
	public ConstFillType(int fillTypeId)
	{
		this.fillTypeId = fillTypeId;
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true))
			v.visit(this, false);
	}
	
	public boolean isConst()
	{
		return true;
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		sb.append("'"+FillType.names[fillTypeId]+"'");
	}
	public void toString(StringBuffer sb)
	{
		sb.append("fillType(");
		sb.append(fillTypeId);
		sb.append(')');
	}
	
	public Object clone()
	{
		return new ConstFillType(fillTypeId);
	}
}
