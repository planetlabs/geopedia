package com.cosylab.pedia.expr.lexer;

import java.io.Reader;

import com.cosylab.pedia.expr.ErrorReporter;
import com.cosylab.pedia.expr.parser.Sym;

%%

%class Lexer
%implements Sym
%unicode
%line
%column
%public
%type Token

%{
	public Lexer(Reader reader, ErrorReporter err)
	{
		this(reader);
		this.err = err;
	}
	
	ErrorReporter err;
	StringBuffer sb = new StringBuffer();
	StringBuffer rawsb = new StringBuffer();
	int stringState = 0;
	
	private void intoString()
	{
		stringState = yystate();
		yybegin(STRING);
	}
	
	private void intoChar()
	{
		stringState = yystate();
		yybegin(CHAR);
	}
	
	private void fromStringChar()
	{
		yybegin(stringState);
	}
	
	private void reportError(String msg)
	{
		err.error(msg, new Token(null, yyline, yycolumn));
	}
	
	private Token tok(int tt)
	{
		return new Token(tt, yyline, yycolumn);
	}
	
	private Token tok(int tt, String txt)
	{
		return new Token(tt, txt, yyline, yycolumn);
	}

	private Token strtok(String txt)
	{
		return new Token(LITERAL_STRING, txt, yyline, yycolumn);
	}

	private Token tok(int tt, Character c)
	{
		return new Token(tt, c, yyline, yycolumn);
	}
	
	private Token tok(int tt, Integer i)
	{
		return new Token(tt, i, yyline, yycolumn);
	}
	
	private Token tok(int tt, Float f)
	{
		return new Token(tt, f, yyline, yycolumn);
	}
	
	private Token tok(int tt, Double d)
	{
		return new Token(tt, d, yyline, yycolumn);
	}
	
	private Token tok(int tt, Long l)
	{
		return new Token(tt, l, yyline, yycolumn);
	}
	
	private long parseLong(int start, int end, int radix)
	{
		long result = 0;
		long digit;
		
		for (int i = start; i < end; i++) {
			digit  = Character.digit(yycharat(i),radix);
			result*= radix;
			result+= digit;
		}
		
		return result;
	}

	int numParens = 0;
	boolean fromStmts = false;
%}

Identifier = [:jletter:] [:jletterdigit:]*
LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]

DecIntegerLiteral = 0 | [1-9][0-9]*
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]
    
/* floating point literals */        
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
FLit3    = [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

%state STRING CHAR

%%

<YYINITIAL> {
	"{"     { return tok(LCURLY);     }
	"}"     { return tok(RCURLY);     }
	"["     { return tok(LBRACKET);   }
	"]"     { return tok(RBRACKET);   }
	"+"     { return tok(PLUS);       }
	"-"     { return tok(MINUS);      }
	"*"     { return tok(STAR);       }
	"/"     { return tok(SLASH);      }
	"%"     { return tok(PERCENT);    }
	"&"     { return tok(AMP);        }
	"&&"    { return tok(AMPAMP);     }
	"|"     { return tok(BAR);        }
	"||"    { return tok(BARBAR);     }
	"?"     { return tok(QUESTION);   }
	":"     { return tok(COLON);      }
	"^"     { return tok(CAR);        }
	"<"     { return tok(LESS);       }
	"<="    { return tok(LESS_EQ);    }
	">"     { return tok(GREATER);    }
	">="    { return tok(GREATER_EQ); }
	"=="    { return tok(EQUALS);     }
	"="     { return tok(ASSIGN);     }
	"!="    { return tok(NOT_EQUALS); }
	"<<"    { return tok(SHL);        }
	">>"    { return tok(SHR);        }
	">>>"   { return tok(SHRU);       }
	"!"     { return tok(EXCL);       }
	"~"     { return tok(TILDE);      }
	"."     { return tok(DOT);        }
	","     { return tok(COMMA);      }
	"null"    { return tok(NULL);       }
	"true"    { return tok(TRUE);       }
	"false"   { return tok(FALSE);      }
	"("     { return tok(LPAREN);       }
	")"     { return tok(RPAREN);       }
	"->"    { return tok(FOLLOW);       }
	
	{Identifier}                   { return tok(IDENT, yytext()); }
    [\"]                           { intoString(); sb.setLength(0); rawsb.setLength(0); }
	"'"                            { intoChar(); }
	{DecIntegerLiteral}            { return tok(LITERAL_INT, new Integer(yytext())); }
	{DecLongLiteral}               { return tok(LITERAL_INT, new Long(yytext().substring(0,yylength()-1))); }
  
	{HexIntegerLiteral}            { return tok(LITERAL_INT, new Integer((int) parseLong(2, yylength(), 16))); }
	{HexLongLiteral}               { return tok(LITERAL_INT, new Long(parseLong(2, yylength()-1, 16))); }
 
	{OctIntegerLiteral}            { return tok(LITERAL_INT, new Integer((int) parseLong(0, yylength(), 8))); }  
	{OctLongLiteral}               { return tok(LITERAL_INT, new Long(parseLong(0, yylength()-1, 8))); }
  
	{FloatLiteral}                 { return tok(LITERAL_FLOAT, new Float(yytext().substring(0,yylength()-1))); }
	{DoubleLiteral}                { return tok(LITERAL_FLOAT, new Double(yytext())); }
	{DoubleLiteral}[dD]            { return tok(LITERAL_FLOAT, new Double(yytext().substring(0,yylength()-1))); }
		
	{WhiteSpace}                   {  }
	.                              { reportError("Unknown character <"+yytext()+">"); }
}

<STRING> {
  \"                             { fromStringChar(); return strtok(sb.toString()); }
  
  {StringCharacter}+             { sb.append( yytext() ); rawsb.append(yytext()); }
  
  "\\b"                          { sb.append( '\b' ); rawsb.append(yytext()); }
  "\\t"                          { sb.append( '\t' ); rawsb.append(yytext()); }
  "\\n"                          { sb.append( '\n' ); rawsb.append(yytext()); }
  "\\f"                          { sb.append( '\f' ); rawsb.append(yytext()); }
  "\\r"                          { sb.append( '\r' ); rawsb.append(yytext()); }
  "\\\""                         { sb.append( '\"' ); rawsb.append(yytext()); }
  "\\'"                          { sb.append( '\'' ); rawsb.append(yytext()); }
  "\\\\"                         { sb.append( '\\' ); rawsb.append(yytext()); }
  \\[0-3]?{OctDigit}?{OctDigit}  { char val = (char) Integer.parseInt(yytext().substring(1),8);
                        				   sb.append( val ); rawsb.append(yytext()); }
  
  
  \\.                            { reportError("Illegal escape sequence \""+yytext()+"\""); sb.append(yytext()); rawsb.append(yytext()); }
  {LineTerminator}               { reportError("Unterminated string at end of line"); fromStringChar(); return strtok(sb.toString()); }
  <<EOF>>                        { reportError("Unterminated string at end of file"); fromStringChar(); return strtok(sb.toString()); }
}

<CHAR> {
  {SingleCharacter}\'            { fromStringChar(); return tok(LITERAL_CHAR, new Character(yytext().charAt(0))); }
  
  "\\b"\'                        { fromStringChar(); return tok(LITERAL_CHAR, new Character('\b'));}
  "\\t"\'                        { fromStringChar(); return tok(LITERAL_CHAR, new Character('\t'));}
  "\\n"\'                        { fromStringChar(); return tok(LITERAL_CHAR, new Character('\n'));}
  "\\f"\'                        { fromStringChar(); return tok(LITERAL_CHAR, new Character('\f'));}
  "\\r"\'                        { fromStringChar(); return tok(LITERAL_CHAR, new Character('\r'));}
  "\\\""\'                       { fromStringChar(); return tok(LITERAL_CHAR, new Character('\"'));}
  "\\'"\'                        { fromStringChar(); return tok(LITERAL_CHAR, new Character('\''));}
  "\\\\"\'                       { fromStringChar(); return tok(LITERAL_CHAR, new Character('\\')); }
  \\[0-3]?{OctDigit}?{OctDigit}\' { fromStringChar();
			                              int val = Integer.parseInt(yytext().substring(1,yylength()-1),8);
			                            return tok(LITERAL_CHAR, new Character((char)val)); }
  '                              { fromStringChar(); reportError("Empty character"); return tok(LITERAL_CHAR, new Character((char)0)); }
  \\.'                           { fromStringChar(); reportError("Illegal escape sequence \""+yytext()+"\""); return tok(LITERAL_CHAR, new Character(yytext().charAt(1))); }
  .*\'                           { fromStringChar(); reportError("Too many characters in character literal"); return tok(LITERAL_CHAR, new Character((char)0)); }
  {LineTerminator}               { fromStringChar(); reportError("Unterminated character literal at end of line"); return tok(LITERAL_CHAR, new Character((char)0)); }
  .                              { fromStringChar(); reportError("Missing ' after '"+yytext()); return tok(LITERAL_CHAR, new Character(yytext().charAt(0))); }
  <<EOF>>                        { fromStringChar(); reportError("Unterminated character literal at end of file"); return tok(LITERAL_CHAR, new Character((char)0)); }
}

<<EOF>> { return new Token(Sym.EOF, yyline, yycolumn); }