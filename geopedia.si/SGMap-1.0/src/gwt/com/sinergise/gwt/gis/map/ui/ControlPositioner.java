/*
 *
 */
package com.sinergise.gwt.gis.map.ui;

import com.google.gwt.user.client.ui.Widget;

public interface ControlPositioner {
    void showControl(Widget wgt);
    void hideControl(Widget wgt);
    void ensureVisible(Widget wgt);
}
