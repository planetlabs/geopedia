package com.sinergise.geopedia.pro.client.ui;

import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasLoadHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TextArea;

public class TinyMCEEditor extends TextArea implements HasLoadHandlers{

	
	public static class Settings {
		public String width = "100%";
		public String height = "200px";
	}
	private String editorId;	
	private boolean isLoaded = false;
	private JavaScriptObject editorInstance;
	private ArrayList<LoadHandler> loadListeners = new ArrayList<LoadHandler>();
	private HandlerManager handlerManager;
	
	private Settings settings;
	public TinyMCEEditor() {
		this(new Settings());
	}
	public TinyMCEEditor(final Settings settings) {
		this.settings = settings;
		editorId = "tinyMCE-" + System.currentTimeMillis();
		handlerManager = new HandlerManager(this);
	    DOM.setElementAttribute(getElement(), "id", editorId);
	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				initEditor(settings);
				
			}
		});
	  
	}
	//forced_root_block : false,
	 private native void initEditor(Settings settings)/*-{
     var editor = new $wnd.tinymce.Editor(this.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::editorId, {
         theme : "advanced",
         mode: "textareas",
         skin : "o2k7",         
         cleanup: false,
         width : settings.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor.Settings::width,
         height : settings.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor.Settings::height,
         plugins : "media,safari,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,inlinepopups",
         media_strict: false,  
     
         theme_advanced_buttons1 : "bold,italic,underline,|,bullist,numlist,|,paste,pastetext,pasteword,|,fullscreen,|,code",
         theme_advanced_buttons2 : "",
         theme_advanced_buttons3 : "",
         theme_advanced_toolbar_location : "top",
         theme_advanced_toolbar_align : "left",
         theme_advanced_statusbar_location : "none",
         theme_advanced_resizing : true,
     });
   
     this.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::editorInstance = editor;
   
     var self = this;
     editor.onInit.add(function(ed) {
         self.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::isLoaded = true;
         self.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::fireLoadEvent()();
     });
     editor.onKeyUp.add(function(ed, l) {
         self.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::fireChangeEvent()();
     });
   
     editor.render();
 }-*/;
	 
	 public native void focus()/*-{
     this.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::editorInstance.focus(false);
 }-*/;
	 
	 private native void _setHTML(String html)/*-{
     this.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::editorInstance.setContent(html);
 	 }-*/;

	 public native String getText()/*-{
	     return this.@com.sinergise.geopedia.pro.client.ui.TinyMCEEditor::editorInstance.getContent();
	 }-*/;
	 private void fireLoadEvent() {
		DomEvent.fireNativeEvent(Document.get().createLoadEvent(), handlerManager);
		 
	 }

	private void fireChangeEvent() {
		DomEvent.fireNativeEvent(Document.get().createChangeEvent(), handlerManager);
	}

	
	@Override
	public void setValue(String text) {			
		if (text==null)
			internalSetValue("");
		else
			internalSetValue(text);
	}

	private void internalSetValue(final String text) {			
		if (isLoaded) {
			_setHTML(text);
		} else {
			addLoadHandler(new LoadHandler() {
				
				@Override
				public void onLoad(LoadEvent event) {
					_setHTML(text);
					
				}
			});
		}
	
	}
	
	@Override
	public String getValue() {
		String text = getText();
		if (text!=null && text.startsWith("<p>***RAWHTML***") && text.endsWith("</p>")) {
			int start = 3;
			int end = text.length()-4;
			if (end>start)
				text = text.substring(start,end);
		}
		return text;
	}
	
	@Override
	public void setText(final String text) {
		setValue(text);
	}
	
	

	@Override
	public HandlerRegistration addLoadHandler(LoadHandler handler) {
		return handlerManager.addHandler(LoadEvent.getType(), handler);
	}
}
