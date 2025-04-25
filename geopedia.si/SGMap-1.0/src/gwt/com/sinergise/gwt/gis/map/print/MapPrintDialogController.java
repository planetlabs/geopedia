package com.sinergise.gwt.gis.map.print;

import static com.sinergise.common.gis.map.print.TemplateParam.PRINT_ATTRIBUTES;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.Window;
import com.sinergise.common.gis.map.print.PrintParams;
import com.sinergise.common.gis.map.print.PrintScaleValue;
import com.sinergise.common.gis.ogc.OGCRequest;
import com.sinergise.common.gis.ogc.wms.request.WMSMapRequest;
import com.sinergise.common.gis.ogc.wms.request.ext.WMSSelectionInfo;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.gwt.gis.ogc.wms.WMSUtilGWT;

public class MapPrintDialogController {
	
	private static final Logger logger = LoggerFactory.getLogger(MapPrintDialogController.class);
	
	protected PrintParams currentParams = new PrintParams();
	protected MapPrintDialog printDialog = null;
	protected MapPrintContext ctx;
	
	public MapPrintDialogController(MapPrintContext ctx) {
		this.ctx = ctx;
		currentParams = constructPrintParams();
	}
	
	public void cancelClicked() {
		// TODO Auto-generated method stub
		
	}
	
	public PrintParams constructPrintParams() {
		PrintParams ret = new PrintParams(currentParams);
		ret.envelope = ctx.getMap().getCoordinateAdapter().worldRect;
		ret.crsID = ctx.getMap().getCoordinateAdapter().worldCRS.getCode();
		//TODO: Increase DPI when text resizing is fixed
		ret.DPI = 127;
		return ret;
	}
	
	public MapPrintDialog getPrintDialog() {
		if (printDialog == null) {
			printDialog = createPrintDialog();
		}
		return printDialog;
	}
	
	protected MapPrintDialog createPrintDialog() {
		return new MapPrintDialog(this);
	}
	
	public void doPrint(final PrintParams prms) {
		WMSMapRequest req = WMSUtilGWT.createWMSExportRequest(ctx.getMap(), ctx.getWMSSource(), false);
		prms.updateRequest(req, new SGAsyncCallback<OGCRequest>() {
			@Override
			public void onSuccess(OGCRequest result) {
				Window.open(result.createRequestURL(prms.getUrlPath(ctx.getWMSSource().getBaseUrl())), "_blank", "");
			}
			
			@Override
			public void onFailure(Throwable caught) {
				logger.error("Error while preparing map print request: "+caught.getMessage());
			}
		});
	}
	
	public void okClicked(PrintParams param) {
		if (param == null) {
			return;
		}
		doPrint(param);
	}
	
	public void showPrintDialog() {
		if (currentParams.highlight.isEmpty()) {
			currentParams.highlight = WMSUtilGWT.createCurrentHighlight(ctx.getMap(), ctx.getWMSSource());
			currentParams.setCustom(PRINT_ATTRIBUTES, Boolean.FALSE.toString());
		}
		if (printDialog == null) {
			printDialog = createPrintDialog();
		}
		printDialog.setTemplates(ctx.getTemplates());
		beforeShow();
		printDialog.showAndPosition();
	}

	public void setHighlightedFeatures(WMSSelectionInfo highlight) {
		currentParams.highlight = highlight;
	}

	protected void beforeShow() {
	}

	public void setScale(PrintScaleValue scaleMode) {
		currentParams.scaleSpec = scaleMode;
	}

	public PrintParams getParams() {
		return currentParams;
	}
}
