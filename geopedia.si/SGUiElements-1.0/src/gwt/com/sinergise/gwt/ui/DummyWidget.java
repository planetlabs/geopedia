/*
 *
 */
package com.sinergise.gwt.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

public class DummyWidget extends Widget {
    public DummyWidget() {
        setElement(DOM.createDiv());
    }
    public DummyWidget(int w, int h) { 
        this();
        setStyleName(StyleConsts.DUMMY_WIDGET);
        setPixelSize(w,h);
    }
}
