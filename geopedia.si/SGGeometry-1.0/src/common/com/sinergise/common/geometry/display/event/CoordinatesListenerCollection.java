/*
 *
 */
package com.sinergise.common.geometry.display.event;

import java.util.Iterator;
import java.util.Vector;

public class CoordinatesListenerCollection extends Vector<CoordinatesListener> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

		public void fireChange(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
        for (Iterator<CoordinatesListener> it = iterator(); it.hasNext();) {
            CoordinatesListener ll = it.next();
            try {
                ll.coordinatesChanged(newX, newY, newScale, coordsChanged, scaleChanged);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void fireSizeChange(int newW, int newH) {
        for (Iterator<CoordinatesListener> it = iterator(); it.hasNext();) {
            CoordinatesListener ll = it.next();
            try {
                ll.displaySizeChanged(newW, newH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
