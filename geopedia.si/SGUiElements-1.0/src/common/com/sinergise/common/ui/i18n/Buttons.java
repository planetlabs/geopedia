package com.sinergise.common.ui.i18n;

import com.google.gwt.i18n.client.Constants;

public interface Buttons extends Constants {
	
	public static final Buttons INSTANCE = ResourceUtil.create(Buttons.class);
	
	@DefaultStringValue("Add")
	String add();
	@DefaultStringValue("Back")
	String back();
	@DefaultStringValue("Cancel")
	String cancel();
	@DefaultStringValue("Close")
	String close();
	@DefaultStringValue("Delete")
	String delete();
	@DefaultStringValue("Edit")
	String edit();
	@DefaultStringValue("Empty")
	String empty();
	@DefaultStringValue("New")
	String newSmall();
	@DefaultStringValue("Next")
	String next();
	@DefaultStringValue("Skip")
	String skip();
	@DefaultStringValue("No")
	String no();
	@DefaultStringValue("OK")
	String ok();
	@DefaultStringValue("Pick")
	String pick();
	@DefaultStringValue("Print")
	String print();
	@DefaultStringValue("Refresh")
	String refresh();
	@DefaultStringValue("Remove")
	String remove();
	@DefaultStringValue("Replace")
	String replace();
	@DefaultStringValue("Reset")
	String reset();
	@DefaultStringValue("Save")
	String save();
	@DefaultStringValue("Search")
	String search();
	@DefaultStringValue("Select")
	String select();
	@DefaultStringValue("Yes")
	String yes();
	@DefaultStringValue("Go")
	String go();
	@DefaultStringValue("Select all")
	String selectAll();
	@DefaultStringValue("Unselect all")
	String selectNone();
	@DefaultStringValue("Download")
	String download();
	@DefaultStringValue("Finish")
	String finish();

}
