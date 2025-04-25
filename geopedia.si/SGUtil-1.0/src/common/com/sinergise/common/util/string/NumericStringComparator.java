/*
 * Copyright (c) 2004 by Cosylab d.o.o.
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

package com.sinergise.common.util.string;

import java.util.Arrays;
import java.util.Comparator;

/**
 * <code>NumericStringComparator</code> ... NumericStringComparator represents class which compares two Strings. Roules are followed:
 * <ol>
 * <li>when there is no number or the strings are different before the first number, result should be the same as str1.compareTo(str2)
 * <ul>
 * <li>"klemen" &lt; "miha"</li>
 * <li>"klemen12" &lt; "miha600"</li>
 * </ul>
 * </li>
 * <li>when two strings are the same up to the first number, the whole number is taken into account and ordered by the result
 * <ul>
 * <li>"miha8" &lt; "miha12"</li>
 * <li>"miha6_klemen3_janez" &lt; "miha6_klemen22a"</li>
 * <li>"miha8y" &lt; "miha13a"</li>
 * </ul>
 * </li>
 * <li>Whitespaces,zeros, and "_" characters preceding numbers should be omitted from the comparison, unless the omission would cause
 * equality:
 * <ul>
 * <li>"klemen 12" &lt; "klemen32"</li>
 * <li>"klemen001" &lt; "klemen12"</li>
 * <li>"klemen__2" &lt; "klemen_13"</li>
 * <li>"klemen__2" &lt; "klemen2"</li>
 * <li>"klemen b" &lt; "klemena" (should be followed by number)</li>
 * <li>"miha0x" &lt; "mihax" (only zeros preceding a number should be omitted)</li>
 * </ul>
 * </li>
 * <li>All of the above should also apply to double numbers.
 * <ul>
 * <li>"miha2.301bla" &lt; "miha12.1423aaa"</li>
 * </ul>
 * </li>
 * <li>When the compared parts of numbers are the same numerically (and written differently), they should be ordered alphabetically:
 * <ul>
 * <li>"miha012klemen5abc" &lt; "miha12klemen5abc"</li>
 * <li>"miha012klemen5abc" &lt; "miha12klemen05abc"</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @author Andrej Kosmrlj (<a href="mailto:andrej.kosmrlj@kgb.ijs.si">andrej.kosmrlj&x40;kgb.ijs.si</a>)
 * @version $Id: NumericStringComparator.java,v 1.11 2005-06-03 15:10:40 mkadunc Exp $
 * @see com.cosylab.util.test
 * @since Oct 15, 2003.
 */

// TODO: Support java.text.Collator - based sorting for locale-dependent strings
public class NumericStringComparator implements Comparator<Object> {
	/** Constant indicating that dot is treated as a decimal separator */
	public static final int                DEC_SEPARATOR_DOT   = 1;
	
	/** Constant indicating that comma is treated as a decimal separator */
	public static final int                DEC_SEPARATOR_COMMA = 2;
	
	/**
	 * Constant indicating that both dot and comma are treated as decimal separators
	 */
	public static final int                DEC_SEPARATOR_BOTH  = 3;
	
	/**
	 * Constant indicating that decimal separators should not be treated differently than other characters
	 */
	public static final int                DEC_SEPARATOR_NONE  = 4;
	private static final char              COMMA               = ',';
	private static final char              DOT                 = '.';
	private static NumericStringComparator defaultInstance;
	
	/**
	 * Convenience method for obtaining a default <code>NumericStringComparator</code>. Array of separators has default value: [' ', '_'].
	 * Decimal mode has default value: <code>DEC_SEPARATOR_DOT</code>.
	 * 
	 * @return The default instance of comparator.
	 */
	public static NumericStringComparator getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new NumericStringComparator();
		}
		
		return defaultInstance;
	}
	
	private boolean   softEquals = false;
	
	/** array of separators */
	private char[]    separators;
	
	/** decimal separator mode: NONE, DOT, COMMA, BOTH */
	private final int decSeparator;
	
	/**
	 * Contructor creates NumericStringComparator with specified array of separators in decimal separator mode. Separator is character which
	 * is not used in compare() method unless there are equal numbers in each String and if Strings are different only with separators
	 * 
	 * @param separators array of separators
	 * @param decSeparator decimal separator mode
	 */
	public NumericStringComparator(final char[] separators, final int decSeparator) {
		setSeparators(separators);
		this.decSeparator = decSeparator;
	}
	
	/**
	 * Contructor creates NumericStringComparator with specified array of separators. Decimal mode has default value: DEC_SEPARATOR_DOT.
	 * 
	 * @param separators array of separators
	 */
	public NumericStringComparator(final char[] separators) {
		this(separators, NumericStringComparator.DEC_SEPARATOR_DOT);
	}
	
	/**
	 * Contructor creates NumericStringComparator with specified decimal mode. Array of separators has default value: [' ', '_'].
	 * 
	 * @param decSeparator decimal mode
	 */
	public NumericStringComparator(final int decSeparator) {
		this(new char[]{' ', '_'}, decSeparator);
	}
	
	/**
	 * Contructor creates NumericStringComparator. Array of separators has default value: [' ', '_']. Decimal mode has default value:
	 * DEC_SEPARATOR_DOT.
	 */
	public NumericStringComparator() {
		this(NumericStringComparator.DEC_SEPARATOR_DOT);
	}
	
	/**
	 * Method adds new separator.
	 * 
	 * @param sep new separator
	 * @throws IllegalArgumentException when the separator is an illegal character such as a digit, a decimal separator or the minus sign
	 */
	public void addSeparator(final char sep) {
		if (Arrays.binarySearch(this.separators, sep) >= 0) {
			return;
		}
		
		if (sep == '-') {
			throw new IllegalArgumentException("Minus sign should not be used as a separator.");
		}
		
		if (isDecSeparator(sep)) {
			throw new IllegalArgumentException("Character '" + sep + "' should not be used as a separator. It is defined as a decimal separator.");
		}
		
		if (Character.isDigit(sep)) {
			throw new IllegalArgumentException("A digit should not be used as a separator.");
		}
		
		final char[] temp = new char[separators.length + 1];
		System.arraycopy(separators, 0, temp, 0, separators.length);
		temp[temp.length - 1] = sep;
		Arrays.sort(temp);
		this.separators = temp;
	}
	
	/**
	 * Method sets separators
	 * 
	 * @param sep array of separators
	 * @throws IllegalArgumentException when the separators array contains illegal character such as digits, decimal separators or minus
	 *             sign
	 */
	public void setSeparators(final char[] sep) {
		this.separators = new char[sep.length];
		System.arraycopy(sep, 0, separators, 0, sep.length);
		Arrays.sort(this.separators);
		
		for (final char element : sep) {
			if (isDecSeparator(element)) {
				throw new IllegalArgumentException("Cannot set " + element + " as separator. It is already set as decimal separator.");
			}
			
			if (Character.isDigit(element)) {
				throw new IllegalArgumentException("Cannot set " + element + " as separator. It is a digit.");
			}
			
			if (element == '-') {
				throw new IllegalArgumentException("Cannot set " + element + " as separator. It is used as a negative sign.");
			}
		}
	}
	
	/**
	 * Method returns separators
	 * 
	 * @return char[] separators
	 */
	public char[] getSeparators() {
		return this.separators;
	}
	
	/**
	 * Method returns decimal separator mode, represented by int
	 * 
	 * @return int
	 */
	public int getDecSeparatorMode() {
		return this.decSeparator;
	}
	
	/**
	 * Method returns true if character is digit or separator, otherwise returns false.
	 * 
	 * @param ch character
	 * @return boolean
	 */
	public boolean isSeparatorOrDigit(final char ch) {
		return (Character.isDigit(ch) || isDecSeparator(ch) || isSeparator(ch));
	}
	
	/**
	 * Method returns true if character is separator, otherwise returns false.
	 * 
	 * @param ch character
	 * @return boolean
	 */
	private final boolean isSeparator(final char ch) {
		int idx = separators.length - 1;
		
		while (idx >= 0) {
			if (separators[idx--] == ch) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Method returns true if character is decimal separator, otherwise returns false.
	 * 
	 * @param ch character
	 * @return boolean
	 */
	private final boolean isDecSeparator(final char ch) {
		switch (this.decSeparator) {
			case DEC_SEPARATOR_NONE:
				return false;
				
			case DEC_SEPARATOR_BOTH:
				return (ch == DOT || ch == COMMA);
				
			case DEC_SEPARATOR_COMMA:
				return ch == COMMA;
				
			case DEC_SEPARATOR_DOT:
				return ch == DOT;
		}
		
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final Object o1, final Object o2) {
		final String s1 = o1.toString();
		final String s2 = o2.toString();
		final int[] idx = {0, 0};
		final int[] len = {s1.length(), s2.length()};
		
		if ((len[0] == 0) || (len[1] == 0)) {
			return s1.compareTo(s2);
		}
		
		final int[] ret = {0, 0, 1}; // {retComp, stringComp, sign}
		
		while (true) {
			if (idx[0] >= len[0]) {
				if (softEquals) {
					return ret[0];
				}
				
				return (ret[1] == 0) ? (idx[1] - len[1]) * ret[2] : ret[1];
			} else if (idx[1] >= len[1]) {
				if (softEquals) {
					return ret[0];
				}
				
				return (ret[1] == 0) ? (len[0] - idx[0]) * ret[2] : ret[1];
			}
			
			if (processItem(s1, s2, idx, len, ret)) {
				return ret[0];
			}
		}
	}
	
	/**
	 * Compares two items of the string at positions specified in idx[] and advances the indices. Items are either characters or numeric
	 * sequences with separators. If a distinct order was determined, <code>true</code> is returned, otherwise this method returns false.
	 * The result is contained in the ret[], where the first number is the distinct comparison value, and the second is used in case two
	 * numeric sequences were equal numerically, but different by string comparison. This second element is not modified it it has
	 * previously been set to a non-zero value.
	 * 
	 * @param s1
	 * @param s2
	 * @param idx
	 * @param len
	 * @param ret
	 * @return
	 */
	private final boolean processItem(final String s1, final String s2, final int[] idx, final int[] len, final int[] ret) {
		char c1;
		char c2;
		int nComp = 0; // holds comparison of the whole part
		boolean decFound = false; // did we find decimal separator
		boolean numFound = false; // did we find any number
		boolean sComp = (ret[1] == 0); // is the string comparison still 0 (in case of numerical equality)
		
		c1 = s1.charAt(idx[0]++);
		c2 = s2.charAt(idx[1]++);
		
		// SKIP SEPARATORS OF 1
		while (isSeparator(c1) || c1 == '0') {
			if (sComp) { // We need this for String comparison if the numbers are equal
			
				if (c1 != c2) {
					ret[1] = c1 - c2;
					sComp = false;
				} else {
					if (idx[1] == len[1]) {
						return false;
					}
					
					c2 = s2.charAt(idx[1]++);
				}
			}
			
			if (idx[0] == len[0]) {
				if (softEquals && c1 == '0') {
					break;
				}
				return false;
			}
			
			c1 = s1.charAt(idx[0]++);
		}
		
		// SKIP (remaining) SEPARATORS OF 2
		while (isSeparator(c2) || c2 == '0') {
			if (sComp) { // We need this for String comparison if the numbers are equal
			
				if (c1 != c2) {
					ret[1] = c1 - c2;
					sComp = false;
				} else {
					if (idx[0] == len[0]) {
						return false;
					}
					
					c1 = s1.charAt(idx[0]++);
				}
			}
			
			if (idx[1] == len[1]) {
				if (softEquals && c2 == '0') {
					break;
				}
				return false;
			}
			
			c2 = s2.charAt(idx[1]++);
		}
		
		boolean minus1 = false;
		
		if (c1 == '-') {
			minus1 = true;
			
			if (idx[0] == len[0]) {
				return false;
			}
			
			c1 = s1.charAt(idx[0]++);
		}
		
		boolean minus2 = false;
		
		if (c2 == '-') {
			minus2 = true;
			
			if (idx[1] == len[1]) {
				return false;
			}
			
			c2 = s2.charAt(idx[1]++);
		}
		
		if (minus1 ^ minus2) {
			if ((isDecSeparator(c1) || Character.isDigit(c1)) && (isDecSeparator(c2) || Character.isDigit(c2))) {
				ret[0] = minus1 ? -1 : 1;
				
				return true;
			} else if (sComp) { // Return string comparison
			
				if (minus1) {
					ret[0] = '-' - c2;
				} else {
					ret[0] = c1 - '-';
				}
				
				return true;
			}
		} else {
			ret[2] = minus1 ? -1 : 1;
		}
		
		// PROCESS WHOLE PART
		while (Character.isDigit(c1)) {
			if (Character.isDigit(c2)) {
				numFound = true;
				
				if (c1 != c2) {
					if (sComp) {
						ret[1] = c1 - c2; // "100" vs. "12"
						sComp = false;
					}
					
					if (nComp == 0) {
						nComp = c1 - c2;
					}
				}
			} else {
				if (numFound) {
					ret[0] = 1; // "1000" vs. "100a"
				} else {
					ret[0] = c1 - c2; // "a12" vs. "ax";
				}
				
				if (minus1 && numFound) {
					ret[0] = -ret[0];
				}
				
				return ret[0] != 0;
			}
			
			if (idx[0] >= len[0]) {
				if (idx[1] == len[1] || !Character.isDigit(s2.charAt(idx[1]))) {
					ret[0] = nComp;
				} else {
					ret[0] = idx[1] - len[1];
				}
				
				if (minus1 && numFound) {
					ret[0] = -ret[0];
				}
				
				return ret[0] != 0;
			} else if (idx[1] == len[1]) {
				if (!Character.isDigit(s1.charAt(idx[0]))) {
					ret[0] = nComp;
				} else {
					ret[0] = len[0] - idx[0];
				}
				
				if (minus1 && numFound) {
					ret[0] = -ret[0];
				}
				
				return ret[0] != 0;
			}
			
			c1 = s1.charAt(idx[0]++);
			c2 = s2.charAt(idx[1]++);
		}
		
		if (Character.isDigit(c2)) {
			if (numFound) {
				ret[0] = -1; // "100a" vs. "1000"
			} else {
				ret[0] = c1 - c2; // "ax" vs. "a12";
			}
			
			if (minus2 && numFound) {
				ret[0] = -ret[0];
			}
			
			return ret[0] != 0;
			/*
			 * ret[0] = minus1 ? 1 : -1; // "100" vs. "1000" return true;
			 */
		}
		
		if (nComp != 0) {
			ret[0] = minus1 ? -nComp : nComp;
			
			return true;
		}
		
		// PROCESS DECIMAL SEPARATOR
		if (isDecSeparator(c1)) {
			if (isDecSeparator(c2)) {
				if (sComp && c1 != c2) { // "10,2" vs. "10.2"
					ret[1] = c1 - c2;
					sComp = false;
				}
				
				if (idx[0] == len[0] || idx[1] == len[1]) {
					return false;
				}
				
				c1 = s1.charAt(idx[0]++);
				c2 = s2.charAt(idx[1]++);
				decFound = true;
			} else { // "10.0000ab" vs. "10ab"
			
				if (sComp) {
					ret[1] = c1 - c2; // '.' - 'a';
					sComp = false;
				}
				
				if (idx[0] == len[0]) {
					return false;
				}
				
				c1 = s1.charAt(idx[0]++);
				
				while (c1 == '0') {
					if (idx[0] == len[0]) {
						return false;
					}
					
					c1 = s1.charAt(idx[0]++);
				}
			}
		} else if (isDecSeparator(c2)) { // "10ab" vs "10.0ab"
		
			if (sComp) {
				ret[1] = c1 - c2; // 'a' - '.';
				sComp = false;
			}
			
			if (idx[1] == len[1]) {
				return false;
			}
			
			c2 = s2.charAt(idx[1]++);
			
			while (c2 == '0') {
				if (idx[1] == len[1]) {
					return false;
				}
				
				c2 = s2.charAt(idx[1]++);
			}
			
			ret[0] = minus1 ? 1 : -1;
			
			return true;
		}
		
		// PROCESS FRACTIONAL PART
		if (decFound) { // "123.1234" vs "123.1235"
		
			while (true) {
				if (Character.isDigit(c1)) {
					if (Character.isDigit(c2)) {
						numFound = true;
						
						if (c1 != c2) {
							ret[0] = c1 - c2;
							
							if (minus1) {
								ret[0] = -ret[0];
							}
							
							return true;
						}
					} else {
						if (sComp) {
							ret[1] = c1 - c2;
							sComp = false;
						}
						
						while (c1 == '0') { // "10.2000" vs. "10.2"
						
							if (idx[0] == len[0]) {
								return false;
							}
							
							c1 = s1.charAt(idx[0]++);
						}
						
						if (Character.isDigit(c1)) {
							ret[0] = 1; // "10.20001" vs. "10.2"
						}
						
						if (minus1) {
							ret[0] = -ret[0];
						}
						
						return ret[0] != 0;
					}
				} else if (Character.isDigit(c2)) {
					if (sComp) {
						ret[1] = c1 - c2;
						sComp = false;
					}
					
					while (c2 == '0') { // "10.2" vs. "10.2000"
					
						if (idx[1] == len[1]) {
							return false;
						}
						
						c2 = s2.charAt(idx[1]++);
					}
					
					if (Character.isDigit(c2)) {
						ret[0] = -1; // "10.2" vs. "10.20001"
					}
					
					if (minus1) {
						ret[0] = -ret[0];
					}
					
					return ret[0] != 0;
				} else if (!numFound || nComp == 0) {
					return false;
				}
				
				if (idx[0] == len[0] || idx[1] == len[1]) {
					return false;
				}
				
				c1 = s1.charAt(idx[0]++);
				c2 = s2.charAt(idx[1]++);
			}
		}
		
		// PROCESS TRAILING SEPARATORS
		if (numFound) {
			// SKIP SEPARATORS OF 1
			while (isSeparator(c1)) {
				if (sComp) {
					if (c1 != c2) {
						ret[1] = c1 - c2;
						sComp = false;
					} else {
						if (idx[1] == len[1]) {
							return false;
						}
						
						c2 = s2.charAt(idx[1]++);
					}
				}
				
				if (idx[0] == len[0]) {
					return false;
				}
				
				c1 = s1.charAt(idx[0]++);
			}
			
			// SKIP (remaining) SEPARATORS OF 2
			while (isSeparator(c2)) {
				if (sComp) {
					if (c1 != c2) {
						ret[1] = c1 - c2;
						sComp = false;
					} else {
						if (idx[0] == len[0]) {
							return false;
						}
						
						c1 = s1.charAt(idx[0]++);
					}
				}
				
				c2 = s2.charAt(idx[1]++);
			}
		} else {
			if (ret[1] != 0) {
				ret[0] = ret[1];
				
				if (minus1) {
					ret[0] = -ret[0];
				}
				
				return true;
			}
		}
		
		if (!numFound) {
			if (c1 != c2) {
				ret[0] = c1 - c2;
				
				if (minus1) {
					ret[0] = -ret[0];
				}
				
				return true;
			}
		} else {
			idx[0]--;
			idx[1]--;
		}
		
		return false;
	}
	
	/**
	 * When <code>softEquals</code> property is set to true, the comparator will return 0 even when two strings are not identical, but are
	 * equal numerically. e.g. compare("100","0100") will return 0;
	 * 
	 * @return Returns the softEquals.
	 */
	public boolean isSoftEquals() {
		return softEquals;
	}
	
	/**
	 * Sets the <code>softEquals</code> property.
	 * 
	 * @param softEquals The softEquals to set.
	 * @see #isSoftEquals()
	 */
	public void setSoftEquals(final boolean softEquals) {
		this.softEquals = softEquals;
	}
}

/* __oOo__ */
