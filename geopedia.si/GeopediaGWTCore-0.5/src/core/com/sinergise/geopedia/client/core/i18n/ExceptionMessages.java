package com.sinergise.geopedia.client.core.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface ExceptionMessages extends Constants {
	public static final ExceptionMessages INSTANCE = (ExceptionMessages) GWT.create(ExceptionMessages.class);
	
	@DefaultStringValue("Unknown or corrupted file! Close the dialog and try again.")
	String GeopediaException_UnknownOrCorruptedFile();
	@DefaultStringValue("Session has expired or is not valid! Log in again.")
	String GeopediaException_IllegalSession();
	@DefaultStringValue("Unknown error! Report it to <a href=\"mailto:info@geopedia.si\">info@geopedia.si</a>")
	String GeopediaException_UnknownException();
	@DefaultStringValue("Server error! Please try again later or report it to <a href=\"mailto:info@geopedia.si\">info@geopedia.si</a>")
	String GeopediaException_ServerError();
	@DefaultStringValue("User is not logged in.")
	String GeopediaException_NotLogged();
	@DefaultStringValue("User doesn't have rights to access data.")
	String GeopediaException_NoRights();
	@DefaultStringValue("Error while accessing the databse.")
	String GeopediaException_DatabaseError();
	@DefaultStringValue("User doesn't exist.")
	String GeopediaException_UserDoesntExist();
	@DefaultStringValue("Invalid user!")
	String GeopediaException_InvalidUser();
	@DefaultStringValue("Invalid widget ID!")
	String GeopediaException_InvalidWidgetID();
	
	@DefaultStringValue("Initial session creation failed!")
	String InitialSessionCreationFailed();
}
