package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.view.client.ProvidesKey;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.i18n.UiConstants;
import com.sinergise.gwt.ui.i18n.UiMessages;
import com.sinergise.gwt.ui.resources.Theme;

public class SGDataGrid<T> extends DataGrid<T> {

	private NotificationPanel notification;
	private boolean autoResize = false;

	public SGDataGrid(int pageSize, ProvidesKey<T> providesKey) {
		super(pageSize, Theme.getGrid(), providesKey, new NotificationPanel(UiConstants.UI_CONSTANTS.loading(),
			MessageType.PROGRESS));

		addStyleName("sgDataGrid");
		setEmptyTableWidget(notification = new NotificationPanel(UiMessages.UI_MESSAGES.tableNoResult(), MessageType.INFO)); // TODO: i18n
		setHeight("100%");
		
		

		addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {
			@Override
			public void onLoadingStateChanged(final LoadingStateChangeEvent event) {
				final Scheduler scheduler = Scheduler.get();
				scheduler.scheduleDeferred(new ScheduledCommand() {
					public void execute() {
						scheduler.scheduleDeferred(new ScheduledCommand() {
							public void execute() {
								onResize();
							}
						});
					}
				});
			}
		});
	}

	public SGDataGrid(int i) {
		this(i, null);
	}

	public SGDataGrid() {
		this(100);
	}

	public void setNotification(String msg, MessageType type) {
		notification.setMsg(type, msg);
	}

	@Override
	public void setHeight(String height) {
		autoResize = height.equals("auto");
		super.setHeight(height);
	}

	public boolean isAutoresize() {
		return autoResize;
	}

	public void setAutoresize(boolean autoResize) {
		this.autoResize = autoResize;
	}

	@Override
	public void redraw() {
		super.redraw();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				onResize();
			}
		});
	}

	@Override
	public void onResize() {
		super.onResize();

		if (autoResize) {

			int gridH = 0;
			Element headerElement = getTableHeadElement().getParentElement();
			if (getRowCount() == 0) {
				gridH += headerElement.getOffsetHeight();
				gridH += getEmptyTableWidget().getParent().getOffsetHeight();
			} else {
				Element contentElement = getTableBodyElement().getParentElement();
				if (contentElement != null && headerElement != null) {
					gridH += contentElement.getOffsetHeight();
					gridH += headerElement.getOffsetHeight();
					gridH += 15; //safety for appearing scrolls 
				}
			}

			super.setHeight(gridH + "px");
		}
	}
}
