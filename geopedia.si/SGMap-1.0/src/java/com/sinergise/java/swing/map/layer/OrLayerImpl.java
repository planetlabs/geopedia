/*
 * Created on Mar 25, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sinergise.java.swing.map.layer;

import java.util.ArrayList;
import java.util.List;

import com.sinergise.java.swing.map.OrDisplay;



/**
 * @author Miha Kadunc (<a href="mailto:miha.kadunc@cosylab.com">miha.kadunc@cosylab.com</a>
 */
public abstract class OrLayerImpl implements OrLayer {
    public String name;
    public LayerProperties properties;
    private transient List<OrDisplay> disps;
	/**
	 * 
	 */
	public OrLayerImpl() {
		super();
	}
    
    public OrLayerImpl(String name) {
        setName(name);
    }

	/* (non-Javadoc)
	 * @see com.cosylab.ortelius.space.display.OrLayer#addedToDisplay(com.cosylab.ortelius.space.display.OrDisplay)
	 */
	public void addedToDisplay(OrDisplay display) {
        if (disps==null) disps=new ArrayList<OrDisplay>(1);
        disps.add(display);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.ortelius.space.display.OrLayer#removedFromDisplay(com.cosylab.ortelius.space.display.OrDisplay)
	 */
	public void removedFromDisplay(OrDisplay display) {
        disps.remove(display);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
    public LayerProperties getProperties() {
        if (properties==null) properties=new LayerProperties();
        return properties;
    }
    
    protected void fireLayerChanged() {
        if (disps==null) return;
        for (OrDisplay d : disps) {
            d.layerChanged(this);
        }
    }
    public void initializeData() {
        
    }
}
