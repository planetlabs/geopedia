package com.sinergise.gwt.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.ui.i18n.Labels;
import com.sinergise.common.ui.i18n.MeasurementUnits;
import com.sinergise.common.util.MeasurementUnit;
import com.sinergise.common.util.MeasurementUnit.MeasurementType;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

/**
 * @author tcerovski
 *
 */
public class MeasurementUnitConverter extends Composite {

	private List<MeasurementUnit> units = null; // model
	
	private ListBoxExt cbFirst;
	private ListBoxExt cbSecond;
	private DoubleEditor tbFirst;
	private DoubleEditor tbSecond;
	
	public MeasurementUnitConverter(List<MeasurementUnit> supportedUnits) {
		units = new ArrayList<MeasurementUnit>(supportedUnits);
		
		init();
		updateUI();
	}
	
	private void init() {
		FlexTableBuilder ftb = new FlexTableBuilder(StyleConsts.UNIT_CONVERTER);
		
		ftb.addFieldLabel(Labels.INSTANCE.fromUnit());
		ftb.addFieldValueWidget(cbFirst = new ListBoxExt());
		ftb.addFieldValueWidget(tbFirst = new DoubleEditor());
		ftb.newRow();
		ftb.addFieldLabel(Labels.INSTANCE.toUnit());
		ftb.addFieldValueWidget(cbSecond = new ListBoxExt());
		ftb.addFieldValueWidget(tbSecond = new DoubleEditor());
		
		SimplePanel unitsCont = new SimplePanel();
		unitsCont.add(ftb.buildTable());
		unitsCont.setStyleName(StyleConsts.UNIT_CONVERTER);
		initWidget(unitsCont);
		
		cbFirst.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				updateEnabledUnits();
				convertFromFirst();
			}
		});
		
		cbSecond.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				convertFromFirst();
			}
		});
		
		tbFirst.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				convertFromFirst();
			}
		});
		
		tbSecond.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				convertFromSecond();
			}
		});
		
	}
	
	private void updateUI() {
		tbFirst.setText("");
		tbSecond.setText("");
		
		cbFirst.clear();
		cbSecond.clear();
		for (MeasurementUnit unit : units) {
			String item = getUnitDisplayName(unit);
			cbFirst.addItem(item);
			cbSecond.addItem(item);
		}
		updateEnabledUnits();
	}
	
	private void updateEnabledUnits() {
		MeasurementType selectedType = units.get(cbFirst.getSelectedIndex()).type;
		boolean changeToUnit = units.get(cbSecond.getSelectedIndex()).type != selectedType;
		
		for (int i=0; i<units.size(); i++) {
			boolean enabled = units.get(i).type == selectedType;
			cbSecond.setOptionEnabled(i, enabled);
			if (changeToUnit && enabled) {
				cbSecond.setSelectedIndex(i);
				changeToUnit = false;
			}
		}
	}
	
	private void convertFromFirst() {
		convert(tbFirst, cbFirst, tbSecond, cbSecond);
	}
	
	private void convertFromSecond() {
		convert(tbSecond, cbSecond, tbFirst, cbFirst);
	}
	
	private void convert(DoubleEditor tbFrom, ListBox cbFrom, DoubleEditor tbTo, ListBox cbTo) {
		tbTo.setText("");
		Double fromVal = tbFrom.getEditorValue();
		
		if (fromVal != null) {
			MeasurementUnit unitFrom = units.get(cbFrom.getSelectedIndex());
			MeasurementUnit unitTo = units.get(cbTo.getSelectedIndex());
			double toVal = unitFrom.convertTo(fromVal.doubleValue(), unitTo);
			tbTo.setEditorValue(Double.valueOf(toVal));
		} 
	}
	
	private static String getUnitDisplayName(MeasurementUnit unit) {
		return MeasurementUnits.INSTANCE.getString(unit.name());
	}
	
}
