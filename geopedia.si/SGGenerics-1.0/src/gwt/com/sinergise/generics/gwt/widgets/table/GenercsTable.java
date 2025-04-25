package com.sinergise.generics.gwt.widgets.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.filter.AttributesFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.DataFilter.OrderOption;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.OrderFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.CreationResolver;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.core.IsFilterProvider;
import com.sinergise.generics.gwt.core.IsFilterable;
import com.sinergise.generics.gwt.widgetbuilders.FilterWidgetBuilder;
import com.sinergise.generics.gwt.widgetbuilders.MutableWidgetBuilder;
import com.sinergise.generics.gwt.widgetbuilders.TableWidgetBuilder;
import com.sinergise.generics.gwt.widgetbuilders.WidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.SimpleBindingWidgetProcessor;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;
import com.sinergise.gwt.ui.StyleConsts;

public class GenercsTable extends GenericWidget implements GenericTableModel{

	/**
	 * widget meta attributes
	 */
	public static final String WGA_HAS_FILTER_ROW = "hasFilterRow";
	
	public static final String WGA_FILTER_ROW_CLOSED = "filterRowClosed";
	
	public static final String WGA_DO_NOT_FORCE_ORDER ="noForceOrder";
	
	public static final String WGA_PAGE_SIZES="pageSizes";
	
	/**
	 * entity meta attributes
	 */
	
	/**
	 * EntityAttribute (table column) order type  (ASC,DESC) 
	 */
	public static final String EA_ORDER_BY="orderBy";
	/**
	 * EntityAttribute (table column) order ability (true,false)
	 * By default everything is sortable except Action or Stub column.
	 */
	public static final String EA_IS_SORTABLE="isSortable";
	
	public static final String STYLE_BASE_PRIMARY = "genericTableWidget";
	protected PagingTable tableWidget=null;
	protected List<GenericObjectProperty> attributeList = null;
	protected List<TableEventsHandler> tableEventHandlers = new ArrayList<TableEventsHandler>();
	
	private FlowPanel basePanel;
	protected TableDataProvider dataProvider;
	
	private int maxRowCount = 10;
	private String baseStyleName = StyleConsts.TABLE;
	private int	rowCount;
	boolean hasFilterRow = true;
	boolean filterRowClosed = false;
	boolean noForceOrder=false;
	
	private EntityType entityType;
	private FilterRowBindingWidgetProcessor filterValueBinder;
	private EntityObject filterObject;
	private TableColumn tblColumns[];
	
	private Anchor[] numTableRowsSelectors;
	
	private FilterWidgetBuilder filterWidgetBuilder = new FilterWidgetBuilder();
	
	
	private class FilterRowBindingWidgetProcessor extends SimpleBindingWidgetProcessor {

		public FilterRowBindingWidgetProcessor(MutableWidgetBuilder wb) {
			super(wb);
		}
		
		public ArrayList<DataFilter> getFilterProvidersDataFilters() {
			ArrayList<DataFilter> filterList = new ArrayList<DataFilter>();
			for (String boundAttribute:boundAttributes.keySet()) {
				Widget w = getWidgetForAttribute(boundAttribute);
				if (w instanceof IsFilterProvider) {
					DataFilter filter = ((IsFilterProvider)w).getFilter();
					if (filter!=null)
						filterList.add(filter);
				}
			}
			return filterList;
		}
	}
	
	public GenercsTable() {
		widgetBuilder = new TableWidgetBuilder(); 
		basePanel = new FlowPanel();
		initWidget(basePanel);
		basePanel.setStyleName(STYLE_BASE_PRIMARY);
		rowCount=0;
	}
	
	private class SubmitListener implements KeyPressHandler, ValueChangeHandler<String> {
		public SubmitListener() {
		}

		
		@Override
		public void onKeyPress(KeyPressEvent event) {
			int charCode = event.getUnicodeCharCode();
			if ((charCode == 0 && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
				|| event.getCharCode() == KeyCodes.KEY_ENTER) 
			{
				onFiltersChanged();
			} 
		  }

		
		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
			onFiltersChanged();
		}
	 }
	
	private SubmitListener filtersl = new SubmitListener();
	private ArrayList<IsFilterable> filterableFilterWidgets = new ArrayList<IsFilterable>();
	
	
	public void setFilterWidgetBuilder (FilterWidgetBuilder fwb) {
		this.filterWidgetBuilder = fwb;
	}
	
	private void applyFilterFilters(final Iterator<IsFilterable> filterableIt, final AsyncCallback<Void> finishedCallback) {
		if (!filterableIt.hasNext()) {
			if (finishedCallback!=null)
				finishedCallback.onSuccess(null);
			tableWidget.repaint();
			return;
		} else {
			IsFilterable filterable = filterableIt.next();
			filterValueBinder.save(filterObject);
			filterable.onStateChanged(filterObject, new AsyncCallback<Void>() {
				
				@Override
				public void onSuccess(Void result) {
					applyFilterFilters(filterableIt, finishedCallback);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
					if (finishedCallback!=null)
						finishedCallback.onFailure(caught);
				}
			});
		}
			
	}
	private void onFiltersChanged() {
		AsyncCallback<Void> filtersAppliedCB = new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				
			}

			@Override
			public void onSuccess(Void result) {
				for (TableEventsHandler teh:getEventsHandlerCollection()) {
					teh.onFiltersChanged();
				}
			}
		};
		applyFilterFilters(filterableFilterWidgets.iterator(),filtersAppliedCB);
		
	}
	
	
	private Widget buildFilterWidget(WidgetBuilder wBuilder, GenericObjectProperty prop) {
		Widget filterWidget = wBuilder.buildWidget(prop.getName(), prop.getAttributes());
		
		
		
		if (filterWidget instanceof IsFilterable) {
			filterableFilterWidgets.add((IsFilterable) filterWidget);
		}
		
		// TODO: better listeners..
		if (filterWidget instanceof TextBox) {
			((TextBox)filterWidget).addKeyPressHandler(filtersl);
		} else if (filterWidget instanceof HasValueChangeHandlers<?>) {
			((HasValueChangeHandlers<String>)filterWidget).addValueChangeHandler(filtersl);
		}

		filterWidget = filterValueBinder.bind(filterWidget, 0, prop, this);
		
		
		return filterWidget;
	}
	
	private class InternalCreationProvider implements IsCreationProvider  {


		private ArrayList<CreationListener> cListeners = new ArrayList<CreationListener>();
		private boolean isCreated = false;
		
		public InternalCreationProvider() {
		}

		public void created() {
			for (CreationListener l:cListeners)
				l.creationCompleted(InternalCreationProvider.this);
			isCreated = true;			
		}
		
		
		@Override
		public boolean isCreated() {
			return isCreated;
		}
		
		@Override
		public void addCreationListener(CreationListener l) {
			cListeners.add(l);
		}	
		
	}
	
	
	
	@Override
	public void build(List<GenericObjectProperty> properties, EntityType entityType) {
		if (isCreated())
			throw new RuntimeException("GenericsTable has already been created! Build should only be called once!");
		setEntityType(entityType);
		CreationResolver creationResolver = new CreationResolver(this);
		creationResolver.addCreationListener(new CreationListener() {
			
			@Override
			public void creationCompleted(IsCreationProvider w) {
				widgetCreated();
			}
		});
		InternalCreationProvider icp = new InternalCreationProvider();
		creationResolver.addCreationProvider(icp);
		
		initializeTableProperties(widgetMetaAttributes);
		this.attributeList = properties;
		int tableColumns = properties.size();
		tblColumns = new TableColumn[tableColumns];
		this.entityType = entityType;

		
		
		filterValueBinder = new FilterRowBindingWidgetProcessor(filterWidgetBuilder);
		filterObject = new AbstractEntityObject(entityType.getId());
		boolean orderBySet = false;
		for (int i=0;i<tableColumns;i++) {
			GenericObjectProperty prop = properties.get(i);
			
			if (hasFilterRow && !prop.isAction()) {
				Widget filterWidget = buildFilterWidget(filterWidgetBuilder, prop);
				if (filterWidget instanceof IsCreationProvider) {
					if (!((IsCreationProvider)filterWidget).isCreated()) {
						creationResolver.addCreationProvider((IsCreationProvider)filterWidget);
					}
				}
				tblColumns[i]= new TableColumn(prop.getName(), prop.getLabel(), filterWidget, i);		
			} else {
				tblColumns[i]= new TableColumn(prop.getName(), prop.getLabel(), i);				
			}
			tblColumns[i].setHidden(prop.isHidden());
			tblColumns[i].setSortable(MetaAttributes.readBoolAttr(prop.getAttributes(), EA_IS_SORTABLE, !prop.isAction()));
			if (!prop.isAction()){
				TypeAttribute ta = entityType.getAttribute(prop.getName());
				if (ta!=null) {
					tblColumns[i].setValueType(ta.getPrimitiveType());
					
				}
			}
			String orderStr=MetaAttributes.readStringAttr(prop.getAttributes(), EA_ORDER_BY, null);
			if (orderStr!=null) {
				tblColumns[i].setOrder(OrderFilter.createFromString(orderStr));
				if (tblColumns[i].getOrder()!=OrderOption.OFF) orderBySet = true; // ordering for the table is set
			}
				
			propertyMap.put(prop.getName(), prop);
		}
		
		if (!orderBySet && !noForceOrder) { // force order by first sortable column unless overriden on specific table
			for (int i=0;i<tableColumns;i++) {
				if (tblColumns[i].isSortable()) {
					tblColumns[i].setOrder(OrderOption.ASC);
					break;
				}
			}
		}
		
		tableWidget = new PagingTable(tblColumns, baseStyleName, hasFilterRow, maxRowCount);
		tableWidget.setDataModel(this);
		addEventsHandler(new TableEventsHandler() {
			
			@Override
			public void onColumnLabelClicked(TableColumn column) {
				repaint();
				
			}

			
			@Override
			public void onCellClicked(Cell cell, int rowIndex) {
				// not supported
			}

			
			@Override
			public void newTableDataRequested() {
				// not supported
			}

			
			@Override
			public void newTableDataReceived(ArrayValueHolder tableData) {
				// not supported
			}

			
			@Override
			public void newTableDataRequestFailed(Throwable th) {
				// not supported
			}

			@Override
			public void onFiltersChanged() {
				// not supported
			}
		});
		basePanel.add(tableWidget);
		if (filterRowClosed)
			tableWidget.setFilterRowVisibility(false);
		icp.created();		
	}
	
	
	private void addNumRowsSelectors(int [] numRowsArray) {
		FlowPanel numRowsSelectorsPanel = new FlowPanel();
		numRowsSelectorsPanel.setStyleName("pageSizeSelectors");
		basePanel.add(numRowsSelectorsPanel);
		
		numTableRowsSelectors = new Anchor[numRowsArray.length];
		for (int i=0;i<numRowsArray.length;i++) {
			final int nRows = numRowsArray[i];
			String strSize = String.valueOf(nRows);			
			final Anchor nr = new Anchor(strSize);
			nr.setStyleName("size"+i);
			nr.setTitle(strSize+" "+WidgetConstants.widgetConstants.rowsPerPage());
			nr.addClickHandler(new ClickHandler() {
				
				
				@Override
				public void onClick(ClickEvent event) {
					event.preventDefault();
					tableWidget.setPageSize(nRows);
					setRowsPerPage(nRows);
					repaint();
					for (Anchor a:numTableRowsSelectors) {
						a.removeStyleName("active");
					}
					nr.addStyleName("active");
				}
			});
			if (maxRowCount==nRows) {
				nr.addStyleName("active");
			}
			numRowsSelectorsPanel.add(nr);
			numTableRowsSelectors[i] = nr;
		}		
	}

	
	public void setDataProvider (TableDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	public TableDataProvider getDataProvider() {
		return dataProvider;
	}
	
	protected void initializeTableProperties(Map<String, String> tableMetaAttr) {
		
		setRowsPerPage(MetaAttributes.readIntAttr(tableMetaAttr, MetaAttributes.TABLE_MAXROWS, maxRowCount));
		setBaseStyleName(MetaAttributes.readStringAttr(tableMetaAttr, MetaAttributes.STYLE_BASE, baseStyleName));
		setFilter(MetaAttributes.readBoolAttr(tableMetaAttr, WGA_HAS_FILTER_ROW, hasFilterRow));
		filterRowClosed = MetaAttributes.readBoolAttr(tableMetaAttr, WGA_FILTER_ROW_CLOSED, filterRowClosed);
		setNoForceOrder(MetaAttributes.readBoolAttr(tableMetaAttr, WGA_DO_NOT_FORCE_ORDER, noForceOrder));
		String name = MetaAttributes.readStringAttr(tableMetaAttr, MetaAttributes.NAME, null);
		if (name!=null) {
			basePanel.addStyleName(name);
		}
		
		String pageSizesStr = MetaAttributes.readStringAttr(tableMetaAttr, WGA_PAGE_SIZES, null);
		if (pageSizesStr!=null) {
			try {
				String[] psa = pageSizesStr.split(",");
				int [] pageSizesArray = new int[psa.length];
				for (int i=0;i<psa.length;i++) {
					pageSizesArray[i] = Integer.valueOf(psa[i]);
				}
				addNumRowsSelectors(pageSizesArray);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
			
		}
		
	}
	
	private void setNoForceOrder(boolean noForceOrder) {
		this.noForceOrder = noForceOrder;
	}
	private void setBaseStyleName(String styleName) {
		baseStyleName = styleName; 
	}

	public void setRowsPerPage(int rowCount) {
		maxRowCount = rowCount; 
	}
	
	public void setFilter(boolean hasFilter) { 
		this.hasFilterRow = hasFilter;
	}
	/** table model **/
	

	@Override
	public void getRowData(int startRow, int endRow) {
		if (dataProvider!=null) 
			dataProvider.provideRowData(startRow, endRow, GenercsTable.this);
	}


	@Override
	public void setRowCount (int rowCount) {
		if (rowCount<0 || rowCount>maxRowCount)
			throw  new RuntimeException("Row count should be between 0 and "+maxRowCount);
		this.rowCount = rowCount;
	}
	
	
	@Override
	public void setDataLocation(int startDataIdx, int endDataIdx, boolean hasMoreData) {
		tableWidget.setDataLocation(startDataIdx, endDataIdx, hasMoreData);		
	}
	
	
	@Override
	public void setTotalRecordsCount (int count) {
		tableWidget.setTotalRecordsCount(count);
	}
	
	@Override
	public void updateTable() {
		if (rowCount==0) {
			tableWidget.resizeTable(0);
			return;
		}
		tableWidget.resizeTable(rowCount);
		for (int i=0;i<rowCount;i++) {
			Widget data[]=new Widget[attributeList.size()];
			for (int j=0;j<attributeList.size();j++) {
				data[j] = createAndBindCellWidget(i, j);
			}
			tableWidget.setRowData(i, data);	
		}			
	}

	protected Widget createAndBindCellWidget(int row, int col) {
		GenericObjectProperty attr = attributeList.get(col);
		Widget cellWidget = widgetBuilder.buildWidget(attr.getName(),attr.getAttributes());
		return bindProcessors(cellWidget, row, attr, GenercsTable.this);
	} 
	
	
	public void repaint() {
		if (!isCreated()) return;
		if (tableWidget!=null) {
			tableWidget.clearTable();
			tableWidget.repaint();
		}
	}
	
	public void repaintFilters() {
		onFiltersChanged();
	}
	
	
	@Override
	public OrderFilter getOrderParameters() {
		ArrayList<Integer> orderSeq  = new ArrayList<Integer>();
		OrderOption[] orderBy = new OrderOption[entityType.getAttributeCount()];
		for (TableColumn tc:tblColumns) {
			TypeAttribute ta = entityType.getAttribute(tc.getName());
			if (ta!=null) { // might be an action column or something else.. 
				orderBy[ta.getId()] = tc.getOrder();
				orderSeq.add(ta.getId());
			}
		}
		OrderFilter filter =  new OrderFilter(orderBy, entityType.getName());
		filter.setOrderSequence(orderSeq);
		return filter;
	}
	@Override
	public DataFilter getSearchParameters() {
		if (!hasFilterRow) return null;
		filterValueBinder.save(filterObject);
		SimpleFilter sf =  new SimpleFilter(filterObject);
		
		ArrayList<DataFilter> filters = filterValueBinder.getFilterProvidersDataFilters();
		if (filters.size()==0) {
			return sf;
		}
		
		CompoundDataFilter cdf = new CompoundDataFilter();
		cdf.addDataFilter(sf, DataFilter.NO_FILTER);
		for (DataFilter f:filters) {
			if (f instanceof AttributesFilter) {
				((AttributesFilter)f).setEntityTypeId(entityType.getId());
			}
			cdf.addDataFilter(f, DataFilter.OPERATOR_AND);	
		}
		return cdf;
	}
	
	@Override
	public void clearSearchParameters() {
		EntityUtils.setNull(filterObject);
		filterValueBinder.load(filterObject);
	}
	
	public void setSearchParameters(EntityObject filterEO) {
		filterValueBinder.load(filterEO);
	}

	@Override
	public void bindProcessors() {
		// ignore, binders are called upon data arrival
	}

	
	public PagingTable getTableWidget() {
		return tableWidget;
	}

	
	@Override
	public void addEventsHandler(TableEventsHandler handler) {
		if (tableEventHandlers.contains(handler))
			return;
		tableEventHandlers.add(handler);
	}

	
	@Override
	public Collection<TableEventsHandler> getEventsHandlerCollection() {
		return tableEventHandlers;
	}

	@Override
	public void destroy() {
		super.destroy();
		if (tableWidget != null) {
			tableWidget.clearTable();
		}
		if (basePanel != null) {
			basePanel.clear();
		}
		filtersl = null;
		if (tableEventHandlers!=null) {
			tableEventHandlers.clear();
		}
	}
	
	@Override
	public  List<GenericObjectProperty> getAttributes() {
		return attributeList;
	}
	
	@Override
	public Map<String,GenericObjectProperty> getGenericObjectPropertyMap() {
		return propertyMap;
	}


}
