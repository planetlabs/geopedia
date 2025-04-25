package com.sinergise.geopedia.pro.client.ui.importexport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractSettingsStackPanel;

public class CRSChooserPanel extends AbstractSettingsStackPanel {
	
	private Map<String, CRS> model = new LinkedHashMap<String, CRS>();
	
	private ListBox lbCRSList;
	public CRSChooserPanel(Collection<CRS> collection) {
		titleText = ProConstants.INSTANCE.Import_StackTitleCRSC();
		titleDetails = null;
		
		init();
		setCRSs(collection);
	}
	
	private void setCRSs(Collection<CRS> crss) {
		for (CRS crs : crss) {
			model.put(crs.getCode(), crs);
		}
		updateUI();
	}
	
	private void init() {
		lbCRSList = new ListBox();
		lbCRSList.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				updateDetails();
			}
		});
		
		add(lbCRSList);
	}
	
	private void updateUI() {
		lbCRSList.clear();
		for (String crsCode : model.keySet()) {
			lbCRSList.addItem(model.get(crsCode).getNiceName(), crsCode);
		}
		updateDetails();
	}
	
	private void updateDetails() {
		int idx = lbCRSList.getSelectedIndex();
		if (idx==-1) return;
		titleDetails=lbCRSList.getItemText(idx);			
		updateTitle();
	}
	
	public void setValue(CRS crs) {
		setValue(crs.getDefaultIdentifier());
	}
	
	public void setValue(CrsIdentifier crsId) {		
		for (int i=0;i<lbCRSList.getItemCount();i++) {
			if (lbCRSList.getValue(i).equals(crsId.getCode())) {
				lbCRSList.setSelectedIndex(i);
				return;
			}
		}
	}
	
	public CRS getValue() {
		int idx = lbCRSList.getSelectedIndex();
		if (idx == -1) return null;
		return model.get(lbCRSList.getValue(idx));
	}
	
	public CrsIdentifier getSelectedId() {
		CRS crs = getValue();
		if (crs != null) {
			return crs.getDefaultIdentifier();
		}
		return null;
	}
}