package com.sinergise.geopedia.style.symbology.rhino;

import org.mozilla.javascript.NativeArray;

import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.symbology.AbstractSymbologyUtils;
import com.sinergise.geopedia.core.symbology.FillSymbolizer;
import com.sinergise.geopedia.core.symbology.LineSymbolizer;
import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.PointSymbolizer;
import com.sinergise.geopedia.core.symbology.Symbolizer;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.core.symbology.TextSymbolizer;

public class JavaSymbologyUtils extends AbstractSymbologyUtils {

	

	public static PointSymbolizer getSymbolizerFromSymbology (GeomType geomType, Symbology symbology) {
		if (symbology==null) return null;
		PaintingPass [] paintingPasses = symbology.getPaintingPasses();
		if (paintingPasses==null || paintingPasses.length==0) return null;
		LineSymbolizer ls = null;
		FillSymbolizer fs = null;
		for (PaintingPass pp:paintingPasses) {
			if (pp!=null) {
				Symbolizer[] symb = pp.getSymbolizers();
				if (symb!=null) {
					for (Symbolizer s:symb) {
						if (s instanceof PointSymbolizer)
							return (PointSymbolizer) s;
						else if (ls ==null && s instanceof LineSymbolizer) {
							ls=(LineSymbolizer) s;
						} else if (fs == null && s instanceof FillSymbolizer) {
							fs = (FillSymbolizer) s;
						}
							
					}
				}
			}
		}
		if (geomType.isPoint()) {
			return null;
		} else if (geomType.isPolygon()) {
			PointSymbolizerImpl psi = new PointSymbolizerImpl();
			psi.setSymbolId(Feature.SYMBOL_ID_POLYGON);
			if (fs!=null) {
				psi.setFill(fs.getFillBackground().getRGB());
			}
			return psi;
		} else if (geomType.isLine()) {
			PointSymbolizerImpl psi = new PointSymbolizerImpl();
			psi.setSymbolId(Feature.SYMBOL_ID_LINE);
			if (ls!=null) {
				psi.setFill(ls.getStroke().getRGB());
			}
			return psi;
				
		}
		return null;
		
	}
	protected String toJavascript(Symbolizer symbolizer) {
		if (symbolizer instanceof FillSymbolizer) {
			return toJavascript((FillSymbolizer)symbolizer);
		} else if (symbolizer instanceof LineSymbolizer) {
			return toJavascript((LineSymbolizer)symbolizer);
		} else if (symbolizer instanceof PointSymbolizer) {
			return toJavascript((PointSymbolizer)symbolizer);
		}  else if (symbolizer instanceof TextSymbolizer) {
			return toJavascript((TextSymbolizer)symbolizer);
		}
		return null;
	}

	

	@Override
	public PointSymbolizer createPointSymbolizer() {
		return new PointSymbolizerImpl();
	}

	@Override
	public LineSymbolizer createLineSymbolizer() {
		return new LineSymbolizerImpl();
	}


	@Override
	public FillSymbolizer createFillSymbolizer() {
		return new FillSymbolizerImpl();
	}
	
	@Override
	public PaintingPass createPaintingPass(Symbolizer[] symbolizers) {
		NativeArray na = new NativeArray(symbolizers);
		return new PaintingPassImpl(na);
	}
	
	@Override
	public Symbology createSymbology(PaintingPass[] paintingPasses) {
		NativeArray na = new NativeArray(paintingPasses);
		return new SymbologyImpl(na);
	}



	@Override
	public TextSymbolizer createTextSymbolizer() {
		return new TextSymbolizerImpl();
	}

	public static void initialize() {
		INSTANCE = new JavaSymbologyUtils();
	}



}
