package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.geopedia.client.core.symbology.EsprimaValidationResult;
import com.sinergise.geopedia.client.ui.widgets.CodeMirrorJSEditor;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.exceptions.TableDataException;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.i18n.ProMessages;
import com.sinergise.geopedia.pro.theme.styler.GeopediaStylerBundle;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGImageLabel;
import com.sinergise.gwt.ui.resources.Theme;

public class AdvancedJSStyleEditor extends SGHeaderPanel {
	CodeMirrorJSEditor editor;
	FlowPanel errorHolder = new FlowPanel();
	private FlowPanel headerHolder;
	private FlowPanel btnHolder;
	private GeopediaStylerBundle STYLE = GWT.create(GeopediaStylerBundle.class);

	public AdvancedJSStyleEditor() {
		this("100%");
		addStyleName("fullHeightEditor");
	}
	public AdvancedJSStyleEditor(String editorHeight) {
		STYLE.styler().ensureInjected();
		errorHolder.setVisible(false);
		errorHolder.setStyleName("codeErrorHolder");
		editor =  new CodeMirrorJSEditor();
		headerHolder = new FlowPanel();
		
		btnHolder = new FlowPanel();
		btnHolder.setStyleName("codeBtnPanel");
		headerHolder.add(btnHolder);
		
		setHeaderWidget(headerHolder);
		setContentWidget(editor);
		
		ImageAnchor helpLink = new ImageAnchor(StandardUIConstants.STANDARD_CONSTANTS.buttonHelp(),Theme.getTheme().standardIcons().help(),"http://portal.geopedia.si/navodila/karta_sloji/stil_slojev#advanced", "_blank");
		helpLink.addStyleName("fl-right");
		btnHolder.add(helpLink);
		
		ImageAnchor btnValidate = new ImageAnchor(ProConstants.INSTANCE.codeEditorValidate(), Theme.getTheme().standardIcons().ok(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				validate();
				Timer timer = new Timer() {
			      public void run() {
			    	  onResize();
			      }
			    };
			    timer.schedule(500);
			}
		});
		btnHolder.add(btnValidate);
		
		headerHolder.add(errorHolder);
		setHeight(editorHeight);
	}
	
	@Override
	public void onResize() {
		super.onResize();
		if (editor!=null)
			editor.refresh();
	}
	
	public void setValue(String javascript) {
		editor.setText(javascript);
		validate();
		clearErrorPanel();
	}
	
	public String getValue() throws GeopediaException {
		if (!validate()) throw TableDataException.create(TableDataException.Type.STYLE_ERROR);
		return editor.getText();
	}

	private native EsprimaValidationResult validateJavascript(String code) /*-{
		var styleFunction = "function style() { " + code + "}";
		var errors = new Array();
		try {
			var result = $wnd.esprima.parse(styleFunction, {
				tolerant : true,
				loc : true
			}).errors;
			if (result.length > 0) {
				var i;
				for (i = 0; i < result.length; i += 1) {
					errors.push(result[i].message);
				}
			}
		} catch (e) {
			errors.push(e.name + ': ' + e.message);
		}
		return errors;
	}-*/;

	public boolean validate() {
		String text = "";
		errorHolder.clear();
		EsprimaValidationResult results = validateJavascript(editor.getText());
		int msgCount = results.getMessageCount();
		
		if (msgCount==0) {
			text+=ProMessages.INSTANCE.codeEditorAllOk();
			errorHolder.add(new SGImageLabel(text, new Image(Theme.getTheme().standardIcons().ok())));
			errorHolder.setVisible(true);
			Timer timer = new Timer() {
				@Override
				public void run() {
					errorHolder.setVisible(false);
					onResize();
				}
			};
			timer.schedule(2000);
			onResize();
			return true;
		}
		else {
			for (int i=0;i<msgCount;i++) {
				text+=results.getMessage(i);
			}
			errorHolder.add(new ImageAnchor(Theme.getTheme().standardIcons().close(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					clearErrorPanel();
				}
			}));
			errorHolder.add(new SGImageLabel(text, new Image(Theme.getTheme().standardIcons().error())));
			errorHolder.setVisible(true);
		}
		onResize();
		return false;
	}
	
	private void clearErrorPanel() {
		errorHolder.clear();
		errorHolder.setVisible(false);
		onResize();
	}

}
