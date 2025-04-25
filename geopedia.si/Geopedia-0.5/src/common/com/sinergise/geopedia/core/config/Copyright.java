/*
 *
 */
package com.sinergise.geopedia.core.config;

import java.io.Serializable;

public class Copyright implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    public String id;
    public String name;
    public String refURL;
    
    public static Copyright create(String id, String name, String refURL) {
    	Copyright copy = new Copyright();
    	copy.id = id;
    	copy.name = name;
    	copy.refURL = refURL;
    	return copy;
    }
    
    public String getImageURL(boolean print) {
        return "images/copyright/"+id.toUpperCase()+(print?"_print.png":".png");
    }
}
