package com.sinergise.geopedia.expr.dom;

public class UnaryOp extends Expression
{
	public int type;
	public Expression base;
	
	public UnaryOp(int type, Expression base)
	{
		super(base.pos);
		
		this.base = base;
		this.type = type;
	}
}
