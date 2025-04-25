package com.sinergise.generics.i18n;

import java.util.HashMap;

public class ServerI18NResources {
	
	private HashMap<String, Language> languages = new HashMap<String, Language>();
	
	public String getWidgetAttributeTranslation (String widgetName, String entityName,  String lookupAttributeName,
			String defaultAttributeValue, String language) {
		
		String lookupKey ="widget."+widgetName+"."+lookupAttributeName;
		String value = getLanguageString(lookupKey, language);
		if (value!=null)
			return value;
		if (entityName!=null) {
			lookupKey = "entity."+entityName+"."+lookupAttributeName;
			value = getLanguageString(lookupKey, language);
			if (value!=null)
				return value;
		}
		return defaultAttributeValue;
	}

	public void addLanguage(Language lang) {
		languages.put(lang.getLanguage(), lang);
	}
	
	public String getLanguageString(String lookupKey, String languageName) {
		Language lang = languages.get(languageName);
		if (lang==null)
			return null;
		return lang.getLanguageString(lookupKey);
	}
}
