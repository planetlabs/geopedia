package com.sinergise.gwt.gis.ui.gfx;

public class VML
{
	public static final String vmlNs;
	static {
		if (isIE())
			vmlNs = initCrap();
		else
			vmlNs = "!!!ERROR!!!";
	}
	
	public static native boolean isIE() /*-{
		var ua = navigator.userAgent.toLowerCase();
		return (ua.indexOf("msie ") != -1);
	}-*/;
	 
	static native String initCrap() /*-{
		var len = $doc.namespaces.length;
		var prefix = "";
		for (var i = 0; i < len; i++) {
			if ($doc.namespaces.item(i).urn == "urn:schemas-microsoft-com:vml") {
				prefix = $doc.namespaces.item(i).name;
				break;
			}
		}
		if (prefix == "") {
			prefix = "vml";
			
			if($doc.documentMode && $doc.documentMode>=8) {
	            $doc.namespaces.add(prefix, "urn:schemas-microsoft-com:vml", "#default#VML");
	            
	            //Generic CSS prefix selectors are no longer supported in IE8 Standards Mode
	        	$doc.createStyleSheet().addRule(
		        		prefix + "\\:group, "
		        		+ prefix + "\\:shape, "
		        		+ prefix + "\\:rect, "
		        		+ prefix + "\\:oval, "
		        		+ prefix + "\\:line, "
		        		+ prefix + "\\:polyline" 
		        	, "behavior:url(#default#VML); display:inline-block;");
	            
	        } else {
	        
	            $doc.namespaces.add(prefix, "urn:schemas-microsoft-com:vml");
	            $doc.createStyleSheet().addRule(prefix + "\\:*", "behavior:url(#default#VML)");
	            
	        }
		}
		return prefix;
	}-*/;
}
