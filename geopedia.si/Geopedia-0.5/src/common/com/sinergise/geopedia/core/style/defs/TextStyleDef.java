package com.sinergise.geopedia.core.style.defs;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.FontIdSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class TextStyleDef extends TextStyleSpec
{
	public ColorSpec color;
	public NumberSpec height;
	public FontIdSpec fontId;
	public BooleanSpec bold;
	public BooleanSpec italic;
	
	public TextStyleDef()
	{
		// ...
	}
	
	public TextStyleDef(ColorSpec color, NumberSpec height, FontIdSpec fontId, BooleanSpec bold,
                    BooleanSpec italic)
    {
	    this.color = color;
	    this.height = height;
	    this.fontId = fontId;
	    this.bold = bold;
	    this.italic = italic;
    }

	public ColorSpec getColorSpec()
    {
    	return color;
    }
	
	public NumberSpec getHeightSpec()
    {
    	return height;
    }
	
	public FontIdSpec getFontIdSpec()
    {
    	return fontId;
    }
	
	public BooleanSpec getBoldSpec()
    {
    	return bold;
    }
	
	public BooleanSpec getItalicSpec()
    {
    	return italic;
    }
	
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(TextStyleDef.class+" not supported");	
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append("textStyle(");
		color.toString(sb);
		sb.append(", ");
		height.toString(sb);
		sb.append(", ");
		fontId.toString(sb);
		sb.append(", ");
		bold.toString(sb);
		sb.append(", ");
		italic.toString(sb);
		sb.append(')');
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			bold.accept(v);
			color.accept(v);
			fontId.accept(v);
			height.accept(v);
			italic.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return bold.isConst() && color.isConst() && fontId.isConst() && height.isConst() && italic.isConst();
	}
	
	public Object clone()
	{
		return new TextStyleDef((ColorSpec)color.clone(), (NumberSpec)height.clone(), (FontIdSpec)fontId.clone(), (BooleanSpec)bold.clone(), (BooleanSpec)italic.clone());
	}
}
