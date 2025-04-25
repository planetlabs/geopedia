package com.sinergise.geopedia.core.style.defs;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.LineTypeSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class LineStyleDef extends LineStyleSpec
{
	public ColorSpec color;
	public NumberSpec lineWidth;
	public LineTypeSpec lineType;
	
	public LineStyleDef()
	{
		// ...
	}
	
	public LineStyleDef(ColorSpec color, NumberSpec lineWidth, LineTypeSpec lineType)
	{
		this.color = color;
		this.lineWidth = lineWidth;
		this.lineType = lineType;
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(LineStyleDef.class+" not supported");	
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append("lineStyle(");
		color.toString(sb);
		sb.append(", ");
		lineWidth.toString(sb);
		sb.append(", ");
		lineType.toString(sb);
		sb.append(')');
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			color.accept(v);
			lineType.accept(v);
			lineWidth.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return color.isConst() && lineType.isConst() && lineWidth.isConst();
	}
	
	public Object clone()
	{
		return new LineStyleDef((ColorSpec)color.clone(), (NumberSpec)lineWidth.clone(), (LineTypeSpec)lineType.clone());
	}
}
