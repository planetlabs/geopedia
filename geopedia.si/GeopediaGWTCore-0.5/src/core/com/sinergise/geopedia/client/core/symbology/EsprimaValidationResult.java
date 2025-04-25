package com.sinergise.geopedia.client.core.symbology;

import com.google.gwt.core.client.JavaScriptObject;

public class EsprimaValidationResult extends JavaScriptObject{
	
	public static EsprimaValidationResult create() {
		EsprimaValidationResult obj =  JavaScriptObject.createObject().cast();
		return obj;
	}
	
	protected EsprimaValidationResult() {
	}
	
	public final native int getMessageCount() /*-{
		return this.length;
	}-*/;

	public final native String getMessage(int idx) /*-{
		return this[idx];
	}-*/;

	
}
