package com.sinergise.geopedia.core.style.dates;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.DateSpec;

public final class DateBoolProp extends BooleanSpec
{
	private static final int F_MIN_FIELD = 1;
	public static final int F_ISWEEKDAY = 1;
	public static final int F_ISWEEKEND = 2;
	private static final int F_MAX_FIELD = 2;
	
	public int field;
	public DateSpec date;

	public DateBoolProp()
	{
		// ...
	}
	
	public DateBoolProp(int field, DateSpec date)
	{
		if (field < F_MIN_FIELD || field > F_MAX_FIELD)
			throw new IllegalArgumentException();
		
		this.field = field;
		this.date = date;
	}

	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(DateBoolProp.class+" not supported");	
	}
	public void toString(StringBuffer sb)
	{
		sb.append("(");
		date.toString(sb);
		
		switch(field) {
		case F_ISWEEKDAY: sb.append(".isWeekday)"); break;
		case F_ISWEEKEND: sb.append(".isWeekend)"); break;
		default:
			throw new IllegalStateException();
		}
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			date.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return date.isConst();
	}
	
	public Object clone()
	{
		return new DateBoolProp(field, (DateSpec) date.clone());
	}
}
