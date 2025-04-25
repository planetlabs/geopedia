package com.sinergise.generics.gwt.widgetprocessors;

import java.util.Date;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.LookupPrimitiveValue;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.PrimitiveValue;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.gwt.core.GWTAttributeUtils;
import com.sinergise.generics.gwt.core.GWTMetaAttributeUtils;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.NotificationHandler;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgets.components.LookupListBox;
import com.sinergise.generics.gwt.widgets.table.GenericTableModel;
import com.sinergise.generics.gwt.widgets.table.HasTableData;
import com.sinergise.generics.gwt.widgets.table.TableEventsHandler;

public class TableValueBinderWidgetProcessor extends WidgetProcessor implements HasTableData {

	
	protected ArrayValueHolder tableData;
	
	public void setTableData(ArrayValueHolder tableData) {
		this.tableData=tableData;
	}
	
	@Override
	public ArrayValueHolder getTableData() {
		return tableData;
	}
	
	@Override
	public Widget bind(Widget widget, int idx, GenericObjectProperty property, GenericWidget gw) {
		if (property.isAction())
			return widget;
		if (widget==null)
			widget = new Label();
		
	
		EntityObject eo = (EntityObject) tableData.get(idx);
		PrimitiveValue primitiveValue = EntityUtils.getPrimitiveValue(eo, property.getName());
		if (primitiveValue == null) // TODO: no value is ignored atm, something might get written here, but can be overriden by a custom widget processor
			return widget;
		
		String value = primitiveValue.value;
		
		if (GWTAttributeUtils.isTrue(property, MetaAttributes.LOOKUP)) { // TODO: check if table has resolve lookups enabled (pass as argument to the binder)
			if (primitiveValue instanceof LookupPrimitiveValue) {
				LookupPrimitiveValue lpv = (LookupPrimitiveValue) primitiveValue;
				if (lpv.lookedUpValue!=null) // don't use lookup if it's undefined..
					value = lpv.lookedUpValue;
			}
		}
		
		Map<String,String> metaAttributes = property.getAttributes();
		if (!StringUtil.isNullOrEmpty(value)) {
			String format = MetaAttributes.readStringAttr(property.getAttributes(), MetaAttributes.VALUE_FORMAT,null);
			
			if (MetaAttributes.isType(metaAttributes,Types.DATE)) {
				DateTimeFormat dtf=GWTMetaAttributeUtils.getDateTimeFormat(format);
				value=dtf.format(new Date(Long.parseLong(value)));
				
			} else if (format !=null && (MetaAttributes.isType(metaAttributes,Types.INT) ||
					MetaAttributes.isType(metaAttributes,Types.FLOAT))) {
				NumberFormat nf = NumberFormat.getFormat(format);
				Double dblVal = Double.valueOf(value);
				value = nf.format(dblVal);
				
			} else if (MetaAttributes.isType(metaAttributes, Types.BOOLEAN) && widget instanceof CheckBox) {
				Boolean bVal = Boolean.valueOf(value);
				((CheckBox)widget).setValue(bVal);
				return widget;
			}
			
		}
		if (value!=null) {
			if (widget instanceof HasText) {
				((HasText)widget).setText(value);// only process label widgets
			} else if (widget instanceof LookupListBox) {
				((LookupListBox)widget).setValue(value, false);
			}
		}
			
		return widget;
	}
	
	protected  class TableDataProviderCallback implements AsyncCallback<ArrayValueHolder> {
		
		private GenericTableModel model;
		public TableDataProviderCallback(GenericTableModel model) {
			this.model = model;
		}
		
		@Override
		public void onSuccess(ArrayValueHolder result) {
			notifyStopProcessing();
			tableData = result;
			if (tableData==null) {
				model.setRowCount(0);
			} else { 
				model.setRowCount(tableData.size());
				model.setDataLocation(tableData.getDataLocationStart(), tableData.getDataLocationEnd(), tableData.hasMoreData());
				model.setTotalRecordsCount(tableData.getTotalDataCount());
				model.updateTable();
			}

			for (TableEventsHandler teh:model.getEventsHandlerCollection()) {
				teh.newTableDataReceived(result);
			}
		}
		
		@Override
		public void onFailure(Throwable caught) {
			notifyStopProcessing(); // TODO send error somewhere
			model.setRowCount(0);
			model.updateTable();
			NotificationHandler.instance().handleException(caught);
		}
	}

	
	protected void notifyStopProcessing() {
		NotificationHandler.instance().processingStop();
	}
	protected void notifyStartProcessing(){
		NotificationHandler.instance().processingStart();
	}
	
	
}
