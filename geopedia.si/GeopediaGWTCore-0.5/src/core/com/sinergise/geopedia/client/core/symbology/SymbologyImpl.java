package com.sinergise.geopedia.client.core.symbology;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.Symbology;

public class SymbologyImpl extends JavaScriptObject implements Symbology{
	
	protected SymbologyImpl() {
	}

	@Override
	public final PaintingPass[] getPaintingPasses() {
		JsArray<PaintingPassImpl> arr = getPaintingPassesNative();
		PaintingPassImpl[] ls = new PaintingPassImpl[arr.length()];
		for (int i=0;i<ls.length;i++) {
			ls[i] = arr.get(i);
		}
		return ls;	
	}
	
	public static SymbologyImpl create() {
		SymbologyImpl si =  JavaScriptObject.createObject().cast();
		si.initNative();
		return si;
	}
	
	public static SymbologyImpl create(JsArray<PaintingPassImpl> ppArray) {
		SymbologyImpl si =  JavaScriptObject.createObject().cast();
		if (ppArray!=null) {
			si.setPaintingPassesNative(ppArray);
		} else {
			si.initNative();	
		}
		return si;
		
	}
	

	private final native void setPaintingPassesNative(JsArray<PaintingPassImpl> ppArray)  /*-{
		this.paintingPasses = ppArray;		
	}-*/;
	
	private final native void initNative() /*-{
		this.paintingPasses = new Array();
	}-*/;

	
	
	public final native JsArray<PaintingPassImpl> getPaintingPassesNative() /*-{
		return this.paintingPasses;
	}-*/;
}