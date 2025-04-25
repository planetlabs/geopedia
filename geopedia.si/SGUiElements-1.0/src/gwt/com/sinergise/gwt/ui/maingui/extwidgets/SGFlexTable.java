package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

public class SGFlexTable extends FlexTable implements HasMouseOutHandlers, HasMouseOverHandlers, HasMouseMoveHandlers {

	public class SGFlexTableCell extends HTMLTable.Cell {
		public SGFlexTableCell(int row, int col) {
			super(row, col);
		}
		public void setText(String text) {
			SGFlexTable.this.setText(getRowIndex(), getCellIndex(), text);
		}
		public void setWidget(Widget wgt) {
			SGFlexTable.this.setWidget(getRowIndex(), getCellIndex(), wgt);
		}
		public void setHTML(String html) {
			SGFlexTable.this.setHTML(getRowIndex(), getCellIndex(), html);
		}
		public void setWidth(String width) {
			SGFlexTable.this.getCellFormatter().setWidth(getRowIndex(), getCellIndex(), width);
		}
		public void addStyleName(String styleName) {
			SGFlexTable.this.getCellFormatter().addStyleName(getRowIndex(), getCellIndex(), styleName);
		}
	}
	
	public SGFlexTableCell createCell(int row, int col) {
		return new SGFlexTableCell(row, col);
	}
	
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
	    return addDomHandler(handler, MouseOverEvent.getType());
	}
	
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
	    return addDomHandler(handler, MouseOutEvent.getType());
	}
	
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		return addDomHandler(handler, MouseMoveEvent.getType());
	}
	
	public SGFlexTableCell getHTMLCellForEvent(MouseEvent<?> event) {
		Element td = getEventTargetCell(Event.as(event.getNativeEvent()));
		if (td == null) {
			return null;
		}

		int row = TableRowElement.as(td.getParentElement()).getSectionRowIndex();
		int column = TableCellElement.as(td).getCellIndex();
		return new SGFlexTableCell(row, column);
	}
}
