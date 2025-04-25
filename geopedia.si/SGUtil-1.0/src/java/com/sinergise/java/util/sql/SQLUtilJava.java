package com.sinergise.java.util.sql;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.sql.SQLUtil;

/**
 * @author dvitas
 * @author mkadunc
 */

public class SQLUtilJava extends SQLUtil {
	private static final Logger logger = LoggerFactory.getLogger(SQLUtilJava.class);

	/**
	 * Call a stored procedure
	 * 
	 * @param connection
	 * @param procedureName
	 * @param params
	 * @param outTypes
	 * @return
	 * @throws SQLException <br>
	 * <br>
	 *             Example for our 'standard' procedures with some input and two output parameters:<br>
	 * <br>
	 *             DB declaration (i.e. in package VISUAL_CONTROLS_PKG):<br>
	 *             <code><br>
	 * PROCEDURE ControlRFV (   p_land_parcel_id land_parcels.id%type,  <br>
	 *                                   p_note  visual_controls.notes%type, <br>
	 *                                   p_due_date visual_controls.d_due%type DEFAULT sysdate+14,<br>
	 *                                   oMsgType    OUT    varchar2,<br>
	 *                                   oMsg        OUT    varchar2);<br>
	 *  </code> <br>
	 *             Java call:<br>
	 *             <code><br>                                
	 *      Object[] values = SQLUtil.execOraFunc(connection, "VISUAL_CONTROLS_PKG.ControlRFV", new Object[]{new Integer(polygonID), note, null}, new int[] { Types.CHAR, Types.CHAR });<br>
	 *      if(!values[0].equals("OK"))<br>
	 *      		throw new SQLException((String)values[1]);<br>
	 *  </code><br>
	 */
	public static Object[] execOraFunc(Connection connection, String procedureName, Object[] params, int[] outTypes)
		throws SQLException {

		if ((params == null || params.length < 1) && (outTypes == null || outTypes.length < 1)) {
			CallableStatement st = connection.prepareCall("{call " + procedureName + "()}");
			try {
				st.execute();
			} finally {
				st.close();
			}
			return new Object[0];
		}

		int num = (params == null ? 0 : params.length);
		int outNum = (outTypes == null ? 0 : outTypes.length);
		int[] outIndices = new int[outNum];
		num += outNum;
		StringBuffer sb = new StringBuffer();
		sb.append("{call ");
		sb.append(procedureName);
		sb.append('(');
		for (int a = 0; a < num; a++) {
			if (a > 0) {
				sb.append(',');
			}
			sb.append('?');
		}
		sb.append(")}");
		String sql = sb.toString();

		CallableStatement st = connection.prepareCall(sql);
		int bindedParamIndex = 1;

		if (params != null)
			for (int i = 0; i < params.length; i++)
				st.setObject(bindedParamIndex++, params[i]);

		for (int i = 0; outTypes != null && i < outTypes.length; i++) {
			st.registerOutParameter(bindedParamIndex++, outTypes[i]);
			outIndices[i] = bindedParamIndex - 1;
		}

		st.execute();

		if (outTypes == null) {
			st.close();
			return new Object[0];
		}

		Object[] out = new Object[outTypes.length];
		for (int i = 0; i < out.length; i++)
			out[i] = st.getObject(outIndices[i]);

		st.close();
		return out;
	}

	public static List<Object[]> executeQuery(String sql, Object[] params, Connection connection) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql);
		for (int i = 0; params != null && i < params.length; i++)
			ps.setObject(i + 1, params[i]);

		ResultSet rs = ps.executeQuery();
		List<Object[]> rows = fetchRS(rs);
		rs.close();
		ps.close();
		return rows;
	}

	public static List<Object[]> fetchRS(ResultSet rs) throws SQLException {
		ArrayList<Object[]> rows = new ArrayList<Object[]>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		while (rs.next()) {
			Object[] columns = new Object[columnCount];
			for (int i = 0; i < columnCount; i++)
				columns[i] = rs.getObject(i + 1);
			rows.add(columns);
		}
		return rows;
	}

	public static boolean prepareAndExecute(String statement, Connection conn) throws SQLException {
		LoggableStatement stmt = LoggableStatement.prepare(conn, statement);
		try {
			return stmt.execute();
		} finally {
			SQLUtilJava.close(stmt);
		}
	}

	public static List<Object[]> executeQuery(final String sql, final Iterable<Object> params,
		final Connection connection) throws SQLException {
		final PreparedStatement ps = connection.prepareStatement(sql);
		final ResultSet rs = null;
		try {
			int idx = 1;
			for (final Object p : params) {
				ps.setObject(idx++, p);
			}
			return fetchRs(ps.executeQuery());
		} finally {
			try {
				close(rs);
			} finally {
				close(ps);
			}
		}
	}

	public static List<Object[]> fetchRs(final ResultSet rs) throws SQLException {
		final ArrayList<Object[]> rows = new ArrayList<Object[]>();
		final ResultSetMetaData rsmd = rs.getMetaData();
		final int columnCount = rsmd.getColumnCount();

		while (rs.next()) {
			final Object[] columns = fetchRow(rs, columnCount);
			rows.add(columns);
		}
		return rows;
	}

	public static Object[] fetchRow(final ResultSet rs, final int columnCount) throws SQLException {
		final Object[] columns = new Object[columnCount];
		for (int i = 0; i < columnCount; i++) {
			columns[i] = rs.getObject(i + 1);
		}
		return columns;
	}

	public static void rollback(final Connection connection) throws SQLException {
		if (connection != null) {
			connection.rollback();
		}
	}

	public static void rollbackSilent(final Connection connection) {
		try {
			rollback(connection);
		} catch(final Throwable t) {
			// be quiet
			logger.warn("Error while executing rollback", t);
		}
	}

	public static void close(final Statement st) throws SQLException {
		if (st != null) {
			st.close();
		}
	}

	public static void closeSilent(final Statement ps) {
		try {
			close(ps);
		} catch(final Throwable t) {
			// be quiet
			logger.warn("Error while closing prepared statement", t);
		}
	}

	public static void close(final ResultSet rs) throws SQLException {
		if (rs != null) {
			rs.close();
		}
	}

	public static void closeSilent(final ResultSet rs) {
		try {
			close(rs);
		} catch(final Throwable t) {
			// be quiet
			logger.warn("Error while closing result set", t);
		}
	}

	public static void close(final Connection conn) throws SQLException {
		if (conn != null)
			conn.close();
	}

	public static void closeSilent(final Connection conn) {
		try {
			close(conn);
		} catch(final Throwable t) {
			// be quiet
			logger.warn("Error while closing connection", t);
		}
	}

	public static Double getDouble(ResultSet rs, String columnName) throws SQLException {
		double val = rs.getDouble(columnName);
		if (rs.wasNull()) {
			return null;
		}
		return Double.valueOf(val);
	}

	public static Double getDouble(ResultSet rs, int columnIndex) throws SQLException {
		double val = rs.getDouble(columnIndex);
		if (rs.wasNull()) {
			return null;
		}
		return Double.valueOf(val);
	}

	public static Double getDouble(CallableStatement cs, String columnName) throws SQLException {
		double val = cs.getDouble(columnName);
		if (cs.wasNull()) {
			return null;
		}
		return Double.valueOf(val);
	}

	public static Double getDouble(CallableStatement cs, int columnIndex) throws SQLException {
		double val = cs.getDouble(columnIndex);
		if (cs.wasNull()) {
			return null;
		}
		return Double.valueOf(val);
	}

	public static Long getLong(ResultSet rs, String columnName) throws SQLException {
		long val = rs.getLong(columnName);
		if (rs.wasNull()) {
			return null;
		}
		return Long.valueOf(val);
	}

	public static Long getLong(ResultSet rs, int columnIndex) throws SQLException {
		long val = rs.getLong(columnIndex);
		if (rs.wasNull()) {
			return null;
		}
		return Long.valueOf(val);
	}

	public static Long getLong(CallableStatement cs, String columnName) throws SQLException {
		long val = cs.getLong(columnName);
		if (cs.wasNull()) {
			return null;
		}
		return Long.valueOf(val);
	}

	public static Long getLong(CallableStatement cs, int columnIndex) throws SQLException {
		long val = cs.getLong(columnIndex);
		if (cs.wasNull()) {
			return null;
		}
		return Long.valueOf(val);
	}

	public static Integer getInt(ResultSet rs, String columnName) throws SQLException {
		int val = rs.getInt(columnName);
		if (rs.wasNull()) {
			return null;
		}
		return Integer.valueOf(val);
	}

	public static Integer getInt(ResultSet rs, int columnIndex) throws SQLException {
		int val = rs.getInt(columnIndex);
		if (rs.wasNull()) {
			return null;
		}
		return Integer.valueOf(val);
	}

	public static Integer getInt(CallableStatement cs, int paramIndex) throws SQLException {
		int val = cs.getInt(paramIndex);
		if (cs.wasNull()) {
			return null;
		}
		return Integer.valueOf(val);
	}

	public static Integer getInt(CallableStatement cs, String paramName) throws SQLException {
		int val = cs.getInt(paramName);
		if (cs.wasNull()) {
			return null;
		}
		return Integer.valueOf(val);
	}

	public static Byte getByte(ResultSet rs, String columnName) throws SQLException {
		byte val = rs.getByte(columnName);
		if (rs.wasNull()) {
			return null;
		}
		return Byte.valueOf(val);
	}

	public static Byte getByte(ResultSet rs, int columnIndex) throws SQLException {
		byte val = rs.getByte(columnIndex);
		if (rs.wasNull()) {
			return null;
		}
		return Byte.valueOf(val);
	}

	public static Byte getByte(CallableStatement cs, int paramIndex) throws SQLException {
		byte val = cs.getByte(paramIndex);
		if (cs.wasNull()) {
			return null;
		}
		return Byte.valueOf(val);
	}

	public static Byte getByte(CallableStatement cs, String paramName) throws SQLException {
		byte val = cs.getByte(paramName);
		if (cs.wasNull()) {
			return null;
		}
		return Byte.valueOf(val);
	}

	public static Boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
		boolean val = rs.getBoolean(columnName);
		if (rs.wasNull()) {
			return null;
		}
		return Boolean.valueOf(val);
	}

	public static Boolean getBoolean(ResultSet rs, int columnIndex) throws SQLException {
		boolean val = rs.getBoolean(columnIndex);
		if (rs.wasNull()) {
			return null;
		}
		return Boolean.valueOf(val);
	}

	public static Boolean getBoolean(CallableStatement cs, int paramIndex) throws SQLException {
		boolean val = cs.getBoolean(paramIndex);
		if (cs.wasNull()) {
			return null;
		}
		return Boolean.valueOf(val);
	}

	public static Boolean getBoolean(CallableStatement cs, String paramName) throws SQLException {
		boolean val = cs.getBoolean(paramName);
		if (cs.wasNull()) {
			return null;
		}
		return Boolean.valueOf(val);
	}

	public static String inParamString(int numParams) {
		StringBuilder sb = new StringBuilder((numParams - 1) * 3 + 1);
		appendInParamString(sb, numParams);
		return sb.toString();
	}

	public static <T extends Appendable> T appendInParamString(T buffer, int numParams) {
		try {
			for (int i = numParams - 2; i >= 0; i--) {
				buffer.append("?, ");
			}
			buffer.append('?');
		} catch(IOException e) {
			throw new IllegalArgumentException("The buffer object should not throw IOException", e);
		}
		return buffer;
	}

	public static int setParamValues(PreparedStatement st, int firstElementIdx, Object[] array, int componentType)
		throws SQLException {
		for (int i = 0; i < array.length; i++) {
			st.setObject(firstElementIdx++, array[i], componentType);
		}
		return firstElementIdx;
	}

	public static int setParamValues(PreparedStatement st, int firstElementIdx, int[] array) throws SQLException {
		for (int i = 0; i < array.length; i++) {
			st.setInt(firstElementIdx++, array[i]);
		}
		return firstElementIdx;
	}

	/**
	 * @param reader
	 * @param conn
	 * @param separatorPattern
	 * @throws SQLException
	 */
	public static void executeScript(Reader reader, Connection conn, String separatorPattern) throws SQLException {
		Scanner scn = new Scanner(reader);
		scn.useDelimiter(separatorPattern);
		while (scn.hasNext()) {
			String stmt = scn.next().trim();
			if (stmt.isEmpty()) {
				continue;
			}
			try {
				logger.trace("Script executing: {}", stmt);
				prepareAndExecute(stmt, conn);
			} catch(Throwable e) {
				throw new SQLException("Failed to execute: " + stmt, e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T unwrap(Object obj, Class<T> c) throws IllegalArgumentException {
		if (obj == null) {
			return null;
		}
		Object ret = silentUnwrap(obj, c);
		if (ret == null) {
			throw new IllegalArgumentException("Cannot unwrap " + obj + " to " + c.getName());
		}
		return (T)ret;
	}


	@SuppressWarnings("unchecked")
	public static <T> T silentUnwrap(Object obj, Class<T> c) {
		if (obj == null) {
			return null;
		}
		if (c.isInstance(obj)) {
			return (T)obj;
		}

		// dbcp wrapper
		try {
			Method met = obj.getClass().getMethod("getInnermostDelegate");
			met.setAccessible(true);
			return (T)met.invoke(obj);
		} catch(NoSuchMethodException e) {
			// don't report
		} catch(Throwable t) {
			t.printStackTrace();
		}

		// weblogic wrappers
		try {
			return (T)obj.getClass().getMethod("getVendorObj").invoke(obj);
		} catch(NoSuchMethodException e) {
			// don't report
		} catch(Throwable t) {
			t.printStackTrace();
		}
		try {
			return (T)obj.getClass().getMethod("getVendorConnection").invoke(obj);
		} catch(NoSuchMethodException e) {
			// don't report
		} catch(Throwable t) {
			t.printStackTrace();
		}

		// wildfly 8 wrappers
		try {
			return (T)obj.getClass().getMethod("getUnderlyingConnection").invoke(obj);
		} catch(NoSuchMethodException e) {
			// don't report
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		// Java 1.6 jdbc API
		try {
			return (T)obj.getClass().getMethod("unwrap", Class.class).invoke(obj, c);
		} catch(NoSuchMethodException e) {
			// don't report
		} catch(Throwable t) {
			t.printStackTrace();
		}

		return null;
	}
}