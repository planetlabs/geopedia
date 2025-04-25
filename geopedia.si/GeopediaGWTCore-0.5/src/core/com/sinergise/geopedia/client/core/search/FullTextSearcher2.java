package com.sinergise.geopedia.client.core.search;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.search.SearchListener.SystemNotificationType;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor.MetaFieldType;

public class FullTextSearcher2 extends BatchSearchExecutor{

	
	
	private static final int TABLE_ID_HS = 450;
	private static final int TABLE_ID_NASELJA = 410;
	private static final int TABLE_ID_ULICE = 411;
	private static final int TABLE_ID_REZI = 1173;
	private ArrayList<String> queryParts;
	protected String query;
	protected int batchMaxSearchResults = ClientGlobals.maxSearchResults;
	
	public FullTextSearcher2(String query2) {	
		query2 = query2.trim().toLowerCase();		
		RegExp regExp = RegExp.compile("[^+\\s]+","g");			
		queryParts = new ArrayList<String>();
		HashSet<String> plainParts = new HashSet<String>();
		for (MatchResult matcher = regExp.exec(query2); matcher != null; matcher = regExp.exec(query2)) {
			String s = matcher.getGroup(0);
			if (s.length() < 1)
				continue;
			if (s.matches("^[0-9]+[a-z]$")) { // handle 13a => "+13" "+a"
				String num = s.substring(0, s.length() - 1);
				char add = s.charAt(s.length() - 1);
				plainParts.add(num.trim());
				plainParts.add(String.valueOf(num));
				queryParts.add("+" + num);
				queryParts.add("+" + add);
			} else if (s.startsWith("-")) {
				int fifi = 1;
				while (fifi < s.length() && s.charAt(fifi) == '-')
					fifi++;
				if (fifi < s.length()) {
					if (fifi == 1) {
						queryParts.add(s);
					} else {
						queryParts.add("-" + s.substring(fifi));
					}
				}
			} else {
				plainParts.add(s.trim());
				queryParts.add("+" + s);
			}
		}
		
		
		StringBuilder sb = new StringBuilder();
		for (String s : queryParts) {
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(s);

//			int sl = s.length();
//			for (int a = 0; a < sl; a++) {
//				char c = s.charAt(a);
//				if (c >= '0' && c <= '9')
//					skipNaselja = true;
//			}
		}
		query = sb.toString();
		
	}
	
	public BatchSearcher getThemesSearcher() {
		return new BatchSearcher() {

			@Override
			public void execute(final SearchListener listener,
					final BatchSearchExecutor executor) {
				RemoteServices.getMetaServiceInstance()
				.findThemesFulltext(query, new AsyncCallback<Theme[]>() {
					@Override
					public void onSuccess(Theme[] result) {
						listener.themesSearchResults(result, false, null);
						executor.searchDone();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						listener.themesSearchResults(null, true, caught.getMessage());
						executor.searchDone();
					}
				});
			}
		};
	}

	public BatchSearcher getLayersSearcher() {
		return new BatchSearcher() {

			@Override
			public void execute(final SearchListener listener,
					final BatchSearchExecutor executor) {
				RemoteServices.getMetaServiceInstance()
					.findTablesFulltext(query, new AsyncCallback<Table[]>() {
					@Override
					public void onSuccess(Table[] result) {
						listener.tablesSearchResults(result, false, null);
						executor.searchDone();
					}
					
					@Override
					public void onFailure(Throwable caught) {
						listener.themesSearchResults(null, true, caught.getMessage());
						executor.searchDone();
					}
				});
			}
		};
	}
	
	
	protected static void executeFullTextSearch 	(String queryString, int tableId, int maxResults, 
			int scale,  final SearchListener listener, final BatchSearchExecutor executor) {
		Query query = new Query();
		query.startIdx=0;
		query.stopIdx=maxResults;
		query.options.add(Query.Options.FLDMETA_ENVLENCEN);
		query.options.add(Query.Options.FLDMETA_BASE);
		query.options.add(Query.Options.FLDUSER_ALL);			
		query.tableId = tableId;
		query.scale = scale;
		query.dataTimestamp = 0;
		
		
		ComparisonOperation co = new ComparisonOperation(
				new TableMetaFieldDescriptor(MetaFieldType.FULLTEXT, tableId),
				FilterCapabilities.SCALAR_OP_COMP_CONTAINS,
				Literal.newInstance(new TextProperty(queryString)));
		
		
		query.filter = new LogicalOperation(new ExpressionDescriptor[]{co, FilterFactory.createDeletedDescriptor(tableId, false)}
				, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
		listener.systemNotification(SystemNotificationType.TABLE_SEARCH_START, null);
		RemoteServices.getFeatureServiceInstance().executeQuery(query, new AsyncCallback<FeaturesQueryResults>() {

			@Override
			public void onFailure(Throwable caught) {
				listener.systemNotification(SystemNotificationType.ERROR, caught.getMessage());
				executor.searchDone();
			}

			@Override
			public void onSuccess(FeaturesQueryResults result) {
				Feature[] features = result.getCollection().toArray(new Feature[result.getCollection().size()]);
				if (features.length>0) {
					listener.searchResults(features, result.table, result.hasMoreData(), false, null);
				}
				executor.searchDone();
			}
		});
	}
	
	
	public void addDefaultSearchers() {
		final int scale = -1; // autoscale
		boolean hasNumber = false;
		RegExp regExp = RegExp.compile("[\\d]+","g");				
		if (regExp.exec(query)!=null) {
			hasNumber= true;
		}
			
		if (!hasNumber) {
			addSearcher(new BatchSearcher() { // rezi
					@Override
					public void execute(SearchListener listener, BatchSearchExecutor executor) {
						executeFullTextSearch(query, TABLE_ID_REZI, batchMaxSearchResults, scale, listener, executor);
					}
				});
			addSearcher(new BatchSearcher() { // ulice
					@Override
					public void execute(SearchListener listener, BatchSearchExecutor executor) {
						executeFullTextSearch(query, TABLE_ID_ULICE, batchMaxSearchResults, scale, listener, executor);
					}
				});
			addSearcher(new BatchSearcher() { // naselja
					@Override
					public void execute(SearchListener listener, BatchSearchExecutor executor) {
						executeFullTextSearch(query, TABLE_ID_NASELJA, batchMaxSearchResults, scale, listener, executor);
					}
				});
		} else {
			addSearcher(new BatchSearcher() { // hišne številke
					@Override
					public void execute(SearchListener listener, BatchSearchExecutor executor) {
						executeFullTextSearch(query, TABLE_ID_HS, batchMaxSearchResults, scale, listener, executor);
					}
				});
		}
	}
	
	
	public BatchSearcher[] getTableContentSearchers(ArrayList<Table> tablesToSearch) {
		ArrayList<BatchSearcher> srch = new ArrayList<BatchSearcher>();
		for(Table t : tablesToSearch){
			if(t.isQueryable()){
				srch.add(getTableContentSearcher(t.getId()));
			}
		}
		
		return srch.toArray(new BatchSearcher[srch.size()]);
	}
	
	public BatchSearcher getTableContentSearcher(final int tableId) {
		final int scale = -1;
		return new BatchSearcher() {
			@Override
			public void execute(SearchListener listener, BatchSearchExecutor executor) {
				executeFullTextSearch(query, tableId, batchMaxSearchResults, scale, listener, executor);
			}
		};
	}
	
}
