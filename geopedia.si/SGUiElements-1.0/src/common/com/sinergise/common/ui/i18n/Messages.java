package com.sinergise.common.ui.i18n;

import com.google.gwt.i18n.client.Constants;

/**
 * @author tcerovski
 *
 */
public interface Messages extends Constants {

	public static final Messages INSTANCE = ResourceUtil.create(Messages.class);
	
	@DefaultStringValue("Error")
	String error();
	
	@DefaultStringValue("Your browser is not compatible.")
	String ie6Compatibility_notCompatible();
	
	@DefaultStringValue("Please upgrade it!")
	String ie6Compatibility_upgradeIt();
	
	@DefaultStringValue("Please wait...")
	String pleaseWait();

	@DefaultStringValue("This page uses cookies.")
	String cookieNotification();
	
	@DefaultStringValue("For better quality of our service we recommend usage of cookies.")
	String cookieNotificationAllow();
	
	@DefaultStringValue("details")
	String details();
	
	@DefaultStringValue("hide")
	String hide();
	
	@DefaultStringValue("Google Analytics")
	String ga();
	
	@DefaultStringValue("To improve quality of our services we remember pages you have visited")
	String gaDesc();
	
	@DefaultStringValue("Save settings")
	String saveSettings();
	
	@DefaultStringValue("Allow cookies")
	String allowCookies();
	@DefaultStringValue("Read more ...")
	String readMore();
	@DefaultStringValue("About cookies")
	String aboutCookiesTitle();
	
	@DefaultStringValue("Piškotki so majhne datoteke z informacijami, ki se shranijo na vaš računalnik za kratek čas. O namestitvi nujnih piškotkov " +
			"niste obveščeni, vendar se uporabljajo izključno z namenom prilagoditvi spletne strani uporabnikom ter poskrbijo za enostavnejšo uporabo " +
			"in hitrost uporabe. Piškotki omogočajo prikaz pravilnih informacij, ki jih navadno iščete. Piškotki ne omogočajo dostopa do uporabnikovega " +
			"računalnika in tudi ne razkrivajo osebnih podatkov, služijo le k boljši uporabniški izkušnji in izboljšanju spletne strani.")
	String aboutCookies();
	@DefaultStringValue("Used cookies")
	String usedCookiesTitle();
	@DefaultStringValue("Cookie explanation")
	String cookieDialogTitle();

}
