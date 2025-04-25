package com.sinergise.geopedia.expr;

import com.sinergise.geopedia.expr.lexer.Token;

public interface ErrorReporter
{
	void error(String msg, Token token);
	
	public boolean hadErrors();
}
