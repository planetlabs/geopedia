package com.sinergise.geopedia.light.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.symbology.GWTSymbologyUtils;
import com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.ui.feature.FeatureInfoContentPanel;
import com.sinergise.geopedia.client.ui.widgets.CodeMirrorEditor;
import com.sinergise.geopedia.client.ui.widgets.CodeMirrorJSEditor;
import com.sinergise.geopedia.core.crs.CRSSettingsSVN;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.light.client.ui.feature.LiteFeatureInfoContentPanel;
import com.sinergise.geopedia.light.theme.GeopediaLightStyle;
import com.sinergise.geopedia.pro.client.ui.widgets.style.SimpleJSStyleEditor;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.IE6CompatibilityPopup;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GeopediaLight extends GeopediaLiteBase implements EntryPoint {

	private static final Logger logger = LoggerFactory.getLogger(GeopediaLight.class);
	
	public void onModuleLoad3() {
		CodeMirrorEditor.setCodeMirrorJSURL(GWT.getHostPageBaseURL()+"externalJS/codemirror/lib/codemirror.js");
		CodeMirrorJSEditor.setJavascriptModeURL(GWT.getHostPageBaseURL()+"externalJS/codemirror/mode/javascript/javascript.js");
		final CodeMirrorJSEditor cme = new CodeMirrorJSEditor();
		
		final FlowPanel holder = new FlowPanel();
		holder.add(cme);
		cme.setSize("97%", "200px");
		RootPanel.get().add(holder);
		
		Button btnRemove = new Button("remove");
		btnRemove.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				holder.clear();
			}
		});
		RootPanel.get().add(btnRemove);
		
		Button btnSetText = new Button("setText");
		btnSetText.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				cme.setText("some text");
			}
		});
		RootPanel.get().add(btnSetText);
		Button btnGetText = new Button("getText");
		btnGetText.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				System.out.println(cme.getText());
			}
		});
		RootPanel.get().add(btnGetText);


		
	}
    public void onModuleLoa1d() {
    	 IE6CompatibilityPopup.showIfIE6("GeopediaLight");
 		GeopediaCommonStyle.INSTANCE.geopediaCommonStyles().ensureInjected();
 		GeopediaLightStyle.INSTANCE.geopediaLightStyles().ensureInjected();
 		GeopediaProStyle.INSTANCE.geopediaProStyles().ensureInjected();
 		GeopediaProStyle.INSTANCE.geopediaSmallStyles().ensureInjected();
 		//FIXME this doesnt make any sense. first declare with some value and then overwrite it! And actually never using the value overwritten.
    	//FIXME the name of the variable js doesnt really tell me what it does
 		//commented out because it made no sense. Waiting for fix
//    	String js = 
//				"var symbology = sf.simpleSymbology([" +
//				"sf.LineSymbolizer({lineType:'Dots', opacity:6, stroke:0xFF00FF00}), " +
//				"sf.FillSymbolizer({fillType:'VER_LINES'})]);";
//    	
//    	js ="return sf.Symbology([sf.PaintingPass(" +
//				"[sf.LineSymbolizer({opacity: 1.0,displacementX: 0.0,displacementY: 0.0,lineType: 'SOLID',stroke: 0xff000000,strokeWidth: 1.0})" +
//				", (sf.TextSymbolizer({font: sf.SymbolizerFont({fontWeight:'BOLD'})})" +
//				")" +
//				"])]);";
		
    	GWTSymbologyUtils symWriter = new GWTSymbologyUtils();
    	String js = SymbologyEvaluatorGWT.toJavascript(symWriter.createDefaultPolygonSimbology());
    	
        //System.out.println("is simple "+ SymbologyEvaluatorGWT.hasExternalIdentifiers(js));
        try {
	        Symbology style = (Symbology) SymbologyEvaluatorGWT.evaluateSimpleSymbology(js);
	        
	        final SimpleJSStyleEditor ed = new SimpleJSStyleEditor();
	        ed.setSymbology(style);
	        ed.addStyleName("style");
	        RootPanel.get().add(ed);
	        Button btn = new Button("go");
	        RootPanel.get().add(btn);
	        btn.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					if(logger.isDebugEnabled()){
						Symbology symb = ed.getSymbology();
						logger.debug(" ______->"+SymbologyEvaluatorGWT.toJavascript(symb));
						logger.debug("ff");
					}
				}
				
			});

        } catch (Throwable th) {
        	th.printStackTrace();
        }
    }

	

	@Override
    public void onModuleLoad() {
        IE6CompatibilityPopup.showIfIE6("GeopediaLight");
		GeopediaCommonStyle.INSTANCE.geopediaCommonStyles().ensureInjected();
		GeopediaLightStyle.INSTANCE.geopediaLightStyles().ensureInjected();
		GeopediaProStyle.INSTANCE.geopediaProStyles().ensureInjected();
		GeopediaProStyle.INSTANCE.geopediaSmallStyles().ensureInjected();

        FeatureInfoContentPanel.CREATORINSTANCE = new LiteFeatureInfoContentPanel.Creator();
        ClientGlobals.maxSearchResults=15;
        ClientGlobals.baseURL = GWT.getHostPageBaseURL()+"lite.jsp";
        ClientGlobals.defaultSearchExecutor = searchExecutor;
        ClientGlobals.crsSettings = new CRSSettingsSVN();
        loadConfigAndInitialize();
    }
}
