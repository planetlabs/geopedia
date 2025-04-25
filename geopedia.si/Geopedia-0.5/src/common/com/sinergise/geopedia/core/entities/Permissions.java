package com.sinergise.geopedia.core.entities;

public interface Permissions
{
  /****
   * 
   * TODO: 
   * no permissions
   * read but no discovery
   * read and discovery
   * add (edit only personal)
   * edit (add, edit)
   * admin (edit+metadata)
   * 
   * 
   * limitexport ability
   */
	public static int NOTHING = 0;
	public static int LOWEST = 32;		// min of all non-null permissions
	public static int ADMINPERMS = 128; // max of all permissions

	public static int THEME_VIEW  = LOWEST;
    public static int THEME_DISCOVER = 36;
	public static int THEME_EDIT  = 80;
	public static int THEME_ADMIN = ADMINPERMS;

	public static int TABLE_VIEW     = LOWEST;
    public static int TABLE_DISCOVER = 36;
	public static int TABLE_EDITDATA = 64;
	public static int TABLE_EDITMETA = 96;
	public static int TABLE_ADMIN    = ADMINPERMS;

	public static int CAT_VIEW = LOWEST;
	public static int CAT_EDITDATA = 64;
	public static int CAT_EDITMETA = 96;
	public static int CAT_ADMIN = ADMINPERMS;
	
	public static int GROUP_VIEW = LOWEST;
	public static int GROUP_ADMIN = ADMINPERMS;
	
	
	public enum GeopediaEntity {
		GENERAL(0), THEME(1),TABLE(2),CATEGORY(3),GROUP(4);
		private int id;
		private GeopediaEntity(int id) {
			this.id=id;
		}
		public int getId() {
			return id;
		}
		public static GeopediaEntity forId(int id) {
			for (GeopediaEntity entity:values()) {
				if (entity.id==id)
					return entity;
			}
			
			throw new IllegalArgumentException("Entity with id="+id+" does not exist!");
		}
	}
//	public static int T_THEME = 1;
//	public static int T_TABLE = 2;
//	public static int T_CAT   = 3;
//	public static int T_GROUP = 4;
	
    public static int T_SERVICE_GEOCODING    = 100;
    public static int T_SERVICE_ALTITUDE     = 101;
    public static int T_SERVICE_LINE_PROFILE = 102;
    
    public static int PERM_PEDIAPRO=12;
	
	public static int A_GROUP = 1;
	public static int A_USER = 2;

	
	public static abstract class Util
	{
	    public static boolean isValidPublicPerm(GeopediaEntity entity, int value) {
	        if (value==ADMINPERMS) return false;
	        return isValidPerm(entity, value);
	    }
		public static boolean isValidPerm(GeopediaEntity entity, int value)
        {
			if (value == NOTHING || value == LOWEST || value == ADMINPERMS)
				return true;
			
			if (entity == GeopediaEntity.THEME)
				return value == THEME_EDIT || value==THEME_DISCOVER;
			if (entity == GeopediaEntity.TABLE)
				return value == TABLE_EDITDATA || value == TABLE_EDITMETA || value == TABLE_DISCOVER;
			if (entity == GeopediaEntity.CATEGORY)
				return value == CAT_EDITDATA || value == CAT_EDITMETA;
			return false;
        }
	}
}
