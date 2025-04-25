package com.sinergise.generics.i18n;

public abstract class Language implements Translations{
	private String language = null;
	
	public void initialize (String language) {
		this.language = language;		
	}
	
	public String getLanguage() {
		return language;
	}
	
	@Override
	public abstract String getLanguageString(String key);
	
}
