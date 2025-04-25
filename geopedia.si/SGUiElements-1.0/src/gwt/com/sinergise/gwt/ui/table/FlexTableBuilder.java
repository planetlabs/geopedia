/**
 * 
 */
package com.sinergise.gwt.ui.table;

import static com.sinergise.common.util.lang.TypeUtil.boxI;
import static com.sinergise.common.util.lang.TypeUtil.unboxI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.StyleConsts;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlexTable;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlexTable.SGFlexTableCell;
import com.sinergise.gwt.util.html.CSS;


/**	
 * @author tcerovski
 */
public class FlexTableBuilder implements HasHandlers {
	
	public class FlexTableRow extends UIObject {
		public FlexTableRow(int row) {
			setElement(table.getRowFormatter().getElement(row));
		}

		public void setVerticalAlign(VerticalAlignmentConstant align) {
			table.getRowFormatter().setVerticalAlign(row, align);
		}

		public TableRowElement getTR() {
			return getElement().cast();
		}
	}
	
	public static final String DEFAULT_STYLE_TABLE = StyleConsts.TABLE;
	public static final String DEFAULT_STYLE_EVEN_ROW_SUFFIX = "even";
	public static final String DEFAULT_STYLE_ODD_ROW_SUFFIX = "odd";
	public static final String DEFAULT_STYLE_BOTTOM_ROW_SUFFIX = "bottom";
	public static final String DEFAULT_STYLE_TITLE_SUFFIX = "title";
	public static final String DEFAULT_STYLE_FIELD_VALUE_SUFFIX = "value";
	public static final String DEFAULT_STYLE_FIELD_LABEL_SUFFIX = "label";
	public static final String DEFAULT_STYLE_FIELD_WIDGET_SUFFIX = "fieldWidget";
	public static final String MANDATORY_HTML = "<font color=\"red\"><b>*</b></font> ";

	protected boolean tableBuilt;
	protected SGFlexTable table;
	protected int row = -1;
	protected int col = -1;
	private boolean hasBottomRow = false;
	private boolean processEvents = false;
	private HandlerManager handlerManager;
	
	protected String style = DEFAULT_STYLE_TABLE;
	protected String styleEvenRowSuffix = DEFAULT_STYLE_EVEN_ROW_SUFFIX;
	protected String styleOddRowSuffix = DEFAULT_STYLE_ODD_ROW_SUFFIX;
	protected String styleBottomRowSuffix = DEFAULT_STYLE_BOTTOM_ROW_SUFFIX;
	protected String styleTitleSuffix = DEFAULT_STYLE_TITLE_SUFFIX;
	protected String styleFieldValueSuffix = DEFAULT_STYLE_FIELD_VALUE_SUFFIX;
	protected String styleFieldLabelSuffix = DEFAULT_STYLE_FIELD_LABEL_SUFFIX;
	protected String styleFieldWidgetSuffix = DEFAULT_STYLE_FIELD_WIDGET_SUFFIX;
	
	private Map<Integer, List<ClickHandler>> rowClickHandlers = new HashMap<Integer, List<ClickHandler>>();
	private Map<Integer, List<DoubleClickHandler>> rowDblClickHandlers = new HashMap<Integer, List<DoubleClickHandler>>();
	private Map<Integer, List<MouseOverHandler>> rowMouseOverHandlers = new HashMap<Integer, List<MouseOverHandler>>();
	private Map<Integer, List<MouseOutHandler>> rowMouseOutHandlers = new HashMap<Integer, List<MouseOutHandler>>();
	
	public FlexTableBuilder() {
		this(DEFAULT_STYLE_TABLE);
	}
	
	public FlexTableBuilder(String tableStyle) {
		super();
		handlerManager = new HandlerManager(this);
		table = new SGFlexTable();
		setTableStyle(tableStyle);
		newRow();
	}
	
	public SGFlexTable getTable() {
		return table;
	}
	
	public FlexTable buildTable() {
		return buildTable(false);
	}
	
	public FlexTable buildTable(boolean addBottomRow) {
		if (tableBuilt) {
			return table;
		}
		
		finalizeRow();
		if(addBottomRow) {
			hasBottomRow = true;
			newRow();
			table.setWidget(row, ++col, new HTML());
			table.getRowFormatter().setStyleName(row, styleBottomRowSuffix);
		}
		doFinalTouch();
		registerListeners();
		
		tableBuilt = true;
		return table;
	}
	
	public void setProcessEvents(boolean tfv) {
		this.processEvents = tfv;
	}
	
	private void finalizeRow() {
		if(row > -1 && col > -1) {
			table.getCellFormatter().addStyleName(row, col, 
					" last");
		}
	}
	
	
	public FlexTableBuilder addCurrentRowStyle(String rowStyle) {
		table.getRowFormatter().addStyleName(currentRow(), rowStyle);
		return this;
	}
	
	public int newRow() {
		finalizeRow();
		col = -1;
		++row;
		if(row%2 == 0) {
			table.getRowFormatter().setStyleName(row, styleEvenRowSuffix);
		} else {
			table.getRowFormatter().setStyleName(row, styleOddRowSuffix);
		}
		
		return row;
	}
	
	public int currentRow() {
		return row;
	}
	
	public int currentColumn() {
		return col;
	}
	
	public int getRowCount() {
		return table.getRowCount();
	}
	
	public FlexTableBuilder addTitle(String title) {
		return addTitle(title, false);
	}
	public FlexTableBuilder addTitle(String title, boolean html) {
		if (html) {
			table.setHTML(row, ++col, title);
		} else {
			table.setText(row, ++col, title);
		}
//		setCurrentCellStyle(styleTitleSuffix);
		addCurrentRowStyle(styleTitleSuffix);
		return this;
	}
	
	public FlexTableBuilder addTitleWidget(Widget widget) {
		table.setWidget(row, ++col, widget);
		addCurrentRowStyle(styleTitleSuffix);
		return this;
	}
	
	public FlexTableBuilder addTitleWidgetsToOneCell(Widget...widgets) {
		addFieldValueWidgetsToOneCell(1, widgets);
		addCurrentRowStyle(styleTitleSuffix);
		return this;
	}
	
	public FlexTableBuilder addFieldLabel(String label) {
		return addFieldLabel(label, false);
	}
	
	public FlexTableBuilder addFieldLabel(String label, boolean isHTML) {
		if (isHTML) {
			table.setHTML(row, ++col, label);
		} else {
			table.setText(row, ++col, label);
		}
		setCurrentCellStyle(styleFieldLabelSuffix);
		return this;
	}
	
	public FlexTableBuilder addFieldLabel(Label label) {
		table.setWidget(row, ++col, label);
		setCurrentCellStyle(styleFieldLabelSuffix);
		return this;
	}
	
	public FlexTableBuilder addFieldValue(String value) {
		return addFieldValue(value, false);
	}
	
	public FlexTableBuilder addFieldValue(String value, boolean isHTML) {
		if (isHTML) {
			table.setHTML(row, ++col, value);
		} else {
			table.setText(row, ++col, value);
		}
		setCurrentCellStyle(styleFieldValueSuffix);
		return this;
	}
	
	public SGFlexTableCell getCurrentCell() {
		return table.createCell(row, col);
	}
	
	public FlexTableRow getCurrentRow() {
		return new FlexTableRow(row);
	}
	
	public FlexTableBuilder addFieldLabelAndValue(String label, String value) {
		addFieldLabel(label);
		addFieldValue(value);
		return this;
	}
	
	public FlexTableBuilder addFieldLabelAndValue(String label, String value, boolean bothHTML) {
		addFieldLabel(label, bothHTML);
		addFieldValue(value, bothHTML);
		return this;
	}
	
	public FlexTableBuilder inlineAppendLabelAndValue(String labelHTML, String valueHTML, String separatorHTML) {
		String cellHTML = getCurrentTD().getInnerHTML();
		if (cellHTML==null || cellHTML.trim().length()<1) {
			cellHTML = "";
		} else {
			cellHTML = cellHTML+" ";
		}
		StringBuilder sb = new StringBuilder(cellHTML);
		sb.append("<span class=\"").append(styleFieldLabelSuffix).append("\">").append(labelHTML).append("</span>");
		sb.append(separatorHTML);
		sb.append("<span class=\"").append(styleFieldValueSuffix).append("\">").append(valueHTML).append("</span>");
		getCurrentTD().setInnerHTML(sb.toString());
		return this;
	}
	
	public FlexTableBuilder addFieldLabelAndWidget(String label, Widget widget) {
        return addFieldLabelAndWidget(label, 1, widget, 1);
    }

	public FlexTableBuilder addFieldLabelAndWidget(String label, boolean labelHTML, Widget widget) {
		return addFieldLabelAndWidget(label, 1, labelHTML, widget, 1);
	}
	
    public FlexTableBuilder addFieldLabelAndWidget(String label, int labelColSpan, Widget widget) {
        return addFieldLabelAndWidget(label, labelColSpan, widget, 1);
    }
    
    public FlexTableBuilder addFieldLabelAndWidget(String label, Widget widget, int widgetColSpan) {
       return addFieldLabelAndWidget(label, 1, widget, widgetColSpan);
    }
    
    public FlexTableBuilder addFieldLabelAndWidget(String label, int labelColSpan, Widget widget, int widgetColSpan) {
    	return addFieldLabelAndWidget(label, labelColSpan, false, widget, widgetColSpan);    	
    }
    
    public FlexTableBuilder addFieldLabelAndWidget(String label, int labelColSpan, boolean labelHTML, Widget widget,
                                       int widgetColSpan) {
        addFieldLabel(label, labelHTML);
        if (labelColSpan > 1) {
            setCurrentCellColSpan(labelColSpan);
        }
        addFieldValueWidget(widget);
        if (widgetColSpan > 1) {
            setCurrentCellColSpan(widgetColSpan);
        }
        return this;
    }
	
	public FlexTableBuilder addFieldValueWidget(Widget widget) {
		table.setWidget(row, ++col, widget);
		if (processEvents) {
			fireEvent(new WidgetAddedEvent(this, widget, row, col));
		}
		setCurrentCellStyle(styleFieldWidgetSuffix);
		return this;
	}
	
	public FlexTableBuilder addFieldLabelWidget(Widget widget) {
		table.setWidget(row, ++col, widget);
		if (processEvents) {
			fireEvent(new WidgetAddedEvent(this, widget, row, col));
		}
		setCurrentCellStyle(styleFieldLabelSuffix);
		return this;
	}
	
	public FlexTableBuilder setCurrentCellStyle(String styleName) {
		table.getCellFormatter().setStyleName(row, col, styleName);
		if(col == 0) {
			table.getCellFormatter().addStyleName(row, col, "first");
		}
		return this;
	}
	
	public void setCurrentCellVAlign(VerticalAlignmentConstant align) {
		getCurrentCellFormatter().setVerticalAlignment(row, col, align);
	}
	
	public void setCurrentCellHAlign(HorizontalAlignmentConstant align) {
		getCurrentCellFormatter().setHorizontalAlignment(row, col, align);
	}
	
	public FlexCellFormatter getCurrentCellFormatter() {
		return table.getFlexCellFormatter();
	}
	
	public FlexTableBuilder setCurrentCellColSpan(int colSpan) {
		getCurrentCellFormatter().setColSpan(row, col, colSpan);
		return this;
	}
	
	public FlexTableBuilder setCurrentCellRowSpan(int rowSpan) {
		getCurrentCellFormatter().setRowSpan(row, col, rowSpan);
		return this;
	}
	
	public FlexTableBuilder setCurrentCellWidth(String width) {
		getCurrentCellFormatter().setWidth(row, col, width);
		return this;
	}
	
	public FlexTableBuilder setCurrentColumnWidth(String width) {
		table.getColumnFormatter().setWidth(col, width);
		return this;
	}
	
	public void setTableStyle(String style) {
		this.style = style;
		table.setStyleName(style);
	}
	
	public void addTableStyle(String newStyle) {
		this.style = newStyle;
		table.addStyleName(newStyle);
	}
	
	/**
	 * New style will be applied only to new cells added after setting the style. 
	 */
	public void setTitleStyleSuffix(String suffix) {
		styleTitleSuffix = suffix;
	}
	
	/**
	 * New style will be applied only to new cells added after setting the style. 
	 */
	public void setEvenRowStyleSuffix(String suffix) {
		styleEvenRowSuffix = suffix;
	}
	
	/**
	 * New style will be applied only to new cells added after setting the style. 
	 */
	public void setOddRowStyleSuffix(String suffix) {
		styleOddRowSuffix = suffix;
	}
	
	public void setBottomRowStyleSuffix(String suffix) {
		styleBottomRowSuffix = suffix;
	}
	
	/**
	 * New style will be applied only to new cells added after setting the style. 
	 */
	public void setFieldLabelStyleSuffix(String suffix) {
		styleFieldLabelSuffix = suffix;
	}
	
	/**
	 * New style will be applied only to new cells added after setting the style. 
	 */
	public void setFieldValueStyleSuffix(String suffix) {
		styleFieldValueSuffix = suffix;
	}
	
	/**
	 * New style will be applied only to new cells added after setting the style. 
	 */
	public void setFieldWidgetStyleSuffix(String suffix) {
		styleFieldWidgetSuffix = suffix;
	}
	
	protected void doFinalTouch() {
		
		//span last columns
		int rows = getRowCount();
		
		//get largest row cell count (including span count)
		int maxCols = 0;
		Map<Integer, Integer> rowLengths = new HashMap<Integer, Integer>(rows);
		for (int r = 0; r < rows; ++r) {
			int numberOfCellsIncldingColspan = 0;
			for (int c = 0; c < table.getCellCount(r); c++) {
				numberOfCellsIncldingColspan += Math.max(1, table.getFlexCellFormatter().getColSpan(r, c));
				if (numberOfCellsIncldingColspan > maxCols) {
					maxCols = numberOfCellsIncldingColspan;
				}
			}
			rowLengths.put(boxI(r), boxI(numberOfCellsIncldingColspan));
		}
		
		for (int r=0; r<rows; r++) {
			int lastCellIdx = table.getCellCount(r) - 1;
			if (lastCellIdx<0) {
				continue;
			}
			
			int lastCellColSpan = table.getFlexCellFormatter().getColSpan(r, lastCellIdx);
			int curRowLen = unboxI(rowLengths.get(boxI(r)));
			table.getFlexCellFormatter().setColSpan(r, lastCellIdx, lastCellColSpan + (maxCols - curRowLen));
		}
		
		//add first row style
		table.getRowFormatter().addStyleName(0," first");
		
		//add last row style (last is the row before bottom-row)
		int lastRow = table.getRowCount()-(hasBottomRow?2:1);
		table.getRowFormatter().addStyleName(lastRow, "last");
	}
	
	protected void registerListeners() {
		if (!rowClickHandlers.isEmpty()) {
			table.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					SGFlexTableCell cell = table.getHTMLCellForEvent(event);
					List<ClickHandler> rowHandlers = rowClickHandlers.get(Integer.valueOf(cell.getRowIndex()));
					if (rowHandlers != null) {
						for (ClickHandler handler : rowHandlers) {
							handler.onClick(event);
						}
					}
				}
			});
		}
		
		if (!rowDblClickHandlers.isEmpty()) {
			table.addDoubleClickHandler(new DoubleClickHandler() {
				@Override
				public void onDoubleClick(DoubleClickEvent event) {
					SGFlexTableCell cell = table.getHTMLCellForEvent(event);
					List<DoubleClickHandler> rowHandlers = rowDblClickHandlers.get(Integer.valueOf(cell.getRowIndex()));
					if (rowHandlers != null) {
						for (DoubleClickHandler handler : rowHandlers) {
							handler.onDoubleClick(event);
						}
					}
				}
			});
		}
		
		if (!rowMouseOverHandlers.isEmpty() || !rowMouseOutHandlers.isEmpty()) {
			
			//detect mouse over and out on rows by looking at mouse move events
			
			table.addMouseMoveHandler(new MouseMoveHandler() {
				
				int lastRow;
				
				public void onMouseMove(MouseMoveEvent event) {
					SGFlexTableCell cell = table.getHTMLCellForEvent(event);
					int currentRow = cell.getRowIndex();
					if (lastRow == currentRow) return;
					
					List<MouseOutHandler> outHandlers = rowMouseOutHandlers.get(Integer.valueOf(lastRow));
					if (outHandlers != null) {
						for (MouseOutHandler handler : outHandlers) {
							handler.onMouseOut(new MouseOutEvent(){});
						}
					}
					
					List<MouseOverHandler> overHandlers = rowMouseOverHandlers.get(Integer.valueOf(currentRow));
					if (overHandlers != null) {
						for (MouseOverHandler handler : overHandlers) {
							handler.onMouseOver(new MouseOverEvent(){});
						}
					}
					
					lastRow = currentRow;
				}
				
			});
			
			//still handle out events to handle mouse exiting the table area
			table.addMouseOutHandler(new MouseOutHandler() {
				public void onMouseOut(MouseOutEvent event) {
					SGFlexTableCell cell = table.getHTMLCellForEvent(event);
					List<MouseOutHandler> rowHandlers = rowMouseOutHandlers.get(Integer.valueOf(cell.getRowIndex()));
					if (rowHandlers != null) {
						for (MouseOutHandler handler : rowHandlers) {
							handler.onMouseOut(event);
						}
					}
				}
			});
		}
	}

	public FlexTableBuilder addBlank() {
		return addBlank(1);
	}
	
	public FlexTableBuilder addBlank(int colSpan) {
		table.setText(row, ++col, null);
		if (colSpan>1) {
			setCurrentCellColSpan(colSpan);
		}
		return this;
	}

	public FlexTableBuilder addFieldValueWidgetsToOneCell(Widget...widgets) {
		return addFieldValueWidgetsToOneCell(1, widgets);
	}
	
	public FlexTableBuilder addFieldValueWidgetsToOneCell(int colSpan, Widget...widgets) {
		return addFieldValueWidgetsToOneCell(colSpan, new FlowPanel(), widgets);
	}
	
	public FlexTableBuilder addFieldValueWidgetsToOneCellHP(int colSpan, Widget... widgets) {
		return addFieldValueWidgetsToOneCell(colSpan, new HorizontalPanel(), widgets);
	}
	
	public FlexTableBuilder addFieldValueWidgetsToOneCell(int colSpan, Panel pnl, Widget...widgets) {
		for (Widget w : widgets) {
			pnl.add(w);
		}
		addFieldValueWidget(pnl);
		if (colSpan>1) {
			setCurrentCellColSpan(colSpan);
		}
		return this;
	}
	
	public FlexTableBuilder addFieldValuesToOneCell(int colSpan, List<String> values, boolean isHtml) {
		FlowPanel pnl = new FlowPanel();
		for (String v : values) {
			pnl.add(isHtml ? new HTML(v) : new Label(v));
		}
		addFieldValueWidget(pnl);
		if (colSpan>1) {
			setCurrentCellColSpan(colSpan);
		}
		return this;
	}
	
	public Element getCurrentTD() {
		return table.getCellFormatter().getElement(row, col);
	}
	
	public FlexTableBuilder noWrap() {
		CSS.whiteSpace(getCurrentTD(),CSS.WS_NOWRAP);
		return this;
	}
	
	public FlexTableBuilder noWrap(int colsBackward) {
		for (int i = col, j = 0; j <= colsBackward; j++, i--) {
			CSS.whiteSpace(table.getCellFormatter().getElement(row, i), CSS.WS_NOWRAP);
		}
		return this;
	}
	
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}
	
	public interface WidgetAddedEventHandler extends EventHandler {
		void onWidgetAdded(WidgetAddedEvent event);
	}
	public static class WidgetAddedEvent extends GwtEvent<WidgetAddedEventHandler> {
		public static final Type<WidgetAddedEventHandler> TYPE = new Type<WidgetAddedEventHandler>();
		
		Widget widget;
		FlexTableBuilder flexTableBuilder;
		int row;
		int column;
		
		public WidgetAddedEvent(FlexTableBuilder flexTableBuilder, Widget widget, int row, int column) {
			super();
			this.widget = widget;
			this.flexTableBuilder = flexTableBuilder;
			this.row = row;
			this.column = column;
		}

		public Widget getWidget() {
			return widget;
		}

		public FlexTableBuilder getFlexTableBuilder() {
			return flexTableBuilder;
		}

		public int getRow() {
			return row;
		}

		public int getColumn() {
			return column;
		}

		@Override
		protected void dispatch(WidgetAddedEventHandler handler) {
			handler.onWidgetAdded(this);
		}

		@Override
		public Type<WidgetAddedEventHandler> getAssociatedType() {
			return TYPE;
		}
	}
	
	public HandlerRegistration addWidgetAddedHandler(WidgetAddedEventHandler handler) {
		return addHandler(WidgetAddedEvent.TYPE, handler);
	}
	
	protected <H extends EventHandler> HandlerRegistration addHandler(Type<H> eventType, H handler) {
		setProcessEvents(true);
		return handlerManager.addHandler(eventType, handler);
	}
	
	public void addCurrentRowClickHandler(ClickHandler handler) {
		Integer r = Integer.valueOf(currentRow());
		List<ClickHandler> handlers = rowClickHandlers.get(r);
		if (handlers == null) {
			rowClickHandlers.put(r, handlers = new ArrayList<ClickHandler>());
		}
		handlers.add(handler);
	}
	
	public void addCurrentRowDoubleClickHandler(DoubleClickHandler handler) {
		Integer r = Integer.valueOf(currentRow());
		List<DoubleClickHandler> handlers = rowDblClickHandlers.get(r);
		if (handlers == null) {
			rowDblClickHandlers.put(r, handlers = new ArrayList<DoubleClickHandler>());
		}
		handlers.add(handler);
	}
	
	public void addCurrentRowMouseOverHandler(MouseOverHandler handler) {
		Integer r = Integer.valueOf(currentRow());
		List<MouseOverHandler> handlers = rowMouseOverHandlers.get(r);
		if (handlers == null) {
			rowMouseOverHandlers.put(r, handlers = new ArrayList<MouseOverHandler>());
		}
		handlers.add(handler);
	}
	
	public void addCurrentRowMouseOutHandler(MouseOutHandler handler) {
		Integer r = Integer.valueOf(currentRow());
		List<MouseOutHandler> handlers = rowMouseOutHandlers.get(r);
		if (handlers == null) {
			rowMouseOutHandlers.put(r, handlers = new ArrayList<MouseOutHandler>());
		}
		handlers.add(handler);
	}

	public void addFieldLabelAndWidgets(String label, Widget... widgets) {
		addFieldLabelAndWidgets(label, null, widgets);
	}
	
	public void addFieldLabelAndWidgets(String label, String label2, Widget... widgets) {
		if (label != null)
			addFieldLabel(label);
		if (label2 != null)
			addFieldLabel(label2);
		
		for (Widget w : widgets) {
			if (w == null)
				addBlank();
			else
				addFieldValueWidget(w);
		}
	}

	public void addFieldLabels(String... labels) {
		for (String label : labels)
			addFieldLabel(label);
	}

	
}
