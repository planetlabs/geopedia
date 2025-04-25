/*
 *
 */
package com.sinergise.geopedia.expr;

import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.expr.lexer.Token;

public class SymUtil implements Sym{
    public static String toString(Token t)
    {   
        return toString(t.type, t.value);
    }

    public static String toString(int tokType, Object value)
    {
        switch (tokType) {
        case EOF:
            return "<<EOF>>";
        case LITERAL_STRING:
            return value == null ? "STRING" : "STRING (" + value + ")";
        case LITERAL_CHAR:
            return value == null ? "CHAR" : "CHAR(" + value + ")";
        case LITERAL_INT:
            return value == null ? "INT" : "INT(" + value + ")";
        case LITERAL_FLOAT:
            return value == null ? "FLOAT" : "FLOAT(" + value + ")";
        case TRUE:
            return "true";
        case FALSE:
            return "false";
        case NULL:
            return "null";
        case LESS:
            return "<";
        case LESS_EQ:
            return "<=";
        case EQUALS:
            return "==";
        case NOT_EQUALS:
            return "!=";
        case GREATER_EQ:
            return ">=";
        case GREATER:
            return ">";

        case PLUS:
            return "+";
        case MINUS:
            return "-";
        case STAR:
            return "*";
        case SLASH:
            return "/";
        case PERCENT:
            return "%";

        case AMP:
            return "&";
        case AMPAMP:
            return "&&";
        case BAR:
            return "|";
        case BARBAR:
            return "||";
        case CAR:
            return "^";

        case SHL:
            return "<<";
        case SHR:
            return ">>";
        case SHRU:
            return ">>>";

        case DOT:
            return ".";
        case FOLLOW:
            return "->";

        case LPAREN:
            return "(";
        case RPAREN:
            return ")";
        case LCURLY:
            return "{";
        case RCURLY:
            return "}";
        case LBRACKET:
            return "[";
        case RBRACKET:
            return "]";

        case TILDE:
            return "~";
        case EXCL:
            return "!";

        case COLON:
            return ":";
        case COMMA:
            return ",";
        case ASSIGN:
            return "=";
        case QUESTION:
            return "?";
        case IDENT:
            return value == null ? "IDENT" : "IDENT(" + value + ")";
        }

        throw new IllegalArgumentException();
    }

	public static String toSqlString(int op)
    {
		switch(op) {
		case EQUALS: return "=";
		}

		return toString(op, null);
    }
}

