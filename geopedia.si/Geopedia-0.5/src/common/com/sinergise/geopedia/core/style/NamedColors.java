package com.sinergise.geopedia.core.style;

import java.util.HashMap;

public abstract class NamedColors
{
	public static final String[] names = { "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure",
	                "beige", "bisque", "black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood",
	                "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson",
	                "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkgrey",
	                "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred",
	                "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey",
	                "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey",
	                "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro",
	                "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew",
	                "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush",
	                "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan",
	                "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink",
	                "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey",
	                "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon",
	                "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen",
	                "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred",
	                "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace",
	                "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen",
	                "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum",
	                "powderblue", "purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon",
	                "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue",
	                "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", "teal", "thistle",
	                "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen" };

	public static final int[] rgbs = { 0XFFF0F8FF, 0XFFFAEBD7, 0XFF00FFFF, 0XFF7FFFD4, 0XFFF0FFFF,
	                0XFFF5F5DC, 0XFFFFE4C4, 0XFF000000, 0XFFFFEBCD, 0XFF0000FF, 0XFF8A2BE2, 0XFFA52A2A,
	                0XFFDEB887, 0XFF5F9EA0, 0XFF7FFF00, 0XFFD2691E, 0XFFFF7F50, 0XFF6495ED, 0XFFFFF8DC,
	                0XFFDC143C, 0XFF00FFFF, 0XFF00008B, 0XFF008B8B, 0XFFB8860B, 0XFFA9A9A9, 0XFF006400,
	                0XFFA9A9A9, 0XFFBDB76B, 0XFF8B008B, 0XFF556B2F, 0XFFFF8C00, 0XFF9932CC, 0XFF8B0000,
	                0XFFE9967A, 0XFF8FBC8F, 0XFF483D8B, 0XFF2F4F4F, 0XFF2F4F4F, 0XFF00CED1, 0XFF9400D3,
	                0XFFFF1493, 0XFF00BFFF, 0XFF696969, 0XFF696969, 0XFF1E90FF, 0XFFB22222, 0XFFFFFAF0,
	                0XFF228B22, 0XFFFF00FF, 0XFFDCDCDC, 0XFFF8F8FF, 0XFFFFD700, 0XFFDAA520, 0XFF808080,
	                0XFF008000, 0XFFADFF2F, 0XFF808080, 0XFFF0FFF0, 0XFFFF69B4, 0XFFCD5C5C, 0XFF4B0082,
	                0XFFFFFFF0, 0XFFF0E68C, 0XFFE6E6FA, 0XFFFFF0F5, 0XFF7CFC00, 0XFFFFFACD, 0XFFADD8E6,
	                0XFFF08080, 0XFFE0FFFF, 0XFFFAFAD2, 0XFFD3D3D3, 0XFF90EE90, 0XFFD3D3D3, 0XFFFFB6C1,
	                0XFFFFA07A, 0XFF20B2AA, 0XFF87CEFA, 0XFF778899, 0XFF778899, 0XFFB0C4DE, 0XFFFFFFE0,
	                0XFF00FF00, 0XFF32CD32, 0XFFFAF0E6, 0XFFFF00FF, 0XFF800000, 0XFF66CDAA, 0XFF0000CD,
	                0XFFBA55D3, 0XFF9370DB, 0XFF3CB371, 0XFF7B68EE, 0XFF00FA9A, 0XFF48D1CC, 0XFFC71585,
	                0XFF191970, 0XFFF5FFFA, 0XFFFFE4E1, 0XFFFFE4B5, 0XFFFFDEAD, 0XFF000080, 0XFFFDF5E6,
	                0XFF808000, 0XFF6B8E23, 0XFFFFA500, 0XFFFF4500, 0XFFDA70D6, 0XFFEEE8AA, 0XFF98FB98,
	                0XFFAFEEEE, 0XFFDB7093, 0XFFFFEFD5, 0XFFFFDAB9, 0XFFCD853F, 0XFFFFC0CB, 0XFFDDA0DD,
	                0XFFB0E0E6, 0XFF800080, 0XFFFF0000, 0XFFBC8F8F, 0XFF4169E1, 0XFF8B4513, 0XFFFA8072,
	                0XFFF4A460, 0XFF2E8B57, 0XFFFFF5EE, 0XFFA0522D, 0XFFC0C0C0, 0XFF87CEEB, 0XFF6A5ACD,
	                0XFF708090, 0XFF708090, 0XFFFFFAFA, 0XFF00FF7F, 0XFF4682B4, 0XFFD2B48C, 0XFF008080,
	                0XFFD8BFD8, 0XFFFF6347, 0XFF40E0D0, 0XFFEE82EE, 0XFFF5DEB3, 0XFFFFFFFF, 0XFFF5F5F5,
	                0XFFFFFF00, 0XFF9ACD32 };

	public static final HashMap<String, Integer> nameToARGB = new HashMap<String, Integer>();
	public static final HashMap<Integer, String> ARGBToName = new HashMap<Integer, String>();
	static {
		if (rgbs.length != names.length)
			throw new IllegalStateException();

		for (int a = 0; a < names.length; a++) {
			Integer argb = new Integer(rgbs[a]);
			
			nameToARGB.put(names[a], argb);
			ARGBToName.put(argb, names[a]);
		}
	}
}
