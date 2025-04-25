/*
 *
 */
package com.sinergise.common.ui.action;

import java.util.Vector;


public class ActionListenerCollection extends Vector<ActionListener> {
    private static final long serialVersionUID = -7751285389310929785L;

    public void fireActionPerformed(SourcesActionEvents sender, Object eventType) {
    	for (ActionListener al : this) {
            al.actionPerformed(sender, eventType);
        }
    }
}
