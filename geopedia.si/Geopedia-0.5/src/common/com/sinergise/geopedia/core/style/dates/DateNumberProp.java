package com.sinergise.geopedia.core.style.dates;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.DateSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class DateNumberProp extends NumberSpec
{
	private static final int F_MIN_FIELD = 1;
	public static final int F_YEAR = 1;
	public static final int F_MONTH = 2;
	public static final int F_DAY_OF_MONTH = 3;
	public static final int F_HOUR_OF_DAY = 4;
	public static final int F_MINUTES = 5;
	public static final int F_SECONDS = 6;
	public static final int F_WEEKDAY = 7;
	private static final int F_MAX_FIELD = 7;
	
	public int field;
	public DateSpec date;

	public DateNumberProp()
	{
		// ...
	}
	
	public DateNumberProp(int field, DateSpec date)
	{
		if (field < F_MIN_FIELD || field > F_MAX_FIELD)
			throw new IllegalArgumentException();
		
		this.field = field;
		this.date = date;
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(DateNumberProp.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
	    sb.append('(');
	    date.toString(sb);
	    
	    switch(field) {
	    case F_YEAR: sb.append(").year"); break;
	    case F_MONTH: sb.append(").month"); break;
	    case F_DAY_OF_MONTH: sb.append(").day"); break;
	    case F_HOUR_OF_DAY: sb.append(").hour"); break;
	    case F_MINUTES: sb.append(").minute"); break;
	    case F_SECONDS: sb.append(").second"); break;
	    case F_WEEKDAY: sb.append(").weekday"); break;
	    default: throw new IllegalStateException();
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
		return new DateNumberProp(field, (DateSpec) date.clone());
	}
}
