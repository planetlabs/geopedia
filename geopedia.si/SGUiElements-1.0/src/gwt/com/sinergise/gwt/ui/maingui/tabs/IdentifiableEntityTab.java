package com.sinergise.gwt.ui.maingui.tabs;

import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.IdentifiableEntity;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public abstract class IdentifiableEntityTab<T extends IdentifiableEntity> extends SGFlowPanel {

	public abstract String getTabTitle();
	
	public abstract T getEntity();
	
	public abstract EntityIdentifier getEntityId();
	
}
