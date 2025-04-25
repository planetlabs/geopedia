package com.sinergise.geopedia.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;

import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;

public class DBUtil
{
	static ThreadLocal<GregorianCalendar> cal = new ThreadLocal<GregorianCalendar>() {
		protected GregorianCalendar initialValue()
		{
			return new GregorianCalendar();
		}
	};

	public static long mkdate(int y, int m, int d)
	{
		return mkdate(y, m, d, 12, 0, 0);
	}

	public static long mkdate(int y, int m, int d, int h, int min, int s)
	{
		GregorianCalendar gc = cal.get();

		gc.set(y, m - 1, d, h, min, s);
		gc.set(GregorianCalendar.MILLISECOND, 0);

		return gc.getTimeInMillis();
	}

	public static int parseHexInt(String s, int startPos, int endPos) throws NumberFormatException
	{
		if (s == null) {
			throw new NumberFormatException("null");
		}

		int result = 0;
		int i = startPos, max = endPos;

		if (max - i > 8)
			throw new NumberFormatException(s.substring(startPos, endPos));
		
		while (i < max) {
			char c = s.charAt(i++);
			result <<= 4;
			if (c >= '0' && c <= '9') {
				result |= (c - '0');
			} else
			if (c >= 'a' && c <= 'f') {
				result |= (c - ('a' - 10));
			} else
			if (c >= 'A' && c <= 'F') {
				result |= (c - ('A' - 10));
			} else {
				throw new NumberFormatException(s.substring(startPos, endPos));
			}
		}
		
		return result;
	}

	public static int parseInt(String s, int startPos, int endPos) throws NumberFormatException
	{
		if (s == null) {
			throw new NumberFormatException("null");
		}

		int result = 0;
		boolean negative = false;
		int i = startPos, max = endPos;
		int limit;
		int multmin;
		int digit;

		if (max > i) {
			if (s.charAt(i) == '-') {
				negative = true;
				limit = Integer.MIN_VALUE;
				i++;
			} else {
				limit = -Integer.MAX_VALUE;
			}
			multmin = limit / 10;
			if (i < max) {
				digit = s.charAt(i++) - '0';
				if (digit < 0 || digit > 9) {
					throw new NumberFormatException(s.substring(startPos, endPos));
				} else {
					result = -digit;
				}
			}
			while (i < max) {
				digit = s.charAt(i++) - '0';
				if (digit < 0 || digit > 9) {
					throw new NumberFormatException(s.substring(startPos, endPos));
				}
				if (result < multmin) {
					throw new NumberFormatException(s.substring(startPos, endPos));
				}
				result *= 10;
				if (result < limit + digit) {
					throw new NumberFormatException(s.substring(startPos, endPos));
				}
				result -= digit;
			}
		} else {
			throw new NumberFormatException(s.substring(startPos, endPos));
		}
		if (negative) {
			if (i > 1) {
				return result;
			} else { /* Only got "-" */
				throw new NumberFormatException(s.substring(startPos, endPos));
			}
		} else {
			return -result;
		}
	}

	public static Object processDefaultValue(String value, FieldType fieldType)
    {
    	if (value == null || value.length() == 0)
    		return null;
    	
    	switch(fieldType) {
    		case BLOB: {
    			int len = value.length();
    			byte[] out = new byte[len];
    			for (int a=0; a<len; a++)
    				out[a] = (byte)value.charAt(a);
    			return out;
    		}
    		
    		case BOOLEAN: {
    			return "0".equals(value) ? Boolean.FALSE : Boolean.TRUE;
    		}
    			
    		case DATE: {
    			if (value.length() != 10)
    				return null;
    			if (value.charAt(4) != '-' || value.charAt(7) != '-')
    				return null;
    			
    			int y, m, d;
    			try {
    				y = parseInt(value, 0, 4);
    				m = parseInt(value, 5, 7);
    				d = parseInt(value, 8, 10);
    				
    				return new Long(mkdate(y, m, d));
    			} catch (NumberFormatException e) {
    				return null;
    			}
    		}
    		
    		case DATETIME: {
    			if (value.length() != 19)
    				return null;
    			if (value.charAt(4) != '-' || value.charAt(7) != '-' || value.charAt(10) != ' ' || value.charAt(13) != ':' || value.charAt(16) != ':')
    				return null;
    			
    			int y, m, d, h, min, s;
    			try {
    				y = parseInt(value, 0, 4);
    				m = parseInt(value, 5, 7);
    				d = parseInt(value, 8, 10);
    				h = parseInt(value, 11, 13);
    				min = parseInt(value, 14, 16);
    				s = parseInt(value, 17, 19);
    				
    				return new Long(mkdate(y, m, d, h, min, s));
    			} catch (NumberFormatException e) {
    				return null;
    			}
    		}
    		
    		case DECIMAL: {
    			try {
    				return Double.parseDouble(value);
    			} catch (NumberFormatException e) {
    				return null;
    			}
    		}
    		
    		case FOREIGN_ID: {
    			try {
    				return Integer.valueOf(value);
    			} catch (NumberFormatException e) {
    				return null;
    			}
    		}
    		
    		case INTEGER: {
    			try {
    				return Long.valueOf(value);
    			} catch (NumberFormatException e) {
    				return null;
    			}
    		}
    		
    		case WIKITEXT:
    		case PLAINTEXT:
    		case LONGPLAINTEXT:
    		case STYLE:
    			return value;
    		
    		default:
    			throw new IllegalStateException();
    	}
    }

	public static void close(Statement ps)
    {
		if (ps == null)
			return;
		
		try {
			ps.close();
		} catch (SQLException e) {
			// ignore
		}
    }
	
	public static void close(ResultSet rs)
    {
		if (rs == null)
			return;
		
		try {
			rs.close();
		} catch (SQLException e) {
			// ignore
		}
    }
	
	public static void rollBack(Connection conn)
	{
		try {
			conn.rollback();
		} catch (SQLException e) {
			try {
				conn.close();
			} catch (SQLException ee) {
				// damnit :)
			}
		}
	}

	public static void close(Connection conn)
    {
		if (conn == null)
			return;
		
		try {
			conn.close();
		} catch (SQLException e) {
			// ignore
		}
    }

	public static String mySqlTypeFor(FieldType fieldType, int fieldFlags)
    {
		
    	StringBuilder sb = new StringBuilder();
    	
    	switch(fieldType) {
    	case BLOB:
    		sb.append("INT"); break;
    	case BOOLEAN:
    		sb.append("TINYINT"); break;
    	case DATE:
    		sb.append("DATE"); break;
    	case DATETIME:
    		sb.append("DATETIME"); break;
    	case DECIMAL:
    		sb.append("DECIMAL(31,4)"); break;
    	case FOREIGN_ID:
    		sb.append("INT"); break;
    	case INTEGER:
    		sb.append("BIGINT"); break;
    	case LONGPLAINTEXT:
    		sb.append("MEDIUMTEXT"); break;
    	case PLAINTEXT:
    		sb.append("VARCHAR(255)"); break;
    	case STYLE:
    		sb.append("MEDIUMTEXT"); break;
    	case WIKITEXT:
    		sb.append("MEDIUMTEXT"); break;
    	default:
    		throw new IllegalStateException();
    	}
    	
    	if ((fieldFlags & Field.F_MANDATORY) != 0)
    		sb.append(" NOT NULL");
    
    	return sb.toString();
    }

	public static void close(ResultSet rs, boolean closeStatement)
    {
		// Note: rs.close() causes getStatement() to fail
		// in MySQL connector..
		
		Statement s = null;
		if (closeStatement) {
			try {
				s = rs.getStatement();
			} catch (SQLException e) {
				// ignore
			}
		}
		try {
			rs.close();
		} catch (SQLException e) {
			// ignore too
		}
		if (s != null) {
			try {
				s.close();
			} catch (SQLException e) {
				// and, finally, ignore this one as well
			}
		}
    }
}
