package com.sinergise.common.gis.ogc.wfs.response;

import java.io.Serializable;

import com.sinergise.common.util.web.MimeType;

public interface WFSResponse extends Serializable{
	MimeType getMimeType();
}
