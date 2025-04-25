package com.sinergise.common.gis.ogc.wfs.response;

import com.sinergise.common.util.web.MimeType;

public interface WFSTransactionResponse extends WFSResponse {
	
	public static final MimeType MIME_OBJECT_TRANSACTION = MimeType.getObjectMime(WFSTransactionResponse.class);

}
