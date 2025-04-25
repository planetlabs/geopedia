package com.sinergise.generics.builder.filter;

import static com.sinergise.common.util.string.WildcardUtil.WCARD_ANSI_LIKE_ESC_AT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sinergise.common.util.lang.Predicate;
import com.sinergise.common.util.lang.Predicate.Composite.CompositionOp;
import com.sinergise.common.util.string.WildcardUtil;
import com.sinergise.common.util.string.WildcardUtil.Wildcards;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.core.filter.AttributesFilter;
import com.sinergise.generics.core.filter.CompoundDataFilter;
import com.sinergise.generics.core.filter.CompoundEntityFilter;
import com.sinergise.generics.core.filter.DataFilter;
import com.sinergise.generics.core.filter.DataFilter.OrderOption;
import com.sinergise.generics.core.filter.FilterUtils;
import com.sinergise.generics.core.filter.NamedSQLFilter;
import com.sinergise.generics.core.filter.OrderFilter;
import com.sinergise.generics.core.filter.SQLFilterParameter;
import com.sinergise.generics.core.filter.SimpleFilter;
import com.sinergise.generics.core.filter.SimpleSQLFilter;
import com.sinergise.generics.core.filter.predicate.EOAttributeComparator;
import com.sinergise.generics.core.filter.predicate.IEntityObjectFilter;
import com.sinergise.generics.java.AbstractEntityTypeStorage;
import com.sinergise.generics.java.GenericsSettings;
import com.sinergise.generics.java.MetaAttributeUtils;
import com.sinergise.generics.server.GenericsServerSession;
import com.sinergise.java.util.format.JavaDateTimeFormatPatterns;

public class SQLFilterHelpers {

	
	public static DataFilter createFilterFromNode (Node node) {
		
		String nodeData = null;
		if (node.getFirstChild() != null && 
				(node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE || node.getFirstChild().getNodeType() == Node.TEXT_NODE )) {
			Text textNode = (Text)node.getFirstChild();
			nodeData = textNode.getData();
			if (nodeData!=null)
				nodeData = nodeData.trim();
		}
		if (DataFilter.FILTER_SIMPLESQL.equals(node.getNodeName())) {
			return new SimpleSQLFilter(nodeData);
		} else if (DataFilter.FILTER_NAMEDSQL.equals(node.getNodeName())) {
			return new NamedSQLFilter(nodeData);
		}
		throw new IllegalArgumentException("Unable to create filter for node '"+node.getNodeName()+"'");
	}
	
	public static SimpleSQLFilter createFilterFor (DataFilter dataFilter, GenericsSettings genericsSettings,
			GenericsServerSession gSession) {

		if (dataFilter == null) // dummy filter - does nothing
			return new SimpleSQLFilter();
		
		if (dataFilter instanceof CompoundDataFilter) {
			CompoundDataFilter cdf = (CompoundDataFilter) dataFilter;
			String SQL ="";
			ArrayList<SQLFilterParameter> params = new ArrayList<SQLFilterParameter>();
			for (int i=0;i<cdf.getLength();i++)  {
				SimpleSQLFilter flt = createFilterFor(cdf.getFilter(i), genericsSettings, gSession);
				SQL+=SimpleSQLFilter.getOperatorString(cdf.getOperator(i))+" ("+flt.getSQLStatement()+") ";
				params.addAll(flt.getParameterList());
			}
			
			return new SimpleSQLFilter(SQL,params);
		}
		
		CompoundEntityFilter cef = null;
		if ( dataFilter instanceof SimpleFilter) {
			
			cef = new CompoundEntityFilter();
			cef.addFilter((SimpleFilter)dataFilter);
			return createSimpleSQLFilterFor(cef, genericsSettings, gSession);
		} else if (dataFilter instanceof CompoundEntityFilter) {
			cef = (CompoundEntityFilter)dataFilter;
			if (!cef.isValid()) // empty CEF filter
				return new SimpleSQLFilter();
			return createSimpleSQLFilterFor(cef, genericsSettings, gSession);
		} else if (dataFilter instanceof SimpleSQLFilter) { 
			SimpleSQLFilter ssf =  (SimpleSQLFilter)dataFilter;
			dataFilter = updateFilterFromSettings(dataFilter, genericsSettings);
			return ssf;
		} else if (dataFilter instanceof NamedSQLFilter){
			dataFilter = updateFilterFromSettings(dataFilter, genericsSettings);
			SimpleSQLFilter ssf = ((NamedSQLFilter)dataFilter).toSimpleSQLFilter();
			return ssf;
		} else if (dataFilter instanceof AttributesFilter) {
			AttributesFilter filter = (AttributesFilter) dataFilter;
			EntityType et = AbstractEntityTypeStorage.getInstance().getEntityType(filter.getEntityTypeId());
			StringBuffer buffer = new StringBuffer();
			ArrayList<SQLFilterParameter> list = new ArrayList<SQLFilterParameter>();
			entityObjectFiltertoSQLString(et, filter.getFilter(), buffer, list);
			return new SimpleSQLFilter(buffer.toString(), list);
		} else 
			throw new RuntimeException("Filter type "+dataFilter.getClass()+" not supported.");
	}
	
	private static void entityObjectFiltertoSQLString (EntityType et, IEntityObjectFilter eof,StringBuffer sqlBuffer, ArrayList<SQLFilterParameter> paramList) {
		if (eof instanceof EOAttributeComparator) {
			EOAttributeComparator cmp = (EOAttributeComparator) eof;
			TypeAttribute ta = et.getAttribute(cmp.getAttributeName());
			if (ta==null) 
				throw new RuntimeException("Attribute '"+cmp.getAttributeName()+"' does not exist for entity type:'"+et.getName()+"'");			
			sqlBuffer.append(ta.getName()+"=?");
			paramList.add(new SQLFilterParameter(null, ta.getPrimitiveType(), cmp.getAttributeValue()));
		} else if (eof instanceof Predicate.Composite) {
			 Predicate.Composite<IEntityObjectFilter, AbstractEntityObject> comp = ( Predicate.Composite<IEntityObjectFilter, AbstractEntityObject>) eof;
			 sqlBuffer.append(" (");
			 boolean first=true;
			 for (IEntityObjectFilter f:comp) {
				 if (!first) {
					 if (comp.getOperation()==CompositionOp.AND)
						 sqlBuffer.append(" AND ");
					 else
						 sqlBuffer.append(" OR ");
				 }
				 entityObjectFiltertoSQLString(et, f, sqlBuffer, paramList);
				 first = false;
			 }
			 sqlBuffer.append(" )");
		}		
	}

	public static String getOrderSQL(OrderFilter filter) {
		EntityType type = AbstractEntityTypeStorage.getInstance().getEntityType(filter.getEntityTypeName());
		String sql="";
		OrderOption[] orderBy = filter.getOrderBy();

		for (int idx:filter.getOrderSequence()) {
			TypeAttribute ta = type.getAttribute(idx);
			if (!ta.dbCanRead()) // ignore support attributes
				continue;
			if (orderBy[idx]!=null && orderBy[idx].isOn()) {
				if (sql.length()>0) sql+=",";
				sql+=ta.getName();
				
				if (orderBy[idx]==OrderOption.DESC)
					sql+=" DESC";
				else 
					sql+=" ASC";
			}
		}
		if (sql.length()>0) {
			return " ORDER BY "+sql;
		}
		return sql;
	}
	private static DataFilter updateFilterFromSettings (DataFilter filter, GenericsSettings settings){
		if (filter instanceof SimpleSQLFilter){
			
			SimpleSQLFilter ssqlf = (SimpleSQLFilter)filter;
			String name = ssqlf.getSQLStatement();
			if (!name.startsWith("@"))
				return ssqlf;
			
			SimpleSQLFilter storedFilter = (SimpleSQLFilter) settings.getFilters().get(name.substring(1));
			if (storedFilter==null)
				throw new RuntimeException("Unable to find filter: '"+name+"'");
			storedFilter.setParameterList(ssqlf.getParameterList());
			return storedFilter;
		} else if (filter instanceof NamedSQLFilter){
			NamedSQLFilter nsqlf = (NamedSQLFilter) filter;
			String name = nsqlf.getSQLStatement();
			if (!name.startsWith("@"))
				return nsqlf;
			
			NamedSQLFilter storedFilter = (NamedSQLFilter) settings.getFilters().get(name.substring(1));
			if (storedFilter==null)
				throw new RuntimeException("Unable to find filter: '"+name+"'");
			storedFilter.setFilterParameters(nsqlf.getFilterParameters());
			return storedFilter;
		}
		return filter;
	}
	
	
	
	
	
	private static class FilterPart {
		public String SQLStatement = "";
		public List<SQLFilterParameter> parameters = new ArrayList<SQLFilterParameter>();
	}
	// move to oracledatasource?
	private  static byte createCompoundFilterForSQLFilterParameter(Element entityMetadata, SimpleFilter[] filters, 
			String paramName,FilterPart filterPart, GenericsServerSession gSession) {
		String paramSQL="";
		if (filters==null || filters.length==0)
			return DataFilter.NO_FILTER;
		int paramId= 0;
		for (SimpleFilter filter:filters) {
			EntityObject fltEO = filter.getFilterData();
			TypeAttribute ta = fltEO.getType().getAttribute(paramName);			
			paramId=ta.getId();
			if (!ta.isPrimitive())
				throw new RuntimeException("Unable to create filters for non primitive types");
			
			boolean searchIgnoreCase = true;
			if (entityMetadata !=null &&
				MetaAttributes.BOOLEAN_FALSE.equalsIgnoreCase(entityMetadata.getAttribute(MetaAttributes.SEARCH_IGNORE_CASE))) {
				
				searchIgnoreCase=false;				
			}
			
			String value = fltEO.getPrimitiveValue(ta.getId());
			if (value != null && value.length() > 0) {
				
				// operator for joining with  previous filter
				if (paramSQL.length() > 0) {
					paramSQL += SimpleSQLFilter.getOperatorString(filter
							.getOperators()[paramId]);
				}
				Wildcards wc = FilterUtils.getWildcardsFor(ta, value);
				
				//  ------------			CSV FIELD		-----------------
				if(entityMetadata!=null && entityMetadata.hasAttribute(MetaAttributes.FILTER_CSV)){
					// prepare separator
					String separator = ",";
					if(entityMetadata.hasAttribute(MetaAttributes.FILTER_CSV_SEPARATOR)) {
						separator = entityMetadata.getAttribute(MetaAttributes.FILTER_CSV_SEPARATOR);
					}
					
					// prepare ignore case
					String prefix = "";
					String suffix = "";
					if(searchIgnoreCase) {
						prefix = "upper(";
						suffix = ")";
						value = value.toUpperCase();
					}
					
					// alone
					paramSQL += "(" + prefix + ta.getName() + suffix + " LIKE ? OR ";
					// beginning
					paramSQL += prefix + ta.getName() + suffix + " LIKE ? || '" + separator + "%' OR ";
					// middle
					paramSQL += prefix + ta.getName() + suffix + " LIKE '%" + separator + "' || ? || '" + separator +"%' OR ";
					// end
					paramSQL += prefix + ta.getName() + suffix + " LIKE '%" + separator + "' || ?)";
					
					filterPart.parameters.add(new SQLFilterParameter(ta, value));
					filterPart.parameters.add(new SQLFilterParameter(ta, value));
					filterPart.parameters.add(new SQLFilterParameter(ta, value));
					filterPart.parameters.add(new SQLFilterParameter(ta, value));
				
				// ------------			STRING		-----------------
				} else if (ta.getPrimitiveType() == Types.STRING) {
					if (searchIgnoreCase) {
						paramSQL += "upper("+ta.getName()+") LIKE ? ";
						value = value.toUpperCase();
					} else {
						paramSQL += ta.getName()+" LIKE ? ";											
					}
					String likeVal = WildcardUtil.replaceWildcards(value, wc, WCARD_ANSI_LIKE_ESC_AT);
					if (WCARD_ANSI_LIKE_ESC_AT.stringContainsEscape(likeVal)) {
						paramSQL += "ESCAPE '@' ";
					}
					value = likeVal;
					filterPart.parameters.add(new SQLFilterParameter(ta, value));
					
				//		------------		WILDCARD DATE		-----------------
				} else if (ta.getPrimitiveType() == Types.DATE && FilterUtils.valueContainsWildcards(value, wc)) {
					// Handle wildcard filters
					if (entityMetadata ==null ||						
						entityMetadata.getAttribute(MetaAttributes.VALUE_FORMAT) == null ||
						entityMetadata.getAttribute(MetaAttributes.VALUE_FORMAT).length()==0) {
						throw new RuntimeException("Unable to find "+MetaAttributes.VALUE_FORMAT+ " meta attribute " +
								"for TypeAttribute "+ta.getName());
					}
					String dateFormater = entityMetadata.getAttribute(MetaAttributes.VALUE_FORMAT);
					
					
					dateFormater = MetaAttributeUtils.getDateTimeFormat(dateFormater, gSession.getLocale(), JavaDateTimeFormatPatterns.COLLECTION_ORACLE);
					String likeVal = WildcardUtil.replaceWildcards(value, wc, WCARD_ANSI_LIKE_ESC_AT);
					paramSQL+="upper(to_char("+ta.getName()+ ",'"+dateFormater+"')) LIKE upper(?) ";
					if (WCARD_ANSI_LIKE_ESC_AT.stringContainsEscape(likeVal)) {
						paramSQL += " ESCAPE '@' ";
					}
					value = likeVal;
					ta = new TypeAttribute(0, ta.getName(), Types.STRING); // change type to string
					filterPart.parameters.add(new SQLFilterParameter(ta, value));					
					
										
				
				// ------------		COMPARISON	FLOAT, INT, DATE		-----------------					
				} else if ((ta.getPrimitiveType() == Types.FLOAT || ta.getPrimitiveType() == Types.INT || ta.getPrimitiveType() == Types.DATE) && containsComparison(value)) {
					String [] ranges;
					String joinOperator="";
					if (value.contains("&")) { 
						ranges = value.split("&");
						joinOperator=" AND ";
					} else if (value.contains("|")) {
						ranges = value.split("\\|");
						joinOperator=" OR ";
					} else  {
						ranges = new String[1];
						ranges[0]=value;
					}
					String sqlStatement = "";
					for (int i=0;i<ranges.length;i++) {
						if (sqlStatement.length()>0)
							sqlStatement+=joinOperator;	
						String valToParse = ranges[i].trim();
						int splitIdx=1;
						if (!Character.isDigit(valToParse.charAt(splitIdx)))
							splitIdx++;
						String matcher = valToParse.substring(0, splitIdx);
						valToParse = valToParse.substring(splitIdx);
						sqlStatement+=ta.getName()+matcher+"?";
						filterPart.parameters.add(new SQLFilterParameter(ta, valToParse));
					}
					paramSQL+="("+sqlStatement+")";
					
				//	------------			INT	BINARY COMPARISON	-----------------
				} else if (ta.getPrimitiveType() == Types.INT && SimpleFilter.isMatchBinary(value)) {
					paramSQL+="BITAND("+ta.getName()+",?)=?";
					String plainValue = SimpleFilter.extractValue(value);
					String mask = SimpleFilter.extractMask(value);
					filterPart.parameters.add(new SQLFilterParameter(ta, mask));
					filterPart.parameters.add(new SQLFilterParameter(ta,
							plainValue));
				
				//	------------			CATCH ALL		-----------------
				} else {
					paramSQL += ta.getName() + "=? ";
					filterPart.parameters
							.add(new SQLFilterParameter(ta, value));
				}
			}
		}

		filterPart.SQLStatement = paramSQL;
		if (paramSQL.length()==0)
			return DataFilter.NO_FILTER;
		return filters[0].getOperators()[paramId];
	}


	private static boolean containsComparison(String value){
		if (value.contains("<") || value.contains(">"))
			return true;
		return false;
	}
	
	public static SimpleSQLFilter createSimpleSQLFilterFor(CompoundEntityFilter cef, GenericsSettings settings, GenericsServerSession gSession) {
		String SQLStatement = "";
		List<SQLFilterParameter> parameterList = new ArrayList<SQLFilterParameter>();
		EntityType et = cef.getEntityType();
		
		if (et==null) // doesn't contain any valid data - dummy filter 
			return new SimpleSQLFilter();
		// TODO: move this to  some utility class
		Element entityMetadata = settings.getEntityMetadataMap().get(et.getName());
		Map<String,Element> entityMetadataMap = new HashMap<String, Element>();
		if (entityMetadata!=null) {
			NodeList ents = entityMetadata.getElementsByTagName(XMLTags.EntityAttribute);
			for (int i=0;i<ents.getLength();i++) {
				Element el = (Element)ents.item(i);
				entityMetadataMap.put(el.getAttribute(MetaAttributes.NAME), el);
			}
		}
		
		for (TypeAttribute ta:et.getAttributes()) {	
			if (!ta.dbCanRead()) // ignore support attributes
				continue;
			FilterPart fPart = new FilterPart();
			byte operator = SQLFilterHelpers.createCompoundFilterForSQLFilterParameter(
					entityMetadataMap.get(ta.getName()), cef.getFilters(), ta.getName(), fPart, gSession);
			if (operator != DataFilter.NO_FILTER) { // there's a filter. Add it.
				if (SQLStatement.length()>0)
					SQLStatement+= SimpleSQLFilter.getOperatorString(operator);
				SQLStatement+=" ( "+fPart.SQLStatement+" ) ";
				parameterList.addAll(fPart.parameters);
			}
		}
		return new SimpleSQLFilter(SQLStatement,parameterList);
	}
	
	
	
	 
	
}
