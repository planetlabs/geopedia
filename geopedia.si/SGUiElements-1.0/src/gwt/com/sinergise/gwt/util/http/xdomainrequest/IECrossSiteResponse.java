package com.sinergise.gwt.util.http.xdomainrequest;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;


public class IECrossSiteResponse extends Response {
	private XDomainRequest _xhr;

	public static class IEHeader extends Header {
		private String _name;
		private String _value;

		public IEHeader(String name, String val) {
			_name = name;
			_value = val;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public String getValue() {
			return _value;
		}
	}

	public IECrossSiteResponse(XDomainRequest xhr) {
		_xhr = xhr;
	}

	@Override
	public String getHeader(String header) {
		return header.equals("Content-Type") ? _xhr.getContentType() : null;
	}

	@Override
	public Header[] getHeaders() {
		if (_xhr.getContentType() != null) {
			Header ret[] = new Header[1];
			ret[0] = new IEHeader("Content-Type", _xhr.getContentType());
			return ret;
		} 
		return null;	
	}

	@Override
	public String getHeadersAsString() {
		return (_xhr.getContentType() == null) ? "" : ("Content-Type : " + _xhr.getContentType());
	}

	@Override
	public int getStatusCode() {
		return (_xhr != null) ? Response.SC_OK : Response.SC_BAD_REQUEST;
	}

	@Override
	public String getStatusText() {
		return "OK";
	}

	@Override
	public String getText() {
		return _xhr.getResponseText();
	}
}