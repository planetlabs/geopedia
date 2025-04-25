/**
 * 
 */
package com.sinergise.geopedia.client.core.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

/**
 * 
 */
public interface Messages extends Constants {
	public static final Messages INSTANCE = (Messages) GWT.create(Messages.class);
	
	/// initialization
	@DefaultStringValue("An error occured while loading configuration")
	String errorLoadingConfiguration();
	@DefaultStringValue("An error occured while loading language")
	String errorLoadingLanguage();
	
	@DefaultStringValue("Language")
	String language();
	@DefaultStringValue("Geopedia Projects")
	String links();
	@DefaultStringValue("Help")
	String help();
	@DefaultStringValue("Share links")
	String shareLink();
	@DefaultStringValue("Highlight")
	String highlight();
	@DefaultStringValue("Zoom to")
	String zoomTo();
	@DefaultStringValue("Layer information")
	String layerInfo();
	@DefaultStringValue("Searched Coordinate")
	String coordSearched();
	
	@DefaultStringValue("Terms of service")
	String TermsOfService();
	
	@DefaultStringValue("Show more surfaces")
	String rasterMoreOptions();
	
	@DefaultStringValue("Error!")
	String generalError();
	@DefaultStringValue("Reading ...")
	String generalReading();
	@DefaultStringValue("Show/Hide")
	String generalShowHide();
	
	@DefaultStringValue("You are about to leave this page. Do you wish to continue?")
	String msgLeavingPage();
	
	@DefaultStringValue("Height profile")
	String FeatureInfoLabel_Height();
	@DefaultStringValue("Show profile")
	String FeatureInfoValue_Profile();
	
	@DefaultStringValue("Export feature")
	String FeatureInfoLabel_Export();
	@DefaultStringValue("Export")
	String FeatureInfoValue_Export();

	
	@DefaultStringValue("<error>")
	String RepText_errorTag();
	@DefaultStringValue("Point Query")
	String PointQueryAction_title();
	@DefaultStringValue("*Selected layers*")
	String MapLayers_SelectedLayers(); //"*Izbrani sloji*"
	
	@DefaultStringValue("Share link with others")
	String ShareLink_Title();
	@DefaultStringValue("Share link")
	String ShareLink_Subtitle();
	@DefaultStringValue("Use \"copy/paste\" command or send the link via e-mail")
	String ShareLink_Text();
	@DefaultStringValue("E-mail")
	String ShareLink_ActionSendEmail();
	@DefaultStringValue("Shared link from Geopedia")
	String ShareLink_MailSubject();
	@DefaultStringValue("Data:")
	String CopyrightPanel_Data();
	
	@DefaultStringValue("Log in")
	String LoginWidget_LoginAction();
	@DefaultStringValue("Username:")
	String LoginWidget_Username();
	@DefaultStringValue("Password:")
	String LoginWidget_Password();
	@DefaultStringValue("Greetings, ")
	String LoginWidget_Greeting();
	@DefaultStringValue("Illegal username or password!")
	String LoginWidget_DialogLoginError();
	@DefaultStringValue("Forgotten password")
	String LoginWidget_DialogPasswordReset();
	@DefaultStringValue("Register")
	String loginRegister();
	@DefaultStringValue("Registration")
	String loginRegistration();
	@DefaultStringValue("Don't have an account? Create it and start exploring world of Geopedia!")
	String loginRegisterText();
	
	@DefaultStringValue("Geopedia projects")
	String LinksDialog_Title();
	@DefaultStringValue("Web applications based on Geopedia technology")
	String LinksDialog_Subtitle();
	@DefaultStringValue("Enter")
	String LinksDialog_FollowLink();
	
	@DefaultStringValue("Error while reading data!")
	String LayerInfoDialog_LoadError();
	
	@DefaultStringValue("News")
	String NewsTitle();
	
	@DefaultStringValue("Search in progress ...")
	String FeatureResultPanel_Loading();
	@DefaultStringValue("Error loading data.")
	String FeatureResultPanel_Error();
	@DefaultStringValue("Too many results")
	String FeatureResultPanel_TableTooManyResults();
	
	@DefaultStringValue("Select result:")
	String WaypointSelector_SelectResult();
	@DefaultStringValue("Searching!")
	String WaypointSelector_Searching();
	@DefaultStringValue("No results!")
	String WaypointSelector_NoResults();
	
	@DefaultStringValue("Find directions")
	String RoutingPanel_Title();
	@DefaultStringValue("Add waypoint")
	String RoutingPanel_AddWaypoint();
	@DefaultStringValue("Find directions")
	String RoutingPanel_Search();
	@DefaultStringValue("Duration:")
	String RoutingPanel_Duration();
	@DefaultStringValue("Distance:")
	String RoutingPanel_Distance();
	@DefaultStringValue("Include avoiding highways")
	String RoutingPanel_AvoidHighways();
	@DefaultStringValue("Zoom to route")
	String RoutingPanel_ZoomToRoute();
	
	@DefaultStringValue("Show information")
	String ResultWidget_ShowInfo();
	@DefaultStringValue("Add layer")
	String ResultWidget_AddLayer();
	@DefaultStringValue("Show theme")
	String ResultWidget_ShowTheme();
	@DefaultStringValue("Themes")
	String ResultWidget_Themes();
	@DefaultStringValue("Layers")
	String ResultWidget_Layers();
	
	@DefaultStringValue("Tools")
	String ToolsPanel_Title();
	
	@DefaultStringValue("Personal content")
	String PersonalPanel_Title();
	@DefaultStringValue("Favorite themes and layers")
	String FavGroup_Title();
	@DefaultStringValue("Personal themes and layers")
	String PerGroup_Title();
	@DefaultStringValue("Go into edit mode")
	String EditMode();
	@DefaultStringValue("Do you wish to delete this layer?")
	String DeleteLayerText();
	@DefaultStringValue("Do you wish to delete this layer group?")
	String DeleteLayerGroupText();

	@DefaultStringValue("Height Profile")
	String HeightProfile_Heading();
	@DefaultStringValue("Altitude")
	String HeightProfile_Altitude();
	@DefaultStringValue("Distance")
	String HeightProfile_Distance();
	@DefaultStringValue("S")
	String HeightProfile_Start();
	@DefaultStringValue("E")
	String HeightProfile_End();
	@DefaultStringValue("Starting height")
	String HeightProfile_ToolTip_Start();
	@DefaultStringValue("Final height")
	String HeightProfile_ToolTip_End();
	@DefaultStringValue("Maximum height")
	String HeightProfile_ToolTip_Highest();
	@DefaultStringValue("Minimum height")
	String HeightProfile_ToolTip_Lowest();
	@DefaultStringValue("Height difference between maximum and minimum height")
	String HeightProfile_ToolTip_Delta();
	@DefaultStringValue("Height where line is ascending")
	String HeightProfile_ToolTip_Climb();
	@DefaultStringValue("Height where line is descending")
	String HeightProfile_ToolTip_Descent();
	@DefaultStringValue("Total distance")
	String HeightProfile_ToolTip_Total();
	@DefaultStringValue("Total distance where line is ascending")
	String HeightProfile_ToolTip_ClimbX();
	@DefaultStringValue("Total distance where line is descending")
	String HeightProfile_ToolTip_DescentX();
	@DefaultStringValue("Section ")
	String HeightProfile_TabPart();

	@DefaultStringValue("Additional layers")
	String virtualLayersGroupTitle();
	
	@DefaultStringValue("Graphical view for content ")
	String printGraphicalView();
	@DefaultStringValue("Print")
	String printBtnPrint();
	
	@DefaultStringValue("Wrong geometry, only one point is allowed!")
	String geometryErrorOnlyPoint();
	
	@DefaultStringValue("Topology error!")
	String geometryErrorTopology();
	
}
