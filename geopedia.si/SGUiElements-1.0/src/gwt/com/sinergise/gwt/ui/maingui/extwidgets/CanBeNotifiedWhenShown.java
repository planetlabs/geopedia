package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.user.client.ui.Widget;

public interface CanBeNotifiedWhenShown {
	void onShownByAncestor(Widget parent);
	void onHiddenByAncestor(Widget parent);
}
