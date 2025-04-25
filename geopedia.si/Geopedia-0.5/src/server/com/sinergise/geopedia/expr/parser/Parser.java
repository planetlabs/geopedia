package com.sinergise.geopedia.expr.parser;

import java.io.IOException;

import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.expr.ErrorReporter;
import com.sinergise.geopedia.expr.SymUtil;
import com.sinergise.geopedia.expr.dom.ArrayAccess;
import com.sinergise.geopedia.expr.dom.BasicNumericOp;
import com.sinergise.geopedia.expr.dom.BitwiseOp;
import com.sinergise.geopedia.expr.dom.CompareOp;
import com.sinergise.geopedia.expr.dom.ConditionalOp;
import com.sinergise.geopedia.expr.dom.EqualsOp;
import com.sinergise.geopedia.expr.dom.Expression;
import com.sinergise.geopedia.expr.dom.Expressions;
import com.sinergise.geopedia.expr.dom.Literal;
import com.sinergise.geopedia.expr.dom.MethodCall;
import com.sinergise.geopedia.expr.dom.Null;
import com.sinergise.geopedia.expr.dom.Property;
import com.sinergise.geopedia.expr.dom.ShiftOp;
import com.sinergise.geopedia.expr.dom.Ternary;
import com.sinergise.geopedia.expr.dom.UnaryOp;
import com.sinergise.geopedia.expr.lexer.Token;
import com.sinergise.geopedia.expr.lexer.Tokens;

public class Parser implements Sym
{
	Tokens toks;

	private ErrorReporter err;

	public Parser(Tokens toks, ErrorReporter err)
	{
		if (toks == null || err == null)
			throw new IllegalArgumentException();

		this.toks = toks;
		this.err = err;
	}

	private void unexpectedToken(Token got, int typeExpected)
	{
		err.error("Unexpected " + SymUtil.toString(got) + " where " + SymUtil.toString(typeExpected, null) + " was expected", got);
	}

	private Token expect(int typeExpected) throws IOException
	{
		Token tok = toks.peek();
		if (tok.type != typeExpected) {
			unexpectedToken(tok, typeExpected);
			return null;
		} else {
			return toks.next();
		}
	}

	public Expression parseExpression() throws IOException
	{
		Expression a = parseConditionalOrExpression();
		if (toks.peek().type == QUESTION) {
			expect(QUESTION);
			Expression b = parseExpression();
			expect(COLON);
			Expression c = parseExpression();

			return new Ternary(a, b, c);
		} else {
			return a;
		}
	}

	private Expression parseConditionalOrExpression() throws IOException
	{
		Expression a = parseConditionalAndExpression();
		while (toks.peek().type == BARBAR) {
			toks.next();
			Expression b = parseConditionalAndExpression();
			a = new ConditionalOp(a, BARBAR, b);
		}
		return a;
	}

	private Expression parseConditionalAndExpression() throws IOException
	{
		Expression a = parseInclusiveOrExpression();
		while (toks.peek().type == AMPAMP) {
			toks.next();
			Expression b = parseInclusiveOrExpression();
			a = new ConditionalOp(a, AMPAMP, b);
		}
		return a;
	}

	private Expression parseInclusiveOrExpression() throws IOException
	{
		Expression a = parseExclusiveOrExpression();
		while (toks.peek().type == BAR) {
			toks.next();
			Expression b = parseExclusiveOrExpression();
			a = new BitwiseOp(a, BAR, b);
		}
		return a;
	}

	private Expression parseExclusiveOrExpression() throws IOException
	{
		Expression a = parseAndExpression();
		while (toks.peek().type == CAR) {
			toks.next();
			Expression b = parseAndExpression();
			a = new BitwiseOp(a, CAR, b);
		}
		return a;
	}

	private Expression parseAndExpression() throws IOException
	{
		Expression a = parseEqualityExpression();
		while (toks.peek().type == AMP) {
			toks.next();
			Expression b = parseEqualityExpression();
			a = new BitwiseOp(a, AMP, b);
		}
		return a;
	}

	private Expression parseEqualityExpression() throws IOException
	{
		Expression a = parseRelationalExpression();
		Token next = toks.peek();
		int nt = next.type;

		while (nt == EQUALS || nt == NOT_EQUALS) {
			toks.next();
			Expression b = parseRelationalExpression();
			a = new EqualsOp(a, nt, b);
			nt = (next = toks.peek()).type;
		}

		return a;
	}

	private Expression parseRelationalExpression() throws IOException
	{
		Expression a = parseShiftExpression();
		Token next = toks.peek();
		int nt = next.type;

		while (nt == LESS || nt == LESS_EQ || nt == GREATER || nt == GREATER_EQ) {
			toks.next();
			Expression b = parseShiftExpression();
			a = new CompareOp(a, nt, b);
			nt = (next = toks.peek()).type;
		}

		return a;
	}

	private Expression parseShiftExpression() throws IOException
	{
		Expression a = parseAdditiveExpression();
		Token next = toks.peek();
		int nt = next.type;

		while (nt == SHL || nt == SHR || nt == SHRU) {
			toks.next();
			Expression b = parseAdditiveExpression();
			a = new ShiftOp(a, nt, b);
			nt = (next = toks.peek()).type;
		}

		return a;
	}

	private Expression parseAdditiveExpression() throws IOException
	{
		Expression a = parseMultiplicativeExpression();
		Token next = toks.peek();
		int nt = next.type;

		while (nt == PLUS || nt == MINUS) {
			toks.next();
			Expression b = parseMultiplicativeExpression();
			a = new BasicNumericOp(a, nt, b);
			nt = (next = toks.peek()).type;
		}

		return a;
	}

	private Expression parseMultiplicativeExpression() throws IOException
	{
		Expression a = parseUnaryExpression();
		Token next = toks.peek();
		int nt = next.type;

		while (nt == STAR || nt == SLASH || nt == PERCENT) {
			toks.next();
			Expression b = parseUnaryExpression();
			a = new BasicNumericOp(a, nt, b);
			nt = (next = toks.peek()).type;
		}

		return a;
	}

	private Expression parseUnaryExpression() throws IOException
	{
		int nt = toks.peek().type;

		if (nt == PLUS || nt == MINUS || nt == TILDE || nt == EXCL) {
			toks.next();
			if (nt == PLUS) {
				return parseUnaryExpression();
			} else {
				Expression a = parseUnaryExpression();
				return new UnaryOp(nt, a);
			}
		} else {
			return parsePrimaryExpression();
		}
	}

	private Expression parsePrimaryExpression() throws IOException
	{
		int nt = toks.peek().type;
		Expression e;

		if (nt == LITERAL_CHAR || nt == LITERAL_STRING || nt == LITERAL_INT || nt == LITERAL_FLOAT) {
			Token a = toks.next();
			e = new Literal(a.value, a);
		} else if (nt == TRUE) {
			e = new Literal(Boolean.TRUE, toks.next());
		} else if (nt == FALSE) {
			e = new Literal(Boolean.FALSE, toks.next());
		} else if (nt == NULL) {
			e = new Literal(Null.instance, toks.next());
		} else if (nt == LPAREN) {
			toks.next();
			e = parseExpression();
			expect(RPAREN);
		} else if (nt == IDENT) {
			int nnt = toks.peek(1).type;
			if (nnt == LPAREN) {
				String methodName = (String) toks.next().value;
				Token a = toks.next();

				Expressions params = null;
				Token next = toks.peek();
				while (next.type != RPAREN && next.type != EOF) {
					if (params == null) {
						params = new Expressions(parseExpression());
					} else {
						expect(COMMA);
						params.add(parseExpression());
					}
					next = toks.peek();
				}
				expect(RPAREN);

				e = new MethodCall(a, methodName, params);
			} else {
				Token a = toks.next();
				e = new Property(a, (String) a.value);
			}
		} else {
			Token first = toks.next();
			err.error("Unexpected " + first.type + " where a primary expression was expected", first);
			e = new Literal(Null.instance, first);
		}

		while (true) {
			nt = toks.peek().type;
			if (nt == LBRACKET) {
				toks.next();
				Expression idx = parseExpression();
				expect(RBRACKET);
				e = new ArrayAccess(e, idx);
			} else if (nt == DOT) {
				toks.next();
				Token id = expect(IDENT);
				if (id != null) {
					String name = (String) id.value;
					if (toks.peek().type == LPAREN) {
						toks.next();

						Expressions params = null;
						while (true) {
							Token next = toks.peek();
							if (next.type == RPAREN || next.type == EOF)
								break;

							if (params == null) {
								params = new Expressions(parseExpression());
							} else {
								expect(COMMA);
								params.add(parseExpression());
							}
						}
						expect(RPAREN);

						e = new MethodCall(e, name, params);
					} else {
						e = new Property(e, name);
					}
				}
			} else {
				return e;
			}
		}
	}
}
