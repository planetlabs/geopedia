package com.sinergise.gwt.gis.map.print;

import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.resources.GisTheme;

public class PrintMapAction extends Action {
	protected final MapPrintContext context;
	
	public PrintMapAction(MapPrintContext context) {
		super(Tooltips.INSTANCE.toolbar_print());
		setIcon(GisTheme.getGisTheme().gisStandardIcons().print());
		this.context = context;
		setStyle("printFeatures");
	}
	@Override
	protected void actionPerformed() {
		MapPrintDialogController ctrl = context.createDialogController();
		prepareDialog(ctrl);
		ctrl.showPrintDialog();
	}
	/**
	 * @param ctrl 
	 */
	protected void prepareDialog(MapPrintDialogController ctrl) {
	}
}
