package com.sinergise.geopedia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.entities.Translation;
import com.sinergise.geopedia.core.entities.Translation.Key;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.db.TableAndFieldNames.TFields;
import com.sinergise.geopedia.db.TableAndFieldNames.TTables;
import com.sinergise.geopedia.db.TableAndFieldNames.TThemes;
import com.sinergise.geopedia.db.TableAndFieldNames.TTranslations;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.db.entities.TranslationRepo;
import com.sinergise.geopedia.db.util.Update;
import com.sinergise.geopedia.server.service.MetaServiceImpl;
import com.sinergise.util.collections.Predicate;

public class TranslationServiceImpl  {
	
    private DBPoolHolder dbPoolHolder;
    private MetaData meta;
    
    public TranslationServiceImpl (DBPoolHolder dbPoolHolder) {
    	this.dbPoolHolder = dbPoolHolder;
    }
    
    private static TranslationRepo getRepository() {
    	return TranslationRepo.getRepository();
    }
	
	public void get(Theme theme, Language language) throws SQLException {
		if (theme==null || language==null) { return; }
		{
			String string = readFromCache(theme.id, Key.THEME_NAME, language);
			if (string!=null) {
				theme.setName(string);
			}
		}
		{
			String string = readFromCache(theme.id, Key.THEME_DESC, language);
			if (string!=null) {
				theme.descRawHtml = string;
				MetaServiceImpl.createDispHTML(theme);
			}
		}
		{
			if (theme.tables!=null && theme.tables.length>0) {
				for (ThemeTableLink link : theme.tables) {
					Table table = link.table;
					if (table!=null) {
						get(table, language);
					}
				}
			}
		}
	}
	public void get(Table table, Language language) throws SQLException {
		if (table==null || language==null) { return; }
		{
			String string = readFromCache(table.id, Key.TABLE_NAME, language);
			if (string!=null) {
				table.setName(string);
			}
		}
		{
			String string = readFromCache(table.id, Key.TABLE_DESC, language);
			if (string!=null) {
				table.descRawHtml = string;
				MetaServiceImpl.createDispHTML(table);
			}
		}
		{
			if (table.fields!=null && table.fields.length>0) {
				for (Field field : table.fields) {
					get(field, language);
				}
			}
		}
	}
	public void get(Field field, Language language) throws SQLException {
		if (field==null || language==null) { return; }
		{
			String string = readFromCache(field.id, Key.FIELD_NAME, language);
			if (string!=null) {
				field.setName(string);
			}
		}
		{
			String string = readFromCache(field.id, Key.FIELD_DESC, language);
			if (string!=null) {
				field.descRawHtml = string;
				MetaServiceImpl.createDispHTML(field);
			}
		}
	}
	
	public void set(Connection conn, Theme theme, Language language) throws SQLException {
		if (theme==null || language==null) { return; }
		write(conn, theme.id, Key.THEME_NAME, language, theme.getName());
		write(conn, theme.id, Key.THEME_DESC, language, theme.descRawHtml);
	}
	public void set(Connection conn, Table table, Language language) throws SQLException {
		if (table==null || language==null) { return; }
		if (table.isDeleted()) {
			deleteTable(table.id);
			return;
		}
		write(conn, table.id, Key.TABLE_NAME, language, table.getName());
		write(conn, table.id, Key.TABLE_DESC, language, table.descRawHtml);
	}
	public void set(Connection conn, Field field, Language language)  throws SQLException {
		if (field==null || language==null) { return; }
		if (field.isDeleted()) {
			deleteField(field.id);
			return;
		}
		write(conn, field.id, Key.FIELD_NAME, language, field.getName());
		write(conn, field.id, Key.FIELD_DESC, language, field.descRawHtml);
	}
	
	private String singleResult(final String sql, final String columnLabel) throws SQLException {
		
		return dbPoolHolder.executeLocal(new DBExecutor<String>() {
			public String execute(Connection conn) throws SQLException {
				
				PreparedStatement ps = conn.prepareStatement(sql);
				try {
					ResultSet rs = ps.executeQuery();
					try {
						while (rs.next()) {
							// this will return a single result
							return rs.getString(columnLabel);
						}
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
				return null;
			}
		});
	}
	
	public void initialLoad(Connection conn) throws SQLException {
		System.out.println("TranslationServiceImpl.initialLoad().started");
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TTranslations.TBL_NAME);
		try {
			ResultSet rs = ps.executeQuery();
			try {
				while (rs.next()) {
					int id = rs.getInt(TTranslations.ID);
					int key = rs.getInt(TTranslations.KEY);
					int language = rs.getInt(TTranslations.LANGUAGE);
					String string = rs.getString(TTranslations.STRING);
					Translation translation = new Translation(id, key, language, string);
					getRepository().writeToCache(translation);
				}
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
		System.out.println("TranslationServiceImpl.initialLoad().complete");
    }
	
	public void load(Connection conn, Theme theme) throws SQLException {
		if (theme==null || conn==null) { return; }
		load(conn, theme.id, Key.THEME_NAME, Key.THEME_DESC);
	}
	public void load(Connection conn, Table table) throws SQLException {
		if (table==null || conn==null) { return; }
		load(conn, table.id, Key.TABLE_NAME, Key.TABLE_DESC);
    }
	public void load(Connection conn, Field field) throws SQLException {
		if (field==null || conn==null) { return; }
		load(conn, field.id, Key.FIELD_NAME, Key.FIELD_DESC);
    }
	private void load(Connection conn, int translationId, Key nameKey, Key descriptionKey) throws SQLException {
		if (nameKey==null || descriptionKey==null || conn==null) { return; }
		
		PreparedStatement ps = conn.prepareStatement(
				" SELECT " + TTranslations.KEY + ", " + TTranslations.LANGUAGE + ", " + TTranslations.STRING +
				" FROM "+TTranslations.TBL_NAME +
				" WHERE " + TTranslations.ID+"="+translationId +
				" AND ( " + TTranslations.KEY+"="+nameKey.key() +
				"    OR " + TTranslations.KEY+"="+descriptionKey.key() +
				" ) ");
		try {
			ResultSet rs = ps.executeQuery();
			try {
				while (rs.next()) {
					int key = rs.getInt(TTranslations.KEY);
					int language = rs.getInt(TTranslations.LANGUAGE);
					String string = rs.getString(TTranslations.STRING);
					Translation translation = new Translation(translationId, key, language, string);
					getRepository().writeToCache(translation);
				}
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
    }
	
	private void write(Connection conn, Translation translation) throws SQLException {
		if (translation==null) { return; }
		Translation existing = getRepository().search(translation.getId(), translation.getKey(), translation.getLanguage());
		if (translation.matches(existing)) {
			// do nothing
		} else {
			// write to db and cache
			writeToDB(conn, translation.getId(), translation.getKey(), translation.getLanguage(), translation.getString());
			getRepository().writeToCache(translation);
		}
		// update theme_languages, table_languages, or field_languges
		updateLanguages(conn, translation.getId(), translation.getKey(), translation.getLanguage());
	}
	private void write(Connection conn, int id, Key key, Language language, String string) throws SQLException {
		write(conn, new Translation(id, key, language, string));
	}
	private void writeToDB(Connection conn, int id, Key key, Language language, String string) throws SQLException {
		if (key==null || language==null || string==null) { return; }
				String existing = readFromDB(id, key, language);
				if (existing!=null) {
					Update
					.update(TTranslations.TBL_NAME)
					.where(TTranslations.ID, id)
					.where(TTranslations.KEY, key.key())
					.where(TTranslations.LANGUAGE, language.key())
					.set(TTranslations.STRING, string)
					.execute(conn);
				} else {
					Update
					.insert(TTranslations.TBL_NAME)
					.set(TTranslations.ID, id)
					.set(TTranslations.KEY, key.key())
					.set(TTranslations.LANGUAGE, language.key())
					.set(TTranslations.STRING, string)
					.execute(conn);
				}
	}
	
	private String readFromCache(int id, Key key, Language language) throws SQLException {
		if (key==null || language==null) { return null; }
		Translation existing = getRepository().search(id, key, language);
		if (existing==null) {
			// read from db
			String string = readFromDB(id, key, language);
			if (string!=null) {
				// save to cache
				getRepository().writeToCache(new Translation(id, key, language, string));
			}
			return string;
		}
		return existing.getString();
	}
	private String readFromDB(int id, Key key, Language language) throws SQLException {
		if (key==null || language==null) { return null; }
		String sql =
			" SELECT " + TTranslations.STRING +
			" FROM " + TTranslations.TBL_NAME + " " +
			" WHERE " + TTranslations.ID + "=" + id +
			" AND " + TTranslations.KEY + "=" + key.key() +
			" AND " + TTranslations.LANGUAGE + "=" + language.key();
		return singleResult(sql, TTranslations.STRING);
	}
	
	public static void TRANSLATION_TAG() {}
	public static void FILTER_LANGUAGE_TAG() {}
	
	public Integer[] findThemes(Language language, Predicate<String> filter) {
		if (language==null || filter==null) { return null; }
		return getRepository().findThemes(language, filter);
	}
	public Integer[] findTables(Language language, Predicate<String> filter) {
		if (language==null || filter==null) { return null; }
		return getRepository().findTables(language, filter);
	}
	
	
	private Map<Language, Void> languageMap(String languages) {
		Map<Language, Void> languageMap = new HashMap<Language, Void>();
		if (!StringUtil.isNullOrEmpty(languages)) {
			languages = languages.trim();
			String[] codes = languages.split(" ");
			for (String code : codes) {
				Language language = Language.get(code);
				languageMap.put(language, null);
			}
		}
		return languageMap;
	}
	
	
	public void commitThemeLanguages(int themeId, String languages) {
		getRepository().commitThemeLanguages(themeId, languageMap(languages).keySet());
	}
	public void commitTableLanguages(int tableId, String languages) {
		getRepository().commitTableLanguages(tableId, languageMap(languages).keySet());
	}
	public void commitFieldLanguages(int fieldId, String languages) {
		getRepository().commitFieldLanguages(fieldId, languageMap(languages).keySet());
	}
	
	public LinkedList<Theme> filterThemeByLanguage(Collection<Theme> themes, Language language) {
		final LinkedList<Theme> outThemes = new LinkedList<Theme>();
		if (themes==null || themes.size()<1 || language==null) { return outThemes; }
		for (Theme theme : themes) {
			if (theme==null) { continue; }
			boolean success = getRepository().themeContains(theme.id, language);
			if (success) {
				outThemes.add(theme);
			}
		}
		return outThemes;
	}
	public LinkedList<Table> filterTableByLanguage(Collection<Table> tables, Language language) {
		final LinkedList<Table> outTables = new LinkedList<Table>();
		if (tables==null || tables.size()<1 || language==null) { return outTables; }
		for (Table table : tables) {
			if (table==null) { continue; }
			boolean success = getRepository().tableContains(table.id, language);
			if (success) {
				outTables.add(table);
			}
		}
		return outTables;
	}
	public LinkedList<Field> filterFieldByLanguage(Collection<Field> fields, Language language) {
		final LinkedList<Field> outFields = new LinkedList<Field>();
		if (fields==null || fields.size()<1 || language==null) { return outFields; }
		for (Field field : fields) {
			if (field==null) { continue; }
			boolean success = getRepository().fieldContains(field.id, language);
			if (success) {
				outFields.add(field);
			}
		}
		return outFields;
	}
	
	private String languageString(Collection<Language> languages) {
		String languageString = "";
		if (languages!=null) {
			for (Language language : languages) {
				if (language!=null) {
					String code = language.code();
					languageString+=code+" ";
				}
			}
		}
		return languageString.trim().toLowerCase();
	}
	
	private void writeThemeLanguages(Connection conn, final int themeId, final Collection<Language> languages) throws SQLException {
		if (languages==null) { return; }
		
		Update
		.update(TThemes.TBL_NAME)
		.where(TThemes.ID, themeId)
		.set(TThemes.LANGUAGES, languageString(languages))
		.execute(conn);
	}
	private void writeTableLanguages(Connection conn, final int tableId, final Collection<Language> languages) throws SQLException {
		
		Update
		.update(TTables.TBL_NAME)
		.where(TTables.ID, tableId)
		.set(TTables.LANGUAGES, languageString(languages))
		.execute(conn);
	}
	private void writeFieldLanguages(Connection conn, final int fieldId, final Collection<Language> languages) throws SQLException {
		if (languages==null) { return; }
		
		Update
		.update(TFields.TBL_NAME)
		.where(TFields.ID, fieldId)
		.set(TFields.LANGUAGES, languageString(languages))
		.execute(conn);
	}
	
	private void updateLanguages(Connection conn, int id, Key key, Language language) throws SQLException {
		{
			boolean isTheme = key==Key.THEME_NAME;
			if (isTheme) {
				boolean themeContains = getRepository().themeContains(id, language);
				if (! themeContains) {
					Collection<Language> languages = getRepository().readThemeLanguages(id);
					if (languages==null) {
						languages = new HashSet<Language>();
					}
					languages.add(language);
					writeThemeLanguages(conn, id, languages);
					getRepository().commitThemeLanguages(id, languages);
				}
			}
		}
		{
			boolean isTable = key==Key.TABLE_NAME;
			if (isTable) {
				boolean tableContains = getRepository().tableContains(id, language);
				if (! tableContains) {
					Collection<Language> languages = getRepository().readTableLanguages(id);
					if (languages==null) {
						languages = new HashSet<Language>();
					}
					(languages).add(language);
					writeTableLanguages(conn, id, languages);
					getRepository().commitTableLanguages(id, languages);
				}
			}
		}
		{
			boolean isField = key==Key.FIELD_NAME;
			if (isField) {
				boolean fieldContains = getRepository().fieldContains(id, language);
				if (! fieldContains) {
					Collection<Language> languages = getRepository().readFieldLanguages(id);
					if (languages==null) {
						languages = new HashSet<Language>();
					}
					languages.add(language);
					if (! SKIP_FIELD_LANGUAGES) {
						writeFieldLanguages(conn, id, languages);
					}
					getRepository().commitFieldLanguages(id, languages);
				}
			}
		}
	}
	
	private final boolean SKIP_FIELD_LANGUAGES = true;
	
	
	public void deleteTheme(int themeId) {
		getRepository().deleteTheme(themeId);
	}
	
	public void deleteTable(int tableId) throws SQLException {
		getRepository().deleteTable(tableId);
		
		Table serverSideTable=meta.getTableById(tableId);
    	serverSideTable=serverSideTable.clone(DataScope.ALL);
    	
    	Field[] fields = serverSideTable.fields;
    	if (fields != null && fields.length > 0) {
    		for (Field field : fields) {
    			if (field != null && field.id > 0) {
    				getRepository().deleteField(field.id);
    			}
    		}
    	}
	}

	public void deleteField(int fieldId) {
		getRepository().deleteField(fieldId);
	}

	public void setMetaData(MetaData meta) {
		this.meta = meta;
		
	}
}
