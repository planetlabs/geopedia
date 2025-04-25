package com.sinergise.geopedia.core.constants;

public class Icons {

	public static String getDestinationMarkerImage(int destIndex, int size) {
		return "img/symbols/circle/"+size+"/blue/"+((char)(65+destIndex))+".png";
	}

	public static String getSuggestionMarkerImage(int destIndex, int size) {
		String symb = destIndex<9?String.valueOf(destIndex+1):String.valueOf(((char)(65+destIndex-9)));
		return "img/symbols/circle/"+size+"/yellow/"+symb+".png";
	}

	
	public interface Sizes {

		public static final int FILL_ICON_WIDTH = 32;
		public static final int FILL_ICON_HEIGHT = 32;
		public static final int SYM_ICON_WIDTH = 32;
		public static final int SYM_ICON_HEIGHT = 32;
		public static final int LINE_ICON_WIDTH = 48;
		public static final int LINE_ICON_HEIGHT = 16;
		public static final int FONT_ICON_WIDTH = 64;
		public static final int FONT_ICON_HEIGHT = 20;

		
		public static final int SYM_SMALL = 20;
		public static final int SYM_MEDIUM = 26;
		public static final int SYM_LARGE = 34;
		
	}
}
