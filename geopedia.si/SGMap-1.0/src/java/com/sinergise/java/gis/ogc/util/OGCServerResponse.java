package com.sinergise.java.gis.ogc.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.sinergise.common.gis.ogc.wms.response.WMSResponse;


public interface OGCServerResponse extends WMSResponse {
	void writeOutput(HttpServletResponse output) throws IOException;
}
