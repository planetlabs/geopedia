/*
 *
 */
package com.sinergise.common.gis.map.render;

public interface RepaintListener {
	boolean beforeRepaintScheduled(int millis, boolean continuous, boolean hard);
	void onRepaintScheduled(int millis, boolean continuous, boolean hard);
	
	boolean beforeRepaint(boolean hard);
	void onRepaint(boolean hard);
}
