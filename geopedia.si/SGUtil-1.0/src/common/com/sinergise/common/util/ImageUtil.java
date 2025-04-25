package com.sinergise.common.util;

public class ImageUtil {
	
	public static final String getWorldSuffix(String imageSuffix) {
		imageSuffix = imageSuffix.toLowerCase();
		final StringBuffer buf = new StringBuffer(3);
		buf.append(imageSuffix.charAt(0));
		buf.append(imageSuffix.charAt(imageSuffix.length() - 1));
		buf.append('w');
		return buf.toString();
	}
	
	public static final String[] getWorldFileNames(final String fName) {
		final int dotIdx = fName.lastIndexOf('.');
		final String firstPart = fName.substring(0, dotIdx + 2);
		final String suffPart = fName.substring(dotIdx + 2);
		final boolean cap = suffPart.charAt(0) == Character.toUpperCase(suffPart.charAt(0));
		final char wChar = cap ? 'W' : 'w';
		
		final String normOut = firstPart + suffPart.charAt(suffPart.length() - 1) + wChar;
		final String addOut = fName + wChar;
		
		if (suffPart.length() == 2) {
			return new String[]{normOut, addOut};
		}
		
		final String extOut = firstPart + suffPart.substring(0, suffPart.length() - 2) + suffPart.charAt(suffPart.length() - 1) + wChar;
		
		return new String[]{normOut, addOut, extOut};
	}
	
	public static String getWorldFileNameDefault(final String fName) {
		final int dotIdx = fName.lastIndexOf('.');
		final String firstPart = fName.substring(0, dotIdx + 2);
		final String suffPart = fName.substring(dotIdx + 2);
		final boolean cap = suffPart.charAt(0) == Character.toUpperCase(suffPart.charAt(0));
		final char wChar = cap ? 'W' : 'w';
		
		return firstPart + suffPart.charAt(suffPart.length() - 1) + wChar;
	}
	
	public static final String[] getWorldSuffixes(final String imageSuffix) {
		final char firstCh = imageSuffix.charAt(0);
		final boolean cap = firstCh == Character.toUpperCase(firstCh);
		final char wChar = cap ? 'W' : 'w';
		
		final String normOut = String.valueOf(firstCh) + imageSuffix.charAt(imageSuffix.length() - 1) + wChar;
		final String addOut = imageSuffix + wChar;
		
		if (imageSuffix.length() <= 3) {
			return new String[]{normOut, addOut};
		}
		
		final String extOut = imageSuffix.substring(0, imageSuffix.length() - 2) + imageSuffix.charAt(imageSuffix.length() - 1) + wChar;
		
		return new String[]{normOut, addOut, extOut};
	}
	
	public static final boolean isImageSuffix(final String suf) {
		final String flLC = suf.toLowerCase();
		if (flLC.endsWith("tif")) {
			return true;
		}
		if (flLC.endsWith("tiff")) {
			return true;
		}
		if (flLC.endsWith("jpg")) {
			return true;
		}
		if (flLC.endsWith("jpeg")) {
			return true;
		}
		if (flLC.endsWith("png")) {
			return true;
		}
		if (flLC.endsWith("gif")) {
			return true;
		}
		if (flLC.endsWith("bmp")) {
			return true;
		}
		if (flLC.endsWith("tga")) {
			return true;
		}
		if (flLC.endsWith("ecw")) {
			return true;
		}
		if (flLC.endsWith("jp2")) {
			return true;
		}
		if (flLC.endsWith("sid")) {
			return true;
		}
		return false;
	}
	
	public static final boolean isImageFile(final String fileName) {
		final int dotIdx = fileName.lastIndexOf('.');
		if (dotIdx < 0) return false;
		return isImageSuffix(fileName.substring(dotIdx + 1));
	}
	
	public static final String imageIOTypeFromMime(final String mime) {
		final int semic = mime.indexOf(';');
		return mime.substring("image/".length(), semic < 0 ? mime.length() : semic).toUpperCase();
	}
	
}
