package com.sinergise.gwt.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public class WidgetsBox extends Composite {
	
	protected final SGFlowPanel wPanel;
	
	protected final List<IndexedWidget> widgets = new ArrayList<IndexedWidget>();
	private int maxWidgetIndex = 0;

	public WidgetsBox() {
		initWidget(wPanel = new SGFlowPanel());
	}

	public void addWidget(IsWidget w) {
		addWidget(maxWidgetIndex+1, w);
	}
	
	public void addWidget(int idx, IsWidget w) {
		widgets.add(new IndexedWidget(w, idx));
		if (idx > maxWidgetIndex) {
			maxWidgetIndex = idx;
		}
		
		if (isAttached()) {
			render();
		}
	}
	
	@Override
	protected void onAttach() {
		render();
		super.onAttach();
	}
	
	protected void render() {
		wPanel.clear();
		Collections.sort(widgets);
		for (IndexedWidget w : widgets) {
			wPanel.add(w);
		}
	}
	
	private static class IndexedWidget implements Comparable<IndexedWidget>, IsWidget {
		final int idx;
		final IsWidget w;
		
		IndexedWidget(IsWidget w, int idx) {
			this.idx = idx;
			this.w = w;
		}
		
		@Override
		public int compareTo(IndexedWidget o) {
			return idx-o.idx;
		}
		
		@Override
		public Widget asWidget() {
			return w.asWidget();
		}
	}
	
	public final static class GroupSepparator extends Breaker {
		public GroupSepparator() {
			setStyleName("widgetBoxSeparator");
		}
	}
}
