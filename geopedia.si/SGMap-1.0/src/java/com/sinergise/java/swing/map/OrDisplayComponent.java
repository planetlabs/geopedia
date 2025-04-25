/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sinergise.java.swing.map;

import static java.awt.event.MouseWheelEvent.WHEEL_BLOCK_SCROLL;
import static java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JComponent;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.java.swing.map.layer.misc.RectangleLayer;


/**
 * @author Miha Kadunc (<a
 *         href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com</a>
 */
public class OrDisplayComponent extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int MIN_DRAG_DISTANCE_SQ = 900;
	private OrDisplayBuffer display;
	
	private DisplayCoordinateAdapter coordAdapter;
	
	private InputHandler mouseHandler;
	
	private LinkedHashMap<String, Cursor> cursors = null;
	
	private boolean repaintScheduled = false;
	
	private class DefaultInputHandler extends InputAdapter {
		boolean dragStarted = false;
		Point wDragOrigin;
		Point wCurEnd = null;
		
		Cursor moveCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		
		int dragButton = -1;
		
		RectangleLayer effectLayer;
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.cosylab.ortelius.space.display.InputAdapter#keyTyped(java.awt.event
		 * .KeyEvent)
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == '+') {
				display.setDisplayScale(display.getDisplayScale() / 1.3);
				e.consume();
			} else if (e.getKeyChar() == '-') {
				display.setDisplayScale(display.getDisplayScale() * 1.3);
				e.consume();
			} else if (e.getKeyCode() == KeyEvent.VK_HOME) {
				display.setDisplayedWindow(display.getOrDisplay().getWorldBounds().expandedForSizeRatio(0.1));
				e.consume();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				Point cent = display.getDisplayCenter();
				Envelope world = display.getCoordinateAdapter().worldRect;
				Point newCent = new Point(cent.x - world.getWidth() / 2, cent.y);
				display.setDisplayCenter(newCent);
				e.consume();
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				Point cent = display.getDisplayCenter();
				Envelope world = display.getCoordinateAdapter().worldRect;
				Point newCent = new Point(cent.x + world.getWidth() / 2, cent.y);
				display.setDisplayCenter(newCent);
				e.consume();
			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
				Point cent = display.getDisplayCenter();
				Envelope world = display.getCoordinateAdapter().worldRect;
				Point newCent = new Point(cent.x, cent.y + world.getHeight() / 2);
				display.setDisplayCenter(newCent);
				e.consume();
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				Point cent = display.getDisplayCenter();
				Envelope world = display.getCoordinateAdapter().worldRect;
				Point newCent = new Point(cent.x, cent.y - world.getHeight() / 2);
				display.setDisplayCenter(newCent);
				e.consume();
			}
			if (e.isConsumed()) {
				repaintAll();
			}
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int scrollType = e.getScrollType();
			double oldSc = display.getDisplayScale();
			Point oldLoc = display.getCoordinateAdapter().worldFromPix.point(e.getX(), e.getY());
			
			double fact = 1;
			if (scrollType == WHEEL_BLOCK_SCROLL) {
				display.setDisplayScale(oldSc / Math.pow(1.5, -e.getWheelRotation()));
			} else if (scrollType == WHEEL_UNIT_SCROLL) {
				display.setDisplayScale(oldSc / Math.pow(1.3, -e.getUnitsToScroll()));
			}
			
			// Move center so the cursor is above the same location as before
			DisplayCoordinateAdapter dca = display.getCoordinateAdapter();
			Point newLoc = dca.worldFromPix.point(e.getX(), e.getY());
			Point newCenter = dca.getDisplayCenterWorld();
			display.setDisplayCenter(new Point(newCenter.x - newLoc.x + oldLoc.x, newCenter.y - newLoc.y
				+ oldLoc.y));
			e.consume();
			repaintAll();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			DisplayCoordinateAdapter dca = display.getCoordinateAdapter();
			if (dragButton == -1) {
				wDragOrigin = dca.worldFromPix.point(e.getX(), e.getY());
				dragButton = e.getButton();
			}
			e.consume();
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == dragButton) {
				if (dragButton == 1 && wDragOrigin != null && wCurEnd != null) {
					setDisplayedWindow(Envelope.create(wDragOrigin, wCurEnd));
				} else if (dragButton == 3) {
					removeCursor("defaultMove");
				} else if (dragStarted && dragButton == 1 && wCurEnd == null) {
					mouseHandler.mouseClicked(new MouseEvent((Component) e.getSource(), e.getID(), e
						.getWhen(), e.getModifiers(), e.getX(), e.getY(), 1, false, dragButton));
				}
				wDragOrigin = null;
				dragButton = -1;
				wCurEnd = null;
				dragStarted = false;
				if (effectLayer != null) effectLayer.clear();
				repaintAll();
			}
			e.consume();
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (wDragOrigin != null) {
				DisplayCoordinateAdapter dca = getCoordinateAdapter();
				Point second = dca.worldFromPix.point(e.getX(), e.getY());
				double dx = dca.pixFromWorld.length(second.x - wDragOrigin.x);
				double dy = dca.pixFromWorld.length(second.y - wDragOrigin.y);
				dragStarted = true;
				
				if (dragButton == 3) {
					wCurEnd = new Point(dca.worldCenterX - second.x + wDragOrigin.x, dca.worldCenterY
						- second.y + wDragOrigin.y);
					display.setDisplayCenter(wCurEnd);
					putCursor("defaultMove", moveCursor);
					repaint();
				} else if (dragButton == 1) {
					if (wCurEnd == null && (dx * dx < MIN_DRAG_DISTANCE_SQ || dy * dy < MIN_DRAG_DISTANCE_SQ)) {
						mouseMoved(e);
						return;
					}
					Graphics2D g = (Graphics2D) getGraphics();
					g.setXORMode(Color.WHITE);
					wCurEnd = second;
					if (effectLayer == null) {
						createEffectLayer();
					}
					effectLayer.setRect(wDragOrigin, wCurEnd);
					g.setPaintMode();
				}
			}
			e.consume();
		}
		
		private void createEffectLayer() {
			display.getOrDisplay().addLayer(effectLayer = new RectangleLayer(Color.YELLOW, new Color(255,255,0,48)));
		}
	}
	
	private class InputHandler implements MouseWheelListener, MouseListener, MouseMotionListener,
			KeyListener {
		public List<MouseMotionListener> motionListeners = new ArrayList<MouseMotionListener>();
		
		public List<MouseListener> mouseListeners = new ArrayList<MouseListener>();
		
		public List<MouseWheelListener> wheelListeners = new ArrayList<MouseWheelListener>();
		
		public List<KeyListener> keyListeners = new ArrayList<KeyListener>();
		
		public void keyPressed(KeyEvent e) {
			int i = keyListeners.size() - 1;
			while (i >= 0) {
				(keyListeners.get(i)).keyPressed(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void keyReleased(KeyEvent e) {
			int i = keyListeners.size() - 1;
			while (i >= 0) {
				(keyListeners.get(i)).keyReleased(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void keyTyped(KeyEvent e) {
			int i = keyListeners.size() - 1;
			while (i >= 0) {
				(keyListeners.get(i)).keyTyped(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mouseWheelMoved(MouseWheelEvent e) {
			int i = wheelListeners.size() - 1;
			while (i >= 0) {
				((MouseWheelListener) mouseListeners.get(i)).mouseWheelMoved(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mousePressed(MouseEvent e) {
			requestFocus();
			int i = mouseListeners.size() - 1;
			while (i >= 0) {
				(mouseListeners.get(i)).mousePressed(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			int i = mouseListeners.size() - 1;
			while (i >= 0) {
				(mouseListeners.get(i)).mouseReleased(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			int i = motionListeners.size() - 1;
			while (i >= 0) {
				(motionListeners.get(i)).mouseDragged(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mouseClicked(MouseEvent e) {
			int i = mouseListeners.size() - 1;
			while (i >= 0) {
				(mouseListeners.get(i)).mouseClicked(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mouseEntered(MouseEvent e) {
			int i = mouseListeners.size() - 1;
			while (i >= 0) {
				(mouseListeners.get(i)).mouseEntered(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mouseExited(MouseEvent e) {
			int i = mouseListeners.size() - 1;
			while (i >= 0) {
				(mouseListeners.get(i)).mouseExited(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
		
		public void mouseMoved(MouseEvent e) {
			int i = motionListeners.size() - 1;
			while (i >= 0) {
				(motionListeners.get(i)).mouseMoved(e);
				if (e.isConsumed()) return;
				i--;
			}
		}
	}
	
	public OrDisplayComponent(OrDisplay disp) {
		// this.display=disp;
		DefaultInputHandler dmh = new DefaultInputHandler();
		display = new OrDisplayBuffer(disp);
		mouseHandler = new InputHandler();
		cursors = new LinkedHashMap<String, Cursor>();
		cursors.put("DEFAULT", getCursor());
		super.addMouseWheelListener(mouseHandler);
		super.addMouseListener(mouseHandler);
		super.addMouseMotionListener(mouseHandler);
		super.addKeyListener(mouseHandler);
		addMouseListener(dmh);
		addMouseMotionListener(dmh);
		addMouseWheelListener(dmh);
		addKeyListener(dmh);
		setFocusable(true);
		setFocusCycleRoot(true);
		requestFocus();
		
		display.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (OrDisplayBuffer.DISPLAY_CHANGE_ACTION.equals(e.getActionCommand())) {
					repaint();
				}
			}
		});
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension sz = getSize();
				display.setDisplaySize(new DimI(sz.width, sz.height));
				repaint();
			}
		});
	}
	
	public void setDisplayedWindow(Envelope worldRect) {
		System.out.println("Setting window "+worldRect);
		display.setDisplayedWindow(worldRect);
	}
	
	public void zoomAll() {
		setDisplayedWindow(display.getOrDisplay().getWorldBounds());
	}
	
	public void zoomBy(int i) {
		display.setDisplayScale(display.getDisplayScale()*Math.pow(2, -i));
	}
	
	@Override
	public synchronized void addKeyListener(KeyListener l) {
		mouseHandler.keyListeners.add(l);
	}
	
	@Override
	public synchronized void removeKeyListener(KeyListener l) {
		mouseHandler.keyListeners.remove(l);
	}
	
	@Override
	public void addMouseMotionListener(MouseMotionListener l) {
		mouseHandler.motionListeners.add(l);
	}
	
	@Override
	public void removeMouseMotionListener(MouseMotionListener l) {
		mouseHandler.motionListeners.remove(l);
	}
	
	@Override
	public synchronized void addMouseListener(MouseListener l) {
		mouseHandler.mouseListeners.add(l);
	}
	
	@Override
	public synchronized void removeMouseListener(MouseListener l) {
		mouseHandler.mouseListeners.remove(l);
	}
	
	@Override
	public synchronized void addMouseWheelListener(MouseWheelListener l) {
		mouseHandler.wheelListeners.add(l);
	}
	
	@Override
	public synchronized void removeMouseWheelListener(MouseWheelListener l) {
		mouseHandler.wheelListeners.remove(l);
	}
	
	@Override
	protected void paintChildren(Graphics g) {
	// Do not do anything
	}
	
	@Override
	protected void paintBorder(Graphics g) {
	// Do not do anything
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		repaintScheduled = false;
		display.setDisplaySize(getDisplaySize());
		coordAdapter = display.paintContents((Graphics2D) g);
	}
	
	public DimI getDisplaySize() {
		return new DimI(getWidth(), getHeight());
	}
	
	/**
	 * @return Returns the coordAdapter.
	 */
	public DisplayCoordinateAdapter getCoordinateAdapter() {
		if (coordAdapter == null) {
			display.setDisplaySize(getDisplaySize());
			coordAdapter = display.getCoordinateAdapter();
		}
		return coordAdapter;
	}
	
	public void putCursor(String key, Cursor c) {
		if (key == null) { return; }
		cursors.put(key, c);
		if (getCursor() != c) {
			super.setCursor(c);
		}
	}
	
	public void removeCursor(Object key) {
		cursors.remove(key);
		Iterator<Cursor> it = cursors.values().iterator();
		Object c = null;
		while (it.hasNext()) {
			c = it.next();
		}
		if (getCursor() != c) {
			super.setCursor((Cursor) c);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#setCursor(java.awt.Cursor)
	 */
	@Override
	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
	}
	
	@Override
	public void repaint() {
		if (!repaintScheduled) {
			repaintScheduled = true;
			super.repaint();
		}
	}
	
	public void repaintAll() {
		display.repaintAll(false);
	}
	
	public OrDisplayBuffer getDisplayBuffer() {
		return display;
	}
	
	public OrDisplay getDisplay() {
		return display.getOrDisplay();
	}
}
