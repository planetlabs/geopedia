/**
 * 
 */
package com.sinergise.gwt.gis.map.messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * @author amarolt
 *
 */
public interface AppMessages extends Constants {

   /** Provides an initialized static instance of the class. */
  public static final AppMessages INSTANCE = (AppMessages) GWT.create(AppMessages.class);

  String welcomeMessage();

  String CoordinateDialog_title();
  String CoordinateDialog_source_label();
  String CoordinateDialog_source_crs_hint();
  String CoordinateDialog_source_coord_hint();
  String CoordinateDialog_target_label();
  String CoordinateDialog_target_crs_hint();
  String CoordinateDialog_target_coord_hint();
  String CoordinateDialog_zoom_label();
  String CoordinateDialog_zoom_box_hint();
  String CoordinateDialog_zoom_field_hint();
  String CoordinateDialog_moveButton();
  String CoordinateDialog_moveButton_hint();
  String CoordinateDialog_button_OK_html();
  String CoordinateDialog_button_OK_hint();
  String CoordinateDialog_button_CANCEL_html();

  String MeasureResultsPanel_Header();
  String MeasureResultsPanel_Length();
  String MeasureResultsPanel_Area();
  String MeasureResultsPanel_Sections();

  String SpireMapControls_ACTION_REDRAW();
  String SpireMapControls_ACTION_ZOOM_ALL();
  String SpireMapControls_ACTION_ZOOM_IN();
  String SpireMapControls_ACTION_ZOOM_OUT();
  String SpireMapControls_ACTION_ZOOM_PREVIOUS();
  String SpireMapControls_ACTION_ZOOM_NEXT();
  String SpireMapControls_COMPONENT_ZOOM_COMBO();
  String SpireMapControls_ACTION_MEASURE_MODE();
  String SpireMapControls_ACTION_FEATURE_INFO_MODE();

  String MapActions_ZoomToMBR_Name();
  String MapActions_MoveToPoint_Name();
  String MapActions_SelectFeature_Name();


  String SpireRightPanel_LayersTab();
  String SpireRightPanel_ControlsTab();
  String SpireRightPanel_FeatureInfoTab();
  String SpireRightPanel_UploadTab();

  String SpireMap_Rasters();
  String SpireMap_Overview();
  String SpireMap_Topography();

  String Field();
  String Value();
  String Search();

  String SearchControls_title();

  String ChooseSearchType();

  String PrintAction_Title();
  String PrintAction_Icon();
  String PrintAction_Large_Icon();
  String PrintAction_Close();

  String ControlsTab_SpireMeasurementPanel_Icon();
  String ControlsTab_SpireFeatureInfoPanel_Icon();
  String ControlsTab_SpireMultipleFeatureInfoPanel_Icon();
  String ControlsTab_overviewButton_title();

  String SpireMeasurementPanel_Name();
  String SpireMeasurementPanel_Description();
  String SpireMeasurementPanel_Icon();
  String SpireMeasurementPanel_Large_Icon();
  String SpireMeasurementPanel_titleLabel();
  String SpireMeasurementPanel_Distance();
  String SpireMeasurementPanel_Surface();
  String SpireMeasurementPanel_Segments();

  String UploadAction_Title();
  String UploadAction_Close();

  String Place();
  String Postcode();
  String Coordinate();
  String County();
  String Region();

  String CSVService_SERVICE_URI();

  String RPCUtils_TEST_URL();

  String header();
  String content();
  String footer();

  String SearchControls_qCombined_Style();
  String SearchControls_Style();

  String controls();
  String SpireApp_mapWidget_Style();
  String SpireApp_mapControls_Style();

  String SpireFooter_Style();
  String SpireMap_WMSLayersSource();

  String Legend_Layer_title();

  String LayerLinkDialog_editButton();
  String LayerLinkDialog_removeButton();
  String LayerLinkDialog_addButton();
  String LayerLinkDialog_Title();
  String LayerLinkDialog_LinkEditor_titleLabel();
  String LayerLinkDialog_LinkEditor_urlLabel();
  String LayerLinkDialog_LinkEditor_header();
  String LayerLinkDialog_LinkEditor_saveButton();
  String LayerLinkDialog_LinkEditor_saveButton_Title();
  String LayerLinkDialog_LinkEditor_cancelButton();
  String LayerLinkDialog_LinkEditor_cancelButton_Title();
  String LayerLinkDialog_LinkAdder_titleLabel();
  String LayerLinkDialog_LinkAdder_urlLabel();
  String LayerLinkDialog_LinkAdder_header();
  String LayerLinkDialog_LinkAdder_saveButton();
  String LayerLinkDialog_LinkAdder_saveButton_Title();
  String LayerLinkDialog_LinkAdder_cancelButton();
  String LayerLinkDialog_LinkAdder_urlBox();

  String OverviewCenterClickAction();

  String OverviewDragAction_Title();
  String OverviewDragAction_Cursor();
  String OverviewDragAction_DragCursor();

  String OverviewMap_SloTiled();
  String OverviewMap_rasters();
  String OverviewMap_NAME_ST_DOF();
  String OverviewMap_NAME_ST_DMV_COLOR();
  String OverviewMap_WMSLayersSource();
  String OverviewMap_APP();
  String OverviewMap_LAYER();

  String OverviewPanAction_Title();
  String OverviewPanAction_Cursor();
  String OverviewPanAction_DragCursor();

  String OverviewZoomBoxAction_Title();
  String OverviewZoomBoxAction_Cursor();
  String OverviewZoomBoxAction_DragCursor();

  String LayersTab_lblActiveLayer();
  String LayersTab_lblActiveLayer_Style();
  String LayersTab_layers();
  String LayersTab_leyers_Style();
  String LayersTab_activeLayer();
  String LayersTab_structuredSearchButton();
  String search();
  String LayersTab_searchButton();
  String LayersTab_searchControls_Style();
  String LayersTab_mapLayersTree();
  String LayersTab_legendPanel();
  String LayersTab_LayerLinks_clickableButton();

  String ResultsTab_Title();

  String UploadTab_titleLabel();
  String UploadTab_titleLabel_Style();
  String UploadTab_uploadForm_Style();
  String UploadTab_uploadForm_Action();

  String FeatureInfoControl_HTMLResultStyle();
  String FeatureInfoControl_InfoRequest();
  String FeatureInfoControl_titleLabel();
  String FeatureInfoControl_titleLabel_style();
  String FeatureInfoControl_onLayers();
  String FeatureInfoControl_distanceString();
  String FeatureInfoControl_distanceFormatPx();
  String FeatureInfoControl_distanceFormatMeters();
  String FeatureInfoControl_showInfo_noLayerToQuery();
  String FeatureInfoControl_callback_onFailure();
  String FeatureInfoControl_callback_emptyResult();
  String FeatureInfoControl_callback_sentToResults();
  String FeatureInfoControl_callback_sentToInfo();
  String FeatureInfoControl_allLayers();

  String FeatureInfoPanel_titleLabel();

  String SpireFeatureInfoPanel_InfoRequest();
  String SpireFeatureInfoPanel_titleLabel();
  String SpireFeatureInfoPanel_titleLabel_Style();
  String SpireFeatureInfoPanel_showInfo_null_activeLayer();
  String SpireFeatureInfoPanel_createHTMLCallback_onFailure();
  String SpireFeatureInfoPanel_createResultCallback_no_results();
  String SpireFeatureInfoPanel_createResultCallback_onFailure();

  String SpireFeaturesSummaryPanel_showInfo();

  String Move();

  String SpireMeasurementPanel_titleLabel_Style();
  String SpireMeasurementPanel_info_Style();
  String SpireMeasurementPanel_segments_Style();
  String SpireMeasurementPanel_updateSegment_index_Style();
  String SpireMeasurementPanel_updateSegment_value_Style();

  String SpireMultipleFeatureInfoPanel_titleLabel();
  String SpireMultipleFeatureInfoPanel_showInfo_null_activeLayer();

  String UploadEditWidget_lblEdit();
  String UploadEditWidget_lblLayerName();
  String UploadEditWidget_lblLayerName_Style();
  String UploadEditWidget_lblFieldNames();
  String UploadEditWidget_lblFieldNames_Style();
  String UploadEditWidget_fpFields_Style();
  String UploadEditWidget_lblCoordinatesFields();
  String UploadEditWidget_lblCoordinatesFields_Style();
  String UploadEditWidget_hpX();
  String UploadEditWidget_hpX_Item();
  String UploadEditWidget_hpY();
  String UploadEditWidget_hpY_Item();
  String UploadEditWidget_lblMapping();
  String UploadEditWidget_lblMapping_Style();
  String UploadEditWidget_Key();
  String UploadEditWidget_table();
  String UploadEditWidget_setFieldNames_Style();
  String UploadEditWidget_fieldName_Style();
  String UploadEditWidget_chkInclude_Style();

  String UploadSaveWidget_lblSubmit();
  String UploadSaveWidget_rdSession();
  String UploadSaveWidget_rdPermanent();
  String UploadSaveWidget_butSave();

  String UploadSelectWidget_lblFileName();
  String UploadSelectWidget_fileUploadWgt();
  String UploadSelectWidget_butUpload();

  String SpireLogin_lblUsr();
  String SpireLogin_lblPwd();
  String SpireLogin_butLogin();
  String SpireLogin_termsURL();
  String SpireLogin_agreeTo();
  String SpireLogin_termsOfUse();
  String SpireLogin_termsOfUse_Style();

  String AdvSelectionPanel_titleLabel();
  String AdvSelectionPanel_Icon();
  String AdvSelectionPanel_opearation();
  String AdvSelectionPanel_onLayers();
  String AdvSelectionPanel_buttonSelect();
  String AdvSelectionPanel_buttonCancel();

  String FilterCapabilities_SPATIAL_OP_INTERSECT();
  String FilterCapabilities_SPATIAL_OP_WITHIN();
  String FilterCapabilities_SPATIAL_OP_CONTAINS();
  String FilterCapabilities_SPATIAL_OP_OVERLAPS();
}

