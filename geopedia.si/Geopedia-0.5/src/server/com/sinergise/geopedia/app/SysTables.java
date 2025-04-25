package com.sinergise.geopedia.app;


import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.db.DBExecutor;
import com.sinergise.geopedia.db.DBPoolHolder;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.geopedia.db.TableAndFieldNames.TFields;
import com.sinergise.geopedia.db.TableAndFieldNames.TTables;

public class SysTables
{
	public static final String SYSTABLE_REZI = "rezi";
	public static final String SYSFIELD_REZI_IME = "rezi/ime";
	public static final String SYSFIELD_REZI_MID = "rezi/mid";
    
	public static final String SYSTABLE_NASELJA = "naselja";
	public static final String SYSFIELD_NASELJA_IME = "naselja/ime";
    public static final String SYSFIELD_NASELJA_OBCINA = "naselja/obcina";
    
    public static final String SYSTABLE_OBCINE = "obcine";
    public static final String SYSFIELD_OBCINE_IME = "obcine/ime";
	
	public static final String SYSTABLE_ULICE = "ulice";
	public static final String SYSFIELD_ULICE_IME = "ulice/ime";
	public static final String SYSFIELD_ULICE_NASELJE = "ulice/naselje";
	
	public static final String SYSTABLE_ULICEBG = "ulicebg";
	public static final String SYSFIELD_ULICEBG_IME = "ulicebg/ime";
	public static final String SYSFIELD_ULICEBG_NASELJE = "ulicebg/naselje";
	public static final String SYSFIELD_ULICEBG_ULICE = "ulicebg/ulice";

	public static final String SYSTABLE_HISNEST = "hisnest";
	public static final String SYSFIELD_HISNEST_ULICA = "hisnest/ulica";
	public static final String SYSFIELD_HISNEST_ST = "hisnest/st";
	public static final String SYSFIELD_HISNEST_DOD = "hisnest/dod";
	public static final String SYSFIELD_HISNEST_POSTA = "hisnest/posta";
	public static final String SYSFIELD_HISNEST_NASELJE = "hisnest/naselje";
	
	public static final String SYSTABLE_POSTE = "poste";
	public static final String SYSFIELD_POSTE_PTST = "poste/ptst";
	public static final String SYSFIELD_POSTE_IME = "poste/ime";

	public static final String SYSTABLE_POSTEGR = "posteGraf";
	public static final String SYSFIELD_POSTEGR_ID = "posteGraf/ptst";

	
	public static final Logger logger = LoggerFactory.getLogger(SysTables.class);

	
	public SysTables(DBPoolHolder dbPoolHolder) {
		doInit(dbPoolHolder);
	}
	
	private Object2ObjectArrayMap<String, SysTableInfo> sysIdToTable;
	
	public int getSysTableId(String key)
	{
		SysTableInfo sti = sysIdToTable.get(key);
		if (sti == null)
			return -1;
		
		return sti.tableId;
	}
	
	public int getSysFieldId(String tableKey, String fieldKey)
	{
		return sysIdToTable.get(tableKey).getFieldId(fieldKey);
	}
	
	private void doInit(DBPoolHolder dbPoolHolder)
	{
		try {
			logger.info("Reading sys tables");
			
			dbPoolHolder.executeLocal(new DBExecutor<Void>() {
				public Void execute(Connection conn) throws SQLException {
					Object2ObjectArrayMap<String, SysTableInfo> sysIdToTable = new Object2ObjectArrayMap<String, SysTableInfo>();
					Int2ObjectArrayMap<SysTableInfo> tableIdToTable = new Int2ObjectArrayMap<SysTableInfo>();
					
					PreparedStatement ps = conn.prepareStatement("SELECT * FROM "+TTables.TBL_NAME+" WHERE "+TTables.DELETED+"=0 AND "+TTables.SYSID+" IS NOT NULL ORDER BY "+TTables.ID);
					try {
						ResultSet rs = ps.executeQuery();
						try {
							while (rs.next()) {
								String sysid = rs.getString(TTables.SYSID);
								int tableId = rs.getInt(TTables.ID);
								
								SysTableInfo sti = new SysTableInfo(tableId);
								sysIdToTable.put(sysid, sti);
							}
						} finally {
							DBUtil.close(rs);
						}
					} finally {
						DBUtil.close(ps);
					}
					
					for (Map.Entry<String, SysTableInfo> e : sysIdToTable.entrySet())
						tableIdToTable.put(e.getValue().tableId, e.getValue());
					
					ps = conn.prepareStatement("SELECT * FROM "+TFields.TBL_NAME+" WHERE "+TFields.SYSID+" IS NOT NULL");
					try {
						ResultSet rs = ps.executeQuery();
						try {
							while (rs.next()) {
								int tableId = rs.getInt(TTables.ID);
								SysTableInfo sti = tableIdToTable.get(tableId);
								if (sti != null) {
									sti.setFieldId(rs.getString(TFields.SYSID), rs.getInt(TFields.ID));
								}
							}
						} finally {
							DBUtil.close(rs);
						}
					} finally {
						DBUtil.close(ps);
					}
					
					SysTables.this.sysIdToTable = sysIdToTable;
					return null;
				}
			});
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}
}
