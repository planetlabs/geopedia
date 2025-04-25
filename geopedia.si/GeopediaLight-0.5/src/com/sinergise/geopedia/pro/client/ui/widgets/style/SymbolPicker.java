package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.geopedia.core.constants.Icons;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.gwt.ui.editor.IntegerEditor;

public class SymbolPicker extends AbstractLinearSelector<Integer> {

	private IntegerEditor symInput = new IntegerEditor(false);
	
	public SymbolPicker() {
		super(new Integer[]{500,521, 582,531,534,535,536,537,542,561,563,577,580,585,590,591,592,613,617,618,619,630,631,632,633,634,635,636,637,638,639,640, 651});
		
		addStyleName("symbolPicker");
		symInput.setVisibleLength(7);
		symInput.setMaxLength(7);				
		symInput.setTitle(ProConstants.INSTANCE.enterSymbolID());
		add(symInput);
		setValue(items[0]);
	}

	// TODO: fix symbol path!
	@Override
	protected void renderItemAnchor(Anchor anchor, Integer item) {
		Image img = new Image("sicon/sym/"+item+"?c1=0xFF5070FF&c2=0&ss="+Icons.Sizes.SYM_SMALL);
		anchor.getElement().appendChild(img.getElement());
		anchor.setStyleName("symbol");
		
	}
	
	@Override
	protected void onAfterItemSelected(Integer item) {
		symInput.setEditorValue(item);
	}
	
	
	@Override
	public Integer getValue() {
		Integer val =  symInput.getEditorValue();
		if (val != null)
			return val;
		if (selectedItem!=null)
			return selectedItem;
		return items[0];
			
	}
}
