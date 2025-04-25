package com.sinergise.gwt.gis.map.print;

import static com.sinergise.common.gis.map.print.PrintScaleValue.AUTO_FROM_ENVELOPE;
import static com.sinergise.common.gis.map.print.PrintScaleValue.AUTO_FROM_FEATURES;
import static com.sinergise.common.gis.map.print.PrintScaleValue.MANUAL;
import static com.sinergise.common.gis.map.print.TemplateParam.PRINT_ATTRIBUTES;
import static com.sinergise.common.gis.map.print.TemplateParam.PRINT_GRAPHICS;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.map.print.PrintOutputFormat;
import com.sinergise.common.gis.map.print.PrintParams;
import com.sinergise.common.gis.map.print.PrintScaleValue;
import com.sinergise.common.gis.map.print.TemplateParam;
import com.sinergise.common.gis.map.print.TemplateSpec;
import com.sinergise.common.gis.map.print.TemplateSpec.PaperSize;
import com.sinergise.common.gis.map.print.TemplateSpec.TemplateUserInput;
import com.sinergise.common.ui.UiUtil;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.gwt.ui.ListBoxExt;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class MapPrintDialog extends DialogBox {
	protected static final PrintScaleValue[] DEFAULT_SCALE_OPTIONS = new PrintScaleValue[] {//
		PrintScaleValue.AUTO_FROM_ENVELOPE,
		PrintScaleValue.AUTO_FROM_FEATURES,
		PrintScaleValue.MANUAL,
		new PrintScaleValue(500), //
		new PrintScaleValue(1000),//
		new PrintScaleValue(2500), //
		new PrintScaleValue(5000), //
		new PrintScaleValue(10000), //
		new PrintScaleValue(25000), // 
		new PrintScaleValue(50000), //
		new PrintScaleValue(100000), //
		new PrintScaleValue(250000), //
		new PrintScaleValue(500000), //
		new PrintScaleValue(1000000), //
		new PrintScaleValue(2500000) //
	};
	private static MapPrintResources RESOURCES = GWT.create(MapPrintResources.class);
	
	public static interface PrintParamUi {
		Widget getLabel();
		Widget getControl();
		void applyValue(PrintParams params);
		boolean isCustom();
		void initializeUi(PrintParams params);
	}
	
	protected class TemplateSelectionUi implements PrintParamUi {

		private transient TemplateSpec[]		lastTemplates = new TemplateSpec[0];
		private ListBoxExt						comboTemplates	= new ListBoxExt(false);
		private Label							label = new Label(UiUtil.ensureColonForLabel(RESOURCES.dialogLabelTemplate()));

		{
			comboTemplates.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					TemplateSelectionUi.this.onValueChange();
				}
			});
		}
		
		@Override
		public boolean isCustom() {
			return false;
		}
		
		protected void onValueChange() {
			MapPrintDialog.this.templateSelectionChanged(getValue());
		}

		public void setTemplates(TemplateSpec[] templateSpecs) {
			comboTemplates.clear();
			this.lastTemplates = templateSpecs;
			for (int i = 0; i < templateSpecs.length; i++) {
				comboTemplates.addItem(templateSpecs[i].templateTitle, templateSpecs[i].templateXML);
			}
			MapPrintDialog.this.templateSelectionChanged(getValue());
			if (lastTemplates.length < 2) {
				comboTemplates.setEnabled(false);
			}
			MapPrintDialog.this.setTemplateUiVisible(lastTemplates.length > 0);
		}

		public TemplateSpec getValue() {
			if (lastTemplates.length == 0) {
				return TemplateSpec.DEFAULT;
			}
			return lastTemplates[comboTemplates.getSelectedIndex()];
		}
		
		@Override
		public void applyValue(PrintParams param) {
			TemplateSpec selected = getValue();
			param.template = selected == null ? null : selected.templateXML;
			param.configName = selected == null ? null : selected.configName;
		}
		
		@Override
		public Widget getLabel() {
			return label;
		}

		@Override
		public Widget getControl() {
			return comboTemplates;
		}
		
		@Override
		public void initializeUi(PrintParams params) {
			 setValue(params.template);
		}

		private void setValue(String templateXml) {
			comboTemplates.setValue(templateXml);
		}
	}

	protected static class ScaleSelectionUi implements PrintParamUi {
		private Label lblScale = new Label(UiUtil.ensureColonForLabel(RESOURCES.dialogLabelScale()));
		private ListBoxExt comboScales = new ListBoxExt(false);
		private TextBox customScale = new TextBox();
		{
			customScale.setVisibleLength(7);
		}

		private transient PrintScaleValue[] lastScales;

		private SGFlowPanel control = new SGFlowPanel();
		private InlineLabel customLabel;
		private boolean showAutoByFeature = true;
		private boolean showAutoByEnv = true;
		private boolean showManual = true;
		
		public ScaleSelectionUi() {
			control.add(comboScales);
			control.add(customLabel = new InlineLabel("1: "));
			control.add(customScale);

			setScaleOptions(DEFAULT_SCALE_OPTIONS);

			updateUi();
			
			comboScales.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					updateManualUi();
				}
			});
		}
		
		public void setShowManual(boolean showManual) {
			this.showManual = showManual;
			updateUi();
		}
		
		@Override
		public boolean isCustom() {
			return false;
		}

		public void setScaleOptions(PrintScaleValue[] scales) {
			lastScales = scales;
			updateUi();
		}

		private void updateUi() {
			PrintScaleValue prevVal = tryGetValue();
			comboScales.clear();
			if (showManual && !ArrayUtil.contains(lastScales, MANUAL)) {
				comboScales.addItem(getComboString(MANUAL), MANUAL.toUiValueString());
			}
			for (PrintScaleValue val : lastScales) {
				if (!enableScaleSelection(val)) {
					continue;
				}
				comboScales.addItem(getComboString(val), val.toUiValueString());
				if (val.equals(prevVal)) {
					comboScales.setSelectedIndex(comboScales.getItemCount() - 1);
				}
			}
			customLabel.setVisible(showManual);
			customScale.setVisible(showManual);

			comboScales.setEnabled(comboScales.getItemCount() > 1);
			updateManualUi();
		}

		private PrintScaleValue tryGetValue() {
			try {
				return getValue();
			} catch (Exception e) {
				return PrintScaleValue.AUTO_FROM_ENVELOPE;
			}
		}

		private boolean enableScaleSelection(PrintScaleValue val) {
			if (val.isAuto()) {
				if (!showAutoByEnv && AUTO_FROM_ENVELOPE.equals(val)) {
					return false;
				}
				if (!showAutoByFeature && AUTO_FROM_FEATURES.equals(val)) {
					return false;
				}
			}
			if (val.isManual() && !showManual) {
				return false;
			}
			return true;
		}

		private void updateManualUi() {
			if (getComboSelection().isManual()) {
				customScale.setEnabled(true);
			} else {
				customScale.setEnabled(false);
			}
		}

		@Override
		public Widget getLabel() {
			return lblScale;
		}
		@Override
		public Widget getControl() {
			return control;
		}
		public PrintScaleValue getComboSelection() {
			if (comboScales.getItemCount() < 1 || ArrayUtil.isNullOrEmpty(lastScales)) {
				return PrintScaleValue.MANUAL;
			}
			return PrintScaleValue.fromUiValueString(comboScales.getValue());
		}
		@Override
		public void applyValue(PrintParams param) {
			param.scaleSpec = getValue();
		}

		private PrintScaleValue getValue() {
			PrintScaleValue sel = getComboSelection();
			if (!sel.isManual()) {
				return sel;
			}
			return new PrintScaleValue(Integer.parseInt(customScale.getValue()));
		}

		@Override
		public void initializeUi(PrintParams params) {
			this.showAutoByFeature = !params.highlight.isEmpty();
			this.showAutoByEnv = !params.envelope.isEmpty();
			updateUi();
			setValue(params.scaleSpec);
		}
		
		private void setValue(PrintScaleValue scaleSpec) {
			for (int i = 0; i < lastScales.length; i++) {
				if (scaleSpec.equals(lastScales[i])) {
					comboScales.setSelectedIndex(i);
					return;
				}
			}
		}

		private static String getComboString(PrintScaleValue val) {
			if (PrintScaleValue.AUTO_FROM_ENVELOPE.equals(val)) {
				return RESOURCES.dialogScaleAutoFromMap();
				
			} else if (PrintScaleValue.AUTO_FROM_FEATURES.equals(val)) {
				return RESOURCES.dialogScaleAutoFromFeatures();
					
			} else if (PrintScaleValue.MANUAL.equals(val)) {
				return RESOURCES.dialogScaleManual();
				
			}
			return val.toCanonicalString();
		}
	}
	
	protected static class PaperSizeUi implements PrintParamUi {
		private RadioButton[] sizeRadios;
		private FlowPanel radioPnl;
		private Label label = new Label(UiUtil.ensureColonForLabel(RESOURCES.dialogLabelPaperSize()));
		
		public PaperSizeUi() {
			createRadioButtons();
			createControl();
		}
		
		@Override
		public boolean isCustom() {
			return false;
		}

		@Override
		public Panel getControl() {
			return radioPnl;
		}
		
		@Override
		public Label getLabel() {
			return label;
		}
		
		protected void updateRadios(EnumSet<PaperSize> available) {
			int cnt = 0;
			RadioButton lastEn = sizeRadios[0];
			for (PaperSize pSize : PaperSize.values()) {
				int idx = pSize.ordinal();
				boolean en = available != null && available.contains(pSize);
				sizeRadios[idx].setEnabled(en);
				sizeRadios[idx].setVisible(en);
				if (en) {
					lastEn = sizeRadios[idx];
					cnt++;
				} else {
					sizeRadios[idx].setValue(Boolean.FALSE);
				}
			}
			if (cnt == 1) {
				lastEn.setEnabled(true);
				lastEn.setValue(Boolean.TRUE);
			}
			setVisible(cnt>1);
		}
		
		private void setVisible(boolean visible) {
			label.setVisible(visible);
			radioPnl.setVisible(visible);
		}

		private void createRadioButtons() {
			sizeRadios = new RadioButton[PaperSize.values().length];
			for (int i = 0; i < sizeRadios.length; i++) {
				PaperSize curPaperSize = PaperSize.values()[i];
				sizeRadios[i] = new RadioButton("pSize", curPaperSize.getLabel());
				sizeRadios[i].setFormValue(curPaperSize.name());
			}
		}

		private void createControl() {
			radioPnl = new FlowPanel();
			for (int i = 0; i < sizeRadios.length; i++) {
				radioPnl.add(sizeRadios[i]);
			}
		}

		public PaperSize getValue() {
			for (RadioButton rb : sizeRadios) {
				if (rb.getValue().booleanValue()) {
					return PaperSize.valueOf(rb.getFormValue());
				}
			}
			return null;
		}
		
		@Override
		public void applyValue(PrintParams param) {
			param.size = getValue();
		}
		
		@Override
		public void initializeUi(PrintParams params) {
			setValue(params.size);
		}

		private void setValue(PaperSize size) {
			for (RadioButton rb : sizeRadios) {
				if (size == null) {
					rb.setValue(Boolean.valueOf(rb.isEnabled()));
					continue;
				}
				if (rb.getFormValue().equals(size.name())) {
					rb.setValue(Boolean.TRUE);
					return;
				}
			}
		}
	}
	
	public static class CustomStringParamUi implements PrintParamUi {
		Label label;
		TextBoxBase field;
		TemplateParam param;
		
		public CustomStringParamUi(TemplateUserInput templateUserParam) {
			this.param = templateUserParam.param;
			this.label = new Label(UiUtil.ensureColonForLabel(templateUserParam.uiLabel));
			this.field = templateUserParam.length < 40 ? createTextBox() : createTextArea(templateUserParam.length);
		}
		
		private TextBoxBase createTextArea(final int len) {
			TextArea ret = new TextArea();
			ret.setVisibleLines(len/32 + 1);
			ret.setCharacterWidth(32);
			ret.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					trimTextBox((TextBoxBase)event.getSource(), len);
				}
			});
			ret.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					trimTextBox((TextBoxBase)event.getSource(), len);
				}
			});
			return ret;
		}

		private TextBoxBase createTextBox() {
			return new TextBox();
		}

		@Override
		public boolean isCustom() {
			return true;
		}

		@Override
		public Label getLabel() {
			return label;
		}
		
		@Override
		public TextBoxBase getControl() {
			return field;
		}

		@Override
		public void applyValue(PrintParams printParams) {
			String value = StringUtil.trimNullEmpty(field.getText());
			if (value == null) {
				printParams.customParams.remove(param);
			} else {
				printParams.setCustom(param, value);
			}
		}
		
		@Override
		public void initializeUi(PrintParams params) {
			field.setText(params.getCustom(param));
		}
		

		public void trimTextBox(TextBoxBase src, final int len) {
			String txt = src.getText();
			if (txt.length() > len) {
				src.setText(txt.substring(0, len));
			}
		}
	}
	
	protected static class ContentSelectionUi implements PrintParamUi {
		private FlowPanel control = new FlowPanel();
		private Label label = new Label(UiUtil.ensureColonForLabel(RESOURCES.dialogLabelContentSelection()));

		private CheckBox mapBox = new CheckBox(RESOURCES.dialogCheckboxPrintMap());
		private CheckBox attributesBox = new CheckBox(RESOURCES.dialogCheckboxPrintAttributes());
		
		public ContentSelectionUi() {
			control.add(mapBox);
			control.add(attributesBox);
			
			mapBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					ContentSelectionUi.this.checkForceSelection(attributesBox);
				}
			});
			attributesBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					ContentSelectionUi.this.checkForceSelection(mapBox);
				}
			});
		}
		
		protected void checkForceSelection(CheckBox boxToForce) {
			if (!mapBox.getValue().booleanValue() && !attributesBox.getValue().booleanValue()) {
				boxToForce.setValue(Boolean.TRUE);
			}
		}

		@Override
		public boolean isCustom() {
			return false;
		}
	
		@Override
		public Widget getControl() {
			return control;
		}
		
		@Override
		public Label getLabel() {
			return label;
		}
	
		@Override
		public void applyValue(PrintParams param) {
			param.setCustom(PRINT_GRAPHICS, mapBox.getValue().toString());
			param.setCustom(PRINT_ATTRIBUTES, attributesBox.getValue().toString());
		}
		
		@Override
		public void initializeUi(PrintParams params) {
			mapBox.setValue(Boolean.valueOf(StringUtil.isTruthy(params.getCustom(PRINT_GRAPHICS), true)));
			attributesBox.setValue(Boolean.valueOf(StringUtil.isTruthy(params.getCustom(PRINT_ATTRIBUTES), true)));
			setHasAttributes(!params.highlight.isEmpty());
		}

		private void setHasAttributes(boolean hasAtts) {
			if (!hasAtts) {
				mapBox.setValue(Boolean.TRUE);
				setVisible(false);
			} else {
				setVisible(true);
			}
		}
		
		private void setVisible(boolean vis) {
			label.setVisible(vis);
			control.setVisible(vis);
		}

		public void setAttributesSupported(boolean hasAttributes) {
			setVisible(hasAttributes);
			if (!hasAttributes) {
				attributesBox.setValue(Boolean.FALSE);
				mapBox.setValue(Boolean.TRUE);
			}
		}
	}

	protected class FormatSelectionUi implements PrintParamUi {
		private PrintOutputFormat[] formats = PrintOutputFormat.values();
		
		private Label label = new Label(UiUtil.ensureColonForLabel(RESOURCES.dialogLabelFormat()));
		private ListBoxExt comboFormats = new ListBoxExt(false);
	
		public FormatSelectionUi() {
			setFormats(PrintOutputFormat.values());
		}
		
		@Override
		public boolean isCustom() {
			return false;
		}
		
		private void setFormats(PrintOutputFormat[] values) {
			this.formats = values;
			for (PrintOutputFormat f : values) {
				comboFormats.addItem(f.getLabel(), f.name());
			}
		}

		public PrintOutputFormat getValue() {
			if (formats.length == 0) {
				return PrintOutputFormat.PDF;
			}
			return PrintOutputFormat.valueOf(comboFormats.getValue());
		}
		
		@Override
		public void applyValue(PrintParams param) {
			PrintOutputFormat selected = getValue();
			param.format = selected == null ? MimeType.MIME_DOCUMENT_PDF : selected.getMimeType();
		}
		
		@Override
		public Widget getLabel() {
			return label;
		}
	
		@Override
		public Widget getControl() {
			return comboFormats;
		}
		
		@Override
		public void initializeUi(PrintParams params) {
			setValue(params.format);
		}

		private void setValue(MimeType format) {
			comboFormats.setValue(PrintOutputFormat.valueOf(format).name());
		}
	}

	protected FormatSelectionUi format  = new FormatSelectionUi();
	protected TemplateSelectionUi templates  = new TemplateSelectionUi();
	protected ScaleSelectionUi scale = new ScaleSelectionUi();
	protected PaperSizeUi paperSize = new PaperSizeUi();
	protected ContentSelectionUi contentSelection = new ContentSelectionUi();
	
	private List<PrintParamUi> allUis = new ArrayList<PrintParamUi>();
	
	private MapPrintDialogController controller;
	
	private FlexTable mainUiTable = new FlexTable();
	
	public MapPrintDialog(MapPrintDialogController controller) {
		super(false, true);
		this.controller = controller;
		setGlassEnabled(true);
		setAnimationEnabled(false);
		setText(RESOURCES.mapPrinting());
		initialize();
	}

	public void setTemplateUiVisible(boolean visible) {
		int row = allUis.indexOf(templates);
		if (mainUiTable.remove(templates.getControl())) {
			mainUiTable.removeCell(row, 1);
		}
		if (mainUiTable.remove(templates.getLabel())) {
			mainUiTable.removeCell(row, 0);
		}
		if (visible) {
			mainUiTable.insertCell(row, 0);
			mainUiTable.insertCell(row, 1);
			mainUiTable.setWidget(row, 0, templates.getLabel());
			mainUiTable.setWidget(row, 1, templates.getControl());
		}
	}

	public void setScaleVisible(boolean visible){
		if(scale == null){
			return;
		}
		
		if (scale.getControl() != null) scale.getControl().setVisible(visible);
		if (scale.getLabel() != null) scale.getLabel().setVisible(visible);
	}

	public void templateSelectionChanged(TemplateSpec value) {
		paperSize.updateRadios(value.availableSizes);
		paperSize.setValue(value.defaultPaperSize);
		
		contentSelection.setAttributesSupported(value.hasAttributes);
		
		updateCustomUi(value);
	}

	private void updateCustomUi(TemplateSpec selectedTemplate) {
		removeCustomUi();
		int customSize = selectedTemplate.options.length;
		for (int i = 0; i < customSize; i++) {
			CustomStringParamUi curUi = new CustomStringParamUi(selectedTemplate.options[i]);
			addParamUi(curUi);
		}
	}

	private void removeCustomUi() {
		int nRows = mainUiTable.getRowCount();
		for (int i = nRows-1; i >= 0; i--) {
			if (allUis.get(i).isCustom()) {
				allUis.remove(i);
				mainUiTable.removeRow(i);
			}
		}
	}

	protected void initialize() {
		FlowPanel bottomP = new FlowPanel();
		bottomP.setStyleName("printButtons");
		{ // Buttons on the bottom
			SGPushButton btnCancel = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonCancel(), Theme.getTheme().standardIcons().close() ,new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					cancelClicked();
				}
			});
			SGPushButton btnOk = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonPrint(), Theme.getTheme().standardIcons().print(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					okClicked();
				}
			});
			bottomP.add(btnOk);
			bottomP.add(btnCancel);
		}
		addParamUi(format);
		addParamUi(templates);
		addParamUi(scale);
		addParamUi(paperSize);
		addParamUi(contentSelection);
		
		FlowPanel rootP = new FlowPanel();
		rootP.setStyleName("printContainter");
		rootP.add(mainUiTable);
		rootP.add(bottomP);
		setWidget(rootP);
	}

	private void addParamUi(PrintParamUi ui) {
		allUis.add(ui);
		final int row = mainUiTable.getRowCount();
		mainUiTable.setWidget(row, 0, ui.getLabel());
		mainUiTable.setWidget(row, 1, ui.getControl());
		
		ui.initializeUi(controller.currentParams);
	}

	public void showAndPosition() {
		beforeShow();
		setPopupPositionAndShow(new PositionCallback() {
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				setPopupPosition((Window.getClientWidth()-offsetWidth)/2,(Window.getClientHeight()-offsetHeight)/2);
			}
		});
	}

	protected void beforeShow() {
		reset();
	}

	protected void cancelClicked() {
		controller.cancelClicked();
		hide(false);
	}

	protected void okClicked() {
		//TODO: UI catch and display errors while applying
		PrintParams param = controller.constructPrintParams();
		for (PrintParamUi ui : allUis) {
			ui.applyValue(param);
		}
		controller.okClicked(param);
		hide(false);
	}

	public void setTemplates(TemplateSpec[] templateSpecs) {
		templates.setTemplates(templateSpecs);
	}

	public void reset() {
	}

	public void setPrintDialogTitleCaption(String title) {
		setText(title);
	}
	
	public MapPrintDialogController getController() {
		return controller;
	}
}
