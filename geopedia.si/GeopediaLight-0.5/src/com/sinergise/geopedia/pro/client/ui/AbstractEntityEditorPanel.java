package com.sinergise.geopedia.pro.client.ui;

import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public abstract class AbstractEntityEditorPanel<T>  extends SGFlowPanel {
	
	public abstract void loadEntity(T entity);
	public abstract boolean saveEntity(T entity);
	public abstract boolean validate();

	public static class HolderPanel extends SGFlowPanel {
		private boolean mandatory = false;
		public void setMandatory(boolean mandatory) {
			if (this.mandatory==mandatory) 
				return;
			this.mandatory=mandatory;
			if (mandatory) {
				addStyleName("mandatory");
				setTitle(ProConstants.INSTANCE.mandatory());
			} else {
				removeStyleName("mandatory");
				setTitle("");
			}
		}
		public boolean isMandatory() {
			return mandatory;
		}
		public void missingMandatory(boolean on) {
			if (on) {
				addStyleName("missingMandatory");
			} else {
				removeStyleName("missingMandatory");
			}
		}		
	}
	protected static HolderPanel createHolderPanel(String label, Widget widget, boolean isMandatory) {
		HolderPanel pnl = createHolderPanel(label, widget);
		pnl.setMandatory(isMandatory);
		return pnl;
	}

	protected static HolderPanel createHolderPanel(String label, Widget widget) {
		HolderPanel holder = new HolderPanel();
		holder.setStyleName("holderPanel");
		holder.add(new InlineLabel(label));
		holder.add(widget);
		return holder;
	}

}
