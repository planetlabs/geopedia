package com.sinergise.generics.i18n.datasource;

import java.util.HashMap;

import com.sinergise.generics.i18n.Translations;

public class SimpleEOAttributeTranslation implements Translations{
	private HashMap<String,String> translationMap = null;
	
	public SimpleEOAttributeTranslation() {		
	}
	
	public SimpleEOAttributeTranslation(HashMap<String,String> translationMap) {
		this.translationMap=translationMap;
	}

	@Override
	public String getLanguageString(String string) {
		for (String key:translationMap.keySet()) {
			System.out.println(key+"="+translationMap.get(key));
		}
	if (string==null) return string;
		if (translationMap==null) return string;
		String translation = translationMap.get(string);
		if (translation==null) return string;
		return translation;
	}

}
