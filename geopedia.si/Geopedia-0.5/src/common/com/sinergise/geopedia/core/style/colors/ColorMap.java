package com.sinergise.geopedia.core.style.colors;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class ColorMap extends ColorSpec
{
	public NumberSpec number;
	public ColorSpec[] colors;
	public NumberSpec[] limits;
	
	public ColorMap()
	{
		// ...
	}
	
	public ColorMap(NumberSpec number, ColorSpec[] colors, NumberSpec[] limits)
	{
		if (colors != null && limits != null && colors.length-1 != limits.length)
			throw new IllegalArgumentException();
		
		this.number = number;
		this.colors = colors;
		this.limits = limits;
	}
	
	
    @Override
	public void toStringJS(StringBuffer sb) {
    	sb.append("colorMap(");
    	number.toStringJS(sb);
    	sb.append(", "+colors[colors.length-1].toStringJS());
    	sb.append(", [");
    	boolean first = true;
    	for (int i=0;i<limits.length;i++) {
    		if (!first) sb.append(",");
    		sb.append(limits[i].toStringJS());
    		first=false;
    	}
    	sb.append("], [");
    	first = true;
    	for (int i=0;i<limits.length;i++) {
    		if (!first) sb.append(",");
    		sb.append(colors[i].toStringJS());
    		first=false;
    	}
    	sb.append("])");
        
	}

	public void toString(StringBuffer sb)
	{
		sb.append("colorMap(");
		number.toString(sb);
		for (int a=0; a<colors.length; a++) {
			sb.append(", ");
			colors[a].toString(sb);
			if (a != colors.length - 1) {
				sb.append(", ");
				limits[a].toString(sb);
			}
		}
		sb.append(')');
	}

	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			number.accept(v);
			
			for (int a=0; a<colors.length; a++)
				colors[a].accept(v);
			for (int a=0; a<limits.length; a++)
				limits[a].accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		if (!number.isConst())
			return false;
		
		for (int a=0; a<colors.length; a++)
			if (!colors[a].isConst())
				return false;
		for (int a=0; a<limits.length; a++)
			if (!limits[a].isConst())
				return false;
		
		return true;
	}
	
	public Object clone()
	{
		NumberSpec number = (NumberSpec) this.number.clone();
		ColorSpec[] colors = new ColorSpec[this.colors.length];
		for (int a=0; a<colors.length; a++)
			colors[a] = (ColorSpec) this.colors[a].clone();
		NumberSpec[] limits = new NumberSpec[this.limits.length];
		for (int a=0; a<limits.length; a++)
			limits[a] = (NumberSpec) this.limits[a].clone();
		
		return new ColorMap(number, colors, limits);
	}
}
