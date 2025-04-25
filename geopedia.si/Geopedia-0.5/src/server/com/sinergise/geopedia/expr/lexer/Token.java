package com.sinergise.geopedia.expr.lexer;

public class Token
{
	public int type;
	public int line, col;
	public Object value;
	public String rawText;
	
	public Token(int type, int line, int col)
	{
		this.type = type;
		this.line = line;
		this.col = col;
	}
	
	public Token(int type, Object value, int line, int col)
	{
		this(type, line, col);
		this.value = value;
	}
}
