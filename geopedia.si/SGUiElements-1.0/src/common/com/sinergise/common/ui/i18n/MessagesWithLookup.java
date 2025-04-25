package com.sinergise.common.ui.i18n;

import java.util.MissingResourceException;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.regexp.shared.RegExp;

public class MessagesWithLookup {

	ConstantsWithLookup constantsToLookup;
	
	public MessagesWithLookup(ConstantsWithLookup constantsToLookup) {
		this.constantsToLookup = constantsToLookup;
	}
	
	public String getString(String key, String... args) {
		try {
			return geSafeString(key, args);
		} catch(MissingResourceException e){
			return null;
		}
	}
	
	public String geSafeString(String key, String... args) {
		String message = constantsToLookup.getString(key);
		if(message == null){
			//probably never comes here because the method before should thrown MissingResourceException if the key is not present in the messages
			return null;
		}
		if(args != null){
			for(int i = 0; i < args.length; i++){
				RegExp reg = RegExp.compile("\\{"+i+"\\}", "g");
				message = reg.replace(message, args[i]);
			}
		}
		
		return message;
	}
	
}
