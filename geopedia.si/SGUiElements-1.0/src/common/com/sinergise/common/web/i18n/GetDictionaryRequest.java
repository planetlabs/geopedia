package com.sinergise.common.web.i18n;

import java.io.Serializable;

import com.sinergise.common.util.format.Locale;

public class GetDictionaryRequest implements Serializable {

	private static final long serialVersionUID = -4439467744096415089L;
	
	private String dictionaryName;
	private Locale locale;
	
	@Deprecated /** Serialization only */
	protected GetDictionaryRequest() { }
	
	public GetDictionaryRequest(String dictionaryName) {
		this(dictionaryName, Locale.getDefault());
	}
	
	public GetDictionaryRequest(String dictionaryName, Locale locale) {
		this.dictionaryName = dictionaryName;
		this.locale = locale;
	}
	
	public String getDictionaryName() {
		return dictionaryName;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
}
