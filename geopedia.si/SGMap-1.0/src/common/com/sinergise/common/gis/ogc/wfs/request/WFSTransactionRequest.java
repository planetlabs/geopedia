package com.sinergise.common.gis.ogc.wfs.request;


public class WFSTransactionRequest extends WFSRequest {
	
	private static final long serialVersionUID = 1L;
	
	public static final String REQ_TRANSACTION = "Transaction";
	
	protected WFSTransactionRequest() {
		super(REQ_TRANSACTION);
	}

}
