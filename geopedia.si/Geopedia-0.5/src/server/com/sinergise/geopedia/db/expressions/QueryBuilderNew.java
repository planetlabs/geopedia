package com.sinergise.geopedia.db.expressions;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.Main;
import com.sinergise.geopedia.config.InstanceConfiguration;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.db.TableAndFieldNames;
import com.sinergise.geopedia.style.symbology.rhino.FillSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.LineSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.PaintingPassImpl;
import com.sinergise.geopedia.style.symbology.rhino.PointSymbolizerImpl;
import com.sinergise.geopedia.style.symbology.rhino.SymbolizerFontImpl;
import com.sinergise.geopedia.style.symbology.rhino.SymbologyImpl;
import com.sinergise.geopedia.style.symbology.rhino.TextSymbolizerImpl;


// TODO: 
// do not "deselect" fields that are already marked as to be selected
public class QueryBuilderNew {

	private Table baseTable;
	private String baseTableAlias = null;
	private HashSet<QueryField> baseTableFields = new HashSet<QueryField>();	
	private ArrayList<QueryField> fieldsList = new ArrayList<QueryField>();
	private ArrayList<Object> parameters = new ArrayList<Object>();
	
	private ConditionBuilder queryCondition = new ConditionBuilder();
	
	
	
	
	public class  ConditionBuilder extends ArrayList<Object> {
		private static final long serialVersionUID = 1961812606530651396L;

		public void append(String string){
			add(string);
		}
		
		public void append(QueryField fld) {
			add(fld);
		}

		public String buildSQL() {
			if (size()==0)
				return "";
			StringBuffer sb = new StringBuffer();
			sb.append(" WHERE ");
			
			Iterator<Object> it = iterator();
			while (it.hasNext()) {
				sb.append(it.next().toString());
			}
			return sb.toString();
		}

	}
	
	
	public QueryBuilderNew (Table baseTable) {
		this.baseTable=baseTable;
	}
	
	public ArrayList<Object> getParameters() {
		return parameters;
	}
	public void addParameter(Object param) {
		parameters.add(param);
	}
	
	public ConditionBuilder getCondition() {
		return queryCondition;
	}
	
	public String getBaseTableAlias() {
		return baseTableAlias;
	}
	
	public QueryField addBaseTableUserField(int fieldId) {		
		QueryField field = QueryField.createField(baseTable, TableAndFieldNames.FeaturesTable.FLD_USERFIELD+String.valueOf(fieldId));
		baseTableFields.add(field);
		field.setSelect(true);
		return field;		
	}
	
	public QueryField addBaseTableMetaField(String metaFieldType) {		
		QueryField field = QueryField.createField(baseTable, metaFieldType+String.valueOf(baseTable.getId()));
		baseTableFields.add(field);
		field.setSelect(true);
		return field;		
	}
	
	
	public QueryField addField(String jsFieldIdentifier) {
		return addField(jsFieldIdentifier, true);
	}
	
	public QueryField addField(String jsFieldIdentifier, boolean select) {
	
		if (StringUtil.isNullOrEmpty(jsFieldIdentifier)) {
			throw new RuntimeException("Change to geopedia exception");
		}
		
		String fieldPath[] = jsFieldIdentifier.split(AbstractExpressionEvaluator.FIELD_DELIMITER);
		
		
		
		QueryField field = null;
		for (String fieldDefinition:fieldPath) {
			if (field==null) {
				for (QueryField btField:baseTableFields) {
					if (btField.equals(fieldDefinition)) {
						field=btField;
						break;
					}
				}
				if (field==null) { // not found, create and add
					field = QueryField.createField(baseTable, fieldDefinition);
					baseTableFields.add(field);
				}				
			} else {
				field = field.createChildField(fieldDefinition);
			}
		
		}
		field.setSelect(select);
		return field;
	}
	
	
	private static String tableAlias(int aliasId) {
		return "tba"+String.valueOf(aliasId);
	}
	
	private int tableAliasId;
	private void createAliases() {
		tableAliasId=0;
		baseTableAlias = tableAlias(tableAliasId);
		for (QueryField qf:baseTableFields) {
			qf.setTableAlias(baseTableAlias);
			createAliases(qf);
			
		}
	}
	private void createAliases(QueryField field) {
		if (!field.hasChildFields())
			return;
		tableAliasId++;
		String alias = tableAlias(tableAliasId);
		for (QueryField qf:field.getChildFields()) {
			qf.setTableAlias(alias);
			createAliases(qf);
		}
	}
	
	private void display() {
		for (QueryField qf:baseTableFields) {
			qf.display("");
		}
	}
	
	public ArrayList<QueryField> getQueryFieldsList() {
		return fieldsList;
	}
	
	public String buildSQL(InstanceConfiguration instanceConfig) {
		return buildSQL(true, instanceConfig);
	}
	public String buildSQL(boolean appendSelection, InstanceConfiguration instanceConfig) {
		createAliases();
		StringBuffer tableJoinsBuffer = new StringBuffer();
		fieldsList.clear();
		for (QueryField fields:baseTableFields) {
			processField(fields, tableJoinsBuffer, fieldsList, instanceConfig);
		}
		
		StringBuffer buffer = new StringBuffer();
		if (appendSelection) {
			buffer.append("SELECT ");
			boolean first=true;
			for (QueryField f:fieldsList) {
				if (!first) buffer.append(", ");
				buffer.append(f.getFieldSQLName(true));
				first=false;
			}
		}
		buffer.append(" FROM "+TableAndFieldNames.FeaturesTable.table(baseTable.getId(), instanceConfig)+" "+tableAlias(0));
		buffer.append(tableJoinsBuffer);
		return buffer.toString();
	}
	
	private void processField(QueryField field, StringBuffer joins,	ArrayList<QueryField> fieldsList, InstanceConfiguration instanceConfig) {		
		fieldsList.add(field);
		boolean joinProcessed = false;
		for (QueryField childField:field.getChildFields()) {
			if (!joinProcessed) {
				joins.append(" LEFT JOIN ");
				joins.append(TableAndFieldNames.FeaturesTable.table(childField.getTableId(), instanceConfig)+" "+childField.getTableAlias());
				joins.append(" ON "+ field.getFieldSQLName(true)+"="+childField.getIDMetaField(true));
				joinProcessed=true;
			}			
			processField(childField, joins, fieldsList, instanceConfig);
		}
			
			
		
	}
	
	
	
	public static void main(String[] args) {
		try {
//			Main.initialize(null);
//			ServerInstance instance = ServerInstance.getInstance(ServerInstance.INSTANCE_ID_GEOPEDIASI);

			Context globalContext = Context.enter();
			final ScriptableObject scopeShared = globalContext.initStandardObjects(null, true);
			
			ScriptableObject.defineClass(scopeShared, LineSymbolizerImpl.class);
			ScriptableObject.defineClass(scopeShared, PointSymbolizerImpl.class);
			ScriptableObject.defineClass(scopeShared, FillSymbolizerImpl.class);
			ScriptableObject.defineClass(scopeShared, SymbologyImpl.class);
			ScriptableObject.defineClass(scopeShared, PaintingPassImpl.class);
			ScriptableObject.defineClass(scopeShared, TextSymbolizerImpl.class);
			ScriptableObject.defineClass(scopeShared, SymbolizerFontImpl.class);
//			globalContext.evaluateReader(scopeShared, new FileReader(new File("js/esprima.js")), "esprima.js",1,null);
			globalContext.evaluateReader(scopeShared, new FileReader(new File("js/uglifyjs.1.2.5.min.js")), "uglifyjs.1.2.5.min.js", 1, null);
			globalContext.evaluateReader(scopeShared, new FileReader(new File("js/utility.js")), "utility.js", 1, null);
			globalContext.evaluateReader(scopeShared, new FileReader(new File("js/utilityJava.js")), "utilityJava.js",1,null);
			scopeShared.sealObject();
			
//			Table table = instance.getMetaData().getTableById(14996);
			final Table table = null;

			final String style="return sf.Symbology([sf.PaintingPass(" +
					"[sf.LineSymbolizer({opacity: 1.0,displacementX: 0.0,displacementY: 0.0,lineType: 'SOLID',stroke: 0xff000000,strokeWidth: 1.0})" +
					", (sf.TextSymbolizer({font: sf.SymbolizerFont({fontWeight:'BOLD'})})" +
					")" +
					"])]);";
			
			int maxThreads = 3;
		    ThreadPoolExecutor executorService = new ThreadPoolExecutor(
				maxThreads, // core thread pool size
				maxThreads, // maximum thread pool size
				1, // time to wait before resizing pool
				TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10*maxThreads, true),
				new ThreadPoolExecutor.CallerRunsPolicy());

			
		    long avg = 0;
		    for (int j=0;j<1;j++) {
			    long start = System.currentTimeMillis();
			    for (int i=0;i<1;i++) {
				QueryBuilderNew qbn = new QueryBuilderNew(table);
				StyleEvaluator rr = new StyleEvaluator(style, qbn, scopeShared);
				rr.preEvaluate(10,3d);
				Symbology symb = rr.evaluate(null);
			    }
			    long time = System.currentTimeMillis()-start;
			    avg+=time;
			    System.out.println(time);
		    }
		    System.out.println(avg/10.0);
		    for (int i=0;i<0;i++) {
		    	final int executionId = i;
				executorService.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
						System.out.print(" Executing "+executionId);
						QueryBuilderNew qbn = new QueryBuilderNew(table);
						StyleEvaluator rr = new StyleEvaluator(style, qbn, scopeShared);
						rr.preEvaluate(10,3d);
						Symbology symb = rr.evaluate(null);
						System.out.println("  symbCount:"+symb.getPaintingPasses().length);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
		    }
			
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}

				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				}
			} catch (InterruptedException ex) {
				executorService.shutdownNow();
				Thread.currentThread().interrupt();
			}

			/*
			DBPoolHolder dbPoolHolder = instance.getDBPoolHolder();
			Connection conn = dbPoolHolder.getLocal();
			LoggableStatement ls = new LoggableStatement(conn, qbn.buildSQL());
			ResultSet rs = ls.executeQuery();
			
			while (rs.next()) {
				HashMap<QueryField, Object> resultsMap = new HashMap<QueryField, Object>();
				for (QueryField qf:qbn.getQueryFieldsList()) {
					resultsMap.put(qf, rs.getObject(qf.getFieldSQLName(true)));
				}
				System.out.println("-- " +rr.evaluate(resultsMap));
			}*/
			
			
			Context evaluateCx = Context.enter();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	}
	public static void main1(String[] args) {
		try {
			Main.initialize(null);
			ServerInstance instance = ServerInstance.getInstance(ServerInstance.INSTANCE_ID_GEOPEDIASI);
			
			Table table = instance.getMetaData().getTableById(12126);
			QueryBuilderNew qbn = new QueryBuilderNew(table);
			String fields[] = new String[]{"f32101.f27232", "f33992.f32091","f33992.id12123", "f33992.f32092", "f33995.f32091"};
			for (String field:fields) {
			QueryField fld = qbn.addField(field);
			System.out.println("field added "+fld);
			}
			qbn.createAliases();
			qbn.display();
			System.out.println(qbn.buildSQL(instance.getConfiguration()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
}
