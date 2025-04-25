package com.sinergise.geopedia.pro.client.ui.widgets.style;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.symbology.SymbologyEvaluatorGWT;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.geopedia.pro.theme.layeredit.LayerEditStyle;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class JSStyleEditorWidget  extends SGHeaderPanel {
	private static final Logger logger = LoggerFactory.getLogger(JSStyleEditorWidget.class);
	AdvancedJSStyleEditor advEditor;
	SimpleJSStyleEditor ssEditor = null;
	protected FlowPanel buttonHolder;
	
	public String getValue() throws GeopediaException{
		if (advEditor != null) {			
			return advEditor.getValue();
		} else {
			Symbology symb = ssEditor.getSymbology();
			if (symb!=null) {							
				return SymbologyEvaluatorGWT.toJavascript(symb);
			}
		}
		return null;
	}
	
	public void setValue (GeomType geomType, String styleJS) {
		LayerEditStyle.INSTANCE.layerStyleEdit().ensureInjected();
		ssEditor = null;
		advEditor = null;
		clear();
		buttonHolder = new FlowPanel();
		
		GetSimpleEditorClickHandler simpleEditorClickHandler = new GetSimpleEditorClickHandler();
		GetAdvancedEditorClickHandler advancedEditorClickHandler = new GetAdvancedEditorClickHandler();
		
		SGPushButton btnSimpleEditor = new SGPushButton(ProConstants.INSTANCE.simpleEditor(), Theme.getTheme().standardIcons().action(), simpleEditorClickHandler);
		SGPushButton btnAdvancedEditor = new SGPushButton(ProConstants.INSTANCE.advanceEditor(), Theme.getTheme().standardIcons().action(), advancedEditorClickHandler);
		
		simpleEditorClickHandler.setBtnAdvancedEditor(btnAdvancedEditor);
		advancedEditorClickHandler.setBtnSimpleEditor(btnSimpleEditor);
		
		try {
			if (!StringUtil.isNullOrEmpty(styleJS) && !SymbologyEvaluatorGWT.hasExternalIdentifiers(styleJS)) {			
			        Symbology style = (Symbology) SymbologyEvaluatorGWT.evaluateSimpleSymbology(styleJS);
			        if (style!=null) {
				        setSimpleEditorView(style, btnAdvancedEditor);
			        }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

		if (ssEditor == null) {
			//if we didnt succeed in adding the simple editor we fallback to the advanced
			setAdvancedEditorView(styleJS, btnSimpleEditor);
		} 		
		
	}
	
	private void setAdvancedEditorView(String styleJS, SGPushButton btnSimpleEditor){
		clear();
		advEditor = new AdvancedJSStyleEditor();
		addStyleName("advancedEditor");
		advEditor.setValue(styleJS);
		
		setContentWidget(advEditor); //add advanced editor
		ssEditor = null;
		
		setButtonOnButtonHolder(btnSimpleEditor);
	}
	
	private void setSimpleEditorView(Symbology style, SGPushButton btnAdvancedEditor){
		clear();
		ssEditor = new SimpleJSStyleEditor();
		ssEditor.setSymbology(style);
		
		setContentWidget(ssEditor); //add advanced editor
		advEditor = null;
		
		setButtonOnButtonHolder(btnAdvancedEditor);
	}
	
	private void setButtonOnButtonHolder(SGPushButton button){
		buttonHolder.clear();
		buttonHolder.setStyleName("layerToolbar");
		buttonHolder.add(new Image(GeopediaProStyle.INSTANCE.shadowPro()));
		buttonHolder.add(button);
		setFooterWidget(buttonHolder); //set button holder panel
	}
	
	
	private class GetSimpleEditorClickHandler implements ClickHandler {
		
		SGPushButton btnAdvancedEditor;
		
		@Override
		public void onClick(ClickEvent event) {
			try {
				Symbology style = (Symbology) SymbologyEvaluatorGWT.evaluateSimpleSymbology(advEditor.getValue());
				setSimpleEditorView(style, btnAdvancedEditor);
			} catch (Throwable th) {
				logger.error("Style fail: ",th);
			}
			
			setButtonOnButtonHolder(btnAdvancedEditor);
		}
		
		public void setBtnAdvancedEditor(SGPushButton btnAdvancedEditor){
			this.btnAdvancedEditor = btnAdvancedEditor;
		}
	}
	
	private class GetAdvancedEditorClickHandler implements ClickHandler {
		
		SGPushButton btnSimpleEditor;
		
		@Override
		public void onClick(ClickEvent event) {
			try {
				String styleJS = null;
				
				Symbology symb = ssEditor.getSymbology();
				if (symb != null) {							
					styleJS = SymbologyEvaluatorGWT.toJavascript(symb);
				}
				
				setAdvancedEditorView(styleJS, btnSimpleEditor);
				
			} catch (Throwable th) {
				logger.error("Style fail: ",th);
			}
			
		}
		
		public void setBtnSimpleEditor(SGPushButton btnSimpleEditor){
			this.btnSimpleEditor = btnSimpleEditor;
		}
	}
		
}