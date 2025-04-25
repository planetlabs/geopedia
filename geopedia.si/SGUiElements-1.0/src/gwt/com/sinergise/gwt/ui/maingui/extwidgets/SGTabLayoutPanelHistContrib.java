package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.util.history.HistoryManager;
import com.sinergise.gwt.util.history.HistoryStateContributor;

public class SGTabLayoutPanelHistContrib extends SGTabLayoutPanel {
	public SGTabLayoutPanelHistContrib() {
		this(HistoryManager.getInstance());
	}
	
	public SGTabLayoutPanelHistContrib(final HistoryManager manager) {
		addSelectionHandler(new SelectionHandler<Integer>() {
			@Deprecated
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (event.getSelectedItem() != null) {
					Widget w = getWidget(event.getSelectedItem().intValue());
					if (w != null && w instanceof HistoryStateContributor)
						((HistoryStateContributor)w).contribute(manager);
				}
			}
		});
	}
}
