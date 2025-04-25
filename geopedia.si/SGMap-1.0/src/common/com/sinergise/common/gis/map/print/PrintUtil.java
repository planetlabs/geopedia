package com.sinergise.common.gis.map.print;

import com.sinergise.common.util.geom.Envelope;

public class PrintUtil {
	public static final String AUTO_CURRENT_PAGE = "AUTO_CurrentPage";
	public static final String AUTO_TOTAL_PAGES = "AUTO_TotalPages";
	public static final String AUTO_CURRENT_DATE="AUTO_CurrentDate";
	public static final String AUTO_MAP_CENTRE_COORDS="AUTO_MapCentreCoords";
	
	public static Envelope getPrintEnvelope(Envelope mapEnv, Envelope parcelEnv) {
		if (Envelope.isNullOrEmpty(parcelEnv)) {
			return mapEnv.expandedForSizeRatio(0.01);
		}
		return parcelEnv.expandedForSizeRatio(0.05);
	}

	public static String expression(String variableOrParam) {
		return "${{"+variableOrParam+"}}";
	}

}
