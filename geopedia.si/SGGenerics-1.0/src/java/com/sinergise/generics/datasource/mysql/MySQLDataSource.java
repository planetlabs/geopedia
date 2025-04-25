package com.sinergise.generics.datasource.mysql;

import static com.sinergise.generics.core.EntityObject.Status.STORED;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.w3c.dom.Element;

import com.sinergise.generics.builder.filter.SQLFilterHelpers;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.OrderFilter;
import com.sinergise.generics.core.filter.SimpleSQLFilter;
import com.sinergise.generics.datasource.DatabaseDataSource;
import com.sinergise.generics.java.DBConnectionProvider;
import com.sinergise.generics.java.GenericsSettings;
import com.sinergise.generics.server.GenericsServerSession;
import com.sinergise.java.util.sql.LoggableStatement;

public class MySQLDataSource extends DatabaseDataSource {
	
	public MySQLDataSource (Element dsElement, GenericsSettings gSettings) {
		super(TYPE_MYSQL,dsElement, gSettings);
		
		String dbConnectionName = dsElement.getAttribute(ATTR_DATABASE_CONNECTION);
		if (dbConnectionName == null || dbConnectionName.length() == 0) {
			dbConnectionName = MySQLConnectionProvider.DEFAULT_CONNECTION;
		}
		dbCP = MySQLConnectionProvider.getInstance(dbConnectionName);
	}
	
	public MySQLDataSource (Element dsElement, GenericsSettings gSettings, DBConnectionProvider dbCP) {
		super(TYPE_MYSQL,dsElement, gSettings);
		this.dbCP = dbCP;
	}


	@Override
	public ArrayValueHolder getData(DataFilter dataFilter, int fromIdx, int toIdx, 
			GenericsServerSession gSession, Connection conn) throws SQLException {		
		
		String orderBySQL=null; 
		// construct SQL from filters
		SimpleSQLFilter sqlFilter = SQLFilterHelpers.createFilterFor(dataFilter, genericsSettings, gSession);		
		if (dataFilter instanceof CompoundDataFilter) {
			OrderFilter orderByFilter  = ((CompoundDataFilter)dataFilter).getOrderFilter();
			if (orderByFilter!=null) {
				orderBySQL = SQLFilterHelpers.getOrderSQL(orderByFilter);
			}

		}
		if (orderBySQL==null || orderBySQL.length()==0)
			orderBySQL=defaultOrderBySQL;
		
		//ensure constant ordering which is required by paging mechanism.
		if (primaryKey != null && primaryKey.length()>0)
			orderBySQL += ","+primaryKey;
		
		/**
		List<String> filterParams = new ArrayList<String>();
		EntityObject entityFilter = null;
		if (dataFilter != null && dataFilter instanceof SimpleFilter)  {
			SimpleFilter sFilter  = (SimpleFilter)dataFilter;
			entityFilter = sFilter.getFilterData();
		}
		String filterSQL=createFilter(entityFilter, filterParams); */
		String filterSQL = sqlFilter.getSQLStatement();
		
				
		boolean paging = true;
		if (fromIdx<=0 && toIdx<=0) {
			paging=false;
		}
		int totalCount = Integer.MIN_VALUE;
		LoggableStatement stmt  = null;
		ResultSet rset = null;
		try {
			String toSelect="";
			for (TypeAttribute ta:entityType.getAttributes())  {
				if (!ta.dbCanRead()) // ignore support attributes
					continue;
				if (toSelect.length()!=0)
					toSelect+=",";
				toSelect+=ta.getName();
			}
			
			if (primaryKey == null || primaryKey.length()==0)
				orderBySQL += ","+toSelect;
			
			String sql= "";
			if (paging) {
				
				
				if (countPages) { // count pages
					LoggableStatement ls = null; ResultSet rs = null;
					String countSQL = "SELECT COUNT(*) FROM "+tableName+" WHERE "+filterSQL;
					try {
						ls = new LoggableStatement(conn,countSQL);
						applyParameters(sqlFilter.getParameterList(), ls, 1);
						rs = ls.executeQuery();
						if (rs.next()) {
							totalCount = rs.getInt(1);
						}
					} finally {
						if (rs!=null)
							rs.close();
						if (ls!=null)
							ls.close();
					}
					
					
					if (totalCount != Integer.MIN_VALUE) {
						if (totalCount==0) { //nothing to return, no sense in executing second query..
							return  new ArrayValueHolder(entityType.getId());
						}
						
						if (fromIdx>totalCount) { // fix page location to the beginning if requested location was higher than total amount of data
							int total = toIdx-fromIdx;
							fromIdx=1;
							toIdx = fromIdx+total;
						}
					}
				}
				/*
				sql = "SELECT v.*,(SELECT COUNT(*) FROM "+tableName+" WHERE "+filterSQL+") NUM_ALL__ FROM "+
				"(SELECT "+toSelect+ ", row_number()  OVER ("+orderBySQL+") "+
				" rn FROM "+tableName+" WHERE "+filterSQL+") v WHERE rn<=? AND rn>=?";
				*/
				sql = "SELECT v.* FROM "+
				"(SELECT "+toSelect+ " FROM "+tableName+" WHERE "+filterSQL+" "+orderBySQL+") v LIMIT ?,?";
			} else {
				sql = "SELECT "+toSelect+" FROM "+tableName+" a WHERE "+filterSQL+" "+orderBySQL;
			}
			
			stmt = new LoggableStatement(conn,sql);	

			int paramIdx=1;
			paramIdx = applyParameters(sqlFilter.getParameterList(), stmt, paramIdx); // apply filter parameters

			if (paging) { // apply page limitations				
				stmt.setInt(paramIdx,fromIdx-1);
				paramIdx++;
	 			stmt.setInt(paramIdx,(toIdx-fromIdx)+2);
			}

			rset = stmt.executeQuery();
	        ArrayValueHolder avh = new ArrayValueHolder(entityType.getId());
	        avh.setDataLocation(fromIdx-1,toIdx-1); // starts with 0, db starts with 1
	        avh.setTotalDataCount(totalCount);
	        int maxCount = (toIdx-fromIdx)+1;
	        int count = 0;
	        while (rset.next()) {
	        	AbstractEntityObject eo = new AbstractEntityObject(entityType.getId());
	        	eo.setStatus(STORED);
	        	EntityType et = eo.getType();
	        	for (TypeAttribute ta:et.getAttributes()) {
	    			if (!ta.dbCanRead()) // ignore support attributes
	    				continue;
	        		String atName = ta.getName();

	        		TypeAttribute typAttribute = entityType.getAttribute(atName);
	        		if (typAttribute.getPrimitiveType() == Types.DATE) {
	        			java.sql.Date dateAttr = rset.getDate(atName, Calendar.getInstance(UTCTime));
	        			if (dateAttr!=null)
	        				eo.setPrimitiveValue(typAttribute.getId(),Long.toString(dateAttr.getTime()));
	        		} else if (booleanFalse != null && booleanTrue != null && typAttribute.getPrimitiveType() == Types.BOOLEAN) {
	        			String value = rset.getString(atName);
	        			if (booleanTrue.equals(value)) {
	        				eo.setPrimitiveValue(typAttribute.getId(), Boolean.TRUE.toString());
	        			} else {
	        				eo.setPrimitiveValue(typAttribute.getId(), Boolean.FALSE.toString());
	        			}
	        		}else {
	        			eo.setPrimitiveValue(typAttribute.getId(), rset.getString(atName));
	        		}
	        	}
	        	
	        	if (paging){
	        		count ++;
	        		if (count<=maxCount)
	        			avh.add(eo);
	        		else 
	        			avh.setHasMoreData(true);
	        	} else {
	        		avh.add(eo);
	        	}
	        }
	        return avh;
		} finally {
			if (rset!=null)
				rset.close();
			if (stmt!=null)
				stmt.close();
		}
	}

	@Override
	protected void processInsert(Connection conn, EntityObject eo) throws SQLException {
		String columns="";
		String vholders="";
		TypeAttribute priKeyTA = getPrimaryKey(eo);
		
		boolean priKeyHasValue = true;
		if (eo.getPrimitiveValue(priKeyTA.getId())==null ||
				eo.getPrimitiveValue(priKeyTA.getId()).length()==0)
			priKeyHasValue = false;


		EntityType objectType = eo.getType();
		for (TypeAttribute ta : objectType.getAttributes())  {
			if (ta.equals(priKeyTA) && !priKeyHasValue) // do not update primary key if there's no value for it
				continue;
			if (!ta.dbCanWrite()) // ignore support attributes
				continue;
			if (columns.length()!=0) {
				columns+=",";
				vholders+=","; 
			}
			columns+=ta.getName();
			vholders+="?";
		}
		String sql = "INSERT INTO "+tableName + "("+columns+") VALUES ("+vholders+")";
		LoggableStatement stmt = null;
		try {
			stmt = new LoggableStatement(conn, sql);
			int paramIdx=1;
			for (TypeAttribute ta:objectType.getAttributes()) {
				if (ta.equals(priKeyTA) && !priKeyHasValue) // do not update primary key if there's no value for it
					continue;
				if (!ta.dbCanWrite()) // ignore support attributes
					continue;
				convertAndSetParameter(stmt,ta , eo.getPrimitiveValue(ta.getId()), paramIdx);
				paramIdx++;
			}
			stmt.executeUpdate();
			if (!priKeyHasValue) {
				ResultSet rs = stmt.getGeneratedKeys();				
				try {
				if (rs.next()) {
					eo.setPrimitiveValue(priKeyTA.getId(), Integer.toString(rs.getInt(1)));
				} else {
					throw new SQLException("Unable to read primary key value!");
				}
				} finally {
					try {
					rs.close();
					} catch (SQLException ex) {}
				}
			}
		} finally {
			if (stmt!=null)
				stmt.close();
		}
	}
	
	

	
	

	/*	
	@Override
	public ValueHolder processData(ValueHolder values, GenericsServerSession gSession) throws SQLException {
		Connection conn = getConnection(gSession);
		ValueHolder returnValueHolder = null;
		try {			
			if (values instanceof ArrayValueHolder) {
				ArrayValueHolder returnResults = new ArrayValueHolder(((ArrayValueHolder)values).getEntityTypeId());
				for (ValueHolder vh:(ArrayValueHolder)values) {
					EntityObject eo = (EntityObject)vh;
					ValueHolder rv = processData(eo,conn);		
					if (rv!=null)
						returnResults.add(rv);
				}
				returnValueHolder = returnResults;
			} else {
				EntityObject eo = (EntityObject)values;
				returnValueHolder = processData(eo,conn);
			}
			
			commit(conn,gSession);
		} finally {
			close(conn,gSession);
		}
		return returnValueHolder;
	}*/
}
