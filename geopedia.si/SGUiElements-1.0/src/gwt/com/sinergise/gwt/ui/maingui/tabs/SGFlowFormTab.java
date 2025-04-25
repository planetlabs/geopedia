package com.sinergise.gwt.ui.maingui.tabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Tooltips;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.core.CanClearEditableFields;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGImageLabel;
import com.sinergise.gwt.ui.resources.Theme;

public class SGFlowFormTab extends SGFlowPanel {
	protected static Widget createTitleBar(Widget... widgets) {
		FlowPanel fp = new FlowPanel();
		fp.setStyleName("sgFlowFormTabHeader");
		for (Widget w : widgets) {
			fp.add(w);
		}
		return fp;
	}
	
	public static class ClearAnchor extends ImageAnchor {
		public ClearAnchor(final CanClearEditableFields cp) {
			setImageRes(Theme.getTheme().standardIcons().clear()); 
			setTitle(Tooltips.INSTANCE.clearForm());
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					cp.clearEditableFields();
				}
			});
		}
	}
	
	public static class LabelWithImage extends SGImageLabel {
		
		public LabelWithImage(String title, ImageResource image) {
			super(title, new Image(image));
			sinkEvents(Event.ONMOUSEOUT | Event.ONMOUSEOVER);
		}
	}
	
	public static class HelpAnchor extends ImageAnchor {
		public HelpAnchor(final String token) {
			// final String url = HelpTokens.getHelpUrl(token);
			final String url = "help?" + token;
			setImageRes(Theme.getTheme().standardIcons().help()); 
			setHref(url);
			setTarget("_blank");
			setTitle(Tooltips.INSTANCE.showHelp());
		}
	}
}
