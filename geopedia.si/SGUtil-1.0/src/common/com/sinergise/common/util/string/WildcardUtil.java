package com.sinergise.common.util.string;

import static com.sinergise.common.util.string.WildcardUtil.WildcardStringToken.TYPE_WCARD_CHAR;
import static com.sinergise.common.util.string.WildcardUtil.WildcardStringToken.TYPE_WCARD_STRING;

import java.util.ArrayList;
import java.util.List;

import com.sinergise.common.util.Util;

public class WildcardUtil {
	public static final Wildcards WCARD_ANSI_LIKE           = new Wildcards('%', '_', null);
	public static final Wildcards WCARD_ANSI_LIKE_ESC_AT    = WCARD_ANSI_LIKE.createForEscape(Character.valueOf('@'));
	public static final Wildcards WCARD_WINDOWS             = new Wildcards('*', '?', null);
	public static final Wildcards WCARD_WINDOWS_ESC_BKSLASH = WCARD_WINDOWS.createForEscape(Character.valueOf('\\'));
	public static final Wildcards WCARD_ORA_CATSEARCH       = new Wildcards('*', '*', Character.valueOf('\\'));
	
	public static class WildcardStringToken {
		public static final byte TYPE_NORMAL       = 0;
		public static final byte TYPE_WCARD_STRING = 1;
		public static final byte TYPE_WCARD_CHAR   = 2;
		
		public final String      value;
		public final byte        type;
		
		public WildcardStringToken(final String value) {
			this.value = value;
			this.type = TYPE_NORMAL;
		}
		
		public WildcardStringToken(final byte specialType) {
			this.value = null;
			this.type = specialType;
		}
		
		public byte getType() {
			return type;
		}
	}
	
	public static class Wildcards {
		public final char      wString;
		public final char      wChar;
		public final Character wEscape;
		
		public Wildcards(final char wString, final char wChar, final Character escape) {
			this.wString = wString;
			this.wChar = wChar;
			this.wEscape = escape;
		}
		
		public Wildcards createForEscape(final Character escape) {
			if (Util.safeEquals(wEscape, escape)) {
				return this;
			}
			return new Wildcards(wString, wChar, escape);
		}
		
		public List<WildcardStringToken> split(final CharSequence str) {
			final ArrayList<WildcardStringToken> ret = new ArrayList<WildcardStringToken>();
			StringBuilder curStr = null;
			int idx = 0;
			while (idx < str.length()) {
				char curCh = str.charAt(idx++);
				if (wEscape != null && wEscape.charValue() == curCh) {
					curCh = str.charAt(idx++);
				} else if ((curCh == wChar) || (curCh == wString)) {
					if (curStr != null) {
						ret.add(new WildcardStringToken(curStr.toString()));
						curStr = null;
					}
					ret.add(new WildcardStringToken((curCh == wChar) ? TYPE_WCARD_CHAR : TYPE_WCARD_STRING));
					continue;
				}
				if (curStr == null) {
					curStr = new StringBuilder();
				}
				curStr.append(curCh);
			}
			if (curStr != null) {
				ret.add(new WildcardStringToken(curStr.toString()));
				curStr = null;
			}
			return ret;
		}
		
		public String concatenate(final Iterable<WildcardStringToken> split) {
			final StringBuilder bld = new StringBuilder();
			for (final WildcardStringToken s : split) {
				if (s.type == TYPE_WCARD_CHAR) {
					bld.append(wChar);
				} else if (s.type == TYPE_WCARD_STRING) {
					bld.append(wString);
				} else {
					bld.append(escape(s.value));
				}
			}
			return bld.toString();
		}
		
		public String escape(final String value) {
			if (wEscape == null || (value.indexOf(wEscape.charValue()) < 0 && value.indexOf(wString) < 0 && value.indexOf(wChar) < 0)) {
				return value;
			}
			final StringBuilder ret = new StringBuilder();
			for (int i = 0; i < value.length(); i++) {
				final char ch = value.charAt(i);
				if (ch == wEscape.charValue() || ch == wChar || ch == wString) {
					ret.append(wEscape);
				}
				ret.append(ch);
			}
			return ret.toString();
		}
		
		public boolean stringContainsWildcards(final String str) {
			int idx = 0;
			while (idx < str.length()) {
				char curCh = str.charAt(idx++);
				if (wEscape != null && wEscape.charValue() == curCh) {
					curCh = str.charAt(idx++);
				} else if ((curCh == wChar) || (curCh == wString)) {
					return true;
				}
			}
			return false;
		}
		
		public boolean stringContainsEscape(final String str) {
			if (wEscape == null || str == null) {
				return false;
			}
			return str.indexOf(wEscape.charValue()) >= 0;
		}
	}
	
	public static String replaceWildcards(final String source, final Wildcards srcW, final Wildcards tgtW) {
		if (source == null) return null;
		if (srcW.equals(tgtW) || source.length() == 0) return source;
		return tgtW.concatenate(srcW.split(source));
	}
	
	public static void main(final String[] args) {
		final String srcStr = "J_O%HN* DO?";
		final String likeStr = replaceWildcards(srcStr, WCARD_WINDOWS, WCARD_ANSI_LIKE.createForEscape(Character.valueOf('\\')));
		final String catStr = replaceWildcards(srcStr, WCARD_WINDOWS, WCARD_ORA_CATSEARCH);
		System.out.println(srcStr);
		System.out.println(likeStr);
		System.out.println(catStr);
	}
}
