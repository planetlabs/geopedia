package com.sinergise.common.geometry.symbology;




public abstract class AbstractSymbologyUtils {

	protected static AbstractSymbologyUtils INSTANCE = null;
	
	public static AbstractSymbologyUtils getInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("No SymbologyUtils instance. Did you forget to initialize?");
		}
		return INSTANCE;
	}
	
	public static boolean hasSymbolizers(Symbology symbology) {
		if (symbology==null) return false;
		PaintingPass [] paintingPasses = symbology.getPaintingPasses();
		if (paintingPasses==null || paintingPasses.length==0) return false;
		for (PaintingPass pp:paintingPasses) {
			if (pp!=null) {
				Symbolizer[] symb = pp.getSymbolizers();
				if (symb!=null) {
					for (Symbolizer s:symb) {
						if (s!=null) return true;
					}
				}
			}
		}
		return false;
	}
	protected String appendSymbolizerAttributes(Symbolizer symbolizer) {
		if (symbolizer==null) return "";
		StringBuffer jsBuffer = new StringBuffer();
		jsBuffer.append("opacity: "+String.valueOf(symbolizer.getOpacity())+",");		
		jsBuffer.append("displacementX: "+String.valueOf(symbolizer.getDisplacement().x)+",");
		jsBuffer.append("displacementY: "+String.valueOf(symbolizer.getDisplacement().y)+",");
		return jsBuffer.toString();
	}
	
	protected String toJavascript(SymbolizerFont symbolizer) {
		if (symbolizer==null) return null;
		StringBuffer jsBuffer = new StringBuffer();
		jsBuffer.append("sf.SymbolizerFont({");
		jsBuffer.append("fontSize: "+String.valueOf(symbolizer.getFontSize())+",");		
		jsBuffer.append("fontFamily: '"+String.valueOf(symbolizer.getFontFamily())+"',");
		jsBuffer.append("fontStyle: '"+String.valueOf(symbolizer.getFontStyle().name())+"',");
		jsBuffer.append("fontWeight: '"+String.valueOf(symbolizer.getFontWeight().name())+"'})");
		return jsBuffer.toString();
	}
	
	protected String toJavascript (FillSymbolizer symbolizer) {
		if (symbolizer==null) return null;
		StringBuffer jsBuffer = new StringBuffer();
		jsBuffer.append("sf.FillSymbolizer({");
		jsBuffer.append(appendSymbolizerAttributes(symbolizer));
		jsBuffer.append("fill: 0x"+Integer.toHexString(symbolizer.getFill().getARGB())+",");
		jsBuffer.append("fillBackground: 0x"+Integer.toHexString(symbolizer.getFillBackground().getARGB())+",");
		jsBuffer.append("fillType: '"+String.valueOf(symbolizer.getFillType().name())+"'})");
		return jsBuffer.toString();
	}
	

	
	
	protected String toJavascript (PointSymbolizer symbolizer) {
		if (symbolizer==null) return null;	
		StringBuffer jsBuffer = new StringBuffer();
		jsBuffer.append("sf.PointSymbolizer({");
		jsBuffer.append(appendSymbolizerAttributes(symbolizer));
		jsBuffer.append("size: "+String.valueOf(symbolizer.getSize())+",");
		jsBuffer.append("symbolId: "+String.valueOf(symbolizer.getSymbolId())+",");		
		jsBuffer.append("fill: 0x"+Integer.toHexString(symbolizer.getFill().getARGB())+"})");
		return jsBuffer.toString();
	}
	
	protected String toJavascript (LineSymbolizer symbolizer) {		
		if (symbolizer==null) return null;
		StringBuffer jsBuffer = new StringBuffer();
		jsBuffer.append("sf.LineSymbolizer({");
		jsBuffer.append(appendSymbolizerAttributes(symbolizer));
		jsBuffer.append("lineType: '"+String.valueOf(symbolizer.getLineType().name())+"',");
		jsBuffer.append("stroke: 0x"+Integer.toHexString(symbolizer.getStroke().getARGB())+",");
		jsBuffer.append("strokeBackground: 0x"+Integer.toHexString(symbolizer.getStrokeBackground().getARGB())+",");
		jsBuffer.append("strokeWidth: "+String.valueOf(symbolizer.getStrokeWidth())+"})");
		return jsBuffer.toString();
	}
	
	protected String toJavascript (TextSymbolizer symbolizer) {		
		if (symbolizer==null) return null;
		StringBuffer jsBuffer = new StringBuffer();
		jsBuffer.append("sf.TextSymbolizer({");
		jsBuffer.append(appendSymbolizerAttributes(symbolizer));
		jsBuffer.append("fill: 0x"+Integer.toHexString(symbolizer.getFill().getARGB())+",");
		jsBuffer.append("label: '"+String.valueOf(symbolizer.getLabel())+"',");
		String fontVar = toJavascript(symbolizer.getFont());
		if (fontVar!=null) {
			jsBuffer.append("font: "+fontVar+"})");
		}
		return jsBuffer.toString();
	}
	
	
	public String toJavaScript(Symbology symbology) {
		StringBuffer jsBuffer = new StringBuffer();
		PaintingPass[] pPasses = symbology.getPaintingPasses();
		jsBuffer.append("return sf.Symbology([");
		boolean isPPFirst = true;
		for (int i=0;i<pPasses.length;i++) {
			if (!isPPFirst) jsBuffer.append(", ");
			Symbolizer [] symbolizers = pPasses[i].getSymbolizers();
			jsBuffer.append("sf.PaintingPass([");

			boolean isFirst = true;
			for (int j=0;j<symbolizers.length;j++) {
				String symbolizerJS = toJavascript(symbolizers[j]);
				if (!isFirst) jsBuffer.append(", ");
				if (symbolizerJS!=null) {
					jsBuffer.append(symbolizerJS);
					isFirst=false;
				}
				
			}
			jsBuffer.append("])");
			isPPFirst=false;
		}
		jsBuffer.append("]);");
		return jsBuffer.toString();
	}
	
	
	
	public Symbology createDefaultPolygonSimbology() {
		PaintingPass pp = createPaintingPass(new Symbolizer[]{createLineSymbolizer(), createFillSymbolizer()});
		Symbology symb = createSymbology(new PaintingPass[]{pp});
		return symb;
	}
	
	public Symbology createDefaultLineSimbology() {
		PaintingPass pp = createPaintingPass(new Symbolizer[]{createLineSymbolizer()});
		Symbology symb = createSymbology(new PaintingPass[]{pp});
		return symb;

	}
	
	public Symbology createDefaultPointSymbology() {		
		PaintingPass pp = createPaintingPass(new Symbolizer[]{createPointSymbolizer(), createTextSymbolizer()});
		Symbology symb = createSymbology(new PaintingPass[]{pp});
		return symb;
	}
	
	protected abstract String toJavascript(Symbolizer symbolizer);
	public abstract Symbology createSymbology(PaintingPass[] paintingPasses);
	public abstract PaintingPass createPaintingPass(Symbolizer [] symbolizers);
	public abstract PointSymbolizer createPointSymbolizer();
	public abstract LineSymbolizer createLineSymbolizer();
	public abstract FillSymbolizer createFillSymbolizer();
	public abstract TextSymbolizer createTextSymbolizer();
}
