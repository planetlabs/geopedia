package com.sinergise.generics.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.sinergise.common.util.format.Locale;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.builder.CompositeInspector;
import com.sinergise.generics.builder.Inspector;
import com.sinergise.generics.builder.file.DBEntityInspector;
import com.sinergise.generics.builder.file.XMLEntityInspector;
import com.sinergise.generics.builder.filter.SQLFilterHelpers;
import com.sinergise.generics.builder.mysql.MySQLTableInspector;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MasterDetailsHolder;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.ValueHolder;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.LimitFilter;
import com.sinergise.generics.core.i18n.I18nProvider;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.ProcessResultSet;
import com.sinergise.generics.core.throwable.ProcessingException;
import com.sinergise.generics.datasource.DatasourceFactory;
import com.sinergise.generics.datasource.EntityDatasource;
import com.sinergise.generics.datasource.LookupResolver;
import com.sinergise.generics.i18n.PropertyFileLanguage;
import com.sinergise.generics.i18n.ServerI18NResources;
import com.sinergise.generics.impl.GeneratedEntityTypeStorage;
import com.sinergise.generics.impl.XMLEntityBuilder;
import com.sinergise.generics.impl.XmlUtils;
import com.sinergise.generics.java.AbstractEntityTypeStorage;
import com.sinergise.generics.java.DBConnectionProvider;
import com.sinergise.generics.java.DatabaseType;
import com.sinergise.generics.java.GenericsSettings;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.util.sql.LoggableStatement;

public class GenericsServiceImpl extends SessionRemoteServiceServlet implements GenericsService, GenericsSettings {

	static {
		UtilJava.initStaticUtils();
	}

	private static final Logger logger = LoggerFactory.getLogger(GenericsServiceImpl.class);
	private static final long serialVersionUID = -8377807841760775406L;

	protected AbstractEntityTypeStorage entityStorage;
	protected Map<String, Element> entityMetadataMap = new HashMap<String, Element>();
	protected Map<String, Element> widgetMetadataMap = new HashMap<String, Element>();
	protected Map<String, DataFilter> filterMap;
	protected Map<String, String> widgetMetadataCacheMap = new HashMap<String, String>();
	protected File exportDirectory;
	protected ServerI18NResources i18nResource;

	@Override
	public Map<String, DataFilter> getFilters() {
		if (filterMap == null) {
			filterMap = new HashMap<String, DataFilter>();
		}
		return filterMap;
	}

	@Override
	public Map<String, Element> getEntityMetadataMap() {
		return entityMetadataMap;
	}

	protected void resetConfiguration() throws ServletException {
		GeneratedEntityTypeStorage.getInstance().clear();
		entityMetadataMap.clear();
		widgetMetadataCacheMap.clear();
		if (filterMap != null) {
			filterMap.clear();
		}
		widgetMetadataCacheMap.clear();
		DatasourceFactory.instance().clear();
		LookupResolver.purgeLookupCache();
	}

	@Override
	public void init() throws ServletException {
		exportDirectory = new File(System.getProperty("java.io.tmpdir"));
		entityStorage = GeneratedEntityTypeStorage.getInstance();
		setFactory();
		DatasourceFactory.instance().setGenericsSettings(this);
	}

	// FIXME - AP - Why is it deprecated and what should be used instead
	@Deprecated
	protected void processConfigFile(File cfgFile, DBConnectionProvider dbConProvider) throws Exception {
		processConfigFile(cfgFile, dbConProvider, new HashMap<String, Element>());
	}

	protected void processConfigFile(File cfgFile, DBConnectionProvider dbConProvider, HashMap<String, Element> includables)
			throws Exception {
		FileInputStream fis = new FileInputStream(cfgFile);
		try {
			processConfigFile(fis, dbConProvider, includables);
		} finally {
			fis.close();
		}
	}

	protected HashMap<String, Element> loadIncludables(File includablesFile) throws ParserConfigurationException, SAXException, IOException {
		FileInputStream fis = null;
		if (includablesFile.exists()) {
			try {
				fis = new FileInputStream(includablesFile);
				return loadIncludables(fis);
			} catch (FileNotFoundException e) {
				logger.error("File not found!", e);
			} finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return new HashMap<String, Element>();
	}

	protected HashMap<String, Element> loadIncludables(InputStream incStream) throws ParserConfigurationException, SAXException,
			IOException {
		HashMap<String, Element> includablesMap = new HashMap<String, Element>();
		DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xmlDocument = dbuilder.parse(incStream);
		NodeList includablesList = xmlDocument.getElementsByTagName(XMLTags.Includables);
		if (includablesList.getLength() != 1)
			return includablesMap;
		NodeList xmlEntitiesList = ((Element) includablesList.item(0)).getChildNodes();

		for (int i = 0; i < xmlEntitiesList.getLength(); i++) {
			if (xmlEntitiesList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element incElement = (Element) xmlEntitiesList.item(i);
				String name = incElement.getAttribute(MetaAttributes.NAME);
				if (!StringUtil.isNullOrEmpty(name)) {
					includablesMap.put(name, incElement);
				}
			}
		}
		return includablesMap;
	}

	private ArrayList<Element> getOrderedConfigurationSection(Document xmlDocument, String sectionElementName) {
		NodeList mainConfig = xmlDocument.getElementsByTagName(sectionElementName);
		ArrayList<Element> elements = new ArrayList<Element>();
		if (mainConfig.getLength() == 1) {
			Element rootElement = (Element) mainConfig.item(0);
			NodeList nodes = rootElement.getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element elm = (Element) nodes.item(i);
					if (elm.getTagName() == "file") {
						elements.add(elm);
					}
				}
			}
			Collections.sort(elements, new Comparator<Element>() {

				@Override
				public int compare(Element o1, Element o2) {
					int p1 = 0, p2 = 0;
					String p1Str = o1.getAttribute(MetaAttributes.POSITION);
					if (p1Str != null) {
						try {
							p1 = Integer.parseInt(p1Str);
						} catch (NumberFormatException ex) {
							logger.error("Unparsable " + MetaAttributes.POSITION + " attribute for element " + o1.toString());
						}
					}
					String p2Str = o2.getAttribute(MetaAttributes.POSITION);
					if (p2Str != null) {
						try {
							p2 = Integer.parseInt(p2Str);
						} catch (NumberFormatException ex) {
							logger.error("Unparsable " + MetaAttributes.POSITION + " attribute for element " + o2.toString());
						}
					}
					if (p1 < p2)
						return -1;
					else if (p1 > p2)
						return 1;
					return 0;
				}
			});
		}
		return elements;
	}

	enum ConfigurationFileSource {
		DATABASE("database"), FILE("file");
		private String source;

		private ConfigurationFileSource(String source) {
			this.source = source;
		}

		public static ConfigurationFileSource fromString(String source) {
			for (ConfigurationFileSource src : values()) {
				if (src.source.equals(source))
					return src;
			}
			return FILE;
		}
	}

	private static File buildFilePath(File configurationBase, File file) {
		if (file.isAbsolute()) {
			return file;
		}
		return new File(configurationBase, file.getPath());
	}

	protected void loadDatabaseMasterConfiguration(String configurationName, int userId, DBConnectionProvider dbConProvider,
			File configurationBase) throws Exception {
		Connection conn = null;
		try {
			conn = dbConProvider.getConnection();
			Clob xmlClob = loadDatabaseConfigurationFile(userId, configurationName, conn);
			if (xmlClob != null) {
				DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document xmlDocument = dbuilder.parse(xmlClob.getAsciiStream());

				HashMap<String, Element> includables = new HashMap<String, Element>();
				ArrayList<Element> files = getOrderedConfigurationSection(xmlDocument, XMLTags.MainConfiguration.TAG_INCLUDABLES);
				logger.info("Processing includables...");
				for (Element e : files) {
					String fileName = e.getAttribute(XMLTags.MainConfiguration.ATTR_NAME);
					ConfigurationFileSource source = ConfigurationFileSource.fromString(e
							.getAttribute(XMLTags.MainConfiguration.ATTR_SOURCE));
					InputStream is = null;
					if (source == ConfigurationFileSource.DATABASE) {
						logger.info("Trying to load configuration file: '" + fileName + "' from database!");
						Clob cfgClob = loadDatabaseConfigurationFile(userId, e.getAttribute(MetaAttributes.NAME), conn);
						if (cfgClob != null) {
							is = cfgClob.getAsciiStream();
						}
					} else {
						logger.info("Trying to load configuration file: '" + fileName + "' from filesystem!");
						is = getClass().getClassLoader().getResourceAsStream(fileName);
						if (is == null) {
							File file = buildFilePath(configurationBase, new File(fileName));
							if (file.exists()) {
								is = new FileInputStream(file);
							}
						}
					}
					if (is == null) {
						logger.info("File '" + fileName + "' was not found!");
					} else {
						HashMap<String, Element> inc = loadIncludables(is);
						includables.putAll(inc);
					}
				}
				files = getOrderedConfigurationSection(xmlDocument, XMLTags.MainConfiguration.TAG_MAIN);
				logger.info("Processing main configuration.");

				for (Element e : files) {
					String fileName = e.getAttribute(XMLTags.MainConfiguration.ATTR_NAME);
					ConfigurationFileSource source = ConfigurationFileSource.fromString(e
							.getAttribute(XMLTags.MainConfiguration.ATTR_SOURCE));
					InputStream is = null;
					if (source == ConfigurationFileSource.DATABASE) {
						logger.info("Trying to load configuration file: '" + fileName + "' from database!");
						Clob cfgClob = loadDatabaseConfigurationFile(userId, e.getAttribute(MetaAttributes.NAME), conn);
						if (cfgClob != null) {
							is = cfgClob.getAsciiStream();
						}
					} else {
						logger.info("Trying to load configuration file: '" + fileName + "' from filesystem!");
						is = getClass().getClassLoader().getResourceAsStream(fileName);
						if (is == null) {
							File file = buildFilePath(configurationBase, new File(fileName));
							if (file.exists()) {
								is = new FileInputStream(file);
							}
						}
					}
					if (is == null) {
						logger.info("File '" + fileName + "' was not found!");
					} else {
						processConfigFile(is, dbConProvider, includables);
					}
				}

				files = getOrderedConfigurationSection(xmlDocument, XMLTags.MainConfiguration.TAG_LANGUAGE);
				logger.info("Processing language configuration.");

				i18nResource = new ServerI18NResources();

				for (Element e : files) {
					String fileName = e.getAttribute(XMLTags.MainConfiguration.ATTR_NAME);
					String language = e.getAttribute(XMLTags.MainConfiguration.ATTR_LANGUAGE);
					InputStream is = null;
					logger.info("Trying to load language file: '" + fileName + "' from filesystem!");
					is = getClass().getClassLoader().getResourceAsStream(fileName);
					if (is == null) {
						File file = buildFilePath(configurationBase, new File(fileName));
						if (file.exists()) {
							is = new FileInputStream(file);
						}
					}
					if (is == null) {
						logger.info("File '" + fileName + "' was not found!");
					} else {
						i18nResource.addLanguage(new PropertyFileLanguage(buildFilePath(configurationBase, new File(fileName)), language));
					}

				}

			}
		} finally {
			if (conn != null) {
				try {
					dbConProvider.closeConnection(conn, null);
				} catch (SQLException ex) {
				}
			}
		}
	}

	private Clob loadDatabaseConfigurationFile(int userId, String configurationName, Connection conn) throws SQLException {
		LoggableStatement ls = null;
		ResultSet rs = null;
		try {
			ls = new LoggableStatement(conn, "SELECT c.xml.getClobVal() FROM app_admin.config_xml c WHERE c.USER_ID=? AND c.NAME=?");
			ls.setInt(1, userId);
			ls.setString(2, configurationName);
			rs = ls.executeQuery();
			if (rs.next()) {
				return rs.getClob(1);
			}
			return null;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (ls != null) {
				try {
					ls.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

	protected void loadConfigurationFiles(File genericsConfig, DBConnectionProvider dbConProvider, File includablesFile,
			File languagesFolder) throws Exception {
		HashMap<String, Element> includables = new HashMap<String, Element>();
		if (includablesFile != null) {
			logger.info("Loading includables...");
			includables = loadIncludables(includablesFile);
		}
		logger.info("Loading generics configurations...");
		BufferedReader cfgBr = new BufferedReader(new FileReader(genericsConfig));
		String line;
		while ((line = cfgBr.readLine()) != null) {

			if (line.startsWith("#"))
				continue; // allow comments in config file

			File configFile = new File(getServletContext().getRealPath("/" + line));

			if (configFile.exists()) {
				logger.info("	Processing file:'" + configFile.getName() + "'");
				processConfigFile(configFile, dbConProvider, includables);
			} else {
				InputStream is = getClass().getClassLoader().getResourceAsStream(line);
				try {
					processConfigFile(is, dbConProvider, includables);
				} catch (Exception e) {
					throw new Exception("Error processing file " + line + ": " + e, e);
				}
			}

		}
		cfgBr.close();

		if (languagesFolder != null && languagesFolder.exists()) {
			logger.debug("Loading language files...");
			loadLanguagePropertyFiles(languagesFolder);
		}

	}

	protected void processConfigFile(InputStream cfgFile, DBConnectionProvider dbConProvider, HashMap<String, Element> includables)
			throws Exception {
		// build entities
		DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xmlDocument = dbuilder.parse(cfgFile);
		processConfigFile(xmlDocument, dbConProvider, includables);
	}

	protected void processConfigFile(Document xmlDocument, DBConnectionProvider dbConProvider, HashMap<String, Element> includables)
			throws Exception {
		DBEntityInspector dbei = new DBEntityInspector(xmlDocument);
		setOTableInspector(dbConProvider, dbei);

		CompositeInspector insp = new CompositeInspector();
		insp.addInspector(dbei);
		insp.addInspector(new XMLEntityInspector(xmlDocument, XMLTags.XMLEntities, includables));
		logger.info("Building " + XMLTags.DefinedEntities);
		build(xmlDocument, XMLTags.DefinedEntities, insp);

		// build datasources
		logger.info("Initializing datasources.");
		initDatasources(xmlDocument);
		// build widget metadata
		logger.info("Initializing widgets.");
		buildWidgetMetaAttributes(xmlDocument, includables);
		// build filters
		logger.info("Initializing filters.");
		buildFilters(xmlDocument);
	}

	protected void setOTableInspector(DBConnectionProvider dbConProvider, DBEntityInspector dbei) throws SQLException {
		DatabaseType dbType = dbConProvider.getDatabaseType();
		if (DatabaseType.MYSQL.equals(dbType)) {
			dbei.setOTI(new MySQLTableInspector(dbConProvider));
		}
	}

	protected void initDatasources(Document xmlDocument) throws ServletException {
		DatasourceFactory.instance().initializeDatasources(xmlDocument);
	}

	protected void setFactory() throws ServletException {
		DatasourceFactory.instance();
	}

	protected void loadLanguagePropertyFiles(File languageDirectory) throws FileNotFoundException, IOException {
		if (!languageDirectory.isDirectory()) {
			throw new IllegalArgumentException(languageDirectory.getPath() + " does not exist or is not a directory!");
		}
		File[] langFiles = languageDirectory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.contains(".properties"))
					return true;
				return false;
			}

		});
		if (langFiles.length > 0) {
			// build languages
			i18nResource = new ServerI18NResources();
			for (File lang : langFiles) {
				i18nResource.addLanguage(new PropertyFileLanguage(lang));
			}
		}

	}

	@Override
	public String getWidgetMetadata(final String widgetName, final String language) {
		Element el = widgetMetadataMap.get(widgetName);
		if (el == null) {
			logger.warn("No data for widget '" + widgetName + "'.");
			return null;
		}

		String cacheKey = widgetName + "LNG:" + language;

		String data = widgetMetadataCacheMap.get(cacheKey);
		if (data != null) {
			return data;
		}

		final String entityName = el.getAttribute(MetaAttributes.TYPE);
		try {
			String widgetMetadataStr = XmlUtils.nodeToString(el, new I18nProvider() {
				@Override
				public String getAttributeTranslation(String attributeName, String attributeValue) {

					if (i18nResource == null)
						return attributeValue;
					if (MetaAttributes.LABEL.equals(attributeName)) {
						return i18nResource.getWidgetAttributeTranslation(widgetName, entityName, attributeValue, attributeValue, language);
					}
					return attributeValue;
				}
			});
			widgetMetadataCacheMap.put(cacheKey, widgetMetadataStr);
			return widgetMetadataStr;
		} catch (Throwable ex) {
			logger.error("Exception occured while requesting widget '" + widgetName + "' data.", ex);
			Document document = el.getOwnerDocument();
			DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
			LSSerializer serializer = domImplLS.createLSSerializer();
			String str = serializer.writeToString(el);
			logger.error("Widget data: {}", str);

		}
		return null;
	}

	protected void buildFilters(Document xmlDocument) throws ParserConfigurationException, SAXException, IOException {
		NodeList xmlEntitiesList = xmlDocument.getElementsByTagName(XMLTags.Filters);
		if (xmlEntitiesList.getLength() == 1) {
			Element rootElement = (Element) xmlEntitiesList.item(0);
			NodeList nodes = rootElement.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element elm = (Element) nodes.item(i);
					String name = elm.getAttribute(MetaAttributes.NAME);
					try {
						getFilters().put(name, SQLFilterHelpers.createFilterFromNode(elm));
						logger.debug("Initialized filter '{}'.", name);
					} catch (Throwable th) {
						logger.error("Filter '" + name + "' initialization failed! {}", XmlUtils.nodeToString(elm), th);
					}
				}
			}
		}
	}

	// FIXME AP: why is it deprecated and what should be used instead?
	@Deprecated
	protected void buildWidgetMetaAttributes(Document xmlDocument) throws ParserConfigurationException, SAXException, IOException {
		buildWidgetMetaAttributes(xmlDocument, new HashMap<String, Element>());
	}

	protected void buildWidgetMetaAttributes(Document xmlDocument, HashMap<String, Element> includables)
			throws ParserConfigurationException, SAXException, IOException {
		NodeList xmlEntitiesList = xmlDocument.getElementsByTagName(XMLTags.Widgets);
		if (xmlEntitiesList.getLength() == 1) {
			Element rootElement = (Element) xmlEntitiesList.item(0);
			NodeList nodes = rootElement.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element elm = (Element) nodes.item(i);
					buildSingleWidget(elm, includables);
				}
			}
		}
	}

	protected void buildSingleWidget(Element elm, HashMap<String, Element> includables) {
		String name = elm.getAttribute(MetaAttributes.NAME);
		try {
			String type = elm.getAttribute(MetaAttributes.TYPE);
			Element entityTypeDefinitionElement = entityMetadataMap.get(type);
			if (entityTypeDefinitionElement != null) {
				Element master = (Element) entityTypeDefinitionElement.cloneNode(true);
				String toIncludeName = elm.getAttribute(MetaAttributes.META_INCLUDE);
				if (!StringUtil.isNullOrEmpty(toIncludeName)) {
					Element toIncludeElement = includables.get(toIncludeName);
					if (toIncludeElement != null) {
						XmlUtils.combineElements(master, toIncludeElement, MetaAttributes.NAME, MetaAttributes.NAME);
					}
				}
				XmlUtils.combineElements(master, elm, MetaAttributes.NAME, MetaAttributes.NAME);
				XmlUtils.fixLabels(master);
				widgetMetadataMap.put(name, master);
				logger.debug("Widget {} initialized.", name);
			} else {
				logger.error("Widget initialization failed! EntityType {} does not exist! {}", type, XmlUtils.nodeToString(elm));
			}
		} catch (Throwable th) {
			logger.error("Widget '" + name + "' initialization failed! {}", XmlUtils.nodeToString(elm), th);
		}

	}

	@Override
	public ArrayValueHolder getCollectionValues(DataFilter filter, String datasourceID, int startIdx, int stopIdx) {
		try {
			GenericsServerSession gSession = sessionManager.getCurrentSession();
			ArrayValueHolder avh = DatasourceFactory.instance().getData(datasourceID, filter, startIdx + 1, stopIdx + 1, gSession);
			return avh;
		} catch (SQLException e) {
			logger.error("Database error while getCollectionValues from datasource '" + datasourceID + "'", e);
		} catch (Throwable th) {
			logger.error("Error while getCollectionValues from datasource '" + datasourceID + "'", th);
		}
		return null;
	}

	@Override
	public EntityType[] getEntityTypes() {
		Collection<EntityType> typesCollection = entityStorage.getTypes();
		EntityType[] types = typesCollection.toArray(new EntityType[typesCollection.size()]);
		return types;
	}

	@Override
	public String getEntityMetadata(String entityTypeName) {
		Element el = entityMetadataMap.get(entityTypeName);
		if (el == null)
			return null;
		return XmlUtils.nodeToString(el);
	}

	public void build(Document xmlDocument, String baseEntityStore, Inspector inspector) throws ParserConfigurationException, SAXException,
			IOException {
		NodeList xmlEntitiesList = xmlDocument.getElementsByTagName(baseEntityStore);
		if (xmlEntitiesList.getLength() == 1) {
			Element rootElement = (Element) xmlEntitiesList.item(0);
			NodeList nodes = rootElement.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element elm = (Element) nodes.item(i);
					String type = elm.getAttribute(MetaAttributes.TYPE);
					logger.debug("Running inspectors for entity type: '{}'", type);
					Element el = inspector.inspect(type);
					entityMetadataMap.put(type, el);
					XMLEntityBuilder.inspect(el);
				}
			}
		}
	}

	/****
	 * 
	 * Use case best practices: Djuro (Kontrole) "Unpacking" of a view entity
	 * and saving to two separate datasources (tables) could be done by
	 * overriding this method TODO (pkolaric): write an example
	 */
	@Override
	public ProcessResultSet processEntities(ValueHolder values, String datasourceID) throws Exception {
		GenericsServerSession gSession = sessionManager.getCurrentSession();
		ValueHolder vh = DatasourceFactory.instance().processData(values, datasourceID, gSession);
		ProcessResultSet prs = new ProcessResultSet();
		prs.valueHolder = vh;
		return prs;
	}

	@Override
	public String[] processEntitiesWithReturnInformation(ValueHolder values, String datasourceID) throws Exception {
		GenericsServerSession gSession = sessionManager.getCurrentSession();
		ValueHolder vh = DatasourceFactory.instance().processData(values, datasourceID, gSession);
		return null;
	}

	@Override
	public ProcessResultSet processMasterDetails(MasterDetailsHolder holder) throws Exception {

		GenericsServerSession gSession = sessionManager.getCurrentSession();
		try {
			EntityDatasource eds = DatasourceFactory.instance().getDatasource(holder.getMasterDatasourceId());
			if (eds != null && eds instanceof EntityDatasource) {
				ValueHolder vh = eds.processData(holder, gSession);
				ProcessResultSet prs = new ProcessResultSet();
				prs.valueHolder = vh;
				return prs;
			}
			return null;
		} catch (SQLException ex) {
			logger.error("Master-Details processing failed", ex);
			throw new ProcessingException("Processing failed!", ex);
		}

	}

	@Override
	public String updateSessionLocale(String localeToSelect) {
		GenericsServerSession gSession = sessionManager.getCurrentSession();
		String selectedLocale = null;
		if (localeToSelect == null || localeToSelect.length() == 0) {
			selectedLocale = gSession.getLocale().toString();
		} else {
			Locale loc = gSession.updateLocale(localeToSelect);
			if (loc != null)
				selectedLocale = loc.toString();
			else
				throw new RuntimeException("Unsupported locale: '" + localeToSelect + "'!");
		}
		return selectedLocale;
	}

	@Override
	public void invalidateCache(String datasourceId) {
		GenericsServerSession gSession = sessionManager.getCurrentSession();
		if (datasourceId == null)
			LookupResolver.purgeLookupCache();
		else
			LookupResolver.purgeLookupCache(datasourceId, gSession.getLocale());
	}

	@Override
	public String prepareExportFile(DataFilter filter, String datasourceID, GenericObjectProperty[] properties, DataExportTypes exportType) {
		GenericsServerSession gSession = sessionManager.getCurrentSession();

		try {
			int step = 200;
			int stopIdx = 0;
			int startIdx = 0;

			int customLimit = Integer.MIN_VALUE;

			if (filter instanceof CompoundDataFilter) {
				LimitFilter limitFilter = ((CompoundDataFilter) filter).getLimitFilter();
				if (limitFilter != null)
					customLimit = limitFilter.getLimitNumber();
			}

			File exportFile = File.createTempFile("export", GenericsDataExporter.getExtensionForType(exportType), exportDirectory);
			exportFile.deleteOnExit();

			GenericsDataExporter exporter = GenericsDataExporter.create(exportFile, exportType);
			boolean addHeaderRow = true;

			if (customLimit > 0) {
				// the maximal number of rows is defined
				ArrayValueHolder avh = DatasourceFactory.instance().getData(datasourceID, filter, 1, customLimit, gSession);
				addHeaderRow = exporterAddRows(properties, exporter, addHeaderRow, avh);

			} else {
				// get all rows in batches
				while (true) {
					stopIdx = startIdx + step;
					ArrayValueHolder avh = DatasourceFactory.instance().getData(datasourceID, filter, startIdx + 1, stopIdx + 1, gSession);
					addHeaderRow = exporterAddRows(properties, exporter, addHeaderRow, avh);

					if (!avh.hasMoreData())
						break;
					startIdx += (step + 1);
				}
			}

			exporter.close();
			return exportFile.getName();
		} catch (Throwable th) {
			logger.error("Error while preparing export for datasource '" + datasourceID + "'", th);
		}
		return null;
	}

	private boolean exporterAddRows(GenericObjectProperty[] properties, GenericsDataExporter exporter, boolean addHeaderRow,
			ArrayValueHolder avh) {
		for (int i = 0; i < avh.size(); i++) {
			ValueHolder vh = avh.get(i);
			EntityObject eo = (EntityObject) vh;
			EntityType et = eo.getType();
			if (addHeaderRow) {
				exporter.addHeaderRow(properties, et);
				addHeaderRow = false;
			}
			exporter.addDataRow(properties, eo, et);
		}
		return addHeaderRow;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String cmd = req.getParameter(GenericsService.PARAM_CMD);
		if (GenericsService.CMD_DOWNLOAD.equals(cmd)) {
			String fileName = req.getParameter(GenericsService.PARAM_FILE);
			if (fileName != null) {
				File file = new File(exportDirectory, fileName);
				if (!file.exists())
					return;
				try {
					ServletContext sc = getServletContext();
					String mimeType = sc.getMimeType(fileName);
					resp.setContentType(mimeType);
					resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);

					FileInputStream in = new FileInputStream(file);
					OutputStream out = resp.getOutputStream();
					byte[] buf = new byte[1024];
					int count = 0;
					while ((count = in.read(buf)) >= 0) {
						out.write(buf, 0, count);
					}
					in.close();
					out.close();
				} finally {
					file.delete();
				}
			}
		}

	}
}
