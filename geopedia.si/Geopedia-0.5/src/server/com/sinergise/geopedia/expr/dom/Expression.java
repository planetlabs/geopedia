package com.sinergise.geopedia.expr.dom;

import com.sinergise.geopedia.expr.lexer.Token;

public abstract class Expression
{
	public final Token pos;

	public Expression(Token pos)
	{
		this.pos = pos;
	}
}
