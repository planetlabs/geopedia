package com.sinergise.generics.datasource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityObject.Status;
import com.sinergise.generics.core.BinaryValueHolder;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MasterDetailsHolder;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.PrimitiveValue;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.SQLFilterParameter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.generics.java.DBConnectionProvider;
import com.sinergise.generics.java.GenericsSettings;
import com.sinergise.generics.server.GenericsServerSession;
import com.sinergise.java.util.sql.LoggableStatement;

public abstract class DatabaseDataSource extends EntityDatasource{
	private static final Logger logger = LoggerFactory.getLogger(DatabaseDataSource.class);
	
	public static final TimeZone UTCTime = TimeZone.getTimeZone("UTC");
	
	public static final String ATTR_DATABASE_TABLE="dbTable";
	public static final String ATTR_TABLE_PRIMARY_KEY = "primarykey";
	public static final String ATTR_DATABASE_CONNECTION ="dbConnectionName";
	public static final String ATTR_PRIMARY_KEY_SEQUENCE="primaryKeySequence";
	public static final String ATTR_TOTAL_DATA_COUNT="totalDataCount";
	public static final String ATTR_DEFAULT_ORDER_BY="defaultOrderBy";
	public static final String BOOLEAN_TRUE="booleanTrue";
	public static final String BOOLEAN_FALSE="booleanFalse";
	public static final String BOOLEAN_TYPE="booleanType";
	
	protected DBConnectionProvider dbCP = null;
	
	protected String tableName;
	protected String primaryKey =null;
	protected String primaryKeySequence = null;
	protected String booleanTrue = null;
	protected String booleanFalse = null;
	protected boolean countPages = false;
	
	protected String defaultOrderBySQL;
	
	protected int booleanDataType = Types.STRING;
	
	private String getElementOrNull(Element el, String name) {
		return el.hasAttribute(name) ? el.getAttribute(name) : null;
	}

	public DatabaseDataSource(int dsType, Element dsElement, GenericsSettings settings) {
		super(dsType, dsElement, settings);
		
		tableName = getElementOrNull(dsElement, ATTR_DATABASE_TABLE);
		primaryKey = getElementOrNull(dsElement, ATTR_TABLE_PRIMARY_KEY);
		primaryKeySequence = getElementOrNull(dsElement, ATTR_PRIMARY_KEY_SEQUENCE);
		
		if (MetaAttributes.BOOLEAN_TRUE.equalsIgnoreCase(dsElement.getAttribute(ATTR_TOTAL_DATA_COUNT))) {
			countPages = true;
		}
		
		String booleanTypeStr = StringUtil.trimNullEmpty(dsElement.getAttribute(BOOLEAN_TYPE));
		if (booleanTypeStr!=null) {
			try {
				booleanDataType = Integer.parseInt(booleanTypeStr);
			} catch (NumberFormatException ex) {
				logger.error("Error parsing attribute: {} value: {}", BOOLEAN_TYPE, booleanTypeStr);
			}
		}
		
		booleanTrue = dsElement.getAttribute(BOOLEAN_TRUE);
		booleanFalse = dsElement.getAttribute(BOOLEAN_FALSE);

		
		
		String dob = dsElement.getAttribute(ATTR_DEFAULT_ORDER_BY);
		if (dob!=null && dob.length()>0) {
			defaultOrderBySQL =" ORDER BY "+dob;
		} else {
			TypeAttribute ta = entityType.getAttribute(primaryKey);
			// TODO handle no primary key differently?
			if (ta!=null) defaultOrderBySQL =" ORDER BY "+ta.getName();
			else throw new RuntimeException("Primary key '"+primaryKey +"' for table: '"+tableName+"' not found! Specify "+ATTR_DEFAULT_ORDER_BY+" or "+ATTR_TABLE_PRIMARY_KEY +" attribute");
		}
		
	}
	
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	protected Connection getConnection (GenericsServerSession gSession) throws SQLException {
		return dbCP.getConnection(false, gSession);
	}
	
	public DBConnectionProvider getConnProvider() {
		return dbCP;
	}
	
	
	protected void close(Connection conn, GenericsServerSession gSession) {
		if(conn == null)
			return;
		try {
			dbCP.rollbackConnection(conn, gSession);
		} catch(SQLException ignore){}
		try {
			dbCP.closeConnection(conn, gSession);
		} catch(SQLException ignore){}
	}
	
	protected void commit(Connection conn, GenericsServerSession gSession) throws SQLException {
		dbCP.commitConnection(conn, gSession);
	}
	
	
	
	
	protected TypeAttribute getPrimaryKey(EntityObject eo) {
		if (primaryKey == null)
			throw new RuntimeException("Datasource without primary key. THIS EXCEPTION SHOULDN'T BE RUNTIME EXCEPTION");
		EntityType type = eo.getType();
		TypeAttribute priKeyTA = type.getAttribute(primaryKey);
		if (priKeyTA == null) 
			throw new RuntimeException("EntityObject doesn't contain primaryKey attribute '"+primaryKey+"'");
		return priKeyTA;
	}

	
	
	public static  void setParameter(PreparedStatement ps, TypeAttribute ta, Object value, int paramIdx) throws SQLException {
		try {
			if (ta.getPrimitiveType()==Types.DATE) {
				if (ta.getSQLType() == java.sql.Types.TIMESTAMP) {
					if (value!=null && !"".equals(value)) {
						Timestamp ts = new Timestamp(Long.parseLong((String)value));
						ps.setTimestamp(paramIdx, ts, Calendar.getInstance(UTCTime));
					} else {
						ps.setTimestamp(paramIdx,null);
					}
				} else {
					if (value!=null && !"".equals(value)) {
						Date date = new Date(Long.parseLong((String)value));
						ps.setDate(paramIdx, date);
					} else {
						ps.setDate(paramIdx, null);
					}
				}
			} else if (ta.getPrimitiveType() == Types.INT) {
				if (value != null && ((String)value).length()>0) {
					ps.setLong(paramIdx, Long.parseLong((String)value));
				} else {
					ps.setNull(paramIdx, java.sql.Types.INTEGER);
				}
			} else if (ta.getPrimitiveType() == Types.FLOAT) {
				if (value !=null && ((String)value).length()>0) {
					ps.setDouble(paramIdx, Double.parseDouble((String)value));
				} else {
					ps.setNull(paramIdx,java.sql.Types.DOUBLE);
				}
			} else if (ta.getPrimitiveType() == Types.STRING) {
				ps.setString(paramIdx, (String)value);
			} else if (ta.getPrimitiveType() == Types.BINARY) {
				ps.setBytes(paramIdx, (byte[])value);
			} else {
				ps.setObject(paramIdx, value);
			}
		} catch (NumberFormatException ex) {
			throw new SQLException("Failed to parse value '"+value+"' for TypeAttribute='"+ta.getName()+"' of type="+ta.getPrimitiveType());
		}
	}
	
	
	protected void convertAndSetParameter(PreparedStatement ps, TypeAttribute ta, Object value, int paramIdx) throws SQLException {
		 if (ta.getPrimitiveType() == Types.BOOLEAN && booleanFalse != null && booleanTrue != null) {
			String newValue = null;
			if (Boolean.TRUE.toString().equals(value)) {
				newValue = booleanTrue;
			} else {
				newValue = booleanFalse;
			}
			TypeAttribute newTa = new TypeAttribute(ta.getId(), ta.getName(), booleanDataType);		
			setParameter(ps, newTa, newValue, paramIdx);
		} else {	
			setParameter(ps, ta, value, paramIdx);
		}
	}
	
	public int applyParameters (List<SQLFilterParameter> parameters, PreparedStatement stmt, int paramIdx) throws SQLException {
		for (SQLFilterParameter p:parameters) {
			convertAndSetParameter(stmt,p.ta , p.value, paramIdx);
			paramIdx++;
		}
		return paramIdx;
	}
	
	protected EntityObject loadByPrimaryKey(Connection conn, String value, GenericsServerSession gSession) throws SQLException {
		EntityObject filterObject = new AbstractEntityObject(entityType.getId());
		EntityUtils.setStringValue(filterObject, primaryKey, value);
		SimpleFilter pkFilter = new SimpleFilter(filterObject);
		ArrayValueHolder vh = getData(pkFilter,  -1, -1, gSession, conn);
		if (vh==null || vh.size()==0)
			return null;
		if (vh.size()>1)
			throw new IllegalArgumentException("Multiple results returned for primary key!!!!!!!!!");
		return (EntityObject)vh.get(0);
	}
	
	
	@Override
	public ArrayValueHolder getData(DataFilter dataFilter, int fromIdx, int toIdx, GenericsServerSession gSession) throws SQLException {		
		Connection conn =  null;
		try {
			conn = getConnection(gSession);    
			ArrayValueHolder data =  getData(dataFilter, fromIdx, toIdx, gSession, conn);
			lookupResolver(data, gSession);
			return data;
		} finally {
			if (conn!=null)
				close(conn,gSession);
		}

	}

	
	private EntityObject processData(EntityObject eo, GenericsServerSession gSession, Connection conn) throws SQLException {
		if (eo.getStatus() == EntityObject.Status.NEW) {
			processInsert(conn, eo);
		} else if (eo.getStatus() == EntityObject.Status.UPDATED) {
			processUpdate(conn, eo);
		} else if (eo.getStatus() == EntityObject.Status.DELETED) {
			processDelete(conn, eo);
			return null;
		} else if (eo.getStatus() == Status.IGNORE) {
			return null;
		} else if (eo.getStatus() == Status.STORED) {
			return eo;
		}
		
		return loadByPrimaryKey(conn, EntityUtils.getStringValue(eo, primaryKey), gSession);
	}
	
	
	@Override
	public  ValueHolder processData(ValueHolder mdHolder, GenericsServerSession gSession) throws SQLException {
		ValueHolder returnValue = null;
		Connection conn = startTransaction(gSession);
		try {			
			returnValue = processValueHolder(mdHolder, new HashMap<Integer,PrimitiveValue>(), this,
					gSession, conn);
			commit(conn, gSession);
		} finally {
			endTransaction(conn, gSession);
		}
		return returnValue;
	}
	
	protected Connection startTransaction(GenericsServerSession gSession) throws SQLException {
		return dbCP.startTransaction(gSession);
	}
	
	protected void endTransaction(Connection conn, GenericsServerSession gSession) throws SQLException {
		dbCP.endTransaction(conn, gSession);
	}

	private void updateValuesFromMaster(EntityObject toUpdate, HashMap<Integer, PrimitiveValue> masterData) {
		if (masterData==null)
			return;
		for (Integer i:masterData.keySet()) {
			toUpdate.setValue(i, masterData.get(i));
		}	
	}
	
	protected ValueHolder processValueHolder(ValueHolder holder, HashMap<Integer,PrimitiveValue> masterData, 
			EntityDatasource ds, GenericsServerSession gSession, Connection conn) throws SQLException {
	
		if (holder instanceof EntityObject) {
			EntityObject eo = (EntityObject)holder;
			updateValuesFromMaster(eo, masterData);
			if (ds instanceof DatabaseDataSource) { // TODO: change to DBDatasource or JDBCdatasource
				return ((DatabaseDataSource)ds).processData(eo, gSession, conn);
			}
			throw new RuntimeException("MasterDetail doesn't support datasource "+ds.toString());
		} else if (holder instanceof ArrayValueHolder) {
			ArrayValueHolder avh = (ArrayValueHolder)holder;
			ArrayValueHolder processedHolder = new ArrayValueHolder(avh.getType());
			Iterator<ValueHolder> it=avh.iterator();
			while (it.hasNext()) {
				ValueHolder vh = it.next();
				ValueHolder rv = processValueHolder(vh, masterData, ds,gSession, conn);
				if (rv!=null) {
					processedHolder.add(rv);
				}	
			}
			processedHolder.setTotalDataCount(processedHolder.size());
			processedHolder.setHasMoreData(false);			
			return processedHolder;
			
		} else if (holder instanceof MasterDetailsHolder) {			
			MasterDetailsHolder mdh = (MasterDetailsHolder)holder;
			EntityDatasource mds = DatasourceFactory.instance().getDatasource(mdh.getMasterDatasourceId());
			EntityObject masterIn = mdh.getMaster();
			updateValuesFromMaster(masterIn, masterData);
			EntityObject masterOut = (EntityObject) processValueHolder(masterIn, masterData, mds, gSession, conn);
			mdh.setMaster(masterOut);
			Map<Integer,Integer> attrMap = mdh.getAttributeMapping();
			masterData = new HashMap<Integer, PrimitiveValue>();
			for (Integer key:attrMap.keySet()) {
				Integer masterId = attrMap.get(key);
				masterData.put(key, (PrimitiveValue) masterOut.getValue(masterId));
			}
			if (mdh.getDetails()!=null) {
				EntityDatasource dds = DatasourceFactory.instance().getDatasource(mdh.getDetailsDatasourceId());
				ValueHolder details = processValueHolder(mdh.getDetails(), masterData, dds, gSession, conn);
				mdh.setDetails(details, mdh.getDetailsDatasourceId());
			}
			return holder;
		} else {
			throw new RuntimeException("ValueHolder of unsupported type "+holder.getClass()+" !");
		}
	}
	
	
	protected abstract void processInsert(Connection conn, EntityObject eo) throws SQLException;
	
	protected void processDelete(Connection conn, EntityObject eo) throws SQLException {
		TypeAttribute priKeyTA = getPrimaryKey(eo);
		if (eo.getPrimitiveValue(priKeyTA.getId())==null ||
				eo.getPrimitiveValue(priKeyTA.getId()).length()==0) {
			throw new SQLException("Unable to delete a record with primaryKey value not set!");
		}
		String sql = "DELETE FROM "+tableName+ " WHERE "+priKeyTA.getName()+"=?";
		LoggableStatement ls = null;
		try {
			ls = new LoggableStatement(conn, sql);
			convertAndSetParameter(ls, priKeyTA, eo.getPrimitiveValue(priKeyTA.getId()), 1);
			ls.execute();
		} finally {
			if (ls!=null)
				ls.close();
		}
	}
	
	
	
	protected void processUpdate(Connection conn, EntityObject eo) throws SQLException {
		ArrayList<TypeAttribute> attributesToUpdate = new ArrayList<TypeAttribute>();
		String toUpdate="";
		TypeAttribute priKeyTA = getPrimaryKey(eo);

		for (TypeAttribute ta:entityType.getAttributes())  {
			if (!ta.dbCanWrite()) // ignore support attributes
				continue;
			if (!ta.equals(priKeyTA)) {
				if (toUpdate.length()!=0)
					toUpdate+=",";
				toUpdate+=ta.getName()+"=?";
				attributesToUpdate.add(ta);
			}
		}
		String sql = "UPDATE "+tableName +" SET "+toUpdate+" WHERE "+priKeyTA.getName()+"=?";
		LoggableStatement stmt = null;
		try {
			stmt = new LoggableStatement(conn,sql);	
			int paramIdx=1;
			for (TypeAttribute ta:attributesToUpdate) {
				if (ta.getPrimitiveType()==Types.BINARY) {
					BinaryValueHolder bvh = (BinaryValueHolder) eo.getValue(ta.getId());
					byte[] byteArray = null;
					if (bvh!=null)
						byteArray=bvh.value;
					convertAndSetParameter(stmt,ta , byteArray, paramIdx);
				} else {
					convertAndSetParameter(stmt,ta , eo.getPrimitiveValue(ta.getId()), paramIdx);
				}
				paramIdx++;
			}
			convertAndSetParameter(stmt, priKeyTA, eo.getPrimitiveValue(priKeyTA.getId()), paramIdx);
			stmt.executeUpdate();
		} finally {
			if (stmt!=null) {
				stmt.close();
			}
		}
	}

	
	public void lookupResolver(ArrayValueHolder avh, GenericsServerSession gSession) {
		Element entityEl = genericsSettings.getEntityMetadataMap().get(getEntityType().getName());
		NodeList childNodes = entityEl.getElementsByTagName(XMLTags.EntityAttribute);
		for (int i=0;i<childNodes.getLength();i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType()==Node.ELEMENT_NODE) {
				Element ea = (Element) node;
				if (MetaAttributes.isTrue(ea.getAttribute(MetaAttributes.LOOKUP))) {
					
					for (ValueHolder vh:avh) {
						EntityObject eo=(EntityObject)vh;
						TypeAttribute attribType = 	eo.getType().getAttribute(ea.getAttribute(MetaAttributes.NAME));//TODO could check if the value is really primitive
						eo.setValue(attribType.getId(), LookupResolver.resolveLookup(ea, (PrimitiveValue) eo.getValue(attribType.getId()), gSession));											
					}
				}
			}
		}
	}
	
	public abstract ArrayValueHolder getData(DataFilter dataFilter, int fromIdx, int toIdx, GenericsServerSession gsession, 
			Connection conn) throws SQLException;

}
