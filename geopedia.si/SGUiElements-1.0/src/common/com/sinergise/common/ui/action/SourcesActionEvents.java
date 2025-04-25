/*
 *
 */
package com.sinergise.common.ui.action;



public interface SourcesActionEvents {
    /**
     * Adds a listener interface.
     * 
     * @param listener the listener interface to add
     */
    public void addActionListener(ActionListener listener);

    /**
     * Removes a previously added listener interface.
     * 
     * @param listener the listener interface to remove
     */
    public void removeActionListener(ActionListener listener);
}
