package com.sinergise.gwt.gis.map.ui.controls.coords;


import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsDescriptor;

public class CoordSystemsCombo extends ListBox {

	CrsDescriptor curCs=null;
	CrsDescriptor[] items=null;
	
	public CoordSystemsCombo(CRS ...systems) {
		this();
		setSystems(systems);
	}
	
	public CoordSystemsCombo() {
		addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int idx=getSelectedIndex();
				if (idx>=0) curCs=items[idx];
				else curCs=null;
			}
		});
	}
	
	public void setSystems(CrsDescriptor[] systems) {
		this.items=systems;
		clear();
		for (int i = 0; i < systems.length; i++) {
			addItem(systems[i].name, systems[i].system.getCode());
		}
		setSelectedIndex(0);
	}
	
	public void setSystems(CRS... crss) {
		CrsDescriptor[] descs = new CrsDescriptor[crss.length];
		for(int i=0; i<crss.length; i++) {
			descs[i] = new CrsDescriptor(crss[i].getNiceName(true), crss[i]);
		}
		setSystems(descs);
	}
	
	@Override
	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		if (index>=0) curCs=items[index];
		else curCs=null;
	}
	
	public CrsDescriptor getSelectedCrs() {
		return curCs;
	}
	
	public CRS getSelectedCRS() {
		return curCs != null ? curCs.system : null;
	}
	
	public void setSelectedCrs(CrsDescriptor desc) {
		if (desc==null) {
			setSelectedIndex(-1);
		} else {
			setSelectedIndex(indexOf(desc));
		}	
	}
	
	public void setSelectedCrs(CRS crs) {
		if (crs==null) {
			setSelectedIndex(-1);
		} else {
			setSelectedIndex(indexOf(crs));
		}	
	}
	
	public int indexOf(CrsDescriptor crs) {
		return indexOf(crs.system);
	}

	public int indexOf(CRS crs) {
		for (int i = 0; i < items.length; i++) {
			if (crs.equals(items[i].system)) return i;
		}
		return -1;
	}

	public CrsDescriptor descriptorFor(CRS crs) {
		if (crs==null) return null;
		int idx=indexOf(crs);
		if (idx>=0) return items[idx];
		return null;
	}
	
}
