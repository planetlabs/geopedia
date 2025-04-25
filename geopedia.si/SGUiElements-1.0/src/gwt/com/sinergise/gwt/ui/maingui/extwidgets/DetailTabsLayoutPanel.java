package com.sinergise.gwt.ui.maingui.extwidgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureSelfVisibility;
import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.gwt.ui.i18n.UiConstants;
import com.sinergise.gwt.ui.resources.Theme;

public abstract class DetailTabsLayoutPanel extends SGTabLayoutPanelHistContrib {
	/**
	 * @return false if the detail window was not found, true instead
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	protected boolean selectDetailWindow(String entityId) {
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget w = getWidget(i);
			if (w instanceof SGItemTabPanel<?>) {
				SGItemTabPanel x = (SGItemTabPanel) w;
				Identifier id = ((SGItemTabPanel<?>) w).getItemIdentifier();
				if (id == null)
					continue;
				
				if (id.getLocalID() != null && id.getLocalID().equals(entityId)) {
					((SGItemTabPanel) w).ensureVisible();
					return true;
				} 
			}
		}
		
		return false;
	}
	
	protected boolean removeTemporaryWindows() {
		List<Widget> toRemove = new ArrayList<Widget>();
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget w = getWidget(i);
			if (w instanceof SGItemTabPanel<?>) {
				SGItemTabPanel<?> x = (SGItemTabPanel<?>) w;
				Identifier id = x.getItemIdentifier();
				if (id == null)
					continue;
				
				if (id instanceof EntityIdentifier && ((EntityIdentifier)id).isTemporary()) {
					toRemove.add(x);
				}
			}
		}
		
		for (Widget w : toRemove) remove(w);
		
		return toRemove.size() > 0;
	}
	
	public void setTabTextAndImage(int index, String text, ImageResource img) {
		checkIndex(index);
		Tab    t = tabs.get(index);
		Widget w = t.getWidget();
		if (w instanceof SGCloseableTab) {
			w.setTitle(text);
			((SGCloseableTab) w).setImage(img);
		}
	}
	
	public void add(SGItemTabPanel<?> child, String text, ImageResource imgRes) {
		super.add(child, new SGCloseableTab(this, child, text, imgRes)); 
	}
	
	public void doSearch(Object searchParams) {
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget w = getWidget(i);
			if (w instanceof SimpleSearchResultsTab && !((SimpleSearchResultsTab) w).isPinned()) {
				((SimpleSearchResultsTab)w).doSearch(searchParams);
				if (w instanceof CanEnsureSelfVisibility) {
					((CanEnsureSelfVisibility) w).ensureVisible();
				}
				return;
			}
		}
		
		final SimpleSearchResultsTab srt = createSearchResultsTab();
		super.insert(srt,
			new SGPinnableTab(this, srt, UiConstants.UI_CONSTANTS.subTabResults(), Theme.getTheme().standardIcons().documents()) {
				@Override
				public void onPinned() {
					srt.setPinned(true);
				}
			}, 1);
		srt.ensureVisible();
		srt.doSearch(searchParams);
	}
	
	public abstract void showItemDetail(String itemId);
	
	public abstract SimpleSearchResultsTab createSearchResultsTab();
}
