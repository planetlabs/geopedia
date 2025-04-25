package com.sinergise.geopedia.client.core.symbology;

import com.google.gwt.core.client.JsArray;
import com.sinergise.geopedia.core.symbology.AbstractSymbologyUtils;
import com.sinergise.geopedia.core.symbology.FillSymbolizer;
import com.sinergise.geopedia.core.symbology.LineSymbolizer;
import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.PointSymbolizer;
import com.sinergise.geopedia.core.symbology.Symbolizer;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.core.symbology.TextSymbolizer;

public class GWTSymbologyUtils extends AbstractSymbologyUtils {

	@Override
	protected String toJavascript(Symbolizer symbolizer) {
		SymbolizerImpl symbolizerImpl = (SymbolizerImpl)symbolizer;
		if (symbolizerImpl.getClassId() == SymbolizerImpl.ID_FILLSYMBOLIZER) {
			return toJavascript((FillSymbolizer)symbolizer);
		} else if (symbolizerImpl.getClassId() == SymbolizerImpl.ID_LINESYMBOLIZER) {
			return toJavascript((LineSymbolizer)symbolizer);
		} else if (symbolizerImpl.getClassId() == SymbolizerImpl.ID_POINTSYMBOLIZER) {
			return toJavascript((PointSymbolizer)symbolizer);
		} else if (symbolizerImpl.getClassId() == SymbolizerImpl.ID_TEXTSYMBOLIZER) {
			return toJavascript((TextSymbolizer)symbolizer);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Symbology createSymbology(PaintingPass[] paintingPasses) {
		JsArray<PaintingPassImpl> array = (JsArray<PaintingPassImpl>) JsArray.createArray();
		for (PaintingPass sym:paintingPasses)
			array.push((PaintingPassImpl) sym);
		return SymbologyImpl.create(array);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PaintingPass createPaintingPass(Symbolizer [] symbolizers) {
		JsArray<SymbolizerImpl> array = (JsArray<SymbolizerImpl>) JsArray.createArray();
		for (Symbolizer sym:symbolizers)
			array.push((SymbolizerImpl) sym);
		return PaintingPassImpl.create(array);
	}

	@Override
	public PointSymbolizer createPointSymbolizer() {
		return PointSymbolizerImpl.create();
	}

	@Override
	public LineSymbolizer createLineSymbolizer() {
		return LineSymbolizerImpl.create();
	}

	@Override
	public FillSymbolizer createFillSymbolizer() {
		return FillSymbolizerImpl.create();
	}

	@Override
	public TextSymbolizer createTextSymbolizer() {
		return TextSymbolizerImpl.create();
	}
	
	public static void initialize() {
		INSTANCE = new GWTSymbologyUtils();
	}


}
