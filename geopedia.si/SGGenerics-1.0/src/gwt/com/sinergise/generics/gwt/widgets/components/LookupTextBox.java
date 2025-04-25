package com.sinergise.generics.gwt.widgets.components;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericActionListener;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.EntityObjectToken;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.core.NotificationHandler;
import com.sinergise.generics.gwt.core.RemoteInspector;
import com.sinergise.generics.gwt.widgetbuilders.GenericWidgetFactory;
import com.sinergise.generics.gwt.widgetbuilders.TableWidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.RemoteTableDataProvider;
import com.sinergise.generics.gwt.widgets.table.GenercsTable;
import com.sinergise.generics.gwt.widgets.table.TableRowSelectionHandler;
import com.sinergise.gwt.ui.HasIcon;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.core.IInputWidget;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.ui.resources.Theme;

public class LookupTextBox extends FlowPanel  implements HasValue<String>, IInputWidget, HasIcon {

	
	protected final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);
	
	private SGTextBox textBox;
	private ImageAnchor btnShowLookup;

	private GenercsTable lookupTableWidget = null;
	private String datasourceId;
	private String lookupWidgetName;
	
	private LookupPanel lookupTablePopup = null;

	private EntityObject lookedUpObject = null;
	
	private TypeAttribute keyAttribute;
	private TypeAttribute valueAttribute;
	private EntityType entityType;
	
	private boolean disabled = false;
	
	private HandlerManager hManager;
	
	public LookupTextBox(Map<String, String> metaAttributes) {
		
		//setStyleName("LookupTextBox");
		hManager = new HandlerManager(this);

		datasourceId = MetaAttributes.readRequiredStringAttribute(metaAttributes,MetaAttributes.LOOKUP_SOURCE);
		lookupWidgetName =  MetaAttributes.readRequiredStringAttribute(metaAttributes,MetaAttributes.LOOKUP_WIDGET);
		String entityTypeName = MetaAttributes.readRequiredStringAttribute(metaAttributes, MetaAttributes.LOOKUP_ENTITYTYPE);
		String lookupKey = MetaAttributes.readRequiredStringAttribute(metaAttributes,MetaAttributes.LOOKUP_KEYS);
		String lookupLabel = MetaAttributes.readRequiredStringAttribute(metaAttributes, MetaAttributes.LOOKUP_LABELS);
		
		entityType = GwtEntityTypesStorage.getInstance().getEntityType(entityTypeName);
		if (entityType == null) {
			throw new RuntimeException("Entity type '"+entityTypeName+"' not found!");
		}
		
		keyAttribute = entityType.getAttribute(lookupKey);
		if (keyAttribute == null) {
			throw new RuntimeException("Lookup key '"+lookupKey+"' not found in entity type '"+entityTypeName+"'!");
		}
		valueAttribute = entityType.getAttribute(lookupLabel);
		if (valueAttribute == null) {
			throw new RuntimeException("Lookup label '"+lookupLabel+"' not found in entity type '"+entityTypeName+"'!");
		}
		
		textBox = new SGTextBox();
		add(textBox);

		
		textBox.addBlurHandler(new BlurHandler() {			
			
			@Override
			public void onBlur(BlurEvent event) {
				valueChanged();
			}
		});
		
		textBox.addKeyPressHandler(new KeyPressHandler() {			
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int charCode = event.getUnicodeCharCode();
				if ((charCode == 0 && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
						|| event.getCharCode() == KeyCodes.KEY_ENTER) {
					valueChanged();
				}
			}
		});
		
		btnShowLookup = new ImageAnchor(Theme.getTheme().standardIcons().search());
		btnShowLookup.addStyleName("btnShowLookup");
		btnShowLookup.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				LookupPanel.createLookupDialog(getTableLookup());
			}
		});
		add(btnShowLookup);
	}

	
	private void valueChanged() {
		String widgetValue = textBox.getValue();
		if (widgetValue == null || widgetValue.trim().length() == 0) {
			return; // do nothing as no value was inputed
		}
		
		if (lookedUpObject != null) {
			String currentValue = lookedUpObject.getPrimitiveValue(valueAttribute.getId());
			if (widgetValue.equals(currentValue))
				return; // do nothing since nothing changed;
		}
		loadByValue(widgetValue);
	}
	private void setLookupData(EntityObject obj) {
		lookedUpObject = obj;
		if (obj!=null) {
			String value = obj.getPrimitiveValue(valueAttribute.getId());
			String key = obj.getPrimitiveValue(keyAttribute.getId());
			textBox.setText(value);
			fireValueChangedEvent(key);
		}
	}

	private GenercsTable getTableLookup() {

		if (lookupTableWidget != null)
			return lookupTableWidget;

		lookupTableWidget = new GenercsTable();
		RemoteTableDataProvider tdProvider = new RemoteTableDataProvider(
				datasourceId);

		lookupTableWidget.setDataProvider(tdProvider);
		lookupTableWidget.addWidgetProcessor(tdProvider);
		lookupTableWidget.setWidgetBuilder(new TableWidgetBuilder());

		final TableRowSelectionHandler trh = new TableRowSelectionHandler(
				lookupTableWidget);
		lookupTableWidget.addEventsHandler(trh);

		GenericWidgetFactory.buildWidget(lookupTableWidget,
				new RemoteInspector(lookupWidgetName ),
				new HashMap<String, String>());

		lookupTableWidget.addCreationListener(new CreationListener() {

			
			@Override
			public void creationCompleted(IsCreationProvider w) {
				lookupTableWidget.repaint();
				
				String title = null;
				Map<String, String> wgMeta = lookupTableWidget
						.getWidgetMetaAttributes();
				if (wgMeta != null) {
					title = MetaAttributes.readStringAttr(wgMeta,
							MetaAttributes.LABEL, "Lookup panel");
					lookupTablePopup.setText(title);
				} 
				lookupTablePopup.center();
			}
		});

		trh.addRowSelectedListener(new GenericActionListener<EntityObjectToken>() {

			
			@Override
			public void actionPerformed(EntityObjectToken object) {
				if (object == null || !object.exists())
					return;
				EntityObject eo = object.getEntityObject();
				setLookupData(eo);

				lookupTablePopup.hide();
			}
		});

		return lookupTableWidget;

	}
	
	private void onLookupEmpty() {
		textBox.setText("");
		lookedUpObject=null;
	}
	
	
	private void loadByKey(String key) {
		if (key == null || key.length() == 0) {
			onLookupEmpty();
			return;
		}
		EntityObject fltrObject = GwtEntityTypesStorage.getInstance().createEntityObject(entityType);
		fltrObject.setPrimitiveValue(keyAttribute.getId(), key);
		lookedUpObject = fltrObject;
		loadByFilter(fltrObject);
	}
	
	private void loadByValue(String value) {
		if (value == null || value.length() == 0) {
			onLookupEmpty();
			return;
		}
		EntityObject fltrObject = GwtEntityTypesStorage.getInstance().createEntityObject(entityType);
		fltrObject.setPrimitiveValue(valueAttribute.getId(), value);
		loadByFilter(fltrObject);
	}
	
	private void loadByFilter (EntityObject filterObject) {
		
		SimpleFilter searchParameters = new SimpleFilter(filterObject);
		
		NotificationHandler.instance().processingStart(LookupTextBox.this);
		genericsService.getCollectionValues(searchParameters, datasourceId, -1, -1, new AsyncCallback<ArrayValueHolder>() {
			
			
			@Override
			public void onSuccess(ArrayValueHolder result) {
				NotificationHandler.instance().processingStop(LookupTextBox.this);
				if (result!=null && result.size()>0) {
					setLookupData((EntityObject) result.get(0));
				} else {
					onLookupEmpty();
				}
			}
			
			
			@Override
			public void onFailure(Throwable caught) {
				NotificationHandler.instance().processingStop(LookupTextBox.this);
				onLookupEmpty();
			}
		});
	}
	
	private void fireValueChangedEvent(String value) {
		ValueChangeEvent.fire(LookupTextBox.this, value);
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
	public String getValue() {
		if (lookedUpObject==null)
			return null;
		return lookedUpObject.getPrimitiveValue(keyAttribute.getId());
	}

	
	@Override
	public void setValue(String value) {
		loadByKey(value);
	}

	
	@Override
	public void setValue(String value, boolean fireEvents) {
		loadByKey(value);		
	}


	
	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		textBox.setEnabled(!disabled);
		btnShowLookup.setEnabled(disabled);
	}


	
	@Override
	public boolean isDisabled() {
		return disabled;
	}


	
	@Override
	public void setTabIndex(int index) {
		textBox.setTabIndex(index);
	}

}
