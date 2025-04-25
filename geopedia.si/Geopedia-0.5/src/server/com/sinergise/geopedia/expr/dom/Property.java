package com.sinergise.geopedia.expr.dom;

import com.sinergise.geopedia.expr.lexer.Token;

public class Property extends Expression
{
	public Expression object;
	public String propName;
	
	public Property(Token pos, String propName)
	{
		super(pos);
		
		this.object = null;
		this.propName = propName;
	}
	
	public Property(Expression object, String propName)
	{
		super(object.pos);
		
		this.object = object;
		this.propName = propName;
	}
}
