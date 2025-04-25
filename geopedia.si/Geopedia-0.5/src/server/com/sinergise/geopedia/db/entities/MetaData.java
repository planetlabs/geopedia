package com.sinergise.geopedia.db.entities;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.geopedia.app.ConfigurationUtils;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.db.DBExecutor;
import com.sinergise.geopedia.db.DBPoolHolder;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.geopedia.db.TableAndFieldNames.TFields;
import com.sinergise.geopedia.db.TableAndFieldNames.TTables;
import com.sinergise.geopedia.db.TableAndFieldNames.TThemeTables;
import com.sinergise.geopedia.db.TableAndFieldNames.TThemes;
import com.sinergise.geopedia.db.TranslationServiceImpl;
import com.sinergise.geopedia.server.service.MetaServiceImpl;
import com.sinergise.java.util.state.StateUtilJava;
import com.sinergise.util.collections.Predicate;

public class MetaData {
	private RefreshThread	refreshThread;
	private static final Logger logger = LoggerFactory.getLogger(MetaData.class);
	public static final String SYSPROP_SKIP_INITIAL_META_LOAD = "geopedia.skip.loading.metadata";
	
	private DBPoolHolder dbPoolHolder;
	private TranslationServiceImpl translationService;
	public MetaData(DBPoolHolder dbPoolHolder, TranslationServiceImpl translationService) throws SQLException
	{
		this.dbPoolHolder = dbPoolHolder;
		this.translationService = translationService;
		translationService.setMetaData(MetaData.this);
		//TODO: Add sysprop
		if (!Boolean.getBoolean(SYSPROP_SKIP_INITIAL_META_LOAD)) {
			initialLoad();
		}
		refreshThread = new RefreshThread(this);
		refreshThread.start();
	}
	
	public  TranslationServiceImpl getTranslationService() {
		return translationService;
    }
	
	//TODO: Remove dependency on fastUtil; ordinary HashMap<Integer, Field> wouuld probably be just fine
	Int2ObjectOpenHashMap<Field> fieldIdToField = new Int2ObjectOpenHashMap<Field>();
	Int2ObjectOpenHashMap<Table> tableIdToTable = new Int2ObjectOpenHashMap<Table>();
	Int2ObjectOpenHashMap<Theme> themeIdToTheme = new Int2ObjectOpenHashMap<Theme>();
	
	ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
	Lock readLock = rrwl.readLock();
	Lock writeLock = rrwl.writeLock();
	
	public Table getTableById(long tableId) throws SQLException {
		return getTableById((int)tableId);
	}
	
	public Table getTableById(int tableId) throws SQLException
	{
		if (tableId < 1)
			throw new IllegalArgumentException();
		
		readLock.lock();
		try {
			Table t = tableIdToTable.get(tableId);
			if (t != null)
				return t;
		} finally {
			readLock.unlock();
		}
		
		return getTableFromDb(tableId, Long.MIN_VALUE, Long.MIN_VALUE);
	}
	
	public Table getTableByIdMeta(int tableId, long minMetaTimestamp) throws SQLException
	{
		if (tableId < 1)
			throw new IllegalArgumentException("tableId should be positive");
		
		readLock.lock();
		try {
			Table t = tableIdToTable.get(tableId);
			if (t != null && t.lastMetaChange >= minMetaTimestamp) {
				return t;
			}
		} finally {
			readLock.unlock();
		}
		
		return getTableFromDb(tableId, Long.MIN_VALUE, minMetaTimestamp);
	}
	
	public Table getTableByIdData(final int tableId, final long minDataTimestamp) throws SQLException
    {
		return dbPoolHolder.executeLocal(new DBExecutor<Table>() {
			public Table execute(Connection conn) throws SQLException {
				if (tableId < 1)
					throw new IllegalArgumentException();
				logger.trace("getTableByIdData {} {}",tableId, minDataTimestamp);
				Table t;
				readLock.lock();
				try {
					t = tableIdToTable.get(tableId);
					if (t != null && t.lastDataWrite >= minDataTimestamp)
						return t;
				} finally {
					readLock.unlock();
				}
				logger.trace("Outdated, let's get from DB!");
				if (t == null) {
					return getTableFromDb(tableId, minDataTimestamp, Long.MIN_VALUE);
				}
				
				int numTries = 8;
				for (int a=0; a<numTries; a++) {
					PreparedStatement ps = conn.prepareStatement("SELECT "+TTables.LASTDATAWRITE+", "+TTables.LASTMETACHANGE+" FROM "+TTables.TBL_NAME+" WHERE "+TTables.ID+"=?");
					try {
						ps.setInt(1, tableId);
						ResultSet rs = ps.executeQuery();
						try {
							if (rs.next()) {
								long ts = rs.getLong(1);
								long metaTs = rs.getLong(2);
								
								if (ts >= minDataTimestamp) {
									writeLock.lock();
									try {
										t = tableIdToTable.get(tableId);
										if (t.lastDataWrite < ts) {
											logger.trace("lastDataWrite timestamp for table {} updated to {}",tableId,ts);
											t.lastDataWrite = ts;
										}
										if (t.lastMetaChange < metaTs)
											return getTableByIdMeta(tableId, metaTs);
										return t;
									} finally {
										writeLock.unlock();
									}
								}
							}
						} finally {
							DBUtil.close(rs);
						}
					} finally {
						DBUtil.close(ps);
					}
					
					if (a == 4) {
						// TODO: check with update
					}
					
					if (a != (numTries-1)) {
						try {
							Thread.sleep(50L << a);
						} catch (InterruptedException e) {
							// eh
						}
					}
				}
				logger.error("DB sync lasted too long!");
				throw new SQLException("DB sync lasted too long");
			}
		});
    }
	
	public Theme getThemeById(int themeId) throws SQLException
	{
		if (themeId < 1)
			throw new IllegalArgumentException();
		
		readLock.lock();
		try {
			Theme t = themeIdToTheme.get(themeId);
			if (t != null)
				return t;
		} finally {
			readLock.unlock();
		}
		
		return getThemeFromDb(themeId, Long.MIN_VALUE);
	}
	
	public Theme getThemeByIdMeta(int themeId, long minMetaTimestamp) throws SQLException
	{
		if (themeId < 1)
			throw new IllegalArgumentException();
		
		readLock.lock();
		try {
			Theme t = themeIdToTheme.get(themeId);
			if (t != null && t.lastMetaChange >= minMetaTimestamp) {
				return t;
			}
		} finally {
			readLock.unlock();
		}
		
		return getThemeFromDb(themeId, minMetaTimestamp);
	}
	
	private Theme getThemeFromDb(final int themeId, final long minMetaTimestamp) throws SQLException
	{
		return dbPoolHolder.executeLocal(new DBExecutor<Theme>() {
			public Theme execute(Connection conn) throws SQLException {
				conn.setAutoCommit(false);
				int numTries = 8;
				for (int a = 0; a < numTries; a++) {
					PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + TThemes.TBL_NAME + " WHERE " + TThemes.ID + "=?");
					try {
						ps.setInt(1, themeId);
						ResultSet rs = ps.executeQuery();
						try {
							if (rs.next()) {
								Theme t = new Theme();
								fillInRaw(t, rs);

								{
									getTranslationService().load(conn, t);
									TranslationServiceImpl.TRANSLATION_TAG();
								}

								if (t.lastMetaChange >= minMetaTimestamp) {
									Int2ObjectOpenHashMap<Table> tables = new Int2ObjectOpenHashMap<Table>();
									readTables(t, tables, conn);
									writeLock.lock();
									try {
										return commit(t);
									} finally {
										writeLock.unlock();
									}
								}
							}
						} finally {
							DBUtil.close(rs);
						}
					} finally {
						DBUtil.close(ps);
					}

					if (a == 4) {
						// TODO: check with update
					}
					
					if (a != (numTries-1)) {
						try {
							Thread.sleep(50L << a);
						} catch (InterruptedException e) {
							// eh
						}
					}
				}
				
				throw new SQLException("DB sync lasted too long");
			}
		});
	}
	
	private void readTables(Theme t, Int2ObjectOpenHashMap<Table> tables, Connection conn) throws SQLException
	{
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TThemeTables.TBL_NAME+" WHERE "+TThemes.ID+"=? ORDER BY "+TThemeTables.ORDER+", "+TTables.ID);
		try {
			ps.setInt(1, t.id);
			ResultSet rs = ps.executeQuery();
			try {
				ArrayList<ThemeTableLink> links = new ArrayList<ThemeTableLink>();
				while (rs.next()) {
					ThemeTableLink ttl = new ThemeTableLink();
					ttl.id = rs.getInt(TThemeTables.ID);
					ttl.on = rs.getInt(TThemeTables.ONOFF);
					ttl.orderInTheme = rs.getInt(TThemeTables.ORDER);
					ttl.group = rs.getString(TThemeTables.GROUP);
                    ttl.setName(rs.getString(TThemeTables.ALTNAME));
                    try {
                    	ttl.properties = stateFromString(rs.getString(TThemeTables.PROPERTIES));
                    } catch (Exception ex) {
                    	logger.error("Failed to parse ThemeTable (ThemeTable.id="+ttl.id+") properties state!",ex);
                    }
                    ttl.setStyleJS(rs.getString(TThemeTables.STYLEJS));
					ttl.setTheme(t);
					
                    ttl.tableId = rs.getInt(TTables.ID);
					ttl.table = tables.get(ttl.tableId);
					if (ttl.table == null) 
						ttl.table = readTable(ttl.tableId, tables, conn);
					links.add(ttl);
				}
				
				ThemeTableLink[] ttls = new ThemeTableLink[links.size()];
				links.toArray(ttls);
				t.tables = ttls;
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
	}
	
	private Table getTableFromDb(final int tableId, final long minDataTimestamp, final long minMetaTimestamp) throws SQLException
	{
		return dbPoolHolder.executeLocal(new DBExecutor<Table>() {
			public Table execute(Connection conn) throws SQLException {
				conn.setAutoCommit(false);
				int numTries = 8;
				for (int a=0; a<numTries; a++) {
					PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TTables.TBL_NAME+" WHERE "+TTables.ID+"=?");
					try {
						ps.setInt(1, tableId);
						ResultSet rs = ps.executeQuery();
						try {
							if (rs.next()) {
								Table t = new Table();
								fillInRaw(t, rs);
								
								{
									getTranslationService().load(conn, t);
									TranslationServiceImpl.TRANSLATION_TAG();
								}
								
								if (t.lastDataWrite >= minDataTimestamp && t.lastMetaChange >= minMetaTimestamp) {
									Int2ObjectOpenHashMap<Table> tables = new Int2ObjectOpenHashMap<Table>();
									tables.put(t.id, t);
									readFields(t, tables, conn);
									writeLock.lock();
									try {
										return commit(t);
									} finally {
										writeLock.unlock();
									}
								}
							}
						} finally {
							DBUtil.close(rs);
						}
					} finally {
						DBUtil.close(ps);
					}
					
					if (a == 4) {
						// TODO: check with update
					}
					
					if (a != (numTries-1)) {
						try {
							Thread.sleep(50L << a);
						} catch (InterruptedException e) {
							// eh
						}
					}
				}
				
				throw new SQLException("DB sync lasted too long or invalid table id "+tableId);
			}
		});
	}
	
	private Theme commit(Theme t)
	{
		Theme existing = themeIdToTheme.get(t.id);
		if (existing != null && existing.lastMetaChange >= t.lastMetaChange)
			return existing;
		
		themeIdToTheme.put(t.id, t);
		ThemeTableLink[] ttls = t.tables;
		for (int a=0; a<ttls.length; a++) {
			ttls[a].setTable(commit(ttls[a].table));
		}
		return t;
	}
	
	private Table commit(Table t)
	{
		Table existing = tableIdToTable.get(t.id);
		if (existing != null) {
			if (existing.lastMetaChange >= t.lastMetaChange) {
				if (existing.lastDataWrite < t.lastDataWrite)
					existing.lastDataWrite = t.lastDataWrite;
				return existing;
			} else {
				if (existing.lastDataWrite > t.lastDataWrite)
					t.lastDataWrite = existing.lastDataWrite;
			}
		}
		
		tableIdToTable.put(t.id, t);
		Field[] fields = t.fields;
		for (int a=0; a<fields.length; a++) {
			fields[a] = commit(fields[a]);
		}
		return t;
	}
	
	private Field commit(Field f)
	{
		Field existing = fieldIdToField.get(f.id);
		if (existing != null && existing.lastTableMeta >= f.lastTableMeta)
			return existing;
		
		fieldIdToField.put(f.id, f);
		if (f.table != null)
			f.table = commit(f.table);
		
		return f;
	}
	
	private void readFields(Table t, Int2ObjectOpenHashMap<Table> tables, Connection conn) throws SQLException
	{
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TFields.TBL_NAME+" WHERE "+TTables.ID+"=? ORDER BY "+TFields.ORDER+", "+TFields.ID);
		try {
			ps.setInt(1, t.id);
			ResultSet rs = ps.executeQuery();
			try {
				ArrayList<Field> flds = new ArrayList<Field>();
				while (rs.next()) {
					Field f = new Field();
					fillInRaw(f, rs);
					
					{
						getTranslationService().load(conn, f);
						TranslationServiceImpl.TRANSLATION_TAG();
					}
					
					if (f.refdTableId > 0) {
						f.refdTable = tables.get(f.refdTableId);
						if (f.refdTable == null) {
							f.refdTable = readTable(f.refdTableId, tables, conn);
						}
					}
					f.lastTableMeta = t.lastMetaChange;
					f.table = t;
					if (!f.isDeleted()) { 
						flds.add(f);
					}
				}
				Field[] fields = new Field[flds.size()];
				flds.toArray(fields);
				t.fields = fields;
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
	}
	
	private Table readTable(int tableId, Int2ObjectOpenHashMap<Table> tables, Connection conn) throws SQLException
	{
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TTables.TBL_NAME+" WHERE "+TTables.ID+"=?");
		try {
			ps.setInt(1, tableId);
			ResultSet rs = ps.executeQuery();
			try {
				if (rs.next()) {
					Table t = new Table();
					fillInRaw(t, rs);
					
					{
						getTranslationService().load(conn, t);
						TranslationServiceImpl.TRANSLATION_TAG();
					}
					
					tables.put(t.id, t);
					readFields(t, tables, conn);
					return t;
				} else {
					throw new IllegalStateException("Missing table "+tableId);
				}
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
	}
	
	public void refresh() throws SQLException
	{
		dbPoolHolder.executeLocal(new DBExecutor<Void>() {
			public Void execute(Connection conn) throws SQLException {
				Throwable exc = null;
				try {
					updateTables(conn);
				} catch (Throwable t) {
					exc = t;
					t.printStackTrace();
				}
				try {
					updateThemes(conn);
				} catch (Throwable t) {
					if (exc==null) exc = t;
					t.printStackTrace();
				}
				if (exc!=null) {
					SQLException se = new SQLException("Metadata Refresh Failed");
					se.initCause(exc);
					throw se;
				}
				return null;
			}
		});
	}

	private void updateThemes(Connection conn) throws SQLException {
		Exception firstEx = null;
		int cntEx = 0;
		PreparedStatement ps = conn.prepareStatement("SELECT " + TThemes.ID + "," + TThemes.LASTMETACHANGE + " FROM " + TThemes.TBL_NAME);
		try {
			ResultSet rs = ps.executeQuery();
			try {
				while (rs.next()) {
					int themeId;
					long minMetaTimestamp;
					
					themeId = rs.getInt(TThemes.ID);
					minMetaTimestamp = rs.getLong(TThemes.LASTMETACHANGE);

					//getThemeByIdMeta returns the original theme if timestamp hasn't changed
					//or the new theme if the timestamp has changed.
					try {
						getThemeByIdMeta(themeId, minMetaTimestamp);
					} catch (SQLException e) {
						cntEx++;
						if (firstEx==null) {
							firstEx = e;
						}
						e.printStackTrace();
					}
				}
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
		if (firstEx!=null) {
			SQLException se = new SQLException("Theme Metadata Refresh Failed "+cntEx+" Times. Throwing first exception.");
			se.initCause(firstEx);
			throw se;
		}
	}

	private void updateTables(Connection conn) throws SQLException {
		Exception firstEx = null;
		int cntEx = 0;
		PreparedStatement ps = conn.prepareStatement("SELECT " + TTables.LASTMETACHANGE + "," + TTables.ID + " FROM " + TTables.TBL_NAME+" WHERE "+TTables.DELETED+"=0");
		try {
			ResultSet rs = ps.executeQuery();
			try {
				while (rs.next()) {
					int tableId;
					long minMetaTimestamp;
					
					tableId = rs.getInt(TTables.ID);
					minMetaTimestamp = rs.getLong(TTables.LASTMETACHANGE);

					try {
						//getTableByIdMeta returns the original table if timestamp hasn't changed
						//or the new table if the timestamp has changed.
						long ts = System.currentTimeMillis();
						getTableByIdMeta(tableId, minMetaTimestamp);
//						System.out.println(System.currentTimeMillis()-ts);
					} catch (Exception e) {
						cntEx++;
						if (firstEx==null) {
							firstEx = e;
						}
						e.printStackTrace();
					}
				}
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
		if (firstEx!=null) {
			SQLException se = new SQLException("Table Metadata Refresh Failed "+cntEx+" Times. Throwing first exception.");
			se.initCause(firstEx);
			throw se;
		}
	}
	
	void initialLoad() throws SQLException
	{
		dbPoolHolder.executeLocal(new DBExecutor<Void>() {
			public Void execute(Connection conn) throws SQLException {
				
				conn.setAutoCommit(false);
				
				Theme[] themes = initialReadThemes(conn);
				Table[] tables = initialReadTables(conn);
				Field[] fields = initialReadFields(conn);
				
				themeIdToTheme.clear();
				for (int a=0; a<themes.length; a++) {
					themeIdToTheme.put(themes[a].id, themes[a]);
					themes[a].tables = ThemeTableLink.emptyArray; // so it's set everywhere
				}
				tableIdToTable.clear();
				for (int a=0; a<tables.length; a++) {
					tableIdToTable.put(tables[a].id, tables[a]);
				}
				fieldIdToField.clear();
				for (int a=0; a<fields.length; a++)
					fieldIdToField.put(fields[a].id, fields[a]);
				
				initialAssignFieldsToTables(fields);
				
				initialReadThemeTables(conn);
				getTranslationService().initialLoad(conn);
				
				return null;
			}
		});
	}

	private void initialReadThemeTables(Connection conn) throws SQLException
    {
	    PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TThemeTables.TBL_NAME+" ORDER BY "+TThemes.ID+", "+TThemeTables.ORDER+", "+TTables.ID);
	    try {
	    	ResultSet rs = ps.executeQuery();
	    	try {
	    		ArrayList<ThemeTableLink> zisOne = new ArrayList<ThemeTableLink>();
	    		Theme current = null;
	    		while (rs.next()) {
	    			int themetable_id = rs.getInt(TThemeTables.ID);
	    			int theme_id = rs.getInt(TThemes.ID);
	    			int table_id = rs.getInt(TTables.ID);
	    			int order = rs.getInt(TThemeTables.ORDER);
	    			int onoff = rs.getInt(TThemeTables.ONOFF);
	    			String group = rs.getString(TThemeTables.GROUP);
                    String altName = rs.getString(TThemeTables.ALTNAME);
                    StateGWT props = null;
                    try {
                    	props = stateFromString(rs.getString(TThemeTables.PROPERTIES));
                    } catch (Exception ex) {
                    	logger.error("Failed to parse ThemeTable (ThemeTable.id="+themetable_id+") properties state!",ex);
                    }

	    			String styleJS = rs.getString(TThemeTables.STYLEJS);
	    			
	    			if (current != null && current.id != theme_id) {
	    				if (zisOne.size() > 0) {
	    					ThemeTableLink[] ttls = new ThemeTableLink[zisOne.size()];
	    					zisOne.toArray(ttls);
	    					current.tables = ttls;
	    					zisOne.clear();
	    				}
	    				current = null;
	    			}
	    			if (current == null) {
	    				current = themeIdToTheme.get(theme_id);
	    				if (current == null) {
	    					System.err.println("Invalid TTL: theme = "+theme_id+"   table = "+table_id);
	    					continue;
	    				}
	    			}
	    			Table t = tableIdToTable.get(table_id);
	    			if (t == null) {
	    				System.err.println("Invalid TTL: theme = "+theme_id+"   table = "+table_id);
	    				continue;
	    			}
	    			ThemeTableLink ttl = new ThemeTableLink();
	    			
	    			ttl.id = themetable_id;
	    			ttl.on = onoff;
	    			ttl.orderInTheme = order;
	    			ttl.setStyleJS(styleJS);

	    			ttl.group = group;
                    ttl.setName(altName);
                    ttl.properties = props;
	    			ttl.setTable(t);
	    			ttl.setTheme(current);
	    			
	    			zisOne.add(ttl);
	    		}
	    		
	    		if (current != null) {
	    			if (zisOne.size() > 0) {
	    				ThemeTableLink[] ttls = new ThemeTableLink[zisOne.size()];
	    				zisOne.toArray(ttls);
	    				current.tables = ttls;
	    				zisOne.clear();
	    			}
	    		}
	    	} finally {
	    		DBUtil.close(rs);
	    	}
	    } finally {
	    	DBUtil.close(ps);
	    }
    }

	private void initialAssignFieldsToTables(Field[] fields)
    {
	    sortByTableOrderId(fields);
	    
	    int pos = 0;
	    while (pos < fields.length) {
	    	int len = 1;
	    	int tableId = fields[pos].tableId;
	    	while (pos + len < fields.length && fields[pos+len].tableId == tableId)
	    		len++;
	    	
	    	Field[] fs = new Field[len];
	    	System.arraycopy(fields, pos, fs, 0, len);
	    	Table t = tableIdToTable.get(tableId);
	    	if (t != null) {
	    		t.fields = fs;
	    		for (int q=0; q<len; q++) {
	    			fs[q].table = t;
	    			fs[q].lastTableMeta = t.lastMetaChange;
	    		}
	    	} else {
	    		// fields without table??
	    		for (int a=0; a < len; a++) {
	    			logger.warn("Field "+fields[pos+a].id+" is without table "+tableId);
	    			fieldIdToField.remove(fields[pos+a].id);
	    		}
	    	}
	    	pos += len;
	    }
	    
	    for (int a=0; a<fields.length; a++) {
	    	if (fields[a].refdTableId > 0) {
	    		fields[a].refdTable = tableIdToTable.get(fields[a].refdTableId);
	    		if (fields[a].refdTable == null)
	    			logger.warn("Missing refd table "+fields[a].refdTableId+" in field "+fields[a].id);
	    	}
	    }
    }

	private void sortByTableOrderId(Field[] fields)
    {
	    Arrays.sort(fields, new Comparator<Field>() {
	    	public int compare(Field f1, Field f2)
	    	{
	    		if (f1.tableId < f2.tableId) return -1;
	    		if (f1.tableId > f2.tableId) return 1;
	    		if (f1.order < f2.order) return -1;
	    		if (f1.order > f2.order) return 1;
	    		if (f1.id < f2.id) return -1;
	    		if (f1.id > f2.id) return 1;
	    		return 0;
	    	}
	    });
    }
	
	private Field[] initialReadFields(Connection conn) throws SQLException
    {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TFields.TBL_NAME);
		try {
			ResultSet rs = ps.executeQuery();
			try {
				ArrayList<Field> res = new ArrayList<Field>();
				
				while (rs.next()) {
					Field f = new Field();
					fillInRaw(f, rs);
					
					{
						TranslationServiceImpl.TRANSLATION_TAG();
						// do nothing
					}
					
					if (!f.isDeleted()) { 
						res.add(f);
					}
				}
				
				return res.toArray(new Field[res.size()]);
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
    }

	private Table[] initialReadTables(Connection conn) throws SQLException
    {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TTables.TBL_NAME);
		try {
			ResultSet rs = ps.executeQuery();
			try {
				ArrayList<Table> res = new ArrayList<Table>();
				
				while (rs.next()) {
					Table t = new Table();
					fillInRaw(t, rs);
					
					{
						TranslationServiceImpl.TRANSLATION_TAG();
						// do nothing
					}
					
					t.fields = Field.emptyArray;
					
					if (!t.isDeleted()) { // skip deleted tables
						res.add(t);
					}
				}
				
				return res.toArray(new Table[res.size()]);
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
    }
	
	private Theme[] initialReadThemes(Connection conn) throws SQLException
    {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TThemes.TBL_NAME);
		try {
			ResultSet rs = ps.executeQuery();
			try {
				ArrayList<Theme> res = new ArrayList<Theme>();
				
				while (rs.next()) {
					Theme t = new Theme();
					fillInRaw(t, rs);
					
					{
						TranslationServiceImpl.TRANSLATION_TAG();
						// do nothing
					}
					
					res.add(t);
				}
				
				return res.toArray(new Theme[res.size()]);
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
    }

	public void fillInRaw(Field f, ResultSet rs) throws SQLException
	{
		f.defaultValueString = rs.getString(TFields.DEFAULTVALUE);
		f.descRawHtml = rs.getString(TFields.DESC);
		f.setFlags( rs.getInt(TFields.FLAGS));
		f.id = rs.getInt(TFields.ID);
		f.setName(rs.getString(TFields.NAME));
		f.order = rs.getInt(TFields.ORDER);
		f.refdTableId = rs.getInt(TFields.REFD_TABLE_ID);
		f.tableId = rs.getInt(TTables.ID);
		f.type = Field.FieldType.forId(rs.getInt(TFields.TYPE));
		f.sysId = rs.getString(TFields.SYSID);
		try {
			f.properties = stateFromString(rs.getString(TFields.PROPERTIES));
		} catch (Exception ex) {
			logger.error("Failed to parse Field (Field.id=" + f.id + ") properties state!", ex);
		}
		
		{
			getTranslationService().commitFieldLanguages(f.id, rs.getString(TFields.LANGUAGES));//TODO:drejmar
			TranslationServiceImpl.TRANSLATION_TAG();
		}
		
		MetaServiceImpl.createDispHTML(f);
	}
	
	public void fillInRaw(Table t, ResultSet rs) throws SQLException
    {
		t.descRawHtml = rs.getString(TTables.DESC);
		t.geomType = GeomType.forId(rs.getInt(TTables.GEOMTYPE));
		t.id = rs.getInt(TTables.ID);
		t.lastDataWrite = rs.getLong(TTables.LASTDATAWRITE);
		t.lastMetaChange = rs.getLong(TTables.LASTMETACHANGE);
		t.setName(rs.getString(TTables.NAME));
		t.styleJS = rs.getString(TTables.STYLEJS);
		t.public_perms = rs.getInt(TTables.PUBLICPERMS);
		t.setRepText(rs.getString(TTables.REPTEXTJS));
		t.sysId = rs.getString(TTables.SYSID);
        t.setDeleted(rs.getInt(TTables.DELETED)!=0);
        try {
        	t.properties = stateFromString(rs.getString(TTables.PROPERTIES));
        } catch (Exception ex) {
        	logger.error("Failed to parse Table (Table.id="+t.id+") properties state!",ex);
        }
        Double minx= rs.getDouble(TTables.MINX);
        Double miny= rs.getDouble(TTables.MINY);
        Double maxx= rs.getDouble(TTables.MAXX);
        Double maxy= rs.getDouble(TTables.MAXY);
        if (minx!=null && miny!=null && maxx!=null && maxy!=null) {
        	t.envelope = new Envelope(minx,miny,maxx,maxy);
        }
        if (t.properties==null) {
        	logger.debug("Table "+t.id+" Properties are null!");
        	
        }
        {
        	getTranslationService().commitTableLanguages(t.id, rs.getString(TTables.LANGUAGES));//TODO:drejmar
        	TranslationServiceImpl.TRANSLATION_TAG();
		}
        
		MetaServiceImpl.createDispHTML(t);
    }

    /**
     * @param rs
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private static StateGWT stateFromString(String str) throws Exception {
        if (str==null || str.length()<4) return null;
       	return StateUtilJava.gwtFromJavaString(str);
    }
	
	public void fillInRaw(Theme t, ResultSet rs) throws SQLException
    {
		t.descRawHtml = rs.getString(TThemes.DESC);
		t.id = rs.getInt(TThemes.ID);
		t.lastMetaChange = rs.getLong(TThemes.LASTMETACHANGE);
		t.setName(rs.getString(TThemes.NAME));
		t.public_perms = rs.getInt(TThemes.PUBLICPERMS);
		t.baseLayers = ConfigurationUtils.themeDatasetsFromString(rs.getString(TThemes.DATASETS));
		try {
			t.properties = stateFromString(rs.getString(TThemes.PROPERTIES));
		 } catch (Exception ex) {
         	logger.error("Failed to parse Theme (Theme.id="+t.id+") properties state!",ex);
         }
		
		{
			getTranslationService().commitThemeLanguages(t.id, rs.getString(TThemes.LANGUAGES));//TODO:drejmar
			TranslationServiceImpl.TRANSLATION_TAG();
		}
		
		MetaServiceImpl.createDispHTML(t);
    }

	public Table[] findTables(Predicate<Table> filter)
    {
		ArrayList<Table> out = new ArrayList<Table>();
		
		readLock.lock();
		try {
			for (Table t : tableIdToTable.values())
				if (filter.eval(t))
					out.add(t);
		} finally {
			readLock.unlock();
		}
		
		return out.toArray(new Table[out.size()]);
    }
	
	public Theme[] findThemes(Predicate<Theme> filter)
    {
		ArrayList<Theme> out = new ArrayList<Theme>();
		
		readLock.lock();
		try {
			for (Theme t : themeIdToTheme.values()) {
				if (filter.eval(t)) {
					out.add(t);
				}
			}
		} finally {
			readLock.unlock();
		}
		
		return out.toArray(new Theme[out.size()]);
    }
	
	/**
	 * @param fieldId
	 * @return
	 * @deprecated use getFieldById(int tableId, int fieldId) to get non-null results even when data isn't precached
	 */
	@Deprecated
	public Field getFieldById(int fieldId)
	{
		if (fieldId < 1) {
			throw new IllegalArgumentException();
		}
		readLock.lock();
		try {
			return fieldIdToField.get(fieldId);
		} finally {
			readLock.unlock();
		}
	}
	
	public Field getFieldById(int tableId, int fieldId) throws SQLException
	{
		if (fieldId < 1) {
			throw new IllegalArgumentException();
		}
		Field ret;
		readLock.lock();
		try {
			ret = fieldIdToField.get(fieldId);
		} finally {
			readLock.unlock();
		}
		if (ret != null) {
			return ret;
		}
		return getTableById(tableId).getFieldById(fieldId);
	}
	
	
	public void deleteField(int fieldId) throws SQLException {
		if (fieldIdToField.containsKey(fieldId)) {
			Field field = fieldIdToField.get(fieldId);
			if (field!=null) {
				Table table = getTableFromDb(field.tableId, Long.MIN_VALUE, Long.MIN_VALUE); // TODO:drejmar:this commits
				if (table!=null && table.getFieldById(field.id)!=null) {
					field = table.getFieldById(fieldId);
					if (field!=null && field.isDeleted()) {
						fieldIdToField.remove(field.id);
					}
				}
			}
			fieldIdToField.remove(fieldId); // TODO:drejmar:make sure field is deleted from metadata.
		}
	}
	
	public void deleteTable(int tableId) throws SQLException {
		if (tableIdToTable.containsKey(tableId)) {
			Table table = getTableFromDb(tableId, Long.MIN_VALUE, Long.MIN_VALUE);
			if (table!=null && table.isDeleted()) {
				tableIdToTable.remove(table.id);
			}
			tableIdToTable.remove(tableId); // TODO:drejmar:make sure table is deleted from metadata.
		}
	}
	
	public void deleteTheme(int themeId) throws SQLException {
		if (themeIdToTheme.containsKey(themeId)) {
//			Theme theme = getThemeFromDb(themeId, Long.MIN_VALUE); // TODO:drejmar: DB sync lasted too long
//			if (theme==null) {
//				themeIdToTheme.remove(themeId);
//			}
			themeIdToTheme.remove(themeId); // TODO:drejmar:make sure theme is deleted from metadata.
		}
	}
	
//	public void display() {
//		String display = "";
//		for (Table layer : tableIdToTable.values()) {
//			if (layer!=null) {
//				display+="Layer " + layer.id + "."+layer.name;
//				if (layer.deleted) {
//					display += " deleted";
//				}
//				display += "\n";
//			}
//		}
//		for (Field field : fieldIdToField.values()) {
//			if (field!=null) {
//				display += "Field " + field.id + "."+field.name;
//				if (field.isHidden()) {
//					display += " hidden";
//				}
//				if (field.isDeleted()) {
//					display += " deleted";
//				}
//				if (field.isPermenantlyDeleted()) {
//					display += " permanently deleted";
//				}
//				display += "\n";
//			}
//		}
//	}
}
