/*
 *
 */
package com.sinergise.common.gis.map.render;

import java.util.Iterator;
import java.util.Vector;

public class RepaintListenerCollection extends Vector<RepaintListener> {
	private static final long serialVersionUID = 1L;

		public boolean fireBeforeScheduled(int millis, boolean continuous, boolean hard) {
        boolean ret=true;
        for (Iterator<RepaintListener> it = iterator(); it.hasNext();) {
            RepaintListener ll = it.next();
            try {
                if (!ll.beforeRepaintScheduled(millis, continuous, hard)) ret=false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    
    public void fireScheduled(int millis, boolean continuous, boolean hard) {
        for (Iterator<RepaintListener> it = iterator(); it.hasNext();) {
            RepaintListener ll = it.next();
            try {
                ll.onRepaintScheduled(millis, continuous, hard);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean fireBeforeRepaint(boolean hard) {
        boolean ret=true;
        for (Iterator<RepaintListener> it = iterator(); it.hasNext();) {
            RepaintListener ll = it.next();
            try {
                if (!ll.beforeRepaint(hard)) ret=false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    
    public void fireRepaint(boolean hard) {
        for (Iterator<RepaintListener> it = iterator(); it.hasNext();) {
            RepaintListener ll = it.next();
            try {
                ll.onRepaint(hard);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
