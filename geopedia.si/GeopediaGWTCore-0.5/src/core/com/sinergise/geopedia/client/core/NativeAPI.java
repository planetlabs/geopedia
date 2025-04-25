package com.sinergise.geopedia.client.core;

import com.sinergise.geopedia.client.ui.NativeMediaDisplayers;

public class NativeAPI {

	private static ParametersProcessor  defaultPP = null;
	
	
	public static void initialize(ParametersProcessor pp) {
		defaultPP=pp;
		initNative();
		NativeMediaDisplayers.init();
	}
	
	public static native void initNative() /*-{
	    $wnd.gp_processInternalLink = function(s) {
	        @com.sinergise.geopedia.client.core.NativeAPI::processLink(Ljava/lang/String;)(s);
	    };  
	}-*/;
	
	 public static void processLink(String link) {
		 if (defaultPP!=null) {
			 defaultPP.processParameters(link);
		 }
    }
		
}
