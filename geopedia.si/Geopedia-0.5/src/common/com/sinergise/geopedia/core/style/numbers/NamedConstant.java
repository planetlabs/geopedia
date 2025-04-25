package com.sinergise.geopedia.core.style.numbers;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class NamedConstant extends NumberSpec
{
	public static final String     N_PI = "pi";
	public static final double     D_PI = 3.14159265358979323846264338327950288419716939937510582;
//	public static final BigDecimal B_PI = new BigDecimal("3.14159265358979323846264338327950288419716939937510582");
	
	String nameUsed;
	
	public NamedConstant()
	{
		//
	}
	
	public NamedConstant(String nameUsed)
	{
		this.nameUsed = nameUsed;
	}
	
	public static NamedConstant get(String name)
	{
		if (name.equalsIgnoreCase(N_PI))
			return new NamedConstant(N_PI);
		
		return null;
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append(nameUsed);
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NamedConstant.class+" not supported");
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return true;
	}
	
	public Object clone()
	{
		return new NamedConstant(nameUsed);
	}
}
