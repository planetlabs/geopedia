/*
 *
 */
package com.sinergise.java.swing.map;

import java.util.ArrayList;
import java.util.HashMap;

import com.sinergise.java.swing.map.layer.OrLayer;


public class LayersList extends ArrayList<OrLayer> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private transient HashMap<String, OrLayer> nameMap=new HashMap<String, OrLayer>();
    
    @Override
	public boolean add(OrLayer layer) {
        checkMap();
        if (super.add(layer)) {
            nameMap.put(layer.getName(),layer);
            return true;
        }
        return false;
    }
    @Override
	public boolean remove(Object layer) {
        checkMap();
        if (super.remove(layer)) {
            nameMap.remove(((OrLayer)layer).getName());
            return true;
        }
        return false;
    }
    public OrLayer get(String name) {
        checkMap();
        return nameMap.get(name);
    }
    private void checkMap() {
        if (nameMap==null || nameMap.size()!=size()) {
            nameMap=new HashMap<String, OrLayer>(size());
            for (OrLayer l : this) {
                nameMap.put(l.getName(), l);
            }
        }
    }
}
