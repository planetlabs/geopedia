package com.sinergise.geopedia.db;

import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;

public class TableAndFieldNames {
	public static String getMetadataDBSchema(String prefix) {
		if (prefix==null) // legacy support
			return "pedicamain";
		return prefix+"metadata";
	}

	public static String getDataDBSchema(String prefix) {
		if (prefix==null) // legacy support
			return "pedicadata";
		return prefix+"data";
	}

	
	public static class FeaturesTable {
		
		public static FieldType getTypeForField(String field) {
			if (field.startsWith(FLD_ID) ||
				field.startsWith(FLD_USER)) {
				return FieldType.INTEGER;
			} else if (field.startsWith(FLD_DELETED)) {
				return FieldType.BOOLEAN;
			} else if (field.startsWith(FLD_FULLTEXT)) {
				return FieldType.PLAINTEXT;
			} else if (field.startsWith(FLD_TIMESTAMP)) {
				return FieldType.DATETIME;
			} else if (field.startsWith(FLD_GEOM)) {
				return FieldType.GEOMETRY;
			}
			throw new IllegalArgumentException("Unsupported field "+field);
		}
		
		private static final String TBL = ".t";
		private static final String HIST_TBL = ".ht";		
		public static final String FLD_ID = "id";
		public static final String FLD_HID = "hid";
		public static final String FLD_USER = "u";
		public static final String FLD_TIMESTAMP = "ts";
		public static final String FLD_FULLTEXT = "ft";
		public static final String FLD_DELETED = "d";
		public static final String FLD_GEOM = "geom";
		
		public static final String FLD_MINX = "minx";
		public static final String FLD_MINY = "miny";
		public static final String FLD_MAXX = "maxx";
		public static final String FLD_MAXY = "maxy";
		
		public static final String FLD_CENTROIDX = "cx";
		public static final String FLD_CENTROIDY = "cy";
		
		public static final String FLD_LENGTH = "len";
		public static final String FLD_AREA = "area";
		
		public static final String FLD_USERFIELD = "f";
		
		public static String id(int tableId) {
			return FLD_ID+tableId;
		}

		public static String id(long tableId) {
			return FLD_ID+tableId;
		}

		public static String hid(int tableId) {
			return FLD_HID+tableId;
		}
		
		public static String user(int tableId) {
			return FLD_USER+tableId;
		}

		public static String timestamp(int tableId) {
			return FLD_TIMESTAMP+tableId;
		}

		public static String deleted(int tableId) {
			return FLD_DELETED+tableId;
		}

		public static String fullText(int tableId) {
			return FLD_FULLTEXT+tableId;
		}

		public static String geometry(int tableId) {
			return FLD_GEOM+tableId;
		}
		
		public static String table(int tableId, InstanceConfiguration instanceConfiguration) {
			return getDataDBSchema(instanceConfiguration.serverCfg.updateDBConfig.schemaPrefix)+TBL+tableId;
		}
		public static String historyTable(int tableId, InstanceConfiguration instanceConfiguration) {
			return getDataDBSchema(instanceConfiguration.serverCfg.updateDBConfig.schemaPrefix)+HIST_TBL+tableId;		}

		public static String minX(int tableId) {
			return FLD_MINX+tableId;
		}
		public static String minY(int tableId) {
			return FLD_MINY+tableId;
		}
		public static String maxX(int tableId) {
			return FLD_MAXX+tableId;
		}
		public static String maxY(int tableId) {
			return FLD_MAXY+tableId;
		}

		public static String centroidX(int tableId) {
			return FLD_CENTROIDX+tableId;
		}

		public static String centroidY(int tableId) {
			return FLD_CENTROIDY+tableId;
		}

		public static String area(int tableId) {
			return FLD_AREA+tableId;
		}

		public static String length(int tableId) {
			return FLD_LENGTH+tableId;
		}

		public static String userField(Field f) {
			return FLD_USERFIELD+f.id;
		}

		public static String userField(int  fieldId) {
			return FLD_USERFIELD+fieldId;
		}
		
	}
	
	
	public interface TTables {

		String TBL_NAME = "tables";
		String ID = "table_id";
		String NAME = "table_name";
		String DESC = "table_desc";
		String STYLE = "table_style";
		String STYLEJS = "table_styleJS";
		String GEOMTYPE = "table_geomtype";
		String LASTDATAWRITE = "table_lastdatawrite";
		String LASTMETACHANGE = "table_lastmetachange";
		String PUBLICPERMS = "table_publicperms";
		//String REPTEXT = "table_reptext";
		String REPTEXTJS = "table_reptextJS";
		String DELETED = "table_deleted";
		String SYSID = "table_sysid";
		String PROPERTIES = "table_properties";
		String LANGUAGES = "table_languages";
		String KEYWORDS = "table_keywords";
		String SOURCE = "table_source";
		String LASTDATAWRITEDATE = "table_lastdatawritedate";
		String MINX = "table_minx";
		String MINY = "table_miny";
		String MAXX = "table_maxx";
		String MAXY = "table_maxy";
		String THUMB = "table_thumb";
	}

	public interface TFields {
		String TBL_NAME = "fields";
		String TABLE_ID = "table_id";
		String ID = "field_id";
		String NAME = "field_name";
		String DESC = "field_desc";
		String TYPE = "field_type";
		String REFD_TABLE_ID = "refd_table_id";
		String FLAGS = "field_flags";
		String DEFAULTVALUE = "field_defaultvalue";
		String ORDER = "field_order";
		String SYSID = "field_sysid";
		String PROPERTIES = "field_properties";
		String LANGUAGES = "field_languages";
	}

	public interface TThemes {
		String TBL_NAME = "themes";
		String ID = "theme_id";
		String NAME = "theme_name";
		String DESC = "theme_desc";
		String LASTMETACHANGE = "theme_lastmetachange";
		String PUBLICPERMS = "theme_publicperms";
		String DATASETS = "theme_datasets";
		String PROPERTIES = "theme_properties";
		String LANGUAGES = "theme_languages";
	}

	public interface TThemeTables {
		String TBL_NAME = "themetables";
		String THEME_ID = "theme_id";
		String TABLE_ID = "table_id";
		String ORDER = "themetable_order";
		//String STYLE = "themetable_style";
		String STYLEJS = "themetable_styleJS";
		String ONOFF = "themetable_onoff";
		String ID = "themetable_id";
		String GROUP = "themetable_group";
		String ALTNAME = "themetable_altName";
		String PROPERTIES = "themetable_properties";
	}

	public interface TCategories {

		String TBL_NAME = "cats";
		String ID = "cat_id";
		String PARENTID = "cat_parentid";
		String NAME = "cat_name";
		String DESC = "cat_desc";
		String PROPERTIES = "cat_properties";

	}

	public interface TCategoryTables {
		String TBL_NAME = "cattables";
		String CAT_ID = "cat_id";
		String TABLE_ID = "table_id";
	}

	@Deprecated
	/**
	 * Obsolete this in postgres, replace with timestamp (uSeconds)
	 *
	 */
	public interface TCounter {

		String TBL_NAME = "counter";
		String CURRVAL = "currval";

	}

	public interface TUsers {

		String TBL_NAME = "users";
		String ID = "user_id";
		String LOGIN = "user_login";
		String PASSMD5 = "user_passmd5";
		String EMAIL = "user_email";
		String ISADMIN = "user_isadmin";
		String FULLNAME = "user_fullname";
		String ORGANISATION = "user_org";
		String LASTACCESS = "user_lastaccess";
		String PROPERTIES = "user_properties";
		String AUTHENTICATION_KEY = "user_authentication_key";

	}

	public interface TPermissions {

		String TBL_NAME = "permissions";
		String OBJ_TYPE = "obj_type";
		String OBJ_ID = "obj_id";
		String ALLOWED_TYPE = "allowed_type";
		String ALLOWED_ID = "allowed_id";
		String ALLOWED_LEVEL = "allowed_level";

	}

	public interface TGroups {

		String TBL_NAME = "groups";
		String ID = "group_id";
		String NAME = "group_name";
		String PROPERTIES = "group_properties";

	}

	public interface TUserGroups {

		String TBL_NAME = "usergroups";
		String USER_ID = "user_id";
		String GROUP_ID = "group_id";
	}

	public interface TUserTables {

		String TBL_NAME = "usertables";
		String USER_ID="user_id";
		String TABLE_ID = "table_id";
		String GROUP = "usertable_group";
		String TIMESTAMP = "usertable_ts";
		
	}
	public interface TUserThemes {

		String TBL_NAME = "userthemes";
		String USER_ID="user_id";
		String THEME_ID = "theme_id";
		String GROUP = "usertheme_group";
		String TIMESTAMP = "usertheme_ts";
		
	}

	public interface TSessions {

		String TBL_NAME = "common.sessions";
		String ID = "id";
		String STATE = "state";
		String LASTUPDATED = "LastUpdated";
	}

	public interface TSymbols {
		String TBL_NAME = "symbols";
		String ID = "symbol_id";
		String ZIP = "zip_bytes";
		String SYMBOL_ORDER_UI = "symbol_order_ui";

	}

	public interface TImages {

		String TBL_NAME = "images";
		String ID = "image_id";
		String MIME = "image_mime";
		String WIDTH = "image_width";
		String HEIGHT = "image_height";
		String BYTES = "image_bytes";
		String BYTESMD5 = "image_bytesmd5";
		String UPLOADED = "image_uploaded";
		String PROPERTIES = "image_properties";

	}

	public interface TTranslations {

		String TBL_NAME = "translations";
		String ID = "translation_id";
		String KEY = "translation_key";
		String LANGUAGE = "translation_language";
		String STRING = "translation_string";

	}

	public interface TWidgets {

		String TBL_NAME = "widgets";
		String WIDGETID = "widget_id";
		String UNIQUEID = "widget_uniqueKey";
		String THEMEID = "theme_id";
		String ASUSER = "widget_allowedUser";

	}

	public interface TNews {

		String TBL_NAME = "news";
		String ID = "news_id";
		String TITLE = "news_title";
		String DATA = "news_data";
		String DATE = "news_date";

	}

	public interface TThemeCategories {
		String TBL_NAME = "catthemes";
		String CATEGORY_ID = "cat_id";
		String THEME_ID = "theme_id";
	}
}
