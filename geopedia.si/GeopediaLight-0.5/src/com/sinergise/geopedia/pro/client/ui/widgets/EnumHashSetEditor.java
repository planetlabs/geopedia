package com.sinergise.geopedia.pro.client.ui.widgets;

import java.util.HashMap;
import java.util.HashSet;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;

public class EnumHashSetEditor<E extends Enum<E>> extends FlowPanel {

	HashMap<Enum<E>,CheckBox> cbMap = new HashMap<Enum<E>,CheckBox>();
	
	public EnumHashSetEditor(HashSet<E> fullEnumSet) {
		for (Enum<E> en:fullEnumSet) {
			CheckBox cb = new CheckBox(getI18NLabelFor(en));
			cbMap.put(en,cb);
			add(cb);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public HashSet<E> getValue() {
		HashSet<E> set = new HashSet<E>();
		for (Enum<E> en:cbMap.keySet()) {
			CheckBox cb = cbMap.get(en);
			if (cb.getValue()) {
				set.add((E) en);
			}
		}
		return set;
	}
	
	public void setValue(HashSet<E> set) {
		for (Enum<E> en:cbMap.keySet()) {
			CheckBox cb = cbMap.get(en);
			cb.setValue(set.contains(en));
		}
	}
	
	public String getI18NLabelFor(Enum<E> enm) {
		return enm.name();
	}
}
