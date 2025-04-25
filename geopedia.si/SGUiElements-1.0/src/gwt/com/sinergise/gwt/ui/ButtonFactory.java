package com.sinergise.gwt.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.resources.icons.StandardIcons;

public class ButtonFactory {
	
	protected static ButtonFactory factoryInstance = new ButtonFactory();
	private StandardIcons SI = Theme.getTheme().standardIcons();
	
	/** Sets factory instance that will produce buttons */
	public static void setFactoryInstance(ButtonFactory factory) {
		factoryInstance = factory;
	}
	
	protected static Buttons bmsg = GWT.create(Buttons.class);

	/* ### STATIC CONSTRUCTORS ### */
	
	public static ButtonBase createFindButton(){
		return factoryInstance.makeFindButton();
	}
	
	public static ButtonBase createPickButton(){
		return factoryInstance.makePickButton();
	}
	
	public static ButtonBase createNewButton() {
		return factoryInstance.makeNewButton();
	}
	
	public static ButtonBase createAddButton() {
		return factoryInstance.makeAddButton();
	}
	
	public static ButtonBase createCancelButton() {
		return factoryInstance.makeCancelButton();
	}
	
	public static ButtonBase createDeleteButton() {
		return factoryInstance.makeDeleteButton();
	}
	
	public static ButtonBase createRemoveButton() {
		return factoryInstance.makeRemoveButton();
	}
	
	public static ButtonBase createSaveButton() {
		return factoryInstance.makeSaveButton();
	}
	
	public static ButtonBase createEditButton() {
		return factoryInstance.makeEditButton();
	}
	
	public static ButtonBase createPrintButton() {
		return factoryInstance.makePrintButton();
	}
	
	public static ButtonBase createOkButton() {
		return factoryInstance.makeOkButton();
	}
	
	public static ButtonBase createClearButton() {
		return factoryInstance.makeClearButton();
	}
	
	public static ButtonBase createRefreshButton() {
		return factoryInstance.makeRefreshButton();
	}
	
	public static ButtonBase createLeftArrowButton(String label) {
		return factoryInstance.makeLeftArrowButton(label);
	}
	
	public static ButtonBase createRightArrowButton(String label) {
		return factoryInstance.makeRightArrowButton(label);
	}
	
	public static ButtonBase createPlainButton(String text) {
		return factoryInstance.makePlainButton(text);
	}
	
	public static ButtonBase createYesButton() {
		return factoryInstance.makeYesButton();
	}
	
	public static ButtonBase createNoButton() {
		return factoryInstance.makeNoButton();
	}
	
	public static ButtonBase createResetButton() {
		return factoryInstance.makeResetButton();
	}
	
	
	
	/* ### INSTANCE CONSTRUCTORS ### */
	
	protected ButtonBase makeFindButton(){
		return new SGPushButton(bmsg.search(), SI.search());
	}
	protected ButtonBase makePickButton(){
		return new SGPushButton(bmsg.pick(), SI.pick());
	}
	protected ButtonBase makeNewButton() {
		return new SGPushButton(bmsg.newSmall(), SI.plus());
	}
	protected ButtonBase makeAddButton() {
		return new SGPushButton(bmsg.add(), SI.plus());
	}
	protected ButtonBase makeCancelButton() {
		return new SGPushButton(bmsg.cancel(), SI.cancel());
	}
	protected ButtonBase makeDeleteButton() {
		return new SGPushButton(bmsg.delete(), SI.delete());
	}
	protected ButtonBase makeEraseButton() {
		return new SGPushButton(bmsg.delete(), SI.delete());
	}
	protected ButtonBase makeRemoveButton() {
		return new SGPushButton(bmsg.remove(), SI.delete());
	}
	protected ButtonBase makeSaveButton() {
		return new SGPushButton(bmsg.save(), SI.save());
	}
	protected ButtonBase makeEditButton() {
		return new SGPushButton(bmsg.edit(), SI.edit());
	}
	protected ButtonBase makePrintButton() {
		return new SGPushButton(bmsg.print(), SI.print());
	}
	protected ButtonBase makeOkButton() {
		return new SGPushButton(bmsg.ok(), SI.ok());
	}
	protected ButtonBase makeClearButton() {
		return new SGPushButton(bmsg.empty(), SI.clear());
	}
	protected ButtonBase makeRefreshButton() {
		return new SGPushButton(bmsg.refresh(), SI.refresh());
	}
	protected ButtonBase makeLeftArrowButton(String label) {
		return new SGPushButton(label, SI.arrowLeft());
	}
	protected ButtonBase makeRightArrowButton(String label) {
		return new SGPushButton(label, SI.arrowRight());
	}
	protected ButtonBase makePlainButton(String text) {
		return new Button(text);
	}
	protected ButtonBase makeYesButton() {
		return new SGPushButton(bmsg.yes(), SI.ok());
	}
	protected ButtonBase makeNoButton() {
		return new SGPushButton(bmsg.no(), SI.cancel());
	}
	protected ButtonBase makeResetButton() {
		return new SGPushButton(bmsg.reset(), SI.refresh());
	}
	
}
