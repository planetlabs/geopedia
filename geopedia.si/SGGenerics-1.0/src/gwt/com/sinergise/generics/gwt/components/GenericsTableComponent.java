package com.sinergise.generics.gwt.components;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.LimitFilter;
import com.sinergise.generics.core.filter.OrderFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsService.DataExportTypes;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.core.NotificationHandler;
import com.sinergise.generics.gwt.core.RemoteInspector;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.resources.GenericsTheme;
import com.sinergise.generics.gwt.widgetbuilders.GenericWidgetFactory;
import com.sinergise.generics.gwt.widgetbuilders.TableWidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.RemoteTableDataProvider;
import com.sinergise.generics.gwt.widgets.i18n.ButtonGenericsConstants;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;
import com.sinergise.generics.gwt.widgets.table.GenercsTable;
import com.sinergise.generics.gwt.widgets.table.TableDataProvider;
import com.sinergise.generics.gwt.widgets.table.TableRowSelectionHandler;
import com.sinergise.gwt.ui.ImageAnchor;

public class GenericsTableComponent extends Composite{
	
	protected final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);

	protected GenercsTable genTable;
	protected TableDataProvider tdProvider;
	protected DataFilter dsUserFilter = null;
	protected DataFilter dsExportFilter = null;
	

	private String widgetName;
	private EnumSet<DataExportTypes> exportSet = EnumSet.noneOf(DataExportTypes.class);
	private List<ImageAnchor> contextButtons = new ArrayList<ImageAnchor>();
	protected HashMap<String,String> widgetAttributesOverride = new HashMap<String,String>();
	protected HashMap<String,Map<String,String>> widgetEntityAttributesOverride =  new HashMap<String, Map<String,String>>();
	private Runnable onBuildFilter = null;
	private FlowPanel contentPanel;
	private TableRowSelectionHandler rowSelectionHandler = null;

	private GenericsTableComponent(String widgetName, TableDataProvider tdProvider, String datasourceName) {
		this(widgetName, tdProvider, new TableWidgetBuilder(), datasourceName);
	}

	public GenericsTableComponent(String widgetName, TableDataProvider tdProvider, TableWidgetBuilder tableWidgetBuilder, String datasourceName) {
		this.widgetName = widgetName;
	
		if (tdProvider == null) {
			tdProvider = new RemoteTableDataProvider(datasourceName) {
				@Override
				public DataFilter getUserFilter() {
					return dsUserFilter;
				}
			};
		}
		this.tdProvider = tdProvider;
		genTable=new GenercsTable();
		genTable.setDataProvider(tdProvider);
		genTable.addWidgetProcessor(tdProvider.getWidgetProcessor());
		genTable.setWidgetBuilder(tableWidgetBuilder);
		contentPanel = new FlowPanel();
		contentPanel.setStyleName("genericsMainTableHolder");
		contentPanel.add(genTable);
		initWidget(contentPanel);
	}

	public GenericsTableComponent(String widgetName, TableDataProvider tdProvider) {
		this(widgetName,tdProvider,null);
	}
	
	public GenericsTableComponent(String widgetName, String datasourceName) {
		this(widgetName,null,datasourceName);
	}
	
	/**
	 * Table EntityAttributes metadata overrides.  Overrides must be applied BEFORE the widget is created (before build call)
	 * @return
	 */
	public  HashMap<String,Map<String,String>> getAttributesOverrideMap() {
		return widgetEntityAttributesOverride;
	}
	
	/**
	 * Widget metadata overrides.   Overrides must be applied BEFORE the widget is created (before build call)
	 * @return
	 */
	public HashMap<String,String> getTableMetaAttributesOverrides() {
		return widgetAttributesOverride;
	}
	
	/**
	 * Build widget without loading data
	 */
	public void build() {
		build(false);
	}
	
	public void enableDataExport(DataExportTypes type) {
		exportSet.add(type);
	}

    public void enableContextButton(ImageAnchor button) {
        contextButtons.add(button);
    }
	
	/**
	 * Build widget
	 * 
	 * @param loadDataOnBuild  - load data after the widget is built
	 */
	public void build(final boolean loadDataOnBuild) {		
			
		if (loadDataOnBuild || onBuildFilter!=null) {
			genTable.addCreationListener(new CreationListener() {
				@Override
				public void creationCompleted(IsCreationProvider cp) {
					if (onBuildFilter!=null)
						onBuildFilter.run();
					if (loadDataOnBuild)
						genTable.repaint();
				}
			});
		}
		GenericWidgetFactory.buildWidget(
				genTable,
				new RemoteInspector(widgetName),
				widgetAttributesOverride,
				widgetEntityAttributesOverride
				);
		
		
		 if (tdProvider instanceof RemoteTableDataProvider && (exportSet.size() > 0 || contextButtons.size() > 0)) {
			 FlowPanel pnlContextButtons = new FlowPanel();
			 pnlContextButtons.setStyleName("exportButtonsPanel");
			 contentPanel.add(pnlContextButtons);
			 FlowPanel holder = new FlowPanel();
			 contentPanel.add(holder);

             for (ImageAnchor imageAnchor: contextButtons) {
                 pnlContextButtons.add(imageAnchor);
             }
			 
			 for (DataExportTypes type: exportSet) {
				 pnlContextButtons.add(createExportToButton(type, holder));
			 }
		 }
	}
	
    private ImageAnchor createExportToButton(final DataExportTypes type, final FlowPanel downloadIFrameHolder) {
        final RemoteTableDataProvider rTDP = (RemoteTableDataProvider) tdProvider;
        ImageAnchor btnExport = new ImageAnchor();
        switch (type) {
        case CSV:
            btnExport.setText(ButtonGenericsConstants.INSTANCE.exportCSV());
            btnExport.setImageRes(GenericsTheme.getGenericsTheme().csv());
            break;
        case XLSX:
            btnExport.setText(ButtonGenericsConstants.INSTANCE.exportExcel());
            btnExport.setImageRes(GenericsTheme.getGenericsTheme().excel());
            break;

        default:
            break;
        }
        btnExport.addStyleName(type.name());
        btnExport.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                exportHandle(type, downloadIFrameHolder, rTDP);

            }
        });
        return btnExport;
    }
    
	public static final native void setIFrameSrc(IFrameElement iframe, String src) /*-{
		iframe.src = src;
		iframe.style.display = 'none';
	}-*/;


	/**
	 * Builds a simple (single TypeAttribute=value pair) filter for underlying datasource's EntityType. 
	 * 
	 * This method may only be called AFTER the component has been built - build method has been called and has finished 
	 * (Mind, build is an asynchronous process!)
	 * 
	 * @param typeAttribute - DataSource entity TypeAttribute name
	 * @param value - value
	 * @return SimpleFilter 
	 */
	public SimpleFilter buildSimpleFilter(String typeAttribute, String value) {
		return buildSimpleFilter(DataFilter.OPERATOR_AND, typeAttribute,value);		
	}
	
	/**
	 * Builds a simple (TypeAttribute=value pairs, joined by the operator) filter for underlying datasource's EntityType. 
	 * 
	 * This method may only be called AFTER the component has been built - build method has been called and has finished 
	 * (Mind, build is an asynchronous process!)
	 * 
	 * @param operator (DataFilter.OPERATOR_AND, DataFilter.OPERATOR_OR)
	 * @param keyValuePairs (TypeAttribute=value pairs)
	 * @return SimpleFilter
	 */
	public SimpleFilter buildSimpleFilter(byte operator, String... keyValuePairs) {
		if (keyValuePairs.length==0)
			return null;
		
		if (keyValuePairs.length%2!=0)
			throw new IllegalArgumentException("Must contain even number of keyValuePairs arguments!");
		if (!genTable.isCreated())
			throw new RuntimeException("Build has to be called before configuring filters!");
		EntityObject filterObject = GwtEntityTypesStorage.getInstance().createEntityObject(genTable.getEntityType());
		EntityType et = filterObject.getType();
		for (int i=0;i<keyValuePairs.length/2;i++) {
			String typeAttribute = keyValuePairs[i*2];
			String value = keyValuePairs[i*2+1];
			TypeAttribute ta = et.getAttribute(typeAttribute);
			if (ta==null) throw new IllegalArgumentException("No TypeAttribute '"+typeAttribute+"' in EntityType '"+et.getName()+"'!");
			filterObject.setPrimitiveValue(ta.getId(), value);
		}
		return new SimpleFilter(filterObject,operator);		
	}
	
	
	public void reloadData() {
		if (genTable.isCreated())
			genTable.repaint();
	}
	
	public GenercsTable getTableWidget() {
		return genTable;
	}
	
	public TableDataProvider getDataProvider() {
		return tdProvider;
	}
	
	public void addWidgetProcessor(WidgetProcessor processor) {
		genTable.addWidgetProcessor(processor);
	}


	public void addCreationListener(CreationListener listener) {
		genTable.addCreationListener(listener);
		
	}

	
	public void createAndSetSimpleFilter(final byte operator, final String... keyValuePairs) {
		if (genTable.isCreated())
			setUserFilter(buildSimpleFilter(operator, keyValuePairs));
		else {
			onBuildFilter = new Runnable() {
				@Override
				public void run() {
					setUserFilter(buildSimpleFilter(operator, keyValuePairs));
				}
			};
		}
	}
	public void createAndSetSimpleFilter(final String typeAttribute, final String value) {
		createAndSetSimpleFilter(DataFilter.OPERATOR_AND, typeAttribute,value);	
	}
	
	/**
	 * Apply user filter for the table
	 * @param userFilter
	 */
	public void setUserFilter(DataFilter userFilter) {
		this.dsUserFilter = userFilter;
	}

	/**
	 * Return displayed table data (just the displayed page)
	 * @return
	 */
	public ArrayValueHolder getTableData() {
		return getDataProvider().getTableData();
	}	
	
	/**
	 * Retrieve table data from start to end row. The call is asynchronous.
	 * 
	 * @param startRow
	 * @param endRow
	 * @param callback
	 */
	public void getTableData(int startRow, int endRow, AsyncCallback<ArrayValueHolder> callback) {
		getDataProvider().provideRowData(startRow, endRow, getTableWidget(), callback);
	}
	
	/**
	 * Retrieve complete table data. The call is asynchronous.
	 * 
	 * @param callback
	 */
	public void getTableData(AsyncCallback<ArrayValueHolder> callback) {		
		getTableData(-1,-1, callback);
	}

	public TableRowSelectionHandler getRowSelectionHandler() {
		if (rowSelectionHandler==null)  {
			rowSelectionHandler = new TableRowSelectionHandler(genTable);
			genTable.addEventsHandler(rowSelectionHandler);
		}
		return rowSelectionHandler;
	}

	protected void exportHandle(final DataExportTypes type,
			final FlowPanel downloadIFrameHolder,
			final RemoteTableDataProvider rTDP) {
		
		ArrayList<GenericObjectProperty> exportAttributes = new ArrayList<GenericObjectProperty>();
		for (GenericObjectProperty att:genTable.getAttributes()) {
			if (!att.isAction() && att.isExportable()) exportAttributes.add(att);
		}

		CompoundDataFilter filter = new CompoundDataFilter();
		filter.addDataFilter(genTable.getSearchParameters(), DataFilter.NO_FILTER);
		filter.setOrderFilter(genTable.getOrderParameters());
		if (dsUserFilter!=null) {
			filter.addDataFilter(dsUserFilter, DataFilter.OPERATOR_AND);
		}
		if (dsExportFilter!=null) {
			// TODO add logic for compound filter
			if(dsExportFilter instanceof LimitFilter) filter.setLimitFilter((LimitFilter)dsExportFilter);
			else if (dsExportFilter instanceof OrderFilter) filter.setOrderFilter((OrderFilter)dsExportFilter);
			else filter.addDataFilter(dsExportFilter, DataFilter.OPERATOR_AND);
		}
		NotificationHandler.instance().processingStart();
		genericsService.prepareExportFile(filter, rTDP.getDatasourceId(), 
				exportAttributes.toArray(new GenericObjectProperty[exportAttributes.size()]),
				type, 
				new AsyncCallback<String>() {

		    @Override
		    public void onFailure(Throwable caught) {
		    	NotificationHandler.instance().processingStop();
		    	NotificationHandler.instance().showMessage(
		    			WidgetConstants.widgetConstants.exportError(), WidgetConstants.widgetConstants.exportError(), MessageType.ERROR);
		    }

		    @Override
		    public void onSuccess(String result) {
		    	NotificationHandler.instance().processingStop();
		    	if (result==null) {
		        	NotificationHandler.instance().showMessage(
		        			WidgetConstants.widgetConstants.exportError(), WidgetConstants.widgetConstants.exportError(), MessageType.ERROR);
		        	return;
		    	}
		        downloadIFrameHolder.clear();
		        IFrameElement downloadIFRame = Document.get().createIFrameElement();
		        setIFrameSrc(downloadIFRame, GWT.getModuleBaseURL()+"generics?"
		        		+ GenericsService.PARAM_CMD+"="+GenericsService.CMD_DOWNLOAD
		        		+ "&"+GenericsService.PARAM_FILE+"="+result);
		        downloadIFrameHolder.getElement().appendChild(downloadIFRame);
		    }
		});
	}
	
	public DataFilter getDsExportFilter() {
		return dsExportFilter;
	}

	public void setDsExportFilter(DataFilter dsExportFilter) {
		this.dsExportFilter = dsExportFilter;
	}
}
