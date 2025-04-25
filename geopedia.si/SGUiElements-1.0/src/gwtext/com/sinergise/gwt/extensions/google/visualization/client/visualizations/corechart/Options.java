package com.sinergise.gwt.extensions.google.visualization.client.visualizations.corechart;

import com.google.gwt.ajaxloader.client.ArrayHelper;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class Options extends com.google.gwt.visualization.client.visualizations.corechart.Options {
	  public static Options create() {
		    return JavaScriptObject.createObject().cast();
		  }

	protected Options () {		
		super();
	}
	
	
	
	public final native void setVAxesOptions(JsArray<AxisOptions> options)  /*-{
    	this.vAxes = options;
	}-*/;
	
	public final void setVAxesOptions(AxisOptions[] options) {
		setVAxesOptions(ArrayHelper.toJsArray(options));
	}

	public final native void setSeries(JsArray<Series> series)  /*-{
  	this.series = series;
	}-*/;

	public final void setSeries(Series[] series) {
		setSeries(ArrayHelper.toJsArray(series));
	}
}
