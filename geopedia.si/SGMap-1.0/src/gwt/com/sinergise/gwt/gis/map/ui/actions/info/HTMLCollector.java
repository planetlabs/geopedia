/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.actions.info;

import com.google.gwt.user.client.ui.HTML;

/**
 * Collector intended to collect HTML results returned from RPC callbacks.
 * <br><br>
 * Implementators of this interface may be GUI panels rendering 
 * the HTML or just simple holders.
 * 
 * @author tcerovski
 */
public interface HTMLCollector {

	public void add(HTML html);
	
	public void clearFeatures();
	
}
