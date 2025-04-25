package com.sinergise.common.gis.ogc.wfs.response;

import com.sinergise.common.util.web.MimeType;

public class DefaultTransactionResponse implements WFSTransactionResponse {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param response source to copy into the returned DefaultTransactionResponse 
	 */
	public static DefaultTransactionResponse createFrom(WFSTransactionResponse response) {
		return new DefaultTransactionResponse();
	}
	

	@Override
	public MimeType getMimeType() {
		return MIME_OBJECT_TRANSACTION;
	}

}
