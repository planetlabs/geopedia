package com.sinergise.geopedia.client.ui.widgets;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.sinergise.gwt.util.ExternalJSLoader;

public class CodeMirrorEditor extends Composite {
    protected static ExternalJSLoader extJSLoader;
    protected JavaScriptObject codeMirrorObj = null;
    
    public static void setCodeMirrorJSURL(String url) {
    	extJSLoader = new ExternalJSLoader.Single(url);
    }
    
    
    private TextArea textArea;
    private String editorId;
    private boolean isLoaded = false;
    public CodeMirrorEditor() {
        textArea = new TextArea();
        initWidget(textArea);
        editorId = "codeMirror-" + System.currentTimeMillis();
        DOM.setElementAttribute(getElement(), "id", editorId);
    }
    
    public void setText(String text) {
    	if (codeMirrorObj==null)
    		textArea.setText(text);
    	else 
    		setEditorValue(codeMirrorObj, text);
    }
    
    public String getText() {
    	if (codeMirrorObj==null)
    		return textArea.getText();
    	else 
    		return getEditorValue(codeMirrorObj);
    }
    @Override
    protected void onAttach() {
        super.onAttach();     
        loadCodeMirror();
    }
    
    @Override
    protected void onDetach() {
    	if (codeMirrorObj!=null)
    		destroyCodeMirrorNative(codeMirrorObj);
    	super.onDetach();
    }
    
    
    
    protected void loadCodeMirror() {
    	 extJSLoader.ensureLoaded(new Callback<Void, Exception>() {

 			@Override
 			public void onFailure(Exception reason) {
 				// ignore, there's ordinary TA anyway
 			}

 			@Override
 			public void onSuccess(Void result) {
 				initializeCodeMirror();
 			}
 		});
    }

    protected void initializeCodeMirror() {
    	codeMirrorObj = initializeCodemirrorNative(editorId);
    }
    
    private native void setEditorValue(JavaScriptObject editor, String text) /*-{
		editor.setValue(text);
	}-*/;

    private native String getEditorValue(JavaScriptObject editor) /*-{
		return editor.getValue();
	}-*/;

    
    private native void destroyCodeMirrorNative(JavaScriptObject editor) /*-{
		editor.toTextArea();
	}-*/;

    
    private native JavaScriptObject initializeCodemirrorNative(String editorID)/*-{
      	var editor = $wnd.CodeMirror.fromTextArea($wnd.document.getElementById(editorID), {
      		lineNumbers: true,
      		gutters: ["CodeMirror-linenumbers"]
    	});
    	return editor;
    }-*/;

    
    private native void refresh(JavaScriptObject editor)/*-{
    	editor.refresh();
    }-*/;
    public void refresh() {
    	if (codeMirrorObj!=null)
    		refresh(codeMirrorObj);
    }


}
