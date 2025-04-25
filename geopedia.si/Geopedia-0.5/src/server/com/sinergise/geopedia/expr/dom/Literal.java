package com.sinergise.geopedia.expr.dom;

import com.sinergise.geopedia.expr.lexer.Token;

public class Literal extends Expression
{
	public Object value;
	
	public Literal(Object value, Token pos)
	{
		super(pos);
		
		this.value = value;
	}
}
