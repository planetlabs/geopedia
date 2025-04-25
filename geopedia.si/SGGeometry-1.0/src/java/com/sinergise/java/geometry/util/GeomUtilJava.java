package com.sinergise.java.geometry.util;

import com.sinergise.java.geometry.crs.CrsLegacyTransformer;
import com.sinergise.java.util.settings.MapTransformer;


public class GeomUtilJava {
	static {
		MapTransformer.registerMapTransformer(new CrsLegacyTransformer());
	}
	public static void initStaticUtils() {
		// force static init
	}
}
