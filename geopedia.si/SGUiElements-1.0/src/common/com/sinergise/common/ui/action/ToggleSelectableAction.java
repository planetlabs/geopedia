package com.sinergise.common.ui.action;

import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;

public class ToggleSelectableAction extends ToggleAction {
	
	private final Selectable selectable;
	
	public ToggleSelectableAction(String name, Selectable selectable) {
		super(name);
		this.selectable = selectable;
		setSelected(selectable.isSelected());
		
		selectable.addToggleListener(new ToggleListener() {
			@Override
			public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
				setSelected(newOn);
			}
		});
	}

	@Override
	protected void selectionChanged(boolean newSelected) {
		selectable.setSelected(newSelected);
	}

}
