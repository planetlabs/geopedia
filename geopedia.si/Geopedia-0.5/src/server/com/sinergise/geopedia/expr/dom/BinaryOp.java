package com.sinergise.geopedia.expr.dom;

public class BinaryOp extends Expression
{
	public int type;
	public Expression left;
	public Expression right;
	
	public BinaryOp(Expression left, int type, Expression right)
	{
		super(left.pos);
		
		this.left = left;
		this.right = right;
		this.type = type;
	}
}
