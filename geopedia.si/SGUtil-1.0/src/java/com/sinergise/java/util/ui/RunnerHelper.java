/*
 * @@COPYRIGHT@@
 */
package com.sinergise.java.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * @author Miha Kadunc (miha.kadunc@cosylab.com)
 * @version @@VERSION@@
 */
public class RunnerHelper {
	public static interface GraphicsDrawer {
		void draw(Graphics2D g, int w, int h);
	} 
	
	public static void runFrame(final Window frame, final int width, final int height) {
		try {
			DisplayMode curMode = frame.getGraphicsConfiguration().getDevice().getDisplayMode();
			if (curMode.getWidth()<width || curMode.getHeight() < height) {
				frame.setSize(curMode.getWidth(), curMode.getHeight());
				if (frame instanceof Frame) {
					((Frame)frame).setExtendedState(Frame.MAXIMIZED_BOTH);
				}
			} else {
				frame.setSize(width, height);
			}
			ComponentPositioner.centerOnScreen(frame);
			frame.setVisible(true);
		} catch(final Exception e) {
			System.out.println(">>> Exception in main");
			e.printStackTrace();
		}
	}
	
	public static JFrame runComponent(final Container c, final int width, final int height) {
		try {
			
			final JFrame frame = new JFrame(c.getName());
			c.addPropertyChangeListener("name", new PropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					frame.setTitle(String.valueOf(evt.getNewValue()));
				}
			});
			frame.setContentPane(c);
			final int w = width + frame.getInsets().left + frame.getInsets().right;
			final int h = height + frame.getInsets().bottom + frame.getInsets().top;
			runFrame(frame, w, h);
			return frame;
		} catch(final Exception e) {
			System.out.println(">>> Exception in main");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void runComponent(final Component c, final int width, final int height) {
		try {
			
			final JFrame frame = new JFrame(c.getName());
			frame.getContentPane().add(c);
			final int w = width + frame.getInsets().left + frame.getInsets().right;
			final int h = height + frame.getInsets().bottom + frame.getInsets().top;
			runFrame(frame, w, h);
		} catch(final Exception e) {
			System.out.println(">>> Exception in main");
			e.printStackTrace();
		}
	}
	
	public static void runDrawer(final GraphicsDrawer gd, int w, int h) {
		final JComponent comp = new JComponent() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				((Graphics2D)g).setBackground(Color.WHITE);
				g.clearRect(0, 0, getWidth(), getHeight());
				gd.draw((Graphics2D)g, getWidth(), getHeight());
			}
		};
		runComponent(comp, w, h);
		Timer tmr = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comp.repaint();
			}
		});
		tmr.start();
	}
}
