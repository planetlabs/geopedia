package com.sinergise.generics.gwt.widgets.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;

public class PagingTable extends SimpleTable {


	private static final String STYLE_BASE_PRIMARY = "pagingTable";
	private static final String STYLE_NAV_PRIMARY = "paginator";

	private int currentPage = 0; // current page
	private int maxRowsPerPage = 0; // maximum results (rows) per expanded page
	private int currentRow = 0; // current row (when table is collapsed)
	private int numCurrentRows = Integer.MIN_VALUE;
	private int selectedRow = Integer.MIN_VALUE; // selected row
	private boolean hasMoreData = false;
	private boolean isCollapsed = false;

	private int maxDataCount = Integer.MIN_VALUE; // total data (if available) for the table
	private Widget[][] tableData = null;

	private boolean keepSelection = false; // keep selection between pages;
	private boolean hideNav = false; // hide navigation panel when it's not needed

	private Anchor back;
	private Anchor fwd;
	private Label dataCounter;

	private Widget navPanel;
	public PagingTable(TableColumn[] columns, String baseStyleName, boolean hasFilterRow, int pageSize) {
		super(columns, baseStyleName, hasFilterRow);
		this.maxDataRows = pageSize;
		setStyleName(STYLE_BASE_PRIMARY);
	}
	
	
	public void setPageSize(int pageSize) {
		this.maxDataRows = pageSize;
	}
	
	public int getPageSize() {
		return maxDataRows;
	}
	
	@Override
	protected void initializeUI() {
		FlowPanel navGrid = new FlowPanel();
		navPanel = navGrid;
		navGrid.setStyleName(STYLE_NAV_PRIMARY);
		back = new Anchor(WidgetConstants.widgetConstants.pagingTablePreviousPageButton());
		fwd = new Anchor(WidgetConstants.widgetConstants.pagingTableNextPageButton());
		dataCounter = new Label();
		back.setStylePrimaryName("back");
		back.setTitle(WidgetConstants.widgetConstants.prevPage());
		fwd.setStylePrimaryName("fwd");
		fwd.setTitle(WidgetConstants.widgetConstants.nextPage());
		dataCounter.setStyleName("counter");
		navGrid.add(back);
		navGrid.add(fwd);
		navGrid.add(dataCounter);
		super.initializeUI();
		getMainPanel().add(navGrid);

		back.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.preventDefault();
				previousPage();
			}
		});

		fwd.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.preventDefault();
				nextPage();
			}
		});

		updateButtons();
	}


	public void nextPage() {
		if (isCollapsed) {
			if ((currentRow + 1) < maxRowsPerPage) {
				currentRow++;
				collapsedRowChanged();
				return;
			} else if (!hasMoreData) {
				updateButtons();
				return;
			} else {
				currentRow = 0;
			}
		}

		if (hasMoreData) {
			internalSetPage(currentPage+1);
			updateButtons();
			repaint();
		}

	}
	
	protected void internalSetPage(int page) {
		currentPage = page;
	}

	public void previousPage() {
		if (isCollapsed) {
			if ((currentRow - 1) >= 0) {
				currentRow--;
				collapsedRowChanged();
				return;
			} else if (currentPage <= 0) {
				updateButtons();
				return;
			} else {
				currentRow = maxDataRows - 1;
			}
		}
		if (currentPage > 0) {
			internalSetPage(currentPage-1);
			updateButtons();
			repaint();
		}
	}


	private void collapsedRowChanged() {
		super.resizeTable(1);
		super.setRowData(0, tableData[currentRow]);
		updateSelection();
		updateButtons();
	}

	private void updateSelection() {
		if (selectedRow != Integer.MIN_VALUE) {
			if (currentRow == selectedRow) {
				setSelectedDataRow(selectedRow, true);
			} else {
				onCellClicked(null, currentRow);
			}
		}
	}

	public int getCurrentPage() {
		return currentPage;
	}
	
	public void setHideNav(boolean hideNav) {
		this.hideNav = hideNav;
		updateButtons();
	}

	private void updateButtons() {
		boolean onePageOnly = (currentPage == 0) && (!hasMoreData);

		if (onePageOnly && hideNav) {
			navPanel.setVisible(false);
		} else {
			navPanel.setVisible(true);

			if ((currentPage > 0) || (isCollapsed && currentRow > 0)) {
				back.setEnabled(true);
				back.removeStyleName("disabled");
			} else {
				back.setEnabled(false);
				back.addStyleName("disabled");
			}

			if ((hasMoreData) || (isCollapsed && currentRow < (maxRowsPerPage - 1))) {
				fwd.setEnabled(true);
				fwd.removeStyleName("disabled");
			} else {
				fwd.setEnabled(false);
				fwd.addStyleName("disabled");
			}
		}
	}


	private void updateDataCountLabel() {
		if (maxDataCount == Integer.MIN_VALUE) {
			if (numCurrentRows > 0 && currentPage >= 0 && maxDataRows >=0) {
				int from = currentPage * maxDataRows + 1;
				int to = from + numCurrentRows;
				String text = WidgetConstants.widgetConstants.pagingTableItems() + " " + from + "-" + to;
				dataCounter.setText(text);
				dataCounter.setVisible(true);
			} else if (dataCounter.getText() != null || dataCounter.getText().length() > 0) {
				dataCounter.setVisible(false);
			}
			return;
		}
		int from = currentPage * maxDataRows + 1;
		int to = (currentPage * maxDataRows + (maxDataRows));
		if (to > maxDataCount) to = maxDataCount;
		String text = WidgetConstants.widgetConstants.pagingTableItems() + " " + from + "-" + to + ", " + WidgetConstants.widgetConstants.pagingTableTotalItems() + ":" + maxDataCount;
		dataCounter.setText(text);
		dataCounter.setVisible(true);
	}

	@Override
	public void repaint() {
		if (tableModel == null) return;
		if (isCollapsed && selectedRow != Integer.MIN_VALUE) keepSelection = true;

		updateDataCountLabel();
		fireNewDataRequestedEvent();
		tableModel.getRowData(currentPage * maxDataRows, currentPage * maxDataRows + (maxDataRows - 1));
	}

	public void setCSSId(String datasourceId) {
		getMainPanel().getElement().setId(datasourceId);
	}

	@Override
	protected void fireNewDataRequestedEvent() {
		numCurrentRows = 0;
		super.fireNewDataRequestedEvent();
	}

	public void setDataLocation(int startDataIdx, int endDataIdx, boolean hasMoreData) {
		this.hasMoreData = hasMoreData;
		if (startDataIdx != Integer.MIN_VALUE && endDataIdx != Integer.MIN_VALUE) {
			if (startDataIdx != (currentPage * maxDataRows)) {
				internalSetPage(startDataIdx / maxDataRows);
			}
		}
		updateButtons();
	}

	public void setTotalRecordsCount(int count) {
		maxDataCount = count;
		updateDataCountLabel();
	}


	@Override
	public void resizeTable(int nRows) {
		maxRowsPerPage = nRows;
		if (nRows == 0) tableData = null;
		else if (tableData == null) tableData = new Widget[nRows][];
		else if (tableData.length != nRows) tableData = new Widget[nRows][];

		if (isCollapsed) {
			super.resizeTable(1);
		} else {
			super.resizeTable(nRows);
		}

		if (nRows == 0 && currentPage != 0) internalSetPage(0);
		updateButtons();
	}

	public void setCollapsed(boolean collapsed) {
		if (isCollapsed == collapsed) return;
		isCollapsed = collapsed;
		if (isCollapsed) {
			collapsedRowChanged();
		} else {
			super.resizeTable(maxRowsPerPage);
			for (int i = 0; i < maxRowsPerPage; i++) {
				super.setRowData(i, tableData[i]);
			}
			setSelectedDataRow(selectedRow, true);
		}
		updateButtons();
	}

	@Override
	public void setSelectedDataRow(int rowIndex, boolean selected) {

		if (isCollapsed) {
			if (rowIndex != currentRow) {
				currentRow = rowIndex;
				super.setRowData(0, tableData[currentRow]);
				updateButtons();
			}
			super.setSelectedDataRow(0, selected);
		} else {
			super.setSelectedDataRow(rowIndex, selected);
			currentRow = rowIndex;
		}

		if (selectedRow == rowIndex && !selected) {
			selectedRow = Integer.MIN_VALUE;
		} else if (selectedRow != rowIndex && selected) {
			selectedRow = rowIndex;
		}
	}

	@Override
	public void setSelectedDataRows(boolean selected) {
		if (!selected) selectedRow = Integer.MIN_VALUE;
		super.setSelectedDataRows(false);
	}

	@Override
	public void setRowData(int rowNum, Widget[] data) {
		if (keepSelection) {
			selectedRow = currentRow;
		}
		keepSelection = false;
		tableData[rowNum] = data;
		if (numCurrentRows < rowNum) {
			numCurrentRows = rowNum;
			updateDataCountLabel();
		}
		if (isCollapsed) {
			if (rowNum == currentRow) {
				super.setRowData(0, data);
				updateSelection();
			}
		} else {
			super.setRowData(rowNum, data);
		}
	}

	public Widget getCellWidget(int rowNum, int colNum) {
		return (tableData==null || tableData[rowNum] == null) ? null : tableData[rowNum][colNum];
	}
}