/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

/*
 * Created on Dec 16, 2003
 *
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.sinergise.common.util.string;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for transforming arrays of strings into javax.naming.CompositeName - like form, with customizable separators, quote chars and escape
 * character.
 * 
 * @author Miha Kadunc
 * @see javax.naming.CompositeName
 */
public class Escaper {
	private static final char[] DEFAULT_SEPARATORS = {'/'};
	private static final char[] DEFAULT_QUOTES     = {'\'', '"'};
	private static final char   DEFAULT_ESCAPE     = '\\';
	private static Escaper      defaultInstance;
	private static final int    STYLE_NONE         = -1;
	private static final int    STYLE_ESCAPE       = -2;
	
	// Index of the quote to be used will be passed for QUOTE style
	private final char[]        separators;
	private final char[]        quotes;
	private final char          escape;
	private final String[]      escapedSeparators;
	private String[]            escapedQuotes;
	private final String        escapedEscape;
	private boolean             separatorIfEmpty   = false;
	
	/**
	 * Returns true iff string rn contains an block of c characters of even length which ends at index endIdx.
	 * 
	 * @param rn
	 * @param endIdx
	 * @param c
	 * @return
	 */
	private static boolean isEven(final char[] rn, int endIdx, final char c) {
		boolean ret = true;
		
		while (endIdx >= 0 && rn[endIdx--] == c) {
			ret = !ret;
		}
		
		return ret;
	}
	
	/**
	 * Creates a new instance of escaper with the specified separators.
	 * 
	 * @param separators The characters that should be used as separators by this escaper.
	 * @param quotes The characters that will be used for quoting components.
	 * @param escapeChar The character that will be used for escaping
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public Escaper(char[] separators, final char[] quotes, final char escapeChar) {
		super();
		
		if (separators == null) {
			separators = DEFAULT_SEPARATORS;
		}
		
		this.separators = separators;
		this.quotes = quotes;
		this.escape = escapeChar;
		
		// Check for consistency
		if (quotes != null) {
			for (final char quote : quotes) {
				if (quote == escape) {
					throw new IllegalArgumentException("Escape character " + escape + " is the same as one of the quote characters.");
				}
				
				if (isSeparator(quote)) {
					throw new IllegalArgumentException("Separator character " + quote + " is the same as one of the quote characters.");
				}
			}
		}
		
		for (final char separator : separators) {
			if (separator == escape) {
				throw new IllegalArgumentException("Escape character " + escape + " is the same as one of the separator characters.");
			}
		}
		
		escapedSeparators = new String[separators.length];
		
		for (int i = 0; i < separators.length; i++) {
			escapedSeparators[i] = (new StringBuffer().append(escape).append(separators[i])).toString();
		}
		
		escapedQuotes = null;
		if (quotes != null) {
			escapedQuotes = new String[quotes.length];
			for (int i = 0; i < quotes.length; i++) {
				escapedQuotes[i] = (new StringBuffer().append(escape).append(quotes[i])).toString();
			}
		}
		
		escapedEscape = (new StringBuffer().append(escape).append(escape)).toString();
	}
	
	/**
	 * Creates a new instance of escaper with the specified separators.
	 * 
	 * @param separators The characters that should be used as separators by this escaper
	 */
	public Escaper(final char[] separators) {
		this(separators, DEFAULT_QUOTES, DEFAULT_ESCAPE);
	}
	
	/**
	 * Creates a new instance of escaper with separators, quotes and escape character equal to that of {@link javax.naming.CompositeName}
	 */
	public Escaper() {
		this(DEFAULT_SEPARATORS, DEFAULT_QUOTES, DEFAULT_ESCAPE);
	}
	
	public Escaper(final char escapeChar, final char[] separators) {
		this(separators, DEFAULT_QUOTES, escapeChar);
	}
	
	/**
	 * Parses the passed string and splits it to components. The components are unescaped if necessary.
	 * 
	 * @param name the name which should be parsed
	 * @return an array of name's components
	 */
	public String[] stringToComponents(final String name) {
		return stringToComponents(name, null);
	}
	
	/**
	 * Parses the passed string and splits it to components. The components are unescaped if necessary. Information about the separators
	 * between names is stored into the passed java.util.List
	 * 
	 * @param name the String that should be parsed
	 * @param outSeparators the List that will contain separators that were used to separate components in the string
	 * @return
	 */
	public String[] stringToComponents(final String name, final List<Character> outSeparators) {
		final List<String> retList = new ArrayList<String>();
		final char[] chars = name.toCharArray();
		final StringBuffer temp = new StringBuffer();
		int sepIndex = -1;
		int oldSepIndex = -1;
		
		do {
			oldSepIndex = sepIndex;
			sepIndex = nextSeparatorIndex(chars, oldSepIndex + 1);
			
			if (sepIndex < chars.length && outSeparators != null) {
				outSeparators.add(new Character(chars[sepIndex]));
			}
			
			temp.delete(0, temp.length());
			temp.append(chars, oldSepIndex + 1, sepIndex - oldSepIndex - 1);
			unescapeComponentBuffer(temp);
			retList.add(temp.toString());
		} while (sepIndex < chars.length);
		
		// Handle cases with all zero components ''={} '/'={""} '/?'={"",""} '///' (3)
		if (separatorIfEmpty && (retList.size() == name.length() + 1)) {
			retList.remove(0);
			if (outSeparators != null && outSeparators.size() > 0) {
				outSeparators.remove(0);
			}
		}
		
		final String[] ret = new String[retList.size()];
		retList.toArray(ret);
		
		return ret;
	}
	
	private int nextSeparatorIndex(final char[] chars, final int startIndex) {
		int i = startIndex;
		final int len = chars.length;
		
		if (startIndex >= len) {
			return len;
		}
		
		final char currentQuote = chars[startIndex];
		
		if (isQuote(currentQuote)) {
			// Next component is quoted
			do {
				i++;
				i = indexOf(chars, currentQuote, i);
			} while (i >= 0 && chars[i - 1] == escape);
			
			if (i < 0) {
				throw new IllegalArgumentException("Unfinished quote: " + currentQuote + " index: " + i + " in " + new String(chars));
			} else if (!(i + 1 == len || isSeparator(chars[i + 1]))) {
				throw new IllegalArgumentException("Unfinished quote: " + currentQuote + " index: " + startIndex + " in " + new String(chars));
			}
			
			return i + 1;
		}
		while (i < len) {
			if (isSeparator(chars[i]) && isEven(chars, i - 1, escape)) {
				// found = true;
				
				return i;
			}
			
			i++;
			
			if (i == len && isEven(chars, i - 1, escape)) {
				return len;
			}
		}
		
		throw new IllegalArgumentException("Unescaped \\ at the end of component " + new String(chars));
	}
	
	private static int indexOf(final char[] chars, final char ch, final int start) {
		for (int i = start; i < chars.length; i++) {
			if (chars[i] == ch) return i;
		}
		return -1;
	}
	
	private void appendEscapedComponents(final StringBuffer target, final String[] components, final char[] compSeparators) {
		if (components.length == 0) return;
		
		final StringBuffer tempBuf = new StringBuffer();
		int meth = determineEscapingMethod(components[0]);
		
		if (meth != STYLE_NONE) {
			tempBuf.append(components[0]);
			target.append(escapeComponentBuffer(tempBuf, meth));
		} else {
			target.append(components[0]);
		}
		
		for (int i = 1; i < components.length; i++) {
			target.append(compSeparators[i - 1]);
			meth = determineEscapingMethod(components[i]);
			
			if (meth != STYLE_NONE) {
				tempBuf.replace(0, tempBuf.length(), components[i]);
				target.append(escapeComponentBuffer(tempBuf, meth));
			} else {
				target.append(components[i]);
			}
		}
		
		return;
	}
	
	/**
	 * Transforms an array of strings into a single string, escaping components if necessary. Each component is followed by an adjacent
	 * separator from the separators array.
	 * 
	 * @param components String[] containing separate components
	 * @param compSeparators char[] containing separators that will be appended to adjacent components
	 * @return composed string with escaped components
	 */
	public String componentsToEscapedString(final String[] components, char[] compSeparators) {
		final int compLen = components.length;
		if (compLen == 0) {
			return "";
		}
		
		if (compSeparators == null) {
			compSeparators = new char[]{this.separators[0]};
		}
		// Fix separators length
		if (compSeparators.length < compLen - 1) {
			compSeparators = fillSeparators(compSeparators, compLen);
		}
		
		final StringBuffer ret = new StringBuffer();
		appendEscapedComponents(ret, components, compSeparators);
		
		if (separatorIfEmpty && ret.length() == components.length - 1) {
			ret.insert(0, this.separators[0]);
		}
		
		return ret.toString();
	}
	
	private char[] fillSeparators(char[] compSeparators, final int compLen) {
		char sep = this.separators[0];
		
		if (compSeparators.length == 1) {
			sep = compSeparators[0];
		}
		
		final char[] newSeps = new char[compLen - 1];
		System.arraycopy(compSeparators, 0, newSeps, 0, compSeparators.length);
		
		for (int i = compSeparators.length; i < newSeps.length; i++) {
			newSeps[i] = sep;
		}
		
		compSeparators = newSeps;
		return compSeparators;
	}
	
	private StringBuffer escapeComponentAsStringBuffer(final String component) {
		final StringBuffer buf = new StringBuffer(component);
		final int esc = determineEscapingMethod(component);
		
		if (esc != STYLE_NONE) {
			escapeComponentBuffer(buf, esc);
		}
		
		return buf;
	}
	
	private StringBuffer escapeComponentBuffer(final StringBuffer component, final int escapingMethod) {
		if (escapingMethod == STYLE_NONE) {
			return component;
		}
		
		if (escapingMethod == STYLE_ESCAPE) {
			// Escape preceding meta char
			// escape
			StringUtil.prepend(component, escapedEscape, escape, 1);
			
			// separators
			for (final String escapedSeparator : escapedSeparators) {
				StringUtil.prepend(component, escapedSeparator, escape);
			}
			
			// quotes
			if (quotes != null) {
				for (final String escapedQuote : escapedQuotes) {
					StringUtil.prepend(component, escapedQuote, escape);
				}
			}
			
			// Separators
			for (final char separator : separators) {
				StringUtil.prepend(component, separator, escape);
			}
			
			// Escape at the end
			if (component.charAt(component.length() - 1) == escape) {
				component.append(escape);
			}
			
			// Leading quote
			if (isQuote(component.charAt(0))) {
				component.insert(0, escape);
			}
		} else {
			// Escape with quotes
			final char quote = quotes[escapingMethod];
			StringUtil.prepend(component, quote, escape);
			component.insert(0, quote);
			component.append(quote);
		}
		
		return component;
	}
	
	private StringBuffer unescapeComponentBuffer(final StringBuffer component) {
		if (component.length() < 1) {
			return component;
		}
		
		final int quoteIndex = quoteIdx(component.charAt(0));
		
		if (quoteIndex >= 0) { // We have a quoted string
			component.deleteCharAt(0);
			component.deleteCharAt(component.length() - 1);
			StringUtil.replace(component, escapedQuotes[quoteIndex], quotes[quoteIndex]);
		} else { // We have an escaped string
		
			int idx = StringUtil.indexOf(component, escape);
			
			while (idx >= 0) {
				if (isMeta(component.charAt(idx + 1))) {
					component.deleteCharAt(idx);
				}
				
				idx++;
				
				if (idx == component.length()) {
					break;
				}
				
				idx = StringUtil.indexOf(component, escape, idx);
			}
		}
		
		return component;
	}
	
	/**
	 * Processes a single component of a name and escapes it so that it can be concatenated with separators to form a string representation
	 * of the name.
	 * 
	 * @param component that should be escaped
	 * @return escaped component
	 * @throws NullPointerException if <code>component</code> is null
	 */
	public String escapeComponent(final String component) {
		if (component == null) {
			throw new NullPointerException("component");
		}
		
		if (determineEscapingMethod(component) != STYLE_NONE) {
			return escapeComponentAsStringBuffer(component).toString();
		}
		
		return component;
	}
	
	/**
	 * Takes an escaped component of a name and removes all escape characters and sequences to reveal original string.
	 * 
	 * @param component escaped string
	 * @return unescaped component
	 */
	public String unescapeComponent(final String component) {
		final StringBuffer buf = new StringBuffer(component);
		unescapeComponentBuffer(buf);
		
		return buf.toString();
	}
	
	private boolean isMeta(final char c) {
		return (isQuote(c) || isSeparator(c) || c == escape);
	}
	
	private boolean isQuote(final char c) {
		if (quotes == null) {
			return false;
		}
		for (final char quote : quotes) {
			if (quote == c) {
				return true;
			}
		}
		
		return false;
	}
	
	private int quoteIdx(final char c) {
		if (quotes == null) {
			return -1;
		}
		for (int i = 0; i < quotes.length; i++) {
			if (quotes[i] == c) {
				return i;
			}
		}
		
		return -1;
	}
	
	private boolean isSeparator(final char c) {
		for (final char separator : separators) {
			if (separator == c) {
				return true;
			}
		}
		
		return false;
	}
	
	private int determineEscapingMethod(final String str) {
		if (str.length() < 1) {
			return STYLE_NONE;
		}
		
		boolean hasSeparator = false;
		
		// Component separator
		for (final char separator : separators) {
			if (str.indexOf(separator) >= 0) {
				hasSeparator = true;
				
				break;
			}
		}
		
		// Leading quote
		int leadingQuote = -1;
		if (quotes != null) {
			for (int i = 0; i < quotes.length; i++) {
				if (str.charAt(0) == quotes[i]) {
					leadingQuote = i;
					break;
				}
			}
		}
		
		// Escape at the end
		boolean escapeAtEnd = false;
		
		if (str.charAt(str.length() - 1) == escape) {
			escapeAtEnd = true;
		}
		
		// Escape preceding meta character
		boolean escapeProblem = false;
		
		if (!escapeProblem && str.indexOf(escapedEscape) >= 0) {
			escapeProblem = true;
		}
		
		if (quotes != null) {
			for (int i = 0; i < quotes.length; i++) {
				if (str.indexOf(escapedQuotes[i]) >= 0) {
					escapeProblem = true;
					break;
				}
			}
		}
		
		if (!(hasSeparator || leadingQuote > -1 || escapeProblem || escapeAtEnd)) {
			return STYLE_NONE;
		} else if (quotes == null || escapeAtEnd) {
			return STYLE_ESCAPE;
		} else { // Determine best quote to use
		
			int quote = 0;
			
			for (int i = 0; i < quotes.length; i++) {
				if (str.indexOf(quotes[i]) < 0) {
					quote = i;
					
					break;
				}
			}
			
			return quote;
		}
	}
	
	/**
	 * Provides access to default escaper instance, using same separators, quotes and escape characters as javax.naming.CompositeName
	 * 
	 * @return default Escaper instance
	 */
	public static Escaper getDefaultEscaper() {
		if (defaultInstance == null) {
			defaultInstance = new Escaper().setAddSeparatorIfEmpty(true);
		}
		
		return defaultInstance;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param nm DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static String toString(final String[] comps, final char[] separators) {
		return getDefaultEscaper().componentsToEscapedString(comps, separators);
	}
	
	/**
	 * @return
	 */
	public char[] getSeparators() {
		return separators;
	}
	
	/**
	 * @return
	 */
	public char getEscapeChar() {
		return escape;
	}
	
	/**
	 * @return
	 */
	public char[] getQuotes() {
		return quotes;
	}
	
	public int specialCharIndex(final String composedStr, final int specialChar) {
		return specialCharIndex(composedStr, specialChar, 0);
	}
	
	public int specialCharIndex(final String composedStr, final int specialChar, final int fromIndex) {
		boolean inQuote = false;
		boolean esc = false;
		boolean tokenStart = true; // first token
		int curIdx = fromIndex;
		final int len = composedStr.length();
		while (curIdx < len) {
			final char ch = composedStr.charAt(curIdx++);
			boolean curTokenStart = false;
			if (!esc && ch == escape) {
				esc = true;
			} else if (esc) {
				esc = false;
			} else {
				esc = false;
				if ((inQuote || tokenStart) && isQuote(ch)) {
					inQuote = !inQuote;
				} else if (!inQuote) {
					if (ch == specialChar) {
						return curIdx - 1;
					}
					if (isMeta(ch)) {
						curTokenStart = true;
					}
				}
			}
			tokenStart = curTokenStart;
		}
		return -1;
	}
	
	public String[] splitEscaped(final String name, final char c) {
		final List<String> retList = new ArrayList<String>();
		final char[] chars = name.toCharArray();
		final int cLen = chars.length;
		final StringBuffer temp = new StringBuffer();
		int sepIndex = -1;
		int oldSepIndex = -1;
		do {
			oldSepIndex = sepIndex;
			do {
				sepIndex = nextSeparatorIndex(chars, sepIndex + 1);
			} while (sepIndex >= 0 && sepIndex < cLen && chars[sepIndex] != c);
			
			temp.delete(0, temp.length());
			temp.append(chars, oldSepIndex + 1, sepIndex - oldSepIndex - 1);
			retList.add(temp.toString());
		} while (sepIndex < cLen);
		
		// Handle cases with all zero components ''={} '/'={""} '/?'={"",""} '///' (3)
		if (separatorIfEmpty && (retList.size() == name.length() + 1)) {
			retList.remove(0);
		}
		
		final String[] ret = new String[retList.size()];
		retList.toArray(ret);
		return ret;
	}
	
	public Escaper setAddSeparatorIfEmpty(final boolean separatorIfEmpty) {
		this.separatorIfEmpty = separatorIfEmpty;
		return this;
	}
	
}

/* __oOo__ */
