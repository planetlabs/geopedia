package com.sinergise.geopedia.pro.client.ui.table;

import java.util.HashSet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldFlags;
import com.sinergise.geopedia.core.entities.Field.FieldType;
import com.sinergise.geopedia.core.entities.Field.FieldVisibility;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.i18n.ProMessages;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.client.ui.EntitySelectorDialog;
import com.sinergise.geopedia.pro.client.ui.EntitySelectorDialog.TableSelectorDialog;
import com.sinergise.geopedia.pro.client.ui.widgets.EnumHashSetEditor;
import com.sinergise.geopedia.pro.client.ui.widgets.EnumListBox;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.ListBoxExt;
import com.sinergise.gwt.ui.maingui.DecoratedAnchor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextArea;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup.PositionType;
import com.sinergise.gwt.ui.tooltip.SGRichTooltipPopup.TooltipPosition;

public class TableFieldsEditorPanel extends AbstractEntityEditorPanel<Table> {
	private static final int DIRECTION_UP = -1;
	private static final int DIRECTION_DOWN = 1;
	
	private static String getFieldTypeI18NName(FieldType ft) {
		switch (ft) {
		case INTEGER:
			return GeopediaTerms.INSTANCE.number();
		case DECIMAL:
			return GeopediaTerms.INSTANCE.decimalNumber();
		case BLOB:
			return GeopediaTerms.INSTANCE.photo();
		case BOOLEAN:
			return GeopediaTerms.INSTANCE.yesNo();
		case DATE:
			return GeopediaTerms.INSTANCE.date();
		case DATETIME:
			return GeopediaTerms.INSTANCE.dateTime();
		case FOREIGN_ID:
			return GeopediaTerms.INSTANCE.foreignID();
		case LONGPLAINTEXT:
			return GeopediaTerms.INSTANCE.longText();
		case PLAINTEXT:
			return GeopediaTerms.INSTANCE.shortText();
		case WIKITEXT:
			return GeopediaTerms.INSTANCE.wikiText();
		case STYLE:
			return GeopediaTerms.INSTANCE.style();
		}
		return ft.name();
	}
	
	private static String getFieldVisibilityI18N(Enum<FieldVisibility> enm) {
		if (enm == FieldVisibility.ALL_HIDDEN) {
			return GeopediaTerms.INSTANCE.allwaysHidden();
		} else if (enm == FieldVisibility.ALL_VISIBLE) {
			return GeopediaTerms.INSTANCE.allwaysVisible();
		} else if (enm == FieldVisibility.VIEW_HIDDEN) {
			return GeopediaTerms.INSTANCE.viewHidden();
		} else if (enm == FieldVisibility.EDIT_HIDDEN) {
			return GeopediaTerms.INSTANCE.editHidden();
		} else {
			return enm.name();
		}
	}
	
	private static String getFieldFlagI18N(Enum<FieldFlags> enm) {
		if (enm == FieldFlags.DELETED) {
			return enm.name();
		} else if (enm == FieldFlags.FULLTEXTEXCLUDE) {
			return GeopediaTerms.INSTANCE.excludeFullText();
		} else if (enm == FieldFlags.MANDATORY) {
			return GeopediaTerms.INSTANCE.mandatory();
		} else if (enm == FieldFlags.READONLY) {			
			return GeopediaTerms.INSTANCE.readOnly();
		}
		return enm.name();
	}
	
	
	private static class ReferencedTableSelector extends FlowPanel {
		private InlineHTML lblTableName;
		private SGPushButton btnSelect;
		private Table table;
		public ReferencedTableSelector() {
			lblTableName = new InlineHTML();			
			btnSelect = new SGPushButton(Buttons.INSTANCE.select());
			btnSelect.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					TableSelectorDialog tsd = new TableSelectorDialog(
							EntitySelectorDialog.createSelectButton()) {
						@Override
						protected boolean onEntitySelected(Table table) {							
							Repo.instance().getTable(table.id, table.lastMetaChange, new AsyncCallback<Table>() {
								
								@Override
								public void onSuccess(Table result) {
									setValue(result);		
								}
								
								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
									
								}
							});							
							return true;
						}
					};
					tsd.show();
				}
			});
			add(lblTableName);
			add(btnSelect);
		}
		
		public void setValue(Table table){
			this.table=table;
			if (table==null) {
				lblTableName.setHTML("<em>"+ ProConstants.INSTANCE.notSet() +"</em>");
				lblTableName.setTitle(null);
			} else {
				lblTableName.setHTML("<b>"+table.getName()+"</b>");
				setTitle(String.valueOf(table.getId()));
			}
		}
		public Table getValue() {
			return table;
		}

		public void setEditable(boolean enabled) {
			btnSelect.setVisible(enabled);
		}
	}
	
	private class FieldEditor extends FlowPanel {
		private SGTextBox tbName;
		private SGTextArea taDescription;
		private ListBoxExt lbType;		
		private Anchor toggleAnchor;
		private EnumHashSetEditor<FieldFlags> flagsEditor;
		private EnumListBox<FieldVisibility> visibilityEditor;
		private Field fld;
		private ReferencedTableSelector refTableSelector;
		private FlowPanel titleHolder;
		private SGFlowPanel editorHolder;
		private FlowPanel holderReferencedTable;
		private InlineLabel tblIdLbl;
		
		public FieldEditor() {
			setStyleName("fieldEditor");
			titleHolder = new FlowPanel();
			titleHolder.setStyleName("fieldHeader");
			
			ImageAnchor btnDelete = new ImageAnchor(Theme.getTheme().standardIcons().delete(), new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					FieldEditor.this.removeFromParent();
					reptextEditor.refresh();
				}
			});
			btnDelete.setTitle(ProConstants.INSTANCE.removeField());
			btnDelete.setStyleName("btnDelete");
			titleHolder.add(btnDelete);
			
			tblIdLbl = new InlineLabel();
			titleHolder.add(tblIdLbl);
			
			ImageAnchor btnMoveUp = new ImageAnchor(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().arrowUp10Dis(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					changeFieldPosition(FieldEditor.this, DIRECTION_UP);
				}
			});
			btnMoveUp.setTitle(GeopediaTerms.INSTANCE.moveUp());
			ImageAnchor btnMoveDown = new ImageAnchor(com.sinergise.gwt.ui.resources.Theme.getTheme().standardIcons().arrowDown10Dis(), new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					changeFieldPosition(FieldEditor.this, DIRECTION_DOWN);					
				}
			});
			btnMoveUp.setStyleName("moveUp");
			btnMoveDown.setStyleName("moveDown");
			titleHolder.add(btnMoveUp);
			titleHolder.add(btnMoveDown);
			toggleAnchor = new Anchor();
			toggleAnchor.addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					expand(!editorHolder.isVisible());
				}
			});
			titleHolder.add(toggleAnchor);
			
			editorHolder = new SGFlowPanel("editorHolder");
			
			// name
			tbName = new SGTextBox();
			tbName.addValueChangeHandler(new ValueChangeHandler<String>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					toggleAnchor.setText(tbName.getValue());
					reptextEditor.refresh();
				}
			});
			editorHolder.add(createHolderPanel(GeopediaTerms.INSTANCE.name()+":", tbName,true));
			
			// type
			lbType = new ListBoxExt();
			for (FieldType ft:FieldType.values()) {
				if (ft.getIdentifier()>=FieldType.GEOMETRY.getIdentifier())
					break;
				lbType.addItem(getFieldTypeI18NName(ft), String.valueOf(ft.getIdentifier()));
			}
			editorHolder.add(createHolderPanel(GeopediaTerms.INSTANCE.type()+":", lbType));
			lbType.addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (getFieldType()==FieldType.FOREIGN_ID) {
						holderReferencedTable.setVisible(true);
					} else {
						holderReferencedTable.setVisible(false);
					}
					
				}
			});
			
			// description
			taDescription = new SGTextArea();
			editorHolder.add(createHolderPanel(GeopediaTerms.INSTANCE.description()+":", taDescription));
			
			
			
			
			refTableSelector = new ReferencedTableSelector();
			refTableSelector.addStyleName("referenceHolder");
			holderReferencedTable = createHolderPanel(GeopediaTerms.INSTANCE.refTable()+":", refTableSelector);
			editorHolder.add(holderReferencedTable);
			holderReferencedTable.setVisible(false);
			
			// visibility
			visibilityEditor = new EnumListBox<FieldVisibility>(FieldVisibility.values()) {
				public String getI18NLabelFor(Enum<FieldVisibility> enm) {
					return getFieldVisibilityI18N(enm);
				}
			};
			editorHolder.add(createHolderPanel(GeopediaTerms.INSTANCE.visibility()+":", visibilityEditor));
			// flags
			HashSet<FieldFlags> fullFlagsSet = FieldFlags.fullSet();
			fullFlagsSet.remove(FieldFlags.DELETED); // TODO: in the future enable administrators to see this so they can "undelete" the field
			flagsEditor = new EnumHashSetEditor<FieldFlags>(fullFlagsSet) {
				@Override
				public String getI18NLabelFor(Enum<FieldFlags> enm) {
					return getFieldFlagI18N(enm);
				}
			};
			flagsEditor.addStyleName("flagsEditor");
			editorHolder.add(createHolderPanel(GeopediaTerms.INSTANCE.flags()+":", flagsEditor));
			
			
			add(titleHolder);
			add(editorHolder);
			titleHolder.setVisible(true);
			editorHolder.setVisible(false);						
			
		}
		
		public void expand (boolean show) {
			if (show) {
				addStyleName("opened");
				editorHolder.setVisible(true);
			} else {
				removeStyleName("opened");
				editorHolder.setVisible(false);				
			}

		}
		private FieldType getFieldType() {
			return FieldType.forId(Integer.valueOf(lbType.getValue()));
		}
		
		public void setField(Field fld) {
			this.fld=fld;
			toggleAnchor.setText(fld.getName());
			tbName.setValue(fld.getName());
			taDescription.setValue(fld.descRawHtml);			
			lbType.setValue(String.valueOf(fld.type.getIdentifier()));
			flagsEditor.setValue(fld.getFieldFlags());
			visibilityEditor.setEnumValue(fld.getVisibility());
			Table refTable = fld.getReferencedTable();
			if (refTable==null && fld.refdTableId>0) {
				refTable = new Table();
				refTable.setId(fld.refdTableId);
			}
			if (refTable!=null) {
				refTableSelector.setValue(refTable);
				holderReferencedTable.setVisible(true);
				if (fld.hasValidId()) {
					refTableSelector.setEditable(false);
				}
			}
			if (fld.hasValidId()) { // updating
				lbType.setEnabled(false);
			} else { // new
				lbType.setEnabled(true);
			}
			if (fld.hasValidId()) {
				tblIdLbl.setText("f"+fld.getId());
			}
		}
		
		public Field getField() {
			fld.setName(tbName.getValue());
			fld.setDescription(taDescription.getValue());
			fld.setType(getFieldType());
			fld.setFieldFlags(flagsEditor.getValue());
			fld.setVisibility(visibilityEditor.getEnumValue());
			fld.setReferencedTable(refTableSelector.getValue());
			return fld;
		}
		
		public boolean validate() {
			for (int i=0;i<editorHolder.getWidgetCount();i++) {
				Widget w = editorHolder.getWidget(i);
				if (w instanceof HolderPanel) {
					((HolderPanel)w).missingMandatory(false);
				}
			}
			// validate name
			String name = tbName.getValue();
			if (StringUtil.isNullOrEmpty(name)) {
				HolderPanel holderPanel = (HolderPanel)tbName.getParent();
				holderPanel.missingMandatory(true);
				return false;
			}
			return true;
		}
	}

	
	
	protected class RepTextEditor extends FlowPanel {

		
		private ListBoxExt simpleEditor=null;
		private SGTextBox advEditor=null;
		
		public RepTextEditor() {
			buildSimpleEditor(null);
		}
		
		

		// TODO: fix this when we have new styles
		public void setValue(String textRepExpr) {	
			Integer fieldId = Table.getSingleFieldReptextId(textRepExpr);
			if (StringUtil.isNullOrEmpty(textRepExpr) || fieldId !=null) {
				buildSimpleEditor(fieldId);
				return;
			}
			buildAdvancedEditor(textRepExpr);
		}
		
		private void buildAdvancedEditor(String value) {
			if (advEditor!=null)
				return;
			clear();
			simpleEditor = null;
			advEditor = new SGTextBox();
			add(advEditor);
			advEditor.setValue(value);
		}
		
		private void buildSimpleEditor(Integer value) {
			if (simpleEditor==null) {
				clear();
				advEditor=null;
				simpleEditor = new ListBoxExt();	
				add(simpleEditor);
				if (ClientSession.getUser().isAdmin()) {
					ImageAnchor toggleRepBtn = new ImageAnchor(Theme.getTheme().standardIcons().edit());
					toggleRepBtn.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							RepTextEditor.this.clear();
							buildAdvancedEditor(getValue());
							RepTextEditor.this.getParent().getParent().addStyleName("editable");
						}
					});
					add(toggleRepBtn);
				}

			}
			refresh();
			if (value!=null) {
				simpleEditor.setValue(String.valueOf(value));
			}
			
			
		}
		
		public void refresh() {
			if (advEditor!=null) {
				return;
			} else {
				String currentValue = simpleEditor.getValue();
				simpleEditor.clear();
				for (int i=0;i<fieldsHolder.getWidgetCount();i++) {
					FieldEditor fe = (FieldEditor)fieldsHolder.getWidget(i);
					if (fe.validate()) {
						Field f = fe.getField();
						simpleEditor.addItem(f.getName(),String.valueOf(f.getId()));
					}
				}
				if (currentValue!=null) {
					simpleEditor.setValue(currentValue);
				}
			}
		}



		public String getValue() {
			if (simpleEditor!=null) {
				if (StringUtil.isNullOrEmpty(simpleEditor.getValue()))
					return null;
				return Table.createSingleFieldReptext(simpleEditor.getValue());
			} else {
				return advEditor.getValue();
			}
		}
		
	}
	
	private SGFlowPanel fieldsHolder;
	private RepTextEditor reptextEditor;
	private int newFieldId=-1;
	private SGTextBox advanceRepInput;

	public TableFieldsEditorPanel() {
		addStyleName("fieldsEditor");
		fieldsHolder = new SGFlowPanel();
		fieldsHolder.setStyleName("fieldsHolder");
		add(fieldsHolder);
		DecoratedAnchor btnAdd = new DecoratedAnchor(ProConstants.INSTANCE.addNewField(), Theme.getTheme().standardIcons().plus());
		btnAdd.setStyleName("dottedAnchor");
		btnAdd.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				onAddNewField();
			}
		});
		
		reptextEditor = new RepTextEditor();
		FlowPanel holderRepText = createHolderPanel(GeopediaTerms.INSTANCE.expressionForName()+":", reptextEditor);
		holderRepText.setStyleName("repTextEditor");
		Image tooltip = new Image(Theme.getTheme().standardIcons().help());
		tooltip.addStyleName("fl-right");
		HTML html = new HTML(ProMessages.INSTANCE.fieldsHelp()+"<div style=\"margin-top: 5px;\"></div>"+new Image(GeopediaProStyle.INSTANCE.fieldExample()));
		SGRichTooltipPopup.addWidgetTooltipPopup(tooltip, html, 
				new TooltipPosition(PositionType.RELATIVE_TO_WIDGET, 20),new TooltipPosition(PositionType.RELATIVE_TO_WIDGET, 0),500);
		
		holderRepText.insert(tooltip,0);
	
		
		advanceRepInput = new SGTextBox();
		advanceRepInput.setEmptyText(""); //fill in with existing field id
		advanceRepInput.setVisibleLength(50);
		advanceRepInput.setVisible(false);
		insert(holderRepText,0);
		add(advanceRepInput);
		add(btnAdd);
	}

	private void onAddNewField() {
		if (validate()) {
			
			reptextEditor.refresh();
			
			Field fld = new Field();
			fld.setId(newFieldId--);
			FieldEditor fe = new FieldEditor();
			fieldsHolder.add(fe);
			fe.setField(fld);
			fe.expand(true);
		}

	}
	private void changeFieldPosition(FieldEditor fieldEditor, int direction) {
		if (fieldsHolder.getWidgetIndex(fieldEditor)==-1)
			return;
		int currentWgIdx = fieldsHolder.getWidgetIndex(fieldEditor);
		if (currentWgIdx==0 && direction == DIRECTION_UP) // first element moving up
			return;
		if (currentWgIdx==(fieldsHolder.getWidgetCount()-1) && direction == DIRECTION_DOWN) // last element moving down
			return;
		fieldsHolder.remove(currentWgIdx);
		fieldsHolder.insert(fieldEditor, currentWgIdx+direction);
	}
	
	@Override
	public void loadEntity(Table table) {
		fieldsHolder.clear();
		for (Field f:table.fields) {
			FieldEditor fe = new FieldEditor();
			fieldsHolder.add(fe);
			fe.setField(f);
		}
		
		reptextEditor.setValue(table.getRepText());
	}
	
	@Override
	public boolean validate() {
		boolean validateOk = true;
		int fieldsCount = fieldsHolder.getWidgetCount();
		for (int i=0;i<fieldsCount;i++) {
			FieldEditor fe = (FieldEditor)fieldsHolder.getWidget(i);
			if (validateOk && !fe.validate()) {
				fe.expand(true);
				validateOk=false;
			} else { 
				fe.expand(false);
			}
		}
		if (fieldsCount>0 && StringUtil.isNullOrEmpty(reptextEditor.getValue())) {
			return false;
		}
		return validateOk;
	}

	@Override
	public boolean saveEntity(Table entity) {
		int fldCount = fieldsHolder.getWidgetCount();
		Field [] fields = new Field[fldCount];
		for (int i=0;i<fldCount;i++) {
			FieldEditor fEditor = (FieldEditor)fieldsHolder.getWidget(i);
			if (!fEditor.validate()) {
				
			}
			fields[i]=fEditor.getField();
			fields[i].setOrder(i);
		}
		entity.fields=fields;
		entity.setRepText(reptextEditor.getValue());
		return true;
	}

}
