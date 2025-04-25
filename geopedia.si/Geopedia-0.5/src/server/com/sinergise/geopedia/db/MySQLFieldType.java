package com.sinergise.geopedia.db;

import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;

public abstract class MySQLFieldType {
	
	public enum DatabaseType {
		IDENTIFIER,
		DOUBLE,
		BOOLEAN,
		GEOMETRY,
		SHORTTEXT,
		TEXT,
		TIMESTAMP,
		DATE,
		NUMERIC,
		BIGINT;
		
		public boolean isText() {
			if (this==SHORTTEXT || this==TEXT)
				return true;
			return false;
		}
	}
	
	
	public enum DatabaseIndex {NONE,PRIMARY,UNIQUE,GEOMETRY,FULLTEXT};
	
	
	public static String toSQLType(DatabaseType type) {
		switch (type) {
			case IDENTIFIER:
				return "INTEGER";
			case DOUBLE:
				return "DOUBLE";
			case BOOLEAN:
				return "TINYINT";
			case GEOMETRY:
				return "GEOMETRY";
			case SHORTTEXT:
				return "VARCHAR(255)";
			case TEXT:
				return "TEXT";
			case TIMESTAMP:
				return "TIMESTAMP";
			case DATE:
				return "DATETIME";
			case NUMERIC:
				return "DECIMAL(31,4)";
			case BIGINT:
				return "BIGINT";
		}
		throw new IllegalArgumentException();
	}
	
	public static DatabaseType getDatabaseType(Field field) {
		return getDatabaseType(field.type);
	}
	
	public static DatabaseType getDatabaseType(FieldType fieldType) {
		switch (fieldType) {
		case BLOB:
			return DatabaseType.IDENTIFIER;
		case BOOLEAN:
			return  DatabaseType.BOOLEAN;
		case DATE:
			return  DatabaseType.DATE;
		case DATETIME:
			return  DatabaseType.DATE;
		case DECIMAL:
			return DatabaseType.NUMERIC;
		case FOREIGN_ID:
			return  DatabaseType.IDENTIFIER;
		case INTEGER:
			return DatabaseType.BIGINT;
		case LONGPLAINTEXT:
			return  DatabaseType.TEXT;
		case PLAINTEXT:
			return  DatabaseType.SHORTTEXT;
		case STYLE:
			return  DatabaseType.TEXT;
		case WIKITEXT:
			return  DatabaseType.TEXT;
		}
		throw new IllegalArgumentException();
		
	}
	
	public static String mySQLType(FieldType fieldType)
	{
		switch (fieldType) {
		case BLOB:
			return "MEDIUMBLOB";
		case BOOLEAN:
			return "TINYINT";
		case DATE:
			return "DATE";
		case DATETIME:
			return "DATETIME";
		case DECIMAL:
			return "DECIMAL(31,4)";
		case FOREIGN_ID:
			return "INT";
		case INTEGER:
			return "BIGINT";
		case LONGPLAINTEXT:
			return "MEDIUMTEXT";
		case PLAINTEXT:
			return "VARCHAR(255)";
		case STYLE:
			return "MEDIUMTEXT";
		case WIKITEXT:
			return "MEDIUMTEXT";
		}
		throw new IllegalArgumentException();
	}

	public static boolean isValid(FieldType fieldType)
    {
		switch (fieldType) {
		case BLOB:
		case BOOLEAN:
		case DATE:
		case DATETIME:
		case DECIMAL:
		case FOREIGN_ID:
		case INTEGER:
		case LONGPLAINTEXT:
		case PLAINTEXT:
		case STYLE:
		case WIKITEXT:
			return true;
		default:
			return false;
		}
    }



	public static boolean isText(FieldType type) {
		return type==FieldType.PLAINTEXT || type==FieldType.LONGPLAINTEXT || type==FieldType.WIKITEXT;
	}


}
