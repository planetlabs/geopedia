/*
 * Created on Jan 26, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sinergise.java.util.event;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.event.MouseInputAdapter;

/**
 * @author Miha Kadunc (<a href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com</a>
 */
public class InputAdapter extends MouseInputAdapter implements KeyListener {
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed(final KeyEvent e) {}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(final KeyEvent e) {}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped(final KeyEvent e) {}
}
