package com.sinergise.gwt.ui.maingui.extwidgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.sinergise.gwt.ui.StyleConsts;

public class SGGrid extends Grid {
	
	public class SGGridRow extends UIObject {
		final int row;
		
		public SGGridRow(int row) {
			this.row = row;
			setElement(getRowFormatter().getElement(row));
		}

		public void setVerticalAlign(VerticalAlignmentConstant align) {
			getRowFormatter().setVerticalAlign(row, align);
		}

		public TableRowElement getTR() {
			return getElement().cast();
		}
	}

	private Map<Integer, List<ClickHandler>> rowClickHandlers = new HashMap<Integer, List<ClickHandler>>();
	private Map<Integer, List<DoubleClickHandler>> rowDblClickHandlers = new HashMap<Integer, List<DoubleClickHandler>>();
	
	public SGGrid() {
		super();
		init();
	}
	
	public SGGrid(int rows, int columns) {
		super(rows, columns);
		init();
	}
	
	public SGGridRow getGridRow(int row) {
		return new SGGridRow(row);
	}
	
	private void init() {
		registerListeners();
		addStyleName(StyleConsts.TABLE);
	}
	
	protected void registerListeners() {
		addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int row = getRowForEvent(event);
				List<ClickHandler> rowHandlers = rowClickHandlers.get(Integer.valueOf(row));
				if (rowHandlers != null) {
					for (ClickHandler handler : rowHandlers) {
						handler.onClick(event);
					}
				}
			}
		});
		
		addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				int row = getRowForEvent(event);
				List<DoubleClickHandler> rowHandlers = rowDblClickHandlers.get(Integer.valueOf(row));
				if (rowHandlers != null) {
					for (DoubleClickHandler handler : rowHandlers) {
						handler.onDoubleClick(event);
					}
				}
			}
		});
	}
	
	private int getRowForEvent(MouseEvent<?> event) {
		Element td = getEventTargetCell(Event.as(event.getNativeEvent()));
		if (td == null) {
			return -1;
		}

		return TableRowElement.as(td.getParentElement()).getSectionRowIndex();
	}
	
	public void addRowClickHandler(int row, ClickHandler handler) {
		Integer r = Integer.valueOf(row);
		List<ClickHandler> handlers = rowClickHandlers.get(r);
		if (handlers == null) {
			rowClickHandlers.put(r, handlers = new ArrayList<ClickHandler>());
		}
		handlers.add(handler);
	}
	
	public void addRowDoubleClickHandler(int row, DoubleClickHandler handler) {
		Integer r = Integer.valueOf(row);
		List<DoubleClickHandler> handlers = rowDblClickHandlers.get(r);
		if (handlers == null) {
			rowDblClickHandlers.put(r, handlers = new ArrayList<DoubleClickHandler>());
		}
		handlers.add(handler);
	}
	
}
