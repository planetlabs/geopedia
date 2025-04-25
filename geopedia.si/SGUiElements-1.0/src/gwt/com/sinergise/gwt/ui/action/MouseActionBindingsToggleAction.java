package com.sinergise.gwt.ui.action;

import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.core.MouseHandler.MouseActionBinding;

public class MouseActionBindingsToggleAction extends ToggleAction {
	private final MouseHandler mh;
	private final MouseActionBinding<?>[] bindWhenOn;
	
	public MouseActionBindingsToggleAction(String name, MouseHandler mh, MouseActionBinding<?>[] bindingsWhenOn, boolean createSelected) {
		super(name);
		this.mh = mh;
		this.bindWhenOn = bindingsWhenOn;
		setSelected(createSelected);
	}

	@Override
	protected void selectionChanged(boolean newSelected) {
		if (newSelected) {
			addBindings();
		} else {
			removeBindings();
		}
	}

	protected void removeBindings() {
		for (MouseActionBinding<?> b : bindWhenOn) {
			mh.deregisterActionBinding(b);
		}
	}
	protected void addBindings() {
		for (MouseActionBinding<?> b : bindWhenOn) {
			mh.registerActionBinding(b);
		}
	}
}
