package com.sinergise.gwt.ui.maingui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface StandardUIConstants extends Constants {
	
	public static final StandardUIConstants STANDARD_CONSTANTS = GWT.create(StandardUIConstants.class);
	
	@DefaultStringValue("OK")
	String buttonOK();
	
	@DefaultStringValue("Cancel")
	String buttonCancel();

	@DefaultStringValue("Yes")	
	String buttonYes();
	
	@DefaultStringValue("No")
	String buttonNo();
	
	@DefaultStringValue("Save")
	String buttonSave();
	
	@DefaultStringValue("Add")
	String buttonAdd();
	
	@DefaultStringValue("Reset")
	String buttonReset();
	
	@DefaultStringValue("Refresh")
	String buttonRefresh();
	
	@DefaultStringValue("Export")
	String buttonExport();
	
	@DefaultStringValue("Search")
	String buttonSearch();
	
	@DefaultStringValue("Close")
	String buttonClose();
	
	@DefaultStringValue("Print")
	String buttonPrint();
	
	@DefaultStringValue("Login")
	String buttonLogin();
	
	@DefaultStringValue("Logout")
	String buttonLogout();
	
	@DefaultStringValue("Copy")
	String buttonCopy();
	
	@DefaultStringValue("Clear")
	String buttonClear();
	
	@DefaultStringValue("Confirm")
	String buttonConfirm();
	
	@DefaultStringValue("Help")
	String buttonHelp();
	
	@DefaultStringValue("Edit")
	String buttonEdit();
	
	@DefaultStringValue("Delete")
	String buttonDelete();
	
	@DefaultStringValue("Retire")
	String buttonRetire();
	
	@DefaultStringValue("Error")
	String error();

	
}
