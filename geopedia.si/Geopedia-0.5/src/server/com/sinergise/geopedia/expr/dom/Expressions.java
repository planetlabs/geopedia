package com.sinergise.geopedia.expr.dom;

import java.util.ArrayList;
import java.util.Iterator;

public class Expressions
{
	ArrayList<Expression> exprs = new ArrayList<Expression>();
	
	public Expressions(Expression first)
	{
		exprs.add(first);
	}
	
	public void add(Expression e)
	{
		exprs.add(e);
	}

	public int size()
    {
		return exprs.size();
    }

	public Iterator<Expression> iterator()
    {
		return exprs.iterator();
    }

	public Expression get(int a)
    {
		return exprs.get(a);
    }
}
