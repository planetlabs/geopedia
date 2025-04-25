package com.sinergise.geopedia.client.core.symbology;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.Symbolizer;

public class PaintingPassImpl extends JavaScriptObject implements PaintingPass{
	
	protected PaintingPassImpl() {
	}

	@Override
	public final Symbolizer[] getSymbolizers() {
		JsArray<SymbolizerImpl> arr = getSymbolizersNative();
		SymbolizerImpl[] ls = new SymbolizerImpl[arr.length()];
		for (int i=0;i<ls.length;i++) {
			ls[i] = arr.get(i);
		}
		return ls;	
	}
	
	public static PaintingPassImpl create(JsArray<SymbolizerImpl> symArray) {
		PaintingPassImpl pp =  JavaScriptObject.createObject().cast();
		if (symArray!=null) {
			pp.setSymbolizersNative(symArray);
		} else {
			pp.initNative();	
		}
		return pp;
		
	}
	
	private final native void initNative() /*-{
		this.symbolizers = new Array();
	}-*/;

	
	private final native void setSymbolizersNative(JsArray<SymbolizerImpl> symArray)  /*-{
		this.symbolizers = symArray;		
	}-*/;
	
	public final native JsArray<SymbolizerImpl> getSymbolizersNative() /*-{
		return this.symbolizers;
	}-*/;
}
