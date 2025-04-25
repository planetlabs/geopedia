package com.sinergise.geopedia.client.core.map;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;




public interface MapOverlay   
{
	/**
	 * Repositions the tiles according to dca. If isTrans is true,
	 * an attempt should be made to make the tiles transparent. If it
	 * is false, an attempt should be made to make the tiles opaque.
	 * <br><br>
	 * If nothing is displayed, false should be returned. Otherwise, true :)
	 * 
	 * @param dca coordinate adapter
	 * @param isTrans enable transparency
	 * @return whether something was displayed
	 */
	boolean reposition(DisplayCoordinateAdapter dca, boolean isTrans);
	public void setVisible(boolean visible);
	public boolean isVisible();
}
