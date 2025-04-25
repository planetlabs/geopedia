package com.sinergise.geopedia.db.util;

import static com.sinergise.geopedia.db.util.ValueMod.Type.T_ENVELOPE;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.db.DBNames;
import com.sinergise.geopedia.db.DBUtil;
import com.sinergise.java.util.sql.LoggableStatement;


public class Update extends UpdateConditions
{
	public static final int T_INSERT = 1;
	public static final int T_UPDATE = 2;
	public static final int T_REPLACE = 3;
	public static final int T_DELETE = 4;
	
	public final int type;
	String table;
	ArrayList<ValueMod> values = new ArrayList<ValueMod>();
	ArrayList<ValueMod> conditions = new ArrayList<ValueMod>();
	
	private int lastInsertedId;
	
	public static Update insert(String table)
	{
		return new Update(T_INSERT, table);
	}
	
	public static Update replace(String table)
	{
		return new Update(T_REPLACE, table);
	}
	
	public static Update update(String table)
	{
		return new Update(T_UPDATE, table);
	}
	
	public static UpdateConditions delete(String table)
	{
		return new Update(T_DELETE, table);
	}

	public static Update dataUpdate(Table t, int id)
	{
		return update(DBNames.table(t)).where(DBNames.id(t), id);
	}
	
	private Update(int type, String table)
	{
		this.type = type;
		this.table = table;
	}
	
	private Update add(ValueMod mod)
	{
		values.add(mod);
		return this;
	}
	
	private Update condition(ValueMod mod)
	{
		conditions.add(mod);
		return this;
	}
	
	public ArrayList<ValueMod> getValues() {
		return values;
	}
	
	public String getTable() {
		return table;
	}
	/**
	 * For update and replace, returns number of affected rows.
	 * For insert, returns auto-generated id or -1 if it couldn't be obtained or if affected rows == 0.
	 * 
	 * @param conn the connection to execute the update on
	 * @return as above
	 * @throws SQLException
	 */
	public int execute(Connection conn) throws SQLException
	{
		if (type != T_DELETE && values.size() < 1)
			throw new IllegalStateException("Nothing to do");
		if (type == T_DELETE && !values.isEmpty())
			throw new IllegalStateException("Can't set values when deleting");
		if (type == T_DELETE && conditions.isEmpty())
			throw new IllegalStateException("Deleting entire table not allowed");
		
		StringBuilder sb = new StringBuilder();
		
		switch(type) {
		case T_DELETE:
			sb.append("DELETE FROM "+table);
			break;
		case T_INSERT:
			sb.append("INSERT INTO "+table+" SET ");
			break;
		case T_UPDATE:
			sb.append("UPDATE "+table+" SET ");
			break;
		case T_REPLACE:
			sb.append("REPLACE INTO "+table+" SET ");
			break;
		default:
			throw new IllegalStateException();
		}
		
		boolean first = true;
		for (ValueMod mod : values) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			if (mod.type == T_ENVELOPE)
				throw new IllegalStateException();
			
			sb.append(mod.field);
			sb.append("=?");
		}
		
		first = true;
		for (ValueMod mod : conditions) {
			if (first) {
				first = false;
				sb.append(" WHERE (");
			} else {
				sb.append(") AND (");
			}
			
			if (mod.type == T_ENVELOPE) {
				sb.append(condEnvelope(mod.field, (Envelope) mod.val));
			} else {
				sb.append(mod.field);
				sb.append("=?");
			}
		}
		if (!first)
			sb.append(")");
		
		String sql = sb.toString();
		LoggableStatement ls = new LoggableStatement(conn, sql);
		try {
			int count = 1;
			for (int a=0; a<2; a++) {
				ArrayList<ValueMod> it = a==0 ? values : conditions;
				for (ValueMod mod : it) {
					count=mod.setValue(ls, count);
				}
			}
			
			int affectedRows = ls.executeUpdate();
			if (type == T_INSERT) {
				if (affectedRows > 0) {
					ResultSet rs = ls.getGeneratedKeys();
					try {
						if (rs.next()) {
							lastInsertedId = rs.getInt(1);
							return lastInsertedId;
						}
					} finally {
						DBUtil.close(rs);
					}
				}
				return -1;
			}
			return affectedRows;
		} finally {
			DBUtil.close(ls);
		}
	}

	public int getLastInsertedId() {
		return lastInsertedId;
	}
	public static String condEnvelope(String field, Envelope env)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("MBRIntersects(geomfromtext('linestring(");
		sb.append(env.getMinX());
		sb.append(' ');
		sb.append(env.getMinY());
		sb.append(',');
		sb.append(env.getMaxX());
		sb.append(' ');
		sb.append(env.getMaxY());
		sb.append(")'), ");
		sb.append(field);
		sb.append(")"); 
		return sb.toString();
	}
	
	@Override
	public Update where(String field, int value)
	{
		return condition(new ValueMod(field, Integer.valueOf(value)));
	}
	
	@Override
	public Update where(String field, String value) {
		return condition(new ValueMod(field, value));		
	}
	
	public Update set(String field, boolean value) {
		return add(new ValueMod(field,value));
	}
	
	public Update set(String field, Integer value)
	{
		return add(new ValueMod(field, value));
	}

	public Update set(String field, int value) {
		return set(field, Integer.valueOf(value));
	}
	
	public Update set(String field, Long value)
	{
		return add(new ValueMod(field, value));
	}
	
	public Update set(String field, String value)
	{
		return add(new ValueMod(field, value));
	}
	
	public Update set(String field, byte[] value)
	{
		return add(new ValueMod(field, value));
	}
	
	public Update set(String field, Geometry value)
	{
		return add(new ValueMod(field, value));
	}
	
	public Update set(String field, Double value)
	{
		return add(new ValueMod(field, value));
	}
	
	public Update set(String field, Date value)
	{
		return add(new ValueMod(field, value));
	}
	
	public Update set(String field, Timestamp value)
	{
		return add(new ValueMod(field, value));
	}
	
	public Update set(String field, BigDecimal value)
	{
		return add(new ValueMod(field, value));
	}
}
