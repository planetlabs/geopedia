package com.sinergise.geopedia.core.style.colors;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class ColorComponent extends NumberSpec
{
	public static final int BLUE = 0;
	public static final int GREEN = 1;
	public static final int RED = 2;
	public static final int ALPHA = 3;

	public ColorSpec color;
	public int component;

	public ColorComponent()
	{
		// ...
	}

	public ColorComponent(int component, ColorSpec color)
	{
		if (component < 0 || component > 3)
			throw new IllegalArgumentException();

		this.color = color;
		this.component = component;
	}

	public void toString(StringBuffer sb)
	{
		sb.append("(");
		color.toString(sb);

		switch (component) {
		case BLUE:
			sb.append(").blue");
			break;
		case RED:
			sb.append(").red");
			break;
		case GREEN:
			sb.append(").green");
			break;
		case ALPHA:
			sb.append(").alpha");
			break;
		default:
			throw new IllegalStateException();
		}
	}
	
    @Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(ColorComponent.class+" not supported");	
	}


	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			color.accept(v);
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return color.isConst();
	}
	
	public Object clone()
	{
		return new ColorComponent(component, (ColorSpec) color.clone());
	}
}
