package com.sinergise.java.util.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteSizeParser {
	private static final Pattern sizepat = Pattern.compile("([0-9]+)[ ]*(B|K|KB|M|MB|G|GB|T|TB|P|PB|E|EB)?", Pattern.CASE_INSENSITIVE);
	
	/**
	 * <p>
	 * Parses a string matching the following form:
	 * </p>
	 * <p>
	 * number[ ][unit]
	 * </p>
	 * <p>
	 * Where number is a decimal number and unit is one of:
	 * </p>
	 * <ul>
	 * <li>b: 0.125 bytes (1 bit)
	 * <li>B: 1 byte
	 * <li>kb: 125 bytes (1 kilobit)
	 * <li>Kb: 128 bytes (1 kibibit)
	 * <li>k or kB: 1000 bytes (1 kilobyte)
	 * <li>K or KB: 1024 bytes (1 kibibyte)
	 * <li>mb: 125000 bytes (1 megabit)
	 * <li>Mb: 131072 bytes (1 mebibit)
	 * <li>m or mB: 1000000 bytes (1 megabyte)
	 * <li>M or MB: 1048576 bytes (1 mebibyte)
	 * <li>gb: 125000000 bytes (1 gigabit)
	 * <li>Gb: 134217728 bytes (1 gibibit)
	 * <li>g or gB: 1000000000 bytes (1 gigabyte)
	 * <li>G or GB: 1073741824 bytes (1 gibibyte)
	 * <li>tb: 125000000000 bytes (1 terabit)
	 * <li>Tb: 137438953472 bytes (1 tebibit)
	 * <li>t or tB: 1000000000000 bytes (1 terabyte)
	 * <li>T or TB: 1099511627776 bytes (1 tebibyte)
	 * <li>pb: 125000000000000 bytes (1 petabit)
	 * <li>Pb: 140737488355328 bytes (1 pebibit)
	 * <li>p or pB: 1000000000000000 bytes (1 petabyte)
	 * <li>P or PB: 1125899906842624 bytes (1 pebibyte)
	 * <li>eb: 125000000000000000 bytes (1 exabit)
	 * <li>Eb: 144115188075855872 bytes (1 exbibit)
	 * <li>e or eB: 1000000000000000000 bytes (1 exabyte)
	 * <li>E or EB: 1152921504606846976 bytes (1 exbibyte)
	 * </ul>
	 * <p>
	 * The number returned is the number of bytes the string represents. If the number is in bits, they are rounded down.
	 * </p>
	 * <p>
	 * To prevent using bits instead of bytes, use parseSizeToBytes(). To use the computerized meaning (1024^n), use parseSizeToBytesComp()
	 * and to use the SI meaning of prefixes (1000^n), use parseSizeToBytesSI().
	 * </p>
	 * 
	 * @param size A string representing a size of something in bits or bytes (kb/mb/gb/tb/etc)
	 * @return The number of bytes represented by size, or -1 on error
	 */
	
	public static long parseSizeToBytesProper(final String size) {
		final Matcher m = sizepat.matcher(size);
		if (m.matches()) {
			final long b = Long.parseLong(m.group(1));
			final String g = m.group(2);
			if (g != null) {
				if (g.length() == 1) {
					switch (g.charAt(0)) {
						case 'b':
							return b >> 3;
						case 'B':
							return b;
							
						case 'k':
							return b * 1000L;
						case 'm':
							return b * 1000000L;
						case 'g':
							return b * 1000000000L;
						case 't':
							return b * 1000000000000L;
						case 'p':
							return b * 1000000000000000L;
						case 'e':
							return b * 1000000000000000000L;
							
						case 'K':
							return b * 0x400L;
						case 'M':
							return b * 0x100000L;
						case 'G':
							return b * 0x40000000L;
						case 'T':
							return b * 0x10000000000L;
						case 'P':
							return b * 0x4000000000000L;
						case 'E':
							return b * 0x1000000000000000L;
					}
				} else {
					if (g.charAt(1) == 'B') {
						switch (g.charAt(0)) {
							case 'b':
								return b >> 3;
							case 'B':
								return b;
								
							case 'k':
								return b * 1000L;
							case 'm':
								return b * 1000000L;
							case 'g':
								return b * 1000000000L;
							case 't':
								return b * 1000000000000L;
							case 'p':
								return b * 1000000000000000L;
							case 'e':
								return b * 1000000000000000000L;
								
							case 'K':
								return b * 0x400L;
							case 'M':
								return b * 0x100000L;
							case 'G':
								return b * 0x40000000L;
							case 'T':
								return b * 0x10000000000L;
							case 'P':
								return b * 0x4000000000000L;
							case 'E':
								return b * 0x1000000000000000L;
						}
					} else {
						switch (g.charAt(0)) {
							case 'k':
								return b * 125L;
							case 'm':
								return b * 125000L;
							case 'g':
								return b * 125000000L;
							case 't':
								return b * 125000000000L;
							case 'p':
								return b * 125000000000000L;
							case 'e':
								return b * 125000000000000000L;
								
							case 'K':
								return b * 0x80L;
							case 'M':
								return b * 0x20000L;
							case 'G':
								return b * 0x8000000L;
							case 'T':
								return b * 0x2000000000L;
							case 'P':
								return b * 0x800000000000L;
							case 'E':
								return b * 0x200000000000000L;
						}
					}
				}
			}
			return b;
		}
		return -1;
	}
	
	/**
	 * <p>
	 * Parses a string matching the following form:
	 * </p>
	 * <p>
	 * number[ ][unit]
	 * </p>
	 * <p>
	 * Where number is a decimal number and unit is one of:
	 * </p>
	 * <ul>
	 * <li>B: 1 byte
	 * <li>k or kB: 1000 bytes (1 kilobyte)
	 * <li>K or KB: 1024 bytes (1 kibibyte)
	 * <li>m or mB: 1000000 bytes (1 megabyte)
	 * <li>M or MB: 1048576 bytes (1 mebibyte)
	 * <li>g or gB: 1000000000 bytes (1 gigabyte)
	 * <li>G or GB: 1073741824 bytes (1 gibibyte)
	 * <li>t or tB: 1000000000000 bytes (1 terabyte)
	 * <li>T or TB: 1099511627776 bytes (1 tebibyte)
	 * <li>p or pB: 1000000000000000 bytes (1 petabyte)
	 * <li>P or PB: 1125899906842624 bytes (1 pebibyte)
	 * <li>e or eB: 1000000000000000000 bytes (1 exabyte)
	 * <li>E or EB: 1152921504606846976 bytes (1 exbibyte)
	 * </ul>
	 * <p>
	 * The number returned is the number of bytes the string represents. The 'B' is case-insensitive, while the first character signifies
	 * whether to use the SI-meaning (1000^n) or the computerized meaning (1024^n) of the prefix.
	 * </p>
	 * 
	 * @param size A string representing a size of something in bytes (kb/mb/gb/tb/etc)
	 * @return The number of bytes represented by size, or -1 on error
	 */
	
	public static long parseSizeToBytes(final String size) {
		final Matcher m = sizepat.matcher(size);
		if (m.matches()) {
			final long b = Long.parseLong(m.group(1));
			final String g = m.group(2);
			if (g != null) {
				switch (g.charAt(0)) {
					case 'b':
					case 'B':
						return b;
						
					case 'k':
						return b * 1000L;
					case 'm':
						return b * 1000000L;
					case 'g':
						return b * 1000000000L;
					case 't':
						return b * 1000000000000L;
					case 'p':
						return b * 1000000000000000L;
					case 'e':
						return b * 1000000000000000000L;
						
					case 'K':
						return b * 0x400L;
					case 'M':
						return b * 0x100000L;
					case 'G':
						return b * 0x40000000L;
					case 'T':
						return b * 0x10000000000L;
					case 'P':
						return b * 0x4000000000000L;
					case 'E':
						return b * 0x1000000000000000L;
				}
			}
			return b;
		}
		return -1;
	}
	
	/**
	 * <p>
	 * Parses a string matching the following form:
	 * </p>
	 * <p>
	 * number[ ][unit]
	 * </p>
	 * <p>
	 * Where number is a decimal number and unit is one of:
	 * </p>
	 * <ul>
	 * <li>B: 1 byte
	 * <li>K or KB: 1024 bytes (1 kibibyte)
	 * <li>M or MB: 1048576 bytes (1 mebibyte)
	 * <li>G or GB: 1073741824 bytes (1 gibibyte)
	 * <li>T or TB: 1099511627776 bytes (1 tebibyte)
	 * <li>P or PB: 1125899906842624 bytes (1 pebibyte)
	 * <li>E or EB: 1152921504606846976 bytes (1 exbibyte)
	 * </ul>
	 * <p>
	 * The number returned is the number of bytes the string represents. The unit is case-insensitive.
	 * </p>
	 * 
	 * @param size A string representing a size of something in bytes (kb/mb/gb/tb/etc)
	 * @return The number of bytes represented by size, or -1 on error
	 */
	
	public static long parseSizeToBytesComp(final String size) {
		final Matcher m = sizepat.matcher(size);
		if (m.matches()) {
			final long b = Long.parseLong(m.group(1));
			final String g = m.group(2);
			if (g != null) {
				switch (g.charAt(0)) {
					case 'b':
					case 'B':
						return b;
						
					case 'k':
					case 'K':
						return b * 0x400L;
						
					case 'm':
					case 'M':
						return b * 0x100000L;
						
					case 'g':
					case 'G':
						return b * 0x40000000L;
						
					case 't':
					case 'T':
						return b * 0x10000000000L;
						
					case 'p':
					case 'P':
						return b * 0x4000000000000L;
						
					case 'e':
					case 'E':
						return b * 0x1000000000000000L;
				}
			}
			return b;
		}
		return -1;
	}
	
	/**
	 * <p>
	 * Parses a string matching the following form:
	 * </p>
	 * <p>
	 * number[ ][unit]
	 * </p>
	 * <p>
	 * Where number is a decimal number and unit is one of:
	 * </p>
	 * <ul>
	 * <li>B: 1 byte
	 * <li>K or KB: 1024 bytes (1 kibibyte)
	 * <li>M or MB: 1048576 bytes (1 mebibyte)
	 * <li>G or GB: 1073741824 bytes (1 gibibyte)
	 * <li>T or TB: 1099511627776 bytes (1 tebibyte)
	 * <li>P or PB: 1125899906842624 bytes (1 pebibyte)
	 * <li>E or EB: 1152921504606846976 bytes (1 exbibyte)
	 * </ul>
	 * <p>
	 * The number returned is the number of bytes the string represents. The unit is case-insensitive.
	 * </p>
	 * 
	 * @param size A string representing a size of something in bytes (kb/mb/gb/tb/etc)
	 * @return The number of bytes represented by size, or -1 on error
	 */
	
	public static long parseSizeToBytesSI(final String size) {
		final Matcher m = sizepat.matcher(size);
		if (m.matches()) {
			final long b = Long.parseLong(m.group(1));
			final String g = m.group(2);
			if (g != null) {
				switch (g.charAt(0)) {
					case 'b':
					case 'B':
						return b;
						
					case 'k':
					case 'K':
						return b * 1000L;
						
					case 'm':
					case 'M':
						return b * 1000000L;
						
					case 'g':
					case 'G':
						return b * 1000000000L;
						
					case 't':
					case 'T':
						return b * 1000000000000L;
						
					case 'p':
					case 'P':
						return b * 1000000000000000L;
						
					case 'e':
					case 'E':
						return b * 1000000000000000000L;
				}
			}
			return b;
		}
		return -1;
	}
	
	public static String encodeSizeApprox(long sizeInBytes) {
		int n = 0;
		while (sizeInBytes >= 9999L) {
			n++;
			sizeInBytes /= 1024L;
		}
		
		switch (n) {
			case 0:
				return sizeInBytes + "B";
			case 1:
				return sizeInBytes + "KB";
			case 2:
				return sizeInBytes + "MB";
			case 3:
				return sizeInBytes + "GB";
			case 4:
				return sizeInBytes + "TB";
			case 5:
				return sizeInBytes + "PB";
			case 6:
				return sizeInBytes + "EB";
		}
		return String.valueOf(sizeInBytes);
	}
	
	public static void main(final String[] args) {
		final String[] test = new String[]{"10k", "10K", "10kb", "10kB", "10Kb", "10KB"};
		
		for (final String element : test) {
			System.out.println(element + " ==Proper==> " + parseSizeToBytesProper(element));
			System.out.println(element + " ===Comp===> " + parseSizeToBytesComp(element));
			System.out.println(element + " ====SI====> " + parseSizeToBytesSI(element));
			System.out.println(element + " =Default==> " + parseSizeToBytes(element));
			System.out.println();
		}
	}
	
}
