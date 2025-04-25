package com.sinergise.geopedia.core.style;

public interface Sym
{
	public static final int EOF = 0;

	public static final int LITERAL_STRING = 1;
	public static final int LITERAL_CHAR = 2;
	public static final int LITERAL_INT = 3;
	public static final int LITERAL_FLOAT = 4;
	public static final int TRUE = 5;
	public static final int FALSE = 6;
	public static final int NULL = 7;

	public static final int LESS = 8;
	public static final int LESS_EQ = 9;
	public static final int EQUALS = 10;
	public static final int NOT_EQUALS = 11;
	public static final int GREATER_EQ = 12;
	public static final int GREATER = 13;

	public static final int PLUS = 14;
	public static final int MINUS = 15;
	public static final int STAR = 16;
	public static final int SLASH = 17;
	public static final int PERCENT = 18;

	public static final int AMP = 19;
	public static final int AMPAMP = 20;
	public static final int BAR = 21;
	public static final int BARBAR = 22;
	public static final int CAR = 23;

	public static final int SHL = 24;
	public static final int SHR = 25;
	public static final int SHRU = 26;

	public static final int DOT = 27;
	public static final int FOLLOW = 28;

	public static final int LPAREN = 29;
	public static final int RPAREN = 30;
	public static final int LCURLY = 31;
	public static final int RCURLY = 32;
	public static final int LBRACKET = 33;
	public static final int RBRACKET = 34;

	public static final int TILDE = 35;
	public static final int EXCL = 36;

	public static final int COLON = 37;
	public static final int COMMA = 38;
	public static final int ASSIGN = 39;
	public static final int QUESTION = 40;
	public static final int IDENT = 41;

	public static class Util {
		public static String toString(int tokType)
		{
			switch (tokType) {
			case EOF:
				return "<<EOF>>";
			case LITERAL_STRING:
				return "STRING";
			case LITERAL_CHAR:
				return "CHAR";
			case LITERAL_INT:
				return "INT";
			case LITERAL_FLOAT:
				return "FLOAT";
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
				return "IDENT";
			}
	
			throw new IllegalArgumentException();
		}
	}
}
