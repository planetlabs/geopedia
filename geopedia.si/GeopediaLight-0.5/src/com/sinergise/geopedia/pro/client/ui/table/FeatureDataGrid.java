package com.sinergise.geopedia.pro.client.ui.table;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Style;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.client.core.search.FeatureByIdSearcher;
import com.sinergise.geopedia.client.ui.feature.ImageURL;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.properties.BinaryFileProperty;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;
import com.sinergise.geopedia.core.entities.properties.HTMLProperty;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.Query.Options;
import com.sinergise.geopedia.core.query.Query.OrderBy;
import com.sinergise.geopedia.core.query.filter.FieldDescriptor;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.ui.ClosableBottomPanel;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;

public class FeatureDataGrid extends ClosableBottomPanel {

	private DataGrid<Feature> grid;
	private Table table;
	private FeatureDataProvider provider;
	private Query query;

	protected interface AbstractFilterElement  {
		public FilterDescriptor getFilterDescriptor();
	}
	
	private static abstract class FDGColumn<C> extends Column<Feature, C> {
		private Field field = null;

		public FDGColumn(Cell<C> cell, Field field) {
			super(cell);
			this.field = field;
		}

		public FDGColumn(Cell<C> cell, TableMetaFieldDescriptor.MetaFieldType property) {
			super(cell);
		}

		public ElementDescriptor getElementDescriptor() {
			if (field != null) {
				return FieldDescriptor.newInstance(field);
			}
			return null;
		}

	}

	private class ResizableGridHolder extends FlowPanel implements RequiresResize {
		Timer redrawTimer = null;

		@Override
		public void onResize() {
			if (redrawTimer == null) {
				if (grid != null) {
					redrawTimer = new Timer() {

						@Override
						public void run() {
							if (grid != null) {
								grid.redraw();
							}
							redrawTimer = null;
						}
					};
				}

			}
			if (redrawTimer != null) {
				redrawTimer.schedule(50);
			}
		}

	}

	private static void addColumn(final Field field, final int fieldIndex, DataGrid<Feature> grid) {
		if (field.type == FieldType.DATE) {
			DateCell dateCell = new DateCell(ClientGlobals.FORMAT_DATE);
			FDGColumn<Date> dateColumn = new FDGColumn<Date>(dateCell, field) {
				@Override
				public Date getValue(Feature feature) {
					Property<?> vhDate = feature.getUserFieldValue(fieldIndex);
					if (vhDate == null)
						return null;
					return ((DateProperty)vhDate).getValue();
				}
			};
			grid.setColumnWidth(dateColumn, "100px");
			grid.addColumn(dateColumn, field.getName());
			return;
		} else if (field.type == FieldType.DATETIME) {
			DateCell dateCell = new DateCell(ClientGlobals.FORMAT_DATETIME);
			FDGColumn<Date> dateColumn = new FDGColumn<Date>(dateCell, field) {
				@Override
				public Date getValue(Feature feature) {
					Property<?> vhDate = feature.getUserFieldValue(fieldIndex);
					if (vhDate == null)
						return null;
					return ((DateProperty)vhDate).getValue();
				}
			};
			grid.setColumnWidth(dateColumn, "100px");
			grid.addColumn(dateColumn, field.getName());
			return;
		} else if (field.type == FieldType.WIKITEXT) {
			SafeHtmlCell cell = new SafeHtmlCell();
			FDGColumn<SafeHtml> column = new FDGColumn<SafeHtml>(cell, field) {

				@Override
				public SafeHtml getValue(Feature feature) {
					Property<?> vh = feature.getUserFieldValue(fieldIndex);
					if (vh == null)
						return null;
					HTMLProperty htmlProperty = (HTMLProperty)vh;
					SafeHtmlBuilder shb = new SafeHtmlBuilder();
					if (htmlProperty.getValue() == null)
						return null;
					shb.appendHtmlConstant(htmlProperty.getValue());
					return shb.toSafeHtml();
				}
			};
			grid.addColumn(column, field.getName());
			grid.setColumnWidth(column, "200px");
			return;

		} else if (field.type == FieldType.BLOB) {
			
			ImageCell imlCell = new ImageCell(); //better for displaying image since imageLoadingCell has some overflow and it displays scrollbars
			FDGColumn<String> column = new FDGColumn<String>(imlCell, field) {
				@Override
				public String getValue(Feature feature) {
					Property<?> vh = feature.getUserFieldValue(fieldIndex);
					if (vh == null)
						return null;
					BinaryFileProperty vhBinary = (BinaryFileProperty) vh;
					if (vhBinary.getValue() == null)
						return null;
					return ImageURL.createUrl(vhBinary.getValue(), 100, 100);
				}
			};
			grid.addColumn(column, field.getName());
			grid.setColumnWidth(column, "110px");
			return;
		} else if (field.type == FieldType.FOREIGN_ID) {
			ClickableTextCell cell = new ClickableTextCell();
			FDGColumn<String> column = new FDGColumn<String>(cell, field) {

				@Override
				public String getValue(Feature feature) {
					ForeignReferenceProperty prop = (ForeignReferenceProperty) feature
							.getUserFieldValue(fieldIndex);
					String value = null;
					if (prop != null) {
						
						value = prop.getReptext();
						if (value == null) {
							value = String.valueOf(prop.getValue());
						}
					}
					return value;
				}
			};
			column.setFieldUpdater(new FieldUpdater<Feature, String>() {
				
				@Override
				public void update(int index, Feature object, String value) {
					ForeignReferenceProperty prop = (ForeignReferenceProperty) object.getUserFieldValue(fieldIndex);
					if (ClientGlobals.defaultSearchExecutor != null) {
						//TODO I dont know if ReferencedTable and getValue can be null if they can then there should be a check before
						FeatureByIdSearcher fis = new FeatureByIdSearcher(field.getReferencedTable().getId(), (int) prop.getValue().longValue());
						ClientGlobals.defaultSearchExecutor.executeSearch(fis);
					}
				}
			});
			grid.addColumn(column, field.getName());
			grid.setColumnWidth(column, "120px");
			column.setSortable(true);
			column.setCellStyleNames("link-like");
			grid.addColumnStyleName(grid.getColumnIndex(column), "foreignField");
			return;
		} else if (field.type == FieldType.INTEGER || field.type == FieldType.DECIMAL) {
			NumberCell numberCell = new NumberCell();
			FDGColumn<Number> column = new FDGColumn<Number>(numberCell, field) {
				@Override
				public Number getValue(Feature feature) {
					Property<?> vh = feature.getUserFieldValue(fieldIndex);
					if (vh == null)
						return null;
					if (field.type == FieldType.INTEGER)
						return ((LongProperty) vh).getValue();
					else if (field.type == FieldType.DECIMAL)
						return ((DoubleProperty) vh).getValue();
					return null; // TODO throw exception?
				}

			};
			grid.addColumn(column, field.getName());
			grid.setColumnWidth(column, "100px");
			column.setSortable(true);
			return;
		} else if (field.type == FieldType.BOOLEAN) {
			TextCell cell = new TextCell();
			FDGColumn<String> column = new FDGColumn<String>(cell, field) {

				@Override
				public String getValue(Feature feature) {
					Property<?> vh = feature.getUserFieldValue(fieldIndex);
					if (vh != null)
						return ((BooleanProperty) vh).getValue() ? StandardUIConstants.STANDARD_CONSTANTS
								.buttonYes() : StandardUIConstants.STANDARD_CONSTANTS
								.buttonNo();
					return null;
				}
			};
			grid.addColumn(column, field.getName());
			grid.setColumnWidth(column, "120px");
			column.setSortable(true);
			return;
		}  
		TextCell cell = new TextCell();
		FDGColumn<String> column = new FDGColumn<String>(cell, field) {

			@Override
			public String getValue(Feature feature) {
				Property<?> vh = feature.getUserFieldValue(fieldIndex);
				if (vh != null)
					return vh.toString();
				return null;
			}
		};
		grid.addColumn(column, field.getName());
		grid.setColumnWidth(column, "120px");
		column.setSortable(true);
		return;
	}

	public FeatureDataGrid(Table table1) {
		super();
		this.table = table1;
		query = new Query();
		query.tableId = table.getId();
		query.options.add(Query.Options.FLDMETA_ENVLENCEN);
		query.options.add(Query.Options.FLDMETA_BASE);
		query.options.add(Query.Options.FLDUSER_ALL);
		query.options.add(Query.Options.FLDUSER_ALL);
		
		setTabTitle(ProConstants.INSTANCE.layerData() +"<b>"+ table.getName()+"</b>");
		grid = new DataGrid<Feature>(50, (DataGrid.Resources) GWT.create(GridResources.class));

		GPDActionCell<Feature> btnShow = new GPDActionCell<Feature>("",ProConstants.INSTANCE.zoomToFeature(), new GPDActionCell.Delegate<Feature>() {
			@Override
			public void execute(Feature object) {
				// TODO: when styles are fixed fix this!!!
				FeatureByIdSearcher searcher = new FeatureByIdSearcher(object.tableId, object.getId());
				searcher.showFeature(true, true, ClientGlobals.mainMapWidget.getMapComponent());
				ClientGlobals.defaultSearchExecutor.executeSearch(searcher);
			}
		});
		
		/** --- advanced filters --- **/
		final TableFilterBuilderPanel advFilterPanel = new TableFilterBuilderPanel(table) {
			@Override
			protected void onFilterChanged() {
				query.filter = getFilterDescriptor();		
				grid.setVisibleRangeAndClearData(grid.getVisibleRange(), true);
			}
		};
		advFilterPanel.addStyleName("filterPopupPanel");
		advFilterPanel.setSize("480px", "150px");
		
		query.filter = advFilterPanel.getFilterDescriptor();
		
		final SGPushButton btnOpenFilterDialog = new SGPushButton(GeopediaTerms.INSTANCE.filter(),GeopediaStandardIcons.INSTANCE.filter());
		btnOpenFilterDialog.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
//				advFilterPanel.showRelativeTo(btnOpenAdvanced);
				advFilterPanel.center();
			}
		});
		btnOpenFilterDialog.addStyleName("withToogleButton");
		addButton(btnOpenFilterDialog);
		
		ToggleButton toggleFilter = new ToggleButton(new Image(GeopediaStandardIcons.INSTANCE.checkOff()), new Image(GeopediaStandardIcons.INSTANCE.checkOn()), new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
			}
		});
		toggleFilter.setStyleName("greyToggleButton");
//		addButton(toggleFilter);  //When implemented add it back!
		

		final SGPushButton btnRefresh = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonRefresh(),GeopediaStandardIcons.INSTANCE.refresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				grid.setVisibleRangeAndClearData(grid.getVisibleRange(), true);
			}
		});
		addButton(btnRefresh);
		
		IdentityColumn<Feature> showColumn = new IdentityColumn<Feature>(btnShow);
		showColumn.setCellStyleNames(GridResources.INSTANCE.dataGridStyle().cellShow());
		grid.addColumn(showColumn, "");
		grid.setColumnWidth(showColumn, "40px");

		for (int i = 0; i < table.fields.length; i++) {
			final Field field = table.fields[i];
			if (!field.getVisibility().canView())
				continue;
			addColumn(field, i, grid);
		}

		grid.setStyleName("featuresGrid");

		ResizableGridHolder gridHolder = new ResizableGridHolder();
		gridHolder.add(grid);
		addContent(gridHolder);
		setHeight("100%");
		SimplePager pager = new SimplePager(TextLocation.CENTER,
				(FeatureDataPagerResources) GWT.create(FeatureDataPagerResources.class), false, 1000, true);
		pager.setDisplay(grid);

		grid.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(grid));
		provider = new FeatureDataProvider();
		provider.addDataDisplay(grid);
		grid.onResize();
		SGFlowPanel pagerWrap = new SGFlowPanel("gridPager");
		pagerWrap.add(pager);
		setFooter(pagerWrap);

	}

	public interface FeatureDataPagerResources extends SimplePager.Resources {
		@Override
		@Source("firstPage.png")
		public ImageResource simplePagerFirstPage();

		@Override
		@Source("firstPageDisabled.png")
		public ImageResource simplePagerFirstPageDisabled();

		@Override
		@Source("lastPage.png")
		public ImageResource simplePagerLastPage();

		@Override
		@Source("lastPageDisabled.png")
		public ImageResource simplePagerLastPageDisabled();

		@Override
		@Source("nextPage.png")
		public ImageResource simplePagerNextPage();

		@Override
		@Source("nextPageDisabled.png")
		public ImageResource simplePagerNextPageDisabled();

		@Override
		@Source("prevPage.png")
		public ImageResource simplePagerPreviousPage();

		@Override
		@Source("prevPageDisabled.png")
		public ImageResource simplePagerPreviousPageDisabled();
	}

	public interface GridResources extends DataGrid.Resources {
		public static final GridResources INSTANCE = GWT.create(GridResources.class);

		@Override
		@Source(value = {/* DataGrid.Style.DEFAULT_CSS, */"dataGridStyle.css" })
		GridCss dataGridStyle();

		ImageResource zoomto();

		public interface GridCss extends Style {
			String cellShow();
		}

		@Override
		@Source("loading.gif")
		public ImageResource dataGridLoading();
		// @Override
		// @Source("sortUp.png")
		// public ImageResource dataGridSortAscending();
		// @Override
		// @Source("sortDown.png")
		// public ImageResource dataGridSortDescending();
	}

	private class FeatureDataProvider extends AsyncDataProvider<Feature> {

		@Override
		protected void onRangeChanged(final HasData<Feature> display) {
			refresh(display);
		}

		public void refresh(final HasData<Feature> display) {
			final Range range = display.getVisibleRange();
			final int start = range.getStart();
			int length = range.getLength();
			query.fetchEverything(false);
			query.options.add(Options.TOTALCOUNT);
			
			query.startIdx = start;
			query.stopIdx = start + length;

			// order
			ColumnSortList list = grid.getColumnSortList();
			query.resetOrderBy();
			for (int i = 0; i < list.size(); i++) {
				ColumnSortInfo csi = list.get(i);
				if (csi.getColumn() instanceof FDGColumn<?>) {
					FDGColumn<?> column = (FDGColumn<?>) csi.getColumn();
					Query.OrderBy by = OrderBy.DESC;
					if (csi.isAscending())
						by = OrderBy.ASC;
					query.addOrderBy(column.getElementDescriptor(), by);
				}
			}

			RemoteServices.getFeatureServiceInstance().executeQuery(query,
					new AsyncCallback<FeaturesQueryResults>() {

						@Override
						public void onSuccess(FeaturesQueryResults result) {
							ArrayList<Feature> features = result.getCollection();
							display.setRowData(start, features);
							if (result.hasTotalCount()) {
								display.setRowCount((int) result.totalCount);
							} else {
								int count = result.getDataLocationEnd();
								//FIXME weird if!
								if (result.hasMoreData())
									;
								count++;
								display.setRowCount(count, !result.hasMoreData());
							}

						}

						@Override
						public void onFailure(Throwable caught) {
							clearContent();
							removeButtons(false);
							InlineLabel exLabel = new InlineLabel(ExceptionI18N.getLocalizedMessage(caught));
							exLabel.setStyleName("error");
							addContent(exLabel);
						}
					});

		}

	}

}
