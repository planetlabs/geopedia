package com.sinergise.geopedia.client.ui.feature;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.geopedia.client.core.RemoteServices;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.util.FeatUtil;
import com.sinergise.geopedia.client.ui.panels.results.FeatureInfoWidget;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.LookupField;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.properties.PropertyUtils;
import com.sinergise.geopedia.core.query.FeaturesQueryResults;
import com.sinergise.geopedia.core.query.Query;
import com.sinergise.geopedia.core.query.filter.FilterFactory;
import com.sinergise.gwt.ui.controls.ClickableLabel;

public class DivFeatureInfoContentPanel extends FeatureInfoContentPanel {

	public static class Creator implements PanelCreator {
		@Override
		public FeatureInfoContentPanel createPanel(FeatureInfoWidget featureInfoWidget) {
			return new DivFeatureInfoContentPanel(featureInfoWidget);
		}
	}

	ClickableLabel layerLink;
	protected FeatureInfoWidget featureInfoWidget;

	public DivFeatureInfoContentPanel(FeatureInfoWidget featureInfoWidget) {
		this.featureInfoWidget = featureInfoWidget;
	}

	protected FlowPanel createRow(String label) {
		FlowPanel fp = new FlowPanel();
		fp.setStyleName("metaData");
		InlineLabel labelPanel = new InlineLabel();
		labelPanel.setStyleName("label");
		labelPanel.setText(label);
		fp.add(labelPanel);
		return fp;
	}

	private FlowPanel createRow(Field fld) {
		if (fld != null && fld.descDisplayableHtml != null && fld.descDisplayableHtml.trim().length() > 0) {
			InlineHTML html = new InlineHTML(fld.descDisplayableHtml.trim());
			return createRow(fld.getName());
		} else {
			return createRow(fld == null ? "" : fld.getName());
		}

	}

	private Widget updateInfoStyle(Widget wgt) {
		wgt.addStyleName("details");
		return wgt;
	}

	private void loadFeature(Feature feature) {
		

		FeatUtil.getFeatureById(feature.tableId, 0, feature.id, false,
				new AsyncCallback<FeaturesQueryResults>() {

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(FeaturesQueryResults result) {
						if (result != null && result.getCollection() != null) {
							if (result.getCollection().size() > 0)
								setFeature(result.getCollection().get(0), result.table);
						}
					}

				});
	}

	public void setFeature(final Feature feature, final Table featureTable) {

		if (feature.fields == null || feature.properties == null) {
			loadFeature(feature);
			return;
		}

		String featureDesc = feature.getTextDesc();
		if (featureDesc == null)
			featureDesc = "";
		if (featureDesc.length() > 40)
			featureDesc = featureDesc.substring(0, 40);
		clear();

		placeFields(feature, featureTable);

	}

	protected void placeFields(final Feature feature, final Table featureTable) {
		addBasicFields(feature);
		loadAndAddManyLinks(feature);
	}

	protected void addBasicFields(final Feature feature) {
		Field[] flds = feature.fields;
		Property<?>[] vals = feature.properties;

		for (int i = 0; i < flds.length; i++) {
			if (PropertyUtils.isNull(vals[i]) || !flds[i].getVisibility().canView())
				continue;
			switch (flds[i].type) {
			case FOREIGN_ID:
			case DECIMAL:
			case BOOLEAN:
			case DATE:
			case DATETIME:
			case INTEGER:
			case PLAINTEXT:
				FlowPanel itemDiv = createRow(flds[i]);
				add(itemDiv);
				itemDiv.add(updateInfoStyle(FeatWidgetFactory.createView(flds[i], vals[i])));
				break;
			case BLOB:
			case WIKITEXT:
			case LONGPLAINTEXT:
			case STYLE:
				FlowPanel div = createRow(flds[i]);
				div.add(FeatWidgetFactory.createView(flds[i], vals[i]));
				add(div);
				break;
			default:
				break;
			}

		}
	}

	protected void loadAndAddManyLinks(final Feature feature) {
		Repo.instance().getTable(feature.tableId, 0L, new AsyncCallback<Table>() {
					@Override
					public void onFailure(Throwable caught) {
						// silently ignore
					}

					@Override
					public void onSuccess(Table result) {
						if (result == null)
							return;
						LookupField[] lookupFields = LookupField.get(result);
						if (lookupFields != null) {
							for (LookupField lookupField : lookupFields) {
								addManyLinks(lookupField, feature);
							}
						}

					}

				});

	}

	private void addManyLinks(final LookupField fld, final Feature feature) {
		
		Repo.instance().getTable(fld.queryTableId, 0L, new AsyncCallback<Table>() {

					@Override
					public void onFailure(Throwable caught) {
						// silently ignore
					}

					@Override
					public void onSuccess(Table result) {
						final Table table = result;
						if (result == null)
							return;

						Field sourceReference = result.getFieldById(fld.sourceReference);
						if (sourceReference == null || sourceReference.type != FieldType.FOREIGN_ID
								|| sourceReference.refdTableId != fld.sourceTableId) {
							return;
						}
						final Field targetReference;
						if (fld.targetReference != null) {
							targetReference = result.getFieldById(fld.targetReference);
						} else {
							targetReference = null;
						}

						
						Query query = new Query();
						LongProperty prop = new LongProperty(feature.id);
						query.tableId = fld.queryTableId;
						query.filter = FilterFactory.filters(
								FilterFactory.createDeletedDescriptor(fld.queryTableId, false),
								FilterFactory.createFieldDescriptor(sourceReference, prop)
								);
						query.options.add(Query.Options.FLDUSER_ALL);

						RemoteServices.getFeatureServiceInstance().executeQuery(query, 
								new AsyncCallback<FeaturesQueryResults>() {

									@Override
									public void onFailure(Throwable caught) {
										// TODO Auto-generated method stub
									}

									@Override
									public void onSuccess(FeaturesQueryResults result) {
										if (result.getCollection().size() == 0)
											return;
										FlowPanel div = createRow(fld.name);
										for (Feature fl : result.getCollection()) {
											if (targetReference != null && targetReference.type == FieldType.FOREIGN_ID
													&& targetReference.refdTableId == fld.targetTableId) {
												div.add(FeatWidgetFactory.createView(targetReference, fl.getValue(targetReference.id)));
											} else {
												div.add(new RepTextLabel(table.id, fl.getId(), fl.repText));
											}
										}
										add(div);
									}
						});

					}

				});
	}

}