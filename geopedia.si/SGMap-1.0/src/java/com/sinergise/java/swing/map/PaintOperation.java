/*
 * Created on Mar 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sinergise.java.swing.map;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;


/**
 * @author Miha Kadunc (<a href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com</a>
 */
public interface PaintOperation {
	
	public static abstract class Default implements PaintOperation {
		protected DisplayCoordinateAdapter coords=null;
		protected boolean cancelled=false;
		private boolean finished=false;
		public Default(DisplayCoordinateAdapter dca) {
			this.coords=dca;
		}
		public void cancelPainting() {
			cancelled=true;
		}

		public DisplayCoordinateAdapter getCoordinateAdapter() {
			return coords;
		}
		public boolean isCancelled() {
			return cancelled;
		}
		public boolean isFinished() {
			return finished;
		}
		protected void finish() {
			finished=true;
		}
	}
	
	public boolean isCancelled();
	public void cancelPainting();
	public void startPainting();
	public boolean isFinished();
	public DisplayCoordinateAdapter getCoordinateAdapter();
}
