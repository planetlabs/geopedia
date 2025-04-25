package com.sinergise.geopedia.pro.client.ui.table;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.ui.widgets.ReferencedTableLookup;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.query.filter.FieldDescriptor;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor.MetaFieldType;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.table.FeatureDataGrid.AbstractFilterElement;
import com.sinergise.geopedia.pro.theme.dialogs.ProDialogsStyle;
import com.sinergise.gwt.ui.ListBoxExt;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.editor.BooleanEditor;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.editor.IntegerEditor;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.ui.resources.Theme;

class TableFilterBuilderPanel extends AbstractDialogBox {
	
	ArrayList<Widget> arrayTB = new ArrayList<Widget>();
	Table table = null;
	
	protected static OperatorListBox buildNumberScalarOperationCombo () {
		OperatorListBox lb = new OperatorListBox();
		lb.addItem("=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_EQUALTO));
		lb.addItem(">", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN));
		lb.addItem(">=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN_EQUALTO));
		lb.addItem("<", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_LESSTHAN));
		lb.addItem("<=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_LESSTHAN_EQUALTO));
		lb.addItem("!=", String.valueOf(FilterCapabilities.SCALAR_OP_COMP_NOTEQUALTO));
		return lb;	
	}
	
	protected static OperatorListBox buildTextScalarOperationCombo () {
		OperatorListBox lb = new OperatorListBox();
		lb.addItem(GeopediaTerms.INSTANCE.contains(), String.valueOf(FilterCapabilities.SCALAR_OP_COMP_LIKE));
		lb.addItem(GeopediaTerms.INSTANCE.equals(), String.valueOf(FilterCapabilities.SCALAR_OP_COMP_EQUALTO));
		return lb;	
	}
	
	protected static OperatorListBox buildForeignIDOperationCombo () {
		OperatorListBox lb = new OperatorListBox();
		lb.addItem(GeopediaTerms.INSTANCE.equals(), String.valueOf(FilterCapabilities.SCALAR_OP_COMP_EQUALTO));
		return lb;	
	}
	
	protected static OperatorListBox buildBooleanOperationCombo () {
		OperatorListBox lb = new OperatorListBox();
		lb.addItem(GeopediaTerms.INSTANCE.equals(), String.valueOf(FilterCapabilities.SCALAR_OP_COMP_EQUALTO));
		return lb;	
	}
	
	
	public interface HasOperator{
		public int getOperator();
	}

	
	private static class OperatorListBox extends ListBoxExt implements HasOperator {

		@Override
		public int getOperator() {
			return Integer.valueOf(getValue()).intValue();		
		}
	}
	
	private class MetaFieldElement extends FlowPanel implements AbstractFilterElement {
		MetaFieldType mfType;
		int tableId;
		HasOperator operator;
		SGTextBox inputText = null;
		IntegerEditor inputInteger = null;
		
		public MetaFieldElement(MetaFieldType mfType, int tableId) {
			this.tableId=tableId;
			this.mfType = mfType;
			add(new InlineLabel(getI18NForMetaFieldType(mfType)));
			switch (mfType) {
				case IDENTIFIER:
					add((Widget) (operator = buildNumberScalarOperationCombo()));
					inputInteger = new IntegerEditor();
					inputInteger.addKeyPressHandler(enterKey);
					add(inputInteger);
					arrayTB.add(inputInteger);
					break;
				case FULLTEXT:
					add((Widget) (operator = buildTextScalarOperationCombo()));
					inputText = new IntegerEditor();
					inputText.addKeyPressHandler(enterKey);
					add(inputText);
					arrayTB.add(inputText);
					break;
			}
			
		}
		
		private String getI18NForMetaFieldType(MetaFieldType mfType) {
			switch (mfType) {
				case DELETED:
					return ProConstants.INSTANCE.deleted();
				case FULLTEXT:
					return ProConstants.INSTANCE.fullText();
				case IDENTIFIER:
					return ProConstants.INSTANCE.identifier();
			}
			return mfType.name();
		}
		@Override
		public FilterDescriptor getFilterDescriptor() {
			switch (mfType) {
				case IDENTIFIER:
					if (inputInteger.getEditorValue()!=null){
						return new ComparisonOperation(					
								new TableMetaFieldDescriptor(mfType, tableId),
								operator.getOperator(), Literal.newInstance(inputInteger.getEditorValue()
										.longValue()));
					} 
					break;
				case FULLTEXT:
					String value = inputText.getValue();
					if (!StringUtil.isNullOrEmpty(value)) {
						int op = operator.getOperator();
						if (op == FilterCapabilities.SCALAR_OP_COMP_LIKE) {
							value="%"+value+"%";
						}
						return new ComparisonOperation(					
								new TableMetaFieldDescriptor(mfType, tableId),
								operator.getOperator(), Literal.newInstance(new TextProperty(value))
								);
					}
					break;
				}
			return null;
		}
		
		public void clearFields() {
			inputText.setNormalText("");
			inputInteger.setNormalText("");
		}
		
	}
	
//	private class IsDeletedElement extends FlowPanel implements AbstractFilterElement {
//		CheckBox cbShowDeleted;
//		TableMetaFieldDescriptor deletedPD;
//		public IsDeletedElement(Table table) {
//			deletedPD = new TableMetaFieldDescriptor(TableMetaFieldDescriptor.MetaFieldType.DELETED, table.getId());
//			cbShowDeleted = new CheckBox(LightMessages.INSTANCE.showDeleted());
//			add(cbShowDeleted);			
//		}
//		@Override
//		public FilterDescriptor getFilterDescriptor() {
//			if (!cbShowDeleted.getValue()) {
//				return new ComparisonOperation(
//						deletedPD,
//						FilterCapabilities.SCALAR_OP_COMP_EQUALTO,
//						Literal.newInstance(false)
//				);
//			}
//			return null;
//		}
//		
//	}
	
	KeyPressHandler enterKey = new KeyPressHandler() {
		@Override
		public void onKeyPress(KeyPressEvent event) {
			int charCode = event.getUnicodeCharCode();
			if (charCode==0) {
				int keyCode = event.getNativeEvent().getKeyCode();
				if (keyCode == KeyCodes.KEY_ENTER) {
					onFilterChanged();
					return;
				}
			}
			if (event.getCharCode() == KeyCodes.KEY_ENTER) {
				onFilterChanged();
			}
		}
	};
	
	private class FieldFilterElement extends FlowPanel implements AbstractFilterElement {

		ListBoxExt scalarOp = null;
		SGTextBox inputText = null;
		BooleanEditor inputBoolean = null;
		DoubleEditor inputDouble = null;
		IntegerEditor inputInteger = null;
		ReferencedTableLookup inputForeignId = null;
		Field field;
		
		

		public FieldFilterElement(Field f) {
			this.field = f;
			add(new InlineLabel(f.getName()));
			switch (f.type) {
			case INTEGER:
				scalarOp = buildNumberScalarOperationCombo();
				add(scalarOp);
				inputInteger = new IntegerEditor();
				inputInteger.addKeyPressHandler(enterKey);
				add(inputInteger);
				arrayTB.add(inputInteger);
				break;
			case DECIMAL:
				scalarOp = buildNumberScalarOperationCombo();
				add(scalarOp);
				inputDouble = new DoubleEditor();
				inputDouble.addKeyPressHandler(enterKey);
				add(inputDouble);
				arrayTB.add(inputDouble);
				break;
			case PLAINTEXT:
			case LONGPLAINTEXT:
			case WIKITEXT:
				scalarOp = buildTextScalarOperationCombo();
				add(scalarOp);
				inputText = new SGTextBox();
				arrayTB.add(inputText);
				inputText.addKeyPressHandler(enterKey);
				add(inputText);
				break;
			case FOREIGN_ID:
				scalarOp = buildForeignIDOperationCombo();
				add(scalarOp);
				inputForeignId = new ReferencedTableLookup(f);
				inputForeignId.addStyleName("foreignId");
				arrayTB.add(inputForeignId);
				add(inputForeignId);
				break;
			case BOOLEAN: 
				scalarOp = buildBooleanOperationCombo();
				add(scalarOp);
				inputBoolean = new BooleanEditor(null, true);
				inputBoolean.addStyleName("foreignId");
				arrayTB.add(inputBoolean);
				add(inputBoolean);
				break;
			}
			
		}

		@Override
		public FilterDescriptor getFilterDescriptor() {
			if (inputText != null && !StringUtil.isNullOrEmpty(inputText.getValue())) {
				String value = inputText.getValue();
				int operator = Integer.valueOf(scalarOp
						.getValue());
				if (operator == FilterCapabilities.SCALAR_OP_COMP_LIKE) {
					value="%"+value+"%";
				}
				return new ComparisonOperation(
							FieldDescriptor.newInstance(field),
							operator,
							Literal.newInstance(new TextProperty(value))
							);
				
				
			} else if (inputInteger != null && inputInteger.getEditorValue() != null) {
				return new ComparisonOperation(FieldDescriptor.newInstance(field), Integer.valueOf(scalarOp
						.getValue()), Literal.newInstance(inputInteger.getEditorValue()
						.longValue()));
			} else if (inputDouble != null && inputDouble.getEditorValue() != null) {
				return new ComparisonOperation(FieldDescriptor.newInstance(field), Integer.valueOf(scalarOp
						.getValue()), Literal.newInstance(new DoubleProperty(inputDouble.getEditorValue())));
			} else if (inputForeignId != null && inputForeignId.getEditorValue() != null) {
				return new ComparisonOperation(FieldDescriptor.newInstance(field), Integer.valueOf(scalarOp
						.getValue()), Literal.newInstance(new LongProperty(inputForeignId.getEditorValue())));
			} else if (inputBoolean != null && inputBoolean.getEditorValue() != null) {
				return new ComparisonOperation(FieldDescriptor.newInstance(field), Integer.valueOf(scalarOp
						.getValue()), Literal.newInstance(new BooleanProperty(inputBoolean.getEditorValue())));
			}
			return null;
		}

	}

	ArrayList<AbstractFilterElement> elements = new ArrayList<AbstractFilterElement>();
	
	public FilterDescriptor getFilterDescriptor() {
		ArrayList<ExpressionDescriptor> fdList = new ArrayList<ExpressionDescriptor>();
		for (AbstractFilterElement el:elements) {
			ExpressionDescriptor fd = el.getFilterDescriptor();
			if (fd!=null) {
				fdList.add(fd);
			}
		}
		fdList.add(new ComparisonOperation( new TableMetaFieldDescriptor(TableMetaFieldDescriptor.MetaFieldType.DELETED, table.getId()),
				FilterCapabilities.SCALAR_OP_COMP_EQUALTO,
				Literal.newInstance(false)));
		FilterDescriptor filter = null; 
		
		if (fdList.size()>1) {
			filter = new LogicalOperation(fdList.toArray(new ExpressionDescriptor[fdList.size()])
					, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
		}else if (fdList.size()==1) {
			filter = (FilterDescriptor) fdList.get(0);
		}
		return filter;
	}
	
	
	
	public TableFilterBuilderPanel(Table table) {
		super(false, true, false, true);
		this.table=table;
		ProDialogsStyle.INSTANCE.filterPopup().ensureInjected();
		
		FlowPanel pnlFilters = new FlowPanel();
		pnlFilters.setStyleName("filtersPanel");
		
		MetaFieldElement idMFFilter = new MetaFieldElement(MetaFieldType.IDENTIFIER, table.getId());
		pnlFilters.add(idMFFilter);
		elements.add(idMFFilter);
		
		for (Field f : table.getFields()) {
			if (!f.getVisibility().canView())
				continue;
			switch (f.type) {
			case BOOLEAN:
			case FOREIGN_ID:
			case INTEGER:
			case DECIMAL:
			case PLAINTEXT:
			case LONGPLAINTEXT:
			case WIKITEXT:
				FieldFilterElement ffe = new FieldFilterElement(f);
				pnlFilters.add(ffe);
				elements.add(ffe);
				break;
			}
		}
		// disabled for now
//		IsDeletedElement elDeleted = new IsDeletedElement(table);
//		pnlFilters.add(elDeleted);
//		elements.add(elDeleted);
		
		FlowPanel btnPanel = new FlowPanel();
		btnPanel.setStyleName("btnPanel");
		SGPushButton btnOk = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonConfirm(),Theme.getTheme().standardIcons().ok(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
				onFilterChanged();
			}
		});
		btnPanel.add(btnOk);

		Anchor btnCancel = new Anchor(ProConstants.INSTANCE.clear());
		btnCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				for(Widget w : arrayTB) {
					if(w instanceof SGTextBox){
						((SGTextBox)w).setNormalText("");
					} else if(w instanceof ReferencedTableLookup){
						((ReferencedTableLookup)w).setEditorValue(null);
					} else if(w instanceof BooleanEditor){
						((BooleanEditor)w).setEditorValue(null);
					}
				}
			}
		});
		btnPanel.add(btnCancel);
		FlowPanel container = new FlowPanel();
		container.add(createCloseButton());
		container.add(pnlFilters);
		container.add(btnPanel);
		setWidget(container);
	}
	
	protected void onFilterChanged() {}
	
	@Override
	protected boolean dialogResizePending(int width, int height) {
		return true;
	}
}