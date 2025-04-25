package com.sinergise.geopedia.expr.dom;

import com.sinergise.geopedia.expr.lexer.Token;

public class MethodCall extends Expression
{
	public Expression object;
	public Expressions params;
	public String methodName;
	
	public MethodCall(Token position, String methodName, Expressions params)
	{
		super(position);
		
		object = null;
		this.params = params;
		this.methodName = methodName;
	}
	
	public MethodCall(Expression object, String methodName, Expressions params)
	{
		super(object.pos);
		
		this.object = object;
		this.params = params;
		this.methodName = methodName;
	}
}
