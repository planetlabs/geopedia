package com.sinergise.generics.gwt.widgets.table;

import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_COLUMN_FIRST;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_COLUMN_LAST;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_COLUMN_ORDER;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_COLUMN_ORDER_ASC;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_COLUMN_ORDER_DESC;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_EVEN;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_FILTER;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_FOOTER;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_HEADER;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_LAST;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_ODD;
import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_SELECTED;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.filter.DataFilter.OrderOption;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;

public class SimpleTable extends Composite{
	
	private static final int HEADER_ROW = 0;
	private static final int FILTER_ROW = 1;
	private String baseStyleName = "SimpleTable";
	private String hiddenStyleName = "hide";
	
	private TableColumn [] columns;
	private AbsolutePanel mainPanel;
	private Grid table;
	protected GenericTableModel tableModel = null;
	private int headerRows = 1;
	private int footerRows = 0;
	protected int maxDataRows = -1;
	protected boolean hasFilterRow = false;
	private boolean filterRowHidden = false;
	protected FocusPanel orderDisplayers[];
	
	protected boolean normalOrder = true;
	
	private Anchor showHideFilterBtn = null;
	private Anchor clearFilterBtn = null;
	
	public SimpleTable(TableColumn [] columns, String baseStyleName, boolean hasFilterRow) {
		if (baseStyleName!=null && baseStyleName.length()>0)
			this.baseStyleName = baseStyleName;
		this.columns = columns;
		this.hasFilterRow = hasFilterRow;
		orderDisplayers = new FocusPanel[columns.length];
		if (hasFilterRow)
			headerRows=2;
		initializeUI();
		
	}
	
	public void setFooterRowCount (int footerRows) {
		this.footerRows = footerRows;
	}
	
	protected AbsolutePanel getMainPanel() {
		if (mainPanel == null)
			mainPanel = new AbsolutePanel();
		return mainPanel;
	}
	
//	public void addTableEventsHandler(TableEventsHandler handler) {
//		if (!eventHandlers.contains(handler))
//			eventHandlers.add(handler);
//	}
//	
	
	protected void tableCellClicked(Cell cell){
		if (cell==null)
			return;
		if (cell.getRowIndex()<headerRows)  // ignore header row
			return;
		if (cell.getRowIndex()>=table.getRowCount()-footerRows) // ignore footer row
			return;
		
		onCellClicked(cell, cell.getRowIndex()-headerRows);
	}
	
	protected void onCellClicked(Cell cell, int row) {
		for (TableEventsHandler handler:tableModel.getEventsHandlerCollection())
			handler.onCellClicked(cell, row);
	}
	
	protected void updateColumnSortingStyle(TableColumn column) {
		OrderOption order = column.getOrder();
		int index = column.getIndex();
		SimplePanel orderDisplayer = orderDisplayers[index];
		CellFormatter tableCellFormatter = table.getCellFormatter();
		
		tableCellFormatter.removeStyleName(HEADER_ROW, index, "sortable");
		tableCellFormatter.removeStyleName(HEADER_ROW, index, "sortable");
		
		if (hasFilterRow) {
			tableCellFormatter.removeStyleName(FILTER_ROW, index, "sortable");
			tableCellFormatter.removeStyleName(FILTER_ROW, index, "sortable");
		}
		
		orderDisplayer.removeStyleName(STYLE_COLUMN_ORDER_DESC);
		orderDisplayer.removeStyleName(STYLE_COLUMN_ORDER_ASC);
		
		
		if (order==OrderOption.ASC)  {
			orderDisplayer.addStyleName(STYLE_COLUMN_ORDER_ASC);
			tableCellFormatter.addStyleName(HEADER_ROW, index, "sortable");
			if (hasFilterRow) {
				tableCellFormatter.addStyleName(FILTER_ROW, index, "sortable");
			}
		} else if (order==OrderOption.DESC) {
			orderDisplayer.addStyleName(STYLE_COLUMN_ORDER_DESC);
			tableCellFormatter.addStyleName(HEADER_ROW, index, "sortable");
			if (hasFilterRow) {
				tableCellFormatter.addStyleName(FILTER_ROW, index, "sortable");
			}
		} 
	}
	protected void tableColumnClicked(TableColumn column) {
		for (int i=0;i<columns.length;i++) {
			TableColumn c = columns[i];
			if (!c.isSortable()) continue;  // ignore unsortables
				
			if (c == column) {
				column.changeOrder(normalOrder);
			} else{ 
				 c.setOrder(OrderOption.OFF);
			}
			updateColumnSortingStyle(c);
		}
				
		for (TableEventsHandler handler:tableModel.getEventsHandlerCollection())
			handler.onColumnLabelClicked(column);
	}
	
	
	public void setFilterRowVisibility(boolean visible ) {
		if (!hasFilterRow)
			return;
		RowFormatter tableRowFormatter = table.getRowFormatter();
		if (!visible) {
			tableRowFormatter.addStyleName(0,"noFilter");
			tableRowFormatter.addStyleName(1,"hide");
			showHideFilterBtn.setStyleName("btnShowFilter");
			filterRowHidden=true;
		} else {
			tableRowFormatter.removeStyleName(1,"hide");
			tableRowFormatter.removeStyleName(0,"noFilter");
			showHideFilterBtn.setStyleName("btnHideFilter");
			filterRowHidden=false;
		}
		clearFilterBtn.setVisible(!filterRowHidden);		
	}
	protected Widget createColumnLabelWidget (final int idx, final TableColumn column) {
		FlowPanel labelPanel = new FlowPanel();
		orderDisplayers[idx] = new FocusPanel();
		final Label lbl = new Label(column.getLabel());
		labelPanel.add(orderDisplayers[idx]);
		labelPanel.add(lbl);
		
		if(column.isSortable()){
//			orderDisplayers[idx].setStyleName(STYLE_COLUMN_ORDER);
			ClickHandler colClickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					tableColumnClicked(column);
				}
			};
			
			
			MouseOverHandler overHandler = new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					orderDisplayers[idx].addStyleName("hover");
				}
			};
			
			MouseOutHandler outHandler = new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					lbl.removeStyleName("hover");
					orderDisplayers[idx].removeStyleName("hover");
				}
			};
			lbl.addClickHandler(colClickHandler);
			//lbl.addMouseOverHandler(overHandler);
			//lbl.addMouseOutHandler(outHandler);
			orderDisplayers[idx].addClickHandler(colClickHandler);
			orderDisplayers[idx].addMouseOverHandler(overHandler);
			orderDisplayers[idx].addMouseOutHandler(outHandler);
		}
		labelPanel.setStyleName(STYLE_COLUMN_ORDER);
		return labelPanel;
	}
	
	private int getLastVisibleColumn() {
		for (int i = columns.length - 1; i >= 0; i--) {
			if (!columns[i].isHidden()) {
				return i;
			}
		}
		return 0;
	}
	
	private int getFirstVisibleColumn() {
		for (int i = 0; i < columns.length; i++) {
			if (!columns[i].isHidden()) {
				return i;
			}
		}
		return 0;
	}
	
	private void setTableHeader() {
		ColumnFormatter tableColFormatter = table.getColumnFormatter();
		RowFormatter tableRowFormatter = table.getRowFormatter();
		CellFormatter cellFormatter = table.getCellFormatter();

		int firstColumn = getFirstVisibleColumn();
		int lastColumn = getLastVisibleColumn();

		for (int i = 0; i < columns.length; i++) {
			TableColumn currentColumn = columns[i];
			if (currentColumn != null) {
				boolean hideColumn = currentColumn.isHidden();

				table.setWidget(0, i, createColumnLabelWidget(i, currentColumn));
				Widget filterWidget = currentColumn.getFilterWidget();
				if (filterWidget != null) {
					addFocusHandler(filterWidget);
					addClickHandler(filterWidget);
					table.setWidget(1, i, filterWidget);
				}

				String styleName = currentColumn.getName();
				tableColFormatter.setStyleName(i, styleName);

				for (int h = 0; h < headerRows; h++) {
					cellFormatter.setStyleName(h, i, styleName);
				}
				cellFormatter.addStyleName(0, i, EntityUtils.styleForPrimitiveType(currentColumn.getValueType()));

				if (hideColumn) {
					for (int h = 0; h < headerRows; h++) {
						cellFormatter.setStyleName(h, i, hiddenStyleName);
					}	
				}

				updateColumnSortingStyle(currentColumn);
			}
		}
		
		for (int h = 0; h < headerRows; h++) {
			cellFormatter.addStyleName(h, firstColumn, STYLE_COLUMN_FIRST);
			cellFormatter.addStyleName(h, lastColumn, STYLE_COLUMN_LAST);
		}
		tableColFormatter.addStyleName(firstColumn, STYLE_COLUMN_FIRST);
		tableColFormatter.addStyleName(lastColumn, STYLE_COLUMN_LAST);

		tableRowFormatter.setStyleName(0, STYLE_ROW_HEADER);
		if (hasFilterRow)
			tableRowFormatter.addStyleName(1, STYLE_ROW_FILTER);
	}
	
	protected void initializeUI () {
		
		clearFilterBtn = new Anchor("");
		clearFilterBtn.setTitle(WidgetConstants.widgetConstants.simpleTableFilterClearButton());
		clearFilterBtn.setStyleName("btnClearFilter");
		clearFilterBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				tableModel.clearSearchParameters();
				repaint();
			}
		});
		
		showHideFilterBtn = new Anchor("");
		showHideFilterBtn.setTitle(WidgetConstants.widgetConstants.simpleTableFilterToggleButton());
		showHideFilterBtn.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				setFilterRowVisibility(filterRowHidden);
			}
		});
		
		getMainPanel().add(showHideFilterBtn);
		getMainPanel().add(clearFilterBtn);
		table = new Grid(headerRows,columns.length);
		table.setStylePrimaryName(baseStyleName);
		table.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Cell clickedCell = table.getCellForEvent(event);
				tableCellClicked(clickedCell);
			}
		});
		getMainPanel().add(table);
		
		setTableHeader();
		
		setFilterRowVisibility(!filterRowHidden);
		initWidget(getMainPanel());
	}
	
	private void addFocusHandler(final Widget filterWidget) {
		if (!(filterWidget instanceof TextBox)) return;
		((TextBox)filterWidget).addFocusHandler(new FocusHandler() {
	        @Override
	        public void onFocus(FocusEvent event) {
	        	((TextBox)filterWidget).selectAll();
	        }
	    });
	}
	
	private void addClickHandler(final Widget filterWidget) {
		if (!(filterWidget instanceof TextBox)) return;
		((TextBox)filterWidget).addClickHandler(new ClickHandler() {
	        @Override
	        public void onClick(ClickEvent event) {
	        	((TextBox)filterWidget).selectAll();
	        }
	    });
	}

	public void setDataModel(GenericTableModel tm) {
		this.tableModel = tm;
	}
	
	public void clearTable() {
		table.resize(headerRows, columns.length);
	}
	public void repaint() {
		if (tableModel == null)
			return;
		tableModel.getRowData(-1, -1);
	}
	
	
	protected void fireNewDataRequestedEvent(){
		for (TableEventsHandler handler:tableModel.getEventsHandlerCollection())
			handler.newTableDataRequested();
	}
	public void resizeTable(int nRows) {
		if (nRows<0)
			return;
		if ((nRows+headerRows+footerRows)==table.getRowCount()) 
			return;
		table.resize(nRows+headerRows+footerRows, columns.length);
		RowFormatter tableRowFormatter = table.getRowFormatter();

		for (int i=0;i<nRows;i++) {
			if ((i+1)%2==0) 
				tableRowFormatter.setStyleName(i+headerRows, STYLE_ROW_EVEN);
			else 
				tableRowFormatter.setStyleName(i+headerRows, STYLE_ROW_ODD);				
			if (i==(nRows-1)) {
				tableRowFormatter.addStyleName(i+headerRows, STYLE_ROW_LAST);
			}
		}
		if (footerRows>0) {
			for (int i=0;i<footerRows;i++) {
				tableRowFormatter.addStyleName(i+nRows+headerRows, STYLE_ROW_FOOTER);				
			}
		}
	}
	

	/**
	 * Get table row from data row
	 * @param data row
	 * @return table row or -1 if illegal data row
	 */
	public int getTableRow(int dataRow) {
		int actualRow = dataRow+headerRows;
		if (actualRow>=table.getRowCount() ||
				dataRow<0)
			return -1;
		return actualRow;
	}
	
	/**
	 * Add style to data row
	 * @param dataRow data row
	 * @param style style
	 * @return true if successful, false otherwise
	 */
	public boolean addDataRowStyle(int dataRow, String style) {
		int row = getTableRow(dataRow);
		if (row>=0) {
			RowFormatter tableRowFormatter = table.getRowFormatter();
			tableRowFormatter.addStyleName(row, style);
			return true;
		}
		return false;
	}
	

	/**
	 * Removes style from data row
	 * @param dataRow data row
	 * @param style style
	 * @return true if successful, false otherwise
	 */
	
	public boolean removeDataRowStyle(int dataRow, String style) {
		int row = getTableRow(dataRow);
		if (row>=0) {
			RowFormatter tableRowFormatter = table.getRowFormatter();
			tableRowFormatter.removeStyleName(row, style);
			return true;
		}
		return false;
	}
	

	
	public void setSelectedDataRow(int rowIndex, boolean selected) {
		int actualRow = rowIndex+headerRows;
		if (actualRow>=table.getRowCount() ||
				rowIndex<0)
			return;
		RowFormatter tableRowFormatter = table.getRowFormatter();
		if (selected)
			tableRowFormatter.addStyleName(actualRow, STYLE_ROW_SELECTED);
		else
			tableRowFormatter.removeStyleName(actualRow, STYLE_ROW_SELECTED);
	
		
	}
	public void setSelectedDataRows(boolean selected) {
		RowFormatter tableRowFormatter = table.getRowFormatter();
		for (int i=headerRows;i<table.getRowCount();i++) {
			if (selected)
				tableRowFormatter.addStyleName(i, STYLE_ROW_SELECTED);
			else
				tableRowFormatter.removeStyleName(i, STYLE_ROW_SELECTED);
		}
		
	}
	public void setRowData(int rowNum, Widget []data) {
		if (data == null || data.length != columns.length || data.length == 0)
			return;
		if (rowNum<0) // illegal row number
			return;
		if (maxDataRows>0 && rowNum>=maxDataRows)
			return;
		
		if ((rowNum+headerRows)>=table.getRowCount()) 
			resizeTable(rowNum+1);
		int trow = rowNum+headerRows;
		CellFormatter cellFormatter = table.getCellFormatter();
		for (int i=0;i<data.length;i++) {
			table.setWidget(trow, i, data[i]);
			cellFormatter.setStyleName(trow, i, columns[i].getName());
			cellFormatter.addStyleName(trow, i, EntityUtils.styleForPrimitiveType(columns[i].getValueType()));
			if(columns[i].isHidden()) {
				cellFormatter.setStyleName(trow, i, hiddenStyleName);
			}
		}
		cellFormatter.addStyleName(trow, getFirstVisibleColumn(), STYLE_COLUMN_FIRST);
		cellFormatter.addStyleName(trow, getLastVisibleColumn(), STYLE_COLUMN_LAST);
	}
	
	/**
	 * 
	 * @param footerRowNum  number of footer row starting with 0
	 * @param data  Array of widgets
	 */
	public void setFooterData(int footerRowNum, Widget[] data) {
		if (footerRows<=0 || footerRowNum>=footerRows)
			return;
		int row = table.getRowCount()-footerRows+footerRowNum;
		CellFormatter cellFormatter = table.getCellFormatter();
		for (int i=0;i<data.length;i++) {
			table.setWidget(row, i, data[i]);
			cellFormatter.setStyleName(row, i, columns[i].getName());
			if(columns[i].isHidden()) {
				cellFormatter.setStyleName(row, i, hiddenStyleName);
			}
		}
		cellFormatter.addStyleName(row, getFirstVisibleColumn(), STYLE_COLUMN_FIRST);
		cellFormatter.addStyleName(row, getLastVisibleColumn(), STYLE_COLUMN_LAST);
	}
	
	/**
	 * 
	 * @param footerRowNum  number of footer row starting with 0
	 * @param data  Array of widgets
	 */
	public void setFooterData(int footerRowNum, int footerColumnNum,  Widget data) {
		if (footerRows<=0 || footerRowNum>=footerRows)
			return;
		if (footerColumnNum<0 || footerColumnNum>=columns.length)
			return;
		int row = table.getRowCount()-footerRows+footerRowNum;
		CellFormatter cellFormatter = table.getCellFormatter();
		table.setWidget(row, footerColumnNum, data);
		cellFormatter.setStyleName(row, footerColumnNum, columns[footerColumnNum].getName());
		if(columns[footerColumnNum].isHidden()) {
			cellFormatter.setStyleName(row, footerColumnNum, hiddenStyleName);
		}
	}
	
	public void setNormalOrder(boolean normalOrder) {
		this.normalOrder = normalOrder;
	}
}
