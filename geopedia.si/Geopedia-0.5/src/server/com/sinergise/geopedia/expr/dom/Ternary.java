package com.sinergise.geopedia.expr.dom;

public class Ternary extends Expression
{
	public Expression cond;
	public Expression ifTrue, ifFalse;
	
	public Ternary(Expression cond, Expression ifTrue, Expression ifFalse)
	{
		super(cond.pos);
		
		this.cond = cond;
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
	}
}
