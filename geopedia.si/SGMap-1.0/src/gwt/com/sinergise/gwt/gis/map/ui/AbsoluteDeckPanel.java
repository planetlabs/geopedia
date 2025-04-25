/*
 *
 */
package com.sinergise.gwt.gis.map.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.util.html.CSS;

public class AbsoluteDeckPanel extends ComplexPanel implements RequiresResize, ProvidesResize {
	public static class FocusableDeckPanel extends FocusPanel {
		final AbsoluteDeckPanel panel;
		public FocusableDeckPanel() {
			super();
			setWidget(panel = new AbsoluteDeckPanel());
		}
		
		public Widget getChildWidget(int i) {
			return panel.getWidget(i);
		}
		
		
		public int getWidgetCount() {
			return panel.getWidgetCount();
		}

		public void insertChildWidget(Widget w, int beforeIndex) {
			panel.insert(w, beforeIndex);
		}
		
		public void removeChildWidget(Widget w) {
			panel.remove(w);
		}
	}
    public AbsoluteDeckPanel() {
        super();
        setElement(DOM.createDiv());
        CSS.position(getElement(), CSS.POS_RELATIVE);
    }
    
    @Override
	public void add(Widget w) {
        insert(w, getWidgetCount());
    }
    
    public void insert(Widget w, int beforeIndex) {
        if ((beforeIndex < 0) || (beforeIndex > getWidgetCount())) {
            throw new IndexOutOfBoundsException();
        }
        insert(w, getElement(), beforeIndex, true);
        
        CSS.leftTop(w, 0, 0);
        CSS.position(w, CSS.POS_ABSOLUTE);
        w.setSize(CSS.PERC_100, CSS.PERC_100);
    }
    
    @Override
	public Widget getWidget(int index) {
        return getChildren().get(index);
    }

    @Override
	public int getWidgetCount() {
        return getChildren().size();
    }

    @Override
	public int getWidgetIndex(Widget child) {
        return getChildren().indexOf(child);
    }

    @Override
	public boolean remove(int index) {
        if (index<0 || index>=getWidgetCount()) return false;
        Widget wgt=getWidget(index);
        if (wgt==null) return false;
        return remove(wgt);
    }

    @Override
	public boolean remove(Widget w) {
      if (!super.remove(w)) {
        return false;
      }
      return true;
    }

    public boolean contains(Widget w) {
        return getWidgetIndex(w)>=0;
    }
    
    @Override
	public void onResize() {
		for (Widget child : getChildren()) {
			if (child instanceof RequiresResize) {
				((RequiresResize)child).onResize();
			}
		}
	}
}
