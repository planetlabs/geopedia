package com.sinergise.gwt.extensions.google.visualization.client.visualizations.corechart;



import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class AxisOptions extends com.google.gwt.visualization.client.visualizations.corechart.AxisOptions{
	
	public enum ViewWindowMode {
		PRETTY,MAXIMIZED,EXPLICIT;
	}
	public static class ViewWindow extends JavaScriptObject {

		public static ViewWindow create() {
			return JavaScriptObject.createObject().cast();
		}

		protected ViewWindow() {
			
		}
		
		public final void setMin(Date date) {
			setMinDate(date.getTime());
		}

		public final void setMax(Date date) {
			setMaxDate(date.getTime());
		}

		private final native void setMinDate(double value) /*-{
	 		var newDate = new $wnd.Date(value);
    		// Safari bug: see issue 219
    		newDate.constructor = $wnd.Date;
    		this.min = newDate;
    	}-*/;
		
		private final native void setMaxDate(double value) /*-{
		 	var newDate = new $wnd.Date(value);
	    	// Safari bug: see issue 219
	    	newDate.constructor = $wnd.Date;
	    	this.max = newDate;
	    }-*/;
		
		public final native void setMax(double max) /*-{
		  this.max = max;
		}-*/;
		
		public final native void setMin(double min) /*-{
		  this.min = min;
		}-*/;
	}
	
	public static AxisOptions create() {
	    return JavaScriptObject.createObject().cast();
	  }

	  protected AxisOptions() {
	  }

	  public final void setViewWindowMode(ViewWindowMode mode) {
		  setViewWindowMode(mode.toString().toLowerCase());
	  }
	  private final native void setViewWindowMode (String mode) /*-{
		  this.viewWindowMode=mode
	  }-*/;
	  public final native void setViewWindow (ViewWindow window) /*-{
		  this.viewWindow=window
	  }-*/;
}
