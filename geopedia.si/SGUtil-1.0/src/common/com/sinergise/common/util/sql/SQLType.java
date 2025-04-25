package com.sinergise.common.util.sql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

public enum SQLType {
	ARRAY(Types.ARRAY, null, true),
	BIGINT(Types.BIGINT, Long.class, true),
	BINARY(Types.BINARY, byte[].class, false),
	BIT(Types.BIT, Boolean.class, true),
	BLOB(Types.BLOB, null, true),
	BOOLEAN(Types.BOOLEAN, Boolean.class, false),
	CHAR(Types.CHAR, String.class, false),
	CLOB(Types.CLOB, null, true),
	DATALINK(Types.DATALINK, null, false),
	DATE(Types.DATE, Date.class, true),
	DECIMAL(Types.DECIMAL, BigDecimal.class, false),
	DISTINCT(Types.DISTINCT, null, false),
	DOUBLE(Types.DOUBLE, Double.class, true),
	FLOAT(Types.FLOAT, Float.class, false),
	INTEGER(Types.INTEGER, Integer.class, true),
	JAVA_OBJECT(Types.JAVA_OBJECT, null, false),
	LONGNVARCHAR(Types.LONGNVARCHAR, String.class, false),
	LONGVARBINARY(Types.LONGVARBINARY, byte[].class, false),
	LONGVARCHAR(Types.LONGVARCHAR, String.class, false),
	NCHAR(Types.NCHAR, String.class, false),
	NCLOB(Types.NCLOB, null, true),
	NULL(Types.NULL, null, false),
	NUMERIC(Types.NUMERIC, BigDecimal.class, true),
	NVARCHAR(Types.NVARCHAR, String.class, false),
	OTHER(Types.OTHER, null, false),
	REAL(Types.REAL, Float.class, true),
	REF(Types.REF, null, true),
	ROWID(Types.ROWID, null, true),
	SMALLINT(Types.SMALLINT, Short.class, true),
	SQLXML(Types.SQLXML, null, true),
	STRUCT(Types.STRUCT, null, true),
	TIME(Types.TIME, Time.class, true),
	TIMESTAMP(Types.TIMESTAMP, Timestamp.class, true),
	TINYINT(Types.TINYINT, Byte.class, true),
	VARBINARY(Types.VARBINARY, byte[].class, true),
	VARCHAR(Types.VARCHAR, String.class, true),
	
	UNKNOWN(Types.OTHER, Object.class, false),
	GEOMETRY(Types.OTHER, Object.class, false),
	CURSOR(-10, null, false); //TODO: OracleTypes.CURSOR find a better mapping?

	private final int typeInt;
	private final Class<?> mapping;
	private boolean defaultSqlTypeForClass;

	private SQLType(int typeInt, Class<?> mapping, boolean defaultType) {
		this.typeInt = typeInt;
		this.mapping = mapping;
		this.defaultSqlTypeForClass = defaultType;
	}
	
	@Deprecated /** User SQLTypeMapper instead */
	public int getInt() {
		return typeInt;
	}

	public static SQLType getFor(Object javaObject) {
		if (javaObject == null) {
			return NULL;
		}
		return getFor(javaObject.getClass());
	}
	
	public static SQLType getFor(Class<?> javaType) {
		SQLType fallBack = null;
		for (final SQLType t : SQLType.values()) {
			if (javaType.equals(t.mapping)) {
				if (t.defaultSqlTypeForClass) {
					return t;
				}
				fallBack = t;
			}
		}
		return fallBack;
	}
	
	public static SQLType getFor(final int jdbcTypeInt) {
		for (final SQLType t : SQLType.values()) {
			if (t.typeInt == jdbcTypeInt) { 
				return t;
			}
		}
		return null;
	}
	
	public static String getDebugTypeString(final int jdbcTypeInt) {
		SQLType typ = getFor(jdbcTypeInt);
		if (typ != null) return typ.name();
		return "UNKNOWN ("+jdbcTypeInt+")";
	}

	public Class<?> jdbcMapping() {
		return mapping;
	}
}