/*
 * @@COPYRIGHT@@
 */
package com.sinergise.java.util.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.SwingUtilities;

/**
 * This is a convenience class with static methods used to position graphic components on screen.
 * 
 * @author Kaiser Soze
 * @version @@VERSION@@
 */

public class ComponentPositioner {
	
	/**
	 * Centers a window in the middle of the screen. Screen size is queried from the AWT Toolkit. The window is centered by calling its
	 * <code>setLocation</code> function.
	 * 
	 * @param w the window to be centered
	 */
	public static void centerOnScreen(final Window w) {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension size = w.getSize();
		w.setLocation(screenSize.width / 2 - size.width / 2, screenSize.height / 2 - size.height / 2);
	}
	
	/**
	 * @param adg
	 * @param topContainer
	 */
	public static void centerOnParent(final Window win, final Container parent) {
		final Point centScr = new Point(parent.getSize().width / 2, parent.getSize().height / 2);
		SwingUtilities.convertPointToScreen(centScr, parent);
		final Dimension dim = win.getSize();
		win.setLocation(centScr.x - dim.width / 2, centScr.y - dim.height / 2);
	}
}
