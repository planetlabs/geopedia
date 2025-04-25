package com.sinergise.gwt.ui.maingui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.StyleConsts;
import com.sinergise.gwt.util.html.CSS;

/**
 * @author tcerovski
 *
 */
public class SimpleLoadingPanel extends SimplePanel implements ILoadingWidget {
	private static SimpleLoadingPanel INSTANCE = null;
	public static final SimpleLoadingPanel getDefault() {
		if (INSTANCE == null) {
			INSTANCE = new SimpleLoadingPanel();
		}
		return INSTANCE;
	}
	
	public static final int Z_INDEX = 1000;
	
	public SimpleLoadingPanel(Widget parent) {
		this();
		DOM.appendChild(parent.getElement(), getElement());
	}
	
	public SimpleLoadingPanel() {
		setStylePrimaryName(StyleConsts.SIMPLE_LOADING_PANEL);
		CSS.position(this, CSS.POS_ABSOLUTE);
		CSS.leftTop(this, 0, 0);
		CSS.size(this, "100%", "100%");
		CSS.zIndex(this, Z_INDEX);
		
		hideLoading();
	}


	/**
	 * @Deprecated Use showLoading() instead
	 */

	@Deprecated
	public void showLoading(int index) {
		showLoading();
	}

	public void showLoading() {
		setVisible(true);
	}

	public void hideLoading() {
		setVisible(false);
	}

}
