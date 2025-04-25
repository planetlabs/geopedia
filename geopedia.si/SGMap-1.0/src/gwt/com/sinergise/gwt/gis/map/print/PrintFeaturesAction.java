package com.sinergise.gwt.gis.map.print;

import java.util.Collections;
import java.util.List;

import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.feature.RepresentsFeatureCollection;
import com.sinergise.common.gis.map.print.PrintScaleValue;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider;

public class PrintFeaturesAction extends PrintMapAction {
	public static class PrintFeaturesActionProvider implements FeatureActionsProvider {
		protected MapPrintContext ctx;
		public PrintFeaturesActionProvider(MapPrintContext ctx) {
			this.ctx = ctx;
		}
		
		@Override
		public List<PrintFeaturesAction> getFeatureActions(HasFeatureRepresentations fRep, Object requestor) {
			return Collections.singletonList(new PrintFeaturesAction(ctx, fRep));
		}
	}

	protected WMSSelectionInfo selectionInfo;
	protected HasFeatureRepresentations fRep;
	
	public PrintFeaturesAction(MapPrintContext ctx) {
		super(ctx);
	}

	public PrintFeaturesAction(MapPrintContext ctx, RepresentsFeature f) {
		this(ctx, new RepresentsFeatureCollection(f));
	}

	public PrintFeaturesAction(MapPrintContext ctx, HasFeatureRepresentations fRep) {
		this(ctx, createSelectionInfo(fRep));
		this.fRep = fRep;
	}

	public PrintFeaturesAction(MapPrintContext ctx, WMSSelectionInfo selection) {
		this(ctx);
		this.selectionInfo = selection;
	}

	@Override
	protected void prepareDialog(MapPrintDialogController ctrl) {
		super.prepareDialog(ctrl);
		if (fRep != null) {
			selectionInfo = createSelectionInfo(fRep);
		}
		ctrl.setHighlightedFeatures(selectionInfo);
		ctrl.setScale(PrintScaleValue.AUTO_FROM_FEATURES);
	}

	protected static WMSSelectionInfo createSelectionInfo(HasFeatureRepresentations featureRep) {
		return WMSSelectionInfo.createEnumerated(featureRep.getFeatures(), System.currentTimeMillis()+"");
	}
}
