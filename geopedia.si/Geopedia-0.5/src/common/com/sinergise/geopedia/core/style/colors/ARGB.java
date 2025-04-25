package com.sinergise.geopedia.core.style.colors;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class ARGB extends ColorSpec
{
	public NumberSpec alpha;
	public NumberSpec red;
	public NumberSpec green;
	public NumberSpec blue;

	public ARGB()
	{
		// ...
	}

	public ARGB(NumberSpec alpha, NumberSpec red, NumberSpec green, NumberSpec blue)
	{
		this.alpha = alpha;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(ARGB.class+" not supported");	
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append("argb(");
		alpha.toString(sb);
		sb.append(", ");
		red.toString(sb);
		sb.append(", ");
		green.toString(sb);
		sb.append(", ");
		blue.toString(sb);
		sb.append(")");
	}

	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			alpha.accept(v);
			red.accept(v);
			green.accept(v);
			blue.accept(v);

			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return alpha.isConst() && red.isConst() && green.isConst() && blue.isConst();
	}
	
	public Object clone()
	{
		return new ARGB((NumberSpec)alpha.clone(), (NumberSpec)red.clone(), (NumberSpec)green.clone(), (NumberSpec)blue.clone());
	}
}
