package com.sinergise.generics.datasource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sinergise.common.util.InstanceAlreadySetException;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.throwable.ProcessingException;
import com.sinergise.generics.datasource.mysql.MySQLDataSource;
import com.sinergise.generics.datasource.xml.XMLDataSource;
import com.sinergise.generics.i18n.datasource.DatasourceTranslationProvider;
import com.sinergise.generics.impl.XmlUtils;
import com.sinergise.generics.java.DBConnectionProvider;
import com.sinergise.generics.java.GenericsSettings;
import com.sinergise.generics.server.GenericsServerSession;


public class DatasourceFactory {
	
	private static DatasourceFactory INSTANCE;
	
	public static void initialize() {
		initialize(new DatasourceFactory());
	}
	
	protected static void initialize(DatasourceFactory table) {
		if (INSTANCE != null) {
			throw new InstanceAlreadySetException(INSTANCE, table);
		}
		INSTANCE = table;
	}
	
	public static DatasourceFactory instance() {
		if (INSTANCE == null) {
			initialize();
		}
		return INSTANCE;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(DatasourceFactory.class);
	
	public static final String DS_GENERIC_ORACLE ="oracle";
	public static final String DS_GENERIC_XML="XML";
	public static final String DS_GENERIC_MYSQL="mysql";
	public static final String DS_GENERIC_MSSQL="mssql";
	public static final String DS_GENERIC_POSTGRES = "postgres";
	
	DBConnectionProvider cp = null;
	
	private HashMap<String, EntityDatasource> datasourceMap = new HashMap<String, EntityDatasource>();
	protected GenericsSettings genericsSettings = null;
	protected String basePath = "";
	
	
	public void setGenericsSettings(GenericsSettings gSettings) {
		genericsSettings = gSettings;
	}
	
	public void setBasePath(String basePath) {
		this.basePath=basePath;
	}

	public void setDBConnectionProvider (DBConnectionProvider dcp) {
		this.cp = dcp;
	}

	public EntityDatasource getDatasource(String dsId) {
		EntityDatasource oeds = datasourceMap.get(dsId);
		if (oeds==null)
			throw new RuntimeException("Unable to find datasource with name/id '"+dsId+"'");
		return oeds;
	}

	/**
	 * For custom datasources extend class and override this method.
	 * Add generally usefull datasources to core 
	 * @param dsId
	 * @return
	 */
	
	protected EntityDatasource createDatasourceForType(String datasourceType, Element datasourceAttributes) {
		if (DS_GENERIC_XML.equals(datasourceType)) {
			return new XMLDataSource(datasourceAttributes, genericsSettings, basePath);
			
		} else if (DS_GENERIC_MYSQL.equals(datasourceType)) {
			return new MySQLDataSource(datasourceAttributes, genericsSettings);
		}

		return null;
	}
	
	private void createDatasource(Element el) {
		String type = el.getAttribute("datasourceType");
		EntityDatasource eds =createDatasourceForType(type, el);
		if (eds==null)
			throw new RuntimeException("Unsupported datasource: '"+type+"'");
		if (datasourceMap.containsKey(eds.getDatasourceName())) {
			throw new RuntimeException("Datasource '"+eds.getDatasourceName()+"' already initialized!");
		}
		extractTranslationProviders(eds, el);
		datasourceMap.put(eds.getDatasourceName(), eds);		
		logger.debug("Initialized datasource '{}' type: '{}'.",eds.getDatasourceName(), type);
	}
	
	
	private void extractTranslationProviders(EntityDatasource ds, Element el) {
		NodeList tpNodeList = el.getElementsByTagName("TranslationProvider");
		if (tpNodeList.getLength()<=0)
			return;
		ArrayList<DatasourceTranslationProvider> providers = new ArrayList<DatasourceTranslationProvider>();
		for (int i=0;i<tpNodeList.getLength();i++) {
			if (tpNodeList.item(i).getNodeType()==Node.ELEMENT_NODE) {
				Element elm = (Element)tpNodeList.item(i);
				DatasourceTranslationProvider dtp = DatasourceTranslationProvider.create(ds, elm, basePath);
				providers.add(dtp);
			}
		}
		ds.setTranslationProviders(providers);
	}
	
	
	public void initializeDatasources(Document xmlDocument) {
		try {
			NodeList entityDSList = xmlDocument.getElementsByTagName(XMLTags.Datasources);
			if (entityDSList.getLength()==1) {
				Element rootElement = (Element) entityDSList.item(0);
				NodeList nodes = rootElement.getChildNodes();
				for (int i=0;i<nodes.getLength();i++) {
					if (nodes.item(i).getNodeType()==Node.ELEMENT_NODE) {
						Element elm = (Element)nodes.item(i);
						try {
						createDatasource(elm);
						} catch (Throwable th) {
							logger.error("Exception while processing Datasource element: "+XmlUtils.nodeToString(elm), th);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Exception while initializing datasources!", ex);
		}
	}
	
	public ArrayValueHolder getData (String dataSourceName, DataFilter filter, int fromIdx, int toIdx, GenericsServerSession gSession) throws Exception {
		EntityDatasource ds = getDatasource(dataSourceName);
		return ds.getData(filter, fromIdx, toIdx, gSession);
	}


	public ValueHolder processData(ValueHolder values, String datasourceID, GenericsServerSession gSession) throws Exception {
		try {
		EntityDatasource ds = getDatasource(datasourceID);
		return ds.processData(values, gSession);
		} catch (SQLException ex) {
			logger.error("Exception while processData!",ex);
			throw new ProcessingException("Processing failed!",ex);
		}
	}

	public void removeDatasource(String name) {
		datasourceMap.remove(name);
	}
	
	public void clear() {
		datasourceMap.clear();
	}
	
	public void addDatasource(String name, EntityDatasource ds) {
		datasourceMap.put(name,  ds);
	}
	public boolean isDatasourceCreated(String name) {
		return datasourceMap.containsKey(name);
	}
}
