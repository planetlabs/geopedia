package com.sinergise.generics.gwt.widgets;

import static com.sinergise.gwt.ui.table.TableStyleConsts.STYLE_ROW_FILTER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityObject.Status;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.CreationResolver;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.IWidgetProcessor;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.widgetbuilders.FilterWidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.FormTableValueBindingProcessor;
import com.sinergise.generics.gwt.widgetprocessors.SimpleBindingWidgetProcessor;
import com.sinergise.generics.gwt.widgetprocessors.helpers.GWTFilterProcessor;
import com.sinergise.generics.gwt.widgets.components.FlexTableBuilderExt;
import com.sinergise.generics.gwt.widgets.i18n.WidgetConstants;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

public class FormTableWidget extends GenericWidget {
	
	private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(FormTableWidget.class); 
	
	public static final String STYLE_BASE="FormTableWidget";
	public static final String ACTION_DELETE="Delete";
	
	private FlexTableBuilderExt ftBuilder;
	
	private List<GenericObjectProperty> propertyList;
	private List<FormRow> widgetRowList;
	private int maxRows = 2;
	private int firstDataRow = 0;
	private Anchor addRowBtn;
	
	private boolean hasFilterRow=false;
	
	private FormTableValueBindingProcessor formValueBinder=null;
	
	/**
	 * Single row widget. It's actually a Generic FormWidget formatted as a table row
	 */
	private class FormRow extends GenericWidget {
		private CreationResolver creationResolver = null;
		private boolean ignoreActions = false;
		
		public IsCreationProvider getCreationProvider() {
			if (creationResolver == null) {
				creationResolver = new CreationResolver();
			}
			return creationResolver;
		}
		
		public FormRow() {
		}
		
		public FormRow(boolean ignoreActions) {
			this.ignoreActions=ignoreActions;
		}

		public Map<String,Widget> boundWidgets = new HashMap<String,Widget>();
		
		@Override
		public void bindProcessors() {
			for (String name:propertyMap.keySet()){
				bindProcessors(boundWidgets.get(name), 0, propertyMap.get(name), this);
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void build(List<GenericObjectProperty> properties, EntityType entityType) {
			setEntityType(entityType);
			creationResolver.begin();
			for (GenericObjectProperty prop:properties) {
				if (prop.isAction() && ignoreActions) {
					ftBuilder.addBlank();
					continue;
				}
				Widget w = null;
				if (!prop.isAction()){ // use widget builder if property is not action
					w = widgetBuilder.buildWidget(prop.getName(),prop.getAttributes());
				} else { // action 
					w = new Anchor(prop.getLabel());
					w.addStyleName(STYLE_BASE+"-Action-"+prop.getName());
				}
				Widget embWidget = w;
				if (embWidget instanceof NoticeableWidgetWrapper) {
					embWidget =((NoticeableWidgetWrapper)w).getWrappedWidget();
				}
				

				if (creationResolver!=null && embWidget instanceof IsCreationProvider) {
					if (!((IsCreationProvider)embWidget).isCreated())
						creationResolver.addCreationProvider((IsCreationProvider)embWidget);
				}
				
				// ignore any labels of check and radio buttons (should be already in table's column)
				if (embWidget instanceof CheckBox) {
					((CheckBox)embWidget).setText("");
				} else if (embWidget instanceof RadioButton) {
					((RadioButton)embWidget).setText("");
				}
				
				ftBuilder.addFieldValueWidget(w);
				boundWidgets.put(prop.getName(),w);
				propertyMap.put(prop.getName(),prop);
			}
			creationResolver.done();
		}
		
	};
	
	

	
	private FlowPanel basePanel;
	
	/**
	 * Returns the number of rows
	 * @return number of rows
	 */
	public int getRowCount() {
		return widgetRowList.size();
	}
	
	
	public FormTableWidget() {
		
		
		basePanel = new FlowPanel();
		ftBuilder = new FlexTableBuilderExt();
		widgetRowList = new ArrayList<FormRow>();
		basePanel.add(ftBuilder.getTable());
		
		/**
		 * Add new row action
		 */
		addRowBtn = new Anchor(WidgetConstants.widgetConstants.btnAddRow());
		addRowBtn.addStyleName(STYLE_BASE+"-AddNewEntry");
		addRowBtn.addClickHandler(new ClickHandler() {
			
			
			@Override
			public void onClick(ClickEvent event) {
				if (addRowBtn.isEnabled())
					addRows(1, null);
			}
		});
		basePanel.add(addRowBtn);
		initWidget(basePanel);
	}
	
	@Override
	public Map<String,GenericObjectProperty> getGenericObjectPropertyMap() {
		return propertyMap;
	}
	
	@Override
	public void addWidgetProcessor (IWidgetProcessor wp) {
		super.addWidgetProcessor(wp);
		if (wp instanceof FormTableValueBindingProcessor) {
			formValueBinder = ((FormTableValueBindingProcessor)wp); 
			formValueBinder.setFormTableWidget(this);
		}
	}
	
	@Override
	protected void bindProcessors() {
		bindProcessors(0,widgetRowList.size());
	}
	
	
	/**
	 * Delete (and unbind) row from the table
	 *  
	 * @param row row number to delete
	 */
	public void deleteRow(int row) {
		FormRow widgetRow = widgetRowList.get(row);
		if (widgetRow!=null) {
			for (String key:propertyMap.keySet()) {
				unbindProcessors(row, propertyMap.get(key));
			}
			widgetRowList.remove(row);
			ftBuilder.removeRow(getFlexTableRow(row));
//			ftBuilder.getTable().removeRow(getFlexTableRow(row));
		}
	}
	
	/**
	 * Just hide the table row - used for items that are marked for deletion.
	 */
	public void setRowDeleted(int row, boolean deleted) {
		int actualDataRow = firstDataRow+row;
		Element tr = ftBuilder.getTable().getRowFormatter().getElement(actualDataRow);
		if (deleted) {
			DOM.setElementProperty(tr, "id", "FormTableWidget-DeletedRow");
		} else { 
			DOM.removeElementAttribute(tr, "id");
		}
	}
	
	private void bindProcessors(int firstIdx, int lastIdx) {
		for (int i=firstIdx;i<lastIdx;i++) {
			FormRow widgetRow = widgetRowList.get(i);
			for (String key:propertyMap.keySet()) {
				bindProcessors(widgetRow.boundWidgets.get(key), i, propertyMap.get(key), this);
			}
		}				
	}
	
	/**
	 * Add new rows to the table
	 * 
	 * @param nRows number of rows to add
	 * @param creationListener creation listener, that's called when the row is completely created
	 */
	public void addRows(int nRows, CreationListener creationListener) {
		int firstRow = widgetRowList.size();
		int lastRow = firstRow+nRows;
		CreationResolver creationResolver = null;
		if (creationListener==null) { // add dummy creation listener if it's not provided
			creationListener = new CreationListener() {
				
				@Override
				public void creationCompleted(IsCreationProvider w) {
				}
			};
		}
		creationResolver = new CreationResolver(FormTableWidget.this);
		creationResolver.addCreationListener(creationListener);
		
		creationResolver.begin();
		for (int i=firstRow;i<lastRow;i++) {
			ftBuilder.newRow();			
			FormRow fr = new FormRow();
			creationResolver.addCreationProvider(fr.getCreationProvider());
			fr.setWidgetMetaAttributes(getWidgetMetaAttributes());
			fr.setWidgetBuilder(widgetBuilder);
			fr.build(propertyList, getEntityType());
			widgetRowList.add(fr);
		}
		bindProcessors(firstRow,lastRow);
		creationResolver.done();
	}

	
	/**
	 * Provides flex table row related to table data row
	 * @param row table data row
	 * @return flex table row of the data row
	 */
	private int getFlexTableRow(int row) {
		if (hasFilterRow)
			return row+2;
		return row+1;
	}
	
	/**
	 * Filter out necessary table rows defined by the filter row.
	 * 
	 */
	private void filterChanged() {
		
		filterBinding.save(filterObject);
		ArrayValueHolder valuesHolder = new ArrayValueHolder(getEntityType());
		formValueBinder.save(valuesHolder);		
		SimpleFilter sFilter = new SimpleFilter(filterObject);
		
		int firstTableRow=getFlexTableRow(0);
		
		GWTFilterProcessor gfp = new GWTFilterProcessor(propertyList);
		for (int i=0;i<valuesHolder.size();i++) {
			EntityObject eo = (EntityObject) valuesHolder.get(i);
			boolean hidden = true;
			if (filterObject.isNull()) { // empty filter - everything visible
				hidden = false;
			} else {
				if (eo.getStatus()==Status.DELETED || eo.getStatus()==Status.IGNORE ||
						eo.isNull()) { // ignore rows with this status or empty rows 
					logger.trace("Ignoring row status: {} is null: {}", eo.getStatus().toString(), Boolean.toString(eo.isNull()));
					hidden = false;
				} else if (gfp.matchesFilter(sFilter, (EntityObject) valuesHolder.get(i))) {
					hidden = false;					
				}
			}
			ftBuilder.setRowHidden(firstTableRow+i, hidden);
		}
		ftBuilder.reformatRows();
	}
	
	private SubmitListener filtersl;
	private SimpleBindingWidgetProcessor filterBinding;
	private EntityObject filterObject;
	
	private class SubmitListener implements KeyPressHandler, ValueChangeHandler<String> {
		
		@Override
		public void onKeyPress(KeyPressEvent event) {
			int charCode = event.getUnicodeCharCode();
			if ((charCode == 0 && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
				|| event.getCharCode() == KeyCodes.KEY_ENTER) 
			{
				filterChanged();
			} 
		  }


		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
			filterChanged();
		}
	 }
	
	
	@Override
	public void build(List<GenericObjectProperty> properties, EntityType entityType) {
		
		hasFilterRow = MetaAttributes.readBoolAttr(widgetMetaAttributes, MetaAttributes.TABLE_FILTERROW, hasFilterRow);
		
		setEntityType(entityType);
		CreationResolver creationResolver = new CreationResolver(FormTableWidget.this);
		creationResolver.addCreationListener(new CreationListener() {			
			
			@Override
			public void creationCompleted(IsCreationProvider w) {
				widgetCreated();
			}
		});
		creationResolver.begin();
		
		// labels;
		propertyList = properties;
		
		//  Add delete row action		 
		propertyList.add(GenericObjectProperty.createSimpleAction(ACTION_DELETE));
		
		// Build first (header) row
		for (GenericObjectProperty prop:properties) {
			if (!prop.isAction()) {
				ftBuilder.addTitle(prop.getLabel());
			} else {
				ftBuilder.addTitle("");
			}
			propertyMap.put(prop.getName(), prop);
		}
		// build filter row if enabled
		if (hasFilterRow) {
			ftBuilder.newRow();
			ftBuilder.addCurrentRowStyle(FlexTableBuilder.DEFAULT_STYLE_TABLE + STYLE_ROW_FILTER);
			FormRow fr = new FormRow(true);
			creationResolver.addCreationProvider(fr.getCreationProvider());
			fr.setWidgetMetaAttributes(getWidgetMetaAttributes());
			filtersl = new SubmitListener();
			filterObject =  new AbstractEntityObject(entityType.getId());
			FilterWidgetBuilder fwBuilder = new FilterWidgetBuilder() {
				@SuppressWarnings("unchecked")
				@Override
				public Widget buildWidget(String attributeName,	Map<String, String> metaAttributes) {
					final Widget filterWidget = super.buildWidget(attributeName, metaAttributes);				
					if (filterWidget instanceof TextBox) {
						((TextBox)filterWidget).addKeyPressHandler(filtersl);
						addFocusHandler(filterWidget);
						addClickHandler(filterWidget);
					} else if (filterWidget instanceof HasValueChangeHandlers<?>) {
						((HasValueChangeHandlers<String>)filterWidget).addValueChangeHandler(filtersl);
					}
					return filterWidget;
				}

			};
			filterBinding = new SimpleBindingWidgetProcessor(fwBuilder);
			fr.addWidgetProcessor(filterBinding);
			fr.setWidgetBuilder(fwBuilder);
			fr.build(properties, entityType);	
			fr.bindProcessors();
		}
		
		// create initial rows
		for (int i=0;i<maxRows;i++) {
			ftBuilder.newRow();
			FormRow fr = new FormRow();
			creationResolver.addCreationProvider(fr.getCreationProvider());
			fr.setWidgetMetaAttributes(getWidgetMetaAttributes());
			fr.setWidgetBuilder(widgetBuilder);
			fr.build(properties, entityType);
			widgetRowList.add(fr);
			if (i==0) firstDataRow = ftBuilder.currentRow();
		}	
		ftBuilder.buildTable();
		creationResolver.done();
	}
	
	
	public void setInitialRows(int maxRows) {
		this.maxRows = maxRows;
		
	}

	public Anchor getAddRowBtn() {
		return addRowBtn;
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
}
