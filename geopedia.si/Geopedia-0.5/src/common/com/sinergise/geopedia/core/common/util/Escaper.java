/*
 *
 */
package com.sinergise.geopedia.core.common.util;

public class Escaper {
    private char escapeChar='\\';
    private char quoteChar='"';
    private char[] toEscape=new char[] {'/'};
    public Escaper() {
    }
    public Escaper(char quote, char esc, char[] tspecials) {
        this.quoteChar=quote;
        this.escapeChar=esc;
        this.toEscape=tspecials;
    }
    public String escape(String str) {
        int len=str.length();
        boolean containsQuote=false;
        boolean containsEscape=false;
        boolean containsOtherSpecial=false;
        for (int i = 0; i < len; i++) {
            char ch=str.charAt(i);
            if (ch==escapeChar) {
                containsEscape=true;
            }
            if (ch==quoteChar) { 
                containsQuote=true;
            }
            if (!containsOtherSpecial) {
                for (int j = 0; j < toEscape.length; j++) {
                    if (ch==toEscape[j]) {
                        containsOtherSpecial=true;
                        break;
                    }
                }
            }
        }
        
        if (!(containsQuote || containsEscape || containsOtherSpecial)) return str;
        // replace the escape char
        if (containsEscape) {
//            System.out.println(str);
            str=str.replaceAll("\\"+escapeChar, "\\"+escapeChar+"\\"+escapeChar);
//            System.out.println(str);
        }
        if (!containsQuote) {
            if (containsOtherSpecial) {
                return quoteChar+str+quoteChar;
            } else {
                return str;
            }
        }
        
        str=str.replaceAll("\\"+quoteChar, "\\"+escapeChar+"\\"+quoteChar);
        if (containsOtherSpecial) return quoteChar+str+quoteChar;
        else return str;
    }
    public static void main(String[] args) {
        System.out.println(new Escaper().escape("Miha Bla"));
    }
}
