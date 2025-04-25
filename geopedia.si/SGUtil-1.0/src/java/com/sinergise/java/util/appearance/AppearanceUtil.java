package com.sinergise.java.util.appearance;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

public class AppearanceUtil {
	
	public static void fixTextFieldWidth(final JTextField fld, final int width) {
		fixComponentWidth(fld, width);
	}
	
	public static void fixComponentWidth(final JComponent comp, final int width) {
		final Dimension dim = comp.getMinimumSize();
		dim.width = width;
		comp.setMaximumSize(dim);
		comp.setPreferredSize(dim);
	}
	
	public static void fixButtonWidth(final AbstractButton button) {
		if (button == null) {
			return;
		}
		
		final Insets i = button.getInsets();
		if (i == null) {
			return;
		}
		final Border b = button.getBorder();
		if (b == null || b instanceof EmptyBorder) {
			button.setBorder(BorderFactory.createEmptyBorder(i.top, i.top, i.top, i.top));
		}
	}
	
	public static void syncButtonSize(final JButton... b) {
		final Dimension min = new Dimension(0, 0);
		for (final JButton element : b) {
			min.width = Math.max(element.getPreferredSize().width, min.width);
			min.height = Math.max(element.getPreferredSize().height, min.height);
		}
		for (final JButton element : b) {
			element.setPreferredSize(min);
			element.setMaximumSize(min);
			element.setMinimumSize(min);
		}
	}
	
	public static void syncComponentSize(final JComponent... c) {
		final Dimension total = new Dimension(0, 0);
		for (final JComponent element : c) {
			total.width += element.getPreferredSize().width;
			total.height += element.getPreferredSize().height;
		}
		final Dimension dim = new Dimension(total.width / c.length, total.height / c.length);
		for (final JComponent element : c) {
			element.setPreferredSize(dim);
			element.setMaximumSize(dim);
			// c[i].setMinimumSize(dim);
		}
	}
	
	public static void fixTableColumn(final TableColumn col, final int width) {
		col.setPreferredWidth(width);
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setResizable(false);
	}
	
	public static void setToolbarButStyle(final JButton but) {
		but.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		but.setContentAreaFilled(false);
		but.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	
	public static void fixBoxHorizontalAlign(Box mainBox, float alignment) { 
		for (int i = 0; i < mainBox.getComponentCount(); i++) { 
			JComponent c = (JComponent)mainBox.getComponent(i); 
			c.setAlignmentX(alignment); 
		} 
	}
	
	public static void fixBoxVerticalAlign(Box mainBox, float alignment) { 
		for (int i = 0; i < mainBox.getComponentCount(); i++) { 
			JComponent c = (JComponent)mainBox.getComponent(i); 
			c.setAlignmentY(alignment); 
		} 
	}
}
