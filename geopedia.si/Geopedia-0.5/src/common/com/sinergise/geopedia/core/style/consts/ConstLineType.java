package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.LineType;
import com.sinergise.geopedia.core.style.model.LineTypeSpec;

public class ConstLineType extends LineTypeSpec
{
	public int lineTypeId;
	
	public ConstLineType()
	{
		
	}
	
	public ConstLineType(int lineTypeId)
	{
		this.lineTypeId = lineTypeId;
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
		sb.append("'"+LineType.names[lineTypeId]+"'");
		
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append("lineType(");
		sb.append(lineTypeId);
		sb.append(')');
	}
	
	public Object clone()
	{
		return new ConstLineType(lineTypeId);
	}
}
