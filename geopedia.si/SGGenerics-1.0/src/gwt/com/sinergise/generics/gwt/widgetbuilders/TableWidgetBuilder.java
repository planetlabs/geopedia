package com.sinergise.generics.gwt.widgetbuilders;

import java.util.Map;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.gwt.ui.ImageAnchor;

public class TableWidgetBuilder implements WidgetBuilder{

	public static final String ACTION_LABEL_STYLE = "sgwebui-table-action"; 
	@Override
	public Widget buildWidget(String attributeName, Map<String, String> metaAttributes) {
		
		String elementType = metaAttributes.get("ElementType");
		if (elementType!=null && elementType.equalsIgnoreCase(XMLTags.Action)) {
			String actionType = metaAttributes.get(MetaAttributes.TYPE);
			if (actionType ==null || actionType.equalsIgnoreCase("button")) {
				
				boolean asLabel = MetaAttributes.isTrue(metaAttributes,MetaAttributes.RENDER_AS_LABEL);
				
				if(asLabel){
					String name = metaAttributes.get(MetaAttributes.NAME);
					Label bt = new Label(name);
					bt.setStyleName(ACTION_LABEL_STYLE);
					bt.addStyleName(name);
					return bt;
				}

				Button bt = new Button(metaAttributes.get(MetaAttributes.NAME));
				return bt;
				
			} else if (actionType.equalsIgnoreCase("RadioButton")) {
				RadioButton bt = new RadioButton(attributeName);
				// TODO: set label?
				return bt;
			} else if (actionType.equalsIgnoreCase("Anchor")) {
				Anchor a =  new Anchor();
				String name = metaAttributes.get(MetaAttributes.NAME);
				if (name !=null && name.length()>0)
					a.setText(name);
				return a;
			} else if (actionType.equalsIgnoreCase("ImageAnchor")) {
				ImageAnchor a =  new ImageAnchor();
				a.addStyleName("centered");
				String name = metaAttributes.get(MetaAttributes.LABEL);
				if (name !=null && name.length()>0)
					a.setTitle(name);
				return a;
			}
		} else if (MetaAttributes.isTrue(metaAttributes, MetaAttributes.RENDER_AS_ANCHOR)) {
			return new Anchor();
		}
		
		if (MetaAttributes.isType(metaAttributes, Types.BOOLEAN)) {
			CheckBox cb = new CheckBox();
			cb.setEnabled(false);
			return cb;
		} 
		
		if (MetaAttributes.isType(metaAttributes, Types.STUB)) {
			return new SimplePanel();
		}
		
		return new Label();
	}

}
