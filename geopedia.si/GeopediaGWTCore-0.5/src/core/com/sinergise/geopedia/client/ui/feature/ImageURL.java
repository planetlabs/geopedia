package com.sinergise.geopedia.client.ui.feature;

import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.core.constants.Globals;


public class ImageURL {
    	
    public static String createUrl(long blobId, int w, int h) {
    	String url = "image/get/"+blobId+"?w="+w+"&h="+h;
    	String session = ClientSession.getSessionValue();
    	if (session!=null) {
    		url+="&"+Globals.SESSION_PARAM_NAME+"="+session;
    	}
    	return url;
    }

    public static String createUrl(int blobId, int w, int h) {
    	return createUrl((long)blobId, w, h);
    }
  
}
