/**
 * 
 */
package com.sinergise.gwt.ui.controls.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.common.ui.controls.object.ObjectStringRenderer;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.event.selection.ObjectSelectionListener;
import com.sinergise.common.util.event.selection.ObjectSelectionListenerCollection;
import com.sinergise.common.util.event.selection.ObjectSelector;

/**
 * @author tcerovski
 */
public class ObjectListBox<T> extends Composite implements ObjectSelector<T> {

	private boolean mandatory;
	private List<T> model;
	protected ListBox view;
	private T selected = null;
	protected T defaultSelected = null;
	
	private ObjectStringRenderer<T> itemRenderer = null;
	
	private ObjectSelectionListenerCollection<T> selectionListeners = new ObjectSelectionListenerCollection<T>();
	
	public ObjectListBox() {
		this(false);
	}
	
	public ObjectListBox(boolean selectionMandatory) {
		this(selectionMandatory, false);
	}
	
	public ObjectListBox(boolean selectionMandatory, boolean isMultipleSelect) {
		this.mandatory = selectionMandatory;
		view = new ListBox(isMultipleSelect);
		initWidget(view);
		
		view.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				processChange();
			}
		});
		processChange();
	}
	
	public HandlerRegistration addClickHandler(ClickHandler ch) {
		return view.addClickHandler(ch);
	}
	
	public void addSelectionListener(ObjectSelectionListener<T> listener) {
		selectionListeners.add(listener);
	}
	
	public void removeSelectionListener(ObjectSelectionListener<T> listener) {
		selectionListeners.remove(listener);
	}
	
	public boolean isSelectionMandatory() {
		return mandatory;
	}
	
	public void setSelectionMandatory(boolean mandatory) {
		if (this.mandatory != mandatory) {
			this.mandatory = mandatory;
			if (model == null) {
				return;
			}
			if (mandatory) {
				model.add(0, null);
			} else {
				model.remove(0);
			}
			processChange();
		}
	}
	
	public boolean isMulitpleSelect() {
		return view.isMultipleSelect();
	}
	
	public void setItemStringRenderer(ObjectStringRenderer<T> renderer) {
		this.itemRenderer = renderer;
	}
	
	public Collection<T> getAllSelected() {
		List<T> selectedList = new ArrayList<T>();
		for(int i=0; i<view.getItemCount(); i++) {
			if(view.isItemSelected(i)) {
				selectedList.add(model.get(i));
			}
		}
		return selectedList;
	}
	
	public final void setSelected(T selected) {
		
		if (Util.safeEquals(this.selected, selected)
			&& model.indexOf(selected) == view.getSelectedIndex()) 
		{
			return;
		}
		
		this.selected = selected;
		if (selected == null) {
			view.setSelectedIndex(0);
			processChange();
			return;
		}
		
		this.defaultSelected = selected;
		if(model != null) {
			for (int i = 0; i < model.size(); i++) {
				T modelObj = model.get(i);
				if (selected.equals(modelObj)) {
					selected = modelObj; // model object should be treated as the reference instance
					view.setSelectedIndex(i);
					processChange();
					return;
				}
			}
		}
		this.selected = null;
		view.setSelectedIndex(0);
		processChange();
	}
	
	public void setVisibleItemCount(int i) {
		view.setVisibleItemCount(i);
	}
	
	public final T getSelected() {
		return selected;
	}
	
	public final void clear() {
		selected = null;
		clearModel();
		updateView();
	}
	
	private void clearModel() {
		if(model == null)
			return;
		
		model.clear();
		if(!mandatory)
			model.add(null);
	}
	
	public final void setItems(T[] items) {
		clearModel();
		for (T item:items) {
			_intAddItem(item);
		}
		
		updateView();
	}
	
	public final void setItems(Collection<? extends T> items) {
		clearModel();
		for (T item : items) {
			_intAddItem(item);
		}
		updateView();
	}
	
	public final void addItem(T item) {
		_intAddItem(item);
		updateView();
	}
	
	private void _intAddItem(T item) {
		if(model == null)
			_newModel();
		model.add(item);
		
		if (mandatory && defaultSelected == null && model.size() == 1) {
			defaultSelected = item;
		}
	}
	
	private void _newModel() {
		model = new ArrayList<T>();
		if(!mandatory)
			model.add(null);
	}

	protected String getItemLabel(T item) {
		if(itemRenderer != null) {
			return itemRenderer.getStringRepresentation(item);
		}
		return item.toString();
	}
	
	protected void addItemToView(T item) {
		view.addItem(getItemLabel(item));
	}

	protected final void updateView() {
		view.clear();
		if(model != null) {
			for(T item : model) {
				if (item == null) {
					view.addItem("", (String) null);
				} else {
					addItemToView(item);
					if (selected == null && item.equals(defaultSelected)) {
						selected = defaultSelected;
					}
				}
			}
			setSelected(selected);
		}
	}

	public void processChange() {
		if (model != null && view.getSelectedIndex() >= 0) {
			T newSel = model.get(view.getSelectedIndex());
			if (newSel != selected) {
				selected = newSel;
				selectionListeners.fireObjectSelected(selected, ObjectListBox.this);
			}
		}
	}

	public List<T> getItems() {
		return model;
	}
	
	public int getItemCount() {
		return model.size();
	}
	
	public void setEnabled(boolean enabled){
		view.setEnabled(enabled);
	}
	
	public boolean hasItem(T item) {
		return model.contains(item);
	}
}
