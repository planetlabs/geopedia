package com.sinergise.gwt.gis.map.ui.vector;



public class TextMarkerStyle extends ClosedMarkerStyle {	
	private static final int DEFAULT_FONT_SIZE = 11;
	
	//GWT serialization
	protected TextMarkerStyle() {
	}

    public TextMarkerStyle(String color) {
        super(null, null, color);
    }

    public TextMarkerStyle(String color, VectorFilter filter) {
        this(color);
        setFilter(filter);
    }
    
    public int getFontSize(){
    	return DEFAULT_FONT_SIZE;
    }

}
