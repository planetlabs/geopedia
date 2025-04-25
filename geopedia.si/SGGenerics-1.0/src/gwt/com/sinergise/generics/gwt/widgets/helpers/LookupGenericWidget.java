package com.sinergise.generics.gwt.widgets.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericActionListener;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.EntityObjectToken;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.core.NotificationHandler;
import com.sinergise.generics.gwt.core.RemoteInspector;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgetbuilders.GenericWidgetFactory;
import com.sinergise.generics.gwt.widgetbuilders.MutableWidgetBuilder;
import com.sinergise.generics.gwt.widgetbuilders.TableWidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.AbstractWidgetActionsProcessor;
import com.sinergise.generics.gwt.widgetprocessors.RemoteTableDataProvider;
import com.sinergise.generics.gwt.widgetprocessors.SimpleBindingWidgetProcessor;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;
import com.sinergise.generics.gwt.widgets.table.GenercsTable;
import com.sinergise.generics.gwt.widgets.table.TableRowSelectionHandler;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.UniversalPanel;
import com.sinergise.gwt.ui.core.IInputWidget;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.resources.Theme;

public class LookupGenericWidget extends UniversalPanel implements IInputWidget, HasValueChangeHandlers<String>, IsCreationProvider, HasValue<String> {
	
	private final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(LookupGenericWidget.class); 
	
	public static final String STUB_LOOKUP_BUTTON="LookupButton";
	public static final String WIDGET_NAME = "MasonWidget";
	private final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	
	private String datasourceId = null;
	private TypeAttribute keyAttribute = null;
	private EntityType entityType = null;
	private SimpleBindingWidgetProcessor bindingProcessor;
	private EntityObject currentValue;
	private boolean widgetDone = false;
	private String keyValue = null;
	private String lookupWidgetName;
	
	private GenercsTable lookupTableWidget  = null;
	private LookupPanel lookupTablePopup = null;
	private ImageAnchor tableLookupBT = null;
	private GenericWidget widget = null;
	private boolean disabled = false;
	private ArrayList<CreationListener> creationListeners = new ArrayList<CreationListener>();
	private HandlerManager hManager;
	private boolean isCreated = false;
	
	private DataFilter lookupFilter = null;
	
	
	public class LookupPanel extends AbstractDialogBox {
	    private FlowPanel content = new FlowPanel();
	    
	    public LookupPanel() {
	    	super(false, true, true, true);
	        setWidget(content);
	        setText(WidgetConstants.widgetConstants.lookupDialog());
	    }

	    public void setContent(GenericWidget w) {
	    	content.clear();
	    	content.add(w);
	    	content.add(createCloseButton());
	    }

	    @Override
		protected boolean dialogResizePending(int width, int height) {
			return true;
		}
	}
	
	
	
	private void showTableLookupPopup() {
		if (lookupTablePopup==null) {
			lookupTablePopup = new LookupPanel();
			lookupTablePopup.setContent(getTableLookup());
		} 
		
		lookupTablePopup.center();
		
	}
	private GenercsTable getTableLookup() {
		
		if (lookupTableWidget!=null)
			return lookupTableWidget;
		
		lookupTableWidget = new GenercsTable();
		
		
		RemoteTableDataProvider tdProvider = new RemoteTableDataProvider(datasourceId) {
			@Override
			public DataFilter getUserFilter() {
				return lookupFilter;
			}
		};
		lookupTableWidget.setDataProvider(tdProvider);
		lookupTableWidget.addWidgetProcessor(tdProvider);
		lookupTableWidget.setWidgetBuilder(new TableWidgetBuilder());
		
		
		final TableRowSelectionHandler trh = new TableRowSelectionHandler(lookupTableWidget);
		lookupTableWidget.addEventsHandler(trh); 
		
		GenericWidgetFactory.buildWidget(
				lookupTableWidget,
				new RemoteInspector(lookupWidgetName+"-table"),
				new HashMap<String,String>()
				);
				
		
		lookupTableWidget.addCreationListener(new CreationListener() {
			
			
			@Override
			public void creationCompleted(IsCreationProvider w) {
				lookupTableWidget.repaint();
				if (lookupTablePopup!=null) {
					String title = null;
			    	Map<String,String> wgMeta = lookupTableWidget.getWidgetMetaAttributes();
			    	if (wgMeta!=null) {
			    		title = MetaAttributes.readStringAttr(wgMeta, MetaAttributes.LABEL, null);
			    		if (title!=null) {
			    	    	lookupTablePopup.setText(title);
			    		}
			    		lookupTablePopup.center();
			    	}
				}
			}
		});
		
		trh.addRowSelectedListener(new GenericActionListener<EntityObjectToken>() {
			
			
			@Override
			public void actionPerformed(EntityObjectToken object) {
				if (object==null || !object.exists())
					return;
				EntityObject eo = object.getEntityObject();
				setLookupData(eo);
				
				if (lookupTablePopup != null)
					lookupTablePopup.hide();
			}
		});
		
		return lookupTableWidget;
		
	}
	
	private class LookupWidgetActionsProcessor extends AbstractWidgetActionsProcessor {

		private MutableWidgetBuilder wBuilder = null;
		
		public LookupWidgetActionsProcessor (MutableWidgetBuilder wBuilder) {
			this.wBuilder = wBuilder;
		}
		
		@Override
		protected void widgetValueChanged(Widget w, String attributeName, Map<String,String> metaAttributes) {
			if (keyAttribute==null)
				return;
			if (attributeName.equals(keyAttribute.getName())) {
				String value = (String) wBuilder.getWidgetValue(w,attributeName, metaAttributes);
				updateWidgetValue(value);
			}
			
		}
		
	}
	
	
	public LookupGenericWidget(GenericWidget gw, MutableWidgetBuilder wgBuilder, Map<String,String> metaAttributes) {
		this.widget = gw;
		hManager = new HandlerManager(this);
		enableLoading(true);
		logger.trace("Initializing "+LookupGenericWidget.class.getName());
		setStyleName("LookupGenericWidget");
		add(gw);
		
		
		
		gw.setWidgetBuilder(wgBuilder);
		
		bindingProcessor = new SimpleBindingWidgetProcessor(wgBuilder);
		gw.setWidgetBuilder(wgBuilder);
		gw.addWidgetProcessor(new WidgetProcessor() {
	
			
			@Override
			public Widget bind(Widget widget, int idx, GenericObjectProperty property,
					GenericWidget gw) {
				if (STUB_LOOKUP_BUTTON.equals(property.getName())) {
					((SimplePanel)widget).add(createLookupButton());
				}
				return widget;
			}
		});
		gw.addWidgetProcessor(bindingProcessor);		
		gw.addWidgetProcessor(new LookupWidgetActionsProcessor(wgBuilder));
		logger.trace("Reading widget attributes.");
		String entityTypeName = MetaAttributes.readRequiredStringAttribute(metaAttributes, MetaAttributes.LOOKUP_ENTITYTYPE);
		lookupWidgetName = MetaAttributes.readRequiredStringAttribute(metaAttributes,MetaAttributes.LOOKUP_WIDGET);
		datasourceId = MetaAttributes.readRequiredStringAttribute(metaAttributes,MetaAttributes.LOOKUP_SOURCE);
		String keyAttributeName = MetaAttributes.readRequiredStringAttribute(metaAttributes,MetaAttributes.LOOKUP_KEYS);
		entityType=GwtEntityTypesStorage.getInstance().getEntityType(entityTypeName);
		keyAttribute = entityType.getAttribute(keyAttributeName); // TODO: check for nonnull
		
		logger.trace("Building widget");
		GenericWidgetFactory.buildWidget(
				gw,
				new RemoteInspector(lookupWidgetName));
		
		gw.addCreationListener(new CreationListener() {
			
			@Override
			public void creationCompleted(IsCreationProvider cp) {
//				((GenericWidget)cp).bindProcessors();
				widgetDone = true;
				if (keyValue!=null) {
					updateWidgetValue(keyValue);
				}
				fireCreationCompleted();
			}
		});
	}
	
	
	
	private ImageAnchor createLookupButton() {
		tableLookupBT = new ImageAnchor(WidgetConstants.widgetConstants.lookupGenericWidgetLookupButton(), Theme.getTheme().standardIcons().search());
		tableLookupBT.addClickHandler(new ClickHandler() {
			
			
			@Override
			public void onClick(ClickEvent event) {
				if (disabled)
					return;
				showTableLookupPopup();
			}
		});
		return tableLookupBT;
	}
	
	
	@Override
	public void setValue(String newKeyValue) {
		setValue(newKeyValue, false);
	}
	
	
	
	@Override
	public void setValue(String newKeyValue, boolean fireEvents) {
		if (keyValue==null && newKeyValue==null)
			return;
		if (keyValue!=null && keyValue.equals(newKeyValue))
			return;
		keyValue = newKeyValue;
		if (!widgetDone)
			return;
		updateWidgetValue(newKeyValue);
//		fireValueChangedEvent(keyValue);
	}
	
	
	@Override
	public String getValue() {
		if (currentValue != null)
			return currentValue.getPrimitiveValue(keyAttribute.getId());
		else if (keyValue!=null) // return set value. It might be different from looked up value
			return keyValue;
		return null;
	}
		
	
	public EntityObject getLookedUpEntityObject() {
		return currentValue;
	}
	
	private void updateWidgetValue(final String newValueLookup) {
		
		if (newValueLookup==null || newValueLookup.length()==0) { // empty value
			EntityObject emptyEO =  GwtEntityTypesStorage.getInstance().createEntityObject(widget.getEntityType());
			setLookupData(emptyEO);
			return;
		}
		final EntityObject filterObject = new AbstractEntityObject(entityType.getId());
		filterObject.setPrimitiveValue(keyAttribute.getId(), newValueLookup);
		
		NotificationHandler.instance().processingStart(LookupGenericWidget.this);
		
		CompoundDataFilter fltr = new CompoundDataFilter();
		fltr.addDataFilter(new SimpleFilter(filterObject), DataFilter.NO_FILTER);
		if (lookupFilter != null) {
			fltr.addDataFilter(lookupFilter, DataFilter.OPERATOR_AND);
		}
		genericsService.getCollectionValues(fltr, datasourceId, -1, -1, new AsyncCallback<ArrayValueHolder>() {

			
			@Override
			public void onFailure(Throwable caught) {
				NotificationHandler.instance().processingStop(LookupGenericWidget.this);
				logger.error("Exception while querying datasource '"+datasourceId+"' with "+keyAttribute.getName()+"="+newValueLookup+".",caught);
				NotificationHandler.instance().handleException(caught);
			}

			
			@Override
			public void onSuccess(ArrayValueHolder result) {
				NotificationHandler.instance().processingStop(LookupGenericWidget.this);
				if (result == null || result.size()==0) { // TODO handle "missing key"!?
					NotificationHandler.instance().showMessageNextTo(LookupGenericWidget.this,
							WidgetConstants.widgetConstants.noResults(), WidgetConstants.widgetConstants.noResultsFound(), MessageType.INFO);
					currentValue = null;
					keyValue = null;
					bindingProcessor.load(filterObject);
					logger.trace("No results were found while querying datasource '"+datasourceId+"' with "+keyAttribute.getName()+"="+newValueLookup+".");
					return;
				}
				setLookupData((EntityObject) result.get(0));
			}
		});
	}
	

	private void setLookupData(EntityObject obj) {
		currentValue = obj;
		bindingProcessor.load(currentValue);
		if (StringUtil.compare(getValue(), keyValue)!=0) {
			keyValue=getValue();
			fireValueChangedEvent(getValue());
		}
	}
	
	
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}
	
	@Override
	public void setDisabled(boolean disabled) {
		if (this.disabled==disabled)
			return;
		this.disabled = disabled;
		if (tableLookupBT!=null) {
			tableLookupBT.setEnabled(!disabled);
		}
		bindingProcessor.updateMetaAttribute(keyAttribute.getName(), MetaAttributes.DISABLED, (disabled?MetaAttributes.BOOLEAN_TRUE:MetaAttributes.BOOLEAN_FALSE) );
	}
	
	
	
	@Override
	public void setTabIndex(int index) {
		// TODO implement??
	}
	
	
	private void fireValueChangedEvent(String value) {
		ValueChangeEvent.fire(LookupGenericWidget.this, value);
	}
	
	@Override
	public void fireEvent(GwtEvent<?> event) {
		if (event instanceof ValueChangeEvent<?>)
			hManager.fireEvent(event);
		else 
			super.fireEvent(event);
	}
	
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return hManager.addHandler(ValueChangeEvent.getType(), handler);
	}
	
	
	@Override
	public void addCreationListener(CreationListener l) {
		creationListeners.add(l);
	}
	
	private void fireCreationCompleted() {
		for (CreationListener l:creationListeners) {
			l.creationCompleted(this);
		}
		isCreated = true;
	}
	
	@Override
	public boolean isCreated() {
		return isCreated;
	}
	
	public void setLookupFilter (DataFilter filter) {
		lookupTablePopup=null;
		lookupTableWidget=null;
		this.lookupFilter = filter;
	}
	
	public DataFilter getLookupFilter () {
		return lookupFilter;
	}
}
