package com.sinergise.geopedia.db.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.db.geometry.WkbWriter;


public class ValueMod
{
	enum Type
	{
		T_LONG, T_BYTES, T_STRING, T_BIGDECIMAL, T_ENVELOPE, T_INT, T_DOUBLE, T_DATE, T_TIMESTAMP;
	}
	
	public String field;
	public Object val;
	public Type type;
	
	private ValueMod(String field, Object val, Type type)
	{
		this.field = field;
		this.val = val;
		this.type = type;
	}

	public ValueMod(String field, boolean val) {
		this(field, val ? 1:0, Type.T_INT);
	}

	public ValueMod(String field, String val)
	{
		this(field, val, Type.T_STRING);
	}
	
	public ValueMod(String field, Long val)
	{
		this(field, val, Type.T_LONG);
	}
	
	public ValueMod(String field, BigDecimal val)
	{
		this(field, val, Type.T_BIGDECIMAL);
	}
	
	public ValueMod(String field, byte[] val)
	{
		this(field, val, Type.T_BYTES);
	}
	
	public ValueMod(String field, Geometry geom)
	{
		this(field, WkbWriter.toMySqlInternal(geom), Type.T_BYTES);
	}
	
	public ValueMod(String field, Envelope env)
	{
		this(field, env, Type.T_ENVELOPE);
	}
	
	public ValueMod(String field, Integer val)
	{
		this(field, val, Type.T_INT);
	}
	
	public ValueMod(String field, Double val)
	{
		this(field, val, Type.T_DOUBLE);
	}
	
	public ValueMod(String field, Date val)
	{
		this(field, val, Type.T_DATE);
	}
	
	public ValueMod(String field, Timestamp val)
	{
		this(field, val, Type.T_TIMESTAMP);
	}

	private boolean isNull(){
		return val==null;
	}
	public int setValue(PreparedStatement ps, int index) throws SQLException
    {
        switch(type) {
        case T_BIGDECIMAL:
        	if (isNull()) {
        		ps.setNull(index++, Types.NUMERIC);
        	} else {
        		ps.setBigDecimal(index++, (BigDecimal)val);
        	}
        	break;
        case T_BYTES:
        	if (isNull()) {
        		ps.setNull(index++, Types.BINARY);
        	} else {
        		ps.setBytes(index++, (byte[])val);
        	}
        	break;
        case T_ENVELOPE:
        	break;
        case T_LONG:
        	if (isNull()) {
        		ps.setNull(index++, Types.BIGINT);
        	} else {
        		ps.setLong(index++, ((Long)val).longValue());
        	}
        	break;
        case T_STRING:
        	if (isNull()) {
        		ps.setNull(index++, Types.VARCHAR);
        	} else {
        		ps.setString(index++, (String)val);
        	}
        	break;
        case T_DOUBLE:
        	if (isNull()) {
        		ps.setNull(index++, Types.DOUBLE);
        	} else {
        		ps.setDouble(index++, (Double)val);
        	}
        	break;
        case T_INT:
        	if (isNull()) {
        		ps.setNull(index++, Types.INTEGER);
        	} else {
        		ps.setInt(index++, (Integer)val);
        	}
        	break;
        case T_DATE:
        	if (isNull()) {
        		ps.setNull(index++, Types.DATE);
        	} else {
        		ps.setDate(index++, (Date)val);
        	}
        	break;
        case T_TIMESTAMP:
        	if (isNull()) {
        		ps.setNull(index++, Types.TIMESTAMP);
        	} else {
        		ps.setTimestamp(index++, (Timestamp)val);
        	}
        	break;
        default:
        	throw new IllegalStateException();
        }
        return index;
    }
}
