package com.sinergise.generics.gwt.widgetprocessors;

import static com.sinergise.common.util.lang.TypeUtil.boxI;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.event.ActionPerformedListener;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.EntityObject.Status;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgetbuilders.MutableWidgetBuilder;
import com.sinergise.generics.gwt.widgets.FormTableWidget;

public class FormTableValueBindingProcessor extends WidgetProcessor{

	
	private int maxIdx=0;
	
	FormTableWidget ftGenWidget = null;
	HashMap<Integer,RowBindingProcessor> boundRowMap = new HashMap<Integer, RowBindingProcessor>();
	private MutableWidgetBuilder wgBuilder;
	
		
	public class RowBindingProcessor extends SimpleBindingWidgetProcessor {
		protected EntityObject boundObject = null;
		protected boolean deleted = false;
		
		
		
		public RowBindingProcessor(MutableWidgetBuilder wb) {
			super(wb);
			
		}
		
		public boolean isDeleted() {
			return deleted;
		}
		
		@Override 
		public void load(ValueHolder genericValue, boolean loadDefaults) {
			boundObject=(EntityObject)genericValue;
			super.load(genericValue, loadDefaults);
			Widget w = getWidgetForAttribute(FormTableWidget.ACTION_DELETE);
			if (w!=null) {
				w.setVisible(true);
			}
		}
		
		public EntityObject saveBoundRow() {
			if (boundObject==null) {
				EntityObject eo = GwtEntityTypesStorage.getInstance().createEntityObject(ftGenWidget.getEntityType());
				super.save(eo);
				return eo;
			}
			super.save(boundObject);
			return boundObject;
		}
	}
	
	
	public FormTableValueBindingProcessor (MutableWidgetBuilder wgBuilder) {
		this.wgBuilder = wgBuilder;
	}


	public RowBindingProcessor getRowBindingProcessor(int row) {
		return boundRowMap.get(boxI(row));
	}
	public void setFormTableWidget(FormTableWidget ftGenWidget) {
		this.ftGenWidget = ftGenWidget;
	}
	
	@Override
	public Widget bind(Widget widget, final int idx, GenericObjectProperty property, GenericWidget gw) {
		
		if (idx > maxIdx) maxIdx=idx;
		
		// delete row action
		if (property.isAction() && FormTableWidget.ACTION_DELETE.equals(property.getName())) {
			final Anchor anchor = (Anchor)widget;
			anchor.addClickHandler(new ClickHandler() {
				
				
				@Override
				public void onClick(ClickEvent event) {
					if (!anchor.isEnabled()) return; // do nothing if disabled
					
					RowBindingProcessor proc = boundRowMap.get(boxI(idx));
					if (proc==null) return;
					if (onBeforeRowDelete(proc)) return;
					
					EntityObject eo = proc.boundObject;
					if (eo!=null) {
						if (eo.getStatus() == Status.NEW) { //shouldn't really happen but..
							eo.setStatus(Status.IGNORE);
						} else if (eo.getStatus() == Status.UPDATED 									
								|| eo.getStatus() == Status.STORED) {
							// set status to deleted if current status is stored or updated
							eo.setStatus(Status.DELETED); 
						}
						ftGenWidget.setRowDeleted(idx, true);
						proc.deleted=true;
					} else { // if data is not stored in the database yet, load row with empty data
						eo = GwtEntityTypesStorage.getInstance().createEntityObject(ftGenWidget.getEntityType());
						eo.setStatus(Status.IGNORE);
						proc.load(eo);
						ftGenWidget.setRowDeleted(idx, true);
						proc.deleted=true;
					}
					onAfterRowDeleted(proc);
				}
			});
//			anchor.setVisible(false);
		} 
		final Integer boxIdx = boxI(idx);
		RowBindingProcessor proc = boundRowMap.get(boxIdx);
		if (proc == null) {
			proc = new RowBindingProcessor(wgBuilder);
			boundRowMap.put(boxIdx,proc);
		}
		proc.bind(widget, idx, property, gw);
		
		return widget;
	}
	
	@Override
	public boolean unBind(int idx, GenericObjectProperty property) {
		SimpleBindingWidgetProcessor proc = boundRowMap.get(boxI(idx));
		if (proc != null) {
			if (proc.unBind(idx, property)) boundRowMap.remove(proc);
		}
		
		if (boundRowMap.isEmpty()) return true;
		return false;
	}
	
	public void updateMetaAttribute(String attributeName, String metaAttribute, String value) {
		for (int idx=0;idx<=maxIdx;idx++) {
			SimpleBindingWidgetProcessor rowBinder = boundRowMap.get(boxI(idx));
			if (rowBinder!=null) {
				rowBinder.updateMetaAttribute(attributeName, metaAttribute, value);
			}
		}
	}
	
	public boolean verifyRequiredAttributes () {
		boolean allOk = true;
		for (int idx=0;idx<=maxIdx;idx++) {			
			SimpleBindingWidgetProcessor rowBinder = boundRowMap.get(boxI(idx));
			if (rowBinder!=null) {
				ArrayList<String> missingAttributes = new ArrayList<String>();
				if (!rowBinder.checkRequiredAtributes(missingAttributes)) {
					if (missingAttributes.size()>0) {
						rowBinder.updateMissingAttributesStyle(missingAttributes);
						allOk=false;
					}
				}
			}
		}
		return allOk;
	}
	
	public void load(ValueHolder holder) {
		load(holder, null);
	}
	
	public void load(final ValueHolder holder, final ActionPerformedListener<Void> loadCompleted ) {

		if (holder==null)
			return;
		
		int dataCount = 1;

		if (holder instanceof ArrayValueHolder){
			dataCount = ((ArrayValueHolder)holder).size();
		}
		
		if (ftGenWidget==null) {
			throw new RuntimeException ("Load failed. FormTableWidget reference is null!");
		}
		
		int rowCount = ftGenWidget.getRowCount();
		if (rowCount<dataCount) {
			ftGenWidget.addRows(dataCount-rowCount, new CreationListener() {
				
				@Override
				public void creationCompleted(IsCreationProvider w) {
					actuallyLoadData(loadCompleted, holder);
				}
				
			});
		} else {
			actuallyLoadData(loadCompleted, holder);
		}
	}
	
	
	void actuallyLoadData(final ActionPerformedListener<Void> loadCompleted, ValueHolder holder) {
		
		ArrayValueHolder valuesHolder = null;
		if (holder instanceof ArrayValueHolder){
			valuesHolder = (ArrayValueHolder)holder;
			
		} else if (holder instanceof EntityObject) {
			EntityObject eo = (EntityObject) holder;
			valuesHolder = new ArrayValueHolder(eo.getType());
			valuesHolder.add(eo);
			
		} else {
			throw new IllegalArgumentException("Invalid holder type: "+holder);
		}
		
		for (int i=0; i<valuesHolder.size(); i++) {
			RowBindingProcessor rowBinder = boundRowMap.get(Integer.valueOf(i));
			ftGenWidget.setRowDeleted(i, false);
			if (rowBinder!=null) {
				rowBinder.deleted=false;
				EntityObject eo = (EntityObject) valuesHolder.get(i);				
				rowBinder.load(eo);
			}
		}
		int remain = valuesHolder.size();
		for (int i=(ftGenWidget.getRowCount()-1);i>=remain;i--) {
			ftGenWidget.deleteRow(i);
		}
//
//		EntityObject empty = GwtEntityTypesStorage.getInstance().createEntityObject(boundValues.getEntityTypeId());		
//		for (int i=(ftGenWidget.getRowCount()-1);i>=remain;i--) {
//			ftGenWidget.setRowDeleted(i, true);
//			String hash = Integer.toString(i);
//			SimpleBindingWidgetProcessor rowBinder = boundRowMap.get(hash);
//			rowBinder.load(empty);
//		}
		
		
		if (loadCompleted!=null) {
			loadCompleted.onActionPerformed(null);
		}
	}
	
	public boolean haveValuesChanged(ArrayValueHolder holder) {
		for (int idx=0;idx<=maxIdx;idx++) {
			RowBindingProcessor rowBinder = boundRowMap.get(boxI(idx));
			if (rowBinder!=null) {
				EntityObject eo=rowBinder.boundObject;

				if (eo==null) {
					eo = new AbstractEntityObject(holder.getEntityTypeId());
				}
				if (rowBinder.haveValuesChanged(eo)) return true;
			}
		}
		return false;
		
	}
	public void save(ArrayValueHolder holder) {
		
		for (int idx=0;idx<=maxIdx;idx++) {
			RowBindingProcessor rowBinder = boundRowMap.get(boxI(idx));
			if (rowBinder!=null) {
				EntityObject eo=rowBinder.boundObject;

				if (eo==null) {
					eo = new AbstractEntityObject(holder.getEntityTypeId());
				}
				rowBinder.save(eo);
				if (!eo.isNull() && !holder.contains(eo) && eo.getStatus() != Status.IGNORE) {
					holder.add(eo);
				}
			}
		}
	}
	
	public int getRowCount() {
		return boundRowMap.size();
	}
	
	public void clear() {
		load(new ArrayValueHolder(ftGenWidget.getEntityType()));
	}

	/**
	 * Gets called right before row is deleted. Returning <code>true</code> prevents deletion.
	 * @param proc
	 * @return
	 */
	protected boolean onBeforeRowDelete(RowBindingProcessor proc) {
		return false;
	}
	
	/**
	 * Gets called right after the row is deleted
	 * @param proc
	 */
	protected void onAfterRowDeleted(RowBindingProcessor proc) {
		
	}
}
