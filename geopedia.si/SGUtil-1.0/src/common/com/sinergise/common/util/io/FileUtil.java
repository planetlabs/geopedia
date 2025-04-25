package com.sinergise.common.util.io;

import com.sinergise.common.util.format.NumberFormatProvider.NumberFormatConstants;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.string.StringUtil;

public class FileUtil {
	public static final long KB = 1024;
	public static final long MB = KB * KB;
	public static final long GB = MB * KB;
	private static final NumberFormatter decFormat = NumberFormatUtil.create("0.0", new NumberFormatConstants(".", ","));

	public static String replaceSuffix(final String fileName, final String newSuffix) {
		final int idx = fileName.lastIndexOf('.');
		if (idx < 0) {
			return fileName + '.' + newSuffix;
		}
		return fileName.substring(0, idx + 1) + newSuffix;
	}

	public static String getNameNoSuffix(final String fileName) {
		int dotIdx = fileName.lastIndexOf('.');
		return dotIdx < 0 ? fileName : fileName.substring(0, dotIdx);
	}

	public static String formatFileSize(final long nBytes) {
		if (nBytes < KB) { return nBytes + " bytes"; }
		if (nBytes < MB) { return decFormat.format((double)nBytes / KB) + " KB"; }
		if (nBytes < GB) { return decFormat.format((double)nBytes / MB) + " MB"; }
		return decFormat.format((double)nBytes / GB) + " GB";
	}

	public static String getSuffix(String fileName) {
		if (fileName == null) return null;
		int idx = fileName.lastIndexOf('.');
		return idx < 0 ? null : fileName.substring(idx + 1);
	}
	
	public static String getSuffixLowerCase(String fileName) {
		String suff = getSuffix(fileName);
		if (suff == null) return null;
		return suff.toLowerCase();
	}

	public static String getSuffixUpperCase(String fileName) {
		String suff = getSuffix(fileName);
		if (suff == null) return null;
		return suff.toUpperCase();
	}

	public static boolean isSuffixIgnoreCase(String fileName, String suffix) {
		return StringUtil.endsWithIgnoreCase(fileName, "."+suffix);
	}
}
