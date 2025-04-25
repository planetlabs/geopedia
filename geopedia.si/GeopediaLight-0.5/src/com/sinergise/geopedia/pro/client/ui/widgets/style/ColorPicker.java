package com.sinergise.geopedia.pro.client.ui.widgets.style;

import java.awt.Color;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.util.html.CSS;

public class ColorPicker extends AbstractLinearSelector<Color> {

	private SGTextBox tbColorText = new SGTextBox();
	
	public ColorPicker() {
		this(new Color[]{ 
				new Color(0xe80c0c),
				new Color(0xe8670c),
				new Color(0xe8c30c),
				new Color(0xc6e80c),
				new Color(0x3ad620),
				new Color(0x20d6a9),
				new Color(0x208fd6),
				new Color(0x9420d6),
				new Color(0xd62094),
				new Color(0x888888)
		});
	}
	
	public ColorPicker(Color[] color) {
		super(color);
		addStyleName("colorPicker");
		tbColorText.setVisibleLength(7);
		tbColorText.setMaxLength(7);
		add(tbColorText);
		setValue(items[0]);
	}
	
	
	
	
	@Override
	protected void renderItemAnchor(Anchor anchor, Color item) {
		final String htmlColor = ColorUtil.toHTMLColor(item.getRGB());
		Image img = new Image();
		anchor.getElement().appendChild(img.getElement());
		anchor.setStyleName("color");
		CSS.backgroundColor(img.getElement(), htmlColor);
	}
	
	
	@Override
	protected void onAfterItemSelected(Color item) {
		String htmlColor = ColorUtil.toHTMLColor(item.getRGB());
		tbColorText.setValue(htmlColor);
	}
	
	
	@Override
	protected boolean equals(Color item1, Color item2) {
		if (item1==null && item2==null) return true;
		if (item1==null || item2==null) return false;
		return (item1.getRGB()&0xFFFFFF) ==(item2.getRGB()&0xFFFFFF); 
	}
	
	@Override
	public Color getValue() {
		String hexColor = tbColorText.getValue();
		if (StringUtil.isNullOrEmpty(hexColor) || hexColor.length()!=7 || !hexColor.startsWith("#"))
			return null;
		hexColor=hexColor.substring(1);
		try {
			int rgb = Integer.parseInt(hexColor,16);
			return new Color(rgb);
		} catch (Throwable th) {
			
		}
		return items[0];
	}
	
}
