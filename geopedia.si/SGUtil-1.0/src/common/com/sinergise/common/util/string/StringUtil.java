package com.sinergise.common.util.string;

import static com.sinergise.common.util.ConversionUtil.toBigDecimal;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import com.sinergise.common.util.Util;


/**
 * @author Miha
 */
public class StringUtil {

	protected StringUtil() {
		super();
	}

	private static final char[] HEX_CHAR_TABLE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private static final char[] WHITESPACE     = {' ', '\n', '\t' };
	private static final char[] DIGITS         = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	public static String getHexString(byte[] raw) {
		final StringBuilder sb = new StringBuilder(2 * raw.length);
		for (byte b : raw) {
			sb.append(HEX_CHAR_TABLE[(b & 0xF0) >>> 4]);
			sb.append(HEX_CHAR_TABLE[b & 0x0F]);
		}
		return sb.toString();
	}
	
	/**
	 * @return true if at least one occurence of replaceWhat was found
	 */
	public static boolean replace(final StringBuffer buf, final String replaceWhat, final String replaceTo) {
		int idxFind = buf.indexOf(replaceWhat, 0);
		final int findLen = replaceWhat.length();
		final int repLen = replaceTo.length();
		boolean found = false;
		while (idxFind >= 0) {
			found = true;
			buf.replace(idxFind, idxFind + findLen, replaceTo);
			idxFind = idxFind + repLen;
			idxFind = buf.indexOf(replaceWhat, idxFind);
		}
		return found;
	}
	
	public static boolean endsWith(CharSequence seq, CharSequence suffix) {
		int searchLen = suffix.length();
		int seqLen = seq.length();
		if (seqLen < searchLen) {
			return false;
		}
		for (int i = 1; i <= searchLen; i++) {
			if (seq.charAt(seqLen-i) != suffix.charAt(searchLen-i)) {
				return false;
			}
		}
		return true;
	}

	public static boolean endsWithIgnoreCase(CharSequence seq, CharSequence suffix) {
		int searchLen = suffix.length();
		int seqLen = seq.length();
		if (seqLen < searchLen) {
			return false;
		}
		for (int i = 1; i <= searchLen; i++) {
			if (!equalsIgnoreCase(seq.charAt(seqLen-i), suffix.charAt(searchLen-i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean equalsIgnoreCase(char a, char b) {
		return Character.toLowerCase(a) == Character.toLowerCase(b);
	}
	
	public static void prepend(final StringBuffer buf, final String beforeWhat, final String toPrepend) {
		prepend(buf, beforeWhat, toPrepend, beforeWhat.length());
	}

	public static void prepend(final StringBuffer buf, final String beforeWhat, final String toPrepend, final int offset) {
		int idxFind = buf.indexOf(beforeWhat, 0);
		final int prepLen = toPrepend.length();
		while (idxFind >= 0) {
			buf.insert(idxFind, toPrepend);
			idxFind = idxFind + prepLen + offset;
			idxFind = buf.indexOf(beforeWhat, idxFind);
		}
	}

	public static void replace(final StringBuffer buf, final char replaceWhat, final char replaceTo) {
		for (int i = buf.length() - 1; i >= 0; i--) {
			if (buf.charAt(i) == replaceWhat) {
				buf.setCharAt(i, replaceTo);
			}
		}
	}

	public static void prepend(final StringBuffer buf, final char beforeWhat, final char toPrepend) {
		for (int i = buf.length() - 1; i >= 0; i--) {
			if (buf.charAt(i) == beforeWhat) {
				buf.insert(i, toPrepend);
			}
		}
	}

	public static void replace(final StringBuffer buf, final char replaceWhat, final String replaceTo) {
		for (int i = buf.length() - 1; i >= 0; i--) {
			if (buf.charAt(i) == replaceWhat) {
				buf.replace(i, i + 1, replaceTo);
			}
		}
	}

	public static void prepend(final StringBuffer buf, final char beforeWhat, final String toPrepend) {
		for (int i = buf.length() - 1; i >= 0; i--) {
			if (buf.charAt(i) == beforeWhat) {
				buf.insert(i, toPrepend);
			}
		}
	}

	public static void replace(final StringBuffer buf, final String replaceWhat, final char replaceTo) {
		int idxFind = buf.indexOf(replaceWhat, 0);
		final int findLen = replaceWhat.length();
		while (idxFind >= 0) {
			buf.delete(idxFind + 1, idxFind + findLen);
			buf.setCharAt(idxFind, replaceTo);
			idxFind = idxFind + 1;
			idxFind = buf.indexOf(replaceWhat, idxFind);
		}
	}

	public static void prepend(final StringBuffer buf, final String beforeWhat, final char toPrepend) {
		prepend(buf, beforeWhat, toPrepend, beforeWhat.length());
	}

	public static void prepend(final StringBuffer buf, final String beforeWhat, final char toPrepend, final int offset) {
		int idxFind = buf.indexOf(beforeWhat, 0);
		while (idxFind >= 0) {
			buf.insert(idxFind, toPrepend);
			idxFind = idxFind + 1 + offset;
			idxFind = buf.indexOf(beforeWhat, idxFind);
		}
	}

	public static int indexOf(final StringBuffer buf, final char toFind) {
		return indexOf(buf, toFind, 0);
	}

	/**
	 * @param buf
	 * @param toFind
	 * @param startIndex inclusive
	 * @return
	 */
	public static int indexOf(final StringBuffer buf, final char toFind, final int startIndex) {
		final int len = buf.length();
		for (int i = startIndex; i < len; i++) {
			if (buf.charAt(i) == toFind) { return i; }
		}
		return -1;
	}

	/**
	 * Tries to find first occurrence of any character in toFind
	 * @param buf
	 * @param toFind
	 * @param startIndex
	 * @return
	 */
	public static int indexOf(final StringBuffer buf, final char [] toFind, final int startIndex) {
		final int len = buf.length();
		if (startIndex >= len)
			return -1;
		
		for (int i=startIndex;i<len;i++) {
			for (int j=0;j<toFind.length;j++) 
				if (buf.charAt(i)==toFind[j]) 
					return i;
		}
		return -1;
	}
	
	
	public static void deleteAll(final StringBuffer buf, final char toDelete) {
		for (int i = buf.length() - 1; i >= 0; i--) {
			if (buf.charAt(i) == toDelete) {
				buf.deleteCharAt(i);
			}
		}
	}

	public static void deleteAll(final StringBuffer buf, final String toDelete) {
		int idxFind = -1;
		final int findLen = 0;
		do {
			if (idxFind >= 0) {
				buf.delete(idxFind, idxFind + findLen);
			}
			idxFind = buf.indexOf(toDelete, idxFind);
		} while (idxFind >= 0);
	}

	public static void main(final String[] args) {
		System.out.println(compare(null, "1", true));
		System.out.println(compare("0", "1", true));
		System.out.println(compare(null, "1", false));
		System.out.println(compare("2", "1", false));
		System.out.println(compareAsNumber("3", "2"));
	}

	public static final String padWith(final String what, final char padCh, final int len, final boolean before) {
		if (what.length() >= len) { return what; }
		StringBuilder sb = new StringBuilder(len);
		try {
			padAndAppend(what, padCh, len, before, sb);
		} catch(IOException e) {//can't happen
		}
		return sb.toString();
	}

	public static void padAndAppend(String what, char padCh, int len, boolean before, Appendable out) throws IOException {
		final int delta = len - what.length();
		if (delta <= 0) {
			out.append(what);
			return;
		}
		if (!before) out.append(what);
		for (int i = 0; i < delta; i++)
			out.append(padCh);
		if (before) out.append(what);
	}

	public static final String padCentered(final String what, final char padCh, final int len) {
		if (what.length() >= len) { return what; }
		StringBuilder sb = new StringBuilder(len);
		final int before = (len - what.length()) / 2;
		final int after = len - what.length() - before;
		for (int i = 0; i < before; i++)
			sb.append(padCh);
		sb.append(what);
		for (int i = 0; i < after; i++)
			sb.append(padCh);
		return sb.toString();
	}
	
	public static String clip(String str, int maxLen) {
		if (str.length() > maxLen) {
			str = str.substring(0, maxLen);
		}
		return str;
	}

	public static String join(final String separator, final int... vals) {
		if (vals == null || vals.length == 0) { return ""; }
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vals.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(vals[i]);
		}
		return sb.toString();
	}
	
	public static String joinCollection(final String separator, final Collection<? extends Object> vals) {
		return join(separator, vals);
	}

	public static String join(final String separator, final Collection<? extends Object> vals) {
		if (vals == null || vals.size() == 0) { return ""; }
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (final Object val : vals) {
			if (i++ > 0) {
				sb.append(separator);
			}
			sb.append(val);
		}
		return sb.toString();
	}

	public static String join(final String separator, final Object... vals) {
		if (vals == null || vals.length == 0) { return ""; }
		final StringBuilder sb = new StringBuilder(vals.length * vals[0].toString().length());
		for (int i = 0; i < vals.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(vals[i]);
		}
		return sb.toString();
	}

	/**
	 * @param s
	 * @return Trimmed string or <code>null</code> iff s==null or s.trim().length()==0
	 */
	public static String trimNullEmpty(String s) {
		if (s == null || s.length() < 1) return null;
		s = s.trim();
		if (s.length() < 1) return null;
		return s;
	}
	//TODO: describe functionality; add unit tests; rewrite for performance
	public static String trimAndCollapseDoubleWhitespace(final String s) {
		
		if(s != null){
		
			final StringBuffer sb         = new StringBuffer(s);
			
			for (int i = 0; i < WHITESPACE.length; i++) {
				for (int j = 0; j < WHITESPACE.length; j++) {
					while (replace(sb, String.valueOf(WHITESPACE[i]) + String.valueOf(WHITESPACE[j]), " ")) {
						// do nothing
					}
				}
			}
			return sb.toString().trim();
			
		}

		return null;
		
	}
	
	public static String emptyIfNull(String s) {
		if (s == null) {
			return "";
		}
		return s;
	}

	public static int indexOfIgnoreCase(final String haystack, final String needle) {
		return indexOfIgnoreCase(haystack, needle, 0);
	}

	public static int indexOfIgnoreCase(final String haystack, final String needle, int startpos) {
		final int haylen = haystack.length();
		final int neelen = needle.length();
		final int lastpos = haylen - neelen;

		if (startpos > lastpos) { return -1; }

		if (neelen == haylen) { return haystack.equalsIgnoreCase(needle) ? 0 : -1; }

		final char n0 = needle.charAt(0);
		final char n1 = Character.toLowerCase(n0);
		final char n2 = Character.toUpperCase(n1);

		while (startpos <= lastpos) {
			char c = haystack.charAt(startpos);
			if (c == n0) {
				if (haystack.regionMatches(true, startpos, needle, 0, neelen)) { return startpos; }
			} else {
				c = Character.toLowerCase(c);
				if (c == n1) {
					if (haystack.regionMatches(true, startpos, needle, 0, neelen)) { return startpos; }
				} else {
					c = Character.toUpperCase(c);
					if (c == n2) {
						if (haystack.regionMatches(true, startpos, needle, 0, neelen)) { return startpos; }
					}
				}
			}

			startpos++;
		}
		return -1;
	}

	public static String toString(final Object value, final String ifNull) {
		if (value == null) return ifNull;
		return toString(value);
	}

	/**
	 * @param value
	 * @return arrayToString for arrays, value.toString() or null if value is null
	 */
	public static String toString(final Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Object[]) {
			return arrayToString((Object[])value, ",");
		}
		if (value instanceof int[]) {
			return arrayToString((int[])value, ",");
		}
		if (value instanceof double[]) {
			return arrayToString((double[])value, ",");
		}
		if (value instanceof float[]) {
			return arrayToString((float[])value, ",");
		}
		return value.toString();
	}

	
	/**
	 * Second parameter is checked, first parameter is prepended.
	 * 
	 * @param toPrepend
	 * @param value
	 * @return
	 */
	public static String prependIfValNotEmpty(final String toPrepend, final String value) {
		return prependIfValNotEmpty(toPrepend, value, "");
	}

	public static String prependIfValNotEmpty(final String toPrepend, final String value, final String ifEmpty) {
		if (value == null || value.length() == 0) { return ifEmpty; }
		if (toPrepend == null) { return value; }
		return toPrepend + value;
	}

	public static String appendIfNotEmpty(final String firstVal, final String toAppend) {
		return appendIfNotEmpty(firstVal, toAppend, "");
	}

	public static String appendIfNotEmpty(final String firstVal, final String toAppend, final String ifEmpty) {
		if (firstVal == null || firstVal.length() == 0) { return ifEmpty; }
		if (toAppend == null) { return firstVal; }
		return firstVal + toAppend;
	}
	
	
	public static StringBuilder appendPadded(StringBuilder out, int toAppend, int len, char padChar, boolean left) {
		return appendPadded(out, String.valueOf(toAppend), len, padChar, left);
	}

	public static StringBuilder appendPadded(StringBuilder out, String toAppend, int len, char padChar, boolean left) {
		len -= toAppend.length();
		if (left) while (--len >= 0) out.append(padChar);
		out.append(toAppend);
		if (!left) while (--len >= 0) out.append(padChar);
		return out;
	}
	
	public static boolean isNotDigitsOnly(String s){
		return !isDigitsOnly(s);
	}
	
	public static boolean isDigitsOnly(String s){
		if(s == null || s.length() == 0){
			return false;
		}
		
		for(int i = 0; i < s.length(); i++){
			if(!Character.isDigit(s.charAt(i))){
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean contains(String str, char ch) {
		return str.indexOf(ch) >= 0;
	}


	public static boolean isLetterOrDigitOrUnderscore(final char ch) {
		return (ch == '_' || Character.isLetterOrDigit(ch));
	}

	public static boolean isLetterOrUnderscore(final char ch) {
		return (ch == '_' || Character.isLetter(ch));
	}

	public static Character firstChar(final String str) {
		if (str == null || str.length() < 1) return null;
		return Character.valueOf(str.charAt(0));
	}

	public static String toTitleCase(String s) {
		if (s == null || s.length() == 0) return s;

		s = s.toLowerCase();

		boolean spaceBefore = true;

		final StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			final char curCh = s.charAt(i);

			if (spaceBefore) sb.append(Character.toUpperCase(curCh));
			else sb.append(curCh);

			if (isSpace(curCh)) spaceBefore = true;
			else spaceBefore = false;
		}
		return sb.toString();
	}

	public static boolean isSpace(final char ch) {
		switch (ch) {
			case ' ':
			case '\t':
			case '\n':
			case '\f':
			case '\r':
				return true;
			default:
				return false;
		}
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	

	public static int compare(String s1, String s2, boolean nullFirst) {
		if (s1 == null) return s2 == null ? 0 : nullFirst ? -1 : 1;
		if (s2 == null) return nullFirst ? 1 : -1;
		return s1.compareTo(s2);
	}

	public static int compare(String s1, String s2) {
		return compare(s1, s2, true);
	}
	
	public static int compareCaseInsensitive(String s1, String s2) {
		return compare(s1 != null ? s1.toUpperCase() : null, s2 != null ? s2.toUpperCase() : null);
	}
	
	public static boolean equalCaseInsensitive(String s1, String s2){
		return compareCaseInsensitive(s1, s2) == 0;
	}
	
	public static String collectionToString(Iterable<?> collection, String separator) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object obj:collection) {
			if (!first) {
				sb.append(separator);
			}
			first=false;
			sb.append(obj);
		}

		return sb.toString();

	}
	
	public static String arrayToString(int[] arr, String separator) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(arr[0]));
		for (int i = 1; i < arr.length; i++) {
			sb.append(separator);
			sb.append(String.valueOf(arr[i]));
		}

		return sb.toString();
	}
	
	public static String arrayToString(double[] arr, String separator) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(arr[0]));
		for (int i = 1; i < arr.length; i++) {
			sb.append(separator);
			sb.append(String.valueOf(arr[i]));
		}

		return sb.toString();
	}
	
	public static String arrayToString(float[] arr, String separator) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(arr[0]));
		for (int i = 1; i < arr.length; i++) {
			sb.append(separator);
			sb.append(String.valueOf(arr[i]));
		}

		return sb.toString();
	}

	public static String arrayToString(Object[] arr, String separator) {
		return arrayToString(arr, separator, "", "");
	}
	public static String arrayToString(Object[] arr, String separator, String prolog, String epilog) {
		StringBuilder sb = new StringBuilder(prolog);
		int len = arr.length;
		if (len > 0) {
			sb.append(toString(arr[0]));
			for (int i = 1; i < arr.length; i++) {
				sb.append(separator);
				sb.append(toString(arr[i]));
			}
		}
		sb.append(epilog);
		return sb.toString();
	}

	/**
	 * empty comes first; non-number comes last; if both are not number, 
	 * @param pv1
	 * @param pv2
	 * @return
	 */
	public static int compareAsNumber(String pv1, String pv2) {
		pv1 = trimNullEmpty(pv1);
		pv2 = trimNullEmpty(pv2);
		if (pv1 == null) return pv2==null ? 0 : -1;
		if (pv2 == null) return 1;
		
		BigDecimal bd1 = toBigDecimal(pv1);
		BigDecimal bd2 = toBigDecimal(pv2);
		if (bd1 == null) return bd2 == null ? pv1.compareTo(pv2) : 1;
		if (bd2 == null) return -1;
		return bd1.compareTo(bd2);
	}

	public static Integer toInteger(String s) {
		return s == null ? null : Integer.valueOf(s);
	}
	
	public static int toInt(String s) {
		return Integer.parseInt(s);
	}

	public static int toInt(String s, int ifEmpty) {
		if (StringUtil.isNullOrEmpty(s)) return ifEmpty;
		return Integer.parseInt(s);
	}
	
	public static Integer tryToInteger(String s) {
		try {
			return toInteger(s);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
	
	public static Long tryToLong(String s) {
		try {
			return toLong(s);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
	
	public static int tryToInt(String s, int ifFailed) {
		try {
			return toInt(s, ifFailed);
		} catch (NumberFormatException ignored) {
			return ifFailed;
		}
	}

	public static Float toFloatObj(String s) {
		return s == null ? null : Float.valueOf(s);
	}
	
	public static float toFloat(String s) {
		return Float.parseFloat(s);
	}

	public static float toFloat(String s, float ifEmpty) {
		if (StringUtil.isNullOrEmpty(s)) return ifEmpty;
		return Float.parseFloat(s);
	}
	
	public static Float tryToFloat(String s) {
		try {
			return toFloatObj(s);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
	
	public static float tryToFloat(String s, float ifFailed) {
		try {
			return toFloat(s, ifFailed);
		} catch (NumberFormatException ignored) {
			return ifFailed;
		}
	}

	public static Double toDoubleObj(String s) {
		return s == null ? null : Double.valueOf(s);
	}
	
	public static double toDouble(String s) {
		return Double.parseDouble(s);
	}

	public static double toDouble(String s, double ifEmpty) {
		if (StringUtil.isNullOrEmpty(s)) return ifEmpty;
		return Double.parseDouble(s);
	}
	
	public static Double tryToDouble(String s) {
		try {
			return toDoubleObj(s);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
	
	public static double tryToDouble(String s, double ifFailed) {
		try {
			return toDouble(s, ifFailed);
		} catch (NumberFormatException ignored) {
			return ifFailed;
		}
	}

	public static Character toCharacter(String str) {
		return isNullOrEmpty(str) ? null : Character.valueOf(str.charAt(0));
	}

	public static boolean isTruthy(String strVal, boolean ifUnclear) {
		Boolean ret = truthyFalsyToBoolean(strVal);
		return Util.isTrue(ret, ifUnclear);
	}

	public static Boolean truthyFalsyToBoolean(String strVal) {
		if ((strVal = trimNullEmpty(strVal)) == null) {
			return null;
		}
		if ("true".equalsIgnoreCase(strVal) || "yes".equalsIgnoreCase(strVal)) {
			return Boolean.TRUE;
		}
		if ("false".equalsIgnoreCase(strVal) || "no".equalsIgnoreCase(strVal)) {
			return Boolean.FALSE;
		}
		if (strVal.length() > 1) {
			return null;
		}
		char c0 = strVal.charAt(0);
		if (c0=='y' || c0=='Y' || c0=='1') {
			return Boolean.TRUE;
		}
		if (c0=='n' || c0=='N' || c0=='0') {
			return Boolean.FALSE;
		}
		return null;
	}
	
	public static boolean isFalsy(String strVal, boolean ifUnclear) {
		return !isTruthy(strVal, !ifUnclear);
	}

	public static Boolean toBoolean(String value) {
		return value == null ? null : Boolean.valueOf(value);
	}

	public static Long toLong(String value) {
		return value == null ? null : Long.valueOf(value);
	}

	public static String times(String string, int count) {
		StringBuilder sb = new StringBuilder(string);
		for (int i = 1; i < count; i++) {
			sb.append(string);
		}
		return sb.toString();
	}

	public static String[] split(CharSequence value, CharSequence separator) {
		return split(value, separator, 0);
	}
	
	public static String[] split(CharSequence value, CharSequence separator, int limit) {
		if (separator.length() == 1) {
			return split(value, separator.charAt(0), limit);
		}
		if (limit == 1) {
			return new String[] {value.toString()};
		}
		ArrayList<String> ret = new ArrayList<String>();
		StringBuilder cur = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			cur.append(value.charAt(i));
			if ((ret.size() < limit-1 || limit == 0) && endsWith(cur, separator)) {
				ret.add(cur.substring(0, cur.length()-separator.length()));
				cur.setLength(0);
			}
		}
		ret.add(cur.toString());		
		return ret.toArray(new String[ret.size()]);
	}

	public static String[] split(CharSequence value, char separator) {
		return split(value, separator, 0);
	}
	
	public static String[] split(CharSequence value, char separator, int limit) {
		if (limit == 1) {
			return new String[] {value.toString()};
		}
		ArrayList<String> ret = new ArrayList<String>();
		StringBuilder cur = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (ch == separator && (ret.size() < limit-1 || limit == 0) ) {
				ret.add(cur.toString());
				cur.setLength(0);
			} else {
				cur.append(ch);
			}
		}
		ret.add(cur.toString());		
		return ret.toArray(new String[ret.size()]);
	}

	public static boolean isNumber(String str) {
		for (int i = 0; i < str.length(); i++) {
			boolean found = false;
			for (int j = 0; j < DIGITS.length; j++) {
				if (str.charAt(i) == DIGITS[j]) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}
	
	public static boolean isIntegerNumber(String str) {		
		return isNumber(str.substring(str.charAt(0) == '-' ? 1 : 0));		
	}
	
	public static double parseDoubleWithSeparator(String parsedString, String decimalSeparator) {
		parsedString = parsedString.replace(decimalSeparator, ".");
		return Double.parseDouble(parsedString);
	}
	
	public static String safeIntern(String s) {
		return safeIntern(s, Integer.MAX_VALUE);
	}
	
	public static String safeIntern(String s, int maxLen) {
		if (s != null && s.length() < maxLen) {
			return s.intern();
		}
		return s;
	}
}
