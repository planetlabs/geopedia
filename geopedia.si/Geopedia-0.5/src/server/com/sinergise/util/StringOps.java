package com.sinergise.util;

public class StringOps
{
	public static int indexOfIgnoreCase(String haystack, String needle)
	{
		return indexOfIgnoreCase(haystack, needle, 0);
	}
	
	public static int indexOfIgnoreCase(String haystack, String needle, int startpos)
	{
		int haylen=haystack.length();
		int neelen=needle.length();
		int lastpos=haylen-neelen;
		
		if (startpos>lastpos)
			return -1;
		
		
		if (neelen==haylen)
			return haystack.equalsIgnoreCase(needle)?0:-1;
		
		char n0=needle.charAt(0);
		char n1=Character.toLowerCase(n0);
		char n2=Character.toUpperCase(n1);
		
		while (startpos<=lastpos) {
			char c=haystack.charAt(startpos);
			if (c==n0) {
				if (haystack.regionMatches(true, startpos, needle, 0, neelen))
					return startpos;
			} else {
				c=Character.toLowerCase(c);
				if (c==n1) {
					if (haystack.regionMatches(true, startpos, needle, 0, neelen))
						return startpos;
				} else {
					c=Character.toUpperCase(c);
					if (c==n2) {
						if (haystack.regionMatches(true, startpos, needle, 0, neelen))
							return startpos;
					}
				}
			}
			
			startpos++;
		}
		return -1;
	}
	
//	public static void main(String[] args)
//	{
//		// Test code
//		
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "sta"));
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "Sta"));
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "STa"));
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "sTa"));
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "sTA"));
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "STA"));
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "StA"));
//		Assert.Equals(18, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "stA"));
//		Assert.Equals(0,  indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "abc"));
//		Assert.Equals(20, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "abc", 1));
//		Assert.Equals(20, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "abc", 20));
//		Assert.Equals(-1, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "abc", 21));
//		Assert.Equals(0,  indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "ABC"));
//		Assert.Equals(20, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "ABC", 1));
//		Assert.Equals(20, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "ABC", 20));
//		Assert.Equals(-1, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "ABC", 21));
//		Assert.Equals(0,  indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "aBc"));
//		Assert.Equals(20, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "aBc", 1));
//		Assert.Equals(20, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "aBc", 20));
//		Assert.Equals(-1, indexOfIgnoreCase("abcdefghijklmnopqrstABCDEFGHIJKLMNOPQRST", "aBc", 21));
//	}
	
	/*public static void intToHexLE(StringBuffer sb, int value)
	{
		for (int a=0; a<4; a++) {
			int v=(value>>>4) & 15;
			if (v<10) {
				sb.append((char)('0'+v));
			} else {
				sb.append((char)(('a'-10)+v));
			}
			v=value & 15;
			if (v<10) {
				sb.append((char)('0'+v));
			} else {
				sb.append((char)(('a'-10)+v));
			}
		
			value>>>=8;
		}
	}
	*/
	public static void intToHexLE(StringBuffer sb, int value)
	{
		for (int a=0; a<4; a++) {
			int v=(value>>>4) & 15;
			if (v<10) {
				sb.append((char)('0'+v));
			} else {
				sb.append((char)(('a'-10)+v));
			}
			v=value & 15;
			if (v<10) {
				sb.append((char)('0'+v));
			} else {
				sb.append((char)(('a'-10)+v));
			}
		
			value>>>=8;
		}
	}
}
