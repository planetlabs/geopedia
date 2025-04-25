package com.sinergise.geopedia.db;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.outerj.daisy.htmlcleaner.HtmlCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.ConfigurationUtils;
import com.sinergise.geopedia.app.UserAccessControl;
import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.constants.Globals.PersonalGroup;
import com.sinergise.geopedia.core.entities.Category;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldFlags;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Image;
import com.sinergise.geopedia.core.entities.News;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.entities.User.UserUpdater;
import com.sinergise.geopedia.core.entities.UserPermissions;
import com.sinergise.geopedia.core.entities.WidgetInfo;
import com.sinergise.geopedia.core.entities.properties.BinaryFileProperty;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.entities.properties.HTMLProperty;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;
import com.sinergise.geopedia.core.entities.utils.EntityConsts.DataScope;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.exceptions.QueryException;
import com.sinergise.geopedia.core.exceptions.TableDataException;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.Query.Options;
import com.sinergise.geopedia.core.query.filter.FieldDescriptor;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor;
import com.sinergise.geopedia.core.symbology.AbstractSymbologyUtils;
import com.sinergise.geopedia.core.symbology.PointSymbolizer;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.db.TableAndFieldNames.FeaturesTable;
import com.sinergise.geopedia.db.TableAndFieldNames.TCategories;
import com.sinergise.geopedia.db.TableAndFieldNames.TCategoryTables;
import com.sinergise.geopedia.db.TableAndFieldNames.TCounter;
import com.sinergise.geopedia.db.TableAndFieldNames.TFields;
import com.sinergise.geopedia.db.TableAndFieldNames.TGroups;
import com.sinergise.geopedia.db.TableAndFieldNames.TImages;
import com.sinergise.geopedia.db.TableAndFieldNames.TNews;
import com.sinergise.geopedia.db.TableAndFieldNames.TPermissions;
import com.sinergise.geopedia.db.TableAndFieldNames.TSessions;
import com.sinergise.geopedia.db.TableAndFieldNames.TSymbols;
import com.sinergise.geopedia.db.TableAndFieldNames.TTables;
import com.sinergise.geopedia.db.TableAndFieldNames.TThemeCategories;
import com.sinergise.geopedia.db.TableAndFieldNames.TThemeTables;
import com.sinergise.geopedia.db.TableAndFieldNames.TThemes;
import com.sinergise.geopedia.db.TableAndFieldNames.TUserGroups;
import com.sinergise.geopedia.db.TableAndFieldNames.TUserTables;
import com.sinergise.geopedia.db.TableAndFieldNames.TUserThemes;
import com.sinergise.geopedia.db.TableAndFieldNames.TUsers;
import com.sinergise.geopedia.db.TableAndFieldNames.TWidgets;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.db.expressions.QueryBuilderNew;
import com.sinergise.geopedia.db.expressions.QueryBuilderNew.ConditionBuilder;
import com.sinergise.geopedia.db.expressions.QueryField;
import com.sinergise.geopedia.db.expressions.ReptextEvaluator;
import com.sinergise.geopedia.db.geometry.WkbReader;
import com.sinergise.geopedia.db.util.DatabaseStructures;
import com.sinergise.geopedia.db.util.Update;
import com.sinergise.geopedia.db.util.UpdateConditions;
import com.sinergise.geopedia.db.util.ValueMod;
import com.sinergise.geopedia.geometry.height.Heights;
import com.sinergise.geopedia.geometry.util.CentroidFinder;
import com.sinergise.geopedia.query.ExpressionDescriptorTransformer;
import com.sinergise.geopedia.server.service.FeatureServiceImpl;
import com.sinergise.geopedia.server.service.FileUploadServiceImpl;
import com.sinergise.geopedia.server.service.MetaServiceImpl;
import com.sinergise.geopedia.style.symbology.rhino.JavaSymbologyUtils;
import com.sinergise.geopedia.util.GeopediaServerUtility;
import com.sinergise.java.util.sql.LoggableStatement;
import com.sinergise.java.util.state.StateHelper;
import com.sinergise.java.util.state.StateUtilJava;
import com.sinergise.util.MD5;


public class DB {
	
	private DBPoolHolder dbPoolHolder;
	private MetaData meta;
	
	private static final Logger logger = LoggerFactory.getLogger(DB.class);
	public DB(DBPoolHolder dbPoolHolder, MetaData meta) { 
		this.dbPoolHolder = dbPoolHolder;
		this.meta = meta;
	}
	
	
	private TranslationServiceImpl getTranslationService() {
		return meta.getTranslationService();
	}
	
	public MetaData getMetaProvider() {
		return meta;
	}
	
	
	
	static void touchThemeMeta(Connection conn, int themeId, long timestamp) throws SQLException {
		LoggableStatement ps = new LoggableStatement(conn, "UPDATE " + TThemes.TBL_NAME + " SET "
			+ TThemes.LASTMETACHANGE + "=? WHERE " + TThemes.ID + "=? AND " + TThemes.LASTMETACHANGE + "<?");
		try {
			ps.setLong(1, timestamp);
			ps.setInt(2, themeId);
			ps.setLong(3, timestamp);
			ps.executeUpdate();
		} finally {
			DBUtil.close(ps);
		}
	}
	
	public void touchTableData(Connection conn, int tableId, long timestamp)
			throws SQLException {
		LoggableStatement ps =  new LoggableStatement(conn, "UPDATE " + TTables.TBL_NAME + " SET " + TTables.LASTDATAWRITE
			+ "=? , " + getLastTableDataWriteDateSQL() + " WHERE " + TTables.ID + "=? AND " + TTables.LASTDATAWRITE + "<?");
		try {
			ps.setLong(1, timestamp);
			ps.setInt(2, tableId);
			ps.setLong(3, timestamp);
			ps.executeUpdate();
		} finally {
			DBUtil.close(ps);
		}
	}
	
	public void touchTableMeta(Connection conn, int tableId, long timestamp) throws SQLException {
		LoggableStatement ls =  new LoggableStatement (conn, "UPDATE " + TTables.TBL_NAME + " SET " + TTables.LASTDATAWRITE
			+ "=? , " + getLastTableDataWriteDateSQL() + " , " + TTables.LASTMETACHANGE + "=? WHERE " + TTables.ID + "=? AND " + TTables.LASTMETACHANGE
			+ "<?");
		int numUpdated = -1;
		try {
			ls.setLong(1, timestamp);
			ls.setLong(2, timestamp);
			ls.setInt(3, tableId);
			ls.setLong(4, timestamp);
			numUpdated = ls.executeUpdate();
		} finally {
			DBUtil.close(ls);
		}
		if (numUpdated <= 0) return;
		
		ls = new LoggableStatement(conn, "SELECT " + TTables.ID + " FROM " + TFields.TBL_NAME
			+ " WHERE " + TFields.REFD_TABLE_ID + "=?");
		try {
			ls.setInt(1, tableId);
			ResultSet rs = ls.executeQuery();
			try {
				while (rs.next())
					touchTableMeta(conn, rs.getInt(1), timestamp);
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ls);
		}
		ls  = new LoggableStatement(conn, "SELECT " + TThemes.ID + " FROM " + TThemeTables.TBL_NAME + " WHERE " + TTables.ID
			+ "=?");
		try {
			ls.setInt(1, tableId);
			ResultSet rs = ls.executeQuery();
			try {
				while (rs.next())
					touchThemeMeta(conn, rs.getInt(1), timestamp);
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ls);
		}
		
	}
	
		
	public void setPermissions(final GeopediaEntity objType, final int objId, final int userGroup,
			final int userGroupId, final int level) throws SQLException {
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			public Void execute(Connection conn) throws SQLException {
				setPermissions(objType, objId, userGroup, userGroupId, level,conn);
				return null;
			}
		});
	}
	
	public long getVersionStamp() throws SQLException {
		return dbPoolHolder.executeUpdate(new DBExecutor<Long>() {
			public Long execute(Connection conn) throws SQLException {
				Statement s = conn.createStatement();
				try {
					s.execute("LOCK TABLES " + TCounter.TBL_NAME + " WRITE");
					try {
						s.execute("UPDATE " + TCounter.TBL_NAME + " SET " + TCounter.CURRVAL + "=" + TCounter.CURRVAL + "+1");
						ResultSet rs = s.executeQuery("SELECT " + TCounter.CURRVAL + " FROM " + TCounter.TBL_NAME);
						try {
							if (rs.next()) {
								return rs.getLong(1);
							} else {
								throw new IllegalStateException("Missing counter");
							}
						} finally {
							DBUtil.close(rs);
						}
					} finally {
						try {
							s.execute("UNLOCK TABLES");
						} catch (SQLException e) {
							DBUtil.close(conn);
						}
					}
				} finally {
					DBUtil.close(s);
				}
			}
		});
	}
	
	private UserUpdater uUpdater = new UserUpdater() {
		
		@Override
		public void updateUser(User user, SGAsyncCallback<User> callback) {
			try {
				user.setPermissions(loadUserPermissions(user.getId()));
				if (callback!=null) {
					callback.onSuccess(user);
					return;
				}
			} catch (SQLException e) {
				logger.error("Database error!",e);
				if (callback!=null) {
					callback.onFailure(e);
					return;
				}
			}
		}
	};
	private User readUser(ResultSet rs, Connection conn) throws SQLException {
		int id = rs.getInt(TUsers.ID);
		String login = rs.getString(TUsers.LOGIN);
		String email = rs.getString(TUsers.EMAIL);
		String fullName = rs.getString(TUsers.FULLNAME);
		String org = rs.getString(TUsers.ORGANISATION);
		boolean isAdmin = 0 != rs.getInt(TUsers.ISADMIN);
		
		User u =  new User(id, login, email, fullName, org, isAdmin, loadUserPermissions(id,conn));
		u.setUserUpdater(uUpdater);
		return u;
	}
	
	public User getUser(final int userId) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<User>() {
			public User execute(Connection conn) throws SQLException {
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + TUsers.TBL_NAME + " WHERE "
					+ TUsers.ID + "=?");
				try {
					ps.setInt(1,userId);
					ResultSet rs = ps.executeQuery();
					try {
						if (rs.next()) {
							return readUser(rs,conn);
						} else {
							return null;
						}
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	public User getUser(final String username, final String password) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<User>() {
			public User execute(Connection conn) throws SQLException {
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + TUsers.TBL_NAME + " WHERE "
					+ TUsers.LOGIN + "=? AND " + TUsers.PASSMD5 + "=?");
				try {
					ps.setString(1, username);
					ps.setString(2, MD5.hash32(password)); //TODO:verify MD5
					ResultSet rs = ps.executeQuery();
					try {
						if (rs.next()) {
							return readUser(rs,conn);
						} else {
							return null;
						}
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	
	/**
	 * @deprecated used only with PermanentCookie
	 */
	public User getUser(final String username) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<User>() {
			public User execute(Connection conn) throws SQLException {
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + TUsers.TBL_NAME + " WHERE "
					+ TUsers.LOGIN + "=?");
				try {
					ps.setString(1, username);
					ResultSet rs = ps.executeQuery();
					try {
						if (rs.next()) {
							int id = rs.getInt(TUsers.ID);
							String login = rs.getString(TUsers.LOGIN);
							String email = rs.getString(TUsers.EMAIL);
							String fullName = rs.getString(TUsers.FULLNAME);
							String org = rs.getString(TUsers.ORGANISATION);
							boolean isAdmin = 0 != rs.getInt(TUsers.ISADMIN);
							
							return new User(id, login, email, fullName, org, isAdmin, loadUserPermissions(id,
								conn));
						} else {
							return null;
						}
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	
		
	
	/**
	 * @param f
	 * @return
	 */
	private static String stateToString(StateGWT st) {
		if (st == null || st.isEmpty()) return null;
		String stateStr = null;
		try {
			stateStr =StateUtilJava.javaStringFromGWT(st);
		} catch (Exception e) {
			e.printStackTrace();
			// Do nothing - state will get lost
		}
		return stateStr;
	}
	
	
	
	@Deprecated
	public int createImage(final int user_id, final String mime, final int width,
			final int height, final byte[] bytes, final String md5) throws SQLException {
		// TODO: check for matching md5?
		return dbPoolHolder.executeUpdate(new DBExecutor<Integer>() {
			@Override
			public Integer execute(Connection conn) throws SQLException {
				return Integer.valueOf(Update.insert(TImages.TBL_NAME).set(TUsers.ID, user_id).set(TImages.MIME, mime).set(TImages.WIDTH,
					width).set(TImages.HEIGHT, height).set(TImages.BYTES, bytes).set(TImages.BYTESMD5, md5)
					.execute(conn));
			}
		}).intValue();
	}
	
	public Image getImage(final int img_id, final boolean includeBytes) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<Image>() {
			@Override
			public Image execute(Connection conn) throws SQLException {
				PreparedStatement ps;
				if (includeBytes) {
					ps = conn.prepareStatement("SELECT * FROM " + TImages.TBL_NAME + " WHERE " + TImages.ID + "=?");
				} else {
					ps = conn.prepareStatement("SELECT " + TImages.ID + ", " + TImages.HEIGHT + ", " + TImages.MIME
						+ ", " + TImages.UPLOADED + ", " + TUsers.ID + ", " + TImages.WIDTH + " FROM " + TImages.TBL_NAME
						+ " WHERE " + TImages.ID + "=?");
				}
				try {
					ps.setInt(1, img_id);
					for (int t = 0; t < 2; t++) {
						ResultSet rs = ps.executeQuery();
						try {
							if (rs.next()) {
								Image img = new Image();
								
								img.blob_id = rs.getInt(TImages.ID);
								img.height = rs.getInt(TImages.HEIGHT);
								img.mime = rs.getString(TImages.MIME);
								img.uploadedTime = rs.getTimestamp(TImages.UPLOADED).getTime();
								img.user_id = rs.getInt(TUsers.ID);
								img.width = rs.getInt(TImages.WIDTH);
								
								if (includeBytes) {
									img.data = rs.getBytes(TImages.BYTES);
									img.datamd5 = rs.getString(TImages.BYTESMD5);
								}
								
								return img;
							}
						} finally {
							DBUtil.close(rs);
						}
						
						if (t == 0) {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								// ignore
							}
						}
					}
					throw new SQLException("DB Sync lasted too long");
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	

	
	public void addThemeToCat(final int catId, final int themeId) throws SQLException {
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			public Void execute(Connection conn) throws SQLException {
				Update.replace(TThemeCategories.TBL_NAME).set(TThemeCategories.CATEGORY_ID, catId).set(TThemeCategories.THEME_ID, themeId).execute(conn);
				return null;
			}
		});
	}
	
	public void removeThemeFromCat(final int catId, final int themeId) throws SQLException {
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			public Void execute(Connection conn) throws SQLException {
				Update.delete(TThemeCategories.TBL_NAME).where(TThemeCategories.THEME_ID, themeId).where(TThemeCategories.CATEGORY_ID, catId).execute(conn);
				return null;
			}
		});
	}
	
	
		
	public void addSessionState(final String sid, final String session) throws SQLException {
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			
			public Void execute(Connection conn) throws SQLException {
				Update.insert(TSessions.TBL_NAME).set(TSessions.ID, sid).set(TSessions.STATE, session).execute(conn);
				return null;
			}
		});
	}
	
	public void updateSessionState(final String sid, final String session) throws SQLException {
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			
			public Void execute(Connection conn) throws SQLException {
				Update.update(TSessions.TBL_NAME).where(TSessions.ID, sid).set(TSessions.STATE, session).execute(conn);
				return null;
			}
		});
	}
	
	public void deleteSession(final String sid) throws SQLException {
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			
			public Void execute(Connection conn) throws SQLException {
				Update.delete(TSessions.TBL_NAME).where(TSessions.ID, sid).execute(conn);
				return null;
			}
		});
	}
	
	
	public Session loadSession(final String sessionID, final long maxAge) throws SQLException{
		return dbPoolHolder.executeLocal(new DBExecutor<Session>() {
			@Override
			public Session execute(Connection conn) throws SQLException {
				ResultSet rs = null;
				LoggableStatement ls = null;
				
				try {
					Timestamp maxAgeTS = new Timestamp(maxAge);
					ls = new LoggableStatement(conn,
					"SELECT * FROM " + TSessions.TBL_NAME + " WHERE " + TSessions.ID + "=? "
					+ " AND "+TSessions.LASTUPDATED+">=?", true);
					ls.setString(1, sessionID);
					ls.setTimestamp(2, maxAgeTS);
					rs = ls.executeQuery();
					if (rs.next()) {
						String sessionState = rs.getString(TSessions.STATE);
						Timestamp ts = rs.getTimestamp(TSessions.LASTUPDATED);						
						if (sessionState==null) // nonexisting session
							return null;
						return Session.createFromString(sessionID, sessionState, ts.getTime(), DB.this);						
					} else {
						return null;
					}
				} finally {
					DBUtil.close(rs);
					DBUtil.close(ls);
					
				}
			}
			
		});
	}
	private static News fillInRaw (News n, ResultSet rs) throws SQLException {
		n.id = rs.getInt(TNews.ID);
		n.data = rs.getString(TNews.DATA);
		n.title = rs.getString(TNews.TITLE);
		n.date = rs.getDate(TNews.DATE);
		return n;
	}

	
	public News[] getNews(final Date date, final int count, final boolean next) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<News[]>() {

			@Override
			public News[] execute(Connection conn) throws SQLException {
				String querySQL = "SELECT * FROM "+TNews.TBL_NAME;
				
				if (date!=null) {
					querySQL+=" WHERE "+TNews.DATE+(next?">":"<");
				}
				querySQL+= " ORDER BY "+TNews.DATE;
				if (next) 
					querySQL+=" ASC ";
				else
					querySQL+=" DESC ";

				querySQL+=" LIMIT ?";

												
				LoggableStatement ls = null;
				ResultSet rs =  null;
				try {
					ls = new LoggableStatement(conn, querySQL);
					int param=1;
					if (date!=null) {
						java.sql.Date sqlDate = new java.sql.Date(date.getTime());
						ls.setDate(1, sqlDate);
						param++;
					}
					ls.setInt(param, count);
					rs = ls.executeQuery();
					if (rs.next()) {
						ArrayList<News> out = new ArrayList<News>();
						
						do {
							out.add(fillInRaw(new News(), rs));
						} while (rs.next());
						
						return out.toArray(new News[out.size()]);
					} else {
						return null;
					}
				} finally {
					DBUtil.close(rs);
					DBUtil.close(ls);

				}
			}
			
		});
	}
		
		
	public Table[] queryTablesFulltext(final String query) throws SQLException {
		
		return dbPoolHolder.executeLocal(new DBExecutor<Table[]>() {
			public Table[] execute(Connection conn) throws SQLException {
				PreparedStatement ps = conn.prepareStatement("SELECT " + TTables.ID + ", "
					+ TTables.LASTDATAWRITE + " FROM " + TTables.TBL_NAME + " WHERE MATCH(" + TTables.NAME + ", "
					+ TTables.DESC + ") AGAINST (? IN BOOLEAN MODE) ORDER BY " + TTables.NAME);
				try {
					ps.setString(1, query);
					ResultSet rs = ps.executeQuery();
					try {
						ArrayList<Table> res = new ArrayList<Table>();
						while (rs.next()) {
							int tableId = rs.getInt(1);
							long tableData = rs.getLong(2);
							Table tbl = meta.getTableByIdData(tableId, tableData);
							if (tbl != null && !tbl.isDeleted()) {
								res.add(tbl);
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
		});
	}
	
	public Image[] getImages(final int userId, final int skip, final int limit)
			throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<Image[]>() {
			public Image[] execute(Connection conn) throws SQLException {
				PreparedStatement ps = conn
					.prepareStatement("SELECT " + TImages.ID + ", " + TImages.HEIGHT + ", " + TImages.MIME + ", "
						+ TImages.UPLOADED + ", " + TUsers.ID + ", " + TImages.WIDTH + " FROM " + TImages.TBL_NAME + " WHERE "
						+ TUsers.ID + "=? ORDER BY " + TImages.ID + " DESC LIMIT ?,?");
				try {
					ps.setInt(1, userId);
					ps.setInt(2, skip);
					ps.setInt(3, limit);
					ResultSet rs = ps.executeQuery();
					try {
						ArrayList<Image> res = new ArrayList<Image>();
						while (rs.next()) {
							Image img = new Image();
							img.blob_id = rs.getInt(TImages.ID);
							img.height = rs.getInt(TImages.HEIGHT);
							img.mime = rs.getString(TImages.MIME);
							img.uploadedTime = rs.getTimestamp(TImages.UPLOADED).getTime();
							img.user_id = rs.getInt(TUsers.ID);
							img.width = rs.getInt(TImages.WIDTH);
							res.add(img);
						}
						
						return res.toArray(new Image[res.size()]);
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	
	public byte[] getSymbol(final int symbol_id) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<byte[]>() {
			public byte[] execute(Connection conn) throws SQLException {
				PreparedStatement ps;
				ps = conn.prepareStatement("SELECT * FROM " + TSymbols.TBL_NAME + " WHERE " + TSymbols.ID + "=?");
				try {
					ps.setInt(1, symbol_id);
					ResultSet rs = ps.executeQuery();
					try {
						if (rs.next()) {
							return rs.getBytes(TSymbols.ZIP);
						} else {
							return null;
						}
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	
	public void setSymbol(final int symbol_id, final byte[] bytes) throws SQLException {
		dbPoolHolder.executeUpdate(new DBExecutor<Void>() {
			public Void execute(Connection conn) throws SQLException {
				byte[] existing = getSymbol(symbol_id);
				if (existing!=null) {
					UpdateConditions update = Update.update(TSymbols.TBL_NAME);
					update
					.where(TSymbols.ID, symbol_id)
					.set(TSymbols.ZIP, bytes)
					.execute(conn);
				} else {
					Update insert = Update.insert(TSymbols.TBL_NAME);
					insert
					.set(TSymbols.ID, symbol_id)
					.set(TSymbols.ZIP, bytes)
					.execute(conn);
				}
				return null;
			}
		});
	}
	
	public Integer[] getSymbolIds() throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<Integer[]>() {
			public Integer[] execute(Connection conn) throws SQLException {
				PreparedStatement ps;
				ps = conn.prepareStatement("SELECT " + TSymbols.ID + " FROM " + TSymbols.TBL_NAME);
				try {
					ResultSet rs = ps.executeQuery();
					try {
						ArrayList<Integer> result = new ArrayList<Integer>();
						while (rs.next()) {
							int symbol_id = rs.getInt(TSymbols.ID);
							result.add(symbol_id);
						}
						return result.toArray(new Integer[result.size()]);
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	
	public Integer[] getVisibleSymbolIds() throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<Integer[]>() {
			public Integer[] execute(Connection conn) throws SQLException {
				PreparedStatement ps;
				ps = conn.prepareStatement("SELECT " + TSymbols.ID + " FROM " + TSymbols.TBL_NAME + " WHERE " + TSymbols.SYMBOL_ORDER_UI + " IS NOT NULL " + " ORDER BY " + TSymbols.SYMBOL_ORDER_UI + " ASC ");
				try {
					ResultSet rs = ps.executeQuery();
					try {
						ArrayList<Integer> result = new ArrayList<Integer>();
						while (rs.next()) {
							int symbol_id = rs.getInt(TSymbols.ID);
							result.add(symbol_id);
						}
						return result.toArray(new Integer[result.size()]);
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	
	
		
	
	
	
	
	/**
	 * replace with configuration table
	 * @param catId
	 * @return
	 * @throws SQLException
	 */
	public WidgetInfo getWidgetInfo(final String uniqueId) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<WidgetInfo>() {
			public WidgetInfo execute(Connection conn) throws SQLException {
				PreparedStatement ps;
				ps = conn.prepareStatement("SELECT * FROM " + TWidgets.TBL_NAME +" WHERE "+TWidgets.UNIQUEID+"= ?");
				ps.setString(1, uniqueId);
				try {
					ResultSet rs = ps.executeQuery();
					try {
						if (rs.next()){
							WidgetInfo widgetInfo = new WidgetInfo();
							
							widgetInfo.widgetId = rs.getInt(TWidgets.WIDGETID);
							widgetInfo.uniqueKey = rs.getString(TWidgets.UNIQUEID);
							widgetInfo.themeId = rs.getInt(TWidgets.THEMEID);
							widgetInfo.asUserId = rs.getInt(TWidgets.ASUSER);
							if (rs.wasNull()) {
								widgetInfo.asUserId = Integer.MIN_VALUE;
							}
							return widgetInfo;
						} 
						return null;
					} finally {
						DBUtil.close(rs);
					}
				} finally {
					DBUtil.close(ps);
				}
			}
		});
	}
	

	
	private static final String getLastTableDataWriteDateSQL() {
		return " " + TTables.LASTDATAWRITEDATE + "=now() ";
	}
	private static final Timestamp currentTime() {
		return new Timestamp(System.currentTimeMillis());
	}

    public User getUserByAuthenticationKey(final String users_authentication_key) throws SQLException {
        return dbPoolHolder.executeLocal(new DBExecutor<User>() {
            public User execute(Connection conn) throws SQLException {
            	
            	LoggableStatement ls = null;
            	ResultSet rs = null;
            	try {
            		ls = new LoggableStatement(conn,
            				"SELECT " + TUsers.ID + " FROM " + TUsers.TBL_NAME + " WHERE " + 
            						TUsers.AUTHENTICATION_KEY + "=?");
            		ls.setString(1, users_authentication_key);
            		rs = ls.executeQuery();
                    if (rs.next()) {
                        int userId = rs.getInt(TUsers.ID);
                        return getUser(userId);
                    } else {
                        return null;
                    }
                } finally {
                    DBUtil.close(rs);
                    DBUtil.close(ls);
                }
            }
        });
    }
    
    
    
    
    /**
     * -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     **/
    
    
    
    
    private static void setPermissions(GeopediaEntity objType, int objId, int type,
			int typeId, int level, Connection conn) throws SQLException {
		Update.replace(TPermissions.TBL_NAME)

		.set(TPermissions.OBJ_TYPE, objType.getId()).set(TPermissions.OBJ_ID, objId).set(TPermissions.ALLOWED_TYPE, type).set(TPermissions.ALLOWED_ID,
			typeId).set(TPermissions.ALLOWED_LEVEL, level)

		.execute(conn);
	}
    
    public Void modifyPersonalEntity (final GeopediaEntity entity, final int entityId, final int userId, final PersonalGroup group, final boolean delete) throws SQLException {
    	return dbPoolHolder.executeUpdate(new DBExecutor<Void>() {

			@Override
			public Void execute(Connection conn) throws SQLException {
				modifyPersonalEntity(entity, entityId, userId, group, delete, conn);
				return null;
			}
    		
    	});
    }
    
    private static void modifyPersonalEntity(GeopediaEntity entity, int entityId, int userId, PersonalGroup group, boolean delete, Connection conn) throws SQLException {
    	if (entity == GeopediaEntity.THEME) {
    		if (!delete) {
    			Update.replace(TUserThemes.TBL_NAME)
  			  .set(TUserThemes.USER_ID, userId)
  			  .set(TUserThemes.THEME_ID, entityId)
  			  .set(TUserThemes.GROUP, group.getSQLValue())
  			  .execute(conn);    			
    		} else {
    			Update.delete(TUserThemes.TBL_NAME)
    				.where(TUserThemes.USER_ID, userId)
    				.where(TUserThemes.THEME_ID, entityId)
    				.where(TUserThemes.GROUP, group.getSQLValue())
    				.execute(conn);
    		}
    	} else if (entity == GeopediaEntity.TABLE) {
    		if (!delete) {
    	    	Update.replace(TUserTables.TBL_NAME)
        		.set(TUserTables.TABLE_ID, entityId)
        		.set(TUserTables.USER_ID, userId)
        		.set(TUserTables.GROUP, group.getSQLValue())
        		.execute(conn);        		    			
    		} else {
    			Update.delete(TUserTables.TBL_NAME)
				.where(TUserTables.USER_ID, userId)
				.where(TUserTables.TABLE_ID, entityId)
				.where(TUserTables.GROUP, group.getSQLValue())
				.execute(conn);
    		}
    		
    	}
    }
    
    public Theme saveTheme(final Theme theme, final User user) throws SQLException {
    	
    	 Connection conn = null;
    	 boolean commited=false; // silly mysql
    	 
		  try {
			  conn = dbPoolHolder.getUpdate();
			  conn.setAutoCommit(false);
				Theme tmpTheme = saveTheme(theme, user, conn);
			    conn.commit();
			    commited=true;
			    return tmpTheme;		
		  } finally {
			  if (!commited) DBUtil.rollBack(conn);
			  DBUtil.close(conn);
		  }
    }
    
    private static void createUserThemePermissions(Theme theme, User user, Connection conn ) throws SQLException {
    	createPermissions(GeopediaEntity.THEME, theme.id, Permissions.A_USER, user.getId(), Permissions.THEME_ADMIN, conn);
    }
    
    private static void createPermissions(GeopediaEntity entityType, int objId, int allowedType, int allowedId, int allowedLevel, Connection conn) throws SQLException {
		Update.insert(TPermissions.TBL_NAME)
			  .set(TPermissions.OBJ_TYPE, entityType.getId())
			  .set(TPermissions.OBJ_ID, objId)
			  .set(TPermissions.ALLOWED_TYPE, allowedType)
			  .set(TPermissions.ALLOWED_ID, allowedId)
			  .set(TPermissions.ALLOWED_LEVEL,	allowedLevel)
			  .execute(conn);
    }
	
    
    private void deleteTheme(Theme theme, User user, Connection conn) throws SQLException {
    	// remove permissions for this theme
    	Update.delete(TPermissions.TBL_NAME)
    		  .where(TPermissions.OBJ_ID, theme.getId())
    		  .where(TPermissions.OBJ_TYPE, GeopediaEntity.THEME.getId())
    		  .execute(conn);
    	
    	// remove from user themes 
    	Update.delete(TUserThemes.TBL_NAME)
    		  .where(TUserThemes.THEME_ID, theme.getId())
    		  .execute(conn);
    	
    	// remove theme from categories
    	Update.delete(TThemeCategories.TBL_NAME)
    	 	  .where(TThemeCategories.THEME_ID, theme.getId())
    	 	  .execute(conn);
    	
    	// remove all theme tables
		Update.delete(TThemeTables.TBL_NAME)
			  .where(TThemes.ID, theme.getId())
			  .execute(conn);
		
		// delete theme  
		Update.delete(TThemes.TBL_NAME)
			  .where(TThemes.ID, theme.getId())
			  .execute(conn);
		
		// TODO: fix this
		{
			getTranslationService().deleteTheme(theme.getId());
        	TranslationServiceImpl.TRANSLATION_TAG();
		}
		
		// remove theme from meta data
		meta.deleteTheme(theme.getId());

    	
    }
	public Theme saveTheme(Theme theme, User user, Connection conn) throws SQLException {

		if (theme.isDeleted()) {
			deleteTheme(theme, user, conn);
			return theme;
		}
		
		Update themeUpd;

		ArrayList<ThemeTableLink> ttlsToDelete = new ArrayList<ThemeTableLink>();
		if (theme.hasValidId()) {
			ttlsToDelete = loadThemeTables(theme.id, conn);
			themeUpd = Update.update(TThemes.TBL_NAME).where(TThemes.ID,theme.id);
		} else {
			themeUpd = Update.insert(TThemes.TBL_NAME);
		}
		
		theme.lastMetaChange = getNewMetaTimestamp(conn);				
		MetaServiceImpl.createDispHTML(theme);
		themeUpd.set(TThemes.NAME, theme.getName());
		themeUpd.set(TThemes.DESC, theme.descRawHtml);
		themeUpd.set(TThemes.DATASETS, ConfigurationUtils.themeDatasetsToString(theme.baseLayers));
		themeUpd.set(TThemes.PROPERTIES, stateToString(theme.properties));
		themeUpd.set(TThemes.LASTMETACHANGE, theme.lastMetaChange);
		themeUpd.set(TThemes.PUBLICPERMS, theme.public_perms);
		themeUpd.execute(conn);
		
		if (!theme.hasValidId()) {
			theme.id = themeUpd.getLastInsertedId();
			createUserThemePermissions(theme, user, conn);		
			user.forcePrivilegesReload();
			modifyPersonalEntity(GeopediaEntity.THEME, theme.getId(), user.getId(), PersonalGroup.PERSONAL, false, conn);
		}

		//todo save theme
		for (ThemeTableLink ttl:theme.tables) {
			ttlsToDelete.remove(ttl);
			ttl.setTheme(theme); // necessary when new theme is created
			saveThemeTableLink(ttl, conn);
		}
		
		for (ThemeTableLink ttl:ttlsToDelete) {
			Update.delete(TThemeTables.TBL_NAME).where(TThemeTables.ID, ttl.id).execute(conn);
		}
		return theme;
	}
	
	
	public long getNewMetaTimestamp(Connection conn) throws SQLException {
		Statement s = conn.createStatement();
		try {
			s.execute("LOCK TABLES " + TCounter.TBL_NAME + " WRITE");
			try {
				s.execute("UPDATE " + TCounter.TBL_NAME + " SET " + TCounter.CURRVAL + "=" + TCounter.CURRVAL + "+1");
				ResultSet rs = s.executeQuery("SELECT " + TCounter.CURRVAL + " FROM " + TCounter.TBL_NAME);
				try {
					if (rs.next()) {
						return rs.getLong(1);
					} else {
						throw new IllegalStateException("Missing counter");
					}
				} finally {
					DBUtil.close(rs);
				}
			} finally {
				try {
					s.execute("UNLOCK TABLES");
				} catch (SQLException e) {
					DBUtil.close(conn);
				}
			}
		} finally {
			DBUtil.close(s);
		}	
	}
	
	private static void saveThemeTableLink(ThemeTableLink ttl, Connection conn) throws SQLException {
		
		Update upd;
		if (ttl.hasValidId()) {
			upd = Update.update(TThemeTables.TBL_NAME);
			upd.where(TThemeTables.ID, ttl.id);
		} else {
			upd = Update.insert(TThemeTables.TBL_NAME);			
		}
		
		
		upd.set(TThemes.ID, ttl.themeId)
		   .set(TTables.ID, ttl.tableId)
		   .set(TThemeTables.ONOFF, ttl.on)
		   .set(TThemeTables.ORDER, ttl.orderInTheme)
		   .set(TThemeTables.STYLEJS,	ttl.getStyle())
		   .set(TThemeTables.GROUP, ttl.group)
		   .set(TThemeTables.ALTNAME, ttl.getAlternativeName())
		   .set(TThemeTables.PROPERTIES, stateToString(ttl.properties));
		
		int rv = upd.execute(conn);
		if (!ttl.hasValidId()) {
			ttl.id = rv;
		}			
	}

	
	
    private static String appendVerifyPermissions(ArrayList<Object> params,  GeopediaEntity objectType, String objectId, User user, int permission) {
    	String sql = "SELECT "+TPermissions.OBJ_ID+" FROM "+TPermissions.TBL_NAME+" p WHERE p."+TPermissions.OBJ_TYPE+"=? AND p."+TPermissions.ALLOWED_LEVEL+">=? "
    		+" AND p."+TPermissions.OBJ_ID+"="+objectId+" AND ("
    		+ "(p."+TPermissions.ALLOWED_TYPE+"="+Permissions.A_USER+" AND p."+TPermissions.ALLOWED_ID+"=?) OR "
    		+ "(p."+TPermissions.ALLOWED_TYPE+"="+Permissions.A_GROUP+" AND p."+TPermissions.ALLOWED_ID+" IN "
    		+ "(SELECT "+TUserGroups.GROUP_ID+" FROM "+TUserGroups.TBL_NAME+" WHERE "+TUserGroups.USER_ID+"=?)))";
    	params.add(objectType.getId());
    	params.add(permission);
    	params.add(user.getId());
    	params.add(user.getId());
    	return sql;
    }

    
    
	public String getTablePermissionsSQL(int minimumPermission, User user, String tableAlias,
			ArrayList<Object> params) {
		if (user != null && user.isAdmin())
			return " 1=1 ";
		if (!StringUtil.isNullOrEmpty(tableAlias) && !tableAlias.endsWith(".")) {
			tableAlias+=".";
		}
		String sql = "(" + tableAlias + TTables.PUBLICPERMS + ">=?";
		params.add(minimumPermission);
		if (user != null) {
			sql += " OR "+tableAlias+ TTables.ID
					+ " IN ("
					+ appendVerifyPermissions(params, GeopediaEntity.TABLE, tableAlias+ TTables.ID, user, minimumPermission) + ")";
		}
		sql += ")";
		return sql;
	}
	
	
	public String getThemePermissionsSQL(int minimumPermission, User user, String themeAlias,
			ArrayList<Object> params) {
		if (user != null && user.isAdmin())
			return " 1=1 ";
		if (!StringUtil.isNullOrEmpty(themeAlias) && !themeAlias.endsWith(".")) {
			themeAlias+=".";
		}
		String sql = "(" + themeAlias + TThemes.PUBLICPERMS + ">=?";
		params.add(minimumPermission);
		if (user != null) {
			sql += " OR "+themeAlias+ TThemes.ID
					+ " IN ("
					+ appendVerifyPermissions(params, GeopediaEntity.THEME, themeAlias+ TThemes.ID, user, minimumPermission) + ")";
		}
		sql += ")";
		return sql;
	}
	
	
	public PagableHolder<ArrayList<Table>> queryUserTables(final User user, final PersonalGroup tableGroup, final int dataStartIdx, final int dataEndIdx) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<PagableHolder<ArrayList<Table>>>() {

			@Override
			public PagableHolder<ArrayList<Table>> execute(Connection conn) throws SQLException {
				
				int permission = Permissions.TABLE_DISCOVER;//TODO fix when permissions are fixed
				
				ArrayList<Object> params = new ArrayList<Object>();
				
				StringBuilder sqlSB = new StringBuilder("SELECT tbl.* FROM "+TTables.TBL_NAME+" tbl, "+TUserTables.TBL_NAME+" utbl WHERE "+
						getTablePermissionsSQL(permission,user,"tbl", params)
						+ " AND tbl."+TTables.DELETED+"=0 " 
						+ " AND utbl."+TUserTables.TABLE_ID+"=tbl."+TTables.ID
						+ " AND utbl."+TUserTables.USER_ID+"=?"
						+ " AND utbl."+TUserTables.GROUP+"=?"
						+" ORDER BY utbl."+TUserTables.TIMESTAMP+" DESC");
				
				params.add(user.getId());
				params.add(tableGroup.getSQLValue());
				
				return doQueryForPageHolder(DB.this, conn, sqlSB, params, dataStartIdx, dataEndIdx, TablesMapper.INSTANCE);
				
			}
			
		});
	}
	
	
	public ArrayList<Object> queryUserTablesAndThemes(final User user, final PersonalGroup tableGroup) throws SQLException {
		PagableHolder<ArrayList<Table>> resTables = queryUserTables(user, tableGroup, PagableHolder.DATA_LOCATION_ALL, PagableHolder.DATA_LOCATION_ALL);
		
		PagableHolder<ArrayList<Theme>> resThemes = queryUserThemes(user, tableGroup, PagableHolder.DATA_LOCATION_ALL, PagableHolder.DATA_LOCATION_ALL);
		
		ArrayList<Object> res = new ArrayList<Object>(resTables.getCollection().size() + resThemes.getCollection().size());
		res.addAll(resTables.getCollection());
		res.addAll(resThemes.getCollection());
		
		return res;
	}
	
	
	
	
	public PagableHolder<ArrayList<Theme>> queryUserThemes(final User user, final PersonalGroup tableGroup, final int dataStartIdx, final int dataEndIdx) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<PagableHolder<ArrayList<Theme>>>() {

			@Override
			public PagableHolder<ArrayList<Theme>> execute(Connection conn) throws SQLException {
				
				int permission = Permissions.THEME_DISCOVER;//TODO fix when permissions are fixed
				
				ArrayList<Object> params = new ArrayList<Object>();
				
				StringBuilder sqlSB = new StringBuilder("SELECT thm.* FROM "+TThemes.TBL_NAME+" thm, "+TUserThemes.TBL_NAME+" uthm WHERE "+
						getThemePermissionsSQL(permission,user,"thm", params)
						// + " AND t."+TThemes.DELETED+"=0 " 
						+ " AND uthm."+TUserThemes.THEME_ID+"=thm."+TThemes.ID
						+ " AND uthm."+TUserThemes.USER_ID+"=?"
						+ " AND uthm."+TUserThemes.GROUP+"=?"
						+" ORDER BY uthm."+TUserThemes.TIMESTAMP+" DESC");
				
				params.add(user.getId());
				params.add(tableGroup.getSQLValue());
				
				return doQueryForPageHolder(DB.this, conn, sqlSB, params, dataStartIdx, dataEndIdx, ThemeMapper.INSTANCE);
				
			}
			
		});
	}
	
	
	public PagableHolder<ArrayList<Theme>>  queryThemes(final Integer categoryId, final String themeName, final User user, final int dataStartIdx,
    		final int dataEndIdx) throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<PagableHolder<ArrayList<Theme>>>() {

			@Override
			public PagableHolder<ArrayList<Theme>> execute(Connection conn)
					throws SQLException {
				
				
				StringBuilder sqlSB = new StringBuilder();
				ArrayList<Object> params = new ArrayList<Object>();
				
				if (user!=null && (categoryId == Category.PERSONAL.getId() || categoryId == Category.FAVOURITE.getId())) {
					sqlSB.append("SELECT t.* FROM "+TThemes.TBL_NAME+" t, "+TUserThemes.TBL_NAME+" ut"
							+ " WHERE t."+TThemes.ID+"=ut."+TUserThemes.THEME_ID+" AND ut."+TUserThemes.USER_ID+"=? AND "
							+ "ut."+TUserThemes.GROUP+"=?");
					params.add(user.getId());
					if (categoryId == Category.PERSONAL.getId()) {
						params.add(PersonalGroup.PERSONAL.getSQLValue());	
					} else {
						params.add(PersonalGroup.FAVOURITE.getSQLValue());
					}
					
				} else {
					int permission = Permissions.THEME_DISCOVER; //TODO fix when permissions are fixed
					
					sqlSB.append("SELECT t.* FROM "+TThemes.TBL_NAME+" t WHERE "+
							getThemePermissionsSQL(permission,user, "t", params));
					//TODO: fix deleted themes
							//+" AND "+TThemes.DELETED+"=0 ";
					if (categoryId!=null) {
						sqlSB.append("AND t."+TThemes.ID+" IN (SELECT "+TThemeCategories.THEME_ID+" FROM "+TThemeCategories.TBL_NAME+" WHERE "+TThemeCategories.CATEGORY_ID+"=?)");
						params.add(categoryId);
					}
				}
				
				if (!StringUtil.isNullOrEmpty(themeName)) {
					sqlSB.append(" AND t."+TThemes.NAME+" LIKE ? ");
					params.add("%"+themeName+"%");
				}

				sqlSB.append(" ORDER BY "+TThemes.NAME);
				
				return doQueryForPageHolder(DB.this, conn, sqlSB, params, dataStartIdx, dataEndIdx, ThemeMapper.INSTANCE);
			}
		});

	}
	
    public PagableHolder<ArrayList<Table>> queryTables(final Integer categoryId, final String tableName, final User user, final int dataStartIdx,
    		final int dataEndIdx) throws SQLException {
    	return dbPoolHolder.executeLocal(new DBExecutor<PagableHolder<ArrayList<Table>>>() {

			@Override
			public PagableHolder<ArrayList<Table>> execute(Connection conn)
					throws SQLException {
				
				StringBuilder sqlSB = new StringBuilder();
				ArrayList<Object> params = new ArrayList<Object>();
				
				if (user!=null && (categoryId == Category.PERSONAL.getId() || categoryId == Category.FAVOURITE.getId())) {
					sqlSB.append("SELECT t.* FROM "+TTables.TBL_NAME+" t, "+TUserTables.TBL_NAME+" ut"
							+ " WHERE t."+TTables.ID+"=ut."+TUserTables.TABLE_ID+" AND ut."+TUserTables.USER_ID+"=? AND "
							+ "ut."+TUserTables.GROUP+"=?");
					params.add(user.getId());
					if (categoryId == Category.PERSONAL.getId()) {
						params.add(PersonalGroup.PERSONAL.getSQLValue());	
					} else {
						params.add(PersonalGroup.FAVOURITE.getSQLValue());
					}
					
				} else {
					int permission = Permissions.TABLE_DISCOVER; //TODO fix when permissions are fixed
					
					sqlSB.append("SELECT t.* FROM "+TTables.TBL_NAME+" t WHERE "+
							getTablePermissionsSQL(permission,user,"t", params)
							+" AND "+TTables.DELETED+"=0 ");
					if (categoryId!=null) {
						sqlSB.append("AND t."+TTables.ID+" IN (SELECT "+TCategoryTables.TABLE_ID+" FROM "+TCategoryTables.TBL_NAME+" WHERE "+TCategoryTables.CAT_ID+"=?)");
						params.add(categoryId);
					}
				}
				
				if (!StringUtil.isNullOrEmpty(tableName)) {
					sqlSB.append(" AND t."+TTables.NAME+" LIKE ? ");
					params.add("%"+tableName+"%");
				}

				sqlSB.append(" ORDER BY "+TTables.NAME);
				
				return doQueryForPageHolder(DB.this, conn, sqlSB, params, dataStartIdx, dataEndIdx, TablesMapper.INSTANCE);
			}
    	});
    }
    
    //Used when calling queries that need PagableHolder
    private static class TablesMapper implements ResultMapper<Table>{
		static TablesMapper INSTANCE = new TablesMapper();
		
		public Table createNewTarget(){
			return new Table();
		}
		
		
		
		public void map(DB dbInstance, ResultSet rs, Table t) throws SQLException {
			loadTable(t, rs);
		}
		
	}
    
    //Used when calling queries that need PagableHolder
  	private static class ThemeMapper implements ResultMapper<Theme>{
  		static ThemeMapper INSTANCE = new ThemeMapper();
  		
  		public Theme createNewTarget(){
  			return new Theme();
  		}
  		
  		
  		public void map(DB dbInstance, ResultSet rs, Theme t) throws SQLException {
  			
  			dbInstance.loadTheme(t, rs);
  			
  	    }
  	}
    
    static <T> PagableHolder<ArrayList<T>> doQueryForPageHolder(DB dbInstance, Connection conn, StringBuilder sqlSB, ArrayList<Object> params, 
			final int dataStartIdx, final int dataEndIdx, ResultMapper<T> mapper) 
					throws SQLException {
		
		int expectedCount = limitQuery(sqlSB, params, dataStartIdx, dataEndIdx);		
		
		LoggableStatement ls = new LoggableStatement(conn, sqlSB.toString());
		
		ResultSet rs = null;
		ArrayList<T> mapped = new ArrayList<T>();
		try {
			for (int i=0;i<params.size();i++) {
				ls.setObject(i+1, params.get(i));
			}
			rs = ls.executeQuery();
			int count = 0;
			while (rs.next()) {
				if (count<expectedCount) {
					T objToMapTo = mapper.createNewTarget();
					mapper.map(dbInstance, rs, objToMapTo);
					mapped.add(objToMapTo);
				}
				count++;
			}
			boolean hasMoreData = false;
			if (expectedCount>0 && expectedCount<count)
				hasMoreData=true;
			return new PagableHolder<ArrayList<T>>(mapped, dataStartIdx, dataStartIdx+count-1, hasMoreData) ;

		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);
		}
	}
    
    private static int limitQuery(StringBuilder sqlSB, ArrayList<Object> params, final int dataStartIdx, final int dataEndIdx){
		int expectedCount = Integer.MAX_VALUE;
		if (dataStartIdx != PagableHolder.DATA_LOCATION_ALL && 
			dataEndIdx != PagableHolder.DATA_LOCATION_ALL) {
			sqlSB.append(" LIMIT ?,?");
			params.add(dataStartIdx);
			expectedCount=(dataEndIdx-dataStartIdx)+1;
			params.add(expectedCount+1);
		}
		
		return expectedCount;
	}
    
    
    public ArrayList<Category> queryCategories(final Category filter, final User user) throws SQLException {
    	return dbPoolHolder.executeLocal(new DBExecutor<ArrayList<Category>>() {

			@Override
			public ArrayList<Category> execute(Connection conn)
					throws SQLException {
				
				String sql = "SELECT "+TCategories.ID+"," +TCategories.PARENTID+","+TCategories.NAME+
						","+TCategories.DESC +" FROM "+TCategories.TBL_NAME +" WHERE (1=1)";

				ArrayList<Object> params = new ArrayList<Object>();
				if (filter !=null) {
					if (filter.hasId()) {
						sql+=" AND "+TCategories.ID+"=?";
						params.add(filter.getId());
					}
					if (filter.hasParentId()) {
						sql+=" AND "+TCategories.PARENTID+"=?";
						params.add(filter.getParentId());
					}
					if (filter.hasName()) {
						sql+=" AND "+TCategories.NAME+" LIKE ?";
						params.add("%"+filter.getName()+"%");
					}
					if (filter.hasDescription()) {
						sql+=" AND "+TCategories.DESC+" LIKE ?";
						params.add("%"+filter.getDescription()+"%");
					}
				}
				ArrayList<Category> categoryList = new ArrayList<Category>();
				if (user!=null) {
					categoryList.add(Category.PERSONAL);
					categoryList.add(Category.FAVOURITE);
				}
				LoggableStatement ls = new LoggableStatement(conn, sql);
				ResultSet rs = null;
				try {
					for (int i=0;i<params.size();i++) {
						ls.setObject(i+1, params.get(i));
					}
					rs = ls.executeQuery();
					while (rs.next()) {
						Category cat = new Category(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4));
						categoryList.add(cat);
					}
					return categoryList;
				} finally {
					DBUtil.close(rs);
					DBUtil.close(ls);
				}
			}
		});
    }
    
    /*------*/
    
    
    public Theme loadTheme(final long themeId) throws SQLException {
    	return dbPoolHolder.executeLocal(new DBExecutor<Theme>() {
			public Theme execute(Connection conn) throws SQLException {
				return loadTheme(themeId,conn);
			}
    	});
    	
    }
    
    
    
    private  ArrayList<ThemeTableLink> loadThemeTables(long themeId, Connection conn) throws SQLException {
    	LoggableStatement ls = null;
		ResultSet rs = null;
		try {
			ls = new LoggableStatement(conn, "SELECT * FROM "+TThemeTables.TBL_NAME+" WHERE "+TThemeTables.THEME_ID+"=? ORDER BY "+TThemeTables.ORDER);
			ls.setLong(1,themeId);
			rs = ls.executeQuery();
			ArrayList<ThemeTableLink> list = new ArrayList<ThemeTableLink>();
			while (rs.next()) {
				ThemeTableLink ttl = new ThemeTableLink();
				loadThemeTableLink(ttl, rs);				
				Table tbl = loadTable(ttl.tableId, conn);
				if (tbl==null) {
					logger.error("ThemeTableLink id:"+ttl.id+" links to non existing table id:"+ttl.tableId);
				} else {
					ttl.setTable(tbl);
				}
				list.add(ttl);
			}
			return list;
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);
		}
    }
    
    private Field[] loadTableFields (long tableId, Connection conn) throws SQLException {
    	LoggableStatement ls = null;
		ResultSet rs = null;
		try {
			ArrayList<Field> list = new ArrayList<Field>();
			ls = new LoggableStatement(conn, "SELECT * FROM " + TFields.TBL_NAME + " WHERE " + TFields.TABLE_ID + "=?");
			ls.setLong(1, tableId);
			rs = ls.executeQuery();
			while (rs.next()) {
				Field f = new Field();
				loadField(f,rs);
				list.add(f);
			}
			return list.toArray(new Field[list.size()]);
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);
		}
    }
    private  Table loadTable(long tableId, Connection conn) throws SQLException {
    	LoggableStatement ls = null;
		ResultSet rs = null;
		try {
			Table table = null;
			ls = new LoggableStatement(conn, "SELECT * FROM " + TTables.TBL_NAME + " WHERE " + TTables.ID + "=?");
			ls.setLong(1, tableId);
			rs = ls.executeQuery();
			if (rs.next()) {
				table = new Table();
				loadTable(table, rs);
				table.fields = loadTableFields(tableId,conn);
			}
			return table;
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);
		}
    }
    private Theme loadTheme(long themeId, Connection conn) throws SQLException {
    	LoggableStatement ls = null;
		ResultSet rs = null;
		try {
			ls = new LoggableStatement(conn, "SELECT * FROM " + TThemes.TBL_NAME + " WHERE " + TThemes.ID + "=?");
			ls.setLong(1, themeId);
			rs = ls.executeQuery();
			Theme theme = null;
			if (rs.next()) {
				theme = new Theme();
				loadTheme(theme,rs);
				ArrayList<ThemeTableLink> ttlList  = loadThemeTables(themeId, conn);
				theme.tables = ttlList.toArray(new ThemeTableLink[ttlList.size()]);;				
			}
			return theme;

		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);
		}
    }
    
	private void loadField(Field f, ResultSet rs) throws SQLException {
		f.defaultValueString = rs.getString(TFields.DEFAULTVALUE);
		f.descRawHtml = rs.getString(TFields.DESC);
		f.setFlags(rs.getInt(TFields.FLAGS));
		f.id = rs.getInt(TFields.ID);
		f.setName(rs.getString(TFields.NAME));
		f.order = rs.getInt(TFields.ORDER);
		f.refdTableId = rs.getInt(TFields.REFD_TABLE_ID);
		f.tableId = rs.getInt(TTables.ID);
		f.type = Field.FieldType.forId(rs.getInt(TFields.TYPE));
		f.sysId = rs.getString(TFields.SYSID);
		f.properties = stateFromString(rs.getString(TFields.PROPERTIES));

		{
			getTranslationService().commitFieldLanguages(f.id,
					rs.getString(TFields.LANGUAGES));// TODO:drejmar
			TranslationServiceImpl.TRANSLATION_TAG();
		}

		MetaServiceImpl.createDispHTML(f);

	}
    
	
	
    private static void loadTable(Table t, ResultSet rs) throws SQLException {    	
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
        Double minx= rs.getDouble(TTables.MINX);
        Double miny= rs.getDouble(TTables.MINY);
        Double maxx= rs.getDouble(TTables.MAXX);
        Double maxy= rs.getDouble(TTables.MAXY);
        if (minx!=null && miny!=null && maxx!=null && maxy!=null) {
        	t.envelope = new Envelope(minx,miny,maxx,maxy);
        }
        
		MetaServiceImpl.createDispHTML(t);
    }
    
    private static void loadThemeTableLink(ThemeTableLink ttl, ResultSet rs) throws SQLException {
		ttl.id = rs.getInt(TThemeTables.ID);
		ttl.on = rs.getInt(TThemeTables.ONOFF);
		ttl.orderInTheme = rs.getInt(TThemeTables.ORDER);
		ttl.setStyleJS(rs.getString(TThemeTables.STYLEJS));
		ttl.group = rs.getString(TThemeTables.GROUP);
        ttl.setName(rs.getString(TThemeTables.ALTNAME));
        ttl.properties = stateFromString(rs.getString(TThemeTables.PROPERTIES));		
        ttl.tableId = rs.getInt(TTables.ID);        
    }
    
    private  void loadTheme(Theme t, ResultSet rs) throws SQLException
    {
		t.descRawHtml = rs.getString(TThemes.DESC);
		t.id = rs.getInt(TThemes.ID);
		t.lastMetaChange = rs.getLong(TThemes.LASTMETACHANGE);
		t.setName(rs.getString(TThemes.NAME));
		t.public_perms = rs.getInt(TThemes.PUBLICPERMS);
		t.baseLayers = ConfigurationUtils.themeDatasetsFromString(rs.getString(TThemes.DATASETS));
		t.properties = stateFromString(rs.getString(TThemes.PROPERTIES));
		
		{
			getTranslationService().commitThemeLanguages(t.id, rs.getString(TThemes.LANGUAGES));//TODO:drejmar
			TranslationServiceImpl.TRANSLATION_TAG();
		}
		
		MetaServiceImpl.createDispHTML(t);
    }
    
    
    
    
    public void loadFeatureEnvelope(Feature feature, ResultSet rs, Heights heights) throws SQLException {
    	int tableId = feature.tableId;
    	
    	double minx=rs.getDouble(FeaturesTable.minX(tableId));
    	double miny=rs.getDouble(FeaturesTable.minY(tableId));
    	double maxx=rs.getDouble(FeaturesTable.maxX(tableId));
    	double maxy=rs.getDouble(FeaturesTable.maxY(tableId));
    	double mbrCentX = 0.5 * (minx + maxx);
		double mbrCentY = 0.5 * (miny + maxy);
		feature.envelope = new Envelope(minx,miny,maxx,maxy);
    	if (feature.geomType.isPoint()) {
    		feature.centroid = new Point(mbrCentX, mbrCentY);
//    		FeatureServiceImpl.getZVal(mbrCentX, mbrCentY, heights)
		} else if (feature.geomType.isLine()) {
			feature.length=	rs.getDouble(FeaturesTable.length(tableId));
//			FeatureServiceImpl.getZVal(mbrCentX, mbrCentY, heights)
		} else {
			double centX = rs.getDouble(FeaturesTable.centroidX(tableId));
			double centY = rs.getDouble(FeaturesTable.centroidY(tableId));
    		feature.centroid = new Point(centX, centY);
    		feature.length = rs.getDouble(FeaturesTable.length(tableId));
    		feature.area = rs.getDouble(FeaturesTable.area(tableId));
//    		FeatureServiceImpl.getZVal(centX, centY, heights)
		}
    }
    private static void loadFeatureBase(Feature curr, Table table, ResultSet rs)
			throws SQLException {
    	int tableId = table.id;
		curr.id = rs.getInt(FeaturesTable.id(tableId));
		curr.lastUserId = rs.getInt(FeaturesTable.user(tableId));
		curr.tableId = tableId;
		curr.geomType = table.geomType;
		curr.tableDataTs = table.lastDataWrite;
		curr.timestamp = rs.getTimestamp(FeaturesTable.timestamp(tableId)).getTime();
		curr.fields = table.fields;
		curr.deleted = rs.getInt(FeaturesTable.deleted(tableId)) != 0;
		curr.fulltext = rs.getString(FeaturesTable.fullText(tableId));
	}
    
    
    public static void loadFeatureBaseMetadataFromResultSet(Feature feature, ResultSet rs,
    	 QueryField qfUser, QueryField qfDeleted, QueryField qfTimestamp) throws SQLException {
    	if (qfUser!=null) {
    		feature.lastUserId = rs.getInt(qfUser.getFieldSQLName(true));
    	}
    	if (qfDeleted!=null) {
    		feature.deleted = ( rs.getInt(qfDeleted.getFieldSQLName(true))==1?true:false);
    	}

    	if (qfTimestamp!=null) {
    		feature.timestamp = ( rs.getTimestamp(qfTimestamp.getFieldSQLName(true)).getTime());
    	}

    }
    
    public static void loadFeatureEnvelopeLengthAreaCentroidFromResultSet(Feature feature, ResultSet rs, 
    		QueryField qfMinX, QueryField qfMinY,QueryField qfMaxX, QueryField qfMaxY,
    		QueryField qfCenX, QueryField qfCenY,
    		QueryField qfLength, QueryField qfArea) throws SQLException {
    	
    	if (qfMinX!=null && qfMinY!=null && qfMaxX!=null && qfMaxY!=null) {
    		feature.envelope = new Envelope(
    				rs.getDouble(qfMinX.getFieldSQLName(true)),
    				rs.getDouble(qfMinY.getFieldSQLName(true)),
    				rs.getDouble(qfMaxX.getFieldSQLName(true)),
    				rs.getDouble(qfMaxY.getFieldSQLName(true)));
    	}
    	if (qfCenX!=null && qfCenY!=null) {
    		feature.centroid = new Point(
    				rs.getDouble(qfCenX.getFieldSQLName(true)),
    				rs.getDouble(qfCenY.getFieldSQLName(true)));
    	}
    	if (qfLength!=null) {
    		feature.length = rs.getDouble(qfLength.getFieldSQLName(true));
    	}
    	if (qfArea!=null) {
    		feature.area = rs.getDouble(qfArea.getFieldSQLName(true));
    	}
    }
    
    public static Property<?> readUserFieldFromResultSet(ResultSet rs, Field field, String fieldName) throws SQLException {
    	Property<?> property = PropertyUtils.forField(field);
    	switch (field.type) {
		case BLOB: {
			Long id = rs.getLong(fieldName);
			if (!rs.wasNull()) {
				((BinaryFileProperty)property).setValue(id);
			}
			break;
		}
		case BOOLEAN: {
			int i = rs.getInt(fieldName);
			if (!rs.wasNull()) {
				((BooleanProperty)property).setValue(Boolean.valueOf(i!=0));
			}
			break;
		}
		case DATE: {
			Timestamp ts = rs.getTimestamp(fieldName);
			if (!rs.wasNull()) {
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(ts);
				gc.set(GregorianCalendar.HOUR, 12);
				gc.set(GregorianCalendar.MINUTE, 0);
				gc.set(GregorianCalendar.SECOND, 0);
				gc.set(GregorianCalendar.MILLISECOND, 0);
				((DateProperty)property).setValue(new Date(gc.getTimeInMillis()));
			}
			break;
		}
		case DATETIME: {
			Timestamp ts = rs.getTimestamp(fieldName);
			if (!rs.wasNull()) {
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(ts);
				gc.set(GregorianCalendar.MILLISECOND, 0);
				((DateProperty)property).setValue(new Date(gc.getTimeInMillis()));
			}
			break;
		}
		case DECIMAL: {
			Double doubleVal = rs.getDouble(fieldName);
			if (!rs.wasNull()) {
				((DoubleProperty)property).setValue(doubleVal);
			}
			break;
		}
		case FOREIGN_ID: {
			long val = rs.getLong(fieldName);
			if (!rs.wasNull()) {
				ForeignReferenceProperty frp = (ForeignReferenceProperty)property;
				frp.setValue(val);				
			}
			break;
		}
		case INTEGER: {
			long val = rs.getLong(fieldName);
			if (!rs.wasNull()) {
				((LongProperty)property).setValue(val);				
			}
			break;
		}
		case WIKITEXT: {
			String val = rs.getString(fieldName);
			if (!rs.wasNull() && val!=null) {
				HTMLProperty hprop = (HTMLProperty)property;
				hprop.setValue(FeatureServiceImpl.modifyHtmlForDisplay(val));
				hprop.setRawHtml(val);
			}
		}
			break;
		case PLAINTEXT:
		case STYLE:
		case LONGPLAINTEXT: {
			String val = rs.getString(fieldName);
			if (!rs.wasNull()) {
				((TextProperty)property).setValue(val);
			}
			break;
		}
		default:
			throw new IllegalStateException();
		}
    	
    	return property;
    }
    
    
        
    private static StateGWT stateFromString(String str) {
        if (str==null || str.length()<4) return null;
        try {
        	return StateUtilJava.gwtFromJava(StateHelper.fromXML(str));
        } catch (Exception e) {
        	System.err.println("ERROR WHILE READING XML:"+str);
        	e.printStackTrace();
            return null;
        }
    }
    
    
	public static String buildFeatureFullText(Feature feature, Connection conn, InstanceConfiguration instanceConfiguration) throws SQLException {
		if (feature.fields == null || feature.properties == null)
			return null;
		StringBuffer ftBuffer = new StringBuffer();
		String sql = "";
		ArrayList<Long> idList = new ArrayList<Long>();
		for (int i = 0; i < feature.fields.length; i++) {
			Field f = feature.fields[i];
			Property<?> value = feature.properties[i];
			if (f == null || f.isDeleted()) {
				continue;
			}

			if (f.hasFlag(FieldFlags.FULLTEXTEXCLUDE) || f.isDeleted())
				continue;

			if (f.type == Field.FieldType.FOREIGN_ID) {
				ForeignReferenceProperty foreignValueHodler = (ForeignReferenceProperty) value;
				if (!PropertyUtils.isNull(foreignValueHodler)) {
					if (sql.length() > 0) {
						sql += " UNION ";
					}
					sql += "SELECT " + FeaturesTable.fullText(f.refdTableId) + " FROM " + FeaturesTable.table(f.refdTableId, instanceConfiguration) + " WHERE "
							+ FeaturesTable.id(f.refdTableId) + "=?";
					idList.add(foreignValueHodler.getValue());
				}

			} else {
				String ftString = toFulltextString(f, value);
				if (ftString != null) {
					String txt = ftString.replaceAll("[^a-zA-Z0-9ščćđžŠČĆŽĐ]+", " ").trim();// TODO: // fix this
					if  (!ftBuffer.toString().contains(txt)) {
						if (ftBuffer.length() > 0)
							ftBuffer.append(" ");
						ftBuffer.append(txt);
					}
				}
			}
		}

		if (sql.length()>0) {
			LoggableStatement ls = null;
			ResultSet rs = null;
			try {
				ls = new LoggableStatement(conn, sql);
				for (int i=0;i<idList.size();i++)
					ls.setLong(i+1,idList.get(i));
				rs = ls.executeQuery();
				while (rs.next()) {
					String ft = rs.getString(1).toLowerCase();
					if(ft!=null) {
						if (!ftBuffer.toString().contains(ft)) {
							if (ftBuffer.length()>0) ftBuffer.append(" ");
							ftBuffer.append(ft.trim());
						}
						ft = removeCarrons(ft);
						if (!ftBuffer.toString().contains(ft)) {
							if (ftBuffer.length()>0) ftBuffer.append(" ");
							ftBuffer.append(ft.trim());
						}
					}
				}
			} finally {
				DBUtil.close(rs);
				DBUtil.close(ls);
			}
		}
		return ftBuffer.toString();
	}
	
	
	private static String removeCarrons(String orig) {
		String transformed = orig.toLowerCase();
		return transformed.replaceAll("č", "c").replaceAll("š", "s").replaceAll("ž", "z").replaceAll("ć", "c");  // đ nije zamenljiv SRBIJA DO TOKIJA 
	}
    
	private static String toFulltextString(Field field, Property<?> property) {
		if (field==null || PropertyUtils.isNull(property))
			return null;
		switch (field.type) {
			case INTEGER:
			case LONGPLAINTEXT:
			case PLAINTEXT:
			case DECIMAL:
				return property.toString();
			case DATE:
			case DATETIME:
				Date date = ((DateProperty)property).getValue();
				return DateFormatter.FORMATTER_ISO_DATETIME.formatDate(date);
			case WIKITEXT:
				HTMLProperty htmlProp = (HTMLProperty)property;
				String raw = htmlProp.getRawHtml();
				if (raw==null) return null;
				return raw.replaceAll("<.*?>", "");
		}
		return null;
	}
	
	
	  public Feature saveFeature(final Feature feature, final Session session, ServerInstance instance) throws SQLException, GeopediaException {
		  Connection conn = null;
		  Statement lockSt = null;
		  boolean commited=false; // silly mysql
		  try {
			  conn = dbPoolHolder.getUpdate();
			  conn.setAutoCommit(false);
			  lockSt = conn.createStatement();
	    	  lockSt.execute("LOCK TABLES "+FeaturesTable.table(feature.tableId, instance.getConfiguration())+" WRITE, "+TTables.TBL_NAME+" WRITE"
	    				+", "+TImages.TBL_NAME+" WRITE");
	    	  long 		newDataTs = getNewMetaTimestamp(conn);
			  Feature tmpTheme = saveFeature(feature, session, conn);
      		  touchTableData(conn, feature.getTableId(), newDataTs);
	    	  feature.tableDataTs = newDataTs;

			  conn.commit();
			  commited=true;
			   return tmpTheme;	
		  } finally {
			  Statement unlockSt=null;
			  try {
			  	 unlockSt = conn.createStatement();
  				unlockSt.execute("UNLOCK TABLES");
			  } catch (SQLException ex) {
				  
			  }finally {
				 DBUtil.close(unlockSt);
			  }
			  DBUtil.close(lockSt);
			  if (!commited) DBUtil.rollBack(conn);
			  DBUtil.close(conn);
		  }
	    }
	
	  
	public void lockFeatureTables(int[] tableIds, Connection conn, InstanceConfiguration instanceConfiguration) throws SQLException {
		Statement lockSt=null;
		try {
			 lockSt = conn.createStatement();
			 String sql = "";
			 for (int tableId:tableIds) {
				 if (sql.length()>0) sql+=", ";
				 sql+=FeaturesTable.table(tableId, instanceConfiguration)+" WRITE";
				 if (sql.length()>0) sql+=", ";
				 sql+=FeaturesTable.historyTable(tableId, instanceConfiguration)+" WRITE";
			 }
			 lockSt.execute("LOCK TABLES "+sql);
		} finally {
			DBUtil.close(lockSt);
		}
	}
	public Feature saveFeature (Feature feature, Session session, Connection conn) throws SQLException, GeopediaException {
		User user = session.getUser();
		Table featureTable = loadTable(feature.tableId, conn);
    	UserAccessControl.verifyAccessRights(featureTable, user, Permissions.TABLE_EDITDATA);    	
   		Feature f = saveFeature(feature,featureTable, session, conn);    		
   		return f;
	}
	
	
	public ArrayList<Feature> batchInsertFeatures(ArrayList<Feature> features, Table featureTable,  User user, Connection conn, InstanceConfiguration instanceConfiguration) throws SQLException, GeopediaException {
		
		
		PreparedStatement ps = null;
		try {
		for (Feature feature:features) {
			feature.timestamp = new Date().getTime();// TODO remove when postgres
			Update update = Update.insert(FeaturesTable.table(featureTable.id, instanceConfiguration));
			for (Property<?> vh:feature.properties) {
	    		if (vh!=null && vh instanceof HTMLProperty) {
	    			HTMLProperty wikiHolder = (HTMLProperty)vh;
	    			wikiHolder.setValue(FeatureServiceImpl.modifyHtmlForDisplay(wikiHolder.getRawHtml()));
	    		}
	    	}	    	
	    	feature.fulltext = buildFeatureFullText(feature, conn, instanceConfiguration);
	    	populateFeatureUpdate(update,feature,user);
	    	
	    	if (ps==null) {
	    		
	    		String fieldsSQL = "";
	    		String valuesSQL = "";
	    		for (ValueMod vm:update.getValues()) {
	    			if (fieldsSQL.length()>0) fieldsSQL+=", ";
	    			fieldsSQL+=vm.field;
	    			if (valuesSQL.length()>0) valuesSQL+=", ";
	    			valuesSQL+="?";
	    		}
	    		ps = conn.prepareStatement("INSERT INTO "+update.getTable()+" ("+fieldsSQL+") VALUES ("+valuesSQL+")");
	    	}
	    	int idx=1;
	    	for (ValueMod vm:update.getValues()) {
	    		vm.setValue(ps, idx);
	    		idx++;
	    	}
	    	ps.addBatch();	    	
		}
		int [] rows = ps.executeBatch();
		ResultSet rs = ps.getGeneratedKeys();
		try {
			for (int i=0;i<rows.length;i++) {
				if (rows[i] >0 && rs.next()) {
					features.get(i).id = rs.getInt(1);
				}
			}	
		} finally {
				DBUtil.close(rs);
		}
		} finally {
			DBUtil.close(ps);
		}
		
		return features;
		
	}
	
	
	//TODO: move to utility class when it's cleaned
		private static String getTableStyle (ThemeTableLink ttl, Table table) {
			String style = null;
			if (ttl!=null) {
				style = ttl.getStyle();
			}
			
			if (style==null) 
				return table.getStyle();	
			return style;
		}
	//TODO: fix timestamps for table and theme,
		public FeaturesQueryResults executeQuery(Query query, ServerInstance instance, Connection conn) throws GeopediaException {
			try {
				MetaData metaProvider = instance.getMetaData();
				Table table = metaProvider.getTableByIdData(query.tableId, 0);
				
				
				Theme theme = null;
				ThemeTableLink ttl = null;
				if (query.themeTableLink!=null) {
					theme = metaProvider.getThemeByIdMeta(query.themeTableLink.themeId,0);
					if (theme !=null) {
						ttl = theme.getThemeTable(query.themeTableLink.id);
					}
				}
				long totalExecutionTime = System.currentTimeMillis();
				QueryBuilderNew qBuilder = new QueryBuilderNew(table);
				ScriptableObject sharedScope = instance.getJSSharedScope();
				ExpressionDescriptorTransformer.transform(query.filter, qBuilder, instance.getCRSSettings().getMainCrsId());
				TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();
				int  scale = query.scale;
				if (scale<=0) scale = table.maxscale(tiledCRS);
				Double pixSize = tiledCRS.zoomLevels.worldPerPix(scale);
				
				boolean resolveForeignFields = query.hasOption(Options.NO_FOREIGNREF_RESOLVE)?false:true;
				boolean queryVisible = query.hasOption(Options.VISIBLE);
				boolean sendUserFields = query.hasOption(Options.FLDUSER_ALL);
				
				ReptextEvaluator repTextEvaluator = null;
				HashMap<Integer, ReptextEvaluator> foreignTablesEvaluators = new HashMap<Integer, ReptextEvaluator>();
				
				QueryField userFields[] = null;
				if (sendUserFields) {
					userFields = new QueryField[table.getFields().length];
					for (int i=0;i<userFields.length;i++) {
						//TODO: deleted/invisible/...
						Field field = table.getFields()[i];
						userFields[i]=qBuilder.addBaseTableUserField(field.getId());
						
						if (resolveForeignFields && field.getType() == FieldType.FOREIGN_ID) {
							try {
								Table refdTable = metaProvider.getTableById(field.refdTableId);
								ReptextEvaluator eval = new ReptextEvaluator(refdTable.getRepText(), qBuilder, sharedScope);
								eval.setPrefix(userFields[i].getFieldSQLName(false));//TODO: chained field names for extra deep resolving
								eval.preEvaluate(scale, pixSize);
								foreignTablesEvaluators.put(i, eval);
							} catch (Throwable th) {
								logger.trace(String.format("Failed to preEvaluate repText for  field id=%d in layer id=%d", field.getId(), query.tableId),th);
							}
						}
					}
				}
				QueryField fldId = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_ID);
				QueryField fldEnvMinX=null,fldEnvMinY=null,fldEnvMaxX=null,fldEnvMaxY=null;
				QueryField fldLength=null, fldArea=null, fldCenX=null, fldCenY=null;
				QueryField fldUserId=null, fldDeleted=null;
				QueryField fldMetaTimestamp = null;
				QueryField fldGeometry = null;
				
				try {
					repTextEvaluator =  new ReptextEvaluator(table.getRepText(), qBuilder, sharedScope);
					repTextEvaluator.preEvaluate(scale, pixSize);
				} catch (Throwable th) {
					repTextEvaluator = null;
					logger.trace(String.format("Failed to preEvaluate repText for layer id=%d",query.tableId),th);
				}
				
				if (query.hasOption(Query.Options.FLDMETA_BASE)) {
					fldUserId = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_USER);
					fldDeleted = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_DELETED);
					fldMetaTimestamp = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_TIMESTAMP);
				}
				if (table.getGeometryType().isGeom()) {
					if(query.hasOption(Query.Options.FLDMETA_ENVLENCEN)) {
						fldEnvMinX = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_MINX);
						fldEnvMinY = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_MINY);
						fldEnvMaxX = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_MAXX);
						fldEnvMaxY = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_MAXY);
						if (table.getGeometryType().isLine()) {
							fldLength = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_LENGTH);
						} else 	if (table.getGeometryType().isPolygon()) {
							fldLength = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_LENGTH);
							fldArea = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_AREA);
							fldCenX = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_CENTROIDX);
							fldCenY = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_CENTROIDY);
						}
					}
					if (query.hasOption(Query.Options.FLDMETA_GEOMETRY)) {
						fldGeometry = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_GEOM);
					}

				}
				
				/// style
				com.sinergise.geopedia.db.expressions.StyleEvaluator styleEvaluator = null;
				Symbology staticSymbology = null;
				
				String styleJS = getTableStyle(ttl,table);
				if (!StringUtil.isNullOrEmpty(styleJS)) {
					try {
						styleEvaluator = new com.sinergise.geopedia.db.expressions.StyleEvaluator(styleJS, qBuilder, sharedScope);
						styleEvaluator.preEvaluate(scale, pixSize);
						
						if (!styleEvaluator.hasExternalIdentifiers()) {
							staticSymbology = styleEvaluator.evaluate(null);
						}
					} catch (Throwable th) {
						styleEvaluator=null;
						logger.trace(String.format("Failed to evaluate style for layer id=%d.",query.tableId),th);
					}
				}

				
				///
				
				String orderBySQL=null;
				if (query.hasOrder()) {
					orderBySQL="";
					for (Query.OrderHolder oh:query.getOrder()) {
						if (orderBySQL.length()>0) orderBySQL+=", ";
						String fldName = null;
						if (oh.element instanceof TableMetaFieldDescriptor) {
							fldName = ExpressionDescriptorTransformer.propertyDescriptorToSQLField((TableMetaFieldDescriptor) oh.element);
						} else if (oh.element instanceof FieldDescriptor) {
							fldName =ExpressionDescriptorTransformer.fieldDescriptorToSQLField((FieldDescriptor) oh.element);
						}
						qBuilder.addField(fldName, false);
						orderBySQL+=fldName;
						switch (oh.by) {
							case ASC:
								orderBySQL+=" ASC";
								break;
							case DESC:
								orderBySQL+=" DESC";
								break;
						}
						
					}
					if (orderBySQL!=null) {
						orderBySQL = " ORDER BY "+orderBySQL;
					}
				}
					
				ArrayList<Object> parameters = qBuilder.getParameters();
				
				String sql = qBuilder.buildSQL(instance.getConfiguration()) + qBuilder.getCondition().buildSQL();
				if (orderBySQL!=null) {
					sql+=orderBySQL;
				}
				
				boolean hasStartStopIdx = query.hasStartStopIndexes();
				if (query.hasOption(Options.VISIBLE)) {
					hasStartStopIdx = false;
				}
				if (hasStartStopIdx) {
					sql+=" LIMIT ?,?";
				}

				ArrayList<Feature> features = new ArrayList<Feature>();
				boolean hasMoreData = false;
				LoggableStatement ls = null;
				ResultSet rs = null;
				long totalCount = Long.MIN_VALUE;;
				long queryExecutionTime = System.currentTimeMillis();
				if (query.hasOption(Options.TOTALCOUNT) || query.hasOption(Options.COUNTONLY)) {
					try {
						ls = new LoggableStatement(conn, "SELECT COUNT(*) "+qBuilder.buildSQL(false, instance.getConfiguration())  + qBuilder.getCondition().buildSQL());
						int paramIdx = 1;
						for (Object arg : parameters) {
							ls.setObject(paramIdx, arg);
							paramIdx++;
						}
						rs = ls.executeQuery();
						rs.next();
						totalCount = rs.getLong(1);
						if (query.hasOption(Options.COUNTONLY)) {
							FeaturesQueryResults qr = new FeaturesQueryResults(new ArrayList<Feature>(), 
									Query.UNDEFINED, Query.UNDEFINED, false);
							qr.table = table.clone(DataScope.ALL);
							qr.totalCount = totalCount;
							return qr;
						}
					} finally {
						DBUtil.close(rs);
						DBUtil.close(ls);
					}
				}
				
				FeaturesQueryResults.UnresolvedReferencesMap unresolvedReferenceMap = new FeaturesQueryResults.UnresolvedReferencesMap();

				
				try {
					Context evalContext = Context.enter();
					ls = new LoggableStatement(conn, sql);
					int paramIdx = 1;
					for (Object arg : parameters) {
						ls.setObject(paramIdx, arg);
						paramIdx++;
					}
					long expectedCount = 0;

					if (hasStartStopIdx) {
						ls.setLong(paramIdx, query.startIdx);
						expectedCount = (query.stopIdx - query.startIdx);
						paramIdx++;
						ls.setLong(paramIdx, expectedCount + 1);
					} else if (query.hasOption(Options.VISIBLE) && query.startIdx==0 && query.stopIdx!=0) {
						expectedCount = query.stopIdx; // TODO: +1?
					} else {
						expectedCount = Long.MAX_VALUE;
					}

					rs = ls.executeQuery();
					queryExecutionTime = System.currentTimeMillis()-queryExecutionTime;
					int count = 0;			
					
					Scriptable evalScope = evalContext.newObject(instance.getJSSharedScope());
					Script styleScript = null;
					if (styleEvaluator!=null) {					
						styleScript = styleEvaluator.getPreEvaluatedScript(evalContext);							
					}
					
					WkbReader wkbReader = null;
					if (fldGeometry!=null) {
						wkbReader = new WkbReader(instance.getCRSSettings().getMainCrsId());
					}
					
					Field [] clonedFields = null;
					if (sendUserFields) {
						clonedFields = new Field[table.fields.length];
						for (int i=0;i<clonedFields.length;i++) {
							clonedFields[i] = table.fields[i].clone(DataScope.MEDIUM);
						}
					}
					long styleEvaluationTime = 0;
					long reptextEvaluationTime = 0;
					long failedGeometries = 0;
					while (rs.next()) {
						if (count < expectedCount) {
							PointSymbolizer pointSymbolizer = null;
							// evaluate style
							if (styleEvaluator!=null) {
								try {
									long startTS = System.currentTimeMillis();
									Symbology symbology = staticSymbology;
									if (staticSymbology == null) {
										HashMap<QueryField, Object> resultsMap =
												styleEvaluator.loadIdentifiersFromResultSet(rs);
										styleEvaluator.populateFields(evalScope, evalContext, resultsMap);
										symbology = com.sinergise.geopedia.db.expressions.StyleEvaluator.objectToSimbology(
												styleScript.exec(evalContext, evalScope));
									}
									styleEvaluationTime+=(System.currentTimeMillis()-startTS);
									if (symbology==null || !AbstractSymbologyUtils.hasSymbolizers(symbology)) {
										if (queryVisible) {
											continue;// TODO: finer check (opacity, null symbol,..)
										}
									} else {
										pointSymbolizer = JavaSymbologyUtils.getSymbolizerFromSymbology(table.getGeometryType(), symbology);
									}
								} catch (Throwable th) {
									logger.trace(String.format("Failed to evaluate style for layer id=%d.",query.tableId),th);
								}
							}
							
							Feature feature = new Feature();
							
							feature.fields = clonedFields;
							feature.geomType = table.geomType;
							feature.tableId = table.getId();
							feature.id = rs.getInt(fldId.getFieldSQLName(true));
							feature.properties = new Property<?>[table.fields.length];	
							// Get symbols. Move to GWT?
							if (pointSymbolizer!=null) {
								feature.styleColor = pointSymbolizer.getFill().getRGB();
								feature.styleSymbolId = pointSymbolizer.getSymbolId();
							}
							
							DB.loadFeatureEnvelopeLengthAreaCentroidFromResultSet(feature, rs, 
									fldEnvMinX,fldEnvMinY,fldEnvMaxX,fldEnvMaxY,
									fldCenX, fldCenY,
									fldLength, fldArea);
							
							DB.loadFeatureBaseMetadataFromResultSet(feature, rs, fldUserId, fldDeleted, fldMetaTimestamp);

							if (fldGeometry!=null) {
								try {
									feature.featureGeometry = wkbReader.fromMySqlInternal(rs.getBytes(fldGeometry.getFieldSQLName(true)));
								} catch (Exception ex) {
									logger.trace(String.format("Failed to read geometry for table id=%d feature id=%d!",feature.tableId, feature.getId()),ex);
									failedGeometries++;
								}
								
							}
							
							// evaluate layer reptext
							if (repTextEvaluator!=null) {
								long startTS = System.currentTimeMillis();
								HashMap<QueryField, Object> fValues =
										repTextEvaluator.loadIdentifiersFromResultSet(rs);
								try {
									feature.repText = repTextEvaluator.evaluateCompiled(fValues, evalContext, evalScope);								
									reptextEvaluationTime+=(System.currentTimeMillis()-startTS);
								} catch (Exception ex) {
									logger.trace(String.format("Failed to evaluate reptext for table id=%d!",table.getId()),ex);
									//TODO: write error evaluation to JS?
								}
							}
							
							// load user field
							if (sendUserFields) {
								for (int i=0;i<userFields.length;i++) {	
									QueryField qf = userFields[i];
									if (qf==null) continue;
									Field field = table.getFields()[i];
									feature.properties[i] = DB.readUserFieldFromResultSet(rs, field, qf.getFieldSQLName(true));								
									// evaluate foreign field reptext
									if (field.getType() == FieldType.FOREIGN_ID) {
										if (resolveForeignFields) {
											ReptextEvaluator eval = foreignTablesEvaluators.get(i);
											if (eval!=null) {
												long startTS = System.currentTimeMillis();
												HashMap<QueryField, Object> fValues =
														eval.loadIdentifiersFromResultSet(rs);
												try {
													String reptext = eval.evaluateCompiled(fValues, evalContext, evalScope);
													((ForeignReferenceProperty)(feature.properties[i])).setRepText(reptext);
													reptextEvaluationTime+=(System.currentTimeMillis()-startTS);
												} catch (Exception ex) {
													logger.trace(String.format("Failed to evaluate reptext for foreign field in table id=%d, field id=%d!",table.getId(), field.getId()),ex);
												}
											
											}
										} else {
											ForeignReferenceProperty frp = (ForeignReferenceProperty)feature.properties[i];
											unresolvedReferenceMap.addUnresolvedReference(field.refdTableId, frp.getValue());
										}
									}
								}
							}
							

							features.add(feature);
						}
						count++;
					}
					if (expectedCount > 0 && expectedCount < count)
						hasMoreData = true;
					totalExecutionTime = System.currentTimeMillis()-totalExecutionTime;
					logger.debug(String.format("Feature query: StyleEval: %d ms, ReptextEval: %d ms, Feature Count: %d, "+
							"DB query: %d ms, Total Execution %d ms",
							styleEvaluationTime, reptextEvaluationTime, count, queryExecutionTime, totalExecutionTime));
					if (failedGeometries>0)
					logger.warn(String.format("Warning! Found %d failed geometries for table id=%d!", failedGeometries, query.tableId));

				} finally {
					Context.exit();		
					DBUtil.close(rs);
					DBUtil.close(ls);
				}
				
				FeaturesQueryResults qr = new FeaturesQueryResults(features, query.startIdx, query.startIdx + features.size(), hasMoreData);
				if (unresolvedReferenceMap!=null && unresolvedReferenceMap.hasData()) {
					qr.unresolvedReferencesMap = unresolvedReferenceMap;
				}
				qr.table = table.clone(DataScope.ALL);
				qr.totalCount = totalCount;
				return qr;
			} catch (SQLException ex) {
				logger.error(String.format("Query execution failed on DB for table id=%d.", query.tableId),ex);
				throw QueryException.create(QueryException.Type.DB_QUERY_FAILED);
			} catch (Exception ex) {
				logger.error(String.format("Query execution for table id=%d.", query.tableId),ex);
				throw QueryException.create(QueryException.Type.SERVER_EXCEPTION);
			}
		}
	
	
    public Feature saveFeature(Feature feature, Table featureTable,  Session session, Connection conn) throws SQLException, GeopediaException {
    	    
		ServerInstance instance = session.getServerInstance(); 
		InstanceConfiguration instanceConfig = instance.getConfiguration();
		TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();
		User user = session.getUser();
		
    	FeatureUtils.verifyFeature(feature, user.isAdmin);
    	feature.timestamp = new Date().getTime();// TODO remove when postgres
    	int tableId = featureTable.id;
    	Update update;
    	Feature oldFeature = null;
    	if (feature.hasValidId()) {
    		update = Update.update(FeaturesTable.table(tableId, instanceConfig));
    		update.where(FeaturesTable.id(tableId), feature.id);
    		// REMOVE WHEN POSTGRES!
    		Query query = new Query();
    		query.startIdx=0;
    		query.stopIdx=1;
    		query.options.add(Query.Options.FLDMETA_ENVLENCEN);
    		query.options.add(Query.Options.FLDMETA_BASE);
    		query.options.add(Query.Options.FLDUSER_ALL);
    			query.options.add(Query.Options.FLDMETA_GEOMETRY);	
    		query.tableId = tableId;    		
    		query.filter = FilterFactory.createIdentifierDescriptor(tableId, feature.getId());
    		FeaturesQueryResults result = executeQuery(query, instance, conn);
    		if (result.getCollection().size()==1) {
    			oldFeature=result.getCollection().get(0);
    		}
    		
    	} else {
    		update = Update.insert(FeaturesTable.table(featureTable.id, instanceConfig));    		
    	}
    	
    	for (Property<?> vh:feature.properties) {
    		if (vh!=null && vh instanceof BinaryFileProperty) {
    			storeBinaryValueHolder((BinaryFileProperty)vh, user, conn);
    		} else if (vh!=null && vh instanceof HTMLProperty) {
    			HTMLProperty wikiHolder = (HTMLProperty)vh;
    			wikiHolder.setValue(FeatureServiceImpl.modifyHtmlForDisplay(wikiHolder.getRawHtml()));
    		}
    	}
    	
    	feature.fulltext = buildFeatureFullText(feature, conn, instanceConfig);
    	populateFeatureUpdate(update,feature,user);
    	update.execute(conn);
    	if (!feature.hasValidId()) {
    		feature.id = update.getLastInsertedId();
    	}
    	if (oldFeature!=null)
    		insertFeatureHistory(oldFeature, featureTable, user, conn, instanceConfig);
    	//TODO: store history (should be done at db level anyway)
    	return feature;
    }
    
    
	
	public int storePicture(final int user_id, final String mime, final int width,
			final int height, final byte[] bytes, final String md5, final Long id) throws SQLException {
		// TODO: check for matching md5?
		return dbPoolHolder.executeUpdate(new DBExecutor<Integer>() {
			@Override
			public Integer execute(Connection conn) throws SQLException {
				Update updt;
				if (id==null) {
					updt = Update.insert(TImages.TBL_NAME);
				} else {
					updt = Update.update(TImages.TBL_NAME).where(TImages.ID, id.intValue());
				}
				
				updt.set(TUsers.ID, user_id)
				.set(TImages.MIME, mime)
				.set(TImages.WIDTH,width)
				.set(TImages.HEIGHT, height)
				.set(TImages.BYTES, bytes)
				.set(TImages.BYTESMD5, md5);
				int newId = updt.execute(conn);
				if (id!=null) {
					return Integer.valueOf(id.intValue());
				}
				return Integer.valueOf(newId);
			}
		}).intValue();
	}
	
    
    //TODO: revise behavior
    private static void storeBinaryValueHolder(BinaryFileProperty binaryHolder, User user,  Connection conn) throws SQLException {
    	if (binaryHolder.isDeleted()) { // delete
    		Update upd = Update.delete(TImages.TBL_NAME).where(TImages.ID,binaryHolder.getValue().intValue());
    		upd.execute(conn);
    		binaryHolder.setValue(null);
    	} 
    	if (binaryHolder.hasFileToken()) {
    		try  {
    			BufferedImage img = FileUploadServiceImpl.INSTANCE.getSizedImage(binaryHolder.getFileToken(), 1280, 720);
    			if (img!=null) {
	    			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    			ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
	    			ImageIO.write(img, "jpg", ios);
	    			byte[] bytes = baos.toByteArray();
	    			int id = Update.insert(TImages.TBL_NAME)
	    				.set(TUsers.ID, user.getId())
	    				.set(TImages.MIME, MimeType.MIME_IMAGE_JPG.toString())
	    				.set(TImages.WIDTH,img.getWidth())
	    				.set(TImages.HEIGHT,img.getHeight())
	    				.set(TImages.BYTES, bytes)
	    				.set(TImages.BYTESMD5, MD5.hash32(bytes))
	 					.execute(conn);
	    			if (id>0) {
	    				binaryHolder.setValue(Long.valueOf(id));
	        			FileUploadServiceImpl.INSTANCE.finishedWithItem(binaryHolder.getFileToken());
	    				binaryHolder.setFileToken(null);
	    			}
    			}
    		}catch (IOException ex) {
    			ex.printStackTrace();
    		}
    	}
    }
    
    private static void insertFeatureHistory(Feature feature, Table featureTable, User user, Connection conn, InstanceConfiguration instanceConfiguration) throws SQLException {
    	int tableId = featureTable.getId();
    	Update update = Update.insert(FeaturesTable.historyTable(tableId, instanceConfiguration));
    	update.set(FeaturesTable.id(tableId), feature.getId());
    	if (GeomType.isGeom(feature.geomType)) {
    		update.set(FeaturesTable.geometry(tableId),feature.featureGeometry);
    	}
    	populateFeatureFieldsUpdate(update,feature);
    	update.set(FeaturesTable.timestamp(tableId), new Timestamp(feature.timestamp));
    	update.set(FeaturesTable.deleted(tableId), feature.deleted); 
		update.set(FeaturesTable.user(tableId), user.getId());
		update.execute(conn);
    }
    
    // TODO: move to saveFeature when history is properly implemented at database level
    private static void populateFeatureUpdate (Update update, Feature feature, User user) {
    	int tableId = feature.tableId;
    	// geometry and related metafields
    	if (GeomType.isGeom(feature.geomType)) {
    		Geometry geom = feature.featureGeometry;
    		update.set(FeaturesTable.geometry(tableId),geom);

    		Envelope e = geom.getEnvelope();
    		update.set(FeaturesTable.minX(tableId), e.getMinX());
    		update.set(FeaturesTable.minY(tableId), e.getMinY());
    		update.set(FeaturesTable.maxX(tableId), e.getMaxX());
    		update.set(FeaturesTable.maxY(tableId), e.getMaxY());

    		if (feature.geomType.isPolygon()) {
    			update.set(FeaturesTable.area(tableId), geom.getArea());
    			update.set(FeaturesTable.length(tableId), geom.getLength());

    			double[] cent = CentroidFinder.calc(geom);
    			if (cent == null)
    				cent = new double[] { 0.5 * (e.getMinX() + e.getMaxX()),
    						0.5 * (e.getMinY() + e.getMaxY()) };

    			update.set(FeaturesTable.centroidX(tableId), cent[0]);
    			update.set(FeaturesTable.centroidY(tableId), cent[1]);
    		} else if (feature.geomType.isLine()) {
    			update.set(FeaturesTable.length(tableId), geom.getLength());
    		}
    	}   
    	
    	
    	populateFeatureFieldsUpdate(update,feature);
    	update.set(FeaturesTable.timestamp(tableId), new Timestamp(feature.timestamp));
    	update.set(FeaturesTable.fullText(tableId), feature.fulltext);    	
    	update.set(FeaturesTable.deleted(tableId), feature.deleted); 
		update.set(FeaturesTable.user(tableId), user.getId());
    }
    
    
    private static void populateFeatureFieldsUpdate(Update update, Feature feature) {
    	if (feature.fields != null && feature.fields.length >0) 
    	{
    		GregorianCalendar gc = null;
    		
    		for (int a = 0; a < feature.fields.length; a++) {
    			Field f = feature.fields[a];
    			Property<?> vh = feature.properties[a];
    			String field = FeaturesTable.userField(f);

				if (vh == null) {
					update.set(field, (String) null);
					continue;
				}

				switch (f.type) {
				// TODO:fix
				case BLOB:
					update.set(field, ((BinaryFileProperty) vh).getValue());
					break;
				case BOOLEAN:
					update.set(field, ((BooleanProperty) vh).getValue() ? 1 : 0);
					break;
				case DATE: {
					java.sql.Date sqlDate = null;
					DateProperty vhLong = (DateProperty)vh;
					if (vhLong.getValue()!=null) {
						if (gc == null)
							gc = new GregorianCalendar();
		
						gc.setTimeInMillis(vhLong.getValue().getTime());
						gc.set(GregorianCalendar.HOUR_OF_DAY, 12);
						gc.set(GregorianCalendar.MINUTE, 0);
						gc.set(GregorianCalendar.SECOND, 0);
						gc.set(GregorianCalendar.MILLISECOND, 0);
						sqlDate =  new java.sql.Date(gc.getTimeInMillis());
					}
					update.set(field, sqlDate);
					break;
				}
				case DATETIME: {
					DateProperty dateProperty = (DateProperty)vh;
					if (PropertyUtils.isNull(dateProperty)) {
						update.set(field, (Timestamp)null);
					} else {						
						if (gc == null)
							gc = new GregorianCalendar();
		
						gc.setTimeInMillis(dateProperty.getValue().getTime());
						gc.set(GregorianCalendar.MILLISECOND, 0);
		
						update.set(field, new Timestamp(gc.getTimeInMillis()));
					}
					break;
				}
				case DECIMAL:					
					update.set(field, ((DoubleProperty) vh).getValue());
					break;
				case FOREIGN_ID:
					update.set(field, ((ForeignReferenceProperty) vh).getValue());
					break;
				case INTEGER:
					update.set(field, ((LongProperty) vh).getValue());
					break;
				case LONGPLAINTEXT:
				case PLAINTEXT:
				case STYLE:
					update.set(field, ((TextProperty) vh).getValue());
					break;
				case WIKITEXT:
					HTMLProperty wikiHolder = ((HTMLProperty)vh);
					wikiHolder.setValue(FeatureServiceImpl.modifyHtmlForDisplay(wikiHolder.getRawHtml()));
					update.set(field, wikiHolder.getRawHtml());
					break;
				default:
					throw new IllegalStateException();
				}
    		}
		}
    }

	public ArrayList<ForeignReferenceProperty> getFeaturesReptext(
			final int tableId, User user, final ServerInstance instance) throws SQLException {
    	return dbPoolHolder.executeLocal(new DBExecutor<ArrayList<ForeignReferenceProperty>>() {
			public ArrayList<ForeignReferenceProperty> execute(Connection conn) throws SQLException {
				Table table = meta.getTableById(tableId);
				
				QueryBuilderNew qBuilder = new QueryBuilderNew(table);
				ScriptableObject sharedScope = instance.getJSSharedScope();
				ArrayList<ForeignReferenceProperty> list = new ArrayList<ForeignReferenceProperty> ();

				ReptextEvaluator repTextEvaluator = null;

				repTextEvaluator =  new ReptextEvaluator(table.getRepText(), qBuilder, sharedScope);
				repTextEvaluator.preEvaluate(null,null);
				
				LoggableStatement ls = null;
				ResultSet rs = null;
				QueryField fldId = qBuilder.addBaseTableMetaField(FeaturesTable.FLD_ID);
				ConditionBuilder cb = qBuilder.getCondition();
				if (cb.size()!=0) 
					cb.append(" AND ");
				cb.append(qBuilder.addBaseTableMetaField(FeaturesTable.FLD_DELETED));
				cb.append("=0");
				String sql = qBuilder.buildSQL(instance.getConfiguration()) + cb.buildSQL();
				ArrayList<Object> parameters = qBuilder.getParameters();
				try {
					Context evalContext = Context.enter();
					ls = new LoggableStatement(conn, sql);
					int paramIdx = 1;
					for (Object arg : parameters) {
						ls.setObject(paramIdx, arg);
						paramIdx++;
					}
					rs = ls.executeQuery();
					Scriptable evalScope = evalContext.newObject(instance.getJSSharedScope());
					Script	repTextScript = repTextEvaluator.getCompiledScript(evalContext);							
					while (rs.next()) {
						HashMap<QueryField, Object> fValues =
								repTextEvaluator.loadIdentifiersFromResultSet(rs);
						repTextEvaluator.populateFields(evalScope, evalContext, fValues);
						String repText = ReptextEvaluator.objectToString(repTextScript.exec(evalContext, evalScope));
						long id = rs.getLong(fldId.getFieldSQLName(true));
						ForeignReferenceProperty frp = new ForeignReferenceProperty(id, repText);
						list.add(frp);
					}
					return list;
				  } finally {
					  	Context.exit();		
			        	DBUtil.close(rs);
			        	DBUtil.close(ls);  	
			        }
			}
    	});
	}
	
	
	// ----- tables
	
	
	 public Table saveTable(final Table table, final Session session) throws GeopediaException, SQLException {
	    	
    	 Connection conn = null;
    	 boolean commited=false; // silly mysql
		  try {
			  conn = dbPoolHolder.getUpdate();
			  conn.setAutoCommit(false);
			  Table updatedTable = saveTable(table, session, conn);
			  conn.commit();
			  commited=true;
			  // TODO: fix this
			  if (updatedTable.isDeleted()) {
				  //meta.refresh();
			  }
			  return updatedTable;
		  } finally {
			  if (!commited) DBUtil.rollBack(conn);
			  DBUtil.close(conn);
		  }
    }
	
	 
	private void deleteTable(Table table, Session session, Connection conn) throws GeopediaException, SQLException {
		validateTableDeletion(table, conn);
		// remove permissions
		Update.delete(TPermissions.TBL_NAME)
			.where(TPermissions.OBJ_ID, table.getId())
			.where(TPermissions.OBJ_TYPE, GeopediaEntity.TABLE.getId())
			.execute(conn);
		// remove from user tables
		Update.delete(TUserTables.TBL_NAME)
			.where(TUserTables.TABLE_ID, table.getId())
			.execute(conn);
		
		
		ArrayList<Integer> themeIdList = new ArrayList<Integer>();
		LoggableStatement ls =null;
		ResultSet rs = null;
		try {
			
			ls = new LoggableStatement(conn,"SELECT DISTINCT "+TThemeTables.THEME_ID+" FROM " 
											+ TThemeTables.TBL_NAME+" WHERE "+TThemeTables.TABLE_ID+"=?");
			ls.setInt(1, table.getId());
			rs = ls.executeQuery();
			while (rs.next()) {
				themeIdList.add(rs.getInt(1));
			}
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);
		}

		// remove all theme tables that have this table
		Update.delete(TThemeTables.TBL_NAME)
			.where(TTables.ID, table.getId())
			.execute(conn);

		if (themeIdList.size()>0) {
			long newMetaTS = getNewMetaTimestamp(conn);
			try {
				ls = new LoggableStatement(conn, "UPDATE "+TThemes.TBL_NAME+" SET "+TThemes.LASTMETACHANGE+"=? WHERE "
						+TThemes.ID+" IN ("+StringUtil.collectionToString(themeIdList, ",")+")");
				ls.setLong(1, newMetaTS);
				ls.executeUpdate();			
			} finally {
				DBUtil.close(ls);
			}
		}
		// this won't work in current setup because it reads from local slave which is not updated before commit happens
//		for (Integer themeId:themeIdList) {
//			meta.getThemeByIdMeta(themeId, newMetaTS);
//		}

		
	}
	public Table saveTable(Table table, Session session, Connection conn) throws GeopediaException, SQLException {
		User user = session.getUser();
		ServerInstance instance = session.getServerInstance(); 
		TiledCRS tiledCRS = instance.getCRSSettings().getMainCRS();

		
		 // TODO: deprecate this when html is replaced
		 try {
			 if (!StringUtil.isNullOrEmpty(table.descRawHtml)) {
				 
				 if (GeopediaServerUtility.isRawHTML(table.descRawHtml)) {
					 if (!session.getUser().isAdmin())  throw TableDataException.create(TableDataException.Type.ILLEGAL_DESCRIPTION);
			 	 } else {
			 		 table.descRawHtml= HtmlCleaner.newDefaultInstance().cleanToString(table.descRawHtml, false);
				 }
			 }
         } catch (Exception e) {
        	 logger.warn("Failed to clean description HTML!",e);
        	 throw TableDataException.create(TableDataException.Type.ILLEGAL_DESCRIPTION);
         }
		
		 
        //TODO: style, reptext validation should occur in gwt when styles are replaced
		Table origTable = null;
		Update update;
		boolean newTable=false;
		Integer reptextField = null;
		if (table.hasValidId()) {
			origTable = meta.getTableByIdMeta(table.getId(), table.lastMetaChange);
			if (table.isDeleted()) {
				deleteTable(origTable, session, conn);			
				for (Field f:table.getFields()) {  // mark all fields as deleted
					f.setDeleted(true);
				}
			}
			update = Update.update(TTables.TBL_NAME)
						   .where(TTables.ID, table.getId());
			
    		if (!StringUtil.isNullOrEmpty(table.getStyle())) {
    			logger.warn("TODO: implement some kind of style verification.");
    		}
	            
		} else { // creating a new table
    		update = Update.insert(TTables.TBL_NAME);    		
    		newTable = true;
    		if (!StringUtil.isNullOrEmpty(table.getStyle())) {
    			logger.warn("TODO: implement some kind of style verification.");
    		}
    	}

		// reptext could have changed to a newly added field
		reptextField = Table.getSingleFieldReptextId(table.getRepText());
		if (reptextField!=null && reptextField<0) { // remove expression for now (we need actual field ID)
			table.setRepText(null);
		}

		
        if (table.getRepText() != null && origTable!=null) {
        	logger.warn("TODO: validate reptext!");
        }
		
        
        MetaServiceImpl.createDispHTML(table);
		table.lastDataWrite = table.lastMetaChange; // TODO: this is an ugly hack to force publicrenderer to repaint (style change)
		update.set(TTables.NAME, table.getName())
			  .set(TTables.DESC, table.descRawHtml)
			  .set(TTables.DELETED, table.isDeleted())
			  .set(TTables.GEOMTYPE, table.geomType.getIdentifier())
			  .set(TTables.PUBLICPERMS,	table.public_perms)
			  .set(TTables.REPTEXTJS, table. getRepText())
			  .set(TTables.STYLEJS, table.getStyle())
			  .set(TTables.PROPERTIES,	stateToString(table.properties))
			  .set(TTables.LASTMETACHANGE, table.lastMetaChange)
			  .set(TTables.LASTDATAWRITE, table.lastDataWrite);
				
		if (table.envelope!=null) {
			update.set(TTables.MINX, table.envelope.getMinX())
				  .set(TTables.MINY, table.envelope.getMinY())
				  .set(TTables.MAXX, table.envelope.getMaxX())
				  .set(TTables.MAXY, table.envelope.getMaxY());
		}
		
		update.execute(conn);
		if (!table.hasValidId()) {
			table.setId(update.getLastInsertedId());
		}
		
		
		  
		  
		  ArrayList<Field> toProcess = new ArrayList<Field>();
		  // find deleted fields
		  if (origTable!=null) {
			  for (Field field:origTable.fields) {
				  if (!ArrayUtil.contains(table.fields, field)) {
					  validateFieldDeletion(field, conn);
					  field.setDeleted(true);
					  toProcess.add(field);
				  }
			  }
		  }
		
		  ArrayUtil.addTo(toProcess, table.fields);
		  
		  ArrayList<Field> newFields = new ArrayList<Field>();
		  for (Field field:toProcess) {
			  Update fldUpdate;
			  if (field.hasValidId()) {
				  fldUpdate = Update.update(TFields.TBL_NAME)
				  					.where(TFields.ID, field.getId());
			  } else {
				  fldUpdate = Update.insert(TFields.TBL_NAME);
			  }
			  
			  fldUpdate.set(TFields.DEFAULTVALUE, field.defaultValueString)
			  			.set(TFields.DESC, field.descRawHtml)
			  			.set(TFields.FLAGS, field.getFlags())
			  			.set(TFields.NAME, field.getName())
						.set(TFields.ORDER, field.order)
						.set(TFields.TYPE, field.type.getIdentifier())
						.set(TFields.REFD_TABLE_ID, field.refdTableId)
						.set(TTables.ID, table.getId())
						.set(TFields.PROPERTIES, stateToString(field.properties));
			  fldUpdate.execute(conn);
			  if (!field.hasValidId()){
				  int actualId = fldUpdate.getLastInsertedId();
				  if (reptextField!=null && reptextField<0 && reptextField==field.getId()) {
					  reptextField=actualId;
				  }
				  field.setId(actualId);
				  newFields.add(field);
			  }
				  
		  }
		  
		  if (reptextField!=null) { // update reptext if necessary
			  if (reptextField>0) { 
				  table.setRepText("f"+reptextField);
				  Update tableUpdate = Update.update(TTables.TBL_NAME)
						   .where(TTables.ID, table.getId());
				  tableUpdate.set(TTables.REPTEXTJS, table.getRepText());
				  tableUpdate.execute(conn);
			  } else {
				  throw TableDataException.create(TableDataException.Type.REPTEXT_ERROR);
			  }
		  }
		  
		  if (newTable) {
			  // full control over the table for the creator
			  setPermissions(GeopediaEntity.TABLE, table.getId(),  Permissions.A_USER, user.getId(), Permissions.TABLE_ADMIN, conn);
			  user.forcePrivilegesReload();
			  // add table to user tables
			  modifyPersonalEntity(GeopediaEntity.TABLE, table.getId(), user.getId(), PersonalGroup.PERSONAL, false, conn);			  
		  }
		  
		  if (newTable) {
			  DatabaseStructures.createTable(table, conn, instance.getConfiguration());
		  } else {
			  DatabaseStructures.alterTable(table, newFields, conn, instance.getConfiguration());
		  }
		  
		  
		  table.lastMetaChange = getNewMetaTimestamp(conn);
		  table.lastDataWrite = table.lastMetaChange;
		  touchTableMeta(conn, table.getId(), table.lastMetaChange);
		  
		  if (table.isDeleted()) { // TODO: fix this?
			  meta.deleteTable(table.getId());      		
      		// remove table from translations
      		{
      			getTranslationService().deleteTable(table.getId());
              	TranslationServiceImpl.TRANSLATION_TAG();
      		}
		  }
		return table;
	}
	
	
	private static String fieldMatch(String columnName, int fieldId) {
		return
		columnName+" LIKE 'f"+fieldId+"'" + " OR "+
		columnName+" LIKE 'f"+fieldId+"_%'" +" OR "+
		columnName+" LIKE '%_f"+fieldId+"'" +" OR "+
		columnName+" LIKE '%_f"+fieldId+"_%'";
	}
	
	private static void validateFieldDeletion(final Field field, Connection conn) throws SQLException, GeopediaException {
		if (field==null || !field.hasValidId() || field.isDeleted())
			return;
		LoggableStatement ls = null;
		ResultSet rs = null;
		try {
			ls = new LoggableStatement(conn, "SELECT " + TTables.ID + " FROM " + TTables.TBL_NAME + 
					" WHERE (" + fieldMatch(TTables.STYLEJS, field.id)+") AND "+TTables.ID+" != ?");
			ls.setInt(1, field.getTableId());
			rs = ls.executeQuery(); 
			if (rs.next()) {
				int referencingTableId =  rs.getInt(TTables.ID);
				logger.warn("Failed to delete field id="+field.getId()+" from table id="+field.getTableId()+ " because it has style reference in table id="+referencingTableId);
				throw TableDataException.create(TableDataException.Type.FIELD_HAS_STYLE_REFERENCE, field,  referencingTableId);
			}
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);			
		}
		try {
			ls = new LoggableStatement(conn, "SELECT " + TTables.ID + " FROM " + TTables.TBL_NAME + " WHERE (" 
					+ fieldMatch(TTables.REPTEXTJS, field.id) +") AND "+TTables.ID+" != ?");
			ls.setInt(1, field.getTableId());
			rs = ls.executeQuery();
			if (rs.next()) {
				int referencingTableId =  rs.getInt(TTables.ID);
				logger.warn("Failed to delete field id="+field.getId()+" from table id="+field.getTableId()+ " because it has reptext reference in table id="+referencingTableId);
				throw TableDataException.create(TableDataException.Type.FIELD_HAS_REPTEXT_REFERENCE, field,  referencingTableId);
			}
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);			
		}								
	}
	
	
	
	private static void validateTableDeletion(Table table, Connection conn) throws SQLException, GeopediaException {

		if (table==null || !table.hasValidId())
			return;
		LoggableStatement ls = null;
		ResultSet rs = null;
		
		try {
			ls = new LoggableStatement(conn, "SELECT " + TFields.TABLE_ID + ", " + TFields.ID + ", " + TFields.NAME 
							+ " FROM " + TFields.TBL_NAME + " WHERE " + TFields.REFD_TABLE_ID + "=" + table.getId()
							+" AND "+TFields.FLAGS+"&"+FieldFlags.DELETED.value+"=0 AND "+TFields.TABLE_ID+" != ?");
			ls.setInt(1, table.getId());
			rs = ls.executeQuery();
			if (rs.next()) {
				int referencingTableId =  rs.getInt(TFields.TABLE_ID);
				logger.warn("Failed to delete table id="+table.getId()+"  because it is referenced by table id="+referencingTableId);
				throw TableDataException.createTableIsReferencedByTable(referencingTableId);
			}
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);			
		}
		if (table.fields != null && table.fields.length > 0) {
			for (Field field : table.fields) {
				if (field != null && field.id > 0 && (!field.isDeleted())) {
					validateFieldDeletion(field, conn);
				}
			}
		}
	}
	
	
		
	public UserPermissions loadUserPermissions(final int userId)  throws SQLException {
		return dbPoolHolder.executeLocal(new DBExecutor<UserPermissions>() {
			public UserPermissions execute(Connection conn) throws SQLException {
				return loadUserPermissions(userId, conn);
			}
		});
	}
	
	private static UserPermissions loadUserPermissions(int userId, Connection conn) throws SQLException {
		String sql = "SELECT "+TPermissions.OBJ_TYPE+","+ TPermissions.OBJ_ID+","+TPermissions.ALLOWED_LEVEL+" FROM "+TPermissions.TBL_NAME+"  WHERE "
				+TPermissions.ALLOWED_TYPE+"="+Permissions.A_USER+" AND "+TPermissions.ALLOWED_ID+"=? "
				+" OR "
				+TPermissions.ALLOWED_TYPE+"="+Permissions.A_GROUP+" AND "+TPermissions.ALLOWED_ID+" IN ("
				+"SELECT "+TUserGroups.GROUP_ID+" FROM "+TUserGroups.TBL_NAME+" WHERE "+TUserGroups.USER_ID+"=?)";
		LoggableStatement ls = null;
		ResultSet rs = null;
		UserPermissions perms = new UserPermissions();
		try {
			ls = new LoggableStatement(conn, sql);
			ls.setInt(1, userId);
			ls.setInt(2, userId);
			rs = ls.executeQuery();
			while (rs.next()) {
				int objType = rs.getInt(1);
				int objId = rs.getInt(2);
				int allowedLevel = rs.getInt(3);
				if (objType > GeopediaEntity.CATEGORY.getId()) { // TODO!!!!!!!!!!!!!!!!!!!   replace silly permissions in database then delete this!!!!!!!!!!!!!
					objId=objType;
					objType = GeopediaEntity.GENERAL.getId();
				}
				HashMap<Integer,Integer> entityPerms = perms.getPermissionsFor(objType);
				entityPerms.put(objId, allowedLevel);
			}
			return perms;
		} finally {
			DBUtil.close(rs);
			DBUtil.close(ls);			
		}
	}
	
	private static Long2IntOpenHashMap readUserPermissions(int userId, Connection conn)
			throws SQLException {
		Long2IntOpenHashMap result = new Long2IntOpenHashMap();
		result.defaultReturnValue(-1);
		
		PreparedStatement ps = conn.prepareStatement("SELECT " + TPermissions.TBL_NAME + ".* FROM " + TUserGroups.TBL_NAME
			+ ", " + TPermissions.TBL_NAME + " WHERE " + TUserGroups.TBL_NAME + "." + TGroups.ID + " = " + TPermissions.TBL_NAME + "."
			+ TPermissions.ALLOWED_ID + " AND " + TPermissions.TBL_NAME + "." + TPermissions.ALLOWED_TYPE + "=" + Permissions.A_GROUP
			+ " AND " + TUserGroups.TBL_NAME + "." + TUsers.ID + "=?");
		try {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			try {
				while (rs.next()) {
					int obj_type = rs.getInt(TPermissions.OBJ_TYPE);
					int obj_id = rs.getInt(TPermissions.OBJ_ID);
					int level = rs.getInt(TPermissions.ALLOWED_LEVEL);
					
					long ququ = (((long) obj_type) << 32) | obj_id;
					
					int existing = result.get(ququ);
					if (result.get(ququ) == -1) {
						result.put(ququ, level);
					} else {
						if (level < existing) result.put(ququ, level);
					}
				}
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
		
		ps = conn.prepareStatement("SELECT * FROM " + TPermissions.TBL_NAME + " WHERE " + TPermissions.ALLOWED_ID + "=? AND "
			+ TPermissions.ALLOWED_TYPE + "=" + Permissions.A_USER);
		try {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			try {
				while (rs.next()) {
					int obj_type = rs.getInt(TPermissions.OBJ_TYPE);
					int obj_id = rs.getInt(TPermissions.OBJ_ID);
					int level = rs.getInt(TPermissions.ALLOWED_LEVEL);
					
					long ququ = (((long) obj_type) << 32) | obj_id;
					
					result.put(ququ, level);
				}
			} finally {
				DBUtil.close(rs);
			}
		} finally {
			DBUtil.close(ps);
		}
		
		return result;
	}
	
	
	public static void main(String[] args) {
	/*
	 * try {
			
			Table tbl = DB.getMetaProvider().getTableById(5721);
			
			TextRepEvaluator tre;
			ArrayList<ValueHolderForeignReference> list = new ArrayList<ValueHolderForeignReference> ();
        	try {
        		HashSet<FieldPath> neededFields = new HashSet<FieldPath>();
        		StringSpec sspec = TableUtils.getTextRepSpec(tbl);
        		if (sspec==null) {
        			sspec = TableUtils.defaultTextRep(tbl);
        		}
				tre = new TextRepEvaluator(sspec, tbl);
				tre.getNeededFields(neededFields);
				FieldPath idFldPath = new MetaFieldPath(new TablePath(tbl.getId()), MetaFieldPath.MF_ID);
				neededFields.add(idFldPath);
				FieldPath deletedFldPath = new MetaFieldPath(new TablePath(tbl.getId()), MetaFieldPath.MF_DELETED);					
				neededFields.add(deletedFldPath);
				
		        QueryPreResult qpr = QueryBuilder.preQuery(tbl, neededFields);
		        System.out.println(qpr.sqlSoFar());
		        
        	} catch (Throwable th) {
        		th.printStackTrace();
        	}
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
}
