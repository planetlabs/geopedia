package com.sinergise.geopedia.pro.client.ui.feature;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.ui.upload.UploadItem;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.geopedia.client.ui.feature.FeatWidgetFactory;
import com.sinergise.geopedia.client.ui.feature.ImageURL;
import com.sinergise.geopedia.client.ui.feature.PictureDisplayer;
import com.sinergise.geopedia.client.ui.feature.PictureDisplayer.PictureProvider;
import com.sinergise.geopedia.client.ui.panels.ActivatableTabPanel;
import com.sinergise.geopedia.client.ui.widgets.ReferencedTableLookup;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldFlags;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.properties.BinaryFileProperty;
import com.sinergise.geopedia.core.entities.properties.HTMLProperty;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.TinyMCEEditor;
import com.sinergise.geopedia.pro.client.ui.widgets.style.AdvancedJSStyleEditor;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.ProgressBar;
import com.sinergise.gwt.ui.SGParagraph;
import com.sinergise.gwt.ui.calendar.SGDatePicker;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.editor.IntegerEditor;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextArea;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.upload.Uploader;
import com.sinergise.gwt.ui.upload.Uploader.Status;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadCompleteHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadErrorHandler;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusEvent;
import com.sinergise.gwt.ui.upload.Uploader.UploadStatusHandler;

public class FeatureMetadataEditor extends ActivatableTabPanel {
	
	private Widget[] editorWidgets;
	private Feature feature;
	private FlowPanel contentPanel; 
	
	private class PhotoEditorWidget extends FlowPanel {
		private BinaryFileProperty valueHolder;
		private PictureDisplayer pictureDisplayer;
		private Uploader uploader;
		private FormPanel uploadForm;
		private SGPushButton btnDelete;
		private SGPushButton btnChange;
		private SGParagraph title;
		
		private SGPushButton btnUpload;
		private SGPushButton btnCancel;
		private FileUpload uploadElement;
		private ProgressBar pBar;

		public PhotoEditorWidget() {
			setStyleName("photoEditor");
			valueHolder = new BinaryFileProperty();
			uploadForm = new FormPanel();
			uploadForm.setStyleName("uploadImage");
			uploadForm.setMethod(FormPanel.METHOD_POST);
			uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
			uploadForm.setAction(GWT.getModuleBaseURL());
			Hidden hidden = new Hidden();
			uploadElement = new FileUpload();
			uploadElement.setName("UploadedFile");
			uploader = new Uploader(hidden, uploadForm);
			uploader.setUseDefaultSessionToken(true);
			uploader.addUploadCompleteHandler(new UploadCompleteHandler() {

				@Override
				public void onUploadComplete(UploadCompleteEvent event) {
					onStatusChanged();
					valueHolder.delete(); // if we're replacing and old value 
					valueHolder.setFileToken(event.getUploader().getCurrentToken());
					updateUI();
				}
			});
			uploader.addUploadStatusHandler(new UploadStatusHandler() {

				@Override
				public void onUploadStatusChange(UploadStatusEvent event) {
					onStatusChanged();
					UploadItem item = event.getItem();
					if (item != null) {
						pBar.setProgress(item.getPercentComplete());
					}
				}
			});
			uploader.addUploadErrorHandler(new UploadErrorHandler() {

				@Override
				public void onUploadError(UploadErrorEvent event) {
					onStatusChanged();
				}
			});
			
			btnUpload = new SGPushButton(GeopediaTerms.INSTANCE.uploadIt(), GeopediaProStyle.INSTANCE.upBlue(), new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					if (uploadElement.getFilename()==null || uploadElement.getFilename().length()==0)
						return;
					try {
						pBar.setVisible(true);
						uploader.reset();
						uploader.beginUpload();						
					} catch (Throwable t) {
					 // TODO: handle
					}

				}
			});

			btnCancel = new SGPushButton(Buttons.INSTANCE.cancel(), Theme.getTheme().standardIcons().close(), new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					showUploadForm(false);
				}
			});
			pBar = new ProgressBar();
			SGFlowPanel formPanel = new SGFlowPanel("uploadImgForm");
			formPanel.add(title = new SGParagraph(""));
			formPanel.add(uploadElement);
			formPanel.add(hidden);
			formPanel.add(btnUpload);
			formPanel.add(btnCancel);
			formPanel.add(pBar);
			pBar.setVisible(false);
			uploadForm.add(formPanel);
			showUploadForm(false);
			
			
			pictureDisplayer = new PictureDisplayer();
			pictureDisplayer.setStyleName("pictureDisplayer");
			btnChange = new SGPushButton();
			btnChange.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					if (!uploadForm.isVisible()) {
						showUploadForm(true);
					}
				}
			});
			btnDelete = new SGPushButton(Buttons.INSTANCE.delete(), Theme.getTheme().standardIcons().delete(), new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					if (valueHolder.hasValidFile()) {
						valueHolder.delete();
						updateUI();
					}
				}
			});
			btnDelete.addStyleName("deleteBtn");
			
			add(pictureDisplayer);
			add(btnChange);
			add(btnDelete);
			add(uploadForm);
			setStyleName("pictureMetaEditor");
			updateUI();
		}
				
		private void updateUI() {
			if (!valueHolder.hasValidFile()) {
				pictureDisplayer.setPictureProvider(null);
				title.setText(ProConstants.INSTANCE.chooseImage());
				btnDelete.setVisible(false);
				btnChange.setText(ProConstants.INSTANCE.uploadImage());
				btnChange.addStyleName("addImg");
				btnChange.setImage(Theme.getTheme().standardIcons().plus());
				addStyleName("noImage");
			} else {
				removeStyleName("noImage");
				btnDelete.setVisible(true);
				title.setText(ProConstants.INSTANCE.replaceImage());
				btnChange.setText(Buttons.INSTANCE.replace());
				btnChange.removeStyleName("addImg");
				btnChange.setImage(Theme.getTheme().standardIcons().refresh());
				if (valueHolder.getFileToken()!=null) {
					pictureDisplayer.setPictureProvider(new PictureProvider() {
						
						@Override
						public String getPictureUrl(int w, int h) {
							return GWT.getHostPageBaseURL()+"fileUploadService?cmd=getFile&token="+valueHolder.getFileToken()+"&w="+w+"&h="+h;
						}
					});
				} else {
					pictureDisplayer.setPictureProvider(new PictureProvider() {

						@Override
						public String getPictureUrl(int w, int h) {
							return ImageURL.createUrl(valueHolder.getValue(), w, h);
						}
					
					});
				}
			}
			uploadForm.setVisible(false);
		}
		private void showUploadForm(boolean show) {
			uploadForm.setVisible(show);
			if (show) {
				removeStyleName("noImage");
			} else {
				addStyleName("noImage");
			}
		}
		
		public void onStatusChanged() {
			Status st = uploader.getStatus();
			if (st == Status.SUBMITTING) {
				pBar.setVisible(true);
				btnUpload.setEnabled(false);
				btnUpload.setVisible(false);
				btnCancel.setVisible(false);
				btnCancel.setEnabled(false);
				uploadElement.setVisible(false);

			} else {
				pBar.setVisible(false);
				btnUpload.setEnabled(true);
				btnUpload.setVisible(true);
				btnCancel.setVisible(true);
				btnCancel.setEnabled(true);
				uploadElement.setVisible(true);
			}

		}

		public void setBinaryHolder(BinaryFileProperty valueHolder) {
			this.valueHolder=valueHolder;
			updateUI();			
		}

		public BinaryFileProperty getValueHolder() {
			return valueHolder;
		}
	}
	
	
	public FeatureMetadataEditor() {
		super(false);
		contentPanel = new FlowPanel();
		contentPanel.setStyleName("metaEditor");
		addContent(contentPanel);
	}
	
	
	public void setFeature(Feature feature) {
		this.feature=feature;
		FlowPanel content = new FlowPanel();
		editorWidgets = new Widget[feature.fields.length];
		for (int i=0;i<editorWidgets.length;i++) {
			Field field = feature.fields[i];
			if(!field.getVisibility().canEdit())
				continue;
			FlowPanel fieldHolderPanel = new FlowPanel();
			fieldHolderPanel.setStyleName("fieldHolder");
			if (field.type == FieldType.BOOLEAN) {
				fieldHolderPanel.addStyleName("checkbox");
			}
			InlineLabel fieldLabel = new InlineLabel(field.getName());//in case the field is editable and of type Boolean this label is hidden with CSS
			fieldHolderPanel.add(fieldLabel);
			if (!field.hasFlag(FieldFlags.READONLY)) {
				editorWidgets[i]=buildEditorWidget(field);
				if (field.isMandatory()){
					fieldHolderPanel.addStyleName("mandatory");
				}
				setEditorValue(field,editorWidgets[i], feature.properties[i]);
				fieldHolderPanel.add(editorWidgets[i]);
			} else {
				fieldHolderPanel.add(FeatWidgetFactory.createView(field, feature.properties[i]));
			}
			content.add(fieldHolderPanel);
		}
		content.add(new Breaker(15));
		contentPanel.add(content);
	}
	
	
	public void saveEditorValues(Feature feature) throws GeopediaException {
		if (this.feature != feature) {
			throw new IllegalArgumentException("Feature object does not match the object this editor was constructed with!");
		}
		for (int i=0;i<editorWidgets.length;i++) {
			Field field = feature.fields[i];
			if(!field.getVisibility().canEdit() || field.hasFlag(FieldFlags.READONLY))
				continue;
			feature.properties[i] = getEditorValue(field, editorWidgets[i], feature.properties[i]);
			Widget fieldHolderWidget = editorWidgets[i].getParent();
			fieldHolderWidget.removeStyleName("missingMandatory");
			if (field.isMandatory() && PropertyUtils.isNull(feature.properties[i])) {
				fieldHolderWidget.addStyleName("missingMandatory");
			}
		}
	}
	
	private Property<?> getEditorValue(Field field, Widget editorWidget, Property<?> valueHolder) throws GeopediaException {
		if (valueHolder == null) {
			valueHolder = PropertyUtils.forField(field);
		}
		switch (field.type) {
			case PLAINTEXT:
			case LONGPLAINTEXT:				
			{
				((TextProperty)valueHolder).setValue(((TextBoxBase)editorWidget).getValue());
				return valueHolder;
			}
			case WIKITEXT:
			{
				((HTMLProperty)valueHolder).setRawHtml(((TextBoxBase)editorWidget).getValue());
				return valueHolder;
			}
			case INTEGER:
			{
				((LongProperty)valueHolder).setValue(((IntegerEditor)editorWidget).getEditorValueLong());
				return valueHolder;
			}
			case FOREIGN_ID:
			{
				((LongProperty)valueHolder).setValue(((ReferencedTableLookup)editorWidget).getEditorValue());
				return valueHolder;
			}
			case DECIMAL:
			{
				((DoubleProperty)valueHolder).setValue(((DoubleEditor)editorWidget).getEditorValue());
				return valueHolder;
			}
			case DATE:
			case DATETIME:
			{
				Date date = ((SGDatePicker)editorWidget).getDate();
				((DateProperty)valueHolder).setValue(date);
				return valueHolder;
			}
			case BOOLEAN:
			{
				((BooleanProperty)valueHolder).setValue(((CheckBox)editorWidget).getValue());
				return valueHolder;
			}
			case BLOB:
			{
				return ((PhotoEditorWidget)editorWidget).getValueHolder();
			}			
			case STYLE:
			{
				((TextProperty)valueHolder).setValue(((AdvancedJSStyleEditor)editorWidget).getValue());
				return valueHolder;
			}
		}
		return valueHolder;
	}
	
	private void setEditorValue(Field field, Widget editorWidget, Property<?> valueHolder) {
		switch (field.type){
			case PLAINTEXT:
			case LONGPLAINTEXT: 
			{
				TextBoxBase tbb = (TextBoxBase) editorWidget;
				if (valueHolder!=null) {
					TextProperty textProp = (TextProperty)valueHolder;
					tbb.setValue(textProp.getValue());
				}
				break;
			}
			case WIKITEXT:
			{
				TextBoxBase tbb = (TextBoxBase) editorWidget;
				if (valueHolder!=null) {
					HTMLProperty htmlProp = (HTMLProperty)valueHolder;
					tbb.setValue(htmlProp.getRawHtml());
				}
				break;				
			}
			case INTEGER:
			{
				if (valueHolder!=null) {					
					((IntegerEditor)editorWidget).setEditorValueNumber(((LongProperty)valueHolder).getValue());
				}
				break;
			}
			case DECIMAL:
			{
				if (valueHolder!=null) {
					((DoubleEditor)editorWidget).setEditorValue(((DoubleProperty)valueHolder).getValue());
				}
				break;
			}
			case FOREIGN_ID:
			{
				if (valueHolder!=null) {
					((ReferencedTableLookup)editorWidget).setEditorValue(((LongProperty)valueHolder).getValue());
				}				
				break;
			}
			case DATE:
			case DATETIME:
			{
				if (valueHolder!=null) {
					Date date;
					DateProperty dateProperty = (DateProperty)valueHolder;					
					((SGDatePicker)editorWidget).setDate(dateProperty.getValue());
				}
				break;
			}
			case BOOLEAN:
			{
				if (valueHolder!=null) {
					((CheckBox)editorWidget).setValue(((BooleanProperty)valueHolder).getValue());
				}
				break;
			}
			case BLOB:
			{
				if (valueHolder!=null) {
					((PhotoEditorWidget)editorWidget).setBinaryHolder((BinaryFileProperty)valueHolder);
				}
				break;
			}
			case STYLE:
			{
				if (valueHolder!=null) {
					((AdvancedJSStyleEditor)editorWidget).setValue(((TextProperty)valueHolder).getValue());
				}
				break;
			}

		}
		
	}
	
	private Widget buildEditorWidget(Field field) {
		switch (field.type) {
			case INTEGER:
			{
				IntegerEditor editor = new IntegerEditor();
				return editor;
			}
			case DECIMAL: 
			{
				DoubleEditor editor = new DoubleEditor();
				return editor;
			}
			
			case WIKITEXT:
			{
				TinyMCEEditor editor = new TinyMCEEditor();
				return editor;
			}
			case PLAINTEXT:
			{
				SGTextBox editor = new SGTextBox();
				return editor;
			}
			case LONGPLAINTEXT: 
			{
				SGTextArea editor = new SGTextArea();
				return editor;
			}

			case DATE:
			{	
				SGDatePicker dp = new SGDatePicker(DateTimeFormat.getFormat("yyyy-MM-dd"));
				return dp;
			}
			case DATETIME:
			{
				SGDatePicker dp = new SGDatePicker(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss"));
				return dp;
				
			}
			case BOOLEAN:
			{
				CheckBox editor = new CheckBox(field.getName());
				return editor;
			}
			case FOREIGN_ID:
			{
				//TODO: better widget
				ReferencedTableLookup rtl = new ReferencedTableLookup(field);
				return rtl;
			}
			case BLOB:
			{
				PhotoEditorWidget iuw = new PhotoEditorWidget();
				return iuw;
			}
			case STYLE:
			{
				AdvancedJSStyleEditor styleEditor = new AdvancedJSStyleEditor("150px");
				styleEditor.onResize();
				return styleEditor;
			}
			
		}
		return new SGTextBox();
	}
	
	
	
	
}
