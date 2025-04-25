package com.sinergise.gwt.ui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ButtonImageBundle extends ClientBundle {

	public static final String DEFAULT_PREFIX = "public/";

	public static final String PATH16 = "icons/oxygen/16x16/";
	public static final String PATH22 = "icons/oxygen/22x22/";

	public final static String DISABLE16 = PATH16 + "disable/";
	public final static String DISABLE22 = PATH22 + "disable/";

	@Source(DEFAULT_PREFIX + "style/icons/general/Cancel16.gif")
	public ImageResource close();

	@Source(DEFAULT_PREFIX + "style/icons/general/Confirm16.gif")
	public ImageResource ok();

	@Source(DEFAULT_PREFIX + PATH22 + "document-print.png")
	public ImageResource print();

	@Source(DEFAULT_PREFIX + PATH22 + "accessories-text-editor.png")
	public ImageResource edit();

	@Source(DEFAULT_PREFIX + DISABLE22 + "accessories-text-editor.png")
	public ImageResource editDisabled();

	@Source(DEFAULT_PREFIX + PATH16 + "accessories-text-editor.png")
	public ImageResource editS();

	@Source(DEFAULT_PREFIX + DISABLE16 + "accessories-text-editor.png")
	public ImageResource editDisabledS();

	@Source(DEFAULT_PREFIX + PATH22 + "document-save.png")
	public ImageResource save(); 
	@Source(DEFAULT_PREFIX + DISABLE22 + "document-save.png")
	public ImageResource saveDisabled(); 

	@Source(DEFAULT_PREFIX + PATH16 + "document-save.png")
	public ImageResource saveS(); 
	@Source(DEFAULT_PREFIX + DISABLE16 + "document-save.png")
	public ImageResource saveDisabledS(); 

	@Source(DEFAULT_PREFIX +  PATH22 + "edit-delete.png")
	public ImageResource cancel(); 
	@Source(DEFAULT_PREFIX + DISABLE22 + "edit-delete.png")
	public ImageResource cancelDisabled(); 

	@Source(DEFAULT_PREFIX + PATH16 + "add_user.png")
	public ImageResource addUserS(); 		
	@Source(DEFAULT_PREFIX + PATH16 + "actions/edit_add.png")
	public ImageResource addS(); 			
	@Source(DEFAULT_PREFIX + PATH16 + "remove.png")
	public ImageResource removeS();
	
	@Source(DEFAULT_PREFIX + PATH22 + "actions/remove.png")
	public ImageResource remove();
	@Source(DEFAULT_PREFIX + PATH22 + "disable/remove.png")
	public ImageResource removeDisabled();
	
	@Source(DEFAULT_PREFIX + PATH16 + "user_group_new.png")
	public ImageResource addUserGroupS(); 	

	@Source(DEFAULT_PREFIX +  PATH16 + "edit-delete.png")
	public ImageResource cancelS(); 
	@Source(DEFAULT_PREFIX + DISABLE16 + "edit-delete.png")
	public ImageResource cancelDisabledS();

	@Source(DEFAULT_PREFIX +  PATH22 + "document-open.png")
	public ImageResource openFolder(); 
	@Source(DEFAULT_PREFIX + DISABLE22 + "document-open.png")
	public ImageResource openFolderDisabled(); 

	@Source(DEFAULT_PREFIX +  PATH16  + "dialog-ok-apply.png")
	public ImageResource okS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "dialog-ok-apply.png")
	public ImageResource okDisabledS(); 

	@Source(DEFAULT_PREFIX +  PATH16  + "edit-clear-locationbar-ltr.png")
	public ImageResource clearS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "edit-clear-locationbar-ltr.png")
	public ImageResource clearDisabledS(); 

	@Source(DEFAULT_PREFIX +  PATH16  + "edit-find.png")
	public ImageResource findS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "edit-find.png")
	public ImageResource findDisabledS(); 

	@Source(DEFAULT_PREFIX +  PATH16  + "pick_map.png")
	public ImageResource pickS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "pick_map.png")
	public ImageResource pickDisabledS(); 
	@Source(DEFAULT_PREFIX +  PATH22  + "pick_map.png")
	public ImageResource pick(); 
	@Source(DEFAULT_PREFIX +  DISABLE22 + "pick_map.png")
	public ImageResource pickDisabled(); 
	
	@Source(DEFAULT_PREFIX +  PATH16  + "document-new.png")
	public ImageResource newS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "document-new.png")
	public ImageResource newDisabledS(); 

	@Source(DEFAULT_PREFIX +  PATH22  + "document-new.png")
	public ImageResource newB(); 
	@Source(DEFAULT_PREFIX +  DISABLE22 + "document-new.png")
	public ImageResource newBDisabled(); 
	
	@Source(DEFAULT_PREFIX +  PATH16  + "actions/reload.png")
	public ImageResource refreshS(); 
	
	@Source(DEFAULT_PREFIX + PATH16 + "preferences_desktop_user.png")
	public ImageResource prefDesktopUser();
	
	@Source(DEFAULT_PREFIX + PATH16 + "x_office_contact.png")
	public ImageResource officeContact();
	
	@Source(DEFAULT_PREFIX + PATH16 + "kontact_todo.png")
	public ImageResource kontactTodo();
	
	@Source(DEFAULT_PREFIX +  PATH16  + "1leftarrow.png")
	public ImageResource leftArrowS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "1leftarrow.png")
	public ImageResource leftArrowDisabledS(); 
	
	@Source(DEFAULT_PREFIX +  PATH16  + "2leftarrow.png")
	public ImageResource leftDoubleArrowS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "2leftarrow.png")
	public ImageResource leftDoubleArrowDisabledS(); 
	
	@Source(DEFAULT_PREFIX +  PATH16  + "1rightarrow.png")
	public ImageResource rightArrowS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "1rightarrow.png")
	public ImageResource rightArrowDisabledS(); 
	
	@Source(DEFAULT_PREFIX +  PATH16  + "2rightarrow.png")
	public ImageResource rightDoubleArrowS(); 
	@Source(DEFAULT_PREFIX +  DISABLE16 + "2rightarrow.png")
	public ImageResource rightDoubleArrowDisabledS(); 
	
	@Source(DEFAULT_PREFIX +  PATH16  + "dialog-ok-apply.png")
	public ImageResource yesS(); 
	
	@Source(DEFAULT_PREFIX +  PATH16 + "no.png")
	public ImageResource noS(); 
	
	@Source(DEFAULT_PREFIX +  PATH16 + "reload.png")
	public ImageResource resetS();
	
}