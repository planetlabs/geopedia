package com.sinergise.gwt.extensions.google.visualization.client.visualizations.corechart;

import com.google.gwt.core.client.JavaScriptObject;

public class HorizontalAxisOptions extends AxisOptions {
	 public static HorizontalAxisOptions create() {
	    return JavaScriptObject.createObject().cast();
	 }
	 
	 protected HorizontalAxisOptions () {		
			super();
		}
		

	 public final native void setFormat(String format) /*-{
	 	this.format = format;
	 }-*/;
	 

	  public final native void setMaxAlternation(int maxAlternation) /*-{
	    this.maxAlternation = maxAlternation;
	  }-*/;

	  public final native void setShowTextEvery(int showTextEvery) /*-{
	    this.showTextEvery = showTextEvery;
	  }-*/;

	  public final native void setSlantedText(boolean isSlanted) /*-{
	    this.slantedText = isSlanted;
	  }-*/;

	  public final native void setSlantedTextAngle(double slantedTextAngle) /*-{
	    this.slantedTextAngle = slantedTextAngle;
	  }-*/;
}
