package com.sinergise.gwt.extensions.google.visualization.client.visualizations.corechart;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.visualization.client.Color;

public class Series extends JavaScriptObject {

	public static Series create() {
		return JavaScriptObject.createObject().cast();
	}

	protected Series() {
	}

	public final native void setPointSize(int pointSize)  /*-{
	  this.pointSize=pointSize
	}-*/;

	public final native void setLineWidth(int lineWidth)  /*-{
	  this.lineWidth=lineWidth;
	}-*/;

	public final native void setLineWidth(double areaOpacity)  /*-{
	  this.areaOpacity=areaOpacity;
	}-*/;

	public final native void visibleInLegend(boolean visibleInLegend)  /*-{
	  this.visibleInLegend=visibleInLegend;
	}-*/;
	
    public final native void setColor(String color) /*-{
	  this.color = color;
	}-*/;

	public final native void setColor(Color color) /*-{
	  this.color = color;
	}-*/;
	
	public final native void setTargetAxisIndex(int index)  /*-{
  	this.targetAxisIndex = index;
	}-*/;
}
