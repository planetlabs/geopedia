package com.sinergise.geopedia.server.service;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.outerj.daisy.htmlcleaner.HtmlCleaner;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.UserAccessControl;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.AbstractEntityWithDescription;
import com.sinergise.geopedia.core.entities.Category;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.entities.UpdateException;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.service.MetaService;
import com.sinergise.geopedia.db.DB;
import com.sinergise.geopedia.db.TranslationServiceImpl;
import com.sinergise.geopedia.db.entities.TableUtils;
import com.sinergise.geopedia.server.PediaRemoteServiceServlet;
import com.sinergise.geopedia.style.ParseStyleException;
import com.sinergise.geopedia.util.GeopediaServerUtility;
import com.sinergise.geopedia.util.LinkStorage;
import com.sinergise.util.collections.ArrayTool;
import com.sinergise.util.collections.Predicate;


public class MetaServiceImpl extends PediaRemoteServiceServlet implements
        MetaService {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MetaServiceImpl.class);
	
	private LinkStorage webLinkStorage;
	
	public MetaServiceImpl(Session defaultSession) {
		super(defaultSession);
	}
    public MetaServiceImpl() {
       /// No effect Gisopedia.configuration = getServerConfig();
    }
    

    protected void onBeforeRequestDeserialized(String serializedRequest) {
        super.onBeforeRequestDeserialized(serializedRequest);
        logger.debug("R: " + serializedRequest);
    }

    public Theme getThemeById(int id, long lastMetaTimestamp,
    		DataScope scope) throws GeopediaException {
    	final Session sess = ensureSession();
		final ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
        try {
            Theme t = instance.getMetaData().getThemeByIdMeta(id,
                    lastMetaTimestamp);
/*
            if (copyType == EntityConsts.COPY_SHALLOW)
                for (ThemeTableLink ttl : t.tables) {
                    try {
                        TableUtils.getStyleSpec(ttl);
                    } catch (ParseStyleException e) {
                    	logger.debug("getStyleSpec exception:",e);
                    }
                }
*/
            int hasPerms = UserAccessControl.checkAccess(sess, GeopediaEntity.THEME, t.id,
                    t.lastMetaChange, Permissions.THEME_VIEW);
            //final Theme out = EntityCloner.doCopy(t, null, copyType);
            final Theme out = t.clone(scope);
            out.user_perms = hasPerms;

//            if (copyType == EntityConsts.COPY_SHALLOW)
            if (out.tables!=null)
                out.tables = ArrayTool.filter(out.tables, new Predicate<ThemeTableLink>() {
                    public boolean eval(ThemeTableLink value) {
                        try {
                            if (UserAccessControl.hasAccess(sess, GeopediaEntity.TABLE,
                                    value.tableId, value.table.lastMetaChange,
                                    Permissions.TABLE_VIEW)) {
                                try {
                                    value.table.user_perms = UserAccessControl.checkAccess(
                                                    sess,
                                                    GeopediaEntity.TABLE,
                                                    value.tableId,
                                                    value.table.lastMetaChange,
                                                    Permissions.TABLE_VIEW);
                                    try {
                                        TableUtils.getStyleSpec(value.table, instance.getMetaData());
                                    } catch (ParseStyleException e) {
                                    }
                                    value.theme=out;
                                    return true;
                                } catch (GeopediaException e) {
                                    return false;
                                }
                            } else
                                return false;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                });
            
            
        	Language language =sess.getLanguage();
        	instance.getTranslationService().get(out, language);
        	TranslationServiceImpl.TRANSLATION_TAG();
            
            return out;
        } catch (SQLException e) {
        	logger.error("Database error:",e);
            throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
        }
    }

    @Override
	public Table getTableById(int id, long lastMetaTimestamp,
            DataScope scope) throws GeopediaException {
    	Session sess = ensureSession();
    	ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
        try {
            Table t = instance.getMetaData().getTableByIdMeta(id,
                    lastMetaTimestamp);
            int perms = UserAccessControl.checkAccess(sess, GeopediaEntity.TABLE, id, lastMetaTimestamp, Permissions.TABLE_VIEW);
            try {
                TableUtils.getStyleSpec(t, instance.getMetaData());
            } catch (ParseStyleException e) {
            }
            Table ret = t.clone(scope);
            ret.user_perms = perms;
            
            {
            	Language language = sess.getLanguage();
            	instance.getTranslationService().get(ret, language);
            	TranslationServiceImpl.TRANSLATION_TAG();
            }
            
            return ret;
        } catch (SQLException e) {
        	logger.error("Database error:",e);
            throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
        }
    }

          
    private static boolean verifyHTML(Session session, String html) {
    	if (StringUtil.isNullOrEmpty(html)) {
    		return true;
    	}
    	if (GeopediaServerUtility.isRawHTML(html) && !session.getUser().isAdmin()) {
    		return false;    		
    	}
		try {
		    HtmlCleaner.newDefaultInstance().cleanToString(html, false);
		} catch (Exception e) {
		    logger.debug("Invalid HTML '"+html+"'",e);
		    return false;
		}
    	return true;
    }

    public static void createDispHTML(AbstractEntityWithDescription e) {
        if (e.descRawHtml == null) {
            e.descDisplayableHtml = null;
            return;
        }
        e.descDisplayableHtml = e.descRawHtml.trim();
        if (GeopediaServerUtility.isRawHTML(e.descDisplayableHtml)) {
            e.descDisplayableHtml =  GeopediaServerUtility.removeRawHTMLHeader( e.descDisplayableHtml);
        } else {
            if (e.descDisplayableHtml.length() > 0) {
                try {
                    e.descDisplayableHtml = HtmlCleaner.newDefaultInstance().cleanToString(e.descDisplayableHtml, true);
                } catch (Exception e1) {
                	logger.error("createDisplayHTML Entity: id={} name: {}", new Object[] {e.id, e.getName(), e1});
                }
            }
        }
    }

    private static final char[][] charReplacements = new char[][] {
        new char[] { 'a', 'à', 'á', 'â', 'ã', 'ä', 'å' },
        new char[] { 'c', 'ç', 'č', 'ć' },
        new char[] { 'e', 'è', 'é', 'ê', 'ë' },
        new char[] { 'i', 'ì', 'í', 'î', 'ï' },
        new char[] { 'o', 'ò', 'ó', 'ô', 'õ', 'ö' },
        new char[] { 'r', 'ř' },
        new char[] { 's', 'š' }, new char[] { 'u', 'ù', 'ú', 'û', 'ü' },
        new char[] { 'z', 'ž' } };

    private static String processWeirdChars(String input) {
        char[][] settings = charReplacements;
        for (int i = 0; i < settings.length; i++) {
            char[] arr = settings[i];
            char target = arr[0];
            for (int j = 1; j < arr.length; j++) {
                input = input.replace(arr[j], target);
            }
        }
        return input;
    }

    public Table[] findTablesFulltext(String query)
            throws UpdateException, GeopediaException {
    	Session sess = getThreadLocalSession();
		if (sess==null) 
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
        query = FeatureServiceImpl.cleanFulltextQuery(query);

        String[] parts = query.split("[ ]");
        ArrayList<String> includes = new ArrayList<String>();
        ArrayList<String> excludes = new ArrayList<String>();
        for (String s : parts) {
            if (s.startsWith("-")) {
                excludes.add(processWeirdChars(s.substring(1)));
            } else {
                includes.add(processWeirdChars(s.substring(1)));
            }
        }
        final String[] inc = includes.toArray(new String[includes.size()]);
        final String[] exc = excludes.toArray(new String[excludes.size()]);
        
        Table[] tables = new Table[0];
        
        {
        	TranslationServiceImpl.TRANSLATION_TAG();
        	Language language = sess.getLanguage();
        	language=null; // TODO: verify why SI doesn't work
        	if (language!=null) {
        		// search table names and descriptions from translation cache
        		Predicate predicate = new Predicate<String>() {
					public boolean eval(String value) {
						String string = processWeirdChars(value.toLowerCase());

						for (String s : exc) {
							if (string.contains(s)) {
								return false;
							}
						}
						for (String s : inc) {
							if (! string.contains(s)) {
								return false;
							}
						}
						return true;
					}
				};
				List<Table> outTables = new LinkedList<Table>();
				Integer[] tableIds = instance.getTranslationService().findTables(language, predicate);
				for (int tableId : tableIds) {
					{
						{
							{
								logger.info("FOUND TABLE ID: " + tableId);
							}
						}
					}
					Table table = getTableById(tableId, 0, DataScope.BASIC); // DO YOU NEED a larger copyType?
					outTables.add(table);
				}
				
				{
                	if (sess.isFilterByLanguage()) {
                		outTables = instance.getTranslationService().filterTableByLanguage(outTables, language);
                	}
                	TranslationServiceImpl.FILTER_LANGUAGE_TAG();
				}
				
				tables = outTables.toArray(new Table[outTables.size()]);
			} else {
				tables = instance.getMetaData().findTables(new Predicate<Table>() {
					public boolean eval(Table value) {
						
						if (value.isDeleted() || !value.isQueryable()) {
							return false;
						}
						
						String name = processWeirdChars(value.getName().toLowerCase());
						String desc = processWeirdChars(value.descRawHtml.toLowerCase());

						for (String s : exc) {
							if (name.indexOf(s) >= 0 || desc.indexOf(s) >= 0) {
								return false;
							}
						}
						for (String s : inc) {
							if (name.indexOf(s) < 0 && desc.indexOf(s) < 0) {
								return false;
							}
						}
						return true;
					}
				});
			}
        }

        try {
            ArrayList<Table> out = new ArrayList<Table>();
            for (Table t : tables)
                if (UserAccessControl.hasAccess(sess, GeopediaEntity.TABLE, t.id, t.lastMetaChange, Permissions.TABLE_DISCOVER)) {
                    Table outt = t.clone(DataScope.BASIC);
                    outt.user_perms = UserAccessControl.checkAccess(sess, GeopediaEntity.TABLE, t.id, t.lastMetaChange, Permissions.TABLE_DISCOVER);
                    out.add(outt);
                }

            return out.toArray(new Table[out.size()]);
        } catch (SQLException e) {
        	logger.error("Database error!",e);
        	throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
        }
    }

    public Theme[] findThemesFulltext(String query)
            throws UpdateException, GeopediaException  {
    	Session sess = getThreadLocalSession();
		if (sess==null) 
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
        query = FeatureServiceImpl.cleanFulltextQuery(query);

        String[] parts = query.split("[ ]");
        ArrayList<String> includes = new ArrayList<String>();
        ArrayList<String> excludes = new ArrayList<String>();
        for (String s : parts) {
            if (s.startsWith("-")) {
                excludes.add(processWeirdChars(s.substring(1)));
            } else {
                includes.add(processWeirdChars(s.substring(1)));
            }
        }
        final String[] inc = includes.toArray(new String[includes.size()]);
        final String[] exc = excludes.toArray(new String[excludes.size()]);
        
        Theme[] themes = new Theme[0];
        
        {
        	TranslationServiceImpl.TRANSLATION_TAG();
        	Language language = sess.getLanguage();
        	language=null;
        	if (language!=null) {
        		// search theme names and descriptions from translation cache
        		Predicate predicate = new Predicate<String>() {
					public boolean eval(String value) {
						String string = processWeirdChars(value.toLowerCase());

						for (String s : exc) {
							if (string.contains(s)) {
								return false;
							}
						}
						for (String s : inc) {
							if (! string.contains(s)) {
								return false;
							}
						}
						return true;
					}
				};
				LinkedList<Theme> outThemes = new LinkedList<Theme>();
				Integer[] themeIds = instance.getTranslationService().findThemes(language, predicate);
				for (int themeId : themeIds) {
					Theme theme = getThemeById(themeId, 0, DataScope.BASIC); // DO YOU NEED a larger copyType?
					outThemes.add(theme);
				}
				
				{				
                	if (sess.isFilterByLanguage()) {
                		outThemes = instance.getTranslationService().filterThemeByLanguage(outThemes, language);
                	}
                	TranslationServiceImpl.FILTER_LANGUAGE_TAG();
				}
				
				themes = outThemes.toArray(new Theme[outThemes.size()]);
			} else {
				themes =instance.getMetaData().findThemes(new Predicate<Theme>() {
					public boolean eval(Theme value) {
						String name = processWeirdChars(value.getName().toLowerCase());
						String desc = processWeirdChars(value.descRawHtml.toLowerCase());

						for (String s : exc) {
							if (name.indexOf(s) >= 0 || desc.indexOf(s) >= 0) {
								return false;
							}
						}
						for (String s : inc) {
							if (name.indexOf(s) < 0 && desc.indexOf(s) < 0) {
								return false;
							}
						}
						return true;
					}
				});
			}
        }

        try {
            ArrayList<Theme> out = new ArrayList<Theme>();
            for (Theme t : themes)
                if (UserAccessControl.hasAccess(sess, GeopediaEntity.THEME, t.id, t.lastMetaChange, Permissions.THEME_DISCOVER)) {
                    Theme outt = t.clone(DataScope.BASIC);//EntityCloner.doCopy(t, null, EntityConsts.COPY_BASIC);
                    outt.user_perms = UserAccessControl.checkAccess(sess, GeopediaEntity.THEME, t.id, t.lastMetaChange, Permissions.THEME_DISCOVER);
                    out.add(outt);
                }

            return out.toArray(new Theme[out.size()]);
        } catch (SQLException e) {
        	logger.error("Database error!",e);
        	throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
        }
    }


 
//    public static void initStatic() {
//        getServerConfig();
//    }
//    
// 
//    
//	
//    private static Configuration SERVER_CONFIG = null;
//
//    public static Configuration getServerConfig() {
//        if (SERVER_CONFIG == null) {
//            SERVER_CONFIG = ConfigurationUtils.createConfiguration();
//        }
//        return SERVER_CONFIG;
//    }


	@Override
	public Configuration getConfiguration() throws GeopediaException {
		Session sess = getThreadLocalSession();
		if (sess==null) {
			HttpServletRequest req = getThreadLocalRequest();
			Enumeration<String> headers = req.getHeaderNames();
			String hdrTxt="";
			if (headers!=null) {
				while (headers.hasMoreElements()) {
					String hdrName = headers.nextElement();
					hdrTxt+="["+hdrName+"="+req.getHeader(hdrName)+"], ";
				}
			}
			String cookiesTxt = "";
			Cookie[] cookies = req.getCookies();
			if (cookies!=null) {
				for (Cookie cookie:cookies) {
					cookiesTxt+="["+cookie.getName()+"="+cookie.getValue()+"], ";
				}
			}
			logger.error("Session not found while loading configuration! This is strange! headers:"+hdrTxt+" cookies: "+cookiesTxt);
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		}
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		return instance.getCommonConfiguration();
	}
    

	/**
	 * ------------------------------------------------- NEW -------------------------------------------
	 * 
	 * hopefully deprecate everything above this line someday!
	 * **/
	
    
	
	@Override
	public ArrayList<Category> queryCategories(Category filter) throws GeopediaException {
		Session sess = ensureSession();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		return queryCategories(sess, instance.getDB(), filter);
	}
	
	public ArrayList<Category> queryCategories(Session session, DB db, Category filter) throws GeopediaException {
		try {
			return db.queryCategories(filter, session.getUser());
		} catch (SQLException ex) {
			logger.error("DB: Failed retrieving categories! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	@Override
	public PagableHolder<ArrayList<Table>> queryTables(Integer categoryId,	String tableName, int dataStartIdx, int dataEndIdx)
			throws GeopediaException {
		Session sess = ensureSession();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			return instance.getDB().queryTables(categoryId, tableName, sess.getUser(), dataStartIdx, dataEndIdx);
		} catch (SQLException ex) {
			logger.error("DB: Failed retrieving tables! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	
	@Override
	public PagableHolder<ArrayList<Theme>> queryThemes(Integer categoryId, String themeName, int dataStartIdx,
			int dataEndIdx) throws GeopediaException {
		Session sess = ensureSession();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			return instance.getDB().queryThemes(categoryId, themeName, sess.getUser(), dataStartIdx, dataEndIdx);
		} catch (SQLException ex) {
			logger.error("DB: Failed retrieving themes! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	
	@Override
	public PagableHolder<ArrayList<Table>> queryUserTables(PersonalGroup group, int dataStartIdx, int dataEndIdx)
			throws GeopediaException {
		Session sess = ensureLoggedInUser();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			return instance.getDB().queryUserTables(sess.getUser(), group, dataStartIdx, dataEndIdx);
		} catch (SQLException ex) {
			logger.error("DB: Failed retrieving user tables! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	
	
	@Override
	public PagableHolder<ArrayList<Theme>> queryUserThemes(PersonalGroup group, int dataStartIdx, int dataEndIdx)
			throws GeopediaException {
		Session sess = ensureLoggedInUser();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			return instance.getDB().queryUserThemes(sess.getUser(), group, dataStartIdx, dataEndIdx);
		} catch (SQLException ex) {
			logger.error("DB: Failed retrieving user themes! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	
	@Override
	public ArrayList<Object> queryUserTablesAndThemes(PersonalGroup group) 
			throws GeopediaException {
		Session sess = ensureLoggedInUser();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			return instance.getDB().queryUserTablesAndThemes(sess.getUser(), group);
		} catch (SQLException ex) {
			logger.error("DB: Failed retrieving user TablesAndThemes! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	
	@Override
	public Theme saveTheme(Theme theme) throws GeopediaException {
		Session sess = ensureLoggedInUser();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			if (theme.hasValidId()) {
				if (theme.isDeleted()) {
					UserAccessControl.verifyAccessRights(theme, sess.getUser(), Permissions.THEME_ADMIN);
				} else {
					UserAccessControl.verifyAccessRights(theme, sess.getUser(), Permissions.THEME_EDIT);
				}
			}
			
			if (!verifyHTML(sess, theme.descRawHtml)) {
				//TODO: throw exception
			}
			return instance.getDB().saveTheme(theme, sess.getUser());
		} catch (SQLException ex) {
			logger.error("DB: Theme update failed! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	@Override
	public Theme loadTheme(long themeId) throws GeopediaException {
		Session sess = ensureSession();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			Theme theme =  instance.getDB().loadTheme(themeId);
			UserAccessControl.verifyAccessRights(theme, sess.getUser(), Permissions.THEME_VIEW);
			return theme;
		} catch (SQLException ex) {
			logger.error("DB: theme loading failed!", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	
	
	@Override
	public Table saveTable(Table table) throws GeopediaException {
		Session sess = ensureLoggedInUser();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			if (table.hasValidId()) {
				if (table.isDeleted()) {
					UserAccessControl.verifyAccessRights(table, sess.getUser(), Permissions.TABLE_ADMIN);
				} else {
					UserAccessControl.verifyAccessRights(table, sess.getUser(), Permissions.TABLE_EDITMETA);
				}
			}
			return instance.getDB().saveTable(table, sess);
		} catch (SQLException ex) {
			logger.error("DB: Table update failed! ", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
	}
	@Override
	public void modifyPersonalGroup(GeopediaEntity entity, int entityId,
			PersonalGroup group, boolean delete) throws GeopediaException {
		Session sess = ensureLoggedInUser();
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		try {
			instance.getDB().modifyPersonalEntity(entity, entityId, sess.getUser().getId(), group, delete);
		} catch (SQLException ex) {
			logger.error("DB: Personal group modification failed!", ex);
			throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
		}
		
	}
	
}
