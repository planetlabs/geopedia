package com.sinergise.common.util.web;

/**
 * {@link http://www.ietf.org/rfc/rfc2616} 
 * @author Miha
 */
public enum HttpMethod {
	HEAD, GET, PUT, DELETE, POST
//	,OPTIONS ,TRACE ,CONNECT
	;
	public String getHttpToken() {
		return name();
	}
}
