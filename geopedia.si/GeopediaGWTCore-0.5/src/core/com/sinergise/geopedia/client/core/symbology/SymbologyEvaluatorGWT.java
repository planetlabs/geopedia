package com.sinergise.geopedia.client.core.symbology;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.sinergise.geopedia.core.symbology.AbstractSymbologyUtils;
import com.sinergise.geopedia.core.symbology.Symbology;
	
public class SymbologyEvaluatorGWT {
	
	private static GWTSymbologyUtils symWriter = new GWTSymbologyUtils();
	
	public static JavaScriptObject createLineSymbolizer() {
		return LineSymbolizerImpl.create();
	}
	
	public static JavaScriptObject createPaintingPass(JsArray<SymbolizerImpl> symArray) {
		return PaintingPassImpl.create(symArray);
	}

	public static JavaScriptObject createFillSymbolizer() {
		return FillSymbolizerImpl.create();
	}
	
	public static JavaScriptObject createPointSymbolizer() {
		return PointSymbolizerImpl.create();
	}
	
	public static JavaScriptObject createTextSymbolizer() {
		return TextSymbolizerImpl.create();
	}
	
	public static JavaScriptObject createSymbolizerFont() {
		return SymbolizerFontImpl.create();
	}
	
	public static JavaScriptObject createSymbology(JsArray<PaintingPassImpl> ppArray) {
		return SymbologyImpl.create(ppArray);
	}
	
	
	
	public static native boolean hasExternalIdentifiers (String js) /*-{
		var identifiers = $wnd.getIdentifiers(js,true);
		if (identifiers.length == 0)
			return false;
		return true;
	}-*/;
 
	
	public static Symbology evaluateSimpleSymbology (String js) {
		Symbology symb = evaluateSimpleSymbologyNative(js);
		if (!AbstractSymbologyUtils.hasSymbolizers(symb))
			return null;
		return symb;
	}
		
	public static native Symbology evaluateSimpleSymbologyNative(String js) /*-{
		var sf = $wnd.sf;		
		sf['newSymbology'] = $entry(@com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT::createSymbology(Lcom/google/gwt/core/client/JsArray;));
		sf['newPaintingPass'] = $entry(@com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT::createPaintingPass(Lcom/google/gwt/core/client/JsArray;));
		sf['newLineSymbolizer'] = $entry(@com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT::createLineSymbolizer());
		sf['newPointSymbolizer'] = $entry(@com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT::createPointSymbolizer());
		sf['newFillSymbolizer'] = $entry(@com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT::createFillSymbolizer());
		sf['newTextSymbolizer'] = $entry(@com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT::createTextSymbolizer());
		sf['newSymbolizerFont'] = $entry(@com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT::createSymbolizerFont());
				
			var myfunc = new Function("sf", js);
			return myfunc(sf);		
		}-*/;

	
	public static String toJavascript(Symbology symbology) {
		return symWriter.toJavaScript(symbology);
	}
}
