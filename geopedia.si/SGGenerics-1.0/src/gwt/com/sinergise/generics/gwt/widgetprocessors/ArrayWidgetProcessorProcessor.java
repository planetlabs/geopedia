package com.sinergise.generics.gwt.widgetprocessors;

import java.util.HashMap;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.gwt.core.ArrayWidgetProcessor;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.WidgetProcessor;

public abstract class ArrayWidgetProcessorProcessor<T extends WidgetProcessor> implements ArrayWidgetProcessor{

	private int maxIdx=-1;
	private HashMap<String,T> boundRowMap = new HashMap<String, T>();
	
	
	protected abstract T createRowWidgetProcessor(int idx);
	
	
	@Override
	public Widget bind(Widget widget, int idx, GenericObjectProperty property,
			GenericWidget gw) {
		if (idx>maxIdx)
			maxIdx=idx;
		String hash = Integer.toString(idx);
		T proc = boundRowMap.get(hash);
		if (proc == null) {
			proc = createRowWidgetProcessor(idx);
			boundRowMap.put(hash,proc);
		}
		proc.bind(widget, idx, property, gw);
		
		return widget;
	}
	
	
	@Override
	public boolean unBind(int idx, GenericObjectProperty property) {
		return false;
	}

	public int getNumRows() {
		return maxIdx+1;
	}
	
	public T getRowWidgetProcessor(int rowIdx) {
		String hash = Integer.toString(rowIdx);
		T wp = boundRowMap.get(hash);
		return wp;
	}
}
