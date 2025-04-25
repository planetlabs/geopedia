package com.sinergise.geopedia.expr.lexer;

import java.io.IOException;

import com.sinergise.geopedia.core.style.Sym;

public class Tokens
{
	Lexer scanner;

	Token[] buff = new Token[16];

	int first = 0;

	int have = 0;

	public Tokens(Lexer scanner)
	{
		if (scanner == null)
			throw new IllegalArgumentException();

		this.scanner = scanner;
	}

	Token eof = null;

	private Token _get() throws IOException
	{
		if (eof != null)
			return eof;

		Token got = scanner.yylex();
		if (got.type == Sym.EOF) {
			eof = got;
		}
		return got;
	}

	public Token next() throws IOException
	{
		if (have < 1)
			return _get();

		Token out = buff[first++];
		first &= buff.length - 1;
		have--;
		return out;
	}

	public Token peek() throws IOException
	{
		if (have < 1) {
			Token got = _get();
			buff[first] = got;
			have = 1;
			return got;
		} else {
			return buff[first];
		}
	}

	public Token peek(int distance) throws IOException
	{
		int bufLen = buff.length;
		if (distance >= bufLen) {
			int newlen = bufLen * 2;
			while (distance >= newlen)
				newlen *= 2;

			Token[] tmp = new Token[newlen * 2];
			if (first + have > bufLen) { // wrappeth
				int firstPart = bufLen - first;
				System.arraycopy(buff, first, tmp, 0, firstPart);
				System.arraycopy(buff, 0, tmp, firstPart, have - firstPart);
			} else {
				System.arraycopy(buff, first, tmp, 0, have);
			}
			buff = tmp;
			first = 0;
			bufLen = newlen;
		}
		int mask = bufLen - 1;
		if (have <= distance) {
			while (have <= distance) {
				buff[(first + (have++)) & mask] = _get();
			}
		}
		return buff[(first + distance) & mask];
	}

	public void skip(int numToSkip) throws IOException
	{
		if (numToSkip <= have) {
			have -= numToSkip;
			first = (first + numToSkip) & (buff.length - 1);
		} else {
			first = (first + have) & (buff.length - 1);
			numToSkip -= have;
			have = 0;
			while (numToSkip-- > 0)
				_get();
		}
	}
}