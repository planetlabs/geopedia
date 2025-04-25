package com.sinergise.generics.builder;

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.impl.XmlUtils;

public class CompositeInspector implements Inspector{

	private ArrayList<Inspector> inspectors = new ArrayList<Inspector>();
	
	public void addInspector(Inspector inspector) {
		inspectors.add(inspector);
	}
	@Override
	public Element inspect(Object toInspect) throws InspectorException {
		if (inspectors == null || inspectors.size()==0)
			throw new InspectorException("No inspectors to combine!");
		
		
		Element baseElement = inspectors.get(0).inspect(toInspect);
		if (inspectors.size()>1) {
			for (int i=1;i<inspectors.size();i++) {
				Element toAdd = inspectors.get(i).inspect(toInspect);
				if (baseElement !=null && toAdd!=null) {
					XmlUtils.combineElements(baseElement, toAdd, 
							MetaAttributes.NAME, MetaAttributes.NAME);
				} else if (toAdd!=null) {
					baseElement = toAdd;
				}
			}
		}
		
		return baseElement;
	}

}
