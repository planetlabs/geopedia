package com.sinergise.geopedia.client.core.events;


import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.core.entities.Table;

public class OpenSidebarPanelEvent extends Event<OpenSidebarPanelEvent.Handler> {

	public enum SidebarPanelType {ROUTING_PANEL, PROTOOLS_PANEL, CONTENT_TAB, INFO_TAB, PERSONAL_TAB, TABLE_EDITOR_TAB, 
		CUSTOM_PANEL, RESULTS_PANEL};
	
	public interface Handler {
		void onOpenSidebarPanel(OpenSidebarPanelEvent event);
		void onCloseSidebarPanel(OpenSidebarPanelEvent event);
		void onOpenCustomSidebarPanel(ActivatableTabPanel panel);
	}

	private SidebarPanelType type;
	private boolean forceRefresh = false;
	private boolean openEditor = false;
	private Table table = null;
	private ActivatableTabPanel panel = null;
	private boolean close = false;
	// do not focus tab
	private boolean noFocus = false;
	
	
	public static OpenSidebarPanelEvent openCustomPanel(ActivatableTabPanel panel) {
		OpenSidebarPanelEvent ospe = new OpenSidebarPanelEvent(SidebarPanelType.CUSTOM_PANEL);
		ospe.panel=panel;
		return ospe;
	}
	public OpenSidebarPanelEvent(SidebarPanelType type) {
		this(type,false);
	}

	public OpenSidebarPanelEvent(SidebarPanelType type, boolean forceRefresh) {
		this.type=type;
		this.forceRefresh=forceRefresh;
	}
	
	private static final Type<OpenSidebarPanelEvent.Handler> TYPE =
		        new Type<OpenSidebarPanelEvent.Handler>();
	
	public static HandlerRegistration register(EventBus eventBus, OpenSidebarPanelEvent.Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	} 

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}
	
	public OpenSidebarPanelEvent setTable(Table table) {
		this.table=table;
		return this;
	}
	
	public OpenSidebarPanelEvent setClose() {
		this.close = true;
		return this;
	}
	
	public OpenSidebarPanelEvent setNoFocus() {
		this.noFocus = true;
		return this;
	}
	
	public Table getTable() {
		return table;
	}

	@Override
	protected void dispatch(Handler handler) {
		if (close) {
			if (type!=SidebarPanelType.CUSTOM_PANEL) {
				handler.onCloseSidebarPanel(this);
			}
		} else {
			if (type==SidebarPanelType.CUSTOM_PANEL) {
				handler.onOpenCustomSidebarPanel(panel);
			} else {
				handler.onOpenSidebarPanel(this);
			}
		}
		
	}
	
	public boolean isRefreshForced() {
		return forceRefresh;
	}
	
	public OpenSidebarPanelEvent openEditor() {
		this.openEditor = true;
		return this;
	}
	
	public SidebarPanelType getType() {
		return type;
	}
	
	public boolean isNoFocus() {
		return noFocus;
	}
	
	public boolean hasOpenEditor() {
		return openEditor;
	}

}
