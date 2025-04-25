package com.sinergise.geopedia.core.style.defs;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;
import com.sinergise.geopedia.core.style.model.FillTypeSpec;

public final class FillStyleDef extends FillStyleSpec
{
	public ColorSpec bgColor;
	public ColorSpec fgColor;
	public FillTypeSpec fillType;

	public FillStyleDef()
	{
		// ...
	}
	
	public FillStyleDef(ColorSpec bgColor, ColorSpec fgColor, FillTypeSpec fillType)
	{
		this.bgColor = bgColor;
		this.fgColor = fgColor;
		this.fillType = fillType;
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(FillStyleDef.class+" not supported");	
	}
    
    public void toString(StringBuffer sb)
    {
    	sb.append("fillStyle(");
    	bgColor.toString(sb);
    	sb.append(", ");
    	fgColor.toString(sb);
    	sb.append(", ");
    	fillType.toString(sb);
    	sb.append(")");
    }
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			bgColor.accept(v);
			fgColor.accept(v);
			fillType.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return bgColor.isConst() && fgColor.isConst() && fillType.isConst();
	}
	
	public Object clone()
	{
		return new FillStyleDef((ColorSpec)bgColor.clone(), (ColorSpec)fgColor.clone(), (FillTypeSpec)fillType.clone());
	}
}
